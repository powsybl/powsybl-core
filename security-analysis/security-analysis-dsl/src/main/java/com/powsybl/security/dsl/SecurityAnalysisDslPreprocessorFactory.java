/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteSource;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessor;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessorFactory;

import java.util.Objects;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
@AutoService(SecurityAnalysisPreprocessorFactory.class)
public class SecurityAnalysisDslPreprocessorFactory implements SecurityAnalysisPreprocessorFactory {

    @Override
    public String getName() {
        return "groovy-dsl";
    }

    @Override
    public SecurityAnalysisPreprocessor newPreprocessor(ByteSource configSource) {
        Objects.requireNonNull(configSource);
        return new SecurityAnalysisDslPreprocessor(configSource);
    }

}
