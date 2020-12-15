package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.RecordGroupIdentification;

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
    MULTI_TERMINAL_DC_TRANSMISSION_LINE("ntermdc", "MULTI-TERMINAL DC"),
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

    private PowerFlowRecordGroup(String rawxNodeName) {
        this.rawxNodeName = rawxNodeName;
        this.rawName = name();
    }

    private PowerFlowRecordGroup(String rawxNodeName, String rawName) {
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
