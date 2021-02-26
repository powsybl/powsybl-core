/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
 * Sensitivity function on a network bus voltage
 * Only available in AC computation mode
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class BusVoltage extends SensitivityFunction {

    @JsonProperty("terminalId")
    private final String terminalId;

    /**
     * Constructor
     *
     * @param id       unique identifier of the function
     * @param name     readable name of the function
     * @param terminalId id of the network terminal which voltage bus is used as sensitivity function
     * @throws NullPointerException if terminalId is null
     */
    @JsonCreator
    public BusVoltage(@JsonProperty("id") String id,
                      @JsonProperty("name") String name,
                      @JsonProperty("terminalId") String terminalId) {
        super(id, name);
        this.terminalId = Objects.requireNonNull(terminalId);
    }

    /**
     * Get the id of the network terminal composing the sensitivity function
     *
     * @return the id of the network terminal
     */
    public String getTerminalId() {
        return terminalId;
    }
}
