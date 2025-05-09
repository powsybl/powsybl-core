/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.dsl

import com.powsybl.action.ial.dsl.spi.DslModificationExtension
import com.powsybl.contingency.Contingency
import com.powsybl.contingency.dsl.ContingencyDslLoader
import com.powsybl.dsl.DslLoader
import com.powsybl.dsl.ast.BooleanLiteralNode
import com.powsybl.dsl.ast.ExpressionNode
import com.powsybl.iidm.modification.NetworkModification
import com.powsybl.iidm.network.Network
import com.powsybl.scripting.groovy.GroovyScriptExtension
import com.powsybl.scripting.groovy.GroovyScripts
import org.codehaus.groovy.control.CompilationFailedException
import org.slf4j.LoggerFactory

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ActionDslLoader extends DslLoader {

    static LOGGER = LoggerFactory.getLogger(ActionDslLoader.class)

    static class RuleSpec {

        String description
        ExpressionNode when
        String[] apply
        String[] test
        int life = 1

        void description(String description) {
            this.description = description
        }

        void when(ExpressionNode when) {
            assert when != null
            this.when = when
        }

        void when(boolean b) {
            ExpressionNode node = new BooleanLiteralNode(b)
            when(node)
        }

        void apply(String[] apply) {
            assert apply != null && apply.length > 0
            this.apply = apply
        }

        void test(String[] test) {
            assert test != null && test.length > 0
            this.test = test
        }

        void life(int life) {
            assert life > 0
            this.life = life
        }

        boolean hasApplyActions() {
            return apply != null && apply.length > 0;
        }

        boolean hasTestActions() {
            return test != null && test.length > 0;
        }
    }

    static class ModificationsSpec {
    }

    static class ActionSpec {

        String description

        final ModificationsSpec modificationsSpec = new ModificationsSpec()

        void description(String description) {
            this.description = description
        }

        void modifications(Closure<Void> closure) {
            def cloned = closure.clone()
            cloned.delegate = modificationsSpec
            cloned()
        }

        void tasks(Closure<Void> closure) {
            def cloned = closure.clone()
            cloned.delegate = modificationsSpec
            cloned()
        }
    }

    ActionDslLoader(GroovyCodeSource dslSrc) {
        super(dslSrc)
    }

    ActionDslLoader(File dslFile) {
        super(dslFile)
    }

    ActionDslLoader(String script) {
        super(script)
    }

    ActionDb load(Network network) {
        load(network, null, new HashMap<Class<?>, Object>())
    }

    ActionDb load(Network network, Map<Class<?>, Object> contextObjects) {
        load(network, null, contextObjects)
    }

    /**
     * Loads in binding the functions which create contingencies, actions, and rules,
     * binding them to the network parameter. The handler defines how created objects will be used.
     *
     * @param binding           The context which functions will be created in
     * @param network           The network which functions will be bound to
     * @param handler           Will allow client code to define how objects created when interpreting a script will be used
     * @param observer          Will allow client code to observe the interpretation of the script
     * @param contextObjects    Context objects used in groovy script extensions
     */
    static void loadDsl(Binding binding, Network network, ActionDslHandler handler, ActionDslLoaderObserver observer, Map<Class<?>, Object> contextObjects)  {

        // set base network
        binding.setVariable("network", network)

        // contingencies
        ContingencyDslLoader.loadDsl(binding, network, {c -> handler.addContingency(c)}, observer)

        ConditionDslLoader.prepareClosures(binding)

        // Bindings through extensions
        Iterable<GroovyScriptExtension> extensions = ServiceLoader.load(GroovyScriptExtension.class, GroovyScripts.class.getClassLoader())
        extensions.forEach { it.load(binding, contextObjects) }

        // rules
        binding.rule = { String id, Closure<Void> closure ->
            def cloned = closure.clone()
            RuleSpec ruleSpec = new RuleSpec()
            cloned.delegate = ruleSpec
            cloned()
            if (!ruleSpec.when) {
                throw new ActionDslException("'when' field is not set in rule '" + id + "'")
            }

            if (ruleSpec.hasApplyActions() && ruleSpec.hasTestActions()) {
                throw new ActionDslException("apply/test actions are both found in rule '" + id + "'");
            }

            if (!ruleSpec.hasApplyActions() && !ruleSpec.hasTestActions()) {
                throw new ActionDslException("neither apply nor test actions are found in rule '" + id + "'");
            }

            List<String> actions;
            RuleType type;
            if (ruleSpec.hasTestActions()) {
                actions = ruleSpec.test
                type = RuleType.TEST
            } else {
                actions = ruleSpec.apply
                type = RuleType.APPLY
            }

            Rule rule = new Rule(id, new ExpressionCondition(ruleSpec.when), ruleSpec.life, type,
                    actions)
            if (ruleSpec.description) {
                rule.setDescription(ruleSpec.description)
            }
            handler.addRule(rule)
            LOGGER.debug("Found rule '{}'", id)
            observer?.ruleFound(id)
        }

        // actions
        binding.action = { String id, Closure<Void> closure ->
            def cloned = closure.clone()

            ActionSpec actionSpec = new ActionSpec()

            // fill modifications spec with extensions
            List<NetworkModification> modifications = new ArrayList<>()
            for (DslModificationExtension extension : ServiceLoader.load(DslModificationExtension.class, ActionDslLoader.class.getClassLoader())) {
                extension.addToSpec(actionSpec.modificationsSpec.metaClass, modifications, binding)
            }

            cloned.delegate = actionSpec
            cloned()

            // create action
            Action action = new Action(id, modifications)
            if (actionSpec.description) {
                action.setDescription(actionSpec.description)
            }
            handler.addAction(action)

            LOGGER.debug("Found action '{}'", id)
            observer?.actionFound(id)
        }
    }

    void load(Network network, ActionDslHandler handler, ActionDslLoaderObserver observer) {
        load(network, handler, observer, new HashMap<Class<?>, Object>())
    }

    void load(Network network, ActionDslHandler handler, ActionDslLoaderObserver observer, Map<Class<?>, Object> contextObjects) {

        LOGGER.debug("Loading DSL '{}'", dslSrc.getName())
        observer?.begin(dslSrc.getName())

        Binding binding = new Binding()

        loadDsl(binding, network, handler, observer, contextObjects)
        try {

            def shell = createShell(binding)

            // Check for thread interruption right before beginning the evaluation
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Execution Interrupted");

            shell.evaluate(dslSrc)

            observer?.end()
        } catch (CompilationFailedException e) {
            throw new ActionDslException(e.getMessage(), e)
        }
    }

    ActionDb load(Network network, ActionDslLoaderObserver observer) {
        return load(network, observer, new HashMap<Class<?>, Object>())
    }

    ActionDb load(Network network, ActionDslLoaderObserver observer, Map<Class<?>, Object> contextObjects) {
        ActionDb rulesDb = new ActionDb()

        //Handler to create an ActionDb instance
        ActionDslHandler actionDbBuilder = new ActionDslHandler() {

            @Override
            void addContingency(Contingency contingency) {
                rulesDb.addContingency(contingency)
            }

            @Override
            void addRule(Rule rule) {
                rulesDb.addRule(rule)
            }

            @Override
            void addAction(Action action) {
                rulesDb.addAction(action)
            }
        }

        load(network, actionDbBuilder, observer, contextObjects)

        rulesDb.checkUndefinedActions()
        rulesDb
    }
}
