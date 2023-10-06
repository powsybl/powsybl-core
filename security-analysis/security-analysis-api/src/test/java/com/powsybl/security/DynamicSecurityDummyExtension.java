/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;

/**
 * @author Laurent Issertial <laurent.issertial at rte-france.com>
 */
public class DynamicSecurityDummyExtension extends AbstractExtension<DynamicSecurityAnalysisParameters> {
    private double parameterDouble;
    private boolean parameterBoolean;
    private String parameterString;

    public DynamicSecurityDummyExtension() {
        super();
    }

    public DynamicSecurityDummyExtension(DynamicSecurityDummyExtension another) {
        this.parameterDouble = another.parameterDouble;
        this.parameterBoolean = another.parameterBoolean;
        this.parameterString = another.parameterString;
    }

    public boolean isParameterBoolean() {
        return parameterBoolean;
    }

    public double getParameterDouble() {
        return parameterDouble;
    }

    public String getParameterString() {
        return parameterString;
    }

    public void setParameterBoolean(boolean parameterBoolean) {
        this.parameterBoolean = parameterBoolean;
    }

    public void setParameterString(String parameterString) {
        this.parameterString = parameterString;
    }

    public void setParameterDouble(double parameterDouble) {
        this.parameterDouble = parameterDouble;
    }

    @Override
    public String getName() {
        return "dummy-extension";
    }
}
