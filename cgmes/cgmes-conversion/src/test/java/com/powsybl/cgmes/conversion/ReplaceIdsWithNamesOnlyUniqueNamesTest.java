/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.conversion.test.ConversionUtil;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class ReplaceIdsWithNamesOnlyUniqueNamesTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceIdsWithNamesOnlyUniqueNamesTest.class);

    private static final String ALIAS_TYPE_ORIGINAL_ID = "original-id";

    private record IdNamePair(String id, String name) { }

    /**
     * Replaces the id of every named identifiable by its name, keeping the original id
     * reachable as an alias of type {@link #ALIAS_TYPE_ORIGINAL_ID}.
     * <p>
     * An identifiable is renamed only when its name is free in the whole network namespace.
     * Two independent conditions are checked, because names collide in two different ways:
     * <ul>
     *   <li><em>Name against name</em>: the name must be unique for all identifiables considered.
     *       Otherwise, the first one processed would take the name and the others would be
     *       left out, making the result dependent on the iteration order of the index.</li>
     *   <li><em>Name against the index</em>: the name must not already be in use as an id
     *       or as an alias of another identifiable. {@code Network.getIdentifiable} resolves
     *       aliases, so a single lookup covers both. This is the condition that actually
     *       fires in practice: fictitious switches created for disconnected CGMES terminals
     *       are named after their terminal, and that identifier is already registered as an
     *       alias of the equipment owning the terminal.</li>
     * </ul>
     * Identifiables that cannot be renamed keep their original id and are returned, so the
     * caller can report them.
     * <p>
     * Notes on the implementation:
     * <ul>
     *   <li>The identifiables are collected into a list first: {@code setId} mutates the
     *       network index, and {@code getIdentifiables()} is a live view of it.</li>
     *   <li>{@code Network} instances (including subnetworks) are excluded: they are
     *       registered in the index too, but renaming them is out of scope.</li>
     *   <li>{@code setId} must be called before {@code addAlias}: if the object still has
     *       the original id, {@code NetworkIndex.addAlias} would silently ignore the request.</li>
     *   <li>Identifiables whose name already equals their id are skipped, as the rename
     *       would be a no-op.</li>
     * </ul>
     *
     * @param network the network whose identifiables are renamed in place
     * @return the identifiables that could not be renamed, with the name that was rejected
     */
    static Set<IdNamePair> replaceIdsWithNames(Network network) {
        Map<String, Long> nameCount = network.getIdentifiables().stream()
                .filter(i -> !(i instanceof Network))
                .map(i -> i.getOptionalName().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));
        Set<IdNamePair> skipped = new HashSet<>();
        network.getIdentifiables().stream()
                .filter(i -> !(i instanceof Network))
                .toList()
                .forEach(i -> i.getOptionalName()
                        .filter(name -> !name.equals(i.getId()))
                        .filter(name -> nameCount.get(name) == 1)
                        .ifPresent(name -> {
                            if (network.getIdentifiable(name) != null) {
                                LOGGER.warn("Cannot rename {}: name '{}' is unique but is already used as identifier", i.getId(), name);
                                skipped.add(new IdNamePair(i.getId(), name));
                                return;
                            }
                            String originalId = i.getId();
                            i.setId(name);
                            i.addAlias(originalId, ALIAS_TYPE_ORIGINAL_ID);
                        }));
        return skipped;
    }

    @Test
    void replaceIdsWithNamesKeepsOriginalIdResolvable() {
        Network network = ConversionUtil.networkModel(Cgmes3Catalog.svedala(), new Conversion.Config());

        int countBefore = network.getIdentifiables().size();

        Set<IdNamePair> skipped = replaceIdsWithNames(network);

        // Nothing must be lost or duplicated by the renaming
        assertEquals(countBefore, network.getIdentifiables().size());

        int changed = 0;
        int untouched = 0;

        for (Identifiable<?> i : network.getIdentifiables()) {
            String originalId = i.getAliasFromType(ALIAS_TYPE_ORIGINAL_ID).orElse(null);
            if (originalId == null) {
                untouched++;
                continue;
            }
            changed++;

            // The original id must still resolve to the same object, now through the alias
            assertSame(i, network.getIdentifiable(originalId));

            // And this object must have a name, and it must be equal to its id
            assertEquals(i.getOptionalName().orElseThrow(), i.getId());
        }

        // Skips are expected:
        // Fictitious switches for disconnected terminals take their name from their terminal identifier.
        // The terminal identifiers have been added as an alias of the related equipment.
        assertTrue(skipped.stream().allMatch(p -> p.id().equals(p.name() + "_SW_fict")),
                "unexpected skip, not a fictitious switch derived from a terminal: " + skipped);

        LOGGER.info("replaceIdsWithNames: changed={} skipped={} untouched={} (total={})",
                changed, skipped.size(), untouched, countBefore);

        // Check that the mechanism actually has renamed something
        assertTrue(changed > 0, "no identifiable was renamed");
        assertEquals(untouched, skipped.size() + 1, "untouched must be the network plus the skipped elements");
    }
}
