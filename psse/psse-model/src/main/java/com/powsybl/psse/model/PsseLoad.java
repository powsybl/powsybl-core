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
public class PsseLoad {

    @Parsed(index = 0)
    private int i;

    @Parsed(index = 1)
    private String id;

    @Parsed(index = 2)
    private int status;

    @Parsed(index = 3)
    private int area;

    @Parsed(index = 4)
    private int zone;

    @Parsed(index = 5)
    private double pl;

    @Parsed(index = 6)
    private double ql;

    @Parsed(index = 7)
    private double ip;

    @Parsed(index = 8)
    private double iq;

    @Parsed(index = 9)
    private double yp;

    @Parsed(index = 10)
    private double yq;

    @Parsed(index = 11)
    private double owner;

    @Parsed(index = 12)
    private double scale;

    @Parsed(index = 13)
    private double intrpt;

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public double getPl() {
        return pl;
    }

    public void setPl(double pl) {
        this.pl = pl;
    }

    public double getQl() {
        return ql;
    }

    public void setQl(double ql) {
        this.ql = ql;
    }

    public double getIp() {
        return ip;
    }

    public void setIp(double ip) {
        this.ip = ip;
    }

    public double getIq() {
        return iq;
    }

    public void setIq(double iq) {
        this.iq = iq;
    }

    public double getYp() {
        return yp;
    }

    public void setYp(double yp) {
        this.yp = yp;
    }

    public double getYq() {
        return yq;
    }

    public void setYq(double yq) {
        this.yq = yq;
    }

    public double getOwner() {
        return owner;
    }

    public void setOwner(double owner) {
        this.owner = owner;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getIntrpt() {
        return intrpt;
    }

    public void setIntrpt(double intrpt) {
        this.intrpt = intrpt;
    }
}
