/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreException;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreTester {

    public TripleStoreTester(List<String> implementations, String baseName, String... inputResourceNames) {
        this.implementations = implementations;
        this.baseName = baseName;
        this.inputResourceNames = inputResourceNames;
        this.tripleStores = new HashMap<>(implementations.size());
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

    void loadClone() {
        for (String impl : implementations) {
            ts = TripleStoreFactory.create(impl);
            TripleStore tsOrigin = tripleStores.get(impl);
            ts.duplicate(tsOrigin, baseName);
        }
    }

    void testQuery(String queryText, Expected expected) {
        for (String impl : implementations) {
            PropertyBags results = tripleStores.get(impl).query(queryText);
            logResults(impl, results, expected);
            assertTrue(!results.isEmpty());
            int size = expected.values().iterator().next().size();
            assertEquals(size, results.size());
            expected.keySet()
                .forEach(property -> assertEquals(expected.get(property), results.pluckLocals(property)));
        }
    }

    void testQueryClone(String queryText, Expected expected) {
        for (String impl : implementations) {
            PropertyBags results = ts.query(queryText);
            logResults(impl, results, expected);
            assertTrue(!results.isEmpty());
            int size = expected.values().iterator().next().size();
            assertEquals(size, results.size());
            expected.keySet()
                .forEach(property -> assertEquals(expected.get(property), results.pluckLocals(property)));
        }
    }

    void testUpdate(String queryText) {
        for (String impl : implementations) {
            tripleStores.get(impl).update(queryText);
        }
    }

    void testUpdateClone(String queryText) {
        ts.update(queryText);
    }

    void testPerformanceCloneByStatements() {
        for (String impl : implementations) {
            start = System.currentTimeMillis();
            LOG.info("totalMemory, GB: " + Runtime.getRuntime().totalMemory() / 1e+9);
            TripleStore ts = TripleStoreFactory.create(impl);
            TripleStore tsOrigin = tripleStores.get(impl);
            ts.duplicate(tsOrigin, baseName);
            LOG.info("usedMemory, GB: " +
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1e+9);
            end = System.currentTimeMillis();
            LOG.info(String.format("Clone repository by statementes for %s took: %d milliseconds", impl, end - start));
        }
    }

    void testPerformanceImportFiles() {
        // Load the model for every triple store implementation
        for (String impl : implementations) {
            start = System.currentTimeMillis();
            LOG.info("totalMemory, GB: " + Runtime.getRuntime().totalMemory() / 1e+9);
            TripleStore ts = TripleStoreFactory.create(impl);
            assertNotNull(ts);
            for (String r : inputResourceNames) {
                try (InputStream is = resourceStream(r)) {
                    ts.read(is, baseName, r);
                } catch (IOException e) {
                    throw new TripleStoreException(String.format("Reading %s %s", baseName, r), e);
                }
            }
            tripleStores.put(impl, ts);
            LOG.info("usedMemory, GB: " +
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1e+9);
            end = System.currentTimeMillis();
            LOG.info(String.format("Load XML files for %s took: %d milliseconds", impl, end - start));
        }
    }

    public void testClear(String contextName, String namespace) {
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
            Set<String> expected = before.stream().collect(Collectors.toSet());
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

    private final List<String> implementations;
    private final String baseName;
    private final String[] inputResourceNames;
    private final Map<String, TripleStore> tripleStores;
    private TripleStore ts;
    private static long start;
    private static long end;

    private static final Logger LOG = LoggerFactory.getLogger(TripleStoreTester.class);
}
