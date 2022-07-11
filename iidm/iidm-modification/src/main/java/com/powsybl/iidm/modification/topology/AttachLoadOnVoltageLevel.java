/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;

import java.util.List;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

/**
 * This method adds a new load on an existing voltage level. The voltage level should be described
 * in node/breaker.
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class AttachLoadOnVoltageLevel implements NetworkModification {

    //Load attributes
    private final String loadId;
    private final double p0;
    private final double q0;
    private final LoadType loadType; //add constant

    //VoltageLevel attributes
    private final String voltageLevelId;
    private String bbsId; //Id of the busBar where the switch will be open

    private List<String> bbsIds;

    public AttachLoadOnVoltageLevel(String loadId, double p0, double q0,
                                    LoadType loadType, String voltageLevelId, String bbsId) {
        this.loadId = loadId;
        this.p0 = p0;
        this.q0 = q0;
        this.loadType = loadType;
        this.voltageLevelId = voltageLevelId;
        this.bbsId = bbsId;
    }

    public AttachLoadOnVoltageLevel(String loadId, double p0, double q0,
                                    LoadType loadType, String voltageLevelId, List<String> bbsIds) {
        this.loadId = loadId;
        this.p0 = p0;
        this.q0 = q0;
        this.loadType = loadType;
        this.voltageLevelId = voltageLevelId;
        this.bbsIds = bbsIds;
    }

    public AttachLoadOnVoltageLevel(String loadId, double p0, double q0,
                                    LoadType loadType, String voltageLevelId) {
        this.loadId = loadId;
        this.p0 = p0;
        this.q0 = q0;
        this.loadType = loadType;
        this.voltageLevelId = voltageLevelId;
    }

    public String getLoadId() {
        return loadId;
    }

    public double getP0() {
        return p0;
    }

    public double getQ0() {
        return q0;
    }

    public LoadType getLoadType() {
        return loadType;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public String getBbsId() {
        return bbsId;
    }

    public void setBbsId(String bbsId) {
        this.bbsId = bbsId;
    }

    public List<String> getBbsIds() {
        return bbsIds;
    }

    public void setBbsIds(List<String> bbsIds) {
        this.bbsIds = bbsIds;
    }

    private void createTopologyAutomatically(Network network, VoltageLevel voltageLevel, int loadNode, int forkNode) {
        BusbarSection bbs = network.getBusbarSection(bbsId);
        int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();
        createNodeBreakerSwitches(loadNode, forkNode, bbsNode, loadId, voltageLevel.getNodeBreakerView());
        if (voltageLevel.getNodeBreakerView().getBusbarSectionCount() > 1) {
            BusbarSectionPosition position = bbs.getExtension(BusbarSectionPosition.class);
            voltageLevel.getNodeBreakerView().getBusbarSectionStream().forEach(busbarSection -> {
                if (busbarSection.getExtension(BusbarSectionPosition.class).getSectionIndex() == position.getSectionIndex() && !busbarSection.getId().equals(bbsId)) {
                    createNodeBreakerDisconnector(forkNode, busbarSection.getTerminal().getNodeBreakerView().getNode(),
                            String.valueOf(busbarSection.getId()), loadId, voltageLevel.getNodeBreakerView(), true);
                }
            });
        }
    }

    private void createTopologyFromBusbarList(Network network, VoltageLevel voltageLevel, int loadNode, int forkNode) {
        createNodeBreakerBreaker(loadNode, forkNode, "", loadId, voltageLevel.getNodeBreakerView(), false);
        bbsIds.stream().forEach(id -> {
            BusbarSection bbs = network.getBusbarSection(id);
            int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();
            createNodeBreakerDisconnector(forkNode, bbsNode, String.valueOf(bbsNode), loadId, voltageLevel.getNodeBreakerView(), true);
        });
    }

    private void createTopologyFromVoltageLevel(VoltageLevel voltageLevel, int loadNode, int forkNode) {
        BusbarSection bbs = voltageLevel.getNodeBreakerView().getBusbarSectionStream().reduce((first, second) -> first).orElse(null);
        if (bbs != null) {
            createNodeBreakerSwitches(loadNode, forkNode, bbs.getTerminal().getNodeBreakerView().getNode(), loadId, voltageLevel.getNodeBreakerView());
        }
    }

    @Override
    public void apply(Network network, ComputationManager computationManager) {
        apply(network);
    }

    @Override
    public void apply(Network network) {
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            throw new PowsyblException(String.format("Voltage level %s is not found", voltageLevelId));
        }

        TopologyKind topologyKind = voltageLevel.getTopologyKind();
        if (topologyKind != TopologyKind.NODE_BREAKER) {
            throw new PowsyblException(String.format("Voltage level %s is not in node/breaker.", voltageLevelId));
        }

        //Create the new load
        LoadAdder loadAdder = createLoadAdder(loadType, p0, q0, network.getVoltageLevel(voltageLevelId));

        int loadNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int forkNode = loadNode + 1;
        loadAdder.setNode(loadNode).add();

        //Create a switch and a breaker linking the load to the busbar sections
        if (bbsId != null) {
            createTopologyAutomatically(network, voltageLevel, loadNode, forkNode);
        }

        if (bbsIds != null) {
            createTopologyFromBusbarList(network, voltageLevel, loadNode, forkNode);
        }

        if (bbsIds == null && bbsId == null) {
            createTopologyFromVoltageLevel(voltageLevel, loadNode, forkNode);
        }
    }
}
