/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.json;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityDummyExtension extends AbstractExtension<DynamicSecurityAnalysisParameters> {

    double parameterDouble;
    boolean parameterBoolean;
    String parameterString;

    public DynamicSecurityDummyExtension() {
        super();
    }

    DynamicSecurityDummyExtension(DynamicSecurityDummyExtension another) {
        this.parameterDouble = another.parameterDouble;
        this.parameterBoolean = another.parameterBoolean;
        this.parameterString = another.parameterString;
    }

    boolean isParameterBoolean() {
        return parameterBoolean;
    }

    double getParameterDouble() {
        return parameterDouble;
    }

    String getParameterString() {
        return parameterString;
    }

    void setParameterBoolean(boolean parameterBoolean) {
        this.parameterBoolean = parameterBoolean;
    }

    void setParameterString(String parameterString) {
        this.parameterString = parameterString;
    }

    void setParameterDouble(double parameterDouble) {
        this.parameterDouble = parameterDouble;
    }

    @Override
    public String getName() {
        return "dummy-extension";
    }
}
