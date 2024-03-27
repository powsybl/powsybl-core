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
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseBus extends PsseVersioned {

    @Parsed(field = {"i", "ibus"})
    private int i;

    @Parsed(defaultNullRead = "            ")
    private String name;

    @Parsed
    private double baskv = 0;

    @Parsed
    private int ide = 1;

    @Parsed
    private int area = 1;

    @Parsed
    private int zone = 1;

    @Parsed
    private int owner = 1;

    @Parsed
    private double vm = 1;

    @Parsed
    private double va = 0;

    @Parsed
    @Revision(since = 33)
    private double nvhi = 1.1;

    @Parsed
    @Revision(since = 33)
    private double nvlo = 0.9;

    @Parsed
    @Revision(since = 33)
    private double evhi = 1.1;

    @Parsed
    @Revision(since = 33)
    private double evlo = 0.9;

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
