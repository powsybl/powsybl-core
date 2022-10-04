/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.criterion;

import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;

import java.util.List;
import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class PropertyCriterion implements Criterion {
    private final String propertyKey;
    private final List<String> propertyValues;

    public PropertyCriterion(String propertyKey, List<String> propertyValues) {
        this.propertyKey = Objects.requireNonNull(propertyKey);
        this.propertyValues = ImmutableList.copyOf(propertyValues);
    }

    @Override
    public CriterionType getType() {
        return CriterionType.PROPERTY;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        return identifiable.hasProperty(propertyKey) && propertyValues.contains(identifiable.getProperty(propertyKey));
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public List<String> getPropertyValues() {
        return propertyValues;
    }
}
