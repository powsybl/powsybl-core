/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class RatioTapChangerAdderAdapter extends AbstractAdapter<RatioTapChangerAdder> implements RatioTapChangerAdder {

    protected RatioTapChangerAdderAdapter(final RatioTapChangerAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public RatioTapChangerAdapter add() {
        return getIndex().getRatioTapChanger(getDelegate().add());
    }

    @Override
    public RatioTapChangerAdderAdapter setRegulationTerminal(final Terminal regulationTerminal) {
        // RatioTapChangerAdderImpl.setRegulationTerminal is casting parameter to TerminalExt
        // So, we need to check if parameter is adapted or not
        final boolean isAdapted = regulationTerminal instanceof AbstractAdapter<?>;
        getDelegate().setRegulationTerminal(isAdapted ? ((AbstractAdapter<Terminal>) regulationTerminal).getDelegate() : regulationTerminal);
        return this;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public RatioTapChangerAdderAdapter setLowTapPosition(final int lowTapPosition) {
        getDelegate().setLowTapPosition(lowTapPosition);
        return this;
    }

    @Override
    public RatioTapChangerAdderAdapter setTapPosition(final int tapPosition) {
        getDelegate().setTapPosition(tapPosition);
        return this;
    }

    @Override
    public RatioTapChangerAdderAdapter setLoadTapChangingCapabilities(final boolean loadTapChangingCapabilities) {
        getDelegate().setLoadTapChangingCapabilities(loadTapChangingCapabilities);
        return this;
    }

    @Override
    public RatioTapChangerAdderAdapter setRegulating(final boolean regulating) {
        getDelegate().setRegulating(regulating);
        return this;
    }

    @Override
    public RatioTapChangerAdderAdapter setTargetV(final double targetV) {
        getDelegate().setTargetV(targetV);
        return this;
    }

    @Override
    public RatioTapChangerAdderAdapter setTargetDeadband(final double targetDeadband) {
        getDelegate().setTargetDeadband(targetDeadband);
        return this;
    }

    @Override
    public RatioTapChangerStepAdderAdapter beginStep() {
        return new RatioTapChangerStepAdderAdapter(this, getDelegate().beginStep(), getIndex());
    }
}
