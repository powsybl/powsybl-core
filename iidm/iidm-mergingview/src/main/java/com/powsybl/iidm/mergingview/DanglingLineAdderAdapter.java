/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.DanglingLineAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DanglingLineAdderAdapter extends AbstractInjectionAdderAdapter<DanglingLineAdder> implements DanglingLineAdder {

    DanglingLineAdderAdapter(final DanglingLineAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public DanglingLine add() {
        checkAndSetUniqueId();
        final DanglingLine dl = getDelegate().add();
        getIndex().checkNewDanglingLine(dl);
        return getIndex().getDanglingLine(dl);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public DanglingLineAdder setP0(final double p0) {
        getDelegate().setP0(p0);
        return this;
    }

    @Override
    public DanglingLineAdder setQ0(final double q0) {
        getDelegate().setQ0(q0);
        return this;
    }

    @Override
    public DanglingLineAdder setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public DanglingLineAdder setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public DanglingLineAdder setG(final double g) {
        getDelegate().setG(g);
        return this;
    }

    @Override
    public DanglingLineAdder setB(final double b) {
        getDelegate().setB(b);
        return this;
    }

    @Override
    public DanglingLineAdder setActivePowerSetpoint(final double activePowerSetpoint) {
        getDelegate().setActivePowerSetpoint(activePowerSetpoint);
        return this;
    }

    @Override
    public DanglingLineAdder setReactivePowerSetpoint(final double reactivePowerSetpoint) {
        getDelegate().setReactivePowerSetpoint(reactivePowerSetpoint);
        return this;
    }

    @Override
    public DanglingLineAdder setVoltageRegulationOn(final boolean voltageRegulationOn) {
        getDelegate().setVoltageRegulationOn(voltageRegulationOn);
        return this;
    }

    @Override
    public DanglingLineAdder setVoltageSetpoint(final double voltageSetpoint) {
        getDelegate().setVoltageSetpoint(voltageSetpoint);
        return this;
    }

    @Override
    public DanglingLineAdder setUcteXnodeCode(final String ucteXnodeCode) {
        getDelegate().setUcteXnodeCode(ucteXnodeCode);
        return this;
    }
}
