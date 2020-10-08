/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors.functions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.sensitivity.SensitivityFunction;

import java.util.Objects;

/**
 * Sensitivity function on a network branch intensity
 * Only available in AC computation mode
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class BranchIntensity extends SensitivityFunction {

    @JsonProperty("branchId")
    private final String branchId;

    /**
     * Constructor
     *
     * @param id       unique identifier of the function
     * @param name     readable name of the function
     * @param branchId id of the network branch which intensity is used as sensitivity function
     * @throws NullPointerException if branchId is null
     */
    @JsonCreator
    public BranchIntensity(@JsonProperty("id") String id,
                           @JsonProperty("name") String name,
                           @JsonProperty("branchId") String branchId) {
        super(id, name);
        this.branchId = Objects.requireNonNull(branchId);
    }

    /**
     * Get the id of the network branch composing the sensitivity function
     *
     * @return the id of the network branch
     */
    public String getBranchId() {
        return branchId;
    }
}
