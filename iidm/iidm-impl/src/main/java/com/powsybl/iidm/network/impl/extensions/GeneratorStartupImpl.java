/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorStartup;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
public class GeneratorStartupImpl extends AbstractExtension<Generator> implements GeneratorStartup {

    private double plannedActivePowerSetpoint;

    private double startupCost;

    private double marginalCost;

    private double plannedOutageRate;

    private double forcedOutageRate;

    public GeneratorStartupImpl(Generator generator, double plannedActivePowerSetpoint, double startupCost, double marginalCost,
                                double plannedOutageRate, double forcedOutageRate) {
        super(generator);
        this.plannedActivePowerSetpoint = plannedActivePowerSetpoint;
        this.startupCost = startupCost;
        this.marginalCost = marginalCost;
        this.plannedOutageRate = plannedOutageRate;
        this.forcedOutageRate = forcedOutageRate;
    }

    @Override
    public double getPlannedActivePowerSetpoint() {
        return plannedActivePowerSetpoint;
    }

    @Override
    public GeneratorStartupImpl setPlannedActivePowerSetpoint(double predefinedActivePowerSetpoint) {
        this.plannedActivePowerSetpoint = predefinedActivePowerSetpoint;
        return this;
    }

    @Override
    public double getStartupCost() {
        return startupCost;
    }

    @Override
    public GeneratorStartup setStartupCost(double startUpCost) {
        this.startupCost = startUpCost;
        return this;
    }

    @Override
    public double getMarginalCost() {
        return marginalCost;
    }

    @Override
    public GeneratorStartupImpl setMarginalCost(double marginalCost) {
        this.marginalCost = marginalCost;
        return this;
    }

    @Override
    public double getPlannedOutageRate() {
        return plannedOutageRate;
    }

    @Override
    public GeneratorStartupImpl setPlannedOutageRate(double plannedOutageRate) {
        this.plannedOutageRate = plannedOutageRate;
        return this;
    }

    @Override
    public double getForcedOutageRate() {
        return forcedOutageRate;
    }

    @Override
    public GeneratorStartupImpl setForcedOutageRate(double forcedOutageRate) {
        this.forcedOutageRate = forcedOutageRate;
        return this;
    }
}
