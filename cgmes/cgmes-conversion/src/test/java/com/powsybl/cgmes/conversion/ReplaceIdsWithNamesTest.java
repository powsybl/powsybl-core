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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class ReplaceIdsWithNamesTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceIdsWithNamesTest.class);

    private static final String ALIAS_TYPE_ORIGINAL_ID = "original-id";

    /**
     * Replaces the id of every named identifiable by its name, keeping the original id
     * reachable as an alias of type {@link #ALIAS_TYPE_ORIGINAL_ID}.
     * <p>
     * Notes on the implementation:
     * <ul>
     *   <li>The identifiables are collected into a list first: {@code setId} mutates the
     *       network index, and {@code getIdentifiables()} is a live view of it.</li>
     *   <li>{@code Network} instances (including subnetworks) are excluded: they are
     *       registered in the index too, but renaming them is out of scope.</li>
     *   <li>{@code setId} must be called before {@code addAlias}: if the object still has
     *       the original id, {@code NetworkIndex.addAlias} would silently ignore the request.</li>
     *   <li>Identifiables whose name already equals their id are skipped.</li>
     * </ul>
     */
    static void replaceIdsWithNames(Network network) {
        network.getIdentifiables().stream()
                .filter(i -> !(i instanceof Network))
                .toList()
                .forEach(i -> i.getOptionalName()
                        .filter(name -> !name.equals(i.getId()))
                        .ifPresent(name -> {
                            String originalId = i.getId();
                            Identifiable<?> clash = network.getIdentifiable(name);
                            i.setId(clash == null ? name : name + "_" + originalId);
                            i.addAlias(originalId, ALIAS_TYPE_ORIGINAL_ID);
                        }));
    }

    @Test
    void replaceIdsWithNamesKeepsOriginalIdResolvable() {
        Network network = ConversionUtil.networkModel(Cgmes3Catalog.microGrid(), new Conversion.Config());

        int countBefore = network.getIdentifiables().size();

        replaceIdsWithNames(network);

        // Nothing must be lost or duplicated by the renaming
        assertEquals(countBefore, network.getIdentifiables().size());

        int clean = 0;
        int suffixed = 0;
        int untouched = 0;

        for (Identifiable<?> i : network.getIdentifiables()) {
            String originalId = i.getAliasFromType(ALIAS_TYPE_ORIGINAL_ID).orElse(null);
            if (originalId == null) {
                untouched++;
                continue;
            }

            // The original id must still resolve to the same object, now through the alias
            assertSame(i, network.getIdentifiable(originalId));

            // And the new id must be either the name, or the name disambiguated with the old id
            String name = i.getNameOrId();
            if (i.getId().equals(name)) {
                clean++;
            } else {
                assertEquals(name + "_" + originalId, i.getId());
                suffixed++;
            }
        }

        LOGGER.error("replaceIdsWithNames: clean={} suffixed={} untouched={} (total={})",
                clean, suffixed, untouched, countBefore);

        // Check that the mechanism actually has renamed something
        assertTrue(clean + suffixed > 0, "no identifiable was renamed");
    }
}
