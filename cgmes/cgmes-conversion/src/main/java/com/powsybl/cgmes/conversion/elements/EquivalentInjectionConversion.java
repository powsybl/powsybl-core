/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.SV;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class EquivalentInjectionConversion extends AbstractReactiveLimitsOwnerConversion {

    private static final String REGULATION_TARGET = "regulationTarget";

    public EquivalentInjectionConversion(PropertyBag ei, Context context) {
        super(CgmesNames.EQUIVALENT_INJECTION, ei, context);
    }

    @Override
    public void convertInsideBoundary() {
        if (context.config().convertBoundary()) {
            if (valid()) {
                convert();
            }
        } else {
            // If we find an Equivalent Injection at a boundary
            // we will decide later what to do with it
            //
            // If finally a dangling line is created at the boundary node
            // and the equivalent injection is regulating voltage
            // we will have to transfer regulating voltage data
            // from the equivalent injection to the dangling line
            context.boundary().addEquivalentInjectionAtNode(this.p, nodeId());
        }
    }

    @Override
    public void convert() {
        // An equivalent injection found inside a modeling authority data
        // will be mapped to a Generator
        convertToGenerator();
    }

    // A dangling line has been created at the boundary node of the equivalent injection
    public DanglingLine convertOverDanglingLine(DanglingLineAdder adder) {
        boolean regulationCapability = p.asBoolean(CgmesNames.REGULATION_CAPABILITY, false);

        DanglingLine dl;
        if (regulationCapability) {
            // If this equivalent injection is regulating voltage,
            // map it over the dangling line 'virtual generator'
            dl = adder
                    .setP0(0.0)
                    .setQ0(0.0)
                    .newGeneration()
                    .setTargetP(0.0)
                    .setTargetQ(0.0)
                    .setVoltageRegulationOn(false)
                    .setMinP(-Double.MAX_VALUE)
                    .setMaxP(Double.MAX_VALUE)
                    .add()
                    .add();
        } else {
            // Map all the observed flows to the 'virtual load'
            // of the dangling line
            dl = adder
                    .setP0(0.0)
                    .setQ0(0.0)
                    .add();
        }
        // We do not call addAliasesAndProperties(dl) !
        // Because we do not want to add this equivalent injection
        // terminal id as a generic "Terminal" alias of the dangling line,
        // Terminal1 and Terminal2 aliases should be used for
        // the original ACLineSegment or Switch terminals
        // We want to keep track add this equivalent injection terminal
        // under a separate, specific, alias type
        dl.addAlias(this.id, CgmesNames.EQUIVALENT_INJECTION);
        dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.EQUIVALENT_INJECTION, this.id);
        CgmesTerminal cgmesTerminal = context.cgmes().terminal(terminalId());
        if (cgmesTerminal != null) {
            dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal", cgmesTerminal.id());
        }
        return dl;
    }

    private void convertToGenerator() {
        double minP = p.asDouble("minP", -Double.MAX_VALUE);
        double maxP = p.asDouble("maxP", Double.MAX_VALUE);
        EnergySource energySource = EnergySource.OTHER;

        GeneratorAdder adder = voltageLevel().newGenerator();
        setMinPMaxP(adder, minP, maxP);
        adder.setVoltageRegulatorOn(false)
                .setEnergySource(energySource);
        identify(adder);
        connect(adder);
        Generator g = adder.add();
        addAliasesAndProperties(g);
        convertedTerminals(g.getTerminal());
        convertReactiveLimits(g);
        g.addAlias(this.id, CgmesNames.EQUIVALENT_INJECTION);

        addSpecificProperties(g, p);
    }

    private static void addSpecificProperties(Generator generator, PropertyBag p) {
        generator.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.EQUIVALENT_INJECTION);
        generator.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS + CgmesNames.REGULATION_CAPABILITY,
                String.valueOf(p.asBoolean(CgmesNames.REGULATION_CAPABILITY, false)));
    }

    @Override
    public void update(Network network) {
        Connectable<?> connectable = network.getConnectable(id);
        if (connectable == null) {
            return;
        }
        if (connectable.getType().equals(IdentifiableType.GENERATOR)) {
            updateTerminalData(connectable);
            updateGenerator((Generator) connectable);
        } else if (connectable.getType().equals(IdentifiableType.DANGLING_LINE)) {
            updateTerminalData(connectable);
            updateDanglingLine((DanglingLine) connectable);
        } else {
            throw new ConversionException("Unexpected connectable type: " + connectable.getType().name());
        }
    }

    private void updateGenerator(Generator generator) {
        boolean regulationCapability = Boolean.parseBoolean(generator.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS + CgmesNames.REGULATION_CAPABILITY));
        Regulation regulation = getRegulation(regulationCapability);
        generator.setTargetP(regulation.targetP)
                .setTargetQ(regulation.targetQ)
                .setTargetV(regulation.targetV)
                .setVoltageRegulatorOn(regulation.status);
    }

    private void updateDanglingLine(DanglingLine danglingLine) {
        DanglingLine.Generation dlGeneration = danglingLine.getGeneration();
        if (dlGeneration != null) {
            Regulation regulation = getRegulation(true);
            dlGeneration.setVoltageRegulationOn(regulation.status)
                    .setTargetP(regulation.targetP)
                    .setTargetQ(regulation.targetQ)
                    .setTargetV(regulation.targetV);
        } else {
            danglingLine.setP0(p0()).setQ0(q0());
        }

        /*** TODO JAM Ver cuando se podria realizar
         (Al final del update de los terminales si antes se ha realizado el update de la P0 y Q0
         Plantearlo como una coherencia entre los valores dando prioridad a la P0 y Q0)

        // If we do not have power flow at model side and we can compute it,
        // do it and assign the result at the terminal of the dangling line
        if (context.config().computeFlowsAtBoundaryDanglingLines()
                && terminalConnected(modelSide)
                && !powerFlowSV(modelSide).defined()
                && context.boundary().hasVoltage(boundaryNode)) {

            if (isZ0(dl)) {
                // Flow out must be equal to the consumption seen at boundary
                Optional<DanglingLine.Generation> generation = Optional.ofNullable(dl.getGeneration());
                dl.getTerminal().setP(dl.getP0() - generation.map(DanglingLine.Generation::getTargetP).orElse(0.0));
                dl.getTerminal().setQ(dl.getQ0() - generation.map(DanglingLine.Generation::getTargetQ).orElse(0.0));

            } else {
                setDanglingLineModelSideFlow(dl, boundaryNode);
            }
        }
        ******/
    }

    /*** Viene de AbstractConductingEquipmentConversion
    PowerFlow powerFlowSV() {
        if (stateVariablesPowerFlow().defined()) {
            return stateVariablesPowerFlow();
        }
        return PowerFlow.UNDEFINED;
    }

     PowerFlow powerFlowSV(int n) {
     if (stateVariablesPowerFlow(n).defined()) {
     return stateVariablesPowerFlow(n);
     }
     return PowerFlow.UNDEFINED;
     }
     **/

    private boolean isZ0(DanglingLine dl) {
        return dl.getR() == 0.0 && dl.getX() == 0.0 && dl.getG() == 0.0 && dl.getB() == 0.0;
    }

    private void setDanglingLineModelSideFlow(DanglingLine dl, String boundaryNode) {

        double v = context.boundary().vAtBoundary(boundaryNode);
        double angle = context.boundary().angleAtBoundary(boundaryNode);
        // The net sum of power flow "entering" at boundary is "exiting"
        // through the line, we have to change the sign of the sum of flows
        // at the node when we consider flow at line end
        Optional<DanglingLine.Generation> generation = Optional.ofNullable(dl.getGeneration());
        double p = dl.getP0() - generation.map(DanglingLine.Generation::getTargetP).orElse(0.0);
        double q = dl.getQ0() - generation.map(DanglingLine.Generation::getTargetQ).orElse(0.0);
        SV svboundary = new SV(-p, -q, v, angle, TwoSides.ONE); // JAM TODO is TWO
        // The other side power flow must be computed taking into account
        // the same criteria used for ACLineSegment: total shunt admittance
        // is divided in 2 equal shunt admittance at each side of series impedance
        double g = dl.getG() / 2;
        double b = dl.getB() / 2;
        SV svmodel = svboundary.otherSide(dl.getR(), dl.getX(), g, b, g, b, 1.0, 0.0);
        dl.getTerminal().setP(svmodel.getP());
        dl.getTerminal().setQ(svmodel.getQ());
    }

    static class Regulation {
        private boolean status;
        private double targetV;
        private double targetP;
        private double targetQ;
    }

    private Regulation getRegulation(boolean regulationCapability) {
        Regulation regulation = new Regulation();

        regulation.status = p.asBoolean("regulationStatus", false) && regulationCapability;
        if (!p.containsKey("regulationStatus") || !p.containsKey(REGULATION_TARGET)) {
            LOG.trace("Attributes regulationStatus or regulationTarget not present for equivalent injection {}. Voltage regulation is considered as off.", id);
        }

        regulation.targetV = Double.NaN;
        if (regulation.status) {
            regulation.targetV = p.asDouble(REGULATION_TARGET);
            if (regulation.targetV == 0) {
                fixed(REGULATION_TARGET, "Target voltage value can not be zero", regulation.targetV,
                        voltageLevel().getNominalV());
                regulation.targetV = voltageLevel().getNominalV();
            }
        }

        PowerFlow f = powerFlow();
        regulation.targetP = 0;
        regulation.targetQ = 0;
        if (f.defined()) {
            regulation.targetP = -f.p();
            regulation.targetQ = -f.q();
        }

        return regulation;
    }

    private static final Logger LOG = LoggerFactory.getLogger(EquivalentInjectionConversion.class);
}
