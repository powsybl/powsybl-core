/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseInterareaTransfer {

    @Parsed
    private int arfrom;

    @Parsed
    private int arto;

    @Parsed(defaultNullRead = "1")
    private String trid;

    @Parsed
    private double ptran = 0.0;

    public int getArfrom() {
        return arfrom;
    }

    public void setArfrom(int arfrom) {
        this.arfrom = arfrom;
    }

    public int getArto() {
        return arto;
    }

    public void setArto(int arto) {
        this.arto = arto;
    }

    public String getTrid() {
        return trid;
    }

    public void setTrid(String trid) {
        this.trid = trid;
    }

    public double getPtran() {
        return ptran;
    }

    public void setPtran(double ptran) {
        this.ptran = ptran;
    }
}
