/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseContext {

    private String delimiter;

    private String[] caseIdentificationDataReadFields;
    private String[] busDataReadFields;
    private String[] loadDataReadFields;
    private String[] fixedBusShuntDataReadFields;
    private String[] generatorDataReadFields;
    private String[] nonTransformerBranchDataReadFields;
    private String[] t3wTransformerDataReadFields;
    private String[] t3wTransformerDataWinding1ReadFields;
    private String[] t3wTransformerDataWinding2ReadFields;
    private String[] t3wTransformerDataWinding3ReadFields;
    private String[] t2wTransformerDataReadFields;
    private String[] t2wTransformerDataWinding1ReadFields;
    private String[] t2wTransformerDataWinding2ReadFields;
    private String[] areaInterchangeDataReadFields;
    private String[] zoneDataReadFields;
    private String[] ownerDataReadFields;
    private String[] switchedShuntDataReadFields;

    PsseContext() {
    }

    void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    String getDelimiter() {
        return this.delimiter;
    }

    void setCaseIdentificationDataReadFields(String[] fields) {
        this.caseIdentificationDataReadFields = fields;
    }

    String[] getCaseIdentificationDataReadFields() {
        return this.caseIdentificationDataReadFields;
    }

    void setBusDataReadFields(String[] fields) {
        this.busDataReadFields = fields;
    }

    String[] getBusDataReadFields() {
        return this.busDataReadFields;
    }

    void setLoadDataReadFields(String[] fields) {
        this.loadDataReadFields = fields;
    }

    String[] getLoadDataReadFields() {
        return this.loadDataReadFields;
    }

    void setFixedBusShuntDataReadFields(String[] fields) {
        this.fixedBusShuntDataReadFields = fields;
    }

    String[] getFixedBusShuntDataReadFields() {
        return this.fixedBusShuntDataReadFields;
    }

    void setGeneratorDataReadFields(String[] fields) {
        this.generatorDataReadFields = fields;
    }

    String[] getGeneratorDataReadFields() {
        return this.generatorDataReadFields;
    }

    void setNonTransformerBranchDataReadFields(String[] fields) {
        this.nonTransformerBranchDataReadFields = fields;
    }

    String[] getNonTransformerBranchDataReadFields() {
        return this.nonTransformerBranchDataReadFields;
    }

    void set3wTransformerDataReadFields(String[] fields, String[] winding1Fields, String[] winding2Fields,
        String[] winding3Fields) {
        this.t3wTransformerDataReadFields = fields;
        this.t3wTransformerDataWinding1ReadFields = winding1Fields;
        this.t3wTransformerDataWinding2ReadFields = winding2Fields;
        this.t3wTransformerDataWinding3ReadFields = winding3Fields;
    }

    boolean is3wTransformerDataReadFieldsEmpty() {
        return this.t3wTransformerDataReadFields == null;
    }

    String[] get3wTransformerDataReadFields() {
        return this.t3wTransformerDataReadFields;
    }

    String[] get3wTransformerDataWinding1ReadFields() {
        return this.t3wTransformerDataWinding1ReadFields;
    }

    String[] get3wTransformerDataWinding2ReadFields() {
        return this.t3wTransformerDataWinding2ReadFields;
    }

    String[] get3wTransformerDataWinding3ReadFields() {
        return this.t3wTransformerDataWinding3ReadFields;
    }

    void set2wTransformerDataReadFields(String[] fields, String[] winding1Fields, String[] winding2Fields) {
        this.t2wTransformerDataReadFields = fields;
        this.t2wTransformerDataWinding1ReadFields = winding1Fields;
        this.t2wTransformerDataWinding2ReadFields = winding2Fields;
    }

    boolean is2wTransformerDataReadFieldsEmpty() {
        return this.t2wTransformerDataReadFields == null;
    }

    String[] get2wTransformerDataReadFields() {
        return this.t2wTransformerDataReadFields;
    }

    String[] get2wTransformerDataWinding1ReadFields() {
        return this.t2wTransformerDataWinding1ReadFields;
    }

    String[] get2wTransformerDataWinding2ReadFields() {
        return this.t2wTransformerDataWinding2ReadFields;
    }

    void setAreaInterchangeDataReadFields(String[] fields) {
        this.areaInterchangeDataReadFields = fields;
    }

    String[] getAreaInterchangeDataReadFields() {
        return this.areaInterchangeDataReadFields;
    }

    void setZoneDataReadFields(String[] fields) {
        this.zoneDataReadFields = fields;
    }

    String[] getZoneDataReadFields() {
        return this.zoneDataReadFields;
    }

    void setOwnerDataReadFields(String[] fields) {
        this.ownerDataReadFields = fields;
    }

    String[] getOwnerDataReadFields() {
        return this.ownerDataReadFields;
    }

    void setSwitchedShuntDataReadFields(String[] fields) {
        this.switchedShuntDataReadFields = fields;
    }

    String[] getSwitchedShuntDataReadFields() {
        return this.switchedShuntDataReadFields;
    }

    static String[] caseIdentificationDataHeaders(int firstRecordFields) {
        String[] first = new String[] {"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq"};
        return ArrayUtils.addAll(ArrayUtils.subarray(first, 0, firstRecordFields), "title1", "title2");
    }

    static String[] caseIdentificationDataHeaders() {
        return new String[] {"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};
    }

    static String[] busDataHeaders() {
        return new String[] {"i", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va", "nvhi", "nvlo", "evhi", "evlo"};
    }

    static String[] loadDataHeaders() {
        return new String[] {"i", "id", "status", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale", "intrpt"};
    }

    static String[] fixedBusShuntDataHeaders() {
        return new String[] {"i", "id", "status", "gl", "bl"};
    }

    static String[] generatorDataHeaders() {
        return new String[] {"i", "id", "pg", "qg", "qt", "qb", "vs", "ireg", "mbase", "zr", "zx", "rt",
            "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};
    }

    static String[] nonTransformerBranchDataHeaders() {
        return new String[] {"i", "j", "ckt", "r", "x", "b", "ratea", "rateb", "ratec", "gi", "bi", "gj", "bj",
            "st", "met", "len", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};
    }

    static String[] transformerDataHeaders(int firstRecordFields) {
        String[] first = new String[] {"i", "j", "k", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmetr", "name", "stat",
            "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "vecgrp"};
        String[] second = new String[] {"r12", "x12", "sbase12", "r23", "x23", "sbase23", "r31", "x31", "sbase31",
            "vmstar", "anstar"};

        return ArrayUtils.addAll(ArrayUtils.subarray(first, 0, firstRecordFields), second);
    }

    static String[] transformerWindingDataHeaders() {
        return new String[] {"windv", "nomv", "ang", "rata", "ratb", "ratc", "cod", "cont", "rma", "rmi", "vma", "vmi",
            "ntp", "tab", "cr", "cx", "cnxa"};
    }

    static String[] areaInterchangeDataHeaders() {
        return new String[] {"i", "isw", "pdes", "ptol", "arname"};
    }

    static String[] zoneDataHeaders() {
        return new String[] {"i", "zoname"};
    }

    static String[] ownerDataHeaders() {
        return new String[] {"i", "owname"};
    }

    static String[] switchedShuntDataHeaders() {
        return new String[] {"i", "modsw", "adjm", "stat", "vswhi", "vswlo", "swrem", "rmpct", "rmidnt", "binit",
            "n1", "b1", "n2", "b2", "n3", "b3", "n4", "b4", "n5", "b5", "n6", "b6", "n7", "b7", "n8", "b8"};
    }
}
