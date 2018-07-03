/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors.variables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.PowsyblException;
import com.powsybl.sensitivity.SensitivityVariable;

import java.util.Map;
import java.util.Objects;

/**
 * Sensitivity variable on a linear GLSK
 *
 * GLSK for "Generation and Load Shift Keys" describes a combination of power injection
 * shift (positive or negative) on generators and loads.
 *
 * This implementation only deals with "factor" GLSK, that means linear combination
 * of each injection.
 */
public class LinearGlsk extends SensitivityVariable {

    @JsonProperty("glskMap")
    private final Map<String, Float> glskMap;

    /**
     * Constructor
     *
     * @param id unique identifier of the variable
     * @param name readable name of the variable
     * @param glskMap map from GLSK injection to factor
     * @throws RuntimeException if glskCollection is null or empty
     * @TODO: Check for correctness of participation factor?
     */
    @JsonCreator
    public LinearGlsk(@JsonProperty("id") String id,
                      @JsonProperty("name") String name,
                      @JsonProperty("glskMap") Map<String, Float> glskMap) {
        super(id, name);
        this.glskMap = Objects.requireNonNull(glskMap);
        if (glskMap.isEmpty()) {
            throw new PowsyblException("Incorrect GLSK map for sensitivity variable " + id);
        }
    }

    /**
     * Get a map of the device ids included in the GLSK and their given factor
     *
     * @return a map associating the device ids with their factor
     */
    public Map<String, Float> getGLSKs() {
        return glskMap;
    }
}
