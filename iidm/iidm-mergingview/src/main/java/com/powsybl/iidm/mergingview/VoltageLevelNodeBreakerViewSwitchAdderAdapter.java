/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageLevel.NodeBreakerView.SwitchAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VoltageLevelNodeBreakerViewSwitchAdderAdapter extends AbstractAdapter<VoltageLevel.NodeBreakerView.SwitchAdder> implements VoltageLevel.NodeBreakerView.SwitchAdder {

    protected VoltageLevelNodeBreakerViewSwitchAdderAdapter(final SwitchAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setId(final String id) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setName(final String name) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setNode1(final int node1) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setNode2(final int node2) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setKind(final SwitchKind kind) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setKind(final String kind) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setOpen(final boolean open) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setRetained(final boolean retained) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setFictitious(final boolean fictitious) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public SwitchAdapter add() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
