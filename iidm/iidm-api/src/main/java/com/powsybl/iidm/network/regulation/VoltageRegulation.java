/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.Terminal;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulation {

    private Double targetValue;
    private Double localTargetValue;
    private Double targetDeadband;
    private Double localTargetDeadband;
    private Double slope;
    private Terminal terminal;
    private final RegulationMode mode;
    private final RegulationStatus status;

    public double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(double targetValue) {
        this.targetValue = targetValue;
    }

    public double getLocalTargetValue() {
        return localTargetValue;
    }

    public void setLocalTargetValue(double localTargetValue) {
        this.localTargetValue = localTargetValue;
    }

    public double getTargetDeadband() {
        return targetDeadband;
    }

    public void setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
    }

    public double getLocalTargetDeadband() {
        return localTargetDeadband;
    }

    public void setLocalTargetDeadband(double localTargetDeadband) {
        this.localTargetDeadband = localTargetDeadband;
    }

    public double getSlope() {
        return slope;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public RegulationMode getMode() {
        return mode;
    }

    public RegulationStatus getStatus() {
        return status;
    }

    public VoltageRegulation(Double targetValue, Double localTargetValue, Double targetDeadband, Double localTargetDeadband, Double slope, Terminal terminal, RegulationMode mode, RegulationStatus status) {
        this.targetValue = targetValue;
        this.localTargetValue = localTargetValue;
        this.targetDeadband = targetDeadband;
        this.localTargetDeadband = localTargetDeadband;
        this.slope = slope;
        this.terminal = terminal;
        this.mode = mode;
        this.status = status;
    }

    public static class Builder {
        private Double targetValue = null;
        private Double localTargetValue = null;
        private Double targetDeadband = null;
        private Double localTargetDeadband = null;
        private Double slope = null;
        private Terminal terminal = null;
        private RegulationMode mode = null;
        private RegulationStatus status = null;

        public Builder setTargetValue(Double targetValue) {
            this.targetValue = targetValue;
            return this;
        }

        public Builder setLocalTargetValue(Double localTargetValue) {
            this.localTargetValue = localTargetValue;
            return this;
        }

        public Builder setTargetDeadband(Double targetDeadband) {
            this.targetDeadband = targetDeadband;
            return this;
        }

        public Builder setLocalTargetDeadband(Double localTargetDeadband) {
            this.localTargetDeadband = localTargetDeadband;
            return this;
        }

        public Builder setSlope(Double slope) {
            this.slope = slope;
            return this;
        }

        public Builder setTerminal(Terminal terminal) {
            this.terminal = terminal;
            return this;
        }

        public Builder setMode(RegulationMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder setStatus(RegulationStatus status) {
            this.status = status;
            return this;
        }

        public VoltageRegulation build() {
            if (mode == null) {
                throw new IllegalArgumentException("Invalid VoltageRegulation: mode must be defined.");
            }
            if (status == null) {
                throw new IllegalArgumentException("Invalid VoltageRegulation: status must be defined.");
            }
            if (terminal == null) {
                throw new IllegalArgumentException("Invalid VoltageRegulation: terminal must be defined.");
            }
            return new VoltageRegulation(targetValue, localTargetValue, targetDeadband, localTargetDeadband, slope, terminal, mode, status);
        }

    }

    public static Builder builder() {
        return new Builder();
    }
}
