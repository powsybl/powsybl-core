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
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */

public class RegulatingControlMappingForGenerators {

    private static final String QPERCENT = "qPercent";

    RegulatingControlMappingForGenerators(RegulatingControlMapping parent, Context context) {
        this.parent = parent;
        this.context = context;
        mapping = new HashMap<>();
    }

    public static void initialize(GeneratorAdder adder) {
        adder.setVoltageRegulatorOn(false);
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
            LOG.trace("Regulating control Id not present for generator {}", gen.getId());
            return;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            return;
        }

        boolean okSet = false;
        if (RegulatingControlMapping.isControlModeVoltage(control.mode)) {
            okSet = setRegulatingControlVoltage(controlId, control, rc.qPercent, gen);
        } else if (RegulatingControlMapping.isControlModeReactivePower(control.mode)) {
            okSet = setRegulatingControlReactivePower(controlId, control, rc.qPercent, gen);
        } else {
            context.ignored(control.mode, "Unsupported regulation mode for generator " + gen.getId());
        }
        control.setCorrectlySet(okSet);
    }

    private boolean setRegulatingControlVoltage(String controlId, RegulatingControl control, double qPercent, Generator gen) {

        // Take default terminal if it has not been defined in CGMES file (it is never null)
        Terminal regulatingTerminal = RegulatingTerminalMapper
                .mapForVoltageControl(control.cgmesTerminal, context)
                .orElse(gen.getTerminal());

        gen.setRegulatingTerminal(regulatingTerminal);

        // add qPercent as an extension
        if (!Double.isNaN(qPercent)) {
            gen.newExtension(CoordinatedReactiveControlAdder.class)
                    .withQPercent(qPercent)
                    .add();
        }
        gen.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", controlId);
        gen.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "mode", control.mode);

        return true;
    }

    private boolean setRegulatingControlReactivePower(String controlId, RegulatingControl control, double qPercent, Generator gen) {
        // Ignore control if the terminal is not mapped.
        TerminalAndSign mappedRegulatingTerminal = RegulatingTerminalMapper
                .mapForFlowControl(control.cgmesTerminal, context)
                .orElseGet(() -> new TerminalAndSign(null, 1));

        if (mappedRegulatingTerminal.getTerminal() == null) {
            context.ignored(controlId, String.format("Regulation terminal %s is not mapped or mapped to a switch", control.cgmesTerminal));
            return false;
        }

        gen.newExtension(RemoteReactivePowerControlAdder.class)
                .withRegulatingTerminal(mappedRegulatingTerminal.getTerminal())
                .withEnabled(false)
                .add();

        // add qPercent as an extension
        if (!Double.isNaN(qPercent)) {
            gen.newExtension(CoordinatedReactiveControlAdder.class)
                    .withQPercent(qPercent)
                    .add();
        }

        gen.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.REGULATING_CONTROL, controlId);
        gen.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.MODE, control.mode);
        gen.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_SIGN, String.valueOf(mappedRegulatingTerminal.getSign()));

        return true;
    }

    private static final class CgmesRegulatingControlForGenerator {
        String regulatingControlId;
        double qPercent;
    }

    private final RegulatingControlMapping parent;
    private final Map<String, CgmesRegulatingControlForGenerator> mapping;
    private final Context context;

    private static final Logger LOG = LoggerFactory.getLogger(RegulatingControlMappingForGenerators.class);
}
