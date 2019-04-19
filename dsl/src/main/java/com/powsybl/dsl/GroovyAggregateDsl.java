/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dsl;

import com.google.common.collect.ImmutableList;
import groovy.lang.Binding;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A {@link GroovyDsl} which is composed of a collection of DSLs.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class GroovyAggregateDsl<C> implements GroovyDsl<C> {

    private final List<GroovyDsl<C>> parts;

    /**
     * Builds a DSL which is the aggregation of the provided parts.
     * Enabling this DSL is equivalent to enabling all parts.
     *
     * @param parts the DSLs which compose this DSL
     */
    public GroovyAggregateDsl(Collection<? extends GroovyDsl<C>> parts) {
        this.parts = ImmutableList.copyOf(Objects.requireNonNull(parts));
    }

    /**
     * Enables all underlying DSLs.
     */
    public void enable(Binding binding, C context) {
        parts.forEach(dsl -> dsl.enable(binding, context));
    }
}
