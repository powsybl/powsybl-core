/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag.network;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EsgDissymmetricalBranch {
    private final EsgBranchName name;
    private final EsgBranchConnectionStatus status;
    private final float rb; // total resistance sending node towards receiving node [p.u.]
    private final float rxb; // total reactance sending node towards receiving node [p.u.]
    private final float gs; // sending side shunt conductance [p.u.]
    private final float bs; // sending side shunt susceptance [p.u.]
    private final float rate; // branch rated power [MVA]
    private final float rb2; // total resistance receiving node towards sending node [p.u.]
    private final float rxb2; // total reactance receiving node towards sending node [p.u.]
    private final float gs2; // receiving side shunt conductance [p.u.]
    private final float bs2; // receiving side shunt susceptance [p.u.]

    public EsgDissymmetricalBranch(EsgBranchName name, EsgBranchConnectionStatus status, float rb, float rxb, float gs, float bs, float rate, float rb2, float rxb2, float gs2, float bs2) {
        this.name = Objects.requireNonNull(name);
        this.status = Objects.requireNonNull(status);
        this.rb = rb;
        this.rxb = rxb;
        this.gs = gs;
        this.bs = bs;
        this.rate = rate;
        this.rb2 = rb2;
        this.rxb2 = rxb2;
        this.gs2 = gs2;
        this.bs2 = bs2;
    }

    public EsgBranchName getName() {
        return name;
    }

    public EsgBranchConnectionStatus getStatus() {
        return status;
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

    public float getRb2() {
        return rb2;
    }

    public float getRxb2() {
        return rxb2;
    }

    public float getGs2() {
        return gs2;
    }

    public float getBs2() {
        return bs2;
    }

    public float getRate() {
        return rate;
    }

}
