/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ThreeWindingsTransformerAdderAdapter extends AbstractAdapter<ThreeWindingsTransformerAdder> implements ThreeWindingsTransformerAdder {

    protected ThreeWindingsTransformerAdderAdapter(final ThreeWindingsTransformerAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public ThreeWindingsTransformerAdapter add() {
        return getIndex().getThreeWindingsTransformer(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ThreeWindingsTransformerAdderAdapter setId(final String id) {
        getDelegate().setId(id);
        return this;
    }

    @Override
    public ThreeWindingsTransformerAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        getDelegate().setEnsureIdUnicity(ensureIdUnicity);
        return this;
    }

    @Override
    public ThreeWindingsTransformerAdderAdapter setName(final String name) {
        getDelegate().setName(name);
        return this;
    }

    @Override
    public Leg1AdderAdapter newLeg1() {
        return new Leg1AdderAdapter(this, getDelegate().newLeg1(), getIndex());
    }

    @Override
    public Leg2or3AdderAdapter newLeg2() {
        return new Leg2or3AdderAdapter(this, getDelegate().newLeg2(), getIndex());
    }

    @Override
    public Leg2or3AdderAdapter newLeg3() {
        return new Leg2or3AdderAdapter(this, getDelegate().newLeg3(), getIndex());
    }
}
