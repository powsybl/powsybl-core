package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Map;

public interface ReportNode {

    String REPORT_MESSAGE_NODE_TYPE = "reportMessage";
    String REPORTER_NODE_TYPE = "reporter";

    void writeJson(JsonGenerator generator, Map<String, String> dictionary) throws IOException;

    String getKey();

    String getDefaultText();

    Map<String, TypedValue> getValues();

    TypedValue getValue(String valueKey);
}
