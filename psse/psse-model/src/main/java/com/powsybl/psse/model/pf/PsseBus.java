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
import de.siegmar.fastcsv.reader.CsvRecord;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

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

    public static PsseBus fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseBus psseBus = new PsseBus();
        psseBus.setI(parseIntFromRecord(rec, headers, "i", "ibus"));
        psseBus.setName(parseStringFromRecord(rec, "            ", headers, "name"));
        psseBus.setBaskv(parseDoubleFromRecord(rec, 0d, headers, "baskv"));
        psseBus.setIde(parseIntFromRecord(rec, 1, headers, "ide"));
        psseBus.setArea(parseIntFromRecord(rec, 1, headers, "area"));
        psseBus.setZone(parseIntFromRecord(rec, 1, headers, "zone"));
        psseBus.setOwner(parseIntFromRecord(rec, 1, headers, "owner"));
        psseBus.setVm(parseDoubleFromRecord(rec, 1d, headers, "vm"));
        psseBus.setVa(parseDoubleFromRecord(rec, 0d, headers, "va"));
        if (version.getMajorNumber() >= 33) {
            psseBus.setNvhi(parseDoubleFromRecord(rec, 1.1, headers, "nvhi"));
            psseBus.setNvlo(parseDoubleFromRecord(rec, 0.9, headers, "nvlo"));
            psseBus.setEvhi(parseDoubleFromRecord(rec, 1.1, headers, "evhi"));
            psseBus.setEvlo(parseDoubleFromRecord(rec, 0.9, headers, "evlo"));
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
