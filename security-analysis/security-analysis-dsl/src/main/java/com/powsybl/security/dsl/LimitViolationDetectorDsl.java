/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl;

import com.google.auto.service.AutoService;
import com.powsybl.security.SecurityAnalysisInput;
import groovy.lang.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Enables a DSL which defines current limits factors, as described in {@link LimitFactorsLoader}.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
@AutoService(SecurityAnalysisDsl.class)
public class LimitViolationDetectorDsl implements SecurityAnalysisDsl {

    private static final Logger LOGGER = LoggerFactory.getLogger(LimitViolationDetectorDsl.class);

    private static void bindDetector(SecurityAnalysisInput inputs, LimitFactors factors) {
        inputs.setDetector(new LimitViolationDetectorWithFactors(factors));
    }

    /**
     * Enable the limit factors DSL, which will collect limit factors definition into the
     * {@link com.powsybl.security.LimitViolationDetector LimitViolationDetector} of the specified input.
     */
    @Override
    public void enable(Binding binding, SecurityAnalysisInput inputs) {
        LOGGER.debug("Loading limits violation detector DSL.");
        LimitFactorsLoader.loadDsl(binding, f -> bindDetector(inputs, f));
    }

}
