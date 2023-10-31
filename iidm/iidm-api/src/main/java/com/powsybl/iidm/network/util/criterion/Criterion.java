/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util.criterion;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;

/**
 * the purpose of these class is to filter contingencies in a criterion contingency list
 *
 * currently all criterion contingency lists have a criterion on countries, one on nominal voltage,
 * one on properties and one on regex.
 *
 * there are criteria to select one and two countries and to select one two
 * or three voltage levels (especially for three windings transformers)
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public interface Criterion {

    enum CriterionType {
        SINGLE_NOMINAL_VOLTAGE,
        TWO_NOMINAL_VOLTAGE,
        THREE_NOMINAL_VOLTAGE,
        PROPERTY,
        SINGLE_COUNTRY,
        TWO_COUNTRY,
        REGEX
    }

    CriterionType getType();

    default boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        return false;
    }
}
