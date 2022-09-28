/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.criterion;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;

import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class PropertyCriterion implements Criterion {
    private final String propertyKey;
    private final String propertyValue;

    public PropertyCriterion(String propertyKey, String propertyValue) {
        this.propertyKey = Objects.requireNonNull(propertyKey);
        this.propertyValue = Objects.requireNonNull(propertyValue);
    }

    @Override
    public CriterionType getType() {
        return CriterionType.PROPERTY;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        return identifiable.hasProperty(propertyKey) && identifiable.getProperty(propertyKey).equals(propertyValue);
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public String getPropertyValue() {
        return propertyValue;
    }
}
