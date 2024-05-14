/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.common.collect.Sets;
import com.powsybl.iidm.network.TopologyLevel;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static com.powsybl.iidm.serde.ExportOptions.IidmVersionIncompatibilityBehavior.THROW_EXCEPTION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ExportOptionsTest {

    @Test
    void exportOptionsTest() {
        ExportOptions options = new ExportOptions();

        Set<String> extensionsList = Sets.newHashSet("loadFoo", "loadBar");
        options.setExtensions(extensionsList);
        assertEquals(Boolean.FALSE, options.withNoExtension());
        assertTrue(options.withExtension("loadFoo"));

        assertFalse(options.withAllExtensions());
        assertEquals(Boolean.FALSE, options.isAnonymized());
        assertEquals(Boolean.TRUE, options.isIndent());
        assertEquals(Boolean.TRUE, options.isWithBranchSV());

        options.addExtension("loadTest");
        assertEquals(3, (int) options.getExtensions().map(Set::size).orElse(-1));
    }

    @Test
    void exportOptionsTest2() {
        Set<String> extensionsList = new HashSet<>();
        ExportOptions options = new ExportOptions();
        options.setCharset(StandardCharsets.ISO_8859_1);
        options.setExtensions(extensionsList);
        options.setWithAutomationSystems(false);
        assertEquals(0, (int) options.getExtensions().map(Set::size).orElse(-1));
        assertEquals(StandardCharsets.ISO_8859_1, options.getCharset());
        assertFalse(options.isWithAutomationSystems());
    }

    @Test
    void exportOptionsTest3() {
        Set<String> extensionsList = Sets.newHashSet("loadFoo");
        ExportOptions options = new ExportOptions(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, TopologyLevel.BUS_BREAKER, Boolean.FALSE, Boolean.TRUE);
        options.setExtensions(extensionsList);
        assertEquals(Boolean.TRUE, options.isWithBranchSV());
        assertEquals(Boolean.TRUE, options.isIndent());
        assertEquals(Boolean.FALSE, options.isOnlyMainCc());
        assertEquals(TopologyLevel.BUS_BREAKER, options.getTopologyLevel());
        assertEquals(Boolean.FALSE, options.isThrowExceptionIfExtensionNotFound());
        assertEquals(1, (int) options.getExtensions().map(Set::size).orElse(-1));
        assertEquals(Boolean.TRUE, options.isSorted());
    }

    @Test
    void defaultExportOptionsTest() {
        testDefaultExportOptions(new ExportOptions());
        testDefaultExportOptions(new ExportOptions(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, TopologyLevel.NODE_BREAKER, Boolean.FALSE));
        testDefaultExportOptions(new ExportOptions(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, TopologyLevel.NODE_BREAKER, Boolean.FALSE, Boolean.FALSE));
        testDefaultExportOptions(new ExportOptions(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, TopologyLevel.NODE_BREAKER, Boolean.FALSE, null));
        testDefaultExportOptions(new ExportOptions(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, TopologyLevel.NODE_BREAKER, Boolean.FALSE, Boolean.FALSE, null));
        testDefaultExportOptions(new ExportOptions(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, TopologyLevel.NODE_BREAKER, Boolean.FALSE, Boolean.FALSE, null, THROW_EXCEPTION));
    }

    private void testDefaultExportOptions(ExportOptions options) {
        assertEquals(-1, (int) options.getExtensions().map(Set::size).orElse(-1));
        assertEquals(Boolean.TRUE, options.isWithBranchSV());
        assertEquals(Boolean.TRUE, options.isIndent());
        assertEquals(Boolean.FALSE, options.isOnlyMainCc());
        assertEquals(TopologyLevel.NODE_BREAKER, options.getTopologyLevel());
        assertEquals(Boolean.FALSE, options.isThrowExceptionIfExtensionNotFound());
        assertEquals(Boolean.FALSE, options.isSorted());
        assertEquals(IidmSerDeConstants.CURRENT_IIDM_VERSION, options.getVersion());
        assertEquals(THROW_EXCEPTION, options.getIidmVersionIncompatibilityBehavior());
        assertEquals(StandardCharsets.UTF_8, options.getCharset());
        assertEquals(Boolean.TRUE, options.isWithAutomationSystems());
    }
}
