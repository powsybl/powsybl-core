/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ShuntCompensatorLinearModel;
import com.powsybl.iidm.network.ShuntCompensatorModelType;
import com.powsybl.iidm.network.ValidationUtil;

import java.util.Objects;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class ShuntCompensatorLinearModelImpl implements ShuntCompensatorModelExt, ShuntCompensatorLinearModel {

    private ShuntCompensatorImpl shuntCompensator;

    private double bPerSection;

    private double gPerSection;

    private int maximumSectionCount;

    ShuntCompensatorLinearModelImpl(double bPerSection, double gPerSection, int maximumSectionCount) {
        this.bPerSection = bPerSection;
        this.gPerSection = gPerSection;
        this.maximumSectionCount = maximumSectionCount;
    }

    @Override
    public ShuntCompensatorLinearModelImpl attach(ShuntCompensatorImpl shuntCompensator) {
        if (this.shuntCompensator != null) {
            throw new IllegalStateException("ShuntCompensatorLinearModelImpl already attached to " + this.shuntCompensator.getId());
        }

        this.shuntCompensator = Objects.requireNonNull(shuntCompensator);
        return this;
    }

    @Override
    public double getBPerSection() {
        return bPerSection;
    }

    @Override
    public ShuntCompensatorLinearModel setBPerSection(double bPerSection) {
        ValidationUtil.checkBPerSection(shuntCompensator, bPerSection);
        double oldValue = this.bPerSection;
        this.bPerSection = bPerSection;
        shuntCompensator.notifyUpdate("bPerSection", oldValue, bPerSection);
        return this;
    }

    @Override
    public double getGPerSection() {
        return gPerSection;
    }

    @Override
    public ShuntCompensatorLinearModel setGPerSection(double gPerSection) {
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
    public double getB(int sectionCount) {
        if (sectionCount < 0 || sectionCount > maximumSectionCount) {
            throw new PowsyblException("the given count of sections in service (" + sectionCount + ") is invalid (negative or strictly greater than the maximum sections count");
        }
        return bPerSection * sectionCount;
    }

    @Override
    public double getG(int sectionCount) {
        if (sectionCount < 0 || sectionCount > maximumSectionCount) {
            throw new PowsyblException("the given count of sections in service (" + sectionCount + ") is invalid (negative or strictly greater than the maximum sections count");
        }
        return Double.isNaN(gPerSection) ? 0 : gPerSection * sectionCount;
    }

    @Override
    public ShuntCompensatorLinearModel setMaximumSectionCount(int maximumSectionCount) {
        ValidationUtil.checkSections(shuntCompensator, shuntCompensator.findSectionCount().isPresent() ? shuntCompensator.getSectionCount() : null, maximumSectionCount, shuntCompensator.getNetwork().getMinValidationLevel());
        int oldValue = this.maximumSectionCount;
        this.maximumSectionCount = maximumSectionCount;
        shuntCompensator.notifyUpdate("maximumSectionCount", oldValue, maximumSectionCount);
        return this;
    }

    @Override
    public ShuntCompensatorModelType getType() {
        return ShuntCompensatorModelType.LINEAR;
    }
}
