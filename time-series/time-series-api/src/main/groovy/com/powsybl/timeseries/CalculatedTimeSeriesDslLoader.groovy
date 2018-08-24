/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries

import com.powsybl.timeseries.ast.CalculatedTimeSeriesDslAstTransformation
import com.powsybl.timeseries.ast.DoubleNodeCalc
import com.powsybl.timeseries.ast.FloatNodeCalc
import com.powsybl.timeseries.ast.IntegerNodeCalc
import com.powsybl.timeseries.ast.NodeCalc
import com.powsybl.timeseries.ast.TimeSeriesNameNodeCalc
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.ZonedDateTime

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CalculatedTimeSeriesDslLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatedTimeSeriesDslLoader.class)

    public static final String SCRIPT_NAME = "script"

    protected final GroovyCodeSource dslSrc

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

        String[] getNames() {
            store.getTimeSeriesNames(new TimeSeriesFilter())
        }
    }

    CalculatedTimeSeriesDslLoader(String script) {
        this.dslSrc = new GroovyCodeSource(script, SCRIPT_NAME, GroovyShell.DEFAULT_CODE_BASE)
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
        def astCustomizer = new ASTTransformationCustomizer(new CalculatedTimeSeriesDslAstTransformation())
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(astCustomizer)
    }

    Map<String, NodeCalc> load(ReadOnlyTimeSeriesStore store) {
        long start = System.currentTimeMillis()

        Map<String, NodeCalc> nodes = new HashMap<>()

        Binding binding = new Binding()
        bind(binding, store, nodes)

        def shell = new GroovyShell(binding, createCompilerConfig())
        shell.evaluate(dslSrc)

        LOGGER.trace("Calculated time series DSL loaded in {} ms", (System.currentTimeMillis() -start))

        nodes
    }
}
