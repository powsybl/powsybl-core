/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.model;

/**
 *
 * @author Caio Luke {@literal <caio.luke at rte-france.com>}
 */
public class MSwitch {

    /**
     * from bus number
     */
    private int from;

    /**
     * to bus number
     */
    private int to;

    /**
     * real power flow at “from” bus end (MW), “from” → “to”
     */
    private double psw;

    /**
     * reactive power flow at “from” bus end (MW), “from” → “to”
     */
    private double qsw;

    /**
     * initial switch state, 1 = closed, 0 = open
     */
    private int state;

    /**
     * MVA thermal rating, set to 0 for unlimited
     */
    private double thermalRating;

    /**
     * initial switch status, 1 = in-service, 0 = out-of-service
     */
    private int status;

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public double getPsw() { return psw; }

    public void setPsw(double psw) { this.psw = psw; }

    public double getQsw() { return qsw; }

    public void setQsw(double qsw) { this.qsw = qsw; }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public double getThermalRating() {
        return thermalRating;
    }

    public void setThermalRating(double thermalRating) {
        this.thermalRating = thermalRating;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
