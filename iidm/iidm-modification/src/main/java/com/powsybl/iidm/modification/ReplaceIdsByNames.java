/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Replaces the id of every named identifiable by its name, keeping the original id
 * reachable as an alias of type {@link #ALIAS_TYPE_ORIGINAL_ID}.
 * <p>
 * An identifiable id is changed only when its name can be used as a unique identifier in the network.
 * Conditions that are checked to skip changes:
 * <ul>
 *   <li>The name must not be empty.</li>
 *   <li>The name and id of the identifiable must be different.</li>
 *   <li>The name must be unique for all identifiables considered.
 *       Otherwise, the first one processed would take the name and the others would be
 *       left out, making the result dependent on the iteration order.</li>
 *   <li>The name must not already be in use as an id or as an alias of another identifiable.
 *       {@code Network.getIdentifiable} resolves aliases, so a single lookup covers both.
 *       This condition fires for example when fictitious switches created for disconnected CGMES terminals.
 *       Fictitious switches are named after their terminal , and that identifier is already registered as an
 *       alias of the equipment owning the terminal.</li>
 *   <li>The alias {@link #ALIAS_TYPE_ORIGINAL_ID} must be available.</li>
 * </ul>
 * <p>
 * Identifiables that cannot be changed keep their original id and are returned.
 * <p>
 * Notes on the implementation:
 * <ul>
 *   <li>The identifiables are collected into a list first: {@code setId} mutates the
 *       network index, and {@code getIdentifiables()} is a live view of it.</li>
 *   <li>{@code Network} instances (including subnetworks) are excluded. Changing their ids is out of scope.</li>
 *   <li>{@code setId} must be called before {@code addAlias}: if the object still has
 *       the original id, {@code NetworkIndex.addAlias} would silently ignore the request.</li>
 * </ul>
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class ReplaceIdsByNames extends AbstractNetworkModification {

    public static final String ALIAS_TYPE_ORIGINAL_ID = "original-id";

    public record IdentifierChangeSkipped(String id, String name, IdentifiableType type, IdentifierChangeSkippedReason reason) {

        public enum IdentifierChangeSkippedReason {
            NETWORK,
            NO_NAME,
            NAME_EQUALS_ID,
            DUPLICATED_NAME,
            UNIQUE_NAME_WAS_ID,
            ORIGINAL_ID_ALIAS_ALREADY_PRESENT
        }

        private static IdentifierChangeSkippedReason reason(Identifiable<?> i, String name, Map<String, Long> nameCount, Network network) {
            if (i instanceof Network) {
                return IdentifierChangeSkippedReason.NETWORK;
            }
            if (i.getAliasFromType(ALIAS_TYPE_ORIGINAL_ID).isPresent()) {
                return IdentifierChangeSkippedReason.ORIGINAL_ID_ALIAS_ALREADY_PRESENT;
            }
            if (name == null) {
                return IdentifierChangeSkippedReason.NO_NAME;
            }
            if (name.equals(i.getId())) {
                return IdentifierChangeSkippedReason.NAME_EQUALS_ID;
            }
            if (nameCount.getOrDefault(name, 0L) != 1) {
                return IdentifierChangeSkippedReason.DUPLICATED_NAME;
            }
            if (network.getIdentifiable(name) != null) {
                return IdentifierChangeSkippedReason.UNIQUE_NAME_WAS_ID;
            }
            return null;
        }
    }

    private final List<IdentifierChangeSkipped> skipped = new ArrayList<>();

    @Override
    public String getName() {
        return "ReplaceIdsByNames";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        Map<String, Long> nameCount = network.getIdentifiables().stream()
                .filter(i -> !(i instanceof Network))
                .map(i -> i.getOptionalName().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));
        skipped.clear();
        for (Identifiable<?> i : network.getIdentifiables().stream().toList()) {
            String name = i.getOptionalName().orElse(null);
            IdentifierChangeSkipped.IdentifierChangeSkippedReason reason;
            reason = IdentifierChangeSkipped.reason(i, name, nameCount, network);
            if (reason != null) {
                skipped.add(new IdentifierChangeSkipped(i.getId(), name, i.getType(), reason));
                continue;
            }
            String originalId = i.getId();
            i.setId(name);
            i.addAlias(originalId, ALIAS_TYPE_ORIGINAL_ID);
        }
    }

    public List<IdentifierChangeSkipped> getSkipped() {
        return skipped;
    }
}
