/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.limitmodification.result;

import com.powsybl.iidm.network.LoadingLimits;
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
    private static LoadingLimits changedLimits;

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
        when(originalLimits.getTemporaryLimitValue(300)).thenReturn(1400.);

        LoadingLimits.TemporaryLimit changedT300 = Mockito.mock(LoadingLimits.TemporaryLimit.class);
        when(changedT300.getAcceptableDuration()).thenReturn(300);
        when(changedT300.getName()).thenReturn("5'");
        when(changedT300.getValue()).thenReturn(1050.);

        changedLimits = Mockito.mock(LoadingLimits.class);
        when(changedLimits.getPermanentLimit()).thenReturn(800.);
        when(changedLimits.getTemporaryLimits()).thenReturn(List.of(changedT300));
        when(changedLimits.getTemporaryLimit(300)).thenReturn(changedT300);
        when(changedLimits.getTemporaryLimitValue(300)).thenReturn(1050.);
    }

    @Test
    void unchangedLimitsContainerTest() {
        LimitsContainer<LoadingLimits> container = new UnchangedLimitsContainer<>(originalLimits);
        assertFalse(container.hasChanged());
        assertEquals(originalLimits, container.getLimits());
        assertEquals(originalLimits, container.getOriginalLimits());
    }

    @Test
    void changedLimitsContainerTest() {
        AbstractChangedLimitsContainer<LoadingLimits, LoadingLimits> container = new AbstractChangedLimitsContainer<>(changedLimits, originalLimits) {
            @Override
            public double getOriginalPermanentLimit() {
                return originalLimits.getPermanentLimit();
            }

            @Override
            public Double getOriginalTemporaryLimit(int acceptableDuration) {
                return originalLimits.getTemporaryLimitValue(acceptableDuration);
            }
        };
        assertTrue(container.hasChanged());
        assertEquals(originalLimits, container.getOriginalLimits());
        assertEquals(changedLimits, container.getLimits());
        assertEquals(800., container.getLimits().getPermanentLimit(), 0.01);
        assertEquals(1050., container.getLimits().getTemporaryLimit(300).getValue(), 0.01);
        assertEquals(1000., container.getOriginalPermanentLimit(), 0.01);
        assertEquals(1400., container.getOriginalTemporaryLimit(300), 0.01);
    }
}
