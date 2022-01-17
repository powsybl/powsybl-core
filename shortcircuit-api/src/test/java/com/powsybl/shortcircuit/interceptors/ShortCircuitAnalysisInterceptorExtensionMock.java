/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.interceptors;

import com.google.auto.service.AutoService;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
@AutoService(ShortCircuitAnalysisInterceptorExtension.class)
public class ShortCircuitAnalysisInterceptorExtensionMock implements ShortCircuitAnalysisInterceptorExtension {
    @Override
    public String getName() {
        return "ShortCircuitInterceptorExtensionMock";
    }

    @Override
    public ShortCircuitAnalysisInterceptor createInterceptor() {
        return new ShortCircuitAnalysisInterceptorMock();
    }
}
