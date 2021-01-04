/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.RecordGroupIdentification;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public enum PowerFlowRecordGroup implements RecordGroupIdentification {
    CASE_IDENTIFICATION("caseid"),
    SYSTEM_WIDE("?"),
    BUS("bus"),
    LOAD("load"),
    FIXED_BUS_SHUNT("fixshunt", "FIXED SHUNT"),
    GENERATOR("generator"),
    NON_TRANSFORMER_BRANCH("acline", "BRANCH"),
    SYSTEM_SWITCHING_DEVICE("sysswd", "SYSTEM SWITCHING DEVICE"),
    TRANSFORMER("transformer"),
    // Transformer record group includes two and three winding transformers
    TRANSFORMER_2("transformer2"),
    TRANSFORMER_3("transformer3"),
    AREA_INTERCHANGE("area", "AREA"),
    TWO_TERMINAL_DC_TRANSMISSION_LINE("twotermdc", "TWO-TERMINAL DC"),
    VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE("vscdc", "VOLTAGE SOURCE CONVERTER"),
    TRANSFORMER_IMPEDANCE_CORRECTION_TABLES("impcor", "IMPEDANCE CORRECTION"),
    // Multi terminal Dc are defined in four Json objects
    MULTI_TERMINAL_DC_TRANSMISSION_LINE("ntermdc", "MULTI-TERMINAL DC"),
    INTERNAL_MULTI_TERMINAL_DC_CONVERTER("ntermdcconv", "MULTI-TERMINAL DC"),
    INTERNAL_MULTI_TERMINAL_DC_BUS("ntermdcbus", "MULTI-TERMINAL DC"),
    INTERNAL_MULTI_TERMINAL_DC_LINK("ntermdclink", "MULTI-TERMINAL DC"),
    MULTI_SECTION_LINE_GROUPING("msline", "MULTI-SECTION LINE"),
    ZONE("zone"),
    INTERAREA_TRANSFER("iatransfer", "INTER-AREA TRANSFER"),
    OWNER("owner"),
    FACTS_CONTROL_DEVICE("facts", "FACTS CONTROL DEVICE"),
    SWITCHED_SHUNT("swshunt", "SWITCHED SHUNT"),
    GNE_DEVICE("gne", "GNE DEVICE"),
    INDUCTION_MACHINE("indmach", "INDUCTION MACHINE"),
    SUBSTATION("sub");

    private final String rawxNodeName;
    private final String rawName;

    PowerFlowRecordGroup(String rawxNodeName) {
        this.rawxNodeName = rawxNodeName;
        this.rawName = name();
    }

    PowerFlowRecordGroup(String rawxNodeName, String rawName) {
        this.rawxNodeName = rawxNodeName;
        this.rawName = rawName;
    }

    @Override
    public String getDataName() {
        return "PowerFlow";
    }

    @Override
    public String getJsonNodeName() {
        return rawxNodeName;
    }

    @Override
    public String getLegacyTextName() {
        return rawName;
    }

    @Override
    public JsonObjectType getJsonObjectType() {
        return rawxNodeName.equals("caseid") ? JsonObjectType.PARAMETER_SET : JsonObjectType.DATA_TABLE;
    }
}
