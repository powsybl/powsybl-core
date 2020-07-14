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

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getDelimiter() {
        return this.delimiter;
    }

    public void setCaseIdentificationDataReadFields(String[] fields) {
        this.caseIdentificationDataReadFields = fields;
    }

    String[] getCaseIdentificationDataReadFields() {
        return this.caseIdentificationDataReadFields;
    }

    public void setBusDataReadFields(String[] fields) {
        this.busDataReadFields = fields;
    }

    String[] getBusDataReadFields() {
        return this.busDataReadFields;
    }

    public void setLoadDataReadFields(String[] fields) {
        this.loadDataReadFields = fields;
    }

    String[] getLoadDataReadFields() {
        return this.loadDataReadFields;
    }

    public void setFixedBusShuntDataReadFields(String[] fields) {
        this.fixedBusShuntDataReadFields = fields;
    }

    String[] getFixedBusShuntDataReadFields() {
        return this.fixedBusShuntDataReadFields;
    }

    public void setGeneratorDataReadFields(String[] fields) {
        this.generatorDataReadFields = fields;
    }

    String[] getGeneratorDataReadFields() {
        return this.generatorDataReadFields;
    }

    public void setNonTransformerBranchDataReadFields(String[] fields) {
        this.nonTransformerBranchDataReadFields = fields;
    }

    String[] getNonTransformerBranchDataReadFields() {
        return this.nonTransformerBranchDataReadFields;
    }

    public void set3wTransformerDataReadFields(String[] fields, String[] winding1Fields, String[] winding2Fields,
        String[] winding3Fields) {
        this.t3wTransformerDataReadFields = fields;
        this.t3wTransformerDataWinding1ReadFields = winding1Fields;
        this.t3wTransformerDataWinding2ReadFields = winding2Fields;
        this.t3wTransformerDataWinding3ReadFields = winding3Fields;
    }

    boolean is3wTransformerDataReadFieldsEmpty() {
        return this.t3wTransformerDataReadFields == null;
    }

    public String[] get3wTransformerDataReadFields() {
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

    public void set2wTransformerDataReadFields(String[] fields, String[] winding1Fields, String[] winding2Fields) {
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

    public void setAreaInterchangeDataReadFields(String[] fields) {
        this.areaInterchangeDataReadFields = fields;
    }

    String[] getAreaInterchangeDataReadFields() {
        return this.areaInterchangeDataReadFields;
    }

    public void setZoneDataReadFields(String[] fields) {
        this.zoneDataReadFields = fields;
    }

    String[] getZoneDataReadFields() {
        return this.zoneDataReadFields;
    }

    public void setOwnerDataReadFields(String[] fields) {
        this.ownerDataReadFields = fields;
    }

    String[] getOwnerDataReadFields() {
        return this.ownerDataReadFields;
    }

    public void setSwitchedShuntDataReadFields(String[] fields) {
        this.switchedShuntDataReadFields = fields;
    }

    String[] getSwitchedShuntDataReadFields() {
        return this.switchedShuntDataReadFields;
    }

    public static String[] transformerDataHeaders(int firstRecordFields) {
        String[] first = new String[] {"i", "j", "k", "ckt", "cw", "cz", "cm", "mag1", "mag2", "nmetr", "name", "stat",
            "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "vecgrp"};
        String[] second = new String[] {"r12", "x12", "sbase12", "r23", "x23", "sbase23", "r31", "x31", "sbase31",
            "vmstar", "anstar"};

        return ArrayUtils.addAll(ArrayUtils.subarray(first, 0, firstRecordFields), second);
    }

    public static String[] transformerWindingDataHeaders() {
        return new String[] {"windv", "nomv", "ang", "rata", "ratb", "ratc", "cod", "cont", "rma", "rmi", "vma", "vmi",
            "ntp", "tab", "cr", "cx", "cnxa"};
    }

    public static String[] switchedShuntDataHeaders() {
        return new String[] {"i", "modsw", "adjm", "stat", "vswhi", "vswlo", "swrem", "rmpct", "rmidnt", "binit",
            "n1", "b1", "n2", "b2", "n3", "b3", "n4", "b4", "n5", "b5", "n6", "b6", "n7", "b7", "n8", "b8"};
    }
}
