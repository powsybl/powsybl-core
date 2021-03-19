/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.RegulationMode;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class StaticVarCompensatorAdderAdapter extends AbstractInjectionAdderAdapter<StaticVarCompensatorAdder> implements StaticVarCompensatorAdder {

    StaticVarCompensatorAdderAdapter(StaticVarCompensatorAdder delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public StaticVarCompensator add() {
        checkAndSetUniqueId();
        return getIndex().getStaticVarCompensator(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public StaticVarCompensatorAdder setBmin(double bMin) {
        getDelegate().setBmin(bMin);
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setBmax(double bMax) {
        getDelegate().setBmax(bMax);
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setVoltageSetpoint(double voltageSetpoint) {
        getDelegate().setVoltageSetpoint(voltageSetpoint);
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setReactivePowerSetpoint(double reactivePowerSetpoint) {
        getDelegate().setReactivePowerSetpoint(reactivePowerSetpoint);
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setRegulationMode(RegulationMode regulationMode) {
        getDelegate().setRegulationMode(regulationMode);
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setRegulatingTerminal(final Terminal regulatingTerminal) {
        Terminal terminal = regulatingTerminal;
        if (terminal instanceof TerminalAdapter) {
            terminal = ((TerminalAdapter) terminal).getDelegate();
        }
        getDelegate().setRegulatingTerminal(terminal);
        return this;
    }
}
