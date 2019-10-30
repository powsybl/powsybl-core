/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VoltageLevelNodeBreakerViewSwitchAdderAdapter extends AbstractIdentifiableAdderAdapter<VoltageLevel.NodeBreakerView.SwitchAdder> implements VoltageLevel.NodeBreakerView.SwitchAdder {

    VoltageLevelNodeBreakerViewSwitchAdderAdapter(final VoltageLevel.NodeBreakerView.SwitchAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Switch add() {
        checkAndSetUniqueId();
        return getIndex().getSwitch(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
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
}
