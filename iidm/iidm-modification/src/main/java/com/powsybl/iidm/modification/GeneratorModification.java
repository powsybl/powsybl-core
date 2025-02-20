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
import com.powsybl.iidm.modification.util.VoltageRegulationUtils;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;

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
        if (modifs.getVoltageRegulatorOn() != null) {
            if (Double.isNaN(g.getTargetV()) && modifs.getVoltageRegulatorOn().booleanValue()) {
                g.setTargetV(getPlausibleTargetV(g));
            }
            g.setVoltageRegulatorOn(modifs.getVoltageRegulatorOn());
        }
        if (modifs.getTargetP() != null || modifs.getDeltaTargetP() != null) {
            applyTargetP(g, skipOtherConnectionChange);
        }
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
        private Boolean voltageRegulatorOn;
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

        public Boolean getVoltageRegulatorOn() {
            return voltageRegulatorOn;
        }

        public void setVoltageRegulatorOn(Boolean voltageRegulatorOn) {
            this.voltageRegulatorOn = voltageRegulatorOn;
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
            && areValuesEqual(modifs.getTargetV(), g.getTargetV(), false)
            && areValuesEqual(modifs.getTargetQ(), g.getTargetQ(), false)
            && (modifs.getConnected() == null || modifs.getConnected() == g.getTerminal().isConnected())
            && (modifs.getVoltageRegulatorOn() == null
            || (!Double.isNaN(g.getTargetV()) || !modifs.getVoltageRegulatorOn() || areValuesEqual(getPlausibleTargetV(g), g.getTargetV(), false))
            && modifs.getVoltageRegulatorOn() == g.isVoltageRegulatorOn())
            && areValuesEqual(modifs.getTargetP(), g.getTargetP(), false)
            && areValuesEqual(modifs.getDeltaTargetP(), 0, false)) {
            impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
        }
        return impact;
    }
}
