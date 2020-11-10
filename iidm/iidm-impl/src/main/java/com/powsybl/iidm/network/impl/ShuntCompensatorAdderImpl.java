/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntCompensatorAdderImpl extends AbstractInjectionAdder<ShuntCompensatorAdderImpl> implements ShuntCompensatorAdder {

    private final VoltageLevelExt voltageLevel;

    private ShuntCompensatorNonLinearModelAdderImpl shuntCompensatorNonLinearModelAdder;

    private ShuntCompensatorLinearModelAdderImpl shuntCompensatorLinearModelAdder;

    private int sectionCount = -1;

    private double targetV = Double.NaN;

    private double targetDeadband = Double.NaN;

    private TerminalExt regulatingTerminal;

    private boolean voltageRegulatorOn = false;

    ShuntCompensatorAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "Shunt compensator";
    }

    class ShuntCompensatorLinearModelAdderImpl implements ShuntCompensatorLinearModelAdder {

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
        public ShuntCompensatorLinearModelAdder setMaximumSectionCount(int maximumSectionCount) {
            this.maximumSectionCount = maximumSectionCount;
            return this;
        }

        @Override
        public ShuntCompensatorAdder add() {
            ValidationUtil.checkLinearBPerSection(ShuntCompensatorAdderImpl.this, bPerSection);
            ValidationUtil.checkMaximumSectionCount(ShuntCompensatorAdderImpl.this, maximumSectionCount);
            shuntCompensatorLinearModelAdder = this;
            // Only one model is available at once
            shuntCompensatorNonLinearModelAdder = null;
            return ShuntCompensatorAdderImpl.this;
        }
    }

    class ShuntCompensatorNonLinearModelAdderImpl implements ShuntCompensatorNonLinearModelAdder {

        private final List<ShuntCompensatorNonLinearModelImpl.SectionImpl> sections = new ArrayList<>();
        private int index = 1;

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
                if (Double.isNaN(g))  {
                    if (sections.isEmpty()) {
                        g = 0;
                    } else {
                        g = sections.get(sections.size() - 1).getG();
                    }
                }
                sections.add(new ShuntCompensatorNonLinearModelImpl.SectionImpl(index, b, g));
                index++;
                return ShuntCompensatorNonLinearModelAdderImpl.this;
            }
        }

        @Override
        public SectionAdder beginSection() {
            return new SectionAdderImpl();
        }

        @Override
        public ShuntCompensatorAdder add() {
            if (sections.isEmpty()) {
                throw new ValidationException(ShuntCompensatorAdderImpl.this, "a shunt compensator must have at least one section");
            }
            shuntCompensatorNonLinearModelAdder = this;
            // Only one model is available at once
            shuntCompensatorLinearModelAdder = null;
            return ShuntCompensatorAdderImpl.this;
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
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();
        AbstractShuntCompensatorModel model = null;
        if (shuntCompensatorLinearModelAdder != null) {
            model = new ShuntCompensatorLinearModelImpl(shuntCompensatorLinearModelAdder.bPerSection,
                                                        shuntCompensatorLinearModelAdder.gPerSection,
                                                        shuntCompensatorLinearModelAdder.maximumSectionCount);
        } else if (shuntCompensatorNonLinearModelAdder != null) {
            model = new ShuntCompensatorNonLinearModelImpl(shuntCompensatorNonLinearModelAdder.sections);
        }

        if (model == null) {
            throw new ValidationException(this, "the shunt compensator model has not been defined");
        }
        ValidationUtil.checkSections(this, sectionCount, model.getMaximumSectionCount());
        if (sectionCount < 0 || sectionCount > model.getMaximumSectionCount()) {
            throw new ValidationException(this, "unexpected section number (" + sectionCount + "): no existing associated section");
        }
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV);
        ValidationUtil.checkTargetDeadband(this, "shunt compensator", voltageRegulatorOn, targetDeadband);
        ShuntCompensatorImpl shunt
                = new ShuntCompensatorImpl(getNetwork().getRef(),
                id, getName(), isFictitious(), model,
                sectionCount, regulatingTerminal == null ? terminal : regulatingTerminal,
                voltageRegulatorOn, targetV, targetDeadband);
        shunt.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        getNetwork().getIndex().checkAndAdd(shunt);
        getNetwork().getListeners().notifyCreation(shunt);
        return shunt;
    }

}
