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
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setId(final String id) {
        getDelegate().setId(id);
        return this;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        getDelegate().setEnsureIdUnicity(ensureIdUnicity);
        return this;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setName(final String name) {
        getDelegate().setName(name);
        return this;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setNode1(final int node1) {
        getDelegate().setNode1(node1);
        return this;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setNode2(final int node2) {
        getDelegate().setNode2(node2);
        return this;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setKind(final SwitchKind kind) {
        getDelegate().setKind(kind);
        return this;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setKind(final String kind) {
        getDelegate().setKind(kind);
        return this;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setOpen(final boolean open) {
        getDelegate().setOpen(open);
        return this;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setRetained(final boolean retained) {
        getDelegate().setRetained(retained);
        return this;
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter setFictitious(final boolean fictitious) {
        getDelegate().setFictitious(fictitious);
        return this;
    }

    @Override
    public SwitchAdapter add() {
        return new SwitchAdapter(getDelegate().add(), getIndex());
    }
}
