/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.SwitchKind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class CreateVoltageLevelTopologyBuilder {

    private String voltageLevelId = null;

    private int lowBusOrBusbarIndex = 1;
    private Integer alignedBusesOrBusbarCount = null;
    private int lowSectionIndex = 1;
    private Integer sectionCount = null;

    private String busOrBusbarSectionPrefixId = null;
    private String switchPrefixId = null;

    private List<SwitchKind> switchKinds = Collections.emptyList();

    /**
     * Set the voltage level ID in which the symmetrical topology will be created.
     * If the voltage level does not exist, an exception is thrown or the modification is ignored.
     *
     * @param voltageLevelId
     */
    public CreateVoltageLevelTopologyBuilder withVoltageLevelId(String voltageLevelId) {
        this.voltageLevelId = voltageLevelId;
        if (busOrBusbarSectionPrefixId == null) {
            busOrBusbarSectionPrefixId = voltageLevelId;
        }
        if (switchPrefixId == null) {
            switchPrefixId = voltageLevelId;
        }
        return this;
    }

    /**
     * Set the lowest index of bus or busbar index that will be created (1 by default).
     *
     * @param lowBusOrBusbarIndex
     */
    public CreateVoltageLevelTopologyBuilder withLowBusOrBusbarIndex(int lowBusOrBusbarIndex) {
        this.lowBusOrBusbarIndex = lowBusOrBusbarIndex;
        return this;
    }

    /**
     * Set the number of parallel bus lines or busbar which will be created.
     * In case of node/breaker topology, it is the number of busbar sections.
     * In case of bus/breaker topology, it is the number of lines of aligned buses.
     *
     * @param alignedBusesOrBusbarCount
     */
    public CreateVoltageLevelTopologyBuilder withAlignedBusesOrBusbarCount(int alignedBusesOrBusbarCount) {
        this.alignedBusesOrBusbarCount = alignedBusesOrBusbarCount;
        return this;
    }

    /**
     * Set the lowest index of section index that will be created (1 by default).
     *
     * @param lowSectionIndex
     */
    public CreateVoltageLevelTopologyBuilder withLowSectionIndex(int lowSectionIndex) {
        this.lowSectionIndex = lowSectionIndex;
        return this;
    }

    /**
     * Set the number of sections for each created busbar or the number of switches between the buses in bus/breaker topology.
     *
     * @param sectionCount
     */
    public CreateVoltageLevelTopologyBuilder withSectionCount(int sectionCount) {
        this.sectionCount = sectionCount;
        return this;
    }

    /**
     * Set the prefix ID for the created buses or busbar sections. By default, it is equals to the voltage level ID.
     *
     * @param busbarSectionPrefixId
     */
    public CreateVoltageLevelTopologyBuilder withBusbarSectionPrefixId(String busbarSectionPrefixId) {
        this.busOrBusbarSectionPrefixId = busbarSectionPrefixId;
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
     * In bus/breaker topology, all the switching devices are by default breakers.
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
     * In bus/breaker topology, all the switching devices are by default breakers.
     *
     * @param switchKinds
     */
    public CreateVoltageLevelTopologyBuilder withSwitchKinds(List<SwitchKind> switchKinds) {
        this.switchKinds = switchKinds != null ? new ArrayList<>(switchKinds) : null;
        return this;
    }

    public CreateVoltageLevelTopology build() {
        return new CreateVoltageLevelTopology(voltageLevelId, lowBusOrBusbarIndex, alignedBusesOrBusbarCount, lowSectionIndex, sectionCount, busOrBusbarSectionPrefixId, switchPrefixId, switchKinds);
    }
}
