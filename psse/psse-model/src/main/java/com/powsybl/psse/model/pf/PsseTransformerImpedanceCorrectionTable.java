/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import com.univocity.parsers.annotations.Parsed;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
@JsonPropertyOrder(alphabetic = false)
public class PsseTransformerImpedanceCorrectionTable extends PsseVersioned {

    @Parsed(field = {"i", "itable"})
    private int i;

    // XXX(Luma) First attempt to read impedance correction tables, we define as fields a subset of taps and factors (only 3 sets)
    // XXX(Luma) For RAWX 35 there is a DataTable of fields: (itable, tap, refact, imfat)

    @Parsed(field = {"t1", "tap1"})
    private double t1 = 0;

    @Parsed(field = {"f1", "ref1", "refact1"})
    private double ref1 = 0;

    @Parsed(field = {"imf1", "imfact1"})
    @Revision(since = 35)
    private double imf1 = 0;

    @Parsed(field = {"t2", "tap2"})
    private double t2 = 0;

    @Parsed(field = {"f2", "ref2"})
    private double ref2 = 0;

    @Parsed(field = {"imf2", "imfact2"})
    @Revision(since = 35)
    private double imf2 = 0;

    @Parsed(field = {"t3", "tap3"})
    private double t3 = 0;

    @Parsed(field = {"f3", "ref3"})
    private double ref3 = 0;

    @Parsed(field = {"imf3", "imfact3"})
    @Revision(since = 35)
    private double imf3 = 0;

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public double getT1() {
        return t1;
    }

    public void setT1(double t1) {
        this.t1 = t1;
    }

    public double getT2() {
        return t2;
    }

    public void setT2(double t2) {
        this.t2 = t2;
    }

    public double getT3() {
        return t3;
    }

    public void setT3(double t3) {
        this.t3 = t3;
    }

    public double getRef1() {
        return ref1;
    }

    public void setRef1(double ref1) {
        this.ref1 = ref1;
    }

    public double getImf1() {
        return imf1;
    }

    public void setImf1(double imf1) {
        this.imf1 = imf1;
    }

    public double getRef2() {
        return ref2;
    }

    public void setRef2(double ref2) {
        this.ref2 = ref2;
    }

    public double getImf2() {
        return imf2;
    }

    public void setImf2(double imf2) {
        this.imf2 = imf2;
    }

    public double getRef3() {
        return ref3;
    }

    public void setRef3(double ref3) {
        this.ref3 = ref3;
    }

    public double getImf3() {
        return imf3;
    }

    public void setImf3(double imf3) {
        this.imf3 = imf3;
    }
}
