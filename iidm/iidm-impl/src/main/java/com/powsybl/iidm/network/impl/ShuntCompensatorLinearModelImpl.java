/**
 * Copyright (c) 2019, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ShuntCompensatorLinearModel;
import com.powsybl.iidm.network.ShuntCompensatorModelType;

import static com.powsybl.iidm.network.ShuntCompensatorModelType.LINEAR;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class ShuntCompensatorLinearModelImpl extends AbstractShuntCompensatorModel implements ShuntCompensatorLinearModel {

    private double bPerSection;

    private int maximumSectionCount;

    ShuntCompensatorLinearModelImpl(double bPerSection, int maximumSectionCount) {
        this.bPerSection = bPerSection;
        this.maximumSectionCount = maximumSectionCount;
    }

    @Override
    public double getbPerSection() {
        return bPerSection;
    }

    @Override
    public ShuntCompensatorLinearModel setbPerSection(double bPerSection) {
        ValidationUtil.checkbPerSection(shuntCompensator, bPerSection);
        this.bPerSection = bPerSection;
        return this;
    }

    @Override
    public ShuntCompensatorLinearModel setMaximumSectionCount(int maximumSectionCount) {
        ValidationUtil.checkSections(shuntCompensator, shuntCompensator.getCurrentSectionCount(), maximumSectionCount);
        this.maximumSectionCount = maximumSectionCount;
        return this;
    }

    @Override
    public double getMaximumB() {
        return bPerSection * maximumSectionCount;
    }

    @Override
    public ShuntCompensatorModelType getType() {
        return LINEAR;
    }

    @Override
    public void checkCurrentSection(int currentSectionCount) {
        ValidationUtil.checkSections(shuntCompensator, currentSectionCount, maximumSectionCount);
    }

    @Override
    public int getMaximumSectionCount() {
        return maximumSectionCount;
    }

    @Override
    public double getB(int sectionNum) {
        return bPerSection * sectionNum;
    }
}
