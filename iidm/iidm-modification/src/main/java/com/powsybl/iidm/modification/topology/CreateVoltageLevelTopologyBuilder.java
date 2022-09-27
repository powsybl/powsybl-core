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

    /**
     * Set the voltage level ID in which the symmetrical topology will be created.
     * If the voltage level does not exist, an exception is thrown or the modification is ignored.
     *
     * @param voltageLevelId
     */
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

    /**
     * Set the number of busbar which will be created.
     *
     * @param busbarCount
     */
    public CreateVoltageLevelTopologyBuilder withBusbarCount(int busbarCount) {
        this.busbarCount = busbarCount;
        return this;
    }

    /**
     * Set the number of sections for each created busbar.
     *
     * @param sectionCount
     */
    public CreateVoltageLevelTopologyBuilder withSectionCount(int sectionCount) {
        this.sectionCount = sectionCount;
        return this;
    }

    /**
     * Set the prefix ID for the created busbar sections. By default, it is equals to the voltage level ID.
     *
     * @param busbarSectionPrefixId
     */
    public CreateVoltageLevelTopologyBuilder withBusbarSectionPrefixId(String busbarSectionPrefixId) {
        this.busbarSectionPrefixId = busbarSectionPrefixId;
        return this;
    }

    /**
     * Set the prefix ID for the created switches. By default, it is equals to the voltage level ID.
     *
     * @param switchPrefixId
     */
    public CreateVoltageLevelTopologyBuilder withSwitchPrefixId(String switchPrefixId) {
        this.switchPrefixId = switchPrefixId;
        return this;
    }

    /**
     * Set the switching devices between the sections of a busbar.
     * There are as many specified switch kinds as there are intervals between sections i.e. <code>sectionCount - 1</code>.
     * The switch kinds can be {@link SwitchKind#BREAKER}, {@link SwitchKind#DISCONNECTOR} or null.
     * If it is {@link SwitchKind#BREAKER}, a closed disconnector, a closed breaker and a closed disconnector are created.
     * If it is {@link SwitchKind#DISCONNECTOR}, a closed disconnector is created.
     * If it is null, no switch is created: the sections are disconnected.
     *
     * @param switchKinds
     */
    public CreateVoltageLevelTopologyBuilder withSwitchKinds(SwitchKind... switchKinds) {
        this.switchKinds = Arrays.asList(switchKinds);
        return this;
    }

    /**
     * Set the switching devices between the sections of a busbar.
     * There are as many specified switch kinds as there are intervals between sections i.e. <code>sectionCount - 1</code>.
     * The switch kinds can be {@link SwitchKind#BREAKER}, {@link SwitchKind#DISCONNECTOR} or null.
     * If it is {@link SwitchKind#BREAKER}, a closed disconnector, a closed breaker and a closed disconnector are created.
     * If it is {@link SwitchKind#DISCONNECTOR}, a closed disconnector is created.
     * If it is null, no switch is created: the sections are disconnected.
     *
     * @param switchKinds
     */
    public CreateVoltageLevelTopologyBuilder withSwitchKinds(List<SwitchKind> switchKinds) {
        this.switchKinds = switchKinds != null ? new ArrayList<>(switchKinds) : null;
        return this;
    }

    public CreateVoltageLevelTopology build() {
        return new CreateVoltageLevelTopology(voltageLevelId, busbarCount, sectionCount, busbarSectionPrefixId, switchPrefixId, switchKinds);
    }
}
