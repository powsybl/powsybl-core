/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class IdBasedBusBean {

    private final String voltageLevelId;

    private final String busId;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public IdBasedBusBean(@JsonProperty("voltageLevelId") String voltageLevelId, @JsonProperty("busId") String busId) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
        this.busId = Objects.requireNonNull(busId);
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public String getBusId() {
        return busId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IdBasedBusBean)) {
            return false;
        }

        IdBasedBusBean that = (IdBasedBusBean) o;

        if (!getVoltageLevelId().equals(that.getVoltageLevelId())) {
            return false;
        }
        return getBusId().equals(that.getBusId());
    }

    @Override
    public int hashCode() {
        int result = getVoltageLevelId().hashCode();
        result = 31 * result + getBusId().hashCode();
        return result;
    }
}
