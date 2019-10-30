/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VoltageLevelBusBreakerViewSwitchAdderAdapter extends AbstractIdentifiableAdderAdapter<VoltageLevel.BusBreakerView.SwitchAdder> implements VoltageLevel.BusBreakerView.SwitchAdder {

    VoltageLevelBusBreakerViewSwitchAdderAdapter(final VoltageLevel.BusBreakerView.SwitchAdder delegate, final MergingViewIndex index) {
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
    public VoltageLevel.BusBreakerView.SwitchAdder setBus1(String bus1) {
        getDelegate().setBus1(bus1);
        return this;
    }

    @Override
    public VoltageLevel.BusBreakerView.SwitchAdder setBus2(String bus2) {
        getDelegate().setBus2(bus2);
        return this;
    }

    @Override
    public VoltageLevel.BusBreakerView.SwitchAdder setOpen(final boolean open) {
        getDelegate().setOpen(open);
        return this;
    }

    @Override
    public VoltageLevel.BusBreakerView.SwitchAdder setFictitious(final boolean fictitious) {
        getDelegate().setFictitious(fictitious);
        return this;
    }
}
