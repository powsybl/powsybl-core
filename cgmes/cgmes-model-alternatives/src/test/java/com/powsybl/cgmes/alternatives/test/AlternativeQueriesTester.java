package com.powsybl.cgmes.alternatives.test;

/*
 * #%L
 * CGMES Model Alternatives
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.CgmesModelFactory;
import com.powsybl.cgmes.test.TestGridModel;
import com.powsybl.cgmes.triplestore.CgmesModelTripleStore;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.triplestore.PropertyBags;
import com.powsybl.triplestore.QueryCatalog;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class AlternativeQueriesTester {

    public AlternativeQueriesTester(List<String> tripleStoreImplementations, QueryCatalog queries,
            TestGridModel gridModel, Expected expected) {
        this(tripleStoreImplementations, queries, gridModel, expected, 5, true, null);
    }

    public AlternativeQueriesTester(List<String> tripleStoreImplementations, QueryCatalog queries,
            TestGridModel gridModel, Expected expected, int experiments, boolean doAssert,
            Consumer<PropertyBags> consumer) {
        this.implementations = tripleStoreImplementations;
        this.queries = queries;
        this.gridModel = gridModel;
        this.expected = expected;
        this.experiments = experiments;
        this.doAssert = doAssert;
        this.consumer = consumer;
        models = new HashMap<>(implementations.size());
    }

    public Expected expected() {
        return this.expected;
    }

    public void load() {
        queries.load(this.getClass().getClassLoader().getResourceAsStream(queries.resource()));

        // Load the model for every triple store implementation
        for (String impl : implementations) {
            ReadOnlyDataSource dataSource = DataSourceUtil.createDataSource(
                    gridModel.path(),
                    gridModel.basename(),
                    gridModel.getCompressionExtension(),
                    null);
            CgmesModelTripleStore cgmes = CgmesModelFactory.create(dataSource, impl);
            models.put(impl, cgmes);
        }
    }

    public void test(String alternative, Expected expected, Consumer<PropertyBags> consumer) {
        String queryText = queries.get(alternative);
        assertNotNull(queryText);
        assertFalse(queryText.isEmpty());
        for (String impl : implementations) {
            testWithExperiments(alternative, impl, queryText, expected, consumer);
        }
    }

    public void test(String alternative) {
        // If no explicit expected result, use the default expected result for the
        // tester
        test(alternative, this.expected);
    }

    public void test(String alternative, Expected expected) {
        test(alternative, expected, this.consumer);
    }

    public static class Expected {
        public Expected() {
            resultSize = 0;
            propertyCount = new HashMap<>();
        }

        public Expected resultSize(long resultSize) {
            this.resultSize = resultSize;
            return this;
        }

        public long resultSize() {
            return this.resultSize;
        }

        public Expected propertyCount(String property, long count) {
            this.propertyCount.put(property, count);
            return this;
        }

        private long resultSize;
        private final Map<String, Long> propertyCount;
    }

    private void testWithExperiments(String alternative, String impl, String queryText, Expected expected,
            Consumer<PropertyBags> consumer) {

        // Initial run to compare against potential "caching" considerations
        // All engines have the opportunity to "activate" caching mechanisms
        final long t00 = System.currentTimeMillis();
        models.get(impl).query(queryText);
        final long t10 = System.currentTimeMillis();
        final long dt0 = t10 - t00;

        long dt = 0;
        long[] dts = new long[experiments];
        for (int k = 0; k < experiments; k++) {

            final long t0 = System.currentTimeMillis();
            PropertyBags result = models.get(impl).query(queryText);
            final long t1 = System.currentTimeMillis();
            dts[k] = t1 - t0;
            dt += dts[k];

            if (consumer != null) {
                LOG.info("{} {} consume result:", alternative, impl);
                consumer.accept(result);
            }

            test(alternative, impl, result, expected);
        }
        LOG.info("{} {} dt avg {} ms {} experiments, dts: {} {}", alternative, impl, dt / experiments, experiments, dt0,
                Arrays.toString(dts));
    }

    private void test(String alternative, String impl, PropertyBags result, Expected expected) {
        if (doAssert) {
            assertEquals(expected.resultSize, result.size());
        } else {
            LOG.info("{} {} results {} {} {}", alternative, impl, expected, result.size(),
                    expected.resultSize == result.size() ? "OK" : "FAIL");
        }
        for (String p : expected.propertyCount.keySet()) {
            long expectedPropertyCount = expected.propertyCount.get(p).longValue();
            long actualPropertyCount = result.stream().filter(r -> r.containsKey(p)).count();
            if (doAssert) {
                assertEquals(expectedPropertyCount, actualPropertyCount);
            } else {
                LOG.info("{} {} {} {} {} {}", alternative, impl, p, expectedPropertyCount, actualPropertyCount,
                        expectedPropertyCount == actualPropertyCount ? "OK" : "FAIL");
            }
        }
    }

    private final List<String> implementations;
    private final Map<String, CgmesModelTripleStore> models;
    private final QueryCatalog queries;
    private final TestGridModel gridModel;
    private final Expected expected;
    private final int experiments;
    private final boolean doAssert;
    private final Consumer<PropertyBags> consumer;

    private static final Logger LOG = LoggerFactory.getLogger(AlternativeQueriesTester.class);
}
