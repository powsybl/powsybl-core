/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ImportOptionsTest extends AbstractOptionsTest<ImportOptions> {

    ImportOptions getOptions() {
        return new ImportOptions();
    }

    @Test
    void importOptionsTest() {
        ImportOptions options = new ImportOptions();
        Set<String> extensionsList = Sets.newHashSet("loadFoo", "loadBar");
        options.setIncludedExtensions(extensionsList);
        options.setWithAutomationSystems(false);
        assertEquals(Boolean.FALSE, options.withNoExtension());
        assertEquals(Boolean.FALSE, options.isWithAutomationSystems());

        options.addIncludedExtension("loadBar");
        assertEquals(2, (int) options.getIncludedExtensions().map(Set::size).orElse(-1));
    }

    @Test
    void importOptionsTest2() {
        Set<String> extensionsList = Sets.newHashSet("loadFoo", "loadBar");
        ImportOptions options = new ImportOptions(Boolean.FALSE);
        options.setIncludedExtensions(extensionsList);
        options.setMissingPermanentLimitPercentage(95.);

        assertEquals(Boolean.FALSE, options.isThrowExceptionIfExtensionNotFound());
        assertEquals(Boolean.FALSE, options.withNoExtension());
        assertEquals(2, (int) options.getIncludedExtensions().map(Set::size).orElse(-1));
        assertEquals(95., options.getMissingPermanentLimitPercentage());
    }

    @Test
    void importOptionsDefaultValues() {
        ImportOptions options = new ImportOptions(Boolean.FALSE);

        assertEquals(Boolean.FALSE, options.isThrowExceptionIfExtensionNotFound());
        assertEquals(Boolean.FALSE, options.withNoExtension());
        assertEquals(-1, (int) options.getIncludedExtensions().map(Set::size).orElse(-1));
        assertEquals(Boolean.TRUE, options.withAllExtensions());
        assertEquals(Boolean.TRUE, options.isWithAutomationSystems());
        assertEquals(100., options.getMissingPermanentLimitPercentage());
    }

    @Test
    void testExcludeAndIncludedExtensionSuccessive() {
        ImportOptions options = getOptions();
        options.addIncludedExtension("loadFoo").addIncludedExtension("loadBar"); // only "loadFoo" and "loadBar" are included
        options.addExcludedExtension("loadBar").addExcludedExtension("loadTest"); // "loadBar" is excluded so only "loadFoo" is included

        Set<String> includedExtensions = options.getIncludedExtensions().orElse(null);
        assertNotNull(includedExtensions);
        assertEquals(1, includedExtensions.size());
        assertTrue(includedExtensions.contains("loadFoo"));
        assertTrue(options.getExcludedExtensions().isEmpty()); // The second list is null (i.e., the optional is empty)
    }

}
