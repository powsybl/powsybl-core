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
 * Sensitivity variable on a voltage target increase
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class TargetV extends SensitivityVariable {

    @JsonProperty("equipmentId")
    private final String equipmentId;

    /**
     * Constructor
     *
     * @param id unique identifier of the variable
     * @param name readable name of the variable
     * @param equipmentId id of the network equipment (generator, static var compensator, two windings transformer,
     *                             three windings transformer, shunt compensator, VSC, etc.) which targetV increase is used as sensitivity variable
     * @throws NullPointerException if equipmentId is null
     */
    @JsonCreator
    public TargetV(@JsonProperty("id") String id,
                   @JsonProperty("name") String name,
                   @JsonProperty("equipmentId") String equipmentId) {
        super(id, name);
        this.equipmentId = Objects.requireNonNull(equipmentId);
    }

    /**
     * Get the id of the equipment that is regulating voltage
     *
     * @return the id of the equipment
     */
    public String getEquipmentId() {
        return equipmentId;
    }
}
