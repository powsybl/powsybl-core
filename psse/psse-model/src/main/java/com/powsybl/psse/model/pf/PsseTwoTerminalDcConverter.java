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
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseTwoTerminalDcConverter extends PsseVersioned {

    @Parsed
    private int ip;

    @Parsed
    private int nb;

    @Parsed
    private double anmx;

    @Parsed
    private double anmn;

    @Parsed
    private double rc;

    @Parsed
    private double xc;

    @Parsed
    private double ebas;

    @Parsed
    private double tr = 1.0;

    @Parsed
    private double tap = 1.0;

    @Parsed
    private double tmx = 1.5;

    @Parsed
    private double tmn = 0.51;

    @Parsed
    private double stp = 0.00625;

    @Parsed
    private int ic = 0;

    // Originally the field name is "if" that is not allowed
    @Parsed(field = {"if"})
    private int ifx = 0;

    @Parsed
    private int it = 0;

    @Parsed(defaultNullRead = "1")
    private String id;

    @Parsed
    private double xcap = 0.0;

    @Parsed
    @Revision(since = 35)
    private int nd = 0;

    public int getIp() {
        return ip;
    }

    public void setIp(int ip) {
        this.ip = ip;
    }

    public int getNb() {
        return nb;
    }

    public void setNb(int nb) {
        this.nb = nb;
    }

    public double getAnmx() {
        return anmx;
    }

    public void setAnmx(double anmx) {
        this.anmx = anmx;
    }

    public double getAnmn() {
        return anmn;
    }

    public void setAnmn(double anmn) {
        this.anmn = anmn;
    }

    public double getRc() {
        return rc;
    }

    public void setRc(double rc) {
        this.rc = rc;
    }

    public double getXc() {
        return xc;
    }

    public void setXc(double xc) {
        this.xc = xc;
    }

    public double getEbas() {
        return ebas;
    }

    public void setEbas(double ebas) {
        this.ebas = ebas;
    }

    public double getTr() {
        return tr;
    }

    public void setTr(double tr) {
        this.tr = tr;
    }

    public double getTap() {
        return tap;
    }

    public void setTap(double tap) {
        this.tap = tap;
    }

    public double getTmx() {
        return tmx;
    }

    public void setTmx(double tmx) {
        this.tmx = tmx;
    }

    public double getTmn() {
        return tmn;
    }

    public void setTmn(double tmn) {
        this.tmn = tmn;
    }

    public double getStp() {
        return stp;
    }

    public void setStp(double stp) {
        this.stp = stp;
    }

    public int getIc() {
        return ic;
    }

    public void setIc(int ic) {
        this.ic = ic;
    }

    public int getIf() {
        return ifx;
    }

    public void setIf(int ifx) {
        this.ifx = ifx;
    }

    public int getIt() {
        return it;
    }

    public void setIt(int it) {
        this.it = it;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getXcap() {
        return xcap;
    }

    public void setXcap(double xcap) {
        this.xcap = xcap;
    }

    public int getNd() {
        checkVersion("nd");
        return nd;
    }

    public void setNd(int nd) {
        this.nd = nd;
    }
}
