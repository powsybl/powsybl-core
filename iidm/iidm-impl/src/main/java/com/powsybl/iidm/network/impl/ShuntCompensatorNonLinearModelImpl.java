/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class ShuntCompensatorNonLinearModelImpl extends AbstractShuntCompensatorModel implements ShuntCompensatorNonLinearModel {

    static class SectionImpl implements Section {

        private ShuntCompensatorImpl shuntCompensator = null;

        private final int index;

        private double b;

        private double g;

        SectionImpl(int index, double b, double g) {
            this.index = index;
            this.b = b;
            this.g = g;
        }

        @Override
        public double getB() {
            return b;
        }

        @Override
        public Section setB(double b) {
            ValidationUtil.checkB(shuntCompensator, b);
            double oldValue = this.b;
            this.b = b;
            shuntCompensator.notifyUpdate(notifyUpdateSection(index, "b"), oldValue, this.b);
            return this;
        }

        @Override
        public double getG() {
            return g;
        }

        @Override
        public Section setG(double g) {
            ValidationUtil.checkG(shuntCompensator, g);
            double oldValue = this.g;
            this.g = g;
            shuntCompensator.notifyUpdate(notifyUpdateSection(index, "g"), oldValue, this.g);
            return this;
        }

        private static String notifyUpdateSection(int sectionNum, String attribute) {
            return "section" + sectionNum + "." + attribute;
        }

        void setShuntCompensator(ShuntCompensatorImpl shuntCompensator) {
            if (this.shuntCompensator != null) {
                throw new PowsyblException("Shunt compensator " + shuntCompensator.getId() + " has been set twice for the section " + index);
            }
            this.shuntCompensator = shuntCompensator;
        }
    }

    private final List<SectionImpl> sections;

    ShuntCompensatorNonLinearModelImpl(List<SectionImpl> sections) {
        this.sections = sections;
    }

    @Override
    public List<Section> getAllSections() {
        return Collections.unmodifiableList(sections);
    }

    @Override
    public ShuntCompensatorModelType getType() {
        return ShuntCompensatorModelType.NON_LINEAR;
    }

    @Override
    public int getMaximumSectionCount() {
        return sections.size();
    }

    @Override
    public double getB(int sectionCount) {
        if (sectionCount < 0 || sectionCount > sections.size()) {
            throw new ValidationException(shuntCompensator, "invalid section count (must be in [0;maximumSectionCount]");
        }
        return sectionCount == 0 ? 0 : sections.get(sectionCount - 1).getB();
    }

    @Override
    public double getG(int sectionCount) {
        if (sectionCount < 0 || sectionCount > sections.size()) {
            throw new ValidationException(shuntCompensator, "invalid section count (must be in [0;maximumSectionCount]");
        }
        return sectionCount == 0 ? 0 : sections.get(sectionCount - 1).getG();
    }

    @Override
    public void setShuntCompensator(ShuntCompensatorImpl shuntCompensator) {
        super.setShuntCompensator(shuntCompensator);
        sections.forEach(section -> section.setShuntCompensator(shuntCompensator));
    }
}
