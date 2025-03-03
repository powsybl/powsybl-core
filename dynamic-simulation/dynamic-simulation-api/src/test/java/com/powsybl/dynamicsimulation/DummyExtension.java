/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation;

import com.powsybl.commons.extensions.AbstractExtension;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DummyExtension extends AbstractExtension<DynamicSimulationParameters> {

    public static final double PARAMETER_DOUBLE_DEFAULT_VALUE = 0;
    public static final boolean PARAMETER_BOOLEAN_DEFAULT_VALUE = false;
    public static final String PARAMETER_STRING_DEFAULT_VALUE = null;

    private double parameterDouble = PARAMETER_DOUBLE_DEFAULT_VALUE;
    private boolean parameterBoolean = PARAMETER_BOOLEAN_DEFAULT_VALUE;
    private String parameterString = PARAMETER_STRING_DEFAULT_VALUE;

    public DummyExtension() {
        super();
    }

    @Override
    public String getName() {
        return "dynamic-simulation-dummy-extension";
    }

    public double getParameterDouble() {
        return this.parameterDouble;
    }

    public void setParameterDouble(double parameterDouble) {
        this.parameterDouble = parameterDouble;
    }

    public boolean isParameterBoolean() {
        return this.parameterBoolean;
    }

    public void setParameterBoolean(boolean parameterBoolean) {
        this.parameterBoolean = parameterBoolean;
    }

    public String getParameterString() {
        return this.parameterString;
    }

    public void setParameterString(String parameterString) {
        this.parameterString = parameterString;
    }
}
