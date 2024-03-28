/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.limitmodification;

import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.limitmodification.result.AbstractReducedLoadingLimits;
import com.powsybl.iidm.network.limitmodification.result.DefaultReducedLimitsContainer;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;
import com.powsybl.iidm.network.limitmodification.result.UnalteredLimitsContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class LimitsContainerTest {

    private static LoadingLimits originalLimits;
    private static AbstractReducedLoadingLimits reducedLimits;

    @BeforeAll
    static void init() {
        LoadingLimits.TemporaryLimit t600 = Mockito.mock(LoadingLimits.TemporaryLimit.class);
        when(t600.getAcceptableDuration()).thenReturn(600);
        when(t600.getName()).thenReturn("10'");
        when(t600.getValue()).thenReturn(1200.);
        LoadingLimits.TemporaryLimit t300 = Mockito.mock(LoadingLimits.TemporaryLimit.class);
        when(t300.getAcceptableDuration()).thenReturn(300);
        when(t300.getName()).thenReturn("5'");
        when(t300.getValue()).thenReturn(1400.);

        originalLimits = Mockito.mock(LoadingLimits.class);
        when(originalLimits.getPermanentLimit()).thenReturn(1000.);
        when(originalLimits.getTemporaryLimits()).thenReturn(List.of(t600, t300));

        AbstractReducedLoadingLimits.ReducedTemporaryLimit reducedT300 = Mockito.mock(AbstractReducedLoadingLimits.ReducedTemporaryLimit.class);
        when(reducedT300.getAcceptableDuration()).thenReturn(300);
        when(reducedT300.getName()).thenReturn("5'");
        when(reducedT300.getValue()).thenReturn(1050.);
        when(reducedT300.getOriginalValue()).thenReturn(1400.);
        when(reducedT300.getLimitReduction()).thenReturn(0.75);

        reducedLimits = Mockito.mock(AbstractReducedLoadingLimits.class);
        when(reducedLimits.getPermanentLimit()).thenReturn(800.);
        when(reducedLimits.getOriginalPermanentLimit()).thenReturn(1000.);
        when(reducedLimits.getPermanentLimitReduction()).thenReturn(0.8);
        when(reducedLimits.getTemporaryLimits()).thenReturn(List.of(reducedT300));
        when(reducedLimits.getTemporaryLimit(300)).thenReturn(reducedT300);
    }

    @Test
    void unalteredLimitsContainerTest() {
        LimitsContainer<LoadingLimits> container = new UnalteredLimitsContainer<>(originalLimits);
        assertFalse(container.hasChanged());
        assertEquals(originalLimits, container.getLimits());
        assertEquals(originalLimits, container.getOriginalLimits());
    }

    @Test
    void defaultReducedLimitsContainerTest() {
        DefaultReducedLimitsContainer container = new DefaultReducedLimitsContainer(reducedLimits, originalLimits);
        assertTrue(container.hasChanged());
        assertEquals(originalLimits, container.getOriginalLimits());
        assertEquals(reducedLimits, container.getLimits());
        assertEquals(800., container.getLimits().getPermanentLimit(), 0.01);
        assertEquals(1050., container.getLimits().getTemporaryLimit(300).getValue(), 0.01);
        assertEquals(1000., container.getOriginalPermanentLimit(), 0.01);
        assertEquals(0.8, container.getPermanentLimitReduction(), 0.01);
        assertEquals(0.75, container.getTemporaryLimitReduction(300), 0.01);
        assertEquals(1400., container.getOriginalTemporaryLimit(300), 0.01);
        assertNull(container.getTemporaryLimitReduction(600));
    }
}
