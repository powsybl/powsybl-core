package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

public interface MessageNode {

    String getKey();

    String getDefaultText();

    Map<String, TypedValue> getValues();

    TypedValue getValue(String valueKey);

    Collection<MessageNode> getChildren();

    void writeJson(JsonGenerator generator, Map<String, String> dictionary) throws IOException;

    void print(Writer writer, String indent, Map<String, TypedValue> inheritedValueMap) throws IOException;
}
