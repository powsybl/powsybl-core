/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ShuntCompensatorLinearModel;
import com.powsybl.iidm.network.ShuntCompensatorModelType;
import com.powsybl.iidm.network.ValidationUtil;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class ShuntCompensatorLinearModelImpl extends AbstractShuntCompensatorModel implements ShuntCompensatorLinearModel {

    private double bPerSection;

    private double gPerSection;

    private int maximumSectionCount;

    ShuntCompensatorLinearModelImpl(double bPerSection, double gPerSection, int maximumSectionCount) {
        this.bPerSection = bPerSection;
        this.gPerSection = gPerSection;
        this.maximumSectionCount = maximumSectionCount;
    }

    @Override
    public double getbPerSection() {
        return bPerSection;
    }

    @Override
    public ShuntCompensatorLinearModel setbPerSection(double bPerSection) {
        ValidationUtil.checkbPerSection(shuntCompensator, bPerSection);
        double oldValue = this.bPerSection;
        this.bPerSection = bPerSection;
        shuntCompensator.notifyUpdate("bPerSection", oldValue, bPerSection);
        return this;
    }

    @Override
    public double getgPerSection() {
        return gPerSection;
    }

    @Override
    public ShuntCompensatorLinearModel setgPerSection(double gPerSection) {
        double oldValue = this.gPerSection;
        this.gPerSection = gPerSection;
        shuntCompensator.notifyUpdate("gPerSection", oldValue, gPerSection);
        return this;
    }

    @Override
    public int getMaximumSectionCount() {
        return maximumSectionCount;
    }

    @Override
    public ShuntCompensatorLinearModel setMaximumSectionCount(int maximumSectionCount) {
        ValidationUtil.checkSections(shuntCompensator, shuntCompensator.getCurrentSectionCount(), maximumSectionCount);
        int oldValue = this.maximumSectionCount;
        this.maximumSectionCount = maximumSectionCount;
        shuntCompensator.notifyUpdate("maximumSectionCount", oldValue, maximumSectionCount);
        return this;
    }

    @Override
    public ShuntCompensatorModelType getType() {
        return ShuntCompensatorModelType.LINEAR;
    }

    @Override
    public boolean containsSection(int sectionNumber) {
        return sectionNumber >= 0 && sectionNumber <= maximumSectionCount;
    }

    @Override
    public double getB(int sectionNum) {
        return bPerSection * sectionNum;
    }

    @Override
    public double getG(int sectionNum) {
        return gPerSection * sectionNum;
    }
}
