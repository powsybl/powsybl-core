/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.internal;

import com.powsybl.psse.model.io.PsseFieldDefinition;
import com.powsybl.psse.model.io.Util;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.defaultStringFor;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_EQID;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_I;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_IBUS;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ID;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_INODE;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_J;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_JBUS;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_K;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_KBUS;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_NI;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_TYPE;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseSubstationEquipmentTerminal {

    private static final Map<String, PsseFieldDefinition<PsseSubstationEquipmentTerminal, ?>> FIELDS = createFields();

    private int i;
    private int ni;
    private String type;
    private String id = defaultStringFor(STR_ID, FIELDS);
    private int j = defaultIntegerFor(STR_J, FIELDS);
    private int k = defaultIntegerFor(STR_K, FIELDS);

    private static Map<String, PsseFieldDefinition<PsseSubstationEquipmentTerminal, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseSubstationEquipmentTerminal, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseSubstationEquipmentTerminal::getI, PsseSubstationEquipmentTerminal::setI));
        addField(fields, createNewField(STR_IBUS, Integer.class, PsseSubstationEquipmentTerminal::getI, PsseSubstationEquipmentTerminal::setI));
        addField(fields, createNewField(STR_NI, Integer.class, PsseSubstationEquipmentTerminal::getNi, PsseSubstationEquipmentTerminal::setNi));
        addField(fields, createNewField(STR_INODE, Integer.class, PsseSubstationEquipmentTerminal::getNi, PsseSubstationEquipmentTerminal::setNi));
        addField(fields, createNewField(STR_TYPE, String.class, PsseSubstationEquipmentTerminal::getType, PsseSubstationEquipmentTerminal::setType));
        addField(fields, createNewField(STR_ID, String.class, PsseSubstationEquipmentTerminal::getId, PsseSubstationEquipmentTerminal::setId, "1"));
        addField(fields, createNewField(STR_EQID, String.class, PsseSubstationEquipmentTerminal::getId, PsseSubstationEquipmentTerminal::setId, "1"));
        addField(fields, createNewField(STR_JBUS, Integer.class, PsseSubstationEquipmentTerminal::getJ, PsseSubstationEquipmentTerminal::setJ, 0));
        addField(fields, createNewField(STR_J, Integer.class, PsseSubstationEquipmentTerminal::getJ, PsseSubstationEquipmentTerminal::setJ, 0));
        addField(fields, createNewField(STR_K, Integer.class, PsseSubstationEquipmentTerminal::getK, PsseSubstationEquipmentTerminal::setK, 0));
        addField(fields, createNewField(STR_KBUS, Integer.class, PsseSubstationEquipmentTerminal::getK, PsseSubstationEquipmentTerminal::setK, 0));

        return fields;
    }

    public static PsseSubstationEquipmentTerminal fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseSubstationEquipmentTerminal::new);
    }

    public static void toRecord(PsseSubstationEquipmentTerminal substationEquipmentTerminal, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        Util.toRecord(substationEquipmentTerminal, headers, FIELDS, row, unexpectedHeaders);
    }

    public static String[] toRecord(PsseSubstationEquipmentTerminal substationEquipmentTerminal, String[] headers) {
        return Util.toRecord(substationEquipmentTerminal, headers, FIELDS);
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getNi() {
        return ni;
    }

    public void setNi(int ni) {
        this.ni = ni;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public PsseSubstationEquipmentTerminal copy() {
        PsseSubstationEquipmentTerminal copy = new PsseSubstationEquipmentTerminal();
        copy.i = this.i;
        copy.ni = this.ni;
        copy.type = this.type;
        copy.id = this.id;
        copy.j = this.j;
        copy.k = this.k;
        return copy;
    }
}
