/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.dsl

import java.util.function.Consumer

import org.codehaus.groovy.control.CompilationFailedException
import org.slf4j.LoggerFactory

import com.powsybl.dsl.DslException
import com.powsybl.dsl.DslLoader
import com.powsybl.dynamicsimulation.Curves
import com.powsybl.iidm.network.Identifiable
import com.powsybl.iidm.network.Network

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
class CurvesDslLoader extends DslLoader {

    static LOGGER = LoggerFactory.getLogger(CurvesDslLoader.class)

        static class CurvesSpec {

        String modelId
        String[] variables

        void modelId(String modelId) {
            this.modelId = modelId
        }

        void variables(String[] variables) {
            this.variables = variables
        }

    }

    public CurvesDslLoader(GroovyCodeSource dslSrc) {
        super(dslSrc)
    }

    static void loadDsl(Binding binding, Network network, Consumer<Curves> consumer, CurvesDslObserver observer) {
        binding.curves = { Closure<Void> closure ->
            def cloned = closure.clone()
            CurvesSpec curvesSpec = new CurvesSpec()
            cloned.delegate = curvesSpec
            cloned()
            if (!curvesSpec.modelId) {
                throw new DslException("'modelId' field is not set")
            }
            if (!curvesSpec.variables) {
                throw new DslException("'variables' field is not set")
            }
            def id = curvesSpec.modelId
            Identifiable identifiable = network.getIdentifiable(id)
            if (identifiable == null) {
                throw new DslException("Curves is invalid: Equipment '" + id + "' not found")
            } else {
                LOGGER.debug("Found curves '{}'", id)
                def variables = curvesSpec.variables
                observer?.curvesFound(id)
                Curves curves = new Curves(id, variables)
                consumer.accept(curves)
            }
        }
    }

    public List<Curves> load(Network network) {
        return load(network, null)
    }

    public List<Curves> load(Network network, CurvesDslObserver observer) {
        List<Curves> curves = new ArrayList<>()

        LOGGER.debug("Loading DSL '{}'", dslSrc.getName())
        try {
            observer?.begin(dslSrc.getName())

            Binding binding = new Binding()

            loadDsl(binding, network, curves.&add, observer)

            // set base network
            binding.setVariable("network", network)

            def shell = createShell(binding)

            shell.evaluate(dslSrc)

            observer?.end()

            curves

        } catch (CompilationFailedException e) {
            throw new DslException(e.getMessage(), e)
        }
    }
}