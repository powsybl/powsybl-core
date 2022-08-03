/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.InjectionAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CreateFeederBayBuilder {

    private InjectionAdder injectionAdder = null;
    private String voltageLevelId = null;
    private String bbsId = null;
    private Integer injectionPositionOrder = null;
    private ConnectablePosition.Direction injectionDirection = ConnectablePosition.Direction.BOTTOM;

    public CreateFeederBay build() {
        if (bbsId != null) {
            return new CreateFeederBay(injectionAdder, voltageLevelId, bbsId, injectionPositionOrder, injectionDirection);
        } else {
            return new CreateFeederBay(injectionAdder, voltageLevelId, injectionPositionOrder, injectionDirection);
        }
    }

    public CreateFeederBayBuilder withInjectionAdder(InjectionAdder injectionAdder) {
        this.injectionAdder = injectionAdder;
        return this;
    }

    public CreateFeederBayBuilder withVoltageLevelId(String voltageLevelId) {
        this.voltageLevelId = voltageLevelId;
        return this;
    }

    public CreateFeederBayBuilder withBbsId(String bbsId) {
        this.bbsId = bbsId;
        return this;
    }

    public CreateFeederBayBuilder withInjectionPositionOrder(int injectionPositionOrder) {
        this.injectionPositionOrder = injectionPositionOrder;
        return this;
    }

    public CreateFeederBayBuilder withInjectionDirection(ConnectablePosition.Direction injectionDirection) {
        this.injectionDirection = injectionDirection;
        return this;
    }

}
