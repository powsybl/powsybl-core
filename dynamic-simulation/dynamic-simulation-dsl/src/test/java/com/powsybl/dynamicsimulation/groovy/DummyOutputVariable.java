/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.dynamicsimulation.groovy;

import com.powsybl.dynamicsimulation.OutputVariable;

import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
record DummyOutputVariable(String id, String variable, OutputType type) implements OutputVariable {

    public DummyOutputVariable(String id, String variable, OutputType type) {
        this.id = Objects.requireNonNull(id);
        this.variable = Objects.requireNonNull(variable);
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public String getModelId() {
        return id;
    }

    @Override
    public String getVariableName() {
        return variable;
    }

    @Override
    public OutputType getOutputType() {
        return type;
    }
}
