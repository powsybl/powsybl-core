/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.dynamicsimulation.groovy;

import com.powsybl.dynamicsimulation.Curve;

import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class DummyCurve implements Curve {

    private final String id;

    private final String variable;

    DummyCurve(String id, String variable) {
        this.id = Objects.requireNonNull(id);
        this.variable = Objects.requireNonNull(variable);
    }

    String getId() {
        return id;
    }

    String getVariable() {
        return variable;
    }

}
