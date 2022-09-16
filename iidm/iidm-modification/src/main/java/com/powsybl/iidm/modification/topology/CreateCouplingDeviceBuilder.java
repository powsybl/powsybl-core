/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CreateCouplingDeviceBuilder {

    private String busbarSectionId1 = null;

    private String busbarSectionId2 = null;

    public CreateCouplingDevice build() {
        return new CreateCouplingDevice(busbarSectionId1, busbarSectionId2);
    }

    public CreateCouplingDeviceBuilder withBusbarSectionId1(String bbsId1) {
        this.busbarSectionId1 = bbsId1;
        return this;
    }

    public CreateCouplingDeviceBuilder withBusbarSectionId2(String bbsId2) {
        this.busbarSectionId2 = bbsId2;
        return this;
    }

}
