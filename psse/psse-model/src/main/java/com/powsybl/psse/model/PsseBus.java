/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseBus {

    @Parsed(index = 0)
    private int i;

    @Parsed(index = 1)
    private String name;

    @Parsed(index = 2)
    private double baskv;

    @Parsed(index = 3)
    private int ide;

    @Parsed(index = 4)
    private int area;

    @Parsed(index = 5)
    private int zone;

    @Parsed(index = 6)
    private int owner;

    @Parsed(index = 7)
    private double vm;

    @Parsed(index = 8)
    private double va;

    @Parsed(index = 9)
    private double nvhi;

    @Parsed(index = 10)
    private double nvlo;

    @Parsed(index = 11)
    private double evhi;

    @Parsed(index = 12)
    private double evlo;

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
