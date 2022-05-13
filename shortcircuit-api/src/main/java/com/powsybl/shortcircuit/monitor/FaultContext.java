/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.shortcircuit.monitor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 * <p>
 * provide the context to get information of the network after a short circuit analysis
 * it contains a fault id
 */
public class FaultContext {

    private final String id;

    public FaultContext(@JsonProperty("id") String faultId) {
        this.id = faultId;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FaultContext that = (FaultContext) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FaultContext(" +
            "id='" + Objects.toString(id, "") + '\'' +
            ')';
    }
}
