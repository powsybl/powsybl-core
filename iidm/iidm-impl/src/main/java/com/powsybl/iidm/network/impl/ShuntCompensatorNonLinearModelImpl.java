/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ShuntCompensatorModelType;
import com.powsybl.iidm.network.ShuntCompensatorNonLinearModel;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class ShuntCompensatorNonLinearModelImpl implements ShuntCompensatorModelExt, ShuntCompensatorNonLinearModel {

    static class SectionImpl implements Section {

        private ShuntCompensatorImpl shuntCompensator;

        private final int index;

        private double b;

        private double g;

        SectionImpl(int index, double b, double g) {
            this.index = index;
            this.b = b;
            this.g = g;
        }

        private SectionImpl attach(ShuntCompensatorImpl shuntCompensator) {
            if (this.shuntCompensator != null) {
                throw new IllegalStateException("Section already attached to " + this.shuntCompensator.getId());
            }
            this.shuntCompensator = Objects.requireNonNull(shuntCompensator);
            return this;
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
            shuntCompensator.notifyUpdate(() -> getAttributeName(index, "b"), oldValue, this.b);
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
            shuntCompensator.notifyUpdate(() -> getAttributeName(index, "g"), oldValue, this.g);
            return this;
        }

        private String getAttributeName(int sectionNum, String attribute) {
            return "section" + sectionNum + "." + attribute;
        }
    }

    private ShuntCompensatorImpl shuntCompensator;

    private final List<SectionImpl> sections;

    ShuntCompensatorNonLinearModelImpl(List<SectionImpl> sections) {
        this.sections = Objects.requireNonNull(sections);
    }

    @Override
    public ShuntCompensatorNonLinearModelImpl attach(ShuntCompensatorImpl shuntCompensator) {
        if (this.shuntCompensator != null) {
            throw new IllegalStateException("ShuntCompensatorNonLinearModelImpl already attached to " + this.shuntCompensator.getId());
        }

        this.shuntCompensator = Objects.requireNonNull(shuntCompensator);
        for (SectionImpl section : sections) {
            section.attach(shuntCompensator);
        }

        return this;
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
}
