/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.InjectionAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class CreateFeederBayBuilder {

    private InjectionAdder<?, ?> injectionAdder = null;
    private String busOrBusbarSection = null;
    private Integer injectionPositionOrder = null;
    private String injectionFeederName = null;
    private ConnectablePosition.Direction injectionDirection = ConnectablePosition.Direction.BOTTOM;
    private boolean logOrThrowIfIncorrectPositionOrder;

    public CreateFeederBay build() {
        return new CreateFeederBay(injectionAdder, busOrBusbarSection, injectionPositionOrder, injectionFeederName, injectionDirection, logOrThrowIfIncorrectPositionOrder);
    }

    public CreateFeederBayBuilder withInjectionAdder(InjectionAdder<?, ?> injectionAdder) {
        this.injectionAdder = injectionAdder;
        return this;
    }

    /**
     * @deprecated Use {@link #withBusOrBusbarSectionId(String)} instead.
     */
    @Deprecated(since = "5.1.0")
    public CreateFeederBayBuilder withBbsId(String bbsId) {
        return withBusOrBusbarSectionId(bbsId);
    }

    public CreateFeederBayBuilder withBusOrBusbarSectionId(String busOrBusbarSection) {
        this.busOrBusbarSection = busOrBusbarSection;
        return this;
    }

    /**
     * Set position order.
     * Should not be defined in BUS_BREAKER (ignored if they are).
     * Required in NODE_BREAKER.
     */
    public CreateFeederBayBuilder withInjectionPositionOrder(int injectionPositionOrder) {
        this.injectionPositionOrder = injectionPositionOrder;
        return this;
    }

    public CreateFeederBayBuilder withInjectionFeederName(String injectionFeederName) {
        this.injectionFeederName = injectionFeederName;
        return this;
    }

    public CreateFeederBayBuilder withInjectionDirection(ConnectablePosition.Direction injectionDirection) {
        this.injectionDirection = injectionDirection;
        return this;
    }

    public CreateFeederBayBuilder withLogOrThrowIfIncorrectPositionOrder(boolean logOrThrowIfIncorrectPositionOrder) {
        this.logOrThrowIfIncorrectPositionOrder = logOrThrowIfIncorrectPositionOrder;
        return this;
    }

}
