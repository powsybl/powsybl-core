/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors.variables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.sensitivity.SensitivityVariable;

import java.util.Objects;

/**
 * Sensitivity variable on injection active power increase
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class InjectionIncrease extends SensitivityVariable {

    @JsonProperty("injectionId")
    private final String injectionId;
    /**
     * Constructor
     *
     * @param id unique identifier of the variable
     * @param name readable name of the variable
     * @param injectionId id of the network injection which active power increase is used as sensitivity variable
     * @throws NullPointerException if injectionId is null
     */
    @JsonCreator
    public InjectionIncrease(@JsonProperty("id") String id,
                             @JsonProperty("name") String name,
                             @JsonProperty("injectionId") String injectionId) {
        super(id, name);
        this.injectionId = Objects.requireNonNull(injectionId);
    }

    /**
     * Get the id of the injection composing the sensitivity variable
     *
     * @return the id of the injection
     */
    public String getInjectionId() {
        return injectionId;
    }
}
