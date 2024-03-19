/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.dsl

import com.powsybl.commons.extensions.Extension
import com.powsybl.contingency.*
import com.powsybl.dsl.DslException
import com.powsybl.dsl.DslLoader
import com.powsybl.dsl.ExtendableDslExtension
import com.powsybl.iidm.network.*
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.slf4j.LoggerFactory

import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class ContingencyDslLoader extends DslLoader {

    static LOGGER = LoggerFactory.getLogger(ContingencyDslLoader.class)

    static class ContingencySpec {

        String[] equipments

        void equipments(String[] equipments) {
            this.equipments = equipments
        }

    }

    ContingencyDslLoader(GroovyCodeSource dslSrc) {
        super(dslSrc)
    }

    ContingencyDslLoader(File dslFile) {
        super(dslFile)
    }

    ContingencyDslLoader(String script) {
        super(script)
    }

    static void loadDsl(Binding binding, Network network, Consumer<Contingency> consumer, ContingencyDslObserver observer) {
        // contingencies
        binding.contingency = { String id, Closure<Void> closure ->
            def (ContingencySpec contingencySpec, ArrayList<Extension<Contingency>> extensionList) = processClosure(closure, binding)
            def valid = true
            def builder = Contingency.builder(id)
            for (String equipment : contingencySpec.equipments) {
                Identifiable identifiable = network.getIdentifiable(equipment)
                if (identifiable == null) {
                    LOGGER.warn("Equipment '{}' of contingency '{}' not found", equipment, id)
                    valid = false
                } else if (identifiable instanceof Line) {
                    builder.addLine(equipment)
                } else if (identifiable instanceof TwoWindingsTransformer) {
                    builder.addTwoWindingsTransformer(equipment)
                } else if (identifiable instanceof HvdcLine) {
                    builder.addHvdcLine(equipment)
                } else if (identifiable instanceof Generator) {
                    builder.addGenerator(equipment)
                } else if (identifiable instanceof BusbarSection) {
                    builder.addBusbarSection(equipment)
                } else if (identifiable instanceof ShuntCompensator) {
                    builder.addShuntCompensator(equipment)
                } else if (identifiable instanceof StaticVarCompensator) {
                    builder.addStaticVarCompensator(equipment)
                } else if (identifiable instanceof DanglingLine) {
                    builder.addDanglingLine(equipment)
                } else if (identifiable instanceof ThreeWindingsTransformer) {
                    builder.addThreeWindingsTransformer(equipment)
                } else {
                    LOGGER.warn("Equipment type {} not supported in contingencies", identifiable.getClass().name)
                    valid = false
                }
            }
            if (valid) {
                LOGGER.debug("Found contingency '{}'", id)
                observer?.contingencyFound(id)
                Contingency contingency = builder.build()
                extensionList.forEach({ ext ->
                    contingency.addExtension(ext.getClass(), ext)
                })
                consumer.accept(contingency)
            } else {
                LOGGER.warn("Contingency '{}' is invalid", id)
            }
        }
    }

    private static List processClosure(Closure<Void> closure, Binding binding) {
        def cloned = closure.clone()
        ContingencySpec contingencySpec = new ContingencySpec()

        List<Extension<Contingency>> extensionList = new ArrayList<>();
        for (ExtendableDslExtension dslContingencyExtension : ServiceLoader.load(ContingencyDslExtension.class, ContingencyDslLoader.class.getClassLoader())) {
            dslContingencyExtension.addToSpec(contingencySpec.metaClass, extensionList, binding)
        }

        cloned.delegate = contingencySpec
        cloned()
        if (!contingencySpec.equipments) {
            throw new DslException("'equipments' field is not set")
        }
        if (contingencySpec.equipments.length == 0) {
            throw new DslException("'equipments' field is empty")
        }
        [contingencySpec, extensionList]
    }

    Map<String, Set<String>> loadNotFoundElements(Network network) {
        loadNotFoundElements(network, null)
    }

    Map<String, Set<String>> loadNotFoundElements(Network network, ContingencyDslObserver observer) {
        Map<String, Set<String>> notFoundElements = new HashMap<>()

        LOGGER.debug("Loading DSL '{}'", dslSrc.getName())
        try {
            observer?.begin(dslSrc.getName())

            Binding binding = new Binding()

            findNotFoundIdentifiables(binding, network, notFoundElements.&put)

            // set base network
            binding.setVariable("network", network)

            def shell = createShell(binding, new ImportCustomizer())

            shell.evaluate(dslSrc)

            observer?.end()

            notFoundElements

        } catch (CompilationFailedException e) {
            throw new DslException(e.getMessage(), e)
        }
    }

    static void findNotFoundIdentifiables(Binding binding, Network network, BiConsumer<String, Set<String>> consumer) {
        binding.contingency = { String id, Closure<Void> closure ->
            def (ContingencySpec contingencySpec) = processClosure(closure, binding)

            Set<String> notFoundIdentifiables = new LinkedHashSet<>();
            for (String equipment : contingencySpec.equipments) {
                Identifiable identifiable = network.getIdentifiable(equipment)
                if (identifiable == null) {
                    LOGGER.warn("Equipment '{}' of contingency '{}' not found", equipment, id)
                    notFoundIdentifiables.add(equipment);
                }
            }
            if (!notFoundIdentifiables.isEmpty()) {
                consumer.accept(id, notFoundIdentifiables)
            }
        }
    }

    List<Contingency> load(Network network) {
        load(network, null, new ImportCustomizer())
    }

    List<Contingency> load(Network network, ImportCustomizer imports) {
        load(network, null, imports)
    }

    List<Contingency> load(Network network, ContingencyDslObserver observer) {
        load(network, observer, new ImportCustomizer())
    }

    List<Contingency> load(Network network, ContingencyDslObserver observer, ImportCustomizer imports) {

        List<Contingency> contingencies = new ArrayList<>()

        LOGGER.debug("Loading DSL '{}'", dslSrc.getName())
        try {
            observer?.begin(dslSrc.getName())

            Binding binding = new Binding()

            loadDsl(binding, network, contingencies.&add, observer)

            // set base network
            binding.setVariable("network", network)

            def shell = createShell(binding, imports)

            shell.evaluate(dslSrc)

            observer?.end()

            contingencies

        } catch (CompilationFailedException e) {
            throw new DslException(e.getMessage(), e)
        }
    }
}
