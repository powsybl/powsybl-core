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
    private Double targetDeadband;
    private Double slope;
    private Terminal terminal;
    private RegulationMode mode;
    private boolean regulating;

    public VoltageRegulation() { }

    public VoltageRegulation(Double targetValue, Double targetDeadband, Double slope, Terminal terminal, RegulationMode mode, boolean regulating) {
        this.targetValue = targetValue;
        this.targetDeadband = targetDeadband;
        this.slope = slope;
        this.terminal = terminal;
        this.mode = mode;
        this.regulating = regulating;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(double targetValue) {
        this.targetValue = targetValue;
    }

    public double getTargetDeadband() {
        return targetDeadband;
    }

    public void setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
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

    public void setMode(RegulationMode mode) {
        this.mode = mode;
    }

    public boolean isRegulating() {
        return regulating;
    }

    public void setRegulating(boolean regulating) {
        this.regulating = regulating;
    }

    public static class Builder {
        private Double targetValue = Double.NaN;
        private Double targetDeadband = Double.NaN;
        private Double slope = Double.NaN;
        private Terminal terminal = null;
        private RegulationMode mode = null;
        private boolean regulating = false;

        public Builder setTargetValue(Double targetValue) {
            this.targetValue = targetValue;
            return this;
        }

        public Builder setTargetDeadband(Double targetDeadband) {
            this.targetDeadband = targetDeadband;
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

        public Builder setRegulating(boolean regulating) {
            this.regulating = regulating;
            return this;
        }

        public VoltageRegulation build() {
            if (mode == null) {
//                throw new IllegalArgumentException("Invalid VoltageRegulation: mode must be defined.");
            }
            if (terminal == null) {
//                throw new IllegalArgumentException("Invalid VoltageRegulation: terminal must be defined.");
            }
            return new VoltageRegulation(targetValue, targetDeadband, slope, terminal, mode, regulating);
        }

    }

    public static Builder builder() {
        return new Builder();
    }
}
