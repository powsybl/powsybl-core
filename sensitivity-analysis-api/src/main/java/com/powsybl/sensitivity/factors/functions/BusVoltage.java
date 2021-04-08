/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors.functions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.iidm.network.BusRef;
import com.powsybl.sensitivity.SensitivityFunction;

import java.util.Objects;

/**
 * Sensitivity function on a network bus voltage
 * Only available in AC computation mode
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class BusVoltage extends SensitivityFunction {

    @JsonProperty("busRef")
    private final BusRef busRef;

    /**
     * Constructor
     *
     * @param id       unique identifier of the function
     * @param name     readable name of the function
     * @param busRef   reference to the voltage bus is used as sensitivity function
     * @throws NullPointerException if busRef is null
     */
    @JsonCreator
    public BusVoltage(@JsonProperty("id") String id,
                      @JsonProperty("name") String name,
                      @JsonProperty("busRef") BusRef busRef) {
        super(id, name);
        this.busRef = Objects.requireNonNull(busRef);
    }

    /**
     * Get the reference to the bus composing the sensitivity function
     *
     * @return the bus ref
     */
    public BusRef getBusRef() {
        return busRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BusVoltage)) {
            return false;
        }

        BusVoltage that = (BusVoltage) o;

        return getBusRef().equals(that.getBusRef());
    }

    @Override
    public int hashCode() {
        return getBusRef().hashCode();
    }
}
