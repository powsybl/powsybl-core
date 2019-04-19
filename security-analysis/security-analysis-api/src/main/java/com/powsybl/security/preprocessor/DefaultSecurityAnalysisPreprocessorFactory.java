/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.preprocessor;

import com.google.common.io.ByteSource;
import com.powsybl.contingency.ContingenciesProviderFactory;
import com.powsybl.contingency.ContingenciesProviders;

/**
 * A factory which creates preprocessors that read contingencies according to a
 * {@link ContingenciesProviderFactory}
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class DefaultSecurityAnalysisPreprocessorFactory implements SecurityAnalysisPreprocessorFactory {

    private final ContingenciesProviderFactory contingenciesProviderFactory;

    public DefaultSecurityAnalysisPreprocessorFactory() {
        this(ContingenciesProviders.newDefaultFactory());
    }

    public DefaultSecurityAnalysisPreprocessorFactory(ContingenciesProviderFactory contingenciesProviderFactory) {
        this.contingenciesProviderFactory = contingenciesProviderFactory;
    }

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public SecurityAnalysisPreprocessor newPreprocessor(ByteSource configSource) {
        return SecurityAnalysisPreprocessors.contingenciesPreprocessor(contingenciesProviderFactory, configSource);
    }
}
