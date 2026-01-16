/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
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
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseVoltageSourceConverter extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseVoltageSourceConverter, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_COMMON_1 = {STR_IBUS, STR_TYPE, STR_MODE, STR_DCSET, STR_ACSET, STR_ALOSS, STR_BLOSS, STR_MINLOSS, STR_SMAX, STR_IMAX, STR_PWF, STR_MAXQ, STR_MINQ};
    private static final String[] FIELD_NAMES_COMMON_2 = {STR_RMPCT};
    private static final String[] FIELD_NAMES_MIDDLE_32_33 = {STR_REMOT};
    private static final String[] FIELD_NAMES_MIDDLE_35 = {STR_VSREG, STR_NREG};
    private static final String[] FIELD_NAMES_32_33 = concatStringArrays(FIELD_NAMES_COMMON_1, FIELD_NAMES_MIDDLE_32_33, FIELD_NAMES_COMMON_2);
    private static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_COMMON_1, FIELD_NAMES_MIDDLE_35, FIELD_NAMES_COMMON_2);

    private int ibus;
    private int type;
    private int mode = defaultIntegerFor(STR_MODE, FIELDS);
    private double dcset;
    private double acset = defaultDoubleFor(STR_ACSET, FIELDS);
    private double aloss = defaultDoubleFor(STR_ALOSS, FIELDS);
    private double bloss = defaultDoubleFor(STR_BLOSS, FIELDS);
    private double minloss = defaultDoubleFor(STR_MINLOSS, FIELDS);
    private double smax = defaultDoubleFor(STR_SMAX, FIELDS);
    private double imax = defaultDoubleFor(STR_IMAX, FIELDS);
    private double pwf = defaultDoubleFor(STR_PWF, FIELDS);
    private double maxq = defaultDoubleFor(STR_MAXQ, FIELDS);
    private double minq = defaultDoubleFor(STR_MINQ, FIELDS);

    @Revision(until = 33)
    private int remot = defaultIntegerFor(STR_REMOT, FIELDS);

    private double rmpct = defaultDoubleFor(STR_RMPCT, FIELDS);

    @Revision(since = 35)
    private int vsreg = defaultIntegerFor(STR_VSREG, FIELDS);

    @Revision(since = 35)
    private int nreg = defaultIntegerFor(STR_NREG, FIELDS);

    public static String[] getFieldNames3233() {
        return FIELD_NAMES_32_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseVoltageSourceConverter fromRecord(CsvRecord rec, String[] headers) {
        return fromRecord(rec, headers, "");
    }

    public static PsseVoltageSourceConverter fromRecord(CsvRecord rec, String[] headers, String headerSuffix) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseVoltageSourceConverter::new, headerSuffix);
    }

    public static void toRecord(PsseVoltageSourceConverter psseVoltageSourceConverter, String[] headers, String[] row,
                                Set<String> unexpectedHeaders, String headerSuffix) {
        Util.toRecord(psseVoltageSourceConverter, headers, FIELDS, row, unexpectedHeaders, headerSuffix);
    }

    public static void toRecord(PsseVoltageSourceConverter psseVoltageSourceConverter, String[] headers, String[] row, Set<String> unexpectedHeaders) {
        toRecord(psseVoltageSourceConverter, headers, row, unexpectedHeaders, "");
    }

    public static String[] toRecord(PsseVoltageSourceConverter psseVoltageSourceConverter, String[] headers) {
        return Util.toRecord(psseVoltageSourceConverter, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseVoltageSourceConverter, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseVoltageSourceConverter, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_IBUS, Integer.class, PsseVoltageSourceConverter::getIbus, PsseVoltageSourceConverter::setIbus));
        addField(fields, createNewField(STR_TYPE, Integer.class, PsseVoltageSourceConverter::getType, PsseVoltageSourceConverter::setType));
        addField(fields, createNewField(STR_MODE, Integer.class, PsseVoltageSourceConverter::getMode, PsseVoltageSourceConverter::setMode, 1));
        addField(fields, createNewField(STR_DCSET, Double.class, PsseVoltageSourceConverter::getDcset, PsseVoltageSourceConverter::setDcset));
        addField(fields, createNewField(STR_ACSET, Double.class, PsseVoltageSourceConverter::getAcset, PsseVoltageSourceConverter::setAcset, 1d));
        addField(fields, createNewField(STR_ALOSS, Double.class, PsseVoltageSourceConverter::getAloss, PsseVoltageSourceConverter::setAloss, 0d));
        addField(fields, createNewField(STR_BLOSS, Double.class, PsseVoltageSourceConverter::getBloss, PsseVoltageSourceConverter::setBloss, 0d));
        addField(fields, createNewField(STR_MINLOSS, Double.class, PsseVoltageSourceConverter::getMinloss, PsseVoltageSourceConverter::setMinloss, 0d));
        addField(fields, createNewField(STR_SMAX, Double.class, PsseVoltageSourceConverter::getSmax, PsseVoltageSourceConverter::setSmax, 0d));
        addField(fields, createNewField(STR_IMAX, Double.class, PsseVoltageSourceConverter::getImax, PsseVoltageSourceConverter::setImax, 0d));
        addField(fields, createNewField(STR_PWF, Double.class, PsseVoltageSourceConverter::getPwf, PsseVoltageSourceConverter::setPwf, 1d));
        addField(fields, createNewField(STR_MAXQ, Double.class, PsseVoltageSourceConverter::getMaxq, PsseVoltageSourceConverter::setMaxq, 9999d));
        addField(fields, createNewField(STR_MINQ, Double.class, PsseVoltageSourceConverter::getMinq, PsseVoltageSourceConverter::setMinq, -9999d));
        addField(fields, createNewField(STR_REMOT, Integer.class, PsseVoltageSourceConverter::getRemot, PsseVoltageSourceConverter::setRemot, 0));
        addField(fields, createNewField(STR_RMPCT, Double.class, PsseVoltageSourceConverter::getRmpct, PsseVoltageSourceConverter::setRmpct, 100d));
        addField(fields, createNewField(STR_VSREG, Integer.class, PsseVoltageSourceConverter::getVsreg, PsseVoltageSourceConverter::setVsreg, 0));
        addField(fields, createNewField(STR_NREG, Integer.class, PsseVoltageSourceConverter::getNreg, PsseVoltageSourceConverter::setNreg, 0));

        return fields;
    }

    public int getIbus() {
        return ibus;
    }

    public void setIbus(int ibus) {
        this.ibus = ibus;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public double getDcset() {
        return dcset;
    }

    public void setDcset(double dcset) {
        this.dcset = dcset;
    }

    public double getAcset() {
        return acset;
    }

    public void setAcset(double acset) {
        this.acset = acset;
    }

    public double getAloss() {
        return aloss;
    }

    public void setAloss(double aloss) {
        this.aloss = aloss;
    }

    public double getBloss() {
        return bloss;
    }

    public void setBloss(double bloss) {
        this.bloss = bloss;
    }

    public double getMinloss() {
        return minloss;
    }

    public void setMinloss(double minloss) {
        this.minloss = minloss;
    }

    public double getSmax() {
        return smax;
    }

    public void setSmax(double smax) {
        this.smax = smax;
    }

    public double getImax() {
        return imax;
    }

    public void setImax(double imax) {
        this.imax = imax;
    }

    public double getPwf() {
        return pwf;
    }

    public void setPwf(double pwf) {
        this.pwf = pwf;
    }

    public double getMaxq() {
        return maxq;
    }

    public void setMaxq(double maxq) {
        this.maxq = maxq;
    }

    public double getMinq() {
        return minq;
    }

    public void setMinq(double minq) {
        this.minq = minq;
    }

    public int getRemot() {
        checkVersion(STR_REMOT);
        return remot;
    }

    public void setRemot(int remot) {
        this.remot = remot;
    }

    public double getRmpct() {
        return rmpct;
    }

    public void setRmpct(double rmpct) {
        this.rmpct = rmpct;
    }

    public int getVsreg() {
        checkVersion(STR_VSREG);
        return vsreg;
    }

    public void setVsreg(int vsreg) {
        this.vsreg = vsreg;
    }

    public int getNreg() {
        checkVersion("nreg");
        return nreg;
    }

    public void setNreg(int nreg) {
        this.nreg = nreg;
    }

    public PsseVoltageSourceConverter copy() {
        PsseVoltageSourceConverter copy = new PsseVoltageSourceConverter();
        copy.ibus = this.ibus;
        copy.type = this.type;
        copy.mode = this.mode;
        copy.dcset = this.dcset;
        copy.acset = this.acset;
        copy.aloss = this.aloss;
        copy.bloss = this.bloss;
        copy.minloss = this.minloss;
        copy.smax = this.smax;
        copy.imax = this.imax;
        copy.pwf = this.pwf;
        copy.maxq = this.maxq;
        copy.minq = this.minq;
        copy.remot = this.remot;
        copy.rmpct = this.rmpct;
        copy.vsreg = this.vsreg;
        copy.nreg = this.nreg;
        return copy;
    }
}
