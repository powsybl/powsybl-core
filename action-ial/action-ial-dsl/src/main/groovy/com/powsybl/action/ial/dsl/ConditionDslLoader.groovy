/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.ial.dsl


import com.powsybl.action.ial.dsl.ast.ActionExpressionHelper
import com.powsybl.action.ial.dsl.ast.NetworkComponentNode
import com.powsybl.action.ial.dsl.ast.NetworkNode
import com.powsybl.action.ial.dsl.ast.NetworkPropertyNode
import com.powsybl.dsl.DslLoader
import com.powsybl.dsl.ExpressionDslLoader
import com.powsybl.dsl.ast.ArithmeticBinaryOperator
import com.powsybl.dsl.ast.ExpressionHelper
import com.powsybl.dsl.ast.ExpressionNode
import com.powsybl.iidm.network.*
import org.codehaus.groovy.control.CompilationFailedException

import static com.powsybl.dsl.GroovyDslConstants.SCRIPT_IS_RUNNING

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ConditionDslLoader extends DslLoader {

    ConditionDslLoader(GroovyCodeSource dslSrc) {
        super(dslSrc)
    }

    ConditionDslLoader(File dslFile) {
        super(dslFile)
    }

    ConditionDslLoader(String script) {
        super(script)
    }

    private static Line getLine(Network network, String id) {
        Line l = network.getLine(id)
        if (l == null) {
            throw new ActionDslException("Line '" + id + "' not found")
        }
        l
    }

    private static Branch getBranch(Network network, String id) {
        Branch b = network.getBranch(id)
        if (b == null) {
            throw new ActionDslException("Branch '" + id + "' not found")
        }
        b
    }

    private static TwoWindingsTransformer getTwoWindingsTransformer(Network network, String id) {
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer(id)
        if (twt == null) {
            throw new ActionDslException("Transformer '" + twt + "' not found")
        }
        twt
    }

    private static Generator getGenerator(Network network, String id) {
        Generator g = network.getGenerator(id)
        if (g == null) {
            throw new ActionDslException("Generator '" + id + "' not found")
        }
        g
    }

    private static Load getLoad(Network network, String id) {
        Load l = network.getLoad(id)
        if (l == null) {
            throw new ActionDslException("Load '" + id + "' not found")
        }
        l
    }

    private static Switch getSwitch(Network network, String id) {
        Switch s = network.getSwitch(id)
        if (s == null) {
            throw new ActionDslException("Switch '" + id + "' not found")
        }
        s
    }

    private static void bindNetwork(Binding binding) {
        binding.line = { id ->
            Network network = binding.getVariable("network")
            Line l = getLine(network, id)
            binding.hasVariable(SCRIPT_IS_RUNNING) ? l : ActionExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.LINE)
        }

        binding.branch = { id ->
            Network network = binding.getVariable("network")
            Branch b = getBranch(network, id)
            binding.hasVariable(SCRIPT_IS_RUNNING) ? b : ActionExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.BRANCH)
        }

        binding.transformer = { id ->
            Network network = binding.getVariable("network")
            TwoWindingsTransformer twt = getTwoWindingsTransformer(network, id)
            binding.hasVariable(SCRIPT_IS_RUNNING) ? twt : ActionExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.TRANSFORMER)
        }

        binding.generator = { id ->
            Network network = binding.getVariable("network")
            Generator g = getGenerator(network, id)
            binding.hasVariable(SCRIPT_IS_RUNNING) ? g : ActionExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.GENERATOR)
        }

        binding.load = { id ->
            Network network = binding.getVariable("network")
            Load l = getLoad(network, id)
            binding.hasVariable(SCRIPT_IS_RUNNING) ? l : ActionExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.LOAD)
        }

        binding.switch_ = { id ->
            Network network = binding.getVariable("network")
            Switch sw = getSwitch(network, id)
            binding.hasVariable(SCRIPT_IS_RUNNING) ? sw : ActionExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.SWITCH)
        }

        binding._switch = { id ->
            Network network = binding.getVariable("network")
            Switch sw = getSwitch(network, id)
            binding.hasVariable(SCRIPT_IS_RUNNING) ? sw : ActionExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.SWITCH)
        }
    }

    static void prepareClosures(Binding binding) {

        ExpressionDslLoader.prepareClosures(binding)

        bindNetwork(binding)

        binding.actionTaken = { id ->
            ActionExpressionHelper.newActionTaken(id)
        }

        binding.contingencyOccurred = { id = null ->
            ActionExpressionHelper.newContingencyOccured(id)
        }

        binding.loadingRank = { branchIdToRank, branchIds ->
            def branchIdToRankNode = ExpressionDslLoader.createExpressionNode(branchIdToRank)
            def branchIdNodes = branchIds.collect({ id -> ExpressionDslLoader.createExpressionNode(id) })
            ActionExpressionHelper.newLoadingRank(branchIdToRankNode, branchIdNodes)
        }

        binding.mostLoaded = { branchIds ->
            ActionExpressionHelper.newMostLoaded(branchIds)
        }

        binding.isOverloaded = { branchIds, limitReduction = 1 as float ->
            ActionExpressionHelper.newIsOverloadedNode(branchIds, limitReduction)
        }

        binding.allOverloaded = { branchIds, limitReduction = 1 as float ->
            ActionExpressionHelper.newAllOverloadedNode(branchIds, limitReduction)
        }

        NetworkNode.metaClass.propertyMissing = { String name ->
            ActionExpressionHelper.newNetworkProperty(delegate, name)
        }

        NetworkNode.metaClass.methodMissing = { String name, args ->
            ActionExpressionHelper.newNetworkMethod(delegate, name, args)
        }

        // operator overloading

        for (op in [['plus', 'PLUS'],
                    ['minus', 'MINUS'],
                    ['multiply', 'MULTIPLY'],
                    ['div', 'DIVIDE']]) {
            def op0 = op[0]
            def op1 = op[1]

            NetworkPropertyNode.metaClass."$op0" = { ExpressionNode value ->
                ExpressionHelper.newArithmeticBinaryOperator(delegate, value, ArithmeticBinaryOperator."$op1")
            }
        }
    }

    Object load(Network network) {
        try {
            Binding binding = new Binding()

            binding.setVariable("network", network)
            prepareClosures(binding)

            def shell = createShell(binding)

            def value = shell.evaluate(dslSrc)
            if (value instanceof Identifiable) {
                value
            } else {
                ExpressionDslLoader.createExpressionNode(value)
            }
        } catch (CompilationFailedException e) {
            throw new ActionDslException(e.getMessage(), e)
        }
    }
}
