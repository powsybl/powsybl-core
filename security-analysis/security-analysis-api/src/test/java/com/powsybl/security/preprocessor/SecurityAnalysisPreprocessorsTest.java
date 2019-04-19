/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.preprocessor;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteSource;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.ContingenciesProviderFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysisInput;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisPreprocessorsTest {

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
    public void factoryForName() {
        Assertions.assertThat(SecurityAnalysisPreprocessors.factoryForName("test"))
                .isInstanceOf(TestPreprocessorFactory.class);
    }

    @Test
    public void contingenciesProviderPreprocessor() {
        ContingenciesProvider provider = Mockito.mock(ContingenciesProvider.class);
        ContingenciesProviderFactory providerFactory = new ContingenciesProviderFactory() {
            @Override
            public ContingenciesProvider create() {
                return provider;
            }
        };
        SecurityAnalysisPreprocessorFactory factory = new DefaultSecurityAnalysisPreprocessorFactory(providerFactory);

        assertEquals("default", factory.getName());
        SecurityAnalysisPreprocessor preprocessor = factory.newPreprocessor(ByteSource.wrap("".getBytes()));

        SecurityAnalysisInput input = new SecurityAnalysisInput(Mockito.mock(Network.class), "variant");
        preprocessor.preprocess(input);

        assertSame(provider, input.getContingenciesProvider());
    }
}
