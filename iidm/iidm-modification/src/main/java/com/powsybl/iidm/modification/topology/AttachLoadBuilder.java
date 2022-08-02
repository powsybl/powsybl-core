/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.LoadAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class AttachLoadBuilder {

    private LoadAdder loadAdder = null;
    private String voltageLevelId = null;
    private String bbsId = null;
    private Integer loadPositionOrder = null;
    private ConnectablePosition.Direction loadDirection = ConnectablePosition.Direction.BOTTOM;

    public AttachLoad build() {
        if (bbsId != null) {
            return new AttachLoad(loadAdder, voltageLevelId, bbsId, loadPositionOrder, loadDirection);
        } else {
            return new AttachLoad(loadAdder, voltageLevelId, loadPositionOrder, loadDirection);
        }
    }

    public AttachLoadBuilder withLoadAdder(LoadAdder loadAdder) {
        this.loadAdder = loadAdder;
        return this;
    }

    public AttachLoadBuilder withVoltageLevelId(String voltageLevelId) {
        this.voltageLevelId = voltageLevelId;
        return this;
    }

    public AttachLoadBuilder withBbsId(String bbsId) {
        this.bbsId = bbsId;
        return this;
    }

    public AttachLoadBuilder withLoadPositionOrder(int loadPositionOrder) {
        this.loadPositionOrder = loadPositionOrder;
        return this;
    }

    public AttachLoadBuilder withLoadDirection(ConnectablePosition.Direction loadDirection) {
        this.loadDirection = loadDirection;
        return this;
    }

}
