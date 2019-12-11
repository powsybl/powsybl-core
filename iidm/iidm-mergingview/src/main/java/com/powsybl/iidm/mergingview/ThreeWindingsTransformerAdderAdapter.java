/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ThreeWindingsTransformerAdderAdapter extends AbstractIdentifiableAdderAdapter<ThreeWindingsTransformerAdder> implements ThreeWindingsTransformerAdder {

    ThreeWindingsTransformerAdderAdapter(final ThreeWindingsTransformerAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public ThreeWindingsTransformer add() {
        checkAndSetUniqueId();
        return getIndex().getThreeWindingsTransformer(getDelegate().add());
    }

    @Override
    public ThreeWindingsTransformerAdder.LegAdder newLeg1() {
        return new LegAdderAdapter(this, getDelegate().newLeg1(), getIndex());
    }

    @Override
    public ThreeWindingsTransformerAdder.LegAdder newLeg2() {
        return new LegAdderAdapter(this, getDelegate().newLeg2(), getIndex());
    }

    @Override
    public ThreeWindingsTransformerAdder.LegAdder newLeg3() {
        return new LegAdderAdapter(this, getDelegate().newLeg3(), getIndex());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ThreeWindingsTransformerAdder setRatedU0(double ratedU0) {
        getDelegate().setRatedU0(ratedU0);
        return this;
    }
}
