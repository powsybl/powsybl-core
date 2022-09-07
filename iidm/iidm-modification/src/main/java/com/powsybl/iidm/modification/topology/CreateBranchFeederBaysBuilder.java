/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.BranchAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateBranchFeederBaysBuilder {

    private BranchAdder<?> branchAdder = null;
    private String bbsId1 = null;
    private String bbsId2 = null;
    private Integer positionOrder1 = null;
    private Integer positionOrder2 = null;
    private ConnectablePosition.Direction direction1 = ConnectablePosition.Direction.TOP;
    private ConnectablePosition.Direction direction2 = ConnectablePosition.Direction.TOP;

    public CreateBranchFeederBays build() {
        return new CreateBranchFeederBays(branchAdder, bbsId1, bbsId2, positionOrder1, positionOrder2, direction1, direction2);
    }

    public CreateBranchFeederBaysBuilder withBranchAdder(BranchAdder<?> branchAdder) {
        this.branchAdder = branchAdder;
        return this;
    }

    public CreateBranchFeederBaysBuilder withBbsId1(String bbsId1) {
        this.bbsId1 = bbsId1;
        return this;
    }

    public CreateBranchFeederBaysBuilder withBbsId2(String bbsId2) {
        this.bbsId2 = bbsId2;
        return this;
    }

    public CreateBranchFeederBaysBuilder withPositionOrder1(int positionOrder1) {
        this.positionOrder1 = positionOrder1;
        return this;
    }

    public CreateBranchFeederBaysBuilder withPositionOrder2(int positionOrder2) {
        this.positionOrder2 = positionOrder2;
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
}
