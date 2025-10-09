/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.GeneratorStartupAdder;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
public class GeneratorStartupAdderImpl extends AbstractExtensionAdder<Generator, GeneratorStartup> implements GeneratorStartupAdder {

    private double plannedActivePowerSetpoint = Double.NaN;

    private double startupCost = Double.NaN;

    private double marginalCost = Double.NaN;

    private double plannedOutageRate = Double.NaN;

    private double forcedOutageRate = Double.NaN;

    public GeneratorStartupAdderImpl(Generator generator) {
        super(generator);
    }

    @Override
    protected GeneratorStartup createExtension(Generator extendable) {
        return new GeneratorStartupImpl(extendable, plannedActivePowerSetpoint, startupCost, marginalCost, plannedOutageRate, forcedOutageRate);
    }

    @Override
    public GeneratorStartupAdderImpl withPlannedActivePowerSetpoint(double plannedActivePowerSetpoint) {
        this.plannedActivePowerSetpoint = plannedActivePowerSetpoint;
        return this;
    }

    @Override
    public GeneratorStartupAdder withStartupCost(double startUpCost) {
        this.startupCost = startUpCost;
        return this;
    }

    @Override
    public GeneratorStartupAdderImpl withMarginalCost(double marginalCost) {
        this.marginalCost = marginalCost;
        return this;
    }

    @Override
    public GeneratorStartupAdderImpl withPlannedOutageRate(double plannedOutageRate) {
        this.plannedOutageRate = plannedOutageRate;
        return this;
    }

    @Override
    public GeneratorStartupAdderImpl withForcedOutageRate(double forcedOutageRate) {
        this.forcedOutageRate = forcedOutageRate;
        return this;
    }
}
