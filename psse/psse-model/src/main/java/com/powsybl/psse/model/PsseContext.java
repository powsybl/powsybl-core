/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

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
}
