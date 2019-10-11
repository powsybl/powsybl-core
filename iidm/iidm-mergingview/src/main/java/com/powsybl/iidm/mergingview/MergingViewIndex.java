/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class MergingViewIndex {

    /** Local storage for adapters created */
    private final Map<Identifiable<?>, AbstractAdapter<?>> adaptersCacheMap = new WeakHashMap<>();

    /** Network asked to be merged */
    private final Collection<Network> mergingNetworks = new ArrayList<>();

    /** Current merging view reference */
    private final MergingView currentView;

    /** Constructor */
    MergingViewIndex(final MergingView currentView) {
        // Keep reference on current view
        this.currentView = Objects.requireNonNull(currentView);
    }

    /** @return current merging view instance */
    MergingView getView() {
        return currentView;
    }

    /** @return stream of merging network */
    Stream<Network> getMergingNetworkStream() {
        return mergingNetworks.stream();
    }

    /** Validate all networks added into merging network list */
    void checkAndAdd(final Network other) {
        // Check multi-variants network
        ValidationUtil.checkEmptyVariant(other);
        // Check unique identifiable network
        ValidationUtil.checkUniqueId(other, this);
        // Local storage for mergeable network
        mergingNetworks.add(other);
    }

    /** @return adapter according to given parameter */
    Identifiable<?> getIdentifiable(final Identifiable<?> identifiable) {
        if (identifiable instanceof Substation) {
            return getSubstation((Substation) identifiable); // container
        } else if (identifiable instanceof VoltageLevel) {
            return getVoltageLevel((VoltageLevel) identifiable); // container
        } else if (identifiable instanceof Network) {
            return currentView; // container
        } else {
            throw new PowsyblException(identifiable.getClass() + " type is not managed in MergingViewIndex.");
        }
    }

    /** @return all adapters according to all Identifiables */
    Stream<Identifiable<?>> getIdentifiableStream() {
        // Search into merging & working networks and return Adapters
        return getMergingNetworkStream()
                .map(Network::getIdentifiables)
                .filter(n -> !(n instanceof Network))
                .flatMap(Collection::stream)
                .map(this::getIdentifiable);
    }

    /** @return all adapters according to all Identifiables */
    Collection<Identifiable<?>> getIdentifiables() {
        // Search into merging & working networks and return Adapters
        return getIdentifiableStream().collect(Collectors.toSet());
    }

    /** @return all Adapters according to all Substations into merging view */
    Collection<Substation> getSubstations() {
        // Search Substations into merging & working networks and return Adapters
        return getMergingNetworkStream()
                .flatMap(Network::getSubstationStream)
                .map(this::getSubstation)
                .collect(Collectors.toSet());
    }

    /** @return adapter according to given Substation */
    SubstationAdapter getSubstation(final Substation substation) {
        return substation == null ? null : (SubstationAdapter) adaptersCacheMap.computeIfAbsent(substation, key -> new SubstationAdapter((Substation) key, this));
    }

    /** @return adapter according to given VoltageLevel */
    VoltageLevelAdapter getVoltageLevel(final VoltageLevel vl) {
        return vl == null ? null : (VoltageLevelAdapter) adaptersCacheMap.computeIfAbsent(vl, k -> new VoltageLevelAdapter((VoltageLevel) k, this));
    }
}
