/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractOptionsTest<T> {

    abstract AbstractOptions<T> getOptions();

    @Test
    void testExcludeAndIncludedExtension() {
        AbstractOptions<T> options = getOptions();
        options.addIncludedExtension("loadFoo"); // only "loadFoo" is included
        options.addIncludedExtension("loadBar"); // only "loadFoo" and "loadBar" are included
        options.addExcludedExtension("loadBar"); // "loadBar" is excluded so only "loadFoo" is included

        Set<String> includedExtensions = options.getIncludedExtensions().orElse(null);
        assertNotNull(includedExtensions);
        assertEquals(1, includedExtensions.size());
        assertTrue(includedExtensions.contains("loadFoo"));
        assertTrue(options.getExcludedExtensions().isEmpty()); // The second list is null (i.e., the optional is empty)
    }

    @Test
    void testIncludeAndExcludedExtension() {
        AbstractOptions<T> options = getOptions();
        options.addExcludedExtension("loadFoo"); // only "loadFoo" is excluded
        options.addExcludedExtension("loadBar"); // only "loadFoo" and "loadBar" are excluded
        options.addIncludedExtension("loadBar"); // "loadBar" is included so only "loadFoo" is excluded

        Set<String> excludedExtensions = options.getExcludedExtensions().orElse(null);
        assertNotNull(excludedExtensions);
        assertEquals(1, excludedExtensions.size());
        assertTrue(excludedExtensions.contains("loadFoo"));
        assertTrue(options.getIncludedExtensions().isEmpty()); // The second list is null (i.e., the optional is empty)
    }

    @Test
    void testIncludeAndExcludedOtherExtension() {
        AbstractOptions<T> options = getOptions();
        options.addExcludedExtension("loadFoo"); // only "loadFoo" is excluded
        options.addExcludedExtension("loadTest"); // only "loadFoo" and "loadTest" are excluded
        options.addIncludedExtension("loadBar"); // "loadBar" is not excluded, so it is already included
        options.addExcludedExtension("loadBar"); // only "loadFoo", "loadTest" and "loadBar" are excluded

        Set<String> excludedExtensions = options.getExcludedExtensions().orElse(null);
        assertNotNull(excludedExtensions);
        assertEquals(3, excludedExtensions.size());
        Set.of("loadFoo", "loadTest", "loadBar").forEach(ext -> assertTrue(excludedExtensions.contains(ext)));
        assertTrue(options.getIncludedExtensions().isEmpty()); // The second list is null (i.e., the optional is empty)
    }

    @Test
    void testExcludeAndIncludedOtherExtension() {
        AbstractOptions<T> options = getOptions();
        options.addIncludedExtension("loadFoo"); // only "loadFoo" is included
        options.addIncludedExtension("loadTest"); // only "loadFoo" and "loadTest" are included
        options.addExcludedExtension("loadBar"); // "loadBar" is not included, so it is already excluded
        options.addIncludedExtension("loadBar"); // only "loadFoo", "loadTest" and "loadBar" are included

        Set<String> includedExtensions = options.getIncludedExtensions().orElse(null);
        assertNotNull(includedExtensions);
        assertEquals(3, includedExtensions.size());
        Set.of("loadFoo", "loadTest", "loadBar").forEach(ext -> assertTrue(includedExtensions.contains(ext)));
        assertTrue(options.getExcludedExtensions().isEmpty()); // The second list is null (i.e., the optional is empty)
    }

    @Test
    void testAddExcludedExtensionThenAddManyIncludedExtensions() {
        AbstractOptions<T> options = getOptions();
        options.addExcludedExtension("loadFoo"); // only "loadFoo" is excluded
        options.addIncludedExtension("loadTest"); // "loadTest" is not excluded, so it is already included
        options.addIncludedExtension("loadFoo"); // "loadFoo" is now removed from the exclusion list, so it is included
        options.addIncludedExtension("loadBar"); // "loadBar" is not excluded, so it is already included

        Set<String> excludedExtensions = options.getExcludedExtensions().orElse(null);
        assertNotNull(excludedExtensions);
        assertEquals(0, excludedExtensions.size());
        assertTrue(options.getIncludedExtensions().isEmpty()); // The second list is null (i.e., the optional is empty)
    }

    @Test
    void testAddIncludedExtensionThenAddManyExcludedExtensions() {
        AbstractOptions<T> options = getOptions();
        options.addIncludedExtension("loadFoo"); // only "loadFoo" is included
        options.addExcludedExtension("loadTest"); // "loadTest" is not included, so it is already excluded
        options.addExcludedExtension("loadFoo"); // "loadFoo" is now removed from the inclusion list, so it is excluded
        options.addExcludedExtension("loadBar"); // "loadBar" is not included, so it is already excluded

        Set<String> includedExtensions = options.getIncludedExtensions().orElse(null);
        assertNotNull(includedExtensions);
        assertEquals(0, includedExtensions.size());
        assertTrue(options.getExcludedExtensions().isEmpty()); // The second list is null (i.e., the optional is empty)
    }

    @Test
    void testIncludeExtensionsAndSetExcludedExtensions() {
        AbstractOptions<T> options = getOptions();
        options.addIncludedExtension("loadFoo"); // only "loadFoo" is included
        options.setExcludedExtensions(Set.of("loadFoo", "loadBar")); // only "loadFoo" and "loadBar" are excluded

        Set<String> excludedExtensions = options.getExcludedExtensions().orElse(null);
        assertNotNull(excludedExtensions);
        assertEquals(2, excludedExtensions.size());
        assertTrue(excludedExtensions.contains("loadFoo"));
        assertTrue(excludedExtensions.contains("loadBar"));
        assertTrue(options.getIncludedExtensions().isEmpty()); // The second list is null (i.e., the optional is empty)
    }

    @Test
    void testExcludeExtensionsAndSetIncludedExtensions() {
        AbstractOptions<T> options = getOptions();
        options.addExcludedExtension("loadFoo"); // only "loadFoo" is excluded
        options.setIncludedExtensions(Set.of("loadFoo", "loadBar")); // only "loadFoo" and "loadBar" are included

        Set<String> includedExtensions = options.getIncludedExtensions().orElse(null);
        assertNotNull(includedExtensions);
        assertEquals(2, includedExtensions.size());
        assertTrue(includedExtensions.contains("loadFoo"));
        assertTrue(includedExtensions.contains("loadBar"));
        assertTrue(options.getExcludedExtensions().isEmpty()); // The second list is null (i.e., the optional is empty)
    }

    @Test
    void testSetExcludedThenSetEmptyIncluded() {
        AbstractOptions<T> options = getOptions();
        options.setExcludedExtensions(Set.of("loadFoo", "loadTest")); // only "loadFoo" and "loadBar" are excluded
        options.setIncludedExtensions(Collections.emptySet()); // The included extensions list is empty: all extensions are excluded

        Set<String> includedExtensions = options.getIncludedExtensions().orElse(null);
        assertNotNull(includedExtensions);
        assertEquals(0, includedExtensions.size());
        assertTrue(options.getExcludedExtensions().isEmpty()); // The second list is null (i.e., the optional is empty)
    }

    @Test
    void testSetIncludedThenSetExcluded() {
        AbstractOptions<T> options = getOptions();
        options.setIncludedExtensions(Set.of("loadFoo", "loadTest")); // only "loadFoo" and "loadBar" are included
        options.setExcludedExtensions(Set.of("loadBar")); // only "loadBar" is excluded

        Set<String> excludedExtensions = options.getExcludedExtensions().orElse(null);
        assertNotNull(excludedExtensions);
        assertEquals(1, excludedExtensions.size());
        assertTrue(excludedExtensions.contains("loadBar"));
        assertTrue(options.getIncludedExtensions().isEmpty()); // The second list is null (i.e., the optional is empty)
    }

    @Test
    void testWithNoExtension() {
        AbstractOptions<T> options = getOptions();

        // Both lists are null
        assertFalse(options.withNoExtension());

        // Define excluded extensions
        options.setExcludedExtensions(Set.of("loadBar"));
        assertFalse(options.withNoExtension()); // We cannot say if there will be no extension or not, so false is returned

        // Define the list of included extensions as empty
        options.setIncludedExtensions(Set.of());
        assertTrue(options.withNoExtension());

        // Define a list of included extensions
        options.setIncludedExtensions(Set.of("loadFoo"));
        assertFalse(options.withNoExtension());
    }

    @Test
    void testWithAllExtensions() {
        AbstractOptions<T> options = getOptions();

        // Both lists are null
        assertTrue(options.withAllExtensions());

        // Define the list of excluded extensions as empty
        options.setExcludedExtensions(Set.of());
        assertTrue(options.withAllExtensions());

        // Define excluded extensions
        options.setExcludedExtensions(Set.of("loadBar"));
        assertFalse(options.withAllExtensions());

        // Define the list of included extensions as empty
        options.setIncludedExtensions(Set.of());
        assertFalse(options.withAllExtensions());

        // Define a list of included extensions
        options.setIncludedExtensions(Set.of("loadFoo"));
        assertFalse(options.withAllExtensions());

    }

    @Test
    void testHasAtLeastOneExtension() {
        AbstractOptions<T> options = getOptions();

        // Both lists are null: all extensions are included
        assertTrue(options.hasAtLeastOneExtension(Set.of("loadBar")));

        // Define the list of excluded extensions as empty: all extensions are included
        options.setExcludedExtensions(Set.of());
        assertTrue(options.hasAtLeastOneExtension(Set.of("loadBar")));

        // Define excluded extensions
        options.setExcludedExtensions(Set.of("loadBar"));
        assertFalse(options.hasAtLeastOneExtension(Set.of("loadBar")));

        // Define the list of included extensions as empty
        options.setIncludedExtensions(Set.of());
        assertFalse(options.hasAtLeastOneExtension(Set.of("loadBar")));

        // Define a list of included extensions
        options.setIncludedExtensions(Set.of("loadBar"));
        assertTrue(options.hasAtLeastOneExtension(Set.of("loadBar")));
        assertFalse(options.hasAtLeastOneExtension(Set.of("loadFoo")));
    }
}
