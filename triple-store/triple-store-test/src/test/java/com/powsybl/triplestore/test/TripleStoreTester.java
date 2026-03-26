/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.triplestore.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreException;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class TripleStoreTester {

    TripleStoreTester(List<String> implementations, String baseName, String... inputResourceNames) {
        this.implementations = implementations;
        this.baseName = baseName;
        this.inputResourceNames = inputResourceNames;
        this.tripleStores = new HashMap<>(implementations.size());
        this.tripleStoreCopies = new HashMap<>(implementations.size());
    }

    void load() {
        // Load the model for every triple store implementation
        for (String impl : implementations) {
            TripleStore ts = TripleStoreFactory.create(impl);
            assertNotNull(ts);
            for (String r : inputResourceNames) {
                try (InputStream is = resourceStream(r)) {
                    ts.read(is, baseName, r);
                } catch (IOException e) {
                    throw new TripleStoreException(String.format("Reading %s %s", baseName, r), e);
                }
            }
            ts.print(LOG::info);
            tripleStores.put(impl, ts);
        }
    }

    void createCopies() {
        for (String impl : implementations) {
            TripleStore source = tripleStores.get(impl);
            TripleStore target = TripleStoreFactory.copy(source);
            tripleStoreCopies.put(impl, target);
        }
    }

    void testQuery(String queryText, Expected expected) {
        for (String impl : implementations) {
            PropertyBags results = tripleStores.get(impl).query(queryText);
            testQueryResults(impl, results, expected);
        }
    }

    void testQueryOnCopies(String queryText, Expected expected) {
        for (String impl : implementations) {
            PropertyBags results = tripleStoreCopies.get(impl).query(queryText);
            testQueryResults(impl, results, expected);
        }
    }

    void testQueryResults(String impl, PropertyBags results, Expected expected) {
        logResults(impl, results, expected);
        if (expected.isEmpty()) {
            assertTrue(results.isEmpty());
        } else {
            assertFalse(results.isEmpty());
            int size = expected.values().iterator().next().size();
            assertEquals(size, results.size());
            expected.keySet()
                .forEach(property -> assertEquals(expected.get(property), results.pluckLocalsUnsorted(property)));
        }
    }

    void testUpdate(String queryText) {
        for (String impl : implementations) {
            tripleStores.get(impl).update(queryText);
        }
    }

    void testUpdateOnCopies(String queryText) {
        for (String impl : implementations) {
            tripleStoreCopies.get(impl).update(queryText);
        }
    }

    void clear(String contextName, String namespace) {
        for (String impl : implementations) {
            TripleStore ts = tripleStores.get(impl);
            Set<String> before = ts.contextNames();
            ts.clear(contextName);
            Set<String> after = ts.contextNames();

            LOG.info("{} before", impl);
            before.forEach(c -> LOG.info("    {}", c));
            LOG.info("{} after", impl);
            after.forEach(c -> LOG.info("    {}", c));

            // Check names gathered from triple store against fully qualified contextName
            String qcontextName = namespace + contextName;
            assertTrue(before.contains(qcontextName));
            assertFalse(after.contains(qcontextName));
            Set<String> expected = new HashSet<>(before);
            expected.remove(qcontextName);
            Set<String> actual = after;
            assertEquals(expected, actual);
        }
    }

    private InputStream resourceStream(String resource) {
        return ClassLoader.getSystemResourceAsStream(resource);
    }

    private void logResults(String impl, PropertyBags results, Expected expected) {
        LOG.info("{} query result size     : {}", impl, results.size());
        if (results.isEmpty()) {
            return;
        }
        LOG.info("{} query result names[0] : {}", impl, results.get(0).keySet());
        LOG.info("{} tabulated results", impl);
        LOG.info(results.tabulate());
        LOG.info("{} tabulated results as localValues", impl);
        LOG.info(results.tabulateLocals());
        expected.keySet().forEach(property -> {
            List<String> expectedValues = expected.get(property);
            List<String> actualValues = results.pluckLocals(property);
            LOG.info("{} expected values for property {} : {}", impl, property, String.join(",", expectedValues));
            LOG.info("{} actual values for property {}   : {}", impl, property, String.join(",", actualValues));
        });
    }

    static class Expected extends HashMap<String, List<String>> {
        Expected expect(String property, String... values) {
            put(property, Arrays.asList(values));
            return this;
        }
    }

    TripleStore tripleStore(String impl) {
        return tripleStores.get(impl);
    }

    TripleStore tripleStoreCopy(String impl) {
        return tripleStoreCopies.get(impl);
    }

    private final List<String> implementations;
    private final String baseName;
    private final String[] inputResourceNames;
    private final Map<String, TripleStore> tripleStores;
    private final Map<String, TripleStore> tripleStoreCopies;

    private static final Logger LOG = LoggerFactory.getLogger(TripleStoreTester.class);
}
