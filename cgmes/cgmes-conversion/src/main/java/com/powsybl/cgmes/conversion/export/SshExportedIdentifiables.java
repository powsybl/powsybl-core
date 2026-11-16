/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.events.*;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Build the identifiables of a partial SSH export from the events recorded on a network,
 * for example with a {@link com.powsybl.iidm.network.NetworkEventRecorder}.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class SshExportedIdentifiables {

    private SshExportedIdentifiables() {
    }

    /**
     * The identifiables changed by the events, each one only once, in the order of their first event.
     * Only the changes of the working variant of the network are considered.
     * The events of identifiables that are not in the network any more are ignored.
     */
    public static Set<Identifiable<?>> fromEvents(Network network, Collection<NetworkEvent> events) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(events);
        String workingVariantId = network.getVariantManager().getWorkingVariantId();
        Set<Identifiable<?>> identifiables = new LinkedHashSet<>();
        for (NetworkEvent event : events) {
            String id = changedIdentifiableId(event, workingVariantId);
            if (id != null) {
                Identifiable<?> identifiable = network.getIdentifiable(id);
                if (identifiable != null) {
                    identifiables.add(identifiable);
                }
            }
        }
        return identifiables;
    }

    private static String changedIdentifiableId(NetworkEvent event, String workingVariantId) {
        return switch (event) {
            case UpdateNetworkEvent e -> isOfVariant(e.variantId(), workingVariantId) ? e.id() : null;
            case ExtensionUpdateNetworkEvent e -> isOfVariant(e.variantId(), workingVariantId) ? e.id() : null;
            case ExtensionCreationNetworkEvent e -> e.id();
            case ExtensionRemovalNetworkEvent e -> e.id();
            case PropertiesUpdateNetworkEvent e -> e.id();
            default -> null;
        };
    }

    /**
     * An attribute that does not depend on the variant has no variant id: its change applies to all the variants.
     */
    private static boolean isOfVariant(String eventVariantId, String workingVariantId) {
        return eventVariantId == null || eventVariantId.equals(workingVariantId);
    }
}
