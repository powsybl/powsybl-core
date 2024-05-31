/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.parameters;

import com.google.common.collect.ImmutableSet;

import java.util.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class ParameterUsageRestrictions {
    private final Map<String, Set<Object>> acceptableValuesPerParameterName;

    ParameterUsageRestrictions(Map<String, Set<Object>> acceptableValuesPerParameterName) {
        this.acceptableValuesPerParameterName = new HashMap<>(acceptableValuesPerParameterName);
    }

    public Set<String> getParametersWithRestrictions() {
        return Set.copyOf(acceptableValuesPerParameterName.keySet());
    }

    public Set<Object> getValuesForParameter(String parameter) {
        return Set.copyOf(acceptableValuesPerParameterName.get(parameter));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, Set<Object>> acceptableValuesPerParameter = new HashMap<>();

        private Builder() { }

        public Builder add(String parameterName, Object value) {
            acceptableValuesPerParameter.put(parameterName, Set.of(value));
            return this;
        }

        public Builder add(String parameterName, Set<Object> values) {
            acceptableValuesPerParameter.put(parameterName, ImmutableSet.copyOf(values));
            return this;
        }

        public ParameterUsageRestrictions build() {
            return new ParameterUsageRestrictions(acceptableValuesPerParameter);
        }
    }

    public static class Adder {
        private final Parameter.Builder parameterBuilder;
        private final ParameterUsageRestrictions.Builder builder;

        protected Adder(Parameter.Builder parameterBuilder) {
            this.parameterBuilder = parameterBuilder;
            this.builder = new ParameterUsageRestrictions.Builder();
        }

        public Adder withRestriction(String parameterName, Object value) {
            builder.add(parameterName, value);
            return this;
        }

        public Adder withRestriction(String parameterName, Set<Object> values) {
            builder.add(parameterName, values);
            return this;
        }

        public Parameter.Builder add() {
            return parameterBuilder.withUsageRestrictions(builder.build());
        }
    }
}
