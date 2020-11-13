/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.powsybl.psse.model.data.AbstractDataBlock;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseContext {

    private final Map<AbstractDataBlock.PsseDataBlock, String[]> fieldNames = new EnumMap<>(AbstractDataBlock.PsseDataBlock.class);
    private String delimiter;
    private PsseConstants.PsseVersion version;

    public PsseContext() {
    }

    public PsseConstants.PsseVersion getVersion() {
        return this.version;
    }

    public PsseContext setVersion(PsseConstants.PsseVersion version) {
        this.version = version;
        return this;
    }

    public String getDelimiter() {
        return this.delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setFieldNames(AbstractDataBlock.PsseDataBlock dataBlock, String[] fieldNames) {
        this.fieldNames.put(dataBlock, fieldNames);
    }

    public String[] getFieldNames(AbstractDataBlock.PsseDataBlock dataBlock) {
        return this.fieldNames.get(dataBlock);
    }

    public boolean is3wTransformerDataReadFieldsEmpty() {
        return fieldNames.get(AbstractDataBlock.PsseDataBlock.TRANSFORMER_3_DATA) == null;
    }

    public boolean is2wTransformerDataReadFieldsEmpty() {
        return fieldNames.get(AbstractDataBlock.PsseDataBlock.TRANSFORMER_2_DATA) == null;
    }
}
