/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl


import com.powsybl.dsl.DslException
import com.powsybl.dsl.DslLoader
import com.powsybl.dsl.ExpressionDslLoader
import com.powsybl.dsl.ast.ExpressionNode
import com.powsybl.security.LimitViolationDetector
import com.powsybl.security.dsl.ast.LimitsVariableNode
import org.codehaus.groovy.control.CompilationFailedException

import java.util.function.Consumer

/**
 * Implementation of the current limits DSL.
 * Defines the methods to define factors for specified current limits, for instance:
 *
 * <pre>
 * current_limits {
 *     N_situation {
 *         permanent {
 *             factor 0.95
 *         }
 *
 *         temporary {
 *             factor 1.0
 *         }
 *     }
 *
 *     contingency('contingency1') {
 *       factor 0.99
 *     }
 *
 *     any_contingency {
 *         factor 0.98
 *     }
 * }
 * </pre>
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
class LimitFactorsLoader extends DslLoader {


    LimitFactorsLoader(GroovyCodeSource dslSrc) {
        super(dslSrc)
    }

    LimitFactorsLoader(File dslFile) {
        super(dslFile)
    }

    LimitFactorsLoader(String script) {
        super(script)
    }

    static class LimitSpec {

        /**
         * Variable which may be referenced as "voltage" from the DSL
         */
        static LimitsVariableNode voltage = LimitsVariableNode.voltage()
        /**
         * Variable which may be referenced as "duration" from the DSL
         */
        static LimitsVariableNode duration =  LimitsVariableNode.duration()

        LimitFactorsNode node

        LimitSpec(LimitMatcher selector) {
            this.node = new ConditionNode(selector)
        }

        LimitSpec() {
            this.node = new LimitFactorsNode()
        }

        void where(ExpressionNode node, @DelegatesTo(LimitSpec) Closure cl) {
            executeClosure new ExpressionLimitMatcher(node), cl
        }

        void factor(float f) {
            node.addChild(new FinalFactorNode(f))
        }

        void executeClosure(LimitMatcher selector, @DelegatesTo(LimitSpec) Closure cl) {
            def cloned = cl.clone()
            cloned.resolveStrategy = Closure.DELEGATE_FIRST

            LimitSpec enclosedSpec = new LimitSpec(selector)
            cloned.delegate = enclosedSpec
            cloned()
            node.addChild(enclosedSpec.node)
        }

        void temporary(@DelegatesTo(LimitSpec) Closure cl) {
            executeClosure LimitMatchers.temporary(), cl
        }

        void permanent(@DelegatesTo(LimitSpec) Closure cl) {
            executeClosure LimitMatchers.permanent(), cl
        }

        void N_situation(@DelegatesTo(LimitSpec) Closure cl) {
            executeClosure LimitMatchers.nSituation(), cl
        }

        void any_contingency( @DelegatesTo(LimitSpec) Closure cl) {
            executeClosure LimitMatchers.anyContingency(), cl
        }

        void contingency(String id, @DelegatesTo(LimitSpec) Closure cl) {
            executeClosure LimitMatchers.contingency(id), cl
        }

        void contingencies(Collection<String> ids, @DelegatesTo(LimitSpec) Closure cl) {
            executeClosure LimitMatchers.contingencies(ids), cl
        }

        void branch(String id, @DelegatesTo(LimitSpec) Closure cl) {
            executeClosure LimitMatchers.branch(id), cl
        }

        void branches(Collection<String> ids, @DelegatesTo(LimitSpec) Closure cl) {
            executeClosure LimitMatchers.branches(ids), cl
        }
    }

    static LimitFactors createFactors(@DelegatesTo(LimitSpec) Closure cl) {
        def cloned = cl.clone()
        cloned.resolveStrategy = Closure.DELEGATE_FIRST

        LimitSpec spec = new LimitSpec()
        cloned.delegate = spec
        cloned()

        return spec.node
    }

    static void loadDsl(Binding binding, Consumer<LimitFactors> handler) {

        ExpressionDslLoader.prepareClosures(binding)

        binding.current_limits = { Closure cl -> handler.accept(createFactors(cl)) }

    }

    LimitFactors loadFactors() {

        LimitFactors root = new LimitFactorsNode()

        try {
            Binding binding = new Binding()

            loadDsl(binding, root.&addChild)

            def shell = createShell(binding)

            shell.evaluate(dslSrc)

            root

        } catch (CompilationFailedException e) {
            throw new DslException(e.getMessage(), e)
        }
    }

    LimitViolationDetector loadDetector() {
        return new LimitViolationDetectorWithFactors(loadFactors())
    }
}
