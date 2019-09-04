/**
 * Copyright (c) 2019, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ShuntCompensatorModelType;
import com.powsybl.iidm.network.ShuntCompensatorNonLinearModel;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static com.powsybl.iidm.network.ShuntCompensatorModelType.NON_LINEAR;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class ShuntCompensatorNonLinearModelImpl extends AbstractShuntCompensatorModel implements ShuntCompensatorNonLinearModel {

    static class SectionImpl implements Section {

        private final double b;

        SectionImpl(double b) {
            this.b = b;
        }

        @Override
        public double getB() {
            return b;
        }
    }

    private final TreeMap<Integer, SectionImpl> sections;

    ShuntCompensatorNonLinearModelImpl(TreeMap<Integer, SectionImpl> sections) {
        this.sections = sections;
    }

    @Override
    public Map<Integer, Section> getSections() {
        return Collections.unmodifiableMap(sections);
    }

    @Override
    public Section getSection(int sectionNum) {
        return sections.get(sectionNum);
    }

    @Override
    public ShuntCompensatorNonLinearModelImpl addOrReplaceSection(int sectionNum, double b) {
        if (sectionNum < 0) {
            throw new ValidationException(shuntCompensator,
                    "this number of section (" + sectionNum
                            + ") should be greater than or equal to 0");
        }
        ValidationUtil.checkbPerSection(shuntCompensator, b);
        sections.put(sectionNum, new SectionImpl(b));
        return this;
    }

    @Override
    public ShuntCompensatorNonLinearModelImpl removeSection(int sectionNum) {
        if (sectionNum < 0) {
            throw new ValidationException(shuntCompensator,
                    "this number of section (" + sectionNum
                            + ") should be greater than or equal to 0");
        }
        if (shuntCompensator.getCurrentSectionCount() == sectionNum) {
            throw new ValidationException(shuntCompensator, "the number of section to remove (" + sectionNum + ") is the current section count");
        }
        sections.remove(sectionNum);
        return this;
    }

    @Override
    public ShuntCompensatorModelType getType() {
        return NON_LINEAR;
    }

    @Override
    public void checkCurrentSection(int currentSectionCount) {
        checkCurrentSection(shuntCompensator, currentSectionCount);
    }

    @Override
    public void checkCurrentSection(Validable validable, int currentSectionCount) {
        ValidationUtil.checkSections(validable, currentSectionCount, getMaximumSectionCount());
        if (!sections.containsKey(currentSectionCount)) {
            throw new ValidationException(validable, "the current number of section (" + currentSectionCount + ") is not associated with any susceptance");
        }
    }

    @Override
    public double getMaximumB() {
        return sections.values().stream().map(SectionImpl::getB).max(Double::compare).orElseThrow(() -> new PowsyblException("a shunt compensator must have at least one section"));
    }

    @Override
    public int getMaximumSectionCount() {
        return sections.lastKey();
    }

    @Override
    public double getB(int sectionNum) {
        return Optional.ofNullable(sections.get(sectionNum))
                .map(Section::getB)
                .orElseThrow(() -> new PowsyblException("There is no section with the number " + sectionNum));
    }
}
