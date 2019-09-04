/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.TreeMap;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntCompensatorAdderImpl extends AbstractInjectionAdder<ShuntCompensatorAdderImpl> implements ShuntCompensatorAdder {

    private final VoltageLevelExt voltageLevel;

    private ShuntCompensatorModelHolder model;

    private int currentSectionCount = -1;

    ShuntCompensatorAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    class ShuntCompensatorLinearModelAdderImpl implements ShuntCompensatorLinearModelAdder {

        private double bPerSection;

        private int maximumSectionCount = -1;

        @Override
        public ShuntCompensatorLinearModelAdderImpl setbPerSection(double bPerSection) {
            ValidationUtil.checkbPerSection(ShuntCompensatorAdderImpl.this, bPerSection);
            this.bPerSection = bPerSection;
            return this;
        }

        @Override
        public ShuntCompensatorLinearModelAdderImpl setMaximumSectionCount(int maximumSectionCount) {
            ValidationUtil.checkMaximumSectionCount(ShuntCompensatorAdderImpl.this, maximumSectionCount);
            this.maximumSectionCount = maximumSectionCount;
            return this;
        }

        @Override
        public ShuntCompensatorAdder add() {
            ValidationUtil.checkbPerSection(ShuntCompensatorAdderImpl.this, bPerSection);
            ValidationUtil.checkMaximumSectionCount(ShuntCompensatorAdderImpl.this, maximumSectionCount);
            model = new ShuntCompensatorLinearModelImpl(bPerSection, maximumSectionCount);
            return ShuntCompensatorAdderImpl.this;
        }
    }

    class ShuntCompensatorNonLinearModelAdderImpl implements ShuntCompensatorNonLinearModelAdder {

        private final TreeMap<Integer, ShuntCompensatorNonLinearModelImpl.SectionImpl> sections = new TreeMap<>();

        class SectionAdderImpl implements SectionAdder {

            private int sectionNum;

            private double b;

            @Override
            public SectionAdder setSectionNumber(int sectionNum) {
                if (sectionNum < 0) {
                    throw new ValidationException(ShuntCompensatorAdderImpl.this,
                            "this number of section (" + sectionNum
                                    + ") should be greater than or equal to 0");
                }
                this.sectionNum = sectionNum;
                return this;
            }

            @Override
            public SectionAdder setB(double b) {
                ValidationUtil.checkbPerSection(ShuntCompensatorAdderImpl.this, b);
                this.b = b;
                return this;
            }

            @Override
            public ShuntCompensatorNonLinearModelAdder endSection() {
                sections.put(sectionNum, new ShuntCompensatorNonLinearModelImpl.SectionImpl(b));
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
                throw new ValidationException(ShuntCompensatorAdderImpl.this, "shunt compensator must have at least one section");
            }
            model = new ShuntCompensatorNonLinearModelImpl(sections);
            return ShuntCompensatorAdderImpl.this;
        }
    }

    @Override
    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "Shunt compensator";
    }

    @Override
    public ShuntCompensatorLinearModelAdder newShuntCompensatorLinearModel() {
        return new ShuntCompensatorLinearModelAdderImpl();
    }

    @Override
    public ShuntCompensatorNonLinearModelAdder newShuntCompensatorNonLinearModel() {
        return new ShuntCompensatorNonLinearModelAdderImpl();
    }

    @Override
    public ShuntCompensatorAdder setCurrentSectionCount(int currentSectionCount) {
        this.currentSectionCount = currentSectionCount;
        return this;
    }

    @Override
    public ShuntCompensatorImpl add() {
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();
        if (model == null) {
            throw new ValidationException(this, "the shunt compensator model is undefined");
        }
        model.checkCurrentSection(currentSectionCount);
        ShuntCompensatorImpl shunt
                = new ShuntCompensatorImpl(getNetwork().getRef(),
                id, getName(), model, currentSectionCount);
        model.setShuntCompensator(shunt);
        shunt.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        getNetwork().getIndex().checkAndAdd(shunt);
        getNetwork().getListeners().notifyCreation(shunt);
        return shunt;
    }

}
