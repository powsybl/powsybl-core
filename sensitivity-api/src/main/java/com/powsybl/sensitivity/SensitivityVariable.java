/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

/**
 * Variable which impact is to be assessed by sensitivity computation
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
public class SensitivityVariable {

    @JsonProperty("id")
    private final String id;

    @JsonProperty("name")
    private final String name;

    /**
     * Constructor
     *
     * @param id sensitivity variable id
     * @param name sensitivity variable name
     */
    @JsonCreator
    public SensitivityVariable(@JsonProperty("id") String id,
                               @JsonProperty("name") String name) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
    }

    /**
     * Get the unique identifier of the variable
     *
     * @return the unique identifier of the variable
     */
    public String getId() {
        return id;
    }

    /**
     * Get the readable name of the variable
     *
     * @return the readable name of the variable
     */
    public String getName() {
        return name;
    }
}
