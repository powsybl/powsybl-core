/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
class DynamicSecurityAnalysisRunParametersTest {

    @Test
    void testNonNullParameters() {
        DynamicSecurityAnalysisRunParameters parameters = DynamicSecurityAnalysisRunParameters.getDefault();
        assertThatThrownBy(() -> parameters.setEventModelsSupplier(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Event models supplier should not be null");
        assertThatThrownBy(() -> parameters.setDynamicSecurityAnalysisParameters(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Security analysis parameters should not be null");
    }
}
