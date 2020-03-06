/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Validate;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseLoad {

    @Parsed(index = 0)
    @Validate
    private int i;

    @Parsed(index = 1)
    private String id = "1";

    @Parsed(index = 2)
    private int status = 1;

    @Parsed(index = 3)
    private int area = -1;

    @Parsed(index = 4)
    private int zone = -1;

    @Parsed(index = 5)
    private double pl = 0;

    @Parsed(index = 6)
    private double ql = 0;

    @Parsed(index = 7)
    private double ip = 0;

    @Parsed(index = 8)
    private double iq = 0;

    @Parsed(index = 9)
    private double yp = 0;

    @Parsed(index = 10)
    private double yq = 0;

    @Parsed(index = 11)
    private int owner = -1;

    @Parsed(index = 12)
    private int scale = 1;

    @Parsed(index = 13)
    private int intrpt = 0;

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

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getIntrpt() {
        return intrpt;
    }

    public void setIntrpt(int intrpt) {
        this.intrpt = intrpt;
    }
}
