/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Terminal;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public class FixedRegulatingControlMappingForGenerators extends AbstractRegulatingControlMappingForGenerators {

    FixedRegulatingControlMappingForGenerators(RegulatingControlMapping parent, Context context) {
        super(parent, context);
    }

    @Override
    public void initialize(GeneratorAdder adder) {
        adder.setVoltageRegulatorOn(false);
    }

    @Override
    protected boolean setRegulatingControlVoltage(String controlId,
                                                  RegulatingControl control, double qPercent, boolean eqControlEnabled, Generator gen) {
        boolean voltageRegulatorOn = control.enabled && eqControlEnabled;
        double targetV = control.targetValue;
        Terminal terminal = parent.findRegulatingTerminal(control.cgmesTerminal);
        if (voltageRegulatorOn && terminal == null) { // If regulating terminal null, regulation is made local
            context.fixed(controlId, () -> "Unmapped regulating terminal and impossible to find an equivalent terminal. Regulation is made local.");
            terminal = gen.getTerminal();
            targetV = terminal.getVoltageLevel().getNominalV();
        }
        if (voltageRegulatorOn && (control.targetValue <= 0.0 || Double.isNaN(control.targetValue))) {
            targetV = terminal.getVoltageLevel().getNominalV();
            context.fixed(controlId, "Invalid value for regulating target value. Regulation value fixed as nominal voltage of regulating terminal.", control.targetValue, targetV);
        }

        // Regulating control is enabled AND this equipment participates in regulating control
        setRegulatingControlVoltage(controlId, targetV, voltageRegulatorOn, terminal, qPercent, gen);
        return true;
    }

    @Override
    protected boolean setRegulatingControlReactivePower(String controlId, RegulatingControl control, double qPercent, boolean eqControlEnabled, Generator gen) {
        // Ignore control if the terminal is not mapped.
        Terminal terminal = parent.findRegulatingTerminal(control.cgmesTerminal, false);
        if (terminal == null) {
            context.ignored(controlId, String.format("Regulation terminal %s is not mapped or mapped to a switch", control.cgmesTerminal));
            return false;
        }

        double targetQ;
        if (Double.isNaN(control.targetValue)) {
            context.fixed(controlId, "Invalid value for regulating target value. Real flows are considered targets.");
            return false;
        } else {
            targetQ = control.targetValue;
        }
        setRegulatingControlReactivePower(controlId, targetQ, control.enabled && eqControlEnabled, terminal, qPercent, gen);
        return true;
    }
}
