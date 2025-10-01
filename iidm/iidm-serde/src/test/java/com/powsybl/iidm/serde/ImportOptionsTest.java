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

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ImportOptionsTest {

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
    void importOptionsTestExcludedExtensions() {

        ImportOptions options = new ImportOptions(Boolean.FALSE);
        options.setIncludedExtensions(Sets.newHashSet("loadFoo", "loadTest"));
        options.setExcludedExtensions(Sets.newHashSet("loadBar"));

        // here a warning should be logged : Previously included extensions [loadFoo, loadTest] will be ignored
        assertEquals(-1, (int) options.getIncludedExtensions().map(Set::size).orElse(-1));
        assertEquals(1, (int) options.getExcludedExtensions().map(Set::size).orElse(-1));

        ImportOptions options0 = new ImportOptions(Boolean.FALSE);
        options0.setExcludedExtensions(Sets.newHashSet("loadFoo", "loadTest"));
        options0.setIncludedExtensions(Collections.emptySet());

        // here a warning is displayed: All Extensions will be disabled
        assertEquals(0, (int) options0.getIncludedExtensions().map(Set::size).orElse(-1));
        assertEquals(-1, (int) options0.getExcludedExtensions().map(Set::size).orElse(-1));

        ImportOptions options2 = new ImportOptions(Boolean.FALSE);
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

        ImportOptions options3 = new ImportOptions(Boolean.FALSE);
        options3.addIncludedExtension("loadFoo");
        options3.addIncludedExtension("loadTest");
        options3.addExcludedExtension("loadBar");
        options3.addIncludedExtension("loadBar");
        // here a warning is displayed:  Extension loadBar is not already explicitly included [loadFoo, loadTest]

        assertEquals(3, (int) options3.getIncludedExtensions().map(Set::size).orElse(-1));
        assertEquals(0, (int) options3.getExcludedExtensions().map(Set::size).orElse(-1));

        ImportOptions options4 = new ImportOptions(Boolean.FALSE);
        options4.addExcludedExtension("loadBar");
        options4.addIncludedExtension("loadFoo");
        options4.addIncludedExtension("loadTest");
        options4.addIncludedExtension("loadBar");
        // here a warning  Extension loadBar is not already explicitly included [loadFoo, loadTest]

        assertEquals(3, (int) options4.getIncludedExtensions().map(Set::size).orElse(-1));
        assertEquals(0, (int) options4.getExcludedExtensions().map(Set::size).orElse(-1));

        ImportOptions options5 = new ImportOptions(Boolean.FALSE);
        options5.addIncludedExtension("loadBar");
        options5.addIncludedExtension("loadBar");
        options5.addExcludedExtension("loadTest");
        options5.addExcludedExtension("loadTest");
        // here the second call is a no op

        assertEquals(1, (int) options5.getIncludedExtensions().map(Set::size).orElse(-1));
        assertEquals(1, (int) options5.getExcludedExtensions().map(Set::size).orElse(-1));
    }

    @Test
    void importOptionsTestExcludedExtensions3() {
        ImportOptions options = new ImportOptions(Boolean.FALSE);
        options.addExcludedExtension("loadBar");
        options.addIncludedExtension("loadBar");
        assertEquals(0, (int) options.getExcludedExtensions().map(Set::size).orElse(-1));
        assertEquals(1, (int) options.getIncludedExtensions().map(Set::size).orElse(-1));
        ImportOptions options1 = new ImportOptions(Boolean.FALSE);
        options1.addIncludedExtension("loadBar");
        options1.addExcludedExtension("loadBar");
        assertEquals(1, (int) options1.getExcludedExtensions().map(Set::size).orElse(-1));
        assertEquals(0, (int) options1.getIncludedExtensions().map(Set::size).orElse(-1));

        ImportOptions options2 = new ImportOptions(Boolean.FALSE);
        options2.setIncludedExtensions(Sets.newHashSet("loadBar"));
        options2.addExcludedExtension("loadBar");
        assertEquals(1, (int) options2.getExcludedExtensions().map(Set::size).orElse(-1));
        assertEquals(0, (int) options2.getIncludedExtensions().map(Set::size).orElse(-1));

        ImportOptions options3 = new ImportOptions(Boolean.FALSE);
        options3.setExcludedExtensions(Sets.newHashSet("loadBar"));
        options3.addIncludedExtension("loadBar");
        assertEquals(0, (int) options3.getExcludedExtensions().map(Set::size).orElse(-1));
        assertEquals(1, (int) options3.getIncludedExtensions().map(Set::size).orElse(-1));

        // test no extensions at all
        ImportOptions options4 = new ImportOptions(Boolean.FALSE);
        options4.setIncludedExtensions(Collections.emptySet());
        options4.addExcludedExtension("loadBar");
        assertEquals(1, (int) options4.getExcludedExtensions().map(Set::size).orElse(-1));
        assertEquals(0, (int) options4.getIncludedExtensions().map(Set::size).orElse(-1));

        ImportOptions options5 = new ImportOptions(Boolean.FALSE);
        options5.setIncludedExtensions(Sets.newHashSet("loadBar", "loadFoo"));
        options5.setExcludedExtensions(Collections.emptySet());
        assertEquals(0, (int) options5.getExcludedExtensions().map(Set::size).orElse(-1));
        assertEquals(-1, (int) options5.getIncludedExtensions().map(Set::size).orElse(-1));
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
}
