/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

import static com.powsybl.psse.model.io.Util.defaultIfEmpty;
import static com.powsybl.psse.model.io.Util.getFieldFromMultiplePotentialHeaders;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseBus extends PsseVersioned {

    private int i;

    private String name;

    private double baskv = 0;

    private int ide = 1;

    private int area = 1;

    private int zone = 1;

    private int owner = 1;

    private double vm = 1;

    private double va = 0;

    @Revision(since = 33)
    private double nvhi = 1.1;

    @Revision(since = 33)
    private double nvlo = 0.9;

    @Revision(since = 33)
    private double evhi = 1.1;

    @Revision(since = 33)
    private double evlo = 0.9;

    public static PsseBus fromRecord(NamedCsvRecord rec, PsseVersion version) {
        PsseBus psseBus = new PsseBus();
        psseBus.setI(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "i", "ibus")));
        psseBus.setName(defaultIfEmpty(rec.getField("name"), "            "));
        psseBus.setBaskv(Double.parseDouble(rec.getField("baskv")));
        psseBus.setIde(Integer.parseInt(rec.getField("ide")));
        psseBus.setArea(Integer.parseInt(rec.getField("area")));
        psseBus.setZone(Integer.parseInt(rec.getField("zone")));
        psseBus.setOwner(Integer.parseInt(rec.getField("owner")));
        psseBus.setVm(Double.parseDouble(rec.getField("vm")));
        psseBus.setVa(Double.parseDouble(rec.getField("va")));
        if (version.getMajorNumber() >= 33) {
            psseBus.setNvhi(Double.parseDouble(rec.getField("nvhi")));
            psseBus.setNvlo(Double.parseDouble(rec.getField("nvlo")));
            psseBus.setEvhi(Double.parseDouble(rec.getField("evhi")));
            psseBus.setEvlo(Double.parseDouble(rec.getField("evlo")));
        }
        return psseBus;
    }

    public static String[] toRecord(PsseBus psseBus, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "i", "ibus" -> String.valueOf(psseBus.getI());
                case "name" -> psseBus.getName() == null ? "            " : psseBus.getName();
                case "baskv" -> String.valueOf(psseBus.getBaskv());
                case "ide" -> String.valueOf(psseBus.getIde());
                case "area" -> String.valueOf(psseBus.getArea());
                case "zone" -> String.valueOf(psseBus.getZone());
                case "owner" -> String.valueOf(psseBus.getOwner());
                case "vm" -> String.valueOf(psseBus.getVm());
                case "va" -> String.valueOf(psseBus.getVa());
                case "nvhi" -> String.valueOf(psseBus.getNvhi());
                case "nvlo" -> String.valueOf(psseBus.getNvlo());
                case "evhi" -> String.valueOf(psseBus.getEvhi());
                case "evlo" -> String.valueOf(psseBus.getEvlo());
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
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
