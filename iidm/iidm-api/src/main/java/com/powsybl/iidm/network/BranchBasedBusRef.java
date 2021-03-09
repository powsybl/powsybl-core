/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class BranchBasedBusRef implements BusRef {

    private final String branchId;

    private final String side;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BranchBasedBusRef(@JsonProperty("branchId") String branchId, @JsonProperty("side") String side) {
        this.branchId = branchId;
        this.side = side;
    }

    @Override
    public Optional<Bus> resolve(Network network) {
        Branch branch = network.getBranch(branchId);
        Terminal terminal;
        switch (Branch.Side.valueOf(side)) {
            case ONE:
                terminal = branch.getTerminal1();
                break;
            case TWO:
                terminal = branch.getTerminal2();
                break;
            default:
                throw new AssertionError("Unexpected side: " + side);
        }
        return Optional.ofNullable(terminal.getBusView().getBus());
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
        if (!(o instanceof BranchBasedBusRef)) {
            return false;
        }

        BranchBasedBusRef that = (BranchBasedBusRef) o;

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
