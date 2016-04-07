/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag.network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EsgLine {

    private final EsgBranchName name;
    private final EsgBranchConnectionStatus status;
    private final float rb; // total line resistance [p.u.]
    private final float rxb; // total line reactance [p.u.]
    private final float gs; // semi shunt conductance [p.u.]
    private final float bs; // semi shunt susceptance [p.u.]
    private final float rate; // line rated power [MVA].

    public EsgLine(EsgBranchName name, EsgBranchConnectionStatus status, float rb, float rxb, float gs, float bs, float rate) {
        this.name = name;
        this.status = status;
        this.rb = rb;
        this.rxb = rxb;
        this.gs = gs;
        this.bs = bs;
        this.rate = rate;
    }

    public EsgBranchName getName() {
        return name;
    }

    public EsgBranchConnectionStatus getStatus() {
        return status;
    }

    public float getRate() {
        return rate;
    }

    public float getRb() {
        return rb;
    }

    public float getRxb() {
        return rxb;
    }

    public float getGs() {
        return gs;
    }

    public float getBs() {
        return bs;
    }

}
