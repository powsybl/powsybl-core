/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class BranchBasedBusRef implements BusRef {

    private final Branch branch;

    private final Branch.Side side;

    public BranchBasedBusRef(Branch branch, Branch.Side side) {
        this.branch = Objects.requireNonNull(branch);
        this.side = Objects.requireNonNull(side);
    }

    @Override
    public Optional<Bus> resolve() {
        Terminal terminal;
        switch (side) {
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
}
