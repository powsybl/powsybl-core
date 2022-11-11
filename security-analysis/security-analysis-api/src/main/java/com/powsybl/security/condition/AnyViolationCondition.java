/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.condition;

/**
 *
 * simulate the associated action as soon there is any violation
 *
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 *
 */
public class AnyViolationCondition implements Condition {

    public static final String NAME = "ANY_VIOLATION_CONDITION";

    @Override
    public String getType() {
        return NAME;
    }
}
