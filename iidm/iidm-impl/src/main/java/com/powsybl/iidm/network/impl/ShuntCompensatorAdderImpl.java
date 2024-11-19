/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ShuntCompensatorAdderImpl extends AbstractInjectionAdder<ShuntCompensatorAdderImpl> implements ShuntCompensatorAdder {

    private ShuntCompensatorModelBuilder modelBuilder;

    private Integer sectionCount;

    private double targetV = Double.NaN;

    private double targetDeadband = Double.NaN;

    private TerminalExt regulatingTerminal;

    private boolean voltageRegulatorOn = false;

    ShuntCompensatorAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    protected String getTypeDescription() {
        return "Shunt compensator";
    }

    interface ShuntCompensatorModelBuilder {

        int getMaximumSectionCount();

        ShuntCompensatorModelExt build();

    }

    class ShuntCompensatorLinearModelAdderImpl implements ShuntCompensatorLinearModelAdder, ShuntCompensatorModelBuilder {

        private double bPerSection = Double.NaN;

        private double gPerSection = Double.NaN;

        private int maximumSectionCount = -1;

        @Override
        public ShuntCompensatorLinearModelAdder setBPerSection(double bPerSection) {
            this.bPerSection = bPerSection;
            return this;
        }

        @Override
        public ShuntCompensatorLinearModelAdder setGPerSection(double gPerSection) {
            this.gPerSection = gPerSection;
            return this;
        }

        @Override
        public int getMaximumSectionCount() {
            return maximumSectionCount;
        }

        @Override
        public ShuntCompensatorLinearModelAdder setMaximumSectionCount(int maximumSectionCount) {
            this.maximumSectionCount = maximumSectionCount;
            return this;
        }

        @Override
        public ShuntCompensatorAdder add() {
            ValidationUtil.checkBPerSection(ShuntCompensatorAdderImpl.this, bPerSection);
            ValidationUtil.checkMaximumSectionCount(ShuntCompensatorAdderImpl.this, maximumSectionCount);

            modelBuilder = this;
            return ShuntCompensatorAdderImpl.this;
        }

        @Override
        public ShuntCompensatorModelExt build() {
            return new ShuntCompensatorLinearModelImpl(bPerSection, gPerSection, maximumSectionCount);
        }
    }

    class ShuntCompensatorNonLinearModelAdderImpl implements ShuntCompensatorNonLinearModelAdder, ShuntCompensatorModelBuilder {

        private final List<SectionAdderImpl> sectionAdders = new ArrayList<>();

        class SectionAdderImpl implements SectionAdder {

            private double b = Double.NaN;

            private double g = Double.NaN;

            @Override
            public SectionAdder setB(double b) {
                this.b = b;
                return this;
            }

            @Override
            public SectionAdder setG(double g) {
                this.g = g;
                return this;
            }

            @Override
            public ShuntCompensatorNonLinearModelAdder endSection() {
                ValidationUtil.checkBPerSection(ShuntCompensatorAdderImpl.this, b);
                if (Double.isNaN(g)) {
                    if (sectionAdders.isEmpty()) {
                        g = 0;
                    } else {
                        g = sectionAdders.get(sectionAdders.size() - 1).g;
                    }
                }
                sectionAdders.add(this);
                return ShuntCompensatorNonLinearModelAdderImpl.this;
            }
        }

        @Override
        public SectionAdder beginSection() {
            return new SectionAdderImpl();
        }

        @Override
        public ShuntCompensatorAdder add() {
            if (sectionAdders.isEmpty()) {
                throw new ValidationException(ShuntCompensatorAdderImpl.this, "a shunt compensator must have at least one section");
            }
            modelBuilder = this;
            return ShuntCompensatorAdderImpl.this;
        }

        @Override
        public ShuntCompensatorModelExt build() {
            List<ShuntCompensatorNonLinearModelImpl.SectionImpl> sections = IntStream.range(0, sectionAdders.size()).mapToObj(s -> {
                SectionAdderImpl adder = sectionAdders.get(s);
                return new ShuntCompensatorNonLinearModelImpl.SectionImpl(s + 1, adder.b, adder.g);
            }).collect(Collectors.toList());

            return new ShuntCompensatorNonLinearModelImpl(sections);
        }

        @Override
        public int getMaximumSectionCount() {
            return sectionAdders.size();
        }
    }

    @Override
    public ShuntCompensatorLinearModelAdder newLinearModel() {
        return new ShuntCompensatorLinearModelAdderImpl();
    }

    @Override
    public ShuntCompensatorNonLinearModelAdder newNonLinearModel() {
        return new ShuntCompensatorNonLinearModelAdderImpl();
    }

    @Override
    public ShuntCompensatorAdder setSectionCount(int sectionCount) {
        this.sectionCount = sectionCount;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = (TerminalExt) regulatingTerminal;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return this;
    }

    @Override
    public ShuntCompensatorImpl add() {
        NetworkImpl network = getNetwork();
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();

        if (modelBuilder == null) {
            throw new ValidationException(this, "the shunt compensator model has not been defined");
        }

        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, network);
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV,
                network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkTargetDeadband(this, "shunt compensator", voltageRegulatorOn, targetDeadband,
                network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkSections(this, sectionCount, modelBuilder.getMaximumSectionCount(),
                network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));

        ShuntCompensatorImpl shunt = new ShuntCompensatorImpl(getNetworkRef(),
                id, getName(), isFictitious(), modelBuilder.build(), sectionCount,
                regulatingTerminal == null ? terminal : regulatingTerminal,
                voltageRegulatorOn, targetV, targetDeadband);

        shunt.addTerminal(terminal);
        voltageLevel.getTopologyModel().attach(terminal, false);
        network.getIndex().checkAndAdd(shunt);
        network.getListeners().notifyCreation(shunt);
        return shunt;
    }

}
