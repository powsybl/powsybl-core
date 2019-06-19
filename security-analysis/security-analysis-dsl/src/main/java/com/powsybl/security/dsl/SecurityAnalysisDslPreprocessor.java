/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl;

import com.google.common.io.ByteSource;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.dsl.GroovyAggregateDsl;
import com.powsybl.dsl.GroovyDsl;
import com.powsybl.dsl.GroovyDslInterpreter;
import com.powsybl.security.SecurityAnalysisInput;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * A {@link SecurityAnalysisPreprocessor} which reads the configuration from a DSL groovy script.
 * The DSL may be defined by multiple services implementing {@link SecurityAnalysisDsl} service.
 * All DSLs will be loaded before actually executing the script
 * by calling the {@link SecurityAnalysisPreprocessor#preprocess(SecurityAnalysisInput)} method.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisDslPreprocessor implements SecurityAnalysisPreprocessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAnalysisDslPreprocessor.class);

    private final ByteSource configurationSource;

    SecurityAnalysisDslPreprocessor(ByteSource configurationSource) {
        this.configurationSource = Objects.requireNonNull(configurationSource);
    }

    @Override
    public void preprocess(SecurityAnalysisInput configuration) {

        LOGGER.debug("Starting configuration of security analysis inputs, based on groovy DSL.");

        List<SecurityAnalysisDsl> dslParts = new ServiceLoaderCache<>(SecurityAnalysisDsl.class).getServices();
        GroovyDsl<SecurityAnalysisInput> dsl = new GroovyAggregateDsl<>(dslParts);

        GroovyDslInterpreter<SecurityAnalysisInput> interpreter = new GroovyDslInterpreter<>(dsl);

        LOGGER.debug("Evaluating security analysis configuration DSL file.");
        interpreter.interprete(configurationSource.asCharSource(StandardCharsets.UTF_8), configuration);
    }

}
