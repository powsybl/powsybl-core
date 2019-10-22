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
    // Not implemented methods -------
    // -------------------------------
    @Override
    public VoltageLevelAdapter getVoltageLevel() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public SwitchKind getKind() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean isOpen() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void setOpen(final boolean open) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean isRetained() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void setRetained(final boolean retained) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public boolean isFictitious() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void setFictitious(final boolean fictitious) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
