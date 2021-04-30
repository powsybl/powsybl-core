/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public class RegulatingControlMappingForGenerators {

    private static final String QPERCENT = "qPercent";

    RegulatingControlMappingForGenerators(RegulatingControlMapping parent, Context context) {
        this.parent = parent;
        this.context = context;
        mapping = new HashMap<>();
    }

    public static void initialize(GeneratorAdder adder) {
        adder.setRegulationMode(RegulationMode.OFF);
    }

    public void add(String generatorId, PropertyBag sm) {
        String cgmesRegulatingControlId = RegulatingControlMapping.getRegulatingControlId(sm);
        double qPercent = sm.asDouble(QPERCENT);

        if (mapping.containsKey(generatorId)) {
            throw new CgmesModelException("Generator already added, IIDM Generator Id: " + generatorId);
        }

        CgmesRegulatingControlForGenerator rd = new CgmesRegulatingControlForGenerator();
        rd.regulatingControlId = cgmesRegulatingControlId;
        rd.qPercent = qPercent;
        rd.controlEnabled = sm.asBoolean("controlEnabled", false);
        mapping.put(generatorId, rd);
    }

    void applyRegulatingControls(Network network) {
        network.getGeneratorStream().forEach(this::apply);
    }

    private void apply(Generator gen) {
        CgmesRegulatingControlForGenerator rd = mapping.get(gen.getId());
        apply(gen, rd);
    }

    private void apply(Generator gen, CgmesRegulatingControlForGenerator rc) {
        if (rc == null) {
            return;
        }

        String controlId = rc.regulatingControlId;
        if (controlId == null) {
            context.missing("Regulating control Id not defined");
            return;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            return;
        }

        boolean okSet = false;
        if (RegulatingControlMapping.isControlModeVoltage(control.mode)) {
            okSet = setRegulatingControlVoltage(controlId, control, rc.qPercent, rc.controlEnabled, gen);
        } else if (RegulatingControlMapping.isControlModeReactivePower(control.mode)) {
            okSet = setRegulatingControlReactivePower(controlId, control, rc.qPercent, rc.controlEnabled, gen);
        } else {
            context.ignored(control.mode, "Unsupported regulation mode for generator " + gen.getId());
        }
        control.setCorrectlySet(okSet);
    }

    private boolean setRegulatingControlVoltage(String controlId,
                                                RegulatingControl control, double qPercent, boolean eqControlEnabled, Generator gen) {

        // Take default terminal if it has not been defined in CGMES file (it is never null)
        Terminal terminal = parent.getRegulatingTerminal(gen, control.cgmesTerminal);

        double targetV;
        if (control.targetValue <= 0.0 || Double.isNaN(control.targetValue)) {
            targetV = terminal.getVoltageLevel().getNominalV();
            terminal = gen.getTerminal();
            context.fixed(controlId, "Invalid value for regulating target value. Regulation considered as local.", control.targetValue, targetV);
        } else {
            targetV = control.targetValue;
        }

        RegulationMode regulationMode = RegulationMode.OFF;
        // Regulating control is enabled AND this equipment participates in regulating control
        if (control.enabled && eqControlEnabled) {
            regulationMode = RegulationMode.VOLTAGE;
        }

        gen.setRegulatingTerminal(terminal)
                .setTargetV(targetV)
                .setRegulationMode(regulationMode);

        // add qPercent as an extension
        if (!Double.isNaN(qPercent)) {
            gen.newExtension(CoordinatedReactiveControlAdder.class)
                    .withQPercent(qPercent)
                    .add();
        }
        gen.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", controlId);

        return true;
    }

    private boolean setRegulatingControlReactivePower(String controlId, RegulatingControl control, double qPercent, boolean eqControlEnabled, Generator gen) {
        // Take default terminal if it has not been defined in CGMES file. If it null, ignore
        Terminal terminal = parent.findRegulatingTerminal(control.cgmesTerminal, true);
        if (terminal == null) {
            context.ignored(controlId, String.format("Regulation terminal %s is not mapped or mapped to a switch", control.cgmesTerminal));
            return false;
        }

        double targetQ;
        if (Double.isNaN(control.targetValue)) {
            targetQ = terminal.getQ();
            context.fixed(controlId, "Invalid value for regulating target value. Real flows are considered targets.");
        } else {
            targetQ = control.targetValue;
        }

        RegulationMode regulationMode = RegulationMode.OFF;
        // Regulating control is enabled AND this equipment participates in regulating control
        if (control.enabled && eqControlEnabled) {
            regulationMode = RegulationMode.VOLTAGE;
        }

        gen.setRegulatingTerminal(terminal)
                .setTargetQ(targetQ)
                .setRegulationMode(regulationMode);

        // add qPercent as an extension
        if (!Double.isNaN(qPercent)) {
            gen.newExtension(CoordinatedReactiveControlAdder.class)
                    .withQPercent(qPercent)
                    .add();
        }
        gen.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", controlId);

        return true;
    }

    private static class CgmesRegulatingControlForGenerator {
        String regulatingControlId;
        double qPercent;
        boolean controlEnabled;
    }

    private final RegulatingControlMapping parent;
    private final Map<String, CgmesRegulatingControlForGenerator> mapping;
    private final Context context;
}
