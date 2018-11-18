/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl

import com.powsybl.action.dsl.ast.BooleanLiteralNode
import com.powsybl.action.dsl.ast.ExpressionNode
import com.powsybl.action.dsl.spi.DslTaskExtension
import com.powsybl.contingency.*
import com.powsybl.contingency.tasks.ModificationTask
import com.powsybl.iidm.network.*
import org.codehaus.groovy.control.CompilationFailedException
import org.slf4j.LoggerFactory

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ActionDslLoader extends DslLoader {

    static LOGGER = LoggerFactory.getLogger(ActionDslLoader.class)

    static class ContingencySpec {

        String[] equipments

        void equipments(String[] equipments) {
            this.equipments = equipments
        }
    }

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

    static class TasksSpec {
    }

    static class ActionSpec {

        String description

        final TasksSpec tasksSpec = new TasksSpec()

        void description(String description) {
            this.description = description
        }

        void tasks(Closure<Void> closure) {
            def cloned = closure.clone()
            cloned.delegate = tasksSpec
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
        load(network, null)
    }

    /**
     * Loads in binding the functions which create contingencies, actions, and rules,
     * binding them to the network parameter. The handler defines how created objects will be used.
     *
     * @param binding  The context which functions will be created in
     * @param network  The network which functions will be bound to
     * @param handler  Will allow client code to define how objects created when interpreting a script will be used
     * @param observer Will allow client code to observe the interpretation of the script
     */
    static void loadDsl(Binding binding, Network network, ActionDslHandler handler, ActionDslLoaderObserver observer)  {

        // set base network
        binding.setVariable("network", network)

        // contingencies
        binding.contingency = { String id, Closure<Void> closure ->
            def cloned = closure.clone()
            ContingencySpec contingencySpec = new ContingencySpec()
            cloned.delegate = contingencySpec
            cloned()
            if (!contingencySpec.equipments) {
                throw new ActionDslException("'equipments' field is not set")
            }
            if (contingencySpec.equipments.length == 0) {
                throw new ActionDslException("'equipments' field is empty")
            }
            def elements = []
            def valid = true
            for (String equipment : contingencySpec.equipments) {
                Identifiable identifiable = network.getIdentifiable(equipment)
                if (identifiable == null) {
                    LOGGER.warn("Equipment '{}' of contingency '{}' not found", equipment, id)
                    valid = false
                } else if (identifiable instanceof Line || identifiable instanceof TwoWindingsTransformer) {
                    elements.add(new BranchContingency(equipment))
                } else if (identifiable instanceof HvdcLine) {
                    elements.add(new HvdcLineContingency(equipment))
                } else if (identifiable instanceof Generator) {
                    elements.add(new GeneratorContingency(equipment))
                } else if (identifiable instanceof BusbarSection) {
                    elements.add(new BusbarSectionContingency(equipment))
                } else {
                    LOGGER.warn("Equipment type {} not supported in contingencies", identifiable.getClass().name)
                    valid = false
                }
            }
            if (valid) {
                LOGGER.debug("Found contingency '{}'", id)
                observer?.contingencyFound(id)
                Contingency contingency = new Contingency(id, elements)
                handler.addContingency(contingency)
            } else {
                LOGGER.warn("Contingency '{}' is invalid", id)
            }
        }

        ConditionDslLoader.prepareClosures(binding)

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

            // fill tasks spec with extensions
            List<ModificationTask> tasks = new ArrayList<>()
            for (DslTaskExtension taskExtension : ServiceLoader.load(DslTaskExtension.class)) {
                taskExtension.addToSpec(actionSpec.tasksSpec.metaClass, tasks, binding)
            }

            cloned.delegate = actionSpec
            cloned()

            // create action
            Action action = new Action(id, tasks)
            if (actionSpec.description) {
                action.setDescription(actionSpec.description)
            }
            handler.addAction(action)

            LOGGER.debug("Found action '{}'", id)
            observer?.actionFound(id)
        }
    }

    void load(Network network, ActionDslHandler handler, ActionDslLoaderObserver observer) {

        LOGGER.debug("Loading DSL '{}'", dslSrc.getName())
        observer?.begin(dslSrc.getName())

        Binding binding = new Binding()

        loadDsl(binding, network, handler, observer)
        try {

            def shell = createShell(binding)

            shell.evaluate(dslSrc)

            observer?.end()
        } catch (CompilationFailedException e) {
            throw new ActionDslException(e.getMessage(), e)
        }
    }

    ActionDb load(Network network, ActionDslLoaderObserver observer) {
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

        load(network, actionDbBuilder, observer)

        rulesDb.checkUndefinedActions()
        rulesDb
    }
}
