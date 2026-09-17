/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.preprocessor;

import com.google.common.io.ByteSource;
import com.powsybl.contingency.ContingenciesProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A factory which creates preprocessors that read contingencies according to a
 * {@link ContingenciesProviderFactory}
 *
 * @deprecated This class will be removed on later versions, avoid new usages of it.
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
@Deprecated(forRemoval = true, since = "7.4.0")
public class ContingenciesProviderPreprocessorFactory implements SecurityAnalysisPreprocessorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ContingenciesProviderPreprocessorFactory.class);
    private static final String DEPRECATED_WARNING_MSG =
            "Since version 7.4.0, the `preprocessor` property of the `security-analysis` module is deprecated and will be removed in a future version." +
            " Avoid new usages of the `SecurityAnalysisPreprocessor` plugin.";

    private final ContingenciesProviderFactory contingenciesProviderFactory;

    public ContingenciesProviderPreprocessorFactory(ContingenciesProviderFactory contingenciesProviderFactory) {
        LOG.warn(DEPRECATED_WARNING_MSG);
        this.contingenciesProviderFactory = Objects.requireNonNull(contingenciesProviderFactory);
    }

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public SecurityAnalysisPreprocessor newPreprocessor(ByteSource configSource) {
        LOG.warn(DEPRECATED_WARNING_MSG);
        return SecurityAnalysisPreprocessors.contingenciesPreprocessor(contingenciesProviderFactory, configSource);
    }
}
