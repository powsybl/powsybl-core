/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class BranchBasedBusBean extends AbstractBusRefBean {

    private final String branchId;

    private final String side;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BranchBasedBusBean(@JsonProperty("branchId") String branchId, @JsonProperty("side") String side) {
        this.branchId = branchId;
        this.side = side;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getSide() {
        return side;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BranchBasedBusBean)) {
            return false;
        }

        BranchBasedBusBean that = (BranchBasedBusBean) o;

        if (!getBranchId().equals(that.getBranchId())) {
            return false;
        }
        return getSide().equals(that.getSide());
    }

    @Override
    public int hashCode() {
        int result = getBranchId().hashCode();
        result = 31 * result + getSide().hashCode();
        return result;
    }
}
