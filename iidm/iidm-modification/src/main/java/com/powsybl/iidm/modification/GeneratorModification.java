/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.util.VoltageRegulationUtils;

import java.util.Objects;

import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;
import static java.lang.Boolean.TRUE;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class GeneratorModification extends AbstractNetworkModification {

    private final String generatorId;
    private final Modifs modifs;

    public GeneratorModification(String generatorId, Modifs modifs) {
        this.generatorId = Objects.requireNonNull(generatorId);
        this.modifs = modifs;
    }

    @Override
    public String getName() {
        return "GeneratorModification";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        Generator g = network.getGenerator(generatorId);
        if (g == null) {
            logOrThrow(throwException, "Generator '" + generatorId + "' not found");
            return;
        }
        if (modifs.getMinP() != null) {
            g.setMinP(modifs.getMinP());
        }
        if (modifs.getMaxP() != null) {
            g.setMaxP(modifs.getMaxP());
        }
        if (modifs.getTargetV() != null) {
            g.setTargetV(modifs.getTargetV());
        }
        if (modifs.getTargetQ() != null) {
            g.setTargetQ(modifs.getTargetQ());
        }
        boolean skipOtherConnectionChange = false;
        if (modifs.getConnected() != null) {
            changeConnectionState(g, modifs.getConnected());
            skipOtherConnectionChange = true;
        }
        addVoltageRegulation(g);
        if (modifs.getTargetP() != null || modifs.getDeltaTargetP() != null) {
            applyTargetP(g, skipOtherConnectionChange);
        }
    }

    private void addVoltageRegulation(Generator g) {
        boolean regulating = modifs.getRegulating() != null ? modifs.getRegulating() : g.isRegulating();
        double localTargetV = computeVoltageTarget(modifs, g);
        double localTargetQ = computeReactiveTarget(modifs, g);
        if (regulating) {
            g.setLocalTargetV(localTargetV);
        } else {
            g.setLocalTargetQ(localTargetQ);
        }
        if (g.getVoltageRegulation() == null) {
            if (regulating) {
                g.newVoltageRegulation().withMode(RegulationMode.VOLTAGE).build();
            }
        } else {
            g.getVoltageRegulation().setRegulating(regulating);
        }

        RegulationMode voltageRegulationMode = modifs.getVoltageRegulationMode();
        if (voltageRegulationMode != null) {
            if (RegulationMode.VOLTAGE.equals(voltageRegulationMode)) {
                g.setLocalTargetV(localTargetV);
                g.newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .build();
            } else {
                throw new IllegalStateException(
                    "Unexpected value: " + voltageRegulationMode + " not yet implemented"
                );
            }
        }
    }

    private double computeVoltageTarget(Modifs modifs, Generator generator) {
        Double fromModifs = modifs.getTargetV();
        if (isNotNullAndNotNaN(fromModifs)) {
            return fromModifs;
        }

        double fromGenerator = generator.getLocalTargetV();
        if (!Double.isNaN(fromGenerator)) {
            return fromGenerator;
        }

        double plausible = getPlausibleTargetV(generator);
        generator.setTargetV(plausible);
        return plausible;
    }

    private double computeReactiveTarget(Modifs modifs, Generator generator) {
        Double fromModifs = modifs.getTargetQ();
        if (isNotNullAndNotNaN(fromModifs)) {
            return fromModifs;
        }
        return generator.getLocalTargetQ();
    }

    private static boolean isNotNullAndNotNaN(Double value) {
        return value != null && !value.isNaN();
    }

    private double getPlausibleTargetV(Generator g) {
        return VoltageRegulationUtils.getTargetVForRegulatingElement(g.getNetwork(), g.getRegulatingTerminal().getBusView().getBus(),
            g.getId(), IdentifiableType.GENERATOR).orElse(g.getRegulatingTerminal().getBusView().getBus().getV());
    }

    private void applyTargetP(Generator g, boolean skipOtherConnectionChange) {
        Double newTargetP;
        if (modifs.getTargetP() != null) {
            newTargetP = modifs.getTargetP();
        } else if (modifs.getDeltaTargetP() != null) {
            newTargetP = g.getTargetP() + modifs.getDeltaTargetP();
        } else {
            return;
        }
        if (modifs.isIgnoreCorrectiveOperations()) {
            g.setTargetP(newTargetP);
        } else {
            setTargetPWithinBoundaries(g, newTargetP, skipOtherConnectionChange);
        }
    }

    private void changeConnectionState(Generator g, boolean connect) {
        if (connect) {
            if (!g.getTerminal().isConnected()) {
                ConnectGenerator.connect(g);
            }
        } else {
            if (g.getTerminal().isConnected()) {
                g.getTerminal().disconnect();
            }
        }
    }

    private void setTargetPWithinBoundaries(Generator g, double targetP, boolean skipConnect) {
        if (!skipConnect) {
            changeConnectionState(g, true);
        }
        g.setTargetP(Math.min(g.getMaxP(), Math.max(g.getMinP(), targetP)));
    }

    public Modifs getModifs() {
        return modifs;
    }

    public String getGeneratorId() {
        return generatorId;
    }

    public static class Modifs {
        private Double minP;
        private Double maxP;
        private Double targetP;
        private Double deltaTargetP;
        private Double targetV;
        private Double targetQ;
        private RegulationMode voltageRegulationMode;
        private Boolean regulating;
        private Boolean connected;
        private boolean ignoreCorrectiveOperations;

        public Double getMinP() {
            return minP;
        }

        public void setMinP(Double minP) {
            this.minP = minP;
        }

        public Double getMaxP() {
            return maxP;
        }

        public void setMaxP(Double maxP) {
            this.maxP = maxP;
        }

        public Double getTargetP() {
            return targetP;
        }

        public void setTargetP(Double targetP) {
            this.targetP = targetP;
        }

        public Double getDeltaTargetP() {
            return deltaTargetP;
        }

        public void setDeltaTargetP(Double deltaTargetP) {
            this.deltaTargetP = deltaTargetP;
        }

        public Double getTargetV() {
            return targetV;
        }

        public void setTargetV(Double targetV) {
            this.targetV = targetV;
        }

        public Double getTargetQ() {
            return targetQ;
        }

        public void setTargetQ(Double targetQ) {
            this.targetQ = targetQ;
        }

        public RegulationMode getVoltageRegulationMode() {
            return voltageRegulationMode;
        }

        public void setVoltageRegulationMode(RegulationMode voltageRegulationMode) {
            this.voltageRegulationMode = voltageRegulationMode;
        }

        public Boolean getRegulating() {
            return regulating;
        }

        public void setRegulating(Boolean regulating) {
            this.regulating = regulating;
        }

        /**
         * @deprecated use {@link VoltageRegulation#isRegulating()} instead
         */
        @Deprecated(forRemoval = true, since = "7.3.0")
        public Boolean getVoltageRegulatorOn() {
            return this.voltageRegulationMode != null && this.voltageRegulationMode == RegulationMode.VOLTAGE;
        }

        /**
         * @deprecated use {@link VoltageRegulation#setRegulating(boolean)} instead
         */
        @Deprecated(forRemoval = true, since = "7.3.0")
        public void setVoltageRegulatorOn(Boolean voltageRegulatorOn) {
            if (TRUE.equals(voltageRegulatorOn)) {
                this.voltageRegulationMode = RegulationMode.VOLTAGE;
                this.regulating = true;
            } else if (Boolean.FALSE.equals(voltageRegulatorOn)) {
                this.voltageRegulationMode = RegulationMode.REACTIVE_POWER;
                this.regulating = true;
            }
        }

        public Boolean getConnected() {
            return connected;
        }

        public void setConnected(Boolean connected) {
            this.connected = connected;
        }

        /**
         * If false and a modification on target P is requested then, the generator is connected (if not before
         * and if it is not contrary to another connected modification), and the target P is set within the generator
         * boundaries (so even after a target P only modification, the generator can have a different connected state
         * and its target P can be its min or max P and not the target P of the modification).
         * If true the modification will simply apply the new target P.
         */
        public boolean isIgnoreCorrectiveOperations() {
            return ignoreCorrectiveOperations;
        }

        /**
         * If set to false and a modification on target P is requested then, the generator is connected (if not before
         * and if it is not contrary to another connected modification), and the target P is set within the generator
         * boundaries (so even after a target P only modification, the generator can have a different connected state
         * and its target P can be its min or max P and not the target P of the modification).
         * If set to true the modification will simply apply the new target P.
         */
        public void setIgnoreCorrectiveOperations(boolean ignoreCorrectiveOperations) {
            this.ignoreCorrectiveOperations = ignoreCorrectiveOperations;
        }
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Generator g = network.getGenerator(generatorId);
        if (g == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else if (areValuesEqual(modifs.getMinP(), g.getMinP(), false)
            && areValuesEqual(modifs.getMaxP(), g.getMaxP(), false)
            && areValuesEqual(modifs.getTargetV(), g.getLocalTargetV(), false)
            && areValuesEqual(modifs.getTargetQ(), g.getLocalTargetQ(), false)
            && (modifs.getConnected() == null || modifs.getConnected() == g.getTerminal().isConnected())
            && voltageRegulationHasNoImpactOnNetwork(g)
            && areValuesEqual(modifs.getTargetP(), g.getTargetP(), false)
            && areValuesEqual(modifs.getDeltaTargetP(), 0, false)) {
            impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
        }
        return impact;
    }

    private boolean voltageRegulationHasNoImpactOnNetwork(Generator g) {
        boolean hasImpact = false;
        if (g.getVoltageRegulation() == null) {
            // a generator without VoltageRegulation need a full VoltageRegulation to impact the network
            hasImpact |= modifs.getVoltageRegulationMode() != null
                && modifs.getRegulating() != null && modifs.getRegulating();
        } else {
            if (g.getVoltageRegulation().isRegulating() || modifs.getRegulating() != null) {
                // New regulating value different from actual value
                hasImpact |= modifs.getRegulating() != null
                    && !modifs.getRegulating().equals(g.getVoltageRegulation().isRegulating());
                // New RegulationMode value different from actual value
                hasImpact |= modifs.getVoltageRegulationMode() != null
                    && !g.isRegulatingWithMode(modifs.getVoltageRegulationMode());
            }
        }
        return !hasImpact;
    }
}
