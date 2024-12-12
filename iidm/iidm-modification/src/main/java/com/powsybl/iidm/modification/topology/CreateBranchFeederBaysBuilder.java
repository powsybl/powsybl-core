/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.BranchAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class CreateBranchFeederBaysBuilder {

    private BranchAdder<?, ?> branchAdder = null;
    private String busOrBbs1 = null;
    private String busOrBbs2 = null;
    private Integer positionOrder1 = null;
    private Integer positionOrder2 = null;
    private String feederName1 = null;
    private String feederName2 = null;
    private ConnectablePosition.Direction direction1 = ConnectablePosition.Direction.TOP;
    private ConnectablePosition.Direction direction2 = ConnectablePosition.Direction.TOP;
    private boolean forceExtensionCreation1 = false;
    private boolean forceExtensionCreation2 = false;

    public CreateBranchFeederBays build() {
        return new CreateBranchFeederBays(branchAdder, busOrBbs1, busOrBbs2, positionOrder1, positionOrder2, feederName1, feederName2, direction1, direction2, forceExtensionCreation1, forceExtensionCreation2);
    }

    public CreateBranchFeederBaysBuilder withBranchAdder(BranchAdder<?, ?> branchAdder) {
        this.branchAdder = branchAdder;
        return this;
    }

    /**
     * @deprecated Use {@link #withBusOrBusbarSectionId1(String)} instead.
     */
    @Deprecated(since = "5.1.0")
    public CreateBranchFeederBaysBuilder withBbs1(String bbs1) {
        return withBusOrBusbarSectionId1(bbs1);
    }

    public CreateBranchFeederBaysBuilder withBusOrBusbarSectionId1(String busOrBbs1) {
        this.busOrBbs1 = busOrBbs1;
        return this;
    }

    /**
     * @deprecated Use {@link #withBusOrBusbarSectionId2(String)} instead.
     */
    @Deprecated(since = "5.1.0")
    public CreateBranchFeederBaysBuilder withBbsId2(String bbsId2) {
        return withBusOrBusbarSectionId2(bbsId2);
    }

    public CreateBranchFeederBaysBuilder withBusOrBusbarSectionId2(String busOrBbs2) {
        this.busOrBbs2 = busOrBbs2;
        return this;
    }

    /**
     * Set position order for end 1.
     * Should not be defined if voltage level attached to end 1 is BUS_BREAKER (ignored if it is).
     * Required if the latter is NODE_BREAKER.
     */
    public CreateBranchFeederBaysBuilder withPositionOrder1(int positionOrder1) {
        this.positionOrder1 = positionOrder1;
        return this;
    }

    /**
     * Set position orders for end 2.
     * Should not be defined if voltage level attached to end 2 is BUS_BREAKER (ignored if it is).
     * Required if the latter is NODE_BREAKER.
     */
    public CreateBranchFeederBaysBuilder withPositionOrder2(int positionOrder2) {
        this.positionOrder2 = positionOrder2;
        return this;
    }

    public CreateBranchFeederBaysBuilder withFeederName1(String feederName1) {
        this.feederName1 = feederName1;
        return this;
    }

    public CreateBranchFeederBaysBuilder withFeederName2(String feederName2) {
        this.feederName2 = feederName2;
        return this;
    }

    public CreateBranchFeederBaysBuilder withDirection1(ConnectablePosition.Direction direction1) {
        this.direction1 = direction1;
        return this;
    }

    public CreateBranchFeederBaysBuilder withDirection2(ConnectablePosition.Direction direction2) {
        this.direction2 = direction2;
        return this;
    }

    public CreateBranchFeederBaysBuilder withForceExtensionCreation1(boolean forceExtensionCreation1) {
        this.forceExtensionCreation1 = forceExtensionCreation1;
        return this;
    }

    public CreateBranchFeederBaysBuilder withForceExtensionCreation2(boolean forceExtensionCreation2) {
        this.forceExtensionCreation2 = forceExtensionCreation2;
        return this;
    }
}
