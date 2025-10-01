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
        options.setIncludedExtensions(extensionsList);
        assertEquals(Boolean.FALSE, options.withNoExtension());
        assertTrue(options.withExtension("loadFoo"));

        assertFalse(options.withAllExtensions());
        assertEquals(Boolean.FALSE, options.isAnonymized());
        assertEquals(Boolean.TRUE, options.isIndent());
        assertEquals(Boolean.TRUE, options.isWithBranchSV());

        options.addIncludedExtension("loadTest");
        assertEquals(3, (int) options.getIncludedExtensions().map(Set::size).orElse(-1));
    }

    @Test
    void exportOptionsTest2() {
        Set<String> extensionsList = new HashSet<>();
        ExportOptions options = new ExportOptions();
        options.setCharset(StandardCharsets.ISO_8859_1);
        options.setIncludedExtensions(extensionsList);
        options.setWithAutomationSystems(false);
        assertEquals(0, (int) options.getIncludedExtensions().map(Set::size).orElse(-1));
        assertEquals(StandardCharsets.ISO_8859_1, options.getCharset());
        assertFalse(options.isWithAutomationSystems());
    }

    @Test
    void exportOptionsTest3() {
        Set<String> extensionsList = Sets.newHashSet("loadFoo");
        ExportOptions options = new ExportOptions(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, TopologyLevel.BUS_BREAKER, Boolean.FALSE, Boolean.TRUE);
        options.setIncludedExtensions(extensionsList);
        assertEquals(Boolean.TRUE, options.isWithBranchSV());
        assertEquals(Boolean.TRUE, options.isIndent());
        assertEquals(Boolean.FALSE, options.isOnlyMainCc());
        assertEquals(TopologyLevel.BUS_BREAKER, options.getTopologyLevel());
        assertEquals(Boolean.FALSE, options.isThrowExceptionIfExtensionNotFound());
        assertEquals(1, (int) options.getIncludedExtensions().map(Set::size).orElse(-1));
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
        assertEquals(-1, (int) options.getIncludedExtensions().map(Set::size).orElse(-1));
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

    @Test
    void exportOptionsTestFilteredExtensions() {

        ExportOptions options = new ExportOptions();
        options.setIncludedExtensions(Sets.newHashSet("loadFoo", "loadTest"));
        options.setExcludedExtensions(Sets.newHashSet("loadBar"));

        // here a warning should be logged : Previously included extensions [loadFoo, loadTest] will be ignored
        assertEquals(-1, (int) options.getIncludedExtensions().map(Set::size).orElse(-1));
        assertEquals(1, (int) options.getExcludedExtensions().map(Set::size).orElse(-1));

        ExportOptions options2 = new ExportOptions();
        options2.addIncludedExtension("loadFoo");
        options2.addIncludedExtension("loadBar");
        options2.addExcludedExtension("loadBar");
        // here only loadFoo should be imported

        assertEquals(1, (int) options2.getIncludedExtensions().map(Set::size).orElse(-1));
        assertTrue(options2.getIncludedExtensions()
                .filter(set -> set.size() == 1)
                .filter(set -> set.contains("loadFoo"))
                .isPresent());
        assertEquals(1, (int) options2.getExcludedExtensions().map(Set::size).orElse(-1));

        ExportOptions options3 = new ExportOptions();
        options3.addIncludedExtension("loadFoo");
        options3.addIncludedExtension("loadTest");
        options3.addExcludedExtension("loadBar");
        options3.addIncludedExtension("loadBar");
        // here a warning  Extension loadBar is not already explicitly included [loadFoo, loadTest]

        assertEquals(3, (int) options3.getIncludedExtensions().map(Set::size).orElse(-1));
        assertEquals(0, (int) options3.getExcludedExtensions().map(Set::size).orElse(-1));

        ExportOptions options4 = new ExportOptions();
        options4.addExcludedExtension("loadBar");
        options4.addIncludedExtension("loadFoo");
        options4.addIncludedExtension("loadTest");
        options4.addIncludedExtension("loadBar");
        // here a warning  Extension loadBar is not already explicitly included [loadFoo, loadTest]

        assertEquals(3, (int) options4.getIncludedExtensions().map(Set::size).orElse(-1));
        assertEquals(0, (int) options4.getExcludedExtensions().map(Set::size).orElse(-1));

        ExportOptions options5 = new ExportOptions();
        options5.addIncludedExtension("loadBar");
        options5.addIncludedExtension("loadBar");
        options5.addExcludedExtension("loadTest");
        options5.addExcludedExtension("loadTest");
        // here the second call is a no op

        assertEquals(1, (int) options5.getIncludedExtensions().map(Set::size).orElse(-1));
        assertEquals(1, (int) options5.getExcludedExtensions().map(Set::size).orElse(-1));

    }

}
