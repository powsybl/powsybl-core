/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class BalanceTypeGuesserTest {

    @Test
    void test() {
        Terminal genTerminal1 = Mockito.mock(Terminal.class);
        Mockito.when(genTerminal1.getP()).thenReturn(-126.083);
        Generator generator1 = Mockito.mock(Generator.class);
        Mockito.when(generator1.getId()).thenReturn("gen1");
        Mockito.when(generator1.getTerminal()).thenReturn(genTerminal1);
        Mockito.when(generator1.getTargetP()).thenReturn(126.0);
        Mockito.when(generator1.getMaxP()).thenReturn(500.0);
        Mockito.when(generator1.getMinP()).thenReturn(0.0);

        Terminal genTerminal2 = Mockito.mock(Terminal.class);
        Mockito.when(genTerminal2.getP()).thenReturn(-129.085);
        Generator generator2 = Mockito.mock(Generator.class);
        Mockito.when(generator2.getId()).thenReturn("gen2");
        Mockito.when(generator2.getTerminal()).thenReturn(genTerminal2);
        Mockito.when(generator2.getTargetP()).thenReturn(129.0);
        Mockito.when(generator2.getMaxP()).thenReturn(500.0);
        Mockito.when(generator2.getMinP()).thenReturn(0.0);

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getGeneratorStream()).thenAnswer(dummy -> Stream.of(generator1, generator2));

        BalanceTypeGuesser guesser = new BalanceTypeGuesser(network, 0.1);
        assertEquals(BalanceType.NONE, guesser.getBalanceType());
        assertEquals("gen2", guesser.getSlack());

        Terminal genTerminal3 = Mockito.mock(Terminal.class);
        Mockito.when(genTerminal3.getP()).thenReturn(-155.236);
        Generator generator3 = Mockito.mock(Generator.class);
        Mockito.when(generator3.getId()).thenReturn("gen3");
        Mockito.when(generator3.getTerminal()).thenReturn(genTerminal3);
        Mockito.when(generator3.getTargetP()).thenReturn(195.107);
        Mockito.when(generator3.getMaxP()).thenReturn(227.5);
        Mockito.when(generator3.getMinP()).thenReturn(-227.5);

        Mockito.when(network.getGeneratorStream()).thenAnswer(dummy -> Stream.of(generator1, generator2, generator3));

        guesser = new BalanceTypeGuesser(network, 0.1);
        assertEquals(BalanceType.PROPORTIONAL_TO_GENERATION_P, guesser.getBalanceType());
        assertNull(guesser.getSlack());
    }

}
