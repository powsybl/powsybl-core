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
import com.powsybl.iidm.modification.ReplaceIdsByNames;
import com.powsybl.iidm.modification.ReplaceIdsByNames.IdentifierChangeSkipped;
import com.powsybl.iidm.modification.ReplaceIdsByNames.IdentifierChangeSkipped.IdentifierChangeSkippedReason;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class ReplaceIdsWithNamesTest {

    @Test
    void testSvedala() {
        Network network = ConversionUtil.networkModel(Cgmes3Catalog.svedala(), new Conversion.Config());

        ReplaceIdsByNames replaceIdsByNames = new ReplaceIdsByNames();
        replaceIdsByNames.apply(network);
        List<IdentifierChangeSkipped> skipped = replaceIdsByNames.getSkipped();

        checkChangedIdentifiables(network);

        // For Svedala test case skips are expected, in addition to the network id not being changed.
        assertTrue(skipped.size() > 1);
        // Fictitious switches for disconnected terminals take their name from their terminal identifier.
        // And the terminal identifiers have been added as aliases of the related equipment.
        // Check that all situations where we have not been able to change the id because name was already an id
        // correspond to these elements.
        assertTrue(skipped.stream()
                        .filter(p -> p.reason() == IdentifierChangeSkippedReason.UNIQUE_NAME_WAS_ID)
                        .allMatch(p -> p.id().equals(p.name() + "_SW_fict")),
                "unexpected UNIQUE_NAME_WAS_ID skip, not a fictitious switch derived from a terminal: " + skipped);
        long countChanged = network.getIdentifiables().size() - skipped.size();

        // If we try again over the already changed identifiers,
        // all identifiables must be skipped
        // There should be skipped changes with reason ORIGINAL_ID_ALIAS_ALREADY_PRESENT
        replaceIdsByNames.apply(network);
        List<IdentifierChangeSkipped> skipped1 = replaceIdsByNames.getSkipped();
        assertEquals(network.getIdentifiables().size(), skipped1.size());
        assertEquals(countChanged, skipped1.stream()
                .filter(p -> p.reason() == IdentifierChangeSkippedReason.ORIGINAL_ID_ALIAS_ALREADY_PRESENT)
                .count());
    }

    private void checkChangedIdentifiables(Network network) {
        for (Identifiable<?> i : network.getIdentifiables()) {
            // An identifiable has been changed if it has an alias for the original id
            Optional<String> originalId = i.getAliasFromType(ReplaceIdsByNames.ALIAS_TYPE_ORIGINAL_ID);
            if (originalId.isEmpty()) {
                continue;
            }

            // The original id must still resolve to the same object, now through the alias
            assertSame(i, network.getIdentifiable(originalId.get()));

            // And this object must have a name, and it must be equal to its id
            assertEquals(i.getOptionalName().orElseThrow(), i.getId());
        }
    }
}
