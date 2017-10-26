package com.secureskytech.msgpackjsonproxy.sampledata;

import java.io.IOException;

import com.secureskytech.msgpackjsonproxy.MessagePackJsonConverter;

public class GenerateTypicalJsonData {

    final String toplevel;

    public GenerateTypicalJsonData(final String toplevel) {
        switch (toplevel) {
        case "array":
        case "map":
            this.toplevel = toplevel;
            break;
        default:
            throw new IllegalArgumentException("Unknown tolevel. use array/map.");
        }
    }

    public String create() throws IOException {
        MessagePackJsonConverter converter = new MessagePackJsonConverter();
        if ("map".equals(this.toplevel)) {
            return converter.object2json(DemoData.asMap());
        } else {
            return converter.object2json(DemoData.asList());
        }
    }
}
