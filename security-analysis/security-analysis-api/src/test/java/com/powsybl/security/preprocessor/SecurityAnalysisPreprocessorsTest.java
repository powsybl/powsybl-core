/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.preprocessor;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteSource;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.ContingenciesProviderFactory;
import com.powsybl.contingency.EmptyContingencyListProvider;
import com.powsybl.contingency.EmptyContingencyListProviderFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysisConfig;
import com.powsybl.security.SecurityAnalysisInput;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class SecurityAnalysisPreprocessorsTest {

    @AutoService(SecurityAnalysisPreprocessorFactory.class)
    public static class TestPreprocessorFactory implements SecurityAnalysisPreprocessorFactory {

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public SecurityAnalysisPreprocessor newPreprocessor(ByteSource configSource) {
            return null;
        }
    }

    @Test
    void factoryForName() {
        assertThat(SecurityAnalysisPreprocessors.factoryForName("test"))
                .isInstanceOf(TestPreprocessorFactory.class);
    }

    @Test
    void contingenciesProviderPreprocessor() {
        ContingenciesProvider provider = Mockito.mock(ContingenciesProvider.class);
        ContingenciesProviderFactory providerFactory = () -> provider;
        SecurityAnalysisPreprocessorFactory factory = new ContingenciesProviderPreprocessorFactory(providerFactory);

        assertEquals("default", factory.getName());
        SecurityAnalysisPreprocessor preprocessor = factory.newPreprocessor(ByteSource.wrap("".getBytes()));

        SecurityAnalysisInput input = new SecurityAnalysisInput(Mockito.mock(Network.class), "variant");
        preprocessor.preprocess(input);

        assertSame(provider, input.getContingenciesProvider());
    }

    @Test
    void configuredFactory() {

        Optional<SecurityAnalysisPreprocessorFactory> factory = SecurityAnalysisPreprocessors.configuredFactory(new SecurityAnalysisConfig("test"));
        assertThat(factory)
                .isPresent()
                .get()
                .isInstanceOf(TestPreprocessorFactory.class);

        factory = SecurityAnalysisPreprocessors.configuredFactory(new SecurityAnalysisConfig());
        assertThat(factory).isNotPresent();
    }

    @Test
    void wrappedContingenciesProviderFactory() {

        SecurityAnalysisPreprocessorFactory factory = SecurityAnalysisPreprocessors.wrap(new EmptyContingencyListProviderFactory());
        assertNotNull(factory);

        SecurityAnalysisPreprocessor preprocessor = factory.newPreprocessor(ByteSource.empty());
        SecurityAnalysisInput input = new SecurityAnalysisInput(Mockito.mock(Network.class), "");
        preprocessor.preprocess(input);
        assertThat(input.getContingenciesProvider())
                .isNotNull()
                .isInstanceOf(EmptyContingencyListProvider.class);
    }
}
