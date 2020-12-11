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
    private final JsonNetwork network;

    JsonModel(JsonNetwork network) {
        this.network = network;
    }

    public JsonNetwork getNetwork() {
        return this.network;
    }

    public static class JsonNetwork {
        private ArrayData caseid;
        private TableData bus;
        private TableData load;
        private TableData fixshunt;
        private TableData generator;
        private TableData acline;
        private TableData transformer;
        private TableData area;
        private TableData zone;
        private TableData owner;

        void setCaseid(ArrayData caseid) {
            this.caseid = caseid;
        }

        public ArrayData getCaseid() {
            return this.caseid;
        }

        void setBus(TableData bus) {
            this.bus = bus;
        }

        public TableData getBus() {
            return this.bus;
        }

        void setLoad(TableData load) {
            this.load = load;
        }

        public TableData getLoad() {
            return this.load;
        }

        void setFixshunt(TableData fixshunt) {
            this.fixshunt = fixshunt;
        }

        public TableData getFixshunt() {
            return this.fixshunt;
        }

        void setGenerator(TableData generator) {
            this.generator = generator;
        }

        public TableData getGenerator() {
            return this.generator;
        }

        void setAcline(TableData acline) {
            this.acline = acline;
        }

        public TableData getAcline() {
            return this.acline;
        }

        void setTransformer(TableData transformer) {
            this.transformer = transformer;
        }

        public TableData getTransformer() {
            return this.transformer;
        }

        void setArea(TableData area) {
            this.area = area;
        }

        public TableData getArea() {
            return this.area;
        }

        void setZone(TableData zone) {
            this.zone = zone;
        }

        public TableData getZone() {
            return this.zone;
        }

        void setOwner(TableData owner) {
            this.owner = owner;
        }

        public TableData getOwner() {
            return this.owner;
        }
    }

    @JsonPropertyOrder({"fields", "data"})
    public static class TableData {
        // XXX(Luma) We should remove the quoting from field names
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
