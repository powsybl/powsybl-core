/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

import groovy.lang.Closure;

import java.util.Objects;

/**
 * A generic script condition, in particular useful for prototyping.
 * Variables "network" and "contingency" are accessible to the script.
 *
 * For instance you may define a condition on a particular line of the network that way:
 *
 * <pre>
 *     myCondition = isTrue({ network.getLine('NHV1_NHV2_1').terminal1.p > 0 })
 * </pre>
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class ScriptNode implements ExpressionNode {

    private final Closure script;

    public ScriptNode(Closure<Boolean> script) {
        this.script = Objects.requireNonNull(script);
    }

    public Closure<Boolean> getScript() {
        return script;
    }

    @Override
    public <R, A> R accept(ExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visitScript(this, arg);
    }
}
