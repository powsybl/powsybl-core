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
import static com.powsybl.psse.model.io.Util.concatStringArrays;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class TransformerImpedances {

    private static final Map<String, PsseFieldDefinition<TransformerImpedances, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_IMPEDANCE_12 = {STR_R12, STR_X12, STR_SBASE12};
    private static final String[] FIELD_NAMES_IMPEDANCE_23 = {STR_R23, STR_X23, STR_SBASE23};
    private static final String[] FIELD_NAMES_IMPEDANCE_31 = {STR_R31, STR_X31, STR_SBASE31};
    private static final String[] FIELD_NAMES_COMMON = {STR_VMSTAR, STR_ANSTAR};
    private static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_IMPEDANCE_12, FIELD_NAMES_IMPEDANCE_23, FIELD_NAMES_IMPEDANCE_31, FIELD_NAMES_COMMON);
    private static final String[] FIELD_NAMES_35_RAWX = {STR_R1_2, STR_X1_2, STR_SBASE1_2, STR_R2_3, STR_X2_3, STR_SBASE2_3, STR_R3_1, STR_X3_1, STR_SBASE3_1, STR_VMSTAR, STR_ANSTAR};

    private double r12 = defaultDoubleFor(STR_R12, FIELDS);
    private double x12 = defaultDoubleFor(STR_X12, FIELDS);
    private double sbase12 = defaultDoubleFor(STR_SBASE12, FIELDS);
    private double r23 = defaultDoubleFor(STR_R23, FIELDS);
    private double x23 = defaultDoubleFor(STR_X23, FIELDS);
    private double sbase23 = defaultDoubleFor(STR_SBASE23, FIELDS);
    private double r31 = defaultDoubleFor(STR_R31, FIELDS);
    private double x31 = defaultDoubleFor(STR_X31, FIELDS);
    private double sbase31 = defaultDoubleFor(STR_SBASE31, FIELDS);
    private double vmstar = defaultDoubleFor(STR_VMSTAR, FIELDS);
    private double anstar = defaultDoubleFor(STR_ANSTAR, FIELDS);

    public static String[] getFieldNamesT2W() {
        return FIELD_NAMES_IMPEDANCE_12;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNames35RawX() {
        return FIELD_NAMES_35_RAWX;
    }

    public static TransformerImpedances fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, TransformerImpedances::new);
    }

    public static void toRecord(TransformerImpedances transformerImpedances, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        Util.toRecord(transformerImpedances, headers, FIELDS, row, unexpectedHeaders);
    }

    public static String[] toRecord(TransformerImpedances transformerImpedances, String[] headers) {
        return Util.toRecord(transformerImpedances, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<TransformerImpedances, ?>> createFields() {
        Map<String, PsseFieldDefinition<TransformerImpedances, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_R12, Double.class, TransformerImpedances::getR12, TransformerImpedances::setR12, 0d));
        addField(fields, createNewField(STR_R1_2, Double.class, TransformerImpedances::getR12, TransformerImpedances::setR12, 0d));
        addField(fields, createNewField(STR_X12, Double.class, TransformerImpedances::getX12, TransformerImpedances::setX12, Double.NaN));
        addField(fields, createNewField(STR_X1_2, Double.class, TransformerImpedances::getX12, TransformerImpedances::setX12, Double.NaN));
        addField(fields, createNewField(STR_SBASE12, Double.class, TransformerImpedances::getSbase12, TransformerImpedances::setSbase12, Double.NaN));
        addField(fields, createNewField(STR_SBASE1_2, Double.class, TransformerImpedances::getSbase12, TransformerImpedances::setSbase12, Double.NaN));
        addField(fields, createNewField(STR_R23, Double.class, TransformerImpedances::getR23, TransformerImpedances::setR23, 0d));
        addField(fields, createNewField(STR_R2_3, Double.class, TransformerImpedances::getR23, TransformerImpedances::setR23, 0d));
        addField(fields, createNewField(STR_X23, Double.class, TransformerImpedances::getX23, TransformerImpedances::setX23, Double.NaN));
        addField(fields, createNewField(STR_X2_3, Double.class, TransformerImpedances::getX23, TransformerImpedances::setX23, Double.NaN));
        addField(fields, createNewField(STR_SBASE23, Double.class, TransformerImpedances::getSbase23, TransformerImpedances::setSbase23, Double.NaN));
        addField(fields, createNewField(STR_SBASE2_3, Double.class, TransformerImpedances::getSbase23, TransformerImpedances::setSbase23, Double.NaN));
        addField(fields, createNewField(STR_R31, Double.class, TransformerImpedances::getR31, TransformerImpedances::setR31, 0d));
        addField(fields, createNewField(STR_R3_1, Double.class, TransformerImpedances::getR31, TransformerImpedances::setR31, 0d));
        addField(fields, createNewField(STR_X31, Double.class, TransformerImpedances::getX31, TransformerImpedances::setX31, Double.NaN));
        addField(fields, createNewField(STR_X3_1, Double.class, TransformerImpedances::getX31, TransformerImpedances::setX31, Double.NaN));
        addField(fields, createNewField(STR_SBASE31, Double.class, TransformerImpedances::getSbase31, TransformerImpedances::setSbase31, Double.NaN));
        addField(fields, createNewField(STR_SBASE3_1, Double.class, TransformerImpedances::getSbase31, TransformerImpedances::setSbase31, Double.NaN));
        addField(fields, createNewField(STR_VMSTAR, Double.class, TransformerImpedances::getVmstar, TransformerImpedances::setVmstar, 1d));
        addField(fields, createNewField(STR_ANSTAR, Double.class, TransformerImpedances::getAnstar, TransformerImpedances::setAnstar, 0d));

        return fields;
    }

    public TransformerImpedances copy() {
        TransformerImpedances copy = new TransformerImpedances();
        copy.r12 = this.r12;
        copy.x12 = this.x12;
        copy.sbase12 = this.sbase12;
        copy.r23 = this.r23;
        copy.x23 = this.x23;
        copy.sbase23 = this.sbase23;
        copy.r31 = this.r31;
        copy.x31 = this.x31;
        copy.sbase31 = this.sbase31;
        copy.vmstar = this.vmstar;
        copy.anstar = this.anstar;
        return copy;
    }

    public double getR12() {
        return r12;
    }

    public void setR12(double r12) {
        this.r12 = r12;
    }

    public double getX12() {
        return x12;
    }

    public void setX12(double x12) {
        this.x12 = x12;
    }

    public double getSbase12() {
        return sbase12;
    }

    public void setSbase12(double sbase12) {
        this.sbase12 = sbase12;
    }

    public double getR23() {
        return r23;
    }

    public void setR23(double r23) {
        this.r23 = r23;
    }

    public double getX23() {
        return x23;
    }

    public void setX23(double x23) {
        this.x23 = x23;
    }

    public double getSbase23() {
        return sbase23;
    }

    public void setSbase23(double sbase23) {
        this.sbase23 = sbase23;
    }

    public double getR31() {
        return r31;
    }

    public void setR31(double r31) {
        this.r31 = r31;
    }

    public double getX31() {
        return x31;
    }

    public void setX31(double x31) {
        this.x31 = x31;
    }

    public double getSbase31() {
        return sbase31;
    }

    public void setSbase31(double sbase31) {
        this.sbase31 = sbase31;
    }

    public double getVmstar() {
        return vmstar;
    }

    public void setVmstar(double vmstar) {
        this.vmstar = vmstar;
    }

    public double getAnstar() {
        return anstar;
    }

    public void setAnstar(double anstar) {
        this.anstar = anstar;
    }
}
