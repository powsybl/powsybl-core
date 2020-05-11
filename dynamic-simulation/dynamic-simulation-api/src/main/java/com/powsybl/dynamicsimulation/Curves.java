/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class Curves {

    public Curves(String modelId, String... variables) {
        this(modelId, Arrays.asList(variables));
    }

    public Curves(String modelId, List<String> variables) {
        this.modelId = Objects.requireNonNull(modelId);
        this.variables = new ArrayList<>(Objects.requireNonNull(variables));
    }

    public String getModelId() {
        return modelId;
    }

    public List<String> getVariables() {
        return variables;
    }

    private final String modelId;
    private final List<String> variables;

}
