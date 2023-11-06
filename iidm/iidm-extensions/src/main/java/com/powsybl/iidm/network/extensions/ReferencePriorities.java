/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;

import java.util.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface ReferencePriorities<C extends Connectable<C>> extends Extension<C> {

    String NAME = "referencePriorities";
    ImmutableMap<IdentifiableType, Integer> DEFAULT_CONNECTABLE_TYPE_PRIORITIES = Maps.immutableEnumMap(
            ImmutableMap.<IdentifiableType, Integer>builder()
                    .put(IdentifiableType.GENERATOR, 1)
                    .put(IdentifiableType.LOAD, 2)
                    .put(IdentifiableType.BUSBAR_SECTION, 3)
                    .build()
    );

    Comparator<ReferencePriority> DEFAULT_CONNECTABLE_TYPE_COMPARATOR =
            Comparator.comparing(rt -> DEFAULT_CONNECTABLE_TYPE_PRIORITIES.getOrDefault(rt.getTerminal().getConnectable().getType(), Integer.MAX_VALUE));

    static List<ReferencePriority> get(Network network, Comparator<ReferencePriority> connectableComparator) {
        return network.getConnectableStream().filter(c -> c.getExtension(ReferencePriorities.class) != null)
                .map(c -> (ReferencePriorities) (c.getExtension(ReferencePriorities.class)))
                .flatMap(rt -> rt.getReferencePriorities().stream())
                .filter(rt -> ((ReferencePriority) rt).getPriority() > 0)
                .sorted(Comparator.comparingInt(ReferencePriority::getPriority).thenComparing(connectableComparator))
                .toList();
    }

    static List<ReferencePriority> get(Network network) {
        return get(network, DEFAULT_CONNECTABLE_TYPE_COMPARATOR);
    }

    static void delete(Network network) {
        network.getConnectableStream().forEach(c -> c.removeExtension(ReferencePriorities.class));
    }

    @Override
    default String getName() {
        return NAME;
    }

    ReferencePriorityAdder newReferencePriority();

    List<ReferencePriority> getReferencePriorities();

}
