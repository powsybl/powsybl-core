/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.computation.Partition;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
class SubContingenciesProviderTest {

    @Test
    void test() {
        ContingenciesProvider provider = n -> IntStream.range(1, 5)
            .mapToObj(i -> new Contingency("contingency-" + i))
            .collect(Collectors.toList());

        Network network = Mockito.mock(Network.class);

        List<String> subList1 = new SubContingenciesProvider(provider, new Partition(1, 2))
            .getContingencies(network)
            .stream().map(Contingency::getId).collect(Collectors.toList());

        List<String> subList2 = new SubContingenciesProvider(provider, new Partition(2, 2))
            .getContingencies(network)
            .stream().map(Contingency::getId).collect(Collectors.toList());

        assertEquals(List.of("contingency-1", "contingency-2"), subList1);
        assertEquals(List.of("contingency-3", "contingency-4"), subList2);
    }

    @Test
    void testWithContextObjects() {
        ContingenciesProvider provider = n -> IntStream.range(1, 5)
            .mapToObj(i -> new Contingency("contingency-" + i))
            .collect(Collectors.toList());

        Network network = Mockito.mock(Network.class);
        Map<Class<?>, Object> contextObjects = Map.of(Generator.class, Mockito.mock(Generator.class));

        List<String> subList1 = new SubContingenciesProvider(provider, new Partition(1, 2))
            .getContingencies(network)
            .stream().map(Contingency::getId).collect(Collectors.toList());

        List<String> subList2 = new SubContingenciesProvider(provider, new Partition(2, 2))
            .getContingencies(network)
            .stream().map(Contingency::getId).collect(Collectors.toList());

        List<String> subList1ContextObjects = new SubContingenciesProvider(provider, new Partition(1, 2))
            .getContingencies(network, contextObjects)
            .stream().map(Contingency::getId).collect(Collectors.toList());

        List<String> subList2ContextObjects = new SubContingenciesProvider(provider, new Partition(2, 2))
            .getContingencies(network, contextObjects)
            .stream().map(Contingency::getId).collect(Collectors.toList());

        assertEquals(subList1, subList1ContextObjects);
        assertEquals(subList2, subList2ContextObjects);
    }

    @Test
    void testEmpty() {
        ContingenciesProvider provider = n -> Collections.emptyList();

        Network network = Mockito.mock(Network.class);

        List<String> subList1 = new SubContingenciesProvider(provider, new Partition(1, 1))
            .getContingencies(network)
            .stream().map(Contingency::getId).collect(Collectors.toList());

        assertEquals(Collections.emptyList(), subList1);
    }

}
