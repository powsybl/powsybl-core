/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class ShuntCompensatorNonLinearModelImpl extends AbstractShuntCompensatorModel implements ShuntCompensatorNonLinearModel {

    private static final String INDEX = "index";

    static class SectionImpl implements Section {

        private final double b;

        private final double g;

        SectionImpl(double b, double g) {
            this.b = b;
            this.g = g;
        }

        @Override
        public double getB() {
            return b;
        }

        @Override
        public double getG() {
            return g;
        }
    }

    private final TreeMap<Integer, SectionImpl> sections;

    ShuntCompensatorNonLinearModelImpl(TreeMap<Integer, SectionImpl> sections) {
        this.sections = sections;
    }

    @Override
    public Optional<Section> getSection(int sectionIndex) {
        ValidationUtil.checkSectionNumber(shuntCompensator, sectionIndex);
        return Optional.ofNullable(sections.get(sectionIndex));
    }

    @Override
    public Map<Integer, Section> getSections() {
        return Collections.unmodifiableMap(sections);
    }

    @Override
    public ShuntCompensatorNonLinearModel addOrReplaceSection(int sectionIndex, double b, double g) {
        if (sectionIndex == 0) {
            throw new ValidationException(shuntCompensator, "section 0 (b = 0.0, g = 0.0) should not be written, it corresponds to disconnected state");
        }
        ValidationUtil.checkSectionNumber(shuntCompensator, sectionIndex);
        ValidationUtil.checkSectionB(shuntCompensator, b);
        SectionImpl oldValue = sections.put(sectionIndex, new SectionImpl(b, g));
        shuntCompensator.notifyUpdate(notifyUpdateSection(sectionIndex, "b"), Optional.ofNullable(oldValue).map(SectionImpl::getB).orElse(Double.NaN), b);
        shuntCompensator.notifyUpdate(notifyUpdateSection(sectionIndex, "g"), Optional.ofNullable(oldValue).map(SectionImpl::getG).orElse(Double.NaN), g);
        return this;
    }

    @Override
    public ShuntCompensatorNonLinearModel removeSection(int sectionIndex) {
        ValidationUtil.checkSectionNumber(shuntCompensator, sectionIndex);
        if (shuntCompensator.getCurrentSectionCount() == sectionIndex) {
            throw new ValidationException(shuntCompensator, "the section index to remove (" + sectionIndex + ") is the current section count");
        }
        if (!sections.containsKey(sectionIndex)) {
            throw new ValidationException(shuntCompensator, invalidSectionNumberMessage(sectionIndex, INDEX, "susceptance nor conductance"));
        }
        SectionImpl oldValue = sections.remove(sectionIndex);
        shuntCompensator.notifyUpdate(notifyUpdateSection(sectionIndex, "b"), oldValue.getB(), Double.NaN);
        shuntCompensator.notifyUpdate(notifyUpdateSection(sectionIndex, "g"), oldValue.getG(), Double.NaN);
        return this;
    }

    @Override
    public ShuntCompensatorModelType getType() {
        return ShuntCompensatorModelType.NON_LINEAR;
    }

    @Override
    public boolean containsSection(int sectionNumber) {
        return sections.containsKey(sectionNumber);
    }

    @Override
    public int getMaximumSectionCount() {
        return sections.lastKey();
    }

    @Override
    public double getB(int sectionCount) {
        if (sectionCount != 0 && !sections.containsKey(sectionCount)) {
            throw new PowsyblException(invalidSectionNumberMessage(sectionCount, "count", "susceptance"));
        }
        return sections.entrySet().stream()
                .filter(e -> e.getKey() <= sectionCount)
                .mapToDouble(e -> e.getValue().getB())
                .sum();
    }

    @Override
    public double getG(int sectionCount) {
        if (sectionCount != 0 && !sections.containsKey(sectionCount)) {
            throw new PowsyblException(invalidSectionNumberMessage(sectionCount, "count", "conductance"));
        }
        return sections.entrySet().stream()
                .filter(e -> e.getKey() <= sectionCount)
                .mapToDouble(e -> e.getValue().getG())
                .filter(g -> !Double.isNaN(g))
                .sum();
    }

    @Override
    public double getBSection(int sectionIndex) {
        if (sectionIndex == 0) {
            return 0;
        }
        if (sections.containsKey(sectionIndex)) {
            return sections.get(sectionIndex).getB();
        }
        throw new PowsyblException(invalidSectionNumberMessage(sectionIndex, INDEX, "susceptance"));
    }

    @Override
    public double getGSection(int sectionIndex) {
        if (sectionIndex == 0) {
            return 0;
        }
        if (sections.containsKey(sectionIndex)) {
            double g = sections.get(sectionIndex).getG();
            return Double.isNaN(g) ? 0 : g;
        }
        throw new PowsyblException(invalidSectionNumberMessage(sectionIndex, INDEX, "conductance"));
    }

    private static String invalidSectionNumberMessage(int sectionNum, String sectionParameter, String attributes) {
        return "the given section " + sectionParameter + " (" + sectionNum + ") is not associated with any " + attributes;
    }

    private static String notifyUpdateSection(int sectionNum, String attribute) {
        return "section" + sectionNum + "." + attribute;
    }
}
