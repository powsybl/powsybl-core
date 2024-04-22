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
import com.univocity.parsers.annotations.NullString;
import com.univocity.parsers.annotations.Parsed;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseTransformerWinding extends PsseVersioned {
    @NullString(nulls = {"null"})
    @Parsed
    private double windv = Double.NaN;

    @NullString(nulls = {"null"})
    @Parsed
    private double nomv = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double ang = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private int cod = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private int cont = 0;

    @NullString(nulls = {"null"})
    @Parsed
    @Revision(since = 35)
    private int node = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double rma = Double.NaN;

    @NullString(nulls = {"null"})
    @Parsed
    private double rmi = Double.NaN;

    @NullString(nulls = {"null"})
    @Parsed
    private double vma = Double.NaN;

    @NullString(nulls = {"null"})
    @Parsed
    private double vmi = Double.NaN;

    @NullString(nulls = {"null"})
    @Parsed
    private int ntp = 33;

    @NullString(nulls = {"null"})
    @Parsed
    private int tab = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double cr = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double cx = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double cnxa = 0;

    public double getWindv() {
        return windv;
    }

    public void setWindv(double windv) {
        this.windv = windv;
    }

    public double getNomv() {
        return nomv;
    }

    public double getAng() {
        return ang;
    }

    public void setAng(double ang) {
        this.ang = ang;
    }

    public int getCod() {
        return cod;
    }

    public void setCont(int cont) {
        this.cont = cont;
    }

    public int getCont() {
        return cont;
    }

    public int getNode() {
        checkVersion("node");
        return node;
    }

    public double getRma() {
        return rma;
    }

    public double getRmi() {
        return rmi;
    }

    public double getVma() {
        return vma;
    }

    public double getVmi() {
        return vmi;
    }

    public int getNtp() {
        return ntp;
    }

    public int getTab() {
        return tab;
    }

    public double getCr() {
        return cr;
    }

    public double getCx() {
        return cx;
    }

    public double getCnxa() {
        return cnxa;
    }

    public PsseTransformerWinding copy() {
        PsseTransformerWinding copy = new PsseTransformerWinding();
        copy.windv = this.windv;
        copy.nomv = this.nomv;
        copy.ang = this.ang;
        copy.cod = this.cod;
        copy.cont = this.cont;
        copy.node = this.node;
        copy.rma = this.rma;
        copy.rmi = this.rmi;
        copy.vma = this.vma;
        copy.vmi = this.vmi;
        copy.ntp = this.ntp;
        copy.tab = this.tab;
        copy.cr = this.cr;
        copy.cx = this.cx;
        copy.cnxa = this.cnxa;
        return copy;
    }
}
