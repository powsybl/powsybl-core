/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.dsl

import com.google.auto.service.AutoService
import com.powsybl.timeseries.CalculatedTimeSeriesDslLoader
import com.powsybl.timeseries.ReadOnlyTimeSeriesStore
import com.powsybl.timeseries.TimeSeriesException
import com.powsybl.timeseries.TimeSeriesFilter
import com.powsybl.timeseries.ast.*
import com.powsybl.timeseries.dsl.CalculatedTimeSeriesGroovyDslAstTransformation
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.ZonedDateTime

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(CalculatedTimeSeriesDslLoader.class)
class CalculatedTimeSeriesGroovyDslLoader implements CalculatedTimeSeriesDslLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatedTimeSeriesGroovyDslLoader.class)

    public static final String SCRIPT_NAME = "script"

    static class TimeSeriesGroovyObject {

        private final ReadOnlyTimeSeriesStore store

        private final Map<String, NodeCalc> nodes

        TimeSeriesGroovyObject(ReadOnlyTimeSeriesStore store, Map<String, NodeCalc> nodes) {
            assert store != null
            assert nodes != null
            this.store = store
            this.nodes = nodes
        }

        NodeCalc getAt(String name) {
            assert name != null
            NodeCalc node = nodes.get(name)
            if (node != null) {
                node
            } else {
                if (!store.timeSeriesExists(name)) {
                    throw new TimeSeriesException("Time Series '" + name + "' not found")
                }
                new TimeSeriesNameNodeCalc(name)
            }
        }

        boolean exists(String name) {
            assert name != null
            NodeCalc node = nodes.get(name)
            if (node == null) {
                return store.timeSeriesExists(name)
            }
            return true
        }

        void putAt(String name, NodeCalc node) {
            assert name != null
            assert node != null
            nodes.put(name, node)
        }

        void putAt(String name, Integer integer) {
            assert name != null
            assert integer != null
            nodes.put(name, new IntegerNodeCalc(integer))
        }

        void putAt(String name, Float aFloat) {
            assert name != null
            assert aFloat != null
            nodes.put(name, new FloatNodeCalc(aFloat))
        }

        void putAt(String name, Double aDouble) {
            assert name != null
            assert aDouble != null
            nodes.put(name, new DoubleNodeCalc(aDouble))
        }

        void putAt(String name, BigDecimal aBigDecimal) {
            assert name != null
            assert aBigDecimal != null
            nodes.put(name, new DoubleNodeCalc(aBigDecimal.doubleValue()))
        }

        String[] getNames() {
            store.getTimeSeriesNames(new TimeSeriesFilter())
        }
    }

    static void bind(Binding binding, ReadOnlyTimeSeriesStore store, Map<String, NodeCalc> nodes) {
        def ts = new TimeSeriesGroovyObject(store, nodes)
        binding.timeSeries = ts
        binding.ts = ts
        binding.time = { String str ->
            ZonedDateTime.parse(str).toInstant().toEpochMilli().toDouble()
        }
    }

    static CompilerConfiguration createCompilerConfig() {
        def astCustomizer = new ASTTransformationCustomizer(new CalculatedTimeSeriesGroovyDslAstTransformation())
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(astCustomizer)
    }

    public Map<String, NodeCalc> load(String script, ReadOnlyTimeSeriesStore store) {
        long start = System.currentTimeMillis()

        Map<String, NodeCalc> nodes = new HashMap<>()

        Binding binding = new Binding()
        bind(binding, store, nodes)

        def shell = new GroovyShell(binding, createCompilerConfig())
        def dslSrc = new GroovyCodeSource(script, SCRIPT_NAME, GroovyShell.DEFAULT_CODE_BASE)
        shell.evaluate(dslSrc)

        LOGGER.trace("Calculated time series DSL loaded in {} ms", (System.currentTimeMillis() -start))

        nodes
    }
}
