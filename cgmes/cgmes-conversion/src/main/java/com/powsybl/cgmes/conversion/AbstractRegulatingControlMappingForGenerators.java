/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractRegulatingControlMappingForGenerators implements RegulatingControlMappingForGenerators {

    private static final String QPERCENT = "qPercent";

    protected final RegulatingControlMapping parent;
    protected final Map<String, CgmesRegulatingControlForGenerator> mapping;
    protected final Context context;

    private static class CgmesRegulatingControlForGenerator {
        String regulatingControlId;
        double qPercent;
        boolean controlEnabled;
    }

    AbstractRegulatingControlMappingForGenerators(RegulatingControlMapping parent, Context context) {
        this.parent = parent;
        this.context = context;
        mapping = new HashMap<>();
    }

    @Override
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

    @Override
    public void applyRegulatingControls(Network network) {
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

        RegulatingControlMapping.RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
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

    protected abstract boolean setRegulatingControlVoltage(String controlId,
                                                           RegulatingControlMapping.RegulatingControl control, double qPercent, boolean eqControlEnabled, Generator gen);

    protected static void setRegulatingControlVoltage(String controlId, double targetV, boolean voltageRegulatorOn, Terminal terminal, double qPercent, Generator gen) {
        gen.setRegulatingTerminal(terminal)
                .setTargetV(targetV)
                .setVoltageRegulatorOn(voltageRegulatorOn);

        // add qPercent as an extension
        if (!Double.isNaN(qPercent)) {
            gen.newExtension(CoordinatedReactiveControlAdder.class)
                    .withQPercent(qPercent)
                    .add();
        }
        gen.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", controlId);
    }

    protected abstract boolean setRegulatingControlReactivePower(String controlId, RegulatingControlMapping.RegulatingControl control, double qPercent, boolean eqControlEnabled, Generator gen);

    protected static void setRegulatingControlReactivePower(String controlId, double targetQ, boolean enabled, Terminal terminal, double qPercent, Generator gen) {
        gen.newExtension(RemoteReactivePowerControlAdder.class)
                .withTargetQ(targetQ)
                .withRegulatingTerminal(terminal)
                .withEnabled(enabled)
                .add();

        // add qPercent as an extension
        if (!Double.isNaN(qPercent)) {
            gen.newExtension(CoordinatedReactiveControlAdder.class)
                    .withQPercent(qPercent)
                    .add();
        }

        gen.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", controlId);
    }
}

