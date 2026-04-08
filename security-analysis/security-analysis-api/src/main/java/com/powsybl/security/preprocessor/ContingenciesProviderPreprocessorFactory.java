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

import java.util.Objects;

/**
 * A factory which creates preprocessors that read contingencies according to a
 * {@link ContingenciesProviderFactory}
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class ContingenciesProviderPreprocessorFactory implements SecurityAnalysisPreprocessorFactory {

    private final ContingenciesProviderFactory contingenciesProviderFactory;

    public ContingenciesProviderPreprocessorFactory(ContingenciesProviderFactory contingenciesProviderFactory) {
        this.contingenciesProviderFactory = Objects.requireNonNull(contingenciesProviderFactory);
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
