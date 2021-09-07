/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
 * Sensitivity variable on HVDC active power setpoint
 *
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 */
public class HvdcSetpointIncrease extends SensitivityVariable {

    @JsonProperty("hvdcId")
    private final String hvdcId;

    /**
     * Constructor
     *
     * @param id unique identifier of the variable
     * @param name readable name of the variable
     * @param hvdcId id of the network HVDC line which active power setpoint increase is used as sensitivity variable
     * @throws NullPointerException if hvdcId is null
     */
    @JsonCreator
    public HvdcSetpointIncrease(@JsonProperty("id") String id,
                                @JsonProperty("name") String name,
                                @JsonProperty("hvdcId") String hvdcId) {
        super(id, name);
        this.hvdcId = Objects.requireNonNull(hvdcId);
    }

    /**
     * Get the id of the HVDC line composing the sensitivity variable
     *
     * @return the id of the HVDC line
     */
    public String getHvdcId() {
        return hvdcId;
    }
}
