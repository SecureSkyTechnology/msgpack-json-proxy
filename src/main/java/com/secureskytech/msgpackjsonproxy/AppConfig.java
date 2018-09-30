package com.secureskytech.msgpackjsonproxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

import com.google.common.base.Strings;

public class AppConfig {
    public static final String KEY_M2J_PORT = "msgpack2json.port";
    public static final String KEY_M2J_UPSTREAM = "msgpack2json.upstream";
    public static final String KEY_M2J_ENABLE_RESCON = "msgpack2json.enable_response_conversion";
    public static final String KEY_J2M_PORT = "json2msgpack.port";
    public static final String KEY_J2M_UPSTREAM = "json2msgpack.upstream";
    public static final String KEY_J2M_ENABLE_RESCON = "json2msgpack.enable_response_conversion";
    public static final String KEY_DEMO_PORT_HTTP = "demo.port.http";
    public static final String KEY_DEMO_PORT_HTTPS = "demo.port.https";

    final Properties prop;
    final int m2jport;
    final InetSocketAddress m2jUpstream;
    final boolean m2jEnableResponseConversion;
    final int j2mport;
    final InetSocketAddress j2mUpstream;
    final boolean j2mEnableResponseConversion;
    final int demoHttpPort;
    final int demoHttpsPort;

    public static String configSample() {
        StringBuilder sb = new StringBuilder();
        sb.append("#----(starting \"#\" means comment line)----\r\n");
        sb.append("## msgpack -> json proxy listening port number\r\n");
        sb.append(KEY_M2J_PORT + " = 8181\r\n");
        sb.append("## msgpack -> json proxy upstream <host>:<port> (comment out if you don't set upstream proxy)\r\n");
        sb.append(KEY_M2J_UPSTREAM + " = localhost:8080\r\n");
        sb.append("## msgpack -> json proxy enable response conversion (json -> msgpack)\r\n");
        sb.append("## (comment out or set 'false' if you don't need it)\r\n");
        sb.append(KEY_M2J_ENABLE_RESCON + " = true\r\n");
        sb.append("## json -> msgpack proxy listening port number\r\n");
        sb.append(KEY_J2M_PORT + " = 8182\r\n");
        sb.append("## json -> msgpack proxy upstream <host>:<port> (comment out if you don't set upstream proxy)\r\n");
        sb.append(KEY_J2M_UPSTREAM + " = localhost:8088\r\n");
        sb.append("## json -> msgpack proxy enable response conversion (msgpack -> json)\r\n");
        sb.append("## (comment out or set 'false' if you don't need it)\r\n");
        sb.append(KEY_J2M_ENABLE_RESCON + " = true\r\n");
        sb.append("## demo web server listening port (http)\r\n");
        sb.append(KEY_DEMO_PORT_HTTP + " = 8183\r\n");
        sb.append("## demo web server listening port (https)\r\n");
        sb.append(KEY_DEMO_PORT_HTTPS + " = 8184\r\n");
        sb.append("#----(end)----\r\n");
        return sb.toString();
    }

    public AppConfig(final File propFile) throws IOException {
        this.prop = new Properties();
        try (InputStream ins = new FileInputStream(propFile)) {
            prop.load(ins);
        }
        this.m2jport = Integer.parseInt(prop.getProperty(KEY_M2J_PORT));
        this.j2mport = Integer.parseInt(prop.getProperty(KEY_J2M_PORT));
        final String m2jupstream = prop.getProperty(KEY_M2J_UPSTREAM);
        if (Strings.isNullOrEmpty(m2jupstream)) {
            this.m2jUpstream = null;
        } else {
            String[] hostport = m2jupstream.split(":");
            m2jUpstream = new InetSocketAddress(hostport[0], Integer.parseInt(hostport[1]));
        }
        final String m2jenablerescon = prop.getProperty(KEY_M2J_ENABLE_RESCON);
        if (Strings.isNullOrEmpty(m2jenablerescon)) {
            this.m2jEnableResponseConversion = false;
        } else {
            this.m2jEnableResponseConversion = Boolean.parseBoolean(m2jenablerescon);
        }
        final String j2mupstream = prop.getProperty(KEY_J2M_UPSTREAM);
        if (Strings.isNullOrEmpty(j2mupstream)) {
            this.j2mUpstream = null;
        } else {
            String[] hostport = j2mupstream.split(":");
            j2mUpstream = new InetSocketAddress(hostport[0], Integer.parseInt(hostport[1]));
        }
        final String j2menablerescon = prop.getProperty(KEY_J2M_ENABLE_RESCON);
        if (Strings.isNullOrEmpty(j2menablerescon)) {
            this.j2mEnableResponseConversion = false;
        } else {
            this.j2mEnableResponseConversion = Boolean.parseBoolean(j2menablerescon);
        }
        this.demoHttpPort = Integer.parseInt(prop.getProperty(KEY_DEMO_PORT_HTTP));
        this.demoHttpsPort = Integer.parseInt(prop.getProperty(KEY_DEMO_PORT_HTTPS));
    }

    public int getMsgpack2JsonPort() {
        return m2jport;
    }

    public InetSocketAddress getMsgpack2JsonUpstream() {
        return m2jUpstream;
    }

    public boolean isEnableMsgpack2JsonResponseConversion() {
        return m2jEnableResponseConversion;
    }

    public int getJson2MsgpackPort() {
        return j2mport;
    }

    public InetSocketAddress getJson2MsgpackUpstream() {
        return j2mUpstream;
    }

    public boolean isEnableJson2MsgpackResponseConversion() {
        return j2mEnableResponseConversion;
    }

    public int getDemoHttpPort() {
        return demoHttpPort;
    }

    public int getDemoHttpsPort() {
        return demoHttpsPort;
    }
}
