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

import static com.powsybl.psse.model.io.Util.addField;
import static com.powsybl.psse.model.io.Util.concatStringArrays;
import static com.powsybl.psse.model.io.Util.createNewField;
import static com.powsybl.psse.model.io.Util.defaultDoubleFor;
import static com.powsybl.psse.model.io.Util.defaultIntegerFor;
import static com.powsybl.psse.model.io.Util.stringHeaders;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_AREA;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_BASKV;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_EVHI;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_EVLO;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_I;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_IBUS;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_IDE;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_NAME;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_NVHI;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_NVLO;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_OWNER;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_VA;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_VM;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_ZONE;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseBus extends PsseVersioned {

    private static final Map<String, PsseFieldDefinition<PsseBus, ?>> FIELDS = createFields();
    private static final String[] FIELD_NAMES_COMMON = {STR_NAME, STR_BASKV, STR_IDE, STR_AREA, STR_ZONE, STR_OWNER, STR_VM, STR_VA};
    private static final String[] FIELD_NAMES_32_33_START = {STR_I};
    private static final String[] FIELD_NAMES_32 = concatStringArrays(FIELD_NAMES_32_33_START, FIELD_NAMES_COMMON);
    private static final String[] FIELD_NAMES_33_PLUS = {STR_NVHI, STR_NVLO, STR_EVHI, STR_EVLO};
    private static final String[] FIELD_NAMES_33 = concatStringArrays(FIELD_NAMES_32_33_START, FIELD_NAMES_COMMON, FIELD_NAMES_33_PLUS);
    private static final String[] FIELD_NAMES_35_START = {STR_IBUS};
    private static final String[] FIELD_NAMES_35 = concatStringArrays(FIELD_NAMES_35_START, FIELD_NAMES_COMMON, FIELD_NAMES_33_PLUS);

    private int i;
    private String name;
    private double baskv = defaultDoubleFor(STR_BASKV, FIELDS);
    private int ide = defaultIntegerFor(STR_IDE, FIELDS);
    private int area = defaultIntegerFor(STR_AREA, FIELDS);
    private int zone = defaultIntegerFor(STR_ZONE, FIELDS);
    private int owner = defaultIntegerFor(STR_OWNER, FIELDS);
    private double vm = defaultDoubleFor(STR_VM, FIELDS);
    private double va = defaultDoubleFor(STR_VA, FIELDS);

    @Revision(since = 33)
    private double nvhi = defaultDoubleFor(STR_NVHI, FIELDS);

    @Revision(since = 33)
    private double nvlo = defaultDoubleFor(STR_NVLO, FIELDS);

    @Revision(since = 33)
    private double evhi = defaultDoubleFor(STR_EVHI, FIELDS);

    @Revision(since = 33)
    private double evlo = defaultDoubleFor(STR_EVLO, FIELDS);

    public static String[] getFieldNames32() {
        return FIELD_NAMES_32;
    }

    public static String[] getFieldNames33() {
        return FIELD_NAMES_33;
    }

    public static String[] getFieldNames35() {
        return FIELD_NAMES_35;
    }

    public static String[] getFieldNamesString() {
        return stringHeaders(FIELDS);
    }

    public static PsseBus fromRecord(CsvRecord rec, String[] headers) {
        return Util.fromRecord(rec.getFields(), headers, FIELDS, PsseBus::new);
    }

    public static String[] toRecord(PsseBus psseBus, String[] headers) {
        return Util.toRecord(psseBus, headers, FIELDS);
    }

    private static Map<String, PsseFieldDefinition<PsseBus, ?>> createFields() {
        Map<String, PsseFieldDefinition<PsseBus, ?>> fields = new HashMap<>();

        addField(fields, createNewField(STR_I, Integer.class, PsseBus::getI, PsseBus::setI));
        addField(fields, createNewField(STR_IBUS, Integer.class, PsseBus::getI, PsseBus::setI));
        addField(fields, createNewField(STR_NAME, String.class, PsseBus::getName, PsseBus::setName));
        addField(fields, createNewField(STR_BASKV, Double.class, PsseBus::getBaskv, PsseBus::setBaskv, 0d));
        addField(fields, createNewField(STR_IDE, Integer.class, PsseBus::getIde, PsseBus::setIde, 1));
        addField(fields, createNewField(STR_AREA, Integer.class, PsseBus::getArea, PsseBus::setArea, 1));
        addField(fields, createNewField(STR_ZONE, Integer.class, PsseBus::getZone, PsseBus::setZone, 1));
        addField(fields, createNewField(STR_OWNER, Integer.class, PsseBus::getOwner, PsseBus::setOwner, 1));
        addField(fields, createNewField(STR_VM, Double.class, PsseBus::getVm, PsseBus::setVm, 1d));
        addField(fields, createNewField(STR_VA, Double.class, PsseBus::getVa, PsseBus::setVa, 0d));
        addField(fields, createNewField(STR_NVHI, Double.class, PsseBus::getNvhi, PsseBus::setNvhi, 1.1));
        addField(fields, createNewField(STR_NVLO, Double.class, PsseBus::getNvlo, PsseBus::setNvlo, 0.9));
        addField(fields, createNewField(STR_EVHI, Double.class, PsseBus::getEvhi, PsseBus::setEvhi, 1.1));
        addField(fields, createNewField(STR_EVLO, Double.class, PsseBus::getEvlo, PsseBus::setEvlo, 0.9));

        return fields;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBaskv() {
        return baskv;
    }

    public void setBaskv(double baskv) {
        this.baskv = baskv;
    }

    public int getIde() {
        return ide;
    }

    public void setIde(int ide) {
        this.ide = ide;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public double getVm() {
        return vm;
    }

    public void setVm(double vm) {
        this.vm = vm;
    }

    public double getVa() {
        return va;
    }

    public void setVa(double va) {
        this.va = va;
    }

    public double getNvhi() {
        checkVersion("nvhi");
        return nvhi;
    }

    public void setNvhi(double nvhi) {
        checkVersion("nvhi");
        this.nvhi = nvhi;
    }

    public double getNvlo() {
        checkVersion("nvlo");
        return nvlo;
    }

    public void setNvlo(double nvlo) {
        checkVersion("nvlo");
        this.nvlo = nvlo;
    }

    public double getEvhi() {
        checkVersion("evhi");
        return evhi;
    }

    public void setEvhi(double evhi) {
        checkVersion("evhi");
        this.evhi = evhi;
    }

    public double getEvlo() {
        checkVersion("evlo");
        return evlo;
    }

    public void setEvlo(double evlo) {
        checkVersion("evlo");
        this.evlo = evlo;
    }

    public PsseBus copy() {
        PsseBus copy = new PsseBus();
        copy.i = this.i;
        copy.name = this.name;
        copy.baskv = this.baskv;
        copy.ide = this.ide;
        copy.area = this.area;
        copy.zone = this.zone;
        copy.owner = this.owner;
        copy.vm = this.vm;
        copy.va = this.va;
        copy.nvhi = this.nvhi;
        copy.nvlo = this.nvlo;
        copy.evhi = this.evhi;
        copy.evlo = this.evlo;
        return copy;
    }
}
