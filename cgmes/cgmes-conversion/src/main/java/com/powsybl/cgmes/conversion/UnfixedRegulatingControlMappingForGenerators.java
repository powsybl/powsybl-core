/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class UnfixedRegulatingControlMappingForGenerators extends AbstractRegulatingControlMappingForGenerators {

    UnfixedRegulatingControlMappingForGenerators(RegulatingControlMapping parent, Context context) {
        super(parent, context);
    }

    @Override
    public void initialize(GeneratorAdder adder) {
        // do nothing
    }

    @Override
    protected boolean setRegulatingControlVoltage(String controlId, RegulatingControlMapping.RegulatingControl control, double qPercent, boolean eqControlEnabled, Generator gen) {
        // Null terminal if it has not been defined in CGMES file
        Terminal terminal = parent.findRegulatingTerminal(control.cgmesTerminal);

        double targetV = control.targetValue;
        if (control.targetValue <= 0.0) {
            targetV = Double.NaN;
        }
        if (Double.isNaN(control.targetValue)) {
            context.invalid(() -> controlId, () -> String.format("Invalid value %s for regulating target value", control.targetValue));
        }

        boolean voltageRegulatorOn = control.enabled && eqControlEnabled;
        // Regulating control is enabled AND this equipment participates in regulating control
        setRegulatingControlVoltage(controlId, targetV, voltageRegulatorOn, terminal, qPercent, gen);
        return true;
    }

    @Override
    protected boolean setRegulatingControlReactivePower(String controlId, RegulatingControlMapping.RegulatingControl control, double qPercent, boolean eqControlEnabled, Generator gen) {
        // Ignore control if the terminal is not mapped.
        Terminal terminal = parent.findRegulatingTerminal(control.cgmesTerminal, false);
        if (terminal == null) {
            context.invalid(() -> controlId, () -> String.format("Regulation terminal %s is not mapped or mapped to a switch", control.cgmesTerminal));
        }

        double targetQ = control.targetValue;
        if (Double.isNaN(control.targetValue)) {
            context.invalid(() -> controlId, () -> "Invalid value for regulating target value. Real flows are considered targets.");
        }
        setRegulatingControlReactivePower(controlId, targetQ, control.enabled && eqControlEnabled, terminal, qPercent, gen);
        return true;
    }
}
