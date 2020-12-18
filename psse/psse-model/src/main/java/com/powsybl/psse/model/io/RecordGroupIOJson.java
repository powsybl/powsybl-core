/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.io;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.PowsyblException;
import com.powsybl.psse.model.PsseException;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.powsybl.psse.model.io.RecordGroupIdentification.JsonObjectType.DATA_TABLE;
import static com.powsybl.psse.model.io.RecordGroupIdentification.JsonObjectType.PARAMETER_SET;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class RecordGroupIOJson<T> implements RecordGroupIO<T> {
    private final AbstractRecordGroup<T> recordGroup;

    public RecordGroupIOJson(AbstractRecordGroup<T> recordGroup) {
        Objects.requireNonNull(recordGroup);
        this.recordGroup = recordGroup;
    }

    @Override
    public List<T> read(BufferedReader reader, Context context) throws IOException {
        if (reader == null) {
            return readJson(context.getNetworkNode(), context);
        }
        // Use Jackson streaming API to skip contents until wanted node is found
        JsonFactory jsonFactory = new JsonFactory();
        try (JsonParser parser = jsonFactory.createParser(reader)) {
            JsonNode node = readJsonNode(parser);
            String[] actualFieldNames = readFieldNames(node);
            List<String> records = readRecords(node);
            context.setFieldNames(recordGroup.identification, actualFieldNames);
            return recordGroup.parseRecords(records, actualFieldNames, context);
        }
    }

    @Override
    public void write(List<T> psseObjects, Context context, OutputStream outputStream) {
        // For generating a proper output
        // We ignore the outputStream and use a JsonGenerator that has been put in the context
        if (outputStream != null) {
            throw new PsseException("Should write to Json generator instead of outputStream");
        }
        String[] headers = context.getFieldNames(recordGroup.identification);
        String[] actualQuotedFields = Util.retainAll(recordGroup.quotedFields(), headers);
        List<String> records = recordGroup.buildRecords(psseObjects, headers, actualQuotedFields, context);
        write(headers, records, context.getJsonGenerator());
    }

    @Override
    public T readHead(BufferedReader reader, Context context) throws IOException {
        throw new PsseException("Generic record group can not be read as head record");
    }

    @Override
    public void writeHead(T psseObject, Context context, OutputStream outputStream) {
        throw new PsseException("Generic record group can not be written as head record");
    }

    private JsonNode readJsonNode(JsonParser parser) throws IOException {
        Objects.requireNonNull(parser);
        ObjectMapper mapper = new ObjectMapper();
        parser.setCodec(mapper);
        String nodeName = recordGroup.getIdentification().getJsonNodeName();
        while (!parser.isClosed()) {
            parser.nextToken();
            if (nodeName.equals(parser.getCurrentName())) {
                return mapper.convertValue(parser.readValueAsTree().get("caseid"), JsonNode.class);
            }
        }
        throw new PsseException("Json node not found: " + nodeName);
    }

    public List<T> readJson(JsonNode networkNode, Context context) {
        // Records in Json file format have arbitrary order for fields.
        // Fields present in the record group are defined explicitly in a header.
        // Order and number of field names is relevant for parsing,
        // the field names must be taken from the explicit header defined in the file.
        // We store the "actual" field names in the context for potential later use.

        JsonNode jsonNode = networkNode.get(recordGroup.getIdentification().getJsonNodeName());
        if (jsonNode == null) {
            return new ArrayList<>();
        }
        String[] actualFieldNames = readFieldNames(jsonNode);
        List<String> records = readRecords(jsonNode);
        context.setFieldNames(recordGroup.getIdentification(), actualFieldNames);
        return recordGroup.parseRecords(records, actualFieldNames, context);
    }

    static String[] readFieldNames(JsonNode n) {
        JsonNode fieldsNode = n.get("fields");
        if (!fieldsNode.isArray()) {
            throw new PowsyblException("Expecting array reading fields");
        }
        List<String> fields = new ArrayList<>();
        for (JsonNode f : fieldsNode) {
            fields.add(f.asText());
        }
        return fields.toArray(new String[0]);
    }

    private List<String> readRecords(JsonNode n) {
        JsonNode dataNode = n.get("data");
        if (!dataNode.isArray()) {
            throw new PowsyblException("Expecting array reading data");
        }
        List<String> records = new ArrayList<>();
        switch (recordGroup.getIdentification().getJsonObjectType()) {
            case PARAMETER_SET:
                records.add(StringUtils.substringBetween(dataNode.toString(), "[", "]"));
                break;
            case DATA_TABLE:
                for (JsonNode r : dataNode) {
                    records.add(StringUtils.substringBetween(r.toString(), "[", "]"));
                }
                break;
            default:
                throw new PsseException("Unsupported Json object type " + recordGroup.getIdentification().getJsonObjectType());
        }
        return records;
    }

    private void write(String[] fields, List<String> data, JsonGenerator g) {
        if (fields == null || data == null || fields.length == 0 || data.isEmpty()) {
            return;
        }
        // If we have a default pretty printer we will adjust it to write differently
        // for Parameter Sets and Data Tables
        DefaultPrettyPrinter dpp = null;
        PrettyPrinter pp = g.getPrettyPrinter();
        if (pp instanceof DefaultPrettyPrinter) {
            dpp = (DefaultPrettyPrinter) pp;
        }
        try {
            g.writeFieldName(recordGroup.identification.getJsonNodeName());

            // Fields
            g.writeStartObject();
            // List of fields is pretty printed in a single line
            if (dpp != null) {
                dpp.indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance);
            }
            g.writeArrayFieldStart("fields");
            for (String field : fields) {
                g.writeString(field);
            }
            g.writeEndArray();

            // Data is pretty printed depending on type of record group
            // Table Data objects write every record in a separate line
            if (dpp != null) {
                dpp.indentArraysWith(recordGroup.identification.getJsonObjectType() == PARAMETER_SET
                    ? DefaultPrettyPrinter.FixedSpaceIndenter.instance
                    : DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            }
            g.writeArrayFieldStart("data");
            for (String s : data) {
                if (recordGroup.identification.getJsonObjectType() == DATA_TABLE) {
                    g.writeStartArray();
                }
                g.writeRaw(" ");
                g.writeRaw(s);
                if (recordGroup.identification.getJsonObjectType() == DATA_TABLE) {
                    g.writeEndArray();
                }
            }
            g.writeEndArray();

            g.writeEndObject();
            g.flush();
        } catch (IOException e) {
            throw new PsseException("Writing PSSE", e);
        }
    }
}
