/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.criterion;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public interface Criterion {

    enum CriterionType {
        SINGLE_NOMINAL_VOLTAGE,
        TWO_NOMINAL_VOLTAGE,
        THREE_NOMINAL_VOLTAGE,
        PROPERTY,
        SINGLE_COUNTRY,
        TWO_COUNTRY,
    }

    CriterionType getType();

    default boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        return false;
    }
}
