/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.ContingenciesProviders;
import com.powsybl.contingency.EmptyContingencyListProvider;
import com.powsybl.iidm.network.Network;
import org.junit.Test;
import org.mockito.Mockito;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisInputTest {

    @Test
    public void test() {

        Network network = Mockito.mock(Network.class);
        SecurityAnalysisInput inputs = new SecurityAnalysisInput(network, INITIAL_VARIANT_ID);

        assertThat(inputs.getContingenciesProvider())
                .isNotNull()
                .isInstanceOf(EmptyContingencyListProvider.class);
        assertThat(inputs.getLimitViolationDetector())
                .isNotNull()
                .isInstanceOf(DefaultLimitViolationDetector.class);

        SecurityAnalysisParameters params = new SecurityAnalysisParameters();
        ContingenciesProvider provider = ContingenciesProviders.emptyProvider();
        LimitViolationDetector detector = new DefaultLimitViolationDetector();

        inputs.setParameters(params);
        inputs.setContingencies(provider);
        inputs.setDetector(detector);

        assertThat(inputs.getNetworkVariant().getNetwork())
                .isSameAs(network);
        assertThat(inputs.getNetworkVariant().getVariantId())
                .isEqualTo(INITIAL_VARIANT_ID);
        assertThat(inputs.getParameters())
                .isSameAs(params);
        assertThat(inputs.getContingenciesProvider())
                .isSameAs(provider);
        assertThat(inputs.getLimitViolationDetector())
                .isSameAs(detector);
    }

}
