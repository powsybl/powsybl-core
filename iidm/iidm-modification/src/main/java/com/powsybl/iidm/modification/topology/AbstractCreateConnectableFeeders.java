/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.topology.ModificationReports.*;
import static com.powsybl.iidm.modification.topology.ModificationReports.noConnectablePositionExtension;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createNBDisconnector;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createNodeBreakerSwitches;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractCreateConnectableFeeders implements NetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCreateConnectableFeeders.class);

    protected final int[] sides;

    protected abstract String getBbsId(int side);

    protected abstract void setNode(int side, int node);

    protected abstract Connectable<?> add();

    protected abstract VoltageLevel getVoltageLevel(int side, Connectable<?> connectable);

    protected abstract int getPositionOrder(int side);

    protected abstract ConnectablePosition.Direction getDirection(int side);

    protected abstract int getNode(int side, Connectable<?> connectable);

    protected AbstractCreateConnectableFeeders(int... sides) {
        this.sides = Arrays.copyOf(sides, sides.length);
    }

    @Override
    public void apply(Network network, ComputationManager computationManager) {
        apply(network, false, Reporter.NO_OP);
    }

    @Override
    public void apply(Network network, ComputationManager computationManager, Reporter reporter) {
        apply(network, false, reporter);
    }

    @Override
    public void apply(Network network, Reporter reporter) {
        apply(network, false, reporter);
    }

    @Override
    public void apply(Network network, boolean throwException, Reporter reporter) {
        for (int side : sides) {
            String bbsId = getBbsId(side);
            BusbarSection bbs = network.getBusbarSection(bbsId); //If the busbar exists, topology of the associated voltage level is node/breaker
            if (bbs == null) {
                LOGGER.error("Busbar section {} not found.", bbsId);
                notFoundBusbarSectionReport(reporter, bbsId);
                if (throwException) {
                    throw new PowsyblException(String.format("Busbar section %s not found.", bbsId));
                }
                return;
            }
            VoltageLevel voltageLevel = bbs.getTerminal().getVoltageLevel();
            int injectionNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
            setNode(side, injectionNode);
        }

        Connectable<?> connectable = add();
        if (connectable.getNetwork() != network) {
            connectable.remove();
            LOGGER.error("Network given in parameters and in injectionAdder are different. Injection '{}' of type {} was added then removed",
                    connectable.getId(), connectable.getType());
            networkMismatchReport(reporter, connectable.getId(), connectable.getType());
            if (throwException) {
                throw new PowsyblException("Network given in parameters and in injectionAdder are different. Injection was added then removed");
            }
            return;
        }
        String connectableId = connectable.getId();

        for (int side : sides) {
            VoltageLevel voltageLevel = getVoltageLevel(side, connectable);
            Set<Integer> takenFeederPositions = TopologyModificationUtils.getFeederPositions(voltageLevel);
            int positionOrder = getPositionOrder(side);
            if (!takenFeederPositions.isEmpty()) {
                if (takenFeederPositions.contains(positionOrder)) {
                    LOGGER.error("InjectionPositionOrder {} already taken.", positionOrder);
                    injectionPositionOrderAlreadyTakenReport(reporter, positionOrder);
                    if (throwException) {
                        throw new PowsyblException(String.format("InjectionPositionOrder %d already taken.", positionOrder));
                    }
                    return;
                }
                connectable.newExtension(ConnectablePositionAdder.class)
                        .newFeeder()
                        .withDirection(getDirection(side))
                        .withOrder(positionOrder)
                        .withName(connectableId)
                        .add()
                        .add();
            } else {
                LOGGER.warn("No extensions found on voltageLevel {}. The extension on the injection is not created.", voltageLevel.getId());
                noConnectablePositionExtension(reporter, voltageLevel);
            }
            // create switches and a breaker linking the injection to the busbar sections.
            int node = getNode(side, connectable);
            createTopology(side, network, voltageLevel, node, node + 1, connectable, reporter);
        }
    }

    private void createTopology(int side, Network network, VoltageLevel voltageLevel, int injectionNode, int forkNode, Connectable<?> connectable, Reporter reporter) {
        String injectionId = connectable.getId();
        String bbsId = getBbsId(side);
        BusbarSection bbs = network.getBusbarSection(bbsId);
        int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();
        createNodeBreakerSwitches(injectionNode, forkNode, bbsNode, injectionId, voltageLevel.getNodeBreakerView());
        BusbarSectionPosition position = bbs.getExtension(BusbarSectionPosition.class);
        int parallelBbsNumber = 0;
        if (position == null) {
            LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs.getId());
            noBusbarSectionPositionExtensionReport(reporter, bbs);
        } else {
            List<BusbarSection> bbsList = voltageLevel.getNodeBreakerView().getBusbarSectionStream()
                    .filter(b -> b.getExtension(BusbarSectionPosition.class) != null)
                    .filter(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex() == position.getSectionIndex())
                    .filter(b -> !b.getId().equals(bbsId)).collect(Collectors.toList());
            parallelBbsNumber = bbsList.size();
            createTopologyFromBusbarSectionList(voltageLevel, forkNode, injectionId, bbsList);
        }
        LOGGER.info("New injection {} was added to voltage level {} on busbar section {}", injectionId, voltageLevel.getId(), bbs.getId());
        newInjectionAddedReport(reporter, voltageLevel.getId(), bbsId, connectable, parallelBbsNumber);
    }

    private static void createTopologyFromBusbarSectionList(VoltageLevel voltageLevel, int forkNode, String injectionId, List<BusbarSection> bbsList) {
        bbsList.forEach(b -> {
            int bbsNode = b.getTerminal().getNodeBreakerView().getNode();
            createNBDisconnector(forkNode, bbsNode, String.valueOf(bbsNode), injectionId, voltageLevel.getNodeBreakerView(), true);
        });
    }
}
