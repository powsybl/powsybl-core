/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.SwitchKind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateVoltageLevelTopologyBuilder {

    private String voltageLevelId = null;

    private Integer busbarCount = null;
    private Integer sectionCount = null;

    private String busbarSectionPrefixId = null;
    private String switchPrefixId = null;

    private List<SwitchKind> switchKinds = null;

    public CreateVoltageLevelTopologyBuilder withVoltageLevelId(String voltageLevelId) {
        this.voltageLevelId = voltageLevelId;
        if (busbarSectionPrefixId == null) {
            busbarSectionPrefixId = voltageLevelId;
        }
        if (switchPrefixId == null) {
            switchPrefixId = voltageLevelId;
        }
        return this;
    }

    public CreateVoltageLevelTopologyBuilder withBusbarCount(int busbarCount) {
        this.busbarCount = busbarCount;
        return this;
    }

    public CreateVoltageLevelTopologyBuilder withSectionCount(int sectionCount) {
        this.sectionCount = sectionCount;
        return this;
    }

    public CreateVoltageLevelTopologyBuilder withBusbarSectionPrefixId(String busbarSectionPrefixId) {
        this.busbarSectionPrefixId = busbarSectionPrefixId;
        return this;
    }

    public CreateVoltageLevelTopologyBuilder withSwitchPrefixId(String switchPrefixId) {
        this.switchPrefixId = switchPrefixId;
        return this;
    }

    public CreateVoltageLevelTopologyBuilder withSwitchKinds(SwitchKind... switchKinds) {
        this.switchKinds = Arrays.asList(switchKinds);
        return this;
    }

    public CreateVoltageLevelTopologyBuilder withSwitchKinds(List<SwitchKind> switchKinds) {
        this.switchKinds = switchKinds != null ? new ArrayList<>(switchKinds) : null;
        return this;
    }

    public CreateVoltageLevelTopology build() {
        return new CreateVoltageLevelTopology(voltageLevelId, busbarCount, sectionCount, busbarSectionPrefixId, switchPrefixId, switchKinds);
    }
}
