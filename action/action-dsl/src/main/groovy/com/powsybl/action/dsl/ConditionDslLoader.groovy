/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl

import com.powsybl.action.dsl.ast.*
import com.powsybl.iidm.network.*
import org.codehaus.groovy.control.CompilationFailedException

import static com.powsybl.action.dsl.GroovyDslConstants.SCRIPT_IS_RUNNING

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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
            binding.hasVariable(SCRIPT_IS_RUNNING) ? l : ExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.LINE)
        }

        binding.branch = { id ->
            Network network = binding.getVariable("network")
            Branch b = getBranch(network, id)
            binding.hasVariable(SCRIPT_IS_RUNNING) ? b : ExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.BRANCH)
        }

        binding.transformer = { id ->
            Network network = binding.getVariable("network")
            TwoWindingsTransformer twt = getTwoWindingsTransformer(network, id)
            binding.hasVariable(SCRIPT_IS_RUNNING) ? twt : ExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.TRANSFORMER)
        }

        binding.generator = { id ->
            Network network = binding.getVariable("network")
            Generator g = getGenerator(network, id)
            binding.hasVariable(SCRIPT_IS_RUNNING) ? g : ExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.GENERATOR)
        }

        binding.load = { id ->
            Network network = binding.getVariable("network")
            Load l = getLoad(network, id)
            binding.hasVariable(SCRIPT_IS_RUNNING) ? l : ExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.LOAD)
        }

        binding.switch_ = { id ->
            Network network = binding.getVariable("network")
            Switch sw = getSwitch(network, id)
            binding.hasVariable(SCRIPT_IS_RUNNING) ? sw : ExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.SWITCH)
        }

        binding._switch = { id ->
            Network network = binding.getVariable("network")
            Switch sw = getSwitch(network, id)
            binding.hasVariable(SCRIPT_IS_RUNNING) ? sw : ExpressionHelper.newNetworkComponent(id, NetworkComponentNode.ComponentType.SWITCH)
        }
    }

    static void prepareClosures(Binding binding) {
        bindNetwork(binding)

        binding.actionTaken = { id ->
            ExpressionHelper.newActionTaken(id)
        }

        binding.contingencyOccurred = { id = null ->
            ExpressionHelper.newContingencyOccured(id)
        }

        binding.loadingRank = { branchIdToRank, branchIds ->
            def branchIdToRankNode = createExpressionNode(branchIdToRank)
            def branchIdNodes = branchIds.collect({ id -> createExpressionNode(id) })
            ExpressionHelper.newLoadingRank(branchIdToRankNode, branchIdNodes)
        }

        binding.mostLoaded = { branchIds ->
            ExpressionHelper.newMostLoaded(branchIds)
        }

        binding.isOverloaded = {branchIds, limitReduction = 1 as float ->
            ExpressionHelper.newIsOverloadedNode(branchIds, limitReduction)
        }

        NetworkNode.metaClass.propertyMissing = { String name ->
            ExpressionHelper.newNetworkProperty(delegate, name)
        }

        NetworkNode.metaClass.methodMissing = { String name, args ->
            ExpressionHelper.newNetworkMethod(delegate, name, args)
        }

        // operator overloading

        for (op in [['plus', 'PLUS'],
                    ['minus', 'MINUS'],
                    ['multiply', 'MULTIPLY'],
                    ['div', 'DIVIDE']]) {
            def op0 = op[0]
            def op1 = op[1]

            // integer
            ExpressionNode.metaClass."$op0" = { Integer value ->
                ExpressionHelper.newArithmeticBinaryOperator(delegate, ExpressionHelper.newIntegerLiteral(value), ArithmeticBinaryOperator."$op1")
            }

            Integer.metaClass."$op0" = { ExpressionNode value ->
                ExpressionHelper.newArithmeticBinaryOperator(ExpressionHelper.newIntegerLiteral(delegate), value, ArithmeticBinaryOperator."$op1")
            }

            // float
            ExpressionNode.metaClass."$op0" = { Float value ->
                ExpressionHelper.newArithmeticBinaryOperator(delegate, ExpressionHelper.newFloatLiteral(value), ArithmeticBinaryOperator."$op1")
            }

            Float.metaClass."$op0" = { ExpressionNode value ->
                ExpressionHelper.newArithmeticBinaryOperator(ExpressionHelper.newFloatLiteral(delegate), value, ArithmeticBinaryOperator."$op1")
            }

            // double
            ExpressionNode.metaClass."$op0" = { Double value ->
                ExpressionHelper.newArithmeticBinaryOperator(delegate, ExpressionHelper.newDoubleLiteral(value), ArithmeticBinaryOperator."$op1")
            }

            Double.metaClass."$op0" = { ExpressionNode value ->
                ExpressionHelper.newArithmeticBinaryOperator(ExpressionHelper.newDoubleLiteral(delegate), value, ArithmeticBinaryOperator."$op1")
            }

            // big decimal
            ExpressionNode.metaClass."$op0" = { BigDecimal value ->
                ExpressionHelper.newArithmeticBinaryOperator(delegate, ExpressionHelper.newBigDecimalLiteral(value), ArithmeticBinaryOperator."$op1")
            }

            BigDecimal.metaClass."$op0" = { ExpressionNode value ->
                ExpressionHelper.newArithmeticBinaryOperator(ExpressionHelper.newBigDecimalLiteral(delegate), value, ArithmeticBinaryOperator."$op1")
            }

            NetworkPropertyNode.metaClass."$op0" = { ExpressionNode value ->
                ExpressionHelper.newArithmeticBinaryOperator(delegate, value, ArithmeticBinaryOperator."$op1")
            }
        }

        // comparison

        java.lang.Object.metaClass.compareTo2 = { Object value, String op ->
            switch (op) {
                case ">":
                    return delegate > value
                case ">=":
                    return delegate >= value
                case "<":
                    return delegate < value
                case "<=":
                    return delegate <= value
                case "==":
                    return delegate == value
                case "!=":
                    return delegate != value
                default:
                    throw new AssertionError()
            }
        }

        ExpressionNode.metaClass.compareTo2 = { ExpressionNode value, String op ->
            nodeCompareToNode(delegate, value, op)
        }
        ExpressionNode.metaClass.compareTo2 = { Integer value, String op ->
            nodeCompareToNode(delegate, ExpressionHelper.newIntegerLiteral(value), op)
        }
        Integer.metaClass.compareTo2 = { ExpressionNode value, String op ->
            nodeCompareToNode(ExpressionHelper.newIntegerLiteral(delegate), value, op)
        }
        ExpressionNode.metaClass.compareTo2 = { Float value, String op ->
            nodeCompareToNode(delegate, ExpressionHelper.newFloatLiteral(value), op)
        }
        Float.metaClass.compareTo2 = { ExpressionNode value, String op ->
            nodeCompareToNode(ExpressionHelper.newFloatLiteral(delegate), value, op)
        }
        ExpressionNode.metaClass.compareTo2 = { Double value, String op ->
            nodeCompareToNode(delegate, ExpressionHelper.newDoubleLiteral(value), op)
        }
        Double.metaClass.compareTo2 = { ExpressionNode value, String op ->
            nodeCompareToNode(ExpressionHelper.newDoubleLiteral(delegate), value, op)
        }
        ExpressionNode.metaClass.compareTo2 = { BigDecimal value, String op ->
            nodeCompareToNode(delegate, ExpressionHelper.newBigDecimalLiteral(value), op)
        }
        BigDecimal.metaClass.compareTo2 = { ExpressionNode value, String op ->
            nodeCompareToNode(ExpressionHelper.newBigDecimalLiteral(delegate), value, op)
        }

        // boolean
        Boolean.metaClass.and2 = { Boolean value ->
            delegate && value
        }
        ExpressionNode.metaClass.and2 = { Boolean value ->
            ExpressionHelper.newLogicalBinaryOperator(delegate, ExpressionHelper.newBooleanLiteral(value), LogicalBinaryOperator.AND)
        }
        Boolean.metaClass.and2 = { ExpressionNode value ->
            ExpressionHelper.newLogicalBinaryOperator(ExpressionHelper.newBooleanLiteral(delegate), value, LogicalBinaryOperator.AND)
        }
        ExpressionNode.metaClass.and2 = { ExpressionNode value ->
            ExpressionHelper.newLogicalBinaryOperator(delegate, value, LogicalBinaryOperator.AND)
        }

        Boolean.metaClass.or2 = { Boolean value ->
            delegate || value
        }
        ExpressionNode.metaClass.or2 = { Boolean value ->
            ExpressionHelper.newLogicalBinaryOperator(delegate, ExpressionHelper.newBooleanLiteral(value), LogicalBinaryOperator.OR)
        }
        Boolean.metaClass.or2 = { ExpressionNode value ->
            ExpressionHelper.newLogicalBinaryOperator(ExpressionHelper.newBooleanLiteral(delegate), value, LogicalBinaryOperator.OR)
        }
        ExpressionNode.metaClass.or2 = { ExpressionNode value ->
            ExpressionHelper.newLogicalBinaryOperator(delegate, value, LogicalBinaryOperator.OR)
        }

        Boolean.metaClass.not = {
            !delegate
        }
        ExpressionNode.metaClass.not = {
            ExpressionHelper.newLogicalNotOperator(delegate)
        }
    }

    private static ExpressionNode nodeCompareToNode(ExpressionNode left, ExpressionNode right, String op) {
        switch (op) {
            case ">":
                return ExpressionHelper.newComparisonOperator(left, right, ComparisonOperator.GREATER_THAN)
            case ">=":
                return ExpressionHelper.newComparisonOperator(left, right, ComparisonOperator.GREATER_THAN_OR_EQUALS_TO)
            case "<":
                return ExpressionHelper.newComparisonOperator(left, right, ComparisonOperator.LESS_THAN)
            case "<=":
                return ExpressionHelper.newComparisonOperator(left, right, ComparisonOperator.LESS_THAN_OR_EQUALS_TO)
            case "==":
                return ExpressionHelper.newComparisonOperator(left, right, ComparisonOperator.EQUALS)
            case "!=":
                return ExpressionHelper.newComparisonOperator(left, right, ComparisonOperator.NOT_EQUALS)
            default:
                throw new AssertionError()
        }
    }

    private static ExpressionNode createExpressionNode(Object value) {
        if (value instanceof ExpressionNode) {
            value
        } else if (value instanceof Integer){
            ExpressionHelper.newIntegerLiteral(value)
        } else if (value instanceof Float){
            ExpressionHelper.newFloatLiteral(value)
        } else if (value instanceof Double){
            ExpressionHelper.newDoubleLiteral(value)
        } else if (value instanceof BigDecimal){
            ExpressionHelper.newBigDecimalLiteral(value)
        } else if (value instanceof Boolean) {
            ExpressionHelper.newBooleanLiteral(value)
        } else if (value instanceof String) {
            ExpressionHelper.newStringLiteral(value)
        } else {
            throw new AssertionError(value?.getClass())
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
                createExpressionNode(value)
            }
        } catch (CompilationFailedException e) {
            throw new ActionDslException(e.getMessage(), e)
        }
    }
}
