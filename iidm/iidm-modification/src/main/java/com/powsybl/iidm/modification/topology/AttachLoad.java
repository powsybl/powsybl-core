/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

/**
 * This method adds a new load on an existing voltage level. The voltage level should be described
 * in node/breaker.
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class AttachLoad implements NetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachLoad.class);

    private final LoadAdder loadAdder;

    //VoltageLevel attributes
    private final String voltageLevelId;
    private String bbsId; //Id of the busBar where the switch will be closed

    public AttachLoad(LoadAdder loadAdder, String voltageLevelId, String bbsId) {
        this.loadAdder = loadAdder;
        this.voltageLevelId = voltageLevelId;
        this.bbsId = bbsId;
    }

    public AttachLoad(LoadAdder loadAdder, String voltageLevelId) {
        this.loadAdder = loadAdder;
        this.voltageLevelId = voltageLevelId;
    }

    public LoadAdder getLoadAdder() {
        return loadAdder;
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

    private void createTopologyAutomatically(Network network, VoltageLevel voltageLevel, int loadNode, int forkNode, String loadId, Reporter reporter) {
        BusbarSection bbs = network.getBusbarSection(bbsId);
        int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();
        createNodeBreakerSwitches(loadNode, forkNode, bbsNode, loadId, voltageLevel.getNodeBreakerView());
        BusbarSectionPosition position = bbs.getExtension(BusbarSectionPosition.class);
        if (position == null) {
            LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs.getId());
            reporter.report(Report.builder()
                    .withKey("noBusbarSectionPositionExtension")
                    .withDefaultMessage("No busbar section position extension found on ${busbarSectionId}, only one disconnector is created")
                    .withValue("busbarSectionId", bbs.getId())
                    .withSeverity(TypedValue.WARN_SEVERITY)
                    .build());
        } else {
            createTopologyFromBusbarList(voltageLevel, forkNode, loadId, voltageLevel.getNodeBreakerView().getBusbarSectionStream()
                    .filter(b -> b.getExtension(BusbarSectionPosition.class) != null)
                    .filter(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex() == position.getSectionIndex())
                    .filter(b -> !b.getId().equals(bbsId)));
        }
        voltageLevel.getNodeBreakerView().getBusbarSectionStream().forEach(busbarSection -> {
            if (busbarSection.getExtension(BusbarSectionPosition.class).getSectionIndex() == position.getSectionIndex() && !busbarSection.getId().equals(bbsId)) {
                createNBDisconnector(forkNode, busbarSection.getTerminal().getNodeBreakerView().getNode(),
                        String.valueOf(busbarSection.getId()), loadId, voltageLevel.getNodeBreakerView(), true);
            }
        });

    }

    private void createTopologyFromBusbarList(VoltageLevel voltageLevel/*, int loadNode*/, int forkNode, String loadId, Stream<BusbarSection> bbsStream) {
        // createNBBreaker(loadNode, forkNode, "", loadId, voltageLevel.getNodeBreakerView(), false);
        bbsStream.forEach(b -> {
            int bbsNode = b.getTerminal().getNodeBreakerView().getNode();
            createNBDisconnector(forkNode, bbsNode, String.valueOf(bbsNode), loadId, voltageLevel.getNodeBreakerView(), true);
        });
    }

    @Override
    public void apply(Network network, ComputationManager computationManager) {
        apply(network);
    }

    @Override
    public void apply(Network network) {
        apply(network, false, Reporter.NO_OP);
    }

    // TODO To move into a utility class
    private static void throwExceptionOrLogError(String message, String key, boolean throwException, Reporter reporter) {
        LOGGER.error(message);
        reporter.report(Report.builder()
                .withKey(key)
                .withDefaultMessage(message)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .build());
        if (throwException) {
            throw new PowsyblException(message);
        }
    }

    @Override
    public void apply(Network network, boolean throwException, Reporter reporter) {
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        BusbarSection bbs = null;
        if (voltageLevel == null) {
            throwExceptionOrLogError(String.format("Voltage level %s is not found", voltageLevelId), "missingVoltageLevel", throwException, reporter);
            return;
        }

        TopologyKind topologyKind = voltageLevel.getTopologyKind();
        if (topologyKind != TopologyKind.NODE_BREAKER) {
            throwExceptionOrLogError(String.format("Voltage level %s is not in node/breaker.", voltageLevelId), "notNodeBreakerVoltageLevel", throwException, reporter);
            return;
        }

        if (bbsId != null) {
            bbs = network.getBusbarSection(bbsId);
            if (bbs == null) {
                throwExceptionOrLogError(String.format("Busbar section %s not found.", bbsId), "notFoundBusbarSection", throwException, reporter);
                return;
            }
            if (bbs.getTerminal().getVoltageLevel() != voltageLevel) {
                throwExceptionOrLogError(String.format("Busbar section %s is not in voltageLevel %s", bbsId, voltageLevelId), "busbarSectionNotInVoltageLevel", throwException, reporter);
                return;
            }
        }

        if (bbs == null) {
            bbs = voltageLevel.getNodeBreakerView().getBusbarSectionStream().findFirst().orElse(null);
            if (bbs == null) {
                throwExceptionOrLogError(String.format("Voltage level %s has no busbar section.", voltageLevelId), "noBusbarSectionInVoltageLevel", throwException, reporter);
                return;
            }
        }

        int loadNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int forkNode = loadNode + 1;
        loadAdder.setNode(loadNode);
        Load load = loadAdder.add();
        String loadId = load.getId();

        //Create switches and a breaker linking the load to the busbar sections
        createTopologyAutomatically(network, voltageLevel, loadNode, forkNode, loadId, reporter);

    }
}
