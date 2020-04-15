/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import com.google.common.collect.Sets;
import com.powsybl.iidm.network.TopologyLevel;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExportOptionsTest {

    @Test
    public void exportOptionsTest() {
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

    public void exportOptionsTest2() {
        Set<String> extensionsList = new HashSet<>();
        ExportOptions options = new ExportOptions();
        options.setExtensions(extensionsList);
        assertEquals(0, (int) options.getExtensions().map(Set::size).orElse(-1));

    }

    @Test
    public void exportOptionsTest3() {
        Set<String> extensionsList = Sets.newHashSet("loadFoo");

        ExportOptions options = new ExportOptions(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, TopologyLevel.BUS_BREAKER, Boolean.FALSE);
        options.setExtensions(extensionsList);
        assertEquals(Boolean.TRUE, options.isWithBranchSV());
        assertEquals(Boolean.TRUE, options.isIndent());
        assertEquals(Boolean.FALSE, options.isOnlyMainCc());
        assertEquals(TopologyLevel.BUS_BREAKER, options.getTopologyLevel());
        assertEquals(Boolean.FALSE, options.isThrowExceptionIfExtensionNotFound());
        assertEquals(1, (int) options.getExtensions().map(Set::size).orElse(-1));
    }
}
