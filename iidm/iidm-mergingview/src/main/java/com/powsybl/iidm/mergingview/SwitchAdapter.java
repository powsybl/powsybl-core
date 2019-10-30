/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class SwitchAdapter extends AbstractIdentifiableAdapter<Switch> implements Switch {

    protected SwitchAdapter(final Switch delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public SwitchKind getKind() {
        return getDelegate().getKind();
    }

    @Override
    public boolean isOpen() {
        return getDelegate().isOpen();
    }

    @Override
    public void setOpen(final boolean open) {
        getDelegate().setOpen(open);
    }

    @Override
    public boolean isRetained() {
        return getDelegate().isRetained();
    }

    @Override
    public void setRetained(final boolean retained) {
        getDelegate().setRetained(retained);
    }

    @Override
    public boolean isFictitious() {
        return getDelegate().isFictitious();
    }

    @Override
    public void setFictitious(final boolean fictitious) {
        getDelegate().setFictitious(fictitious);
    }

    @Override
    public VoltageLevelAdapter getVoltageLevel() {
        return getIndex().getVoltageLevel(getDelegate().getVoltageLevel());
    }
}
