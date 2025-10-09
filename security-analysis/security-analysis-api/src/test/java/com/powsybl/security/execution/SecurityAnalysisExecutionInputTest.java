/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.execution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.SecurityAnalysisParameters;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class SecurityAnalysisExecutionInputTest {

    @Test
    void defaultValues() {
        SecurityAnalysisExecutionInput input = new SecurityAnalysisExecutionInput();

        assertFalse(input.getContingenciesSource().isPresent());
        assertNull(input.getNetworkVariant());
        assertNull(input.getParameters());
        assertTrue(input.getViolationTypes().isEmpty());
        assertTrue(input.getResultExtensions().isEmpty());
    }

    @Test
    void setters() {
        Network network = Mockito.mock(Network.class);
        ByteSource source = ByteSource.wrap(new byte[0]);
        SecurityAnalysisParameters params = new SecurityAnalysisParameters();
        Set<String> extensions = ImmutableSet.of("ext1", "ext2");
        Set<LimitViolationType> types = EnumSet.of(LimitViolationType.CURRENT);

        SecurityAnalysisExecutionInput input = new SecurityAnalysisExecutionInput();
        input.setNetworkVariant(network, "variantId");
        input.setContingenciesSource(source);
        input.setParameters(params);
        input.addResultExtensions(extensions);
        input.addResultExtension("ext3");
        input.addViolationTypes(types);
        input.addViolationType(LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT);

        assertSame(network, input.getNetworkVariant().getNetwork());
        assertEquals("variantId", input.getNetworkVariant().getVariantId());
        assertSame(source, input.getContingenciesSource().orElseThrow(IllegalStateException::new));
        assertSame(params, input.getParameters());
        assertEquals(ImmutableList.of("ext1", "ext2", "ext3"), input.getResultExtensions());
        assertEquals(EnumSet.of(LimitViolationType.CURRENT,
                LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT),
                input.getViolationTypes());
    }
}
