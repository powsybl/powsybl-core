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
    private String[] t2wTransformerDataReadFields;
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

    public void set3wTransformerDataReadFields(String[] fields) {
        this.t3wTransformerDataReadFields = fields;
    }

    public boolean is3wTransformerDataReadFieldsEmpty() {
        return this.t3wTransformerDataReadFields == null;
    }

    public String[] get3wTransformerDataReadFields() {
        return this.t3wTransformerDataReadFields;
    }

    public void set2wTransformerDataReadFields(String[] fields) {
        this.t2wTransformerDataReadFields = fields;
    }

    public boolean is2wTransformerDataReadFieldsEmpty() {
        return this.t2wTransformerDataReadFields == null;
    }

    String[] get2wTransformerDataReadFields() {
        return this.t2wTransformerDataReadFields;
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
}
