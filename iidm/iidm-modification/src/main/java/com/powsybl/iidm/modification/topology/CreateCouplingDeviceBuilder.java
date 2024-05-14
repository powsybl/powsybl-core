/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class CreateCouplingDeviceBuilder {

    private String busOrBbsId1 = null;

    private String busOrBbsId2 = null;

    private String switchPrefixId = null;

    public CreateCouplingDevice build() {
        return new CreateCouplingDevice(busOrBbsId1, busOrBbsId2, switchPrefixId);
    }

    public CreateCouplingDeviceBuilder withBusOrBusbarSectionId1(String busOrBbsId1) {
        this.busOrBbsId1 = busOrBbsId1;
        return this;
    }

    /**
     * @deprecated Use {@link #withBusOrBusbarSectionId1(String)} instead.
     */
    @Deprecated(since = "5.2.0")
    public CreateCouplingDeviceBuilder withBusbarSectionId1(String bbsId1) {
        return withBusOrBusbarSectionId1(bbsId1);
    }

    public CreateCouplingDeviceBuilder withBusOrBusbarSectionId2(String busOrBbsId2) {
        this.busOrBbsId2 = busOrBbsId2;
        return this;
    }

    /**
     * @deprecated Use {@link #withBusOrBusbarSectionId2(String)} instead.
     */
    @Deprecated(since = "5.2.0")
    public CreateCouplingDeviceBuilder withBusbarSectionId2(String bbsId2) {
        return withBusOrBusbarSectionId2(bbsId2);
    }

    public CreateCouplingDeviceBuilder withSwitchPrefixId(String switchPrefixId) {
        this.switchPrefixId = switchPrefixId;
        return this;
    }
}
