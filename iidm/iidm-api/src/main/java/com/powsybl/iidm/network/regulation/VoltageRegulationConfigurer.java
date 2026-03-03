/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.Terminal;

import java.util.Objects;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface VoltageRegulationConfigurer extends VoltageRegulationAdderOrBuilder<VoltageRegulationConfigurer> {

    static VoltageRegulationConfigurer create(VoltageRegulationHolder voltageRegulationHolder, boolean regulationActiveOnCreation) {
        Objects.requireNonNull(voltageRegulationHolder);
        if (voltageRegulationHolder.getVoltageRegulation().isPresent()) {
            return new Updater(voltageRegulationHolder.getVoltageRegulation().get());
        }
        return new Builder(voltageRegulationHolder.newVoltageRegulation().withRegulating(regulationActiveOnCreation));
    }

    VoltageRegulation end();

    final class Updater implements VoltageRegulationConfigurer {
        private final VoltageRegulation voltageRegulation;

        private Updater(VoltageRegulation voltageRegulation) {
            this.voltageRegulation = voltageRegulation;
        }

        @Override
        public VoltageRegulation end() {
            return voltageRegulation;
        }

        @Override
        public VoltageRegulationConfigurer withTargetValue(double targetValue) {
            voltageRegulation.setTargetValue(targetValue);
            return this;
        }

        @Override
        public VoltageRegulationConfigurer withTargetDeadband(double targetDeadband) {
            voltageRegulation.setTargetDeadband(targetDeadband);
            return this;
        }

        @Override
        public VoltageRegulationConfigurer withSlope(double slope) {
            voltageRegulation.setSlope(slope);
            return this;
        }

        @Override
        public VoltageRegulationConfigurer withTerminal(Terminal terminal) {
            voltageRegulation.setTerminal(terminal);
            return this;
        }

        @Override
        public VoltageRegulationConfigurer withMode(RegulationMode mode) {
            voltageRegulation.setMode(mode);
            return this;
        }

        @Override
        public VoltageRegulationConfigurer withRegulating(boolean regulating) {
            voltageRegulation.setRegulating(regulating);
            return this;
        }
    }

    final class Builder implements VoltageRegulationConfigurer {
        private final VoltageRegulationBuilder voltageRegulationBuilder;

        private Builder(VoltageRegulationBuilder voltageRegulationBuilder) {
            this.voltageRegulationBuilder = voltageRegulationBuilder;
        }

        @Override
        public VoltageRegulation end() {
            return voltageRegulationBuilder.build();
        }

        @Override
        public VoltageRegulationConfigurer withTargetValue(double targetValue) {
            voltageRegulationBuilder.withTargetValue(targetValue);
            return this;
        }

        @Override
        public VoltageRegulationConfigurer withTargetDeadband(double targetDeadband) {
            voltageRegulationBuilder.withTargetDeadband(targetDeadband);
            return this;
        }

        @Override
        public VoltageRegulationConfigurer withSlope(double slope) {
            voltageRegulationBuilder.withSlope(slope);
            return this;
        }

        @Override
        public VoltageRegulationConfigurer withTerminal(Terminal terminal) {
            voltageRegulationBuilder.withTerminal(terminal);
            return this;
        }

        @Override
        public VoltageRegulationConfigurer withMode(RegulationMode mode) {
            voltageRegulationBuilder.withMode(mode);
            return this;
        }

        @Override
        public VoltageRegulationConfigurer withRegulating(boolean regulating) {
            voltageRegulationBuilder.withRegulating(regulating);
            return this;
        }
    }
}
