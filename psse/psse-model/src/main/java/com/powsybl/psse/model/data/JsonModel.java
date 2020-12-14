/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class JsonModel {
    @JsonPropertyOrder({"fields", "data"})
    public static class TableData {
        // XXX(Luma) We should be able to remove the addQuote from field names
        // It is required only to keep all field names in the same line
        // Should be solved by using different settings to serialize over destination output
        // In fact this whole class should be removed
        // It is dealing only with proper formatting of the output
        @JsonRawValue
        @JsonProperty("fields")
        private final List<String> quotedFields;
        private final List<String> data;

        TableData(String[] fields, List<String> data) {
            this.quotedFields = addQuote(fields);
            this.data = addSquareBrackets(data);
        }

        public List<String> getQuotedFields() {
            return this.quotedFields;
        }

        public List<String> getData() {
            return this.data;
        }

        private static List<String> addSquareBrackets(List<String> stringList) {
            List<String> bracketList = new ArrayList<>();
            stringList.forEach(s -> bracketList.add("[" + s + "]"));
            return bracketList;
        }
    }

    @JsonPropertyOrder({"fields", "data"})
    public static class ArrayData {
        @JsonRawValue
        @JsonProperty("fields")
        private final List<String> quotedFields;
        @JsonRawValue
        private final List<String> data;

        ArrayData(String[] fields, List<String> data) {
            this.quotedFields = addQuote(fields);
            this.data = data;
        }

        public List<String> getQuotedFields() {
            return this.quotedFields;
        }

        public List<String> getData() {
            return this.data;
        }
    }

    // null if this block have not read
    private static List<String> addQuote(String[] fields) {
        List<String> list = new ArrayList<>();
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                list.add("\"" + fields[i] + "\"");
            }
        }
        return list;
    }
}
