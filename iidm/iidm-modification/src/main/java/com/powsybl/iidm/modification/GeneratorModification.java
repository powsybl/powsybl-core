/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public class GeneratorModification extends AbstractNetworkModification {

    private final String generatorId;
    private final Modifs modifs;

    public GeneratorModification(String generatorId, Modifs modifs) {
        this.generatorId = Objects.requireNonNull(generatorId);
        this.modifs = modifs;
    }

    @Override
    public void apply(Network network, boolean throwException,
                      ComputationManager computationManager, Reporter reporter) {
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

        if (modifs.getVoltageRegulatorOn() != null) {
            g.setVoltageRegulatorOn(modifs.getVoltageRegulatorOn());
        }

        boolean skipOtherConnectionChange = false;
        if (modifs.getConnected() != null) {
            changeConnectionState(g, modifs.getConnected());
            skipOtherConnectionChange = true;
        }

        if (modifs.getTargetP() != null) {
            setTargetPWithinBoundaries(g, modifs.getTargetP(), skipOtherConnectionChange);
        } else if (modifs.getDeltaTargetP() != null) {
            setTargetPWithinBoundaries(g, g.getTargetP() + modifs.getDeltaTargetP(), skipOtherConnectionChange);
        }

        if (modifs.getTargetV() != null) {
            g.setTargetV(modifs.getTargetV());
        }
        if (modifs.getTargetQ() != null) {
            g.setTargetQ(modifs.getTargetQ());
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
    }
}
