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
 * Sensitivity variable on a voltage target at a regulating terminal
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class TargetV extends SensitivityVariable {

    @JsonProperty("variableId")
    private final String regulatingTerminalId;

    /**
     * Constructor
     *
     * @param id unique identifier of the variable
     * @param name readable name of the variable
     * @param regulatingTerminalId id of the network regulating terminal which targetV increase is used as sensitivity variable
     * @throws NullPointerException if regulatingTerminalId is null
     */
    @JsonCreator
    public TargetV(@JsonProperty("id") String id,
                   @JsonProperty("name") String name,
                   @JsonProperty("regulatingTerminalId") String regulatingTerminalId) {
        super(id, name);
        this.regulatingTerminalId = Objects.requireNonNull(regulatingTerminalId);
    }

    /**
     * Get the id of the regulating terminal composing the sensitivity variable
     *
     * @return the id of the regulating terminal
     */
    public String getRegulatingTerminalId() {
        return regulatingTerminalId;
    }
}
