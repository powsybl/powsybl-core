/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.conversion.RegulatingTerminalMapper.TerminalAndSign;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Map;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_MODE;
import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_TERMINAL_SIGN;

/**
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */

public class RegulatingControlMappingForGenerators {

    RegulatingControlMappingForGenerators(RegulatingControlMapping parent, Context context) {
        this.parent = parent;
        this.context = context;
        mapping = new HashMap<>();
    }

    public void add(String generatorId, PropertyBag sm) {
        String rcId = RegulatingControlMapping.getRegulatingControlId(sm);

        if (mapping.containsKey(generatorId)) {
            throw new CgmesModelException("Generator already added, IIDM Generator Id: " + generatorId);
        }

        mapping.put(generatorId, rcId);
    }

    void applyRegulatingControls(Network network) {
        network.getGeneratorStream().forEach(this::apply);
    }

    private void apply(Generator gen) {
        String controlId = mapping.get(gen.getId());
        if (controlId == null) {
            return;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            return;
        }

        boolean okSet = false;
        if (RegulatingControlMapping.isControlModeVoltage(control.mode)) {
            okSet = setRegulatingControlVoltage(controlId, control, gen);
        } else if (RegulatingControlMapping.isControlModeReactivePower(control.mode)) {
            okSet = setRegulatingControlReactivePower(controlId, control, gen);
        } else {
            context.ignored(control.mode, "Unsupported regulation mode for generator " + gen.getId());
        }
        control.setCorrectlySet(okSet);
    }

    private boolean setRegulatingControlVoltage(String controlId, RegulatingControl control, Generator gen) {

        // Take default terminal if it has not been defined in CGMES file (it is never null)
        Terminal regulatingTerminal = RegulatingTerminalMapper
                .mapForVoltageControl(control.cgmesTerminal, context)
                .orElse(gen.getTerminal());

        if (regulatingTerminal.getConnectable().getId().equals(gen.getId())) {
            regulatingTerminal = null;
        }
        gen.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(regulatingTerminal)
            .withRegulating(false)
            .build();

        gen.setProperty(Conversion.PROPERTY_REGULATING_CONTROL, controlId);
        gen.setProperty(PROPERTY_MODE, control.mode);

        return true;
    }

    private boolean setRegulatingControlReactivePower(String controlId, RegulatingControl control, Generator gen) {
        // Ignore control if the terminal is not mapped.
        TerminalAndSign mappedRegulatingTerminal = RegulatingTerminalMapper
                .mapForFlowControl(control.cgmesTerminal, context)
                .orElseGet(() -> new TerminalAndSign(null, 1));

        if (mappedRegulatingTerminal.getTerminal() == null) {
            context.ignored(controlId, String.format("Regulation terminal %s is not mapped or mapped to a switch", control.cgmesTerminal));
            return false;
        }

        VoltageRegulationBuilder voltageRegulationBuilder = gen.newVoltageRegulation()
            .withMode(RegulationMode.REACTIVE_POWER)
            .withRegulating(false)
            // always set the terminal in case of reactive power regulation
            .withTerminal(mappedRegulatingTerminal.getTerminal());
        voltageRegulationBuilder.build();

        gen.setProperty(Conversion.PROPERTY_REGULATING_CONTROL, controlId);
        gen.setProperty(PROPERTY_MODE, control.mode);
        gen.setProperty(PROPERTY_TERMINAL_SIGN, String.valueOf(mappedRegulatingTerminal.getSign()));

        return true;
    }

    private final RegulatingControlMapping parent;
    private final Map<String, String> mapping;
    private final Context context;
}
