package com.secureskytech.msgpackjsonproxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.littleshoot.proxy.HttpProxyServer;

import com.google.common.io.Files;
import com.secureskytech.msgpackjsonproxy.proxy.MessagePackJsonProxy;
import com.secureskytech.msgpackjsonproxy.sampledata.GenerateTypicalJsonData;
import com.secureskytech.msgpackjsonproxy.sampledata.GenerateTypicalMessagePackData;
import com.secureskytech.msgpackjsonproxy.web.DemoHttpServer;

public class Main {

    static void usage() {
        System.out.println("usage: ");
        System.out.println("start               [config file]        : start proxy and demo servers (Ctrl-C to quit)");
        System.out.println("gen_typical_msgpack [sequence|array|map] : output typical msgpack data to stdout");
        System.out.println("gen_typical_json    [array|map]          : output typical json    data to stdout");
        System.out.println("msgpack2json        [msgpack filename]   : convert msgpack to json");
        System.out.println("json2msgpack        [json filename]      : convert json to msgpack");
        System.out.println("json2msgpack2       [json filename]      : convert array json to sequenced msgpack");
        System.out.println("dump_msgpack        [msgpack filename]   : dump msgpack object");
        System.out.println("dump_json           [json filename]      : dump json object");
        System.out.println("dump_hex            [filename]           : hex dump (lowercase, separated by 0x20)");
        System.out.println("\r\n[config file] example");
        System.out.println(AppConfig.configSample());
    }

    public static void main(String args[]) throws Exception {
        if (args.length < 2) {
            usage();
            System.out.print("Enter component and args (Ctrl-C or ENTER to quit): ");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                args = br.readLine().trim().split("\\s");
                // call invoke() before closing System.in for console client
                invoke(args);
            }
        } else {
            invoke(args);
        }
    }

    static void invoke(String[] args) throws Exception {
        if (args.length < 2) {
            System.exit(-1);
        }
        MessagePackJsonConverter converter = new MessagePackJsonConverter();
        MsgpackedObjectDumper objdumper = new MsgpackedObjectDumper(System.out);
        switch (args[0]) {
        case "start":
            startProxies(new AppConfig(new File(args[1])));
            break;
        case "gen_typical_msgpack":
            GenerateTypicalMessagePackData gtmpd = new GenerateTypicalMessagePackData(args[1]);
            System.out.write(gtmpd.create());
            System.out.flush();
            break;
        case "gen_typical_json":
            GenerateTypicalJsonData gtjd = new GenerateTypicalJsonData(args[1]);
            System.out.print(gtjd.create());
            break;
        case "msgpack2json":
            byte[] srcmsgpack = Files.toByteArray(new File(args[1]));
            String dstjson = converter.msgpack2json(srcmsgpack);
            if (converter.isSequence()) {
                System.err.println("## sequence data ##");
            }
            System.out.print(dstjson);
            break;
        case "json2msgpack":
            String srcjson = Files.toString(new File(args[1]), StandardCharsets.UTF_8);
            byte[] dstmsgpack = converter.json2msgpack(srcjson, false);
            System.out.write(dstmsgpack);
            System.out.flush();
            break;
        case "json2msgpack2":
            String srcjson2 = Files.toString(new File(args[1]), StandardCharsets.UTF_8);
            byte[] dstmsgpack2 = converter.json2msgpack(srcjson2, true);
            System.out.write(dstmsgpack2);
            System.out.flush();
            break;
        case "dump_msgpack":
            byte[] srcmsgpack1 = Files.toByteArray(new File(args[1]));
            Object o1 = converter.msgpack2object(srcmsgpack1);
            if (converter.isSequence()) {
                System.err.println("## sequence data ##");
            }
            objdumper.dump(o1);
            break;
        case "dump_json":
            String srcjson3 = Files.toString(new File(args[1]), StandardCharsets.UTF_8);
            Object o2 = converter.json2object(srcjson3);
            objdumper.dump(o2);
            break;
        case "dump_hex":
            byte[] srchex = Files.toByteArray(new File(args[1]));
            HexDumper hexDumper = new HexDumper();
            hexDumper.setPrefix("");
            hexDumper.setSeparator(" ");
            hexDumper.setToUpperCase(false);
            System.out.println(hexDumper.dump(srchex));
            break;
        default:
            System.exit(-1);
        }
    }

    static void startProxies(AppConfig appConfig) throws Exception {
        HttpProxyServer msgpack2jsonProxy =
            MessagePackJsonProxy.createMsgpack2JsonProxy(
                appConfig.getMsgpack2JsonPort(),
                appConfig.getMsgpack2JsonUpstream(),
                appConfig.isEnableMsgpack2JsonResponseConversion());
        HttpProxyServer json2msgpackProxy =
            MessagePackJsonProxy.createJson2MsgpackProxy(
                appConfig.getJson2MsgpackPort(),
                appConfig.getJson2MsgpackUpstream(),
                appConfig.isEnableJson2MsgpackResponseConversion());

        DemoHttpServer httpDemoServer = DemoHttpServer.createHTTP(appConfig.getDemoHttpPort());
        httpDemoServer.start();
        DemoHttpServer sslDemoServer = DemoHttpServer.createHTTPS(appConfig.getDemoHttpsPort());
        sslDemoServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                msgpack2jsonProxy.stop();
                json2msgpackProxy.stop();
                httpDemoServer.shutdown();
                sslDemoServer.shutdown();
            }
        });
    }
}
