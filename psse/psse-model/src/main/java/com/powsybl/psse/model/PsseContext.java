/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.powsybl.psse.model.PsseConstants.PsseVersion;

import java.util.EnumMap;
import java.util.Map;

import static com.powsybl.psse.model.data.AbstractRecordGroup.PsseRecordGroup;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseContext {

    private final Map<PsseRecordGroup, String[]> fieldNames = new EnumMap<>(PsseRecordGroup.class);
    private String delimiter;
    private PsseVersion version;
    private int currentRecordGroupMaxNumFields;

    public PsseContext() {
    }

    public PsseVersion getVersion() {
        return this.version;
    }

    public PsseContext setVersion(PsseVersion version) {
        this.version = version;
        return this;
    }

    public String getDelimiter() {
        return this.delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setFieldNames(PsseRecordGroup recordGroup, String[] fieldNames) {
        this.fieldNames.put(recordGroup, fieldNames);
    }

    public String[] getFieldNames(PsseRecordGroup recordGroup) {
        return this.fieldNames.get(recordGroup);
    }

    public boolean is3wTransformerDataReadFieldsEmpty() {
        return fieldNames.get(PsseRecordGroup.TRANSFORMER_3_DATA) == null;
    }

    public boolean is2wTransformerDataReadFieldsEmpty() {
        return fieldNames.get(PsseRecordGroup.TRANSFORMER_2_DATA) == null;
    }

    public void resetCurrentRecordGroup() {
        currentRecordGroupMaxNumFields = 0;
    }

    public int getCurrentRecordGroupMaxNumFields() {
        return currentRecordGroupMaxNumFields;
    }

    public void setCurrentRecordNumFields(int numFields) {
        currentRecordGroupMaxNumFields = Math.max(currentRecordGroupMaxNumFields, numFields);
    }
}
