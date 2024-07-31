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
    public void doApply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, boolean dryRun, ReportNode reportNode) {
        Generator g = network.getGenerator(generatorId);
        if (g == null) {
            logOrThrow(throwException, "Generator '" + generatorId + "' not found");
            return;
        }
        if (modifs.getMinP() != null) {
            g.setMinP(modifs.getMinP(), dryRun);
        }
        if (modifs.getMaxP() != null) {
            g.setMaxP(modifs.getMaxP(), dryRun);
        }
        if (modifs.getTargetV() != null) {
            g.setTargetV(modifs.getTargetV(), dryRun);
        }
        if (modifs.getTargetQ() != null) {
            g.setTargetQ(modifs.getTargetQ(), dryRun);
        }
        boolean skipOtherConnectionChange = false;
        if (modifs.getConnected() != null) {
            changeConnectionState(g, modifs.getConnected(), dryRun);
            skipOtherConnectionChange = true;
        }
        if (modifs.getVoltageRegulatorOn() != null) {
            if (Double.isNaN(g.getTargetV()) && modifs.getVoltageRegulatorOn().booleanValue()) {
                double plausibleTargetV = VoltageRegulationUtils.getTargetVForRegulatingElement(g.getNetwork(), g.getRegulatingTerminal().getBusView().getBus(),
                        g.getId(), IdentifiableType.GENERATOR).orElse(g.getRegulatingTerminal().getBusView().getBus().getV());
                g.setTargetV(plausibleTargetV, dryRun);
            }
            g.setVoltageRegulatorOn(modifs.getVoltageRegulatorOn(), dryRun);
        }
        if (modifs.getTargetP() != null || modifs.getDeltaTargetP() != null) {
            applyTargetP(g, skipOtherConnectionChange, dryRun);
        }
    }

    @Override
    public String getName() {
        return "GeneratorModification";
    }

    @Override
    public boolean hasImpactOnNetwork() {
        return false;
    }

    @Override
    public boolean isLocalDryRunPossible() {
        return true;
    }

    private void applyTargetP(Generator g, boolean skipOtherConnectionChange, boolean dryRun) {
        Double newTargetP;
        if (modifs.getTargetP() != null) {
            newTargetP = modifs.getTargetP();
        } else if (modifs.getDeltaTargetP() != null) {
            newTargetP = g.getTargetP() + modifs.getDeltaTargetP();
        } else {
            return;
        }
        if (modifs.isIgnoreCorrectiveOperations()) {
            g.setTargetP(newTargetP, dryRun);
        } else {
            setTargetPWithinBoundaries(g, newTargetP, skipOtherConnectionChange, dryRun);
        }
    }

    private void changeConnectionState(Generator g, boolean connect, boolean dryRun) {
        if (connect) {
            if (!g.getTerminal().isConnected()) {
                ConnectGenerator.connect(g, dryRun);
            }
        } else {
            if (g.getTerminal().isConnected()) {
                g.getTerminal().disconnect(dryRun);
            }
        }
    }

    private void setTargetPWithinBoundaries(Generator g, double targetP, boolean skipConnect, boolean dryRun) {
        if (!skipConnect) {
            changeConnectionState(g, true, dryRun);
        }
        g.setTargetP(Math.min(g.getMaxP(), Math.max(g.getMinP(), targetP)), dryRun);
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
}
