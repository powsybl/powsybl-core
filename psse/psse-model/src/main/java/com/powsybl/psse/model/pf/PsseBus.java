/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf;

import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseBus {

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
    private double nvhi = 1.1;

    @Parsed
    private double nvlo = 0.9;

    @Parsed
    private double evhi = 1.1;

    @Parsed
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
        return nvhi;
    }

    public void setNvhi(double nvhi) {
        this.nvhi = nvhi;
    }

    public double getNvlo() {
        return nvlo;
    }

    public void setNvlo(double nvlo) {
        this.nvlo = nvlo;
    }

    public double getEvhi() {
        return evhi;
    }

    public void setEvhi(double evhi) {
        this.evhi = evhi;
    }

    public double getEvlo() {
        return evlo;
    }

    public void setEvlo(double evlo) {
        this.evlo = evlo;
    }
}
