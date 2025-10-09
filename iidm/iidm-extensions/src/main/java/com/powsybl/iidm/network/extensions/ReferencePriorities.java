/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;

import java.util.*;

/**
 * ReferencePriorities iIDM extension allows specifying priorities among network equipments terminals for the selection
 * of a reference bus, for example the angle reference in the context of a load flow.<br/>
 * The extension is attached to a {@link Connectable}, i.e. any equipment with Terminals.<br/>
 * A priority level can be defined, as an integer, for each Terminal of the connectable.<br/>
 * A priority 0 means should not be used.
 * 1 is highest priority for selection.
 * 2 is second highest priority, etc ...
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface ReferencePriorities<C extends Connectable<C>> extends Extension<C> {

    String NAME = "referencePriorities";
    ImmutableMap<IdentifiableType, Integer> DEFAULT_CONNECTABLE_TYPE_PRIORITIES = Maps.immutableEnumMap(
            ImmutableMap.<IdentifiableType, Integer>builder()
                    .put(IdentifiableType.GENERATOR, 1)
                    .put(IdentifiableType.BUSBAR_SECTION, 2)
                    .put(IdentifiableType.LOAD, 3)
                    .build()
    );

    Comparator<ReferencePriority> DEFAULT_CONNECTABLE_TYPE_COMPARATOR =
            Comparator.comparing(rt -> DEFAULT_CONNECTABLE_TYPE_PRIORITIES.getOrDefault(rt.getTerminal().getConnectable().getType(), Integer.MAX_VALUE));

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Gets the reference priorities defined in the network,
     * sorted by decreasing priority (i.e. higher priorities are first in the list).
     * Priorities 0 are filtered out.
     * The connectableComparator is used to control the sorting of equal priority values.
     * Note that this method provides "raw" priorities, API users would typically want to
     * check whether the terminals are connected, in which SynchronousComponent the Terminal belongs to, etc ...
     * @param network network from which reference priorities should be listed
     * @param connectableComparator comparator to
     */
    static List<ReferencePriority> get(Network network, Comparator<ReferencePriority> connectableComparator) {
        return network.getConnectableStream().filter(c -> c.getExtension(ReferencePriorities.class) != null)
                .map(c -> (ReferencePriorities) (c.getExtension(ReferencePriorities.class)))
                .flatMap(rt -> rt.getReferencePriorities().stream())
                .filter(rt -> ((ReferencePriority) rt).getPriority() > 0)
                .sorted(Comparator.comparingInt(ReferencePriority::getPriority).thenComparing(connectableComparator))
                .toList();
    }

    /**
     * Gets the reference priorities defined in the network for the current variant,
     * sorted by decreasing priority (i.e. higher priorities are first in the list).
     * Priorities 0 are filtered out.
     * In case of equal priority between different equipment types, generators are put first, busbar sections  second, loads third,
     * and all other equipment types last.
     * Note that this method provides "raw" priorities, API users would typically want to
     * check whether the terminals are connected, in which SynchronousComponent the Terminal belongs to, etc ...
     * @param network network from which reference priorities should be listed
     * @return list of sorted reference priorities
     */
    static List<ReferencePriority> get(Network network) {
        return get(network, DEFAULT_CONNECTABLE_TYPE_COMPARATOR);
    }

    /**
     * Deletes all defined reference priorities in the network for the current variant
     * @param network network whose reference priorities should be deleted
     */
    static void delete(Network network) {
        network.getConnectableStream().filter(c -> c.getExtension(ReferencePriorities.class) != null)
                .forEach(c -> ((ReferencePriorities) c.getExtension(ReferencePriorities.class)).deleteReferencePriorities());
    }

    ReferencePriorityAdder newReferencePriority();

    List<ReferencePriority> getReferencePriorities();

    void deleteReferencePriorities();

}
