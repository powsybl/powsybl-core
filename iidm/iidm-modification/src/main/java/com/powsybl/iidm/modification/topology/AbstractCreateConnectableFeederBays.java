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
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.topology.ModificationReports.*;
import static com.powsybl.iidm.modification.topology.ModificationReports.noConnectablePositionExtension;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractCreateConnectableFeederBays extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCreateConnectableFeederBays.class);

    protected final int[] sides;

    protected abstract String getBbsId(int side);

    protected abstract void setNode(int side, int node, String voltageLevelId);

    protected abstract Connectable<?> add();

    protected abstract VoltageLevel getVoltageLevel(int side, Connectable<?> connectable);

    protected abstract int getPositionOrder(int side);

    protected abstract Optional<String> getFeederName(int side);

    protected abstract ConnectablePosition.Direction getDirection(int side);

    protected abstract int getNode(int side, Connectable<?> connectable);

    protected abstract ConnectablePositionAdder.FeederAdder<?> getFeederAdder(int side, ConnectablePositionAdder<?> connectablePositionAdder);

    protected AbstractCreateConnectableFeederBays(int... sides) {
        this.sides = Arrays.copyOf(sides, sides.length);
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        Map<VoltageLevel, Integer> firstAvailableNodes = new HashMap<>();
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
            int connectableNode = firstAvailableNodes.compute(voltageLevel, (vl, node) -> node == null ? vl.getNodeBreakerView().getMaximumNodeIndex() + 1 : node + 1);
            setNode(side, connectableNode, voltageLevel.getId());
        }

        Connectable<?> connectable = add();
        if (connectable.getNetwork() != network) {
            connectable.remove();
            LOGGER.error("Network given in parameters and in connectableAdder are different. Connectable '{}' of type {} was added then removed",
                    connectable.getId(), connectable.getType());
            networkMismatchReport(reporter, connectable.getId(), connectable.getType());
            if (throwException) {
                throw new PowsyblException("Network given in parameters and in connectableAdder are different. Connectable was added then removed");
            }
            return;
        }
        String connectableId = connectable.getId();

        boolean createConnectablePosition = false;
        ConnectablePositionAdder<?> connectablePositionAdder = connectable.newExtension(ConnectablePositionAdder.class);
        for (int side : sides) {
            VoltageLevel voltageLevel = getVoltageLevel(side, connectable);
            Set<Integer> takenFeederPositions = TopologyModificationUtils.getFeederPositions(voltageLevel);
            int positionOrder = getPositionOrder(side);
            if (!takenFeederPositions.isEmpty() || voltageLevel.getConnectableStream().filter(c -> !(c instanceof BusbarSection)).count() == 1) {
                // check that there is only one connectable (that we added) or there are existing position extensions on other connectables
                if (!takenFeederPositions.contains(positionOrder)) {
                    getFeederAdder(side, connectablePositionAdder)
                            .withDirection(getDirection(side))
                            .withOrder(positionOrder)
                            .withName(getFeederName(side).orElse(connectableId))
                            .add();
                    createConnectablePosition = true;
                } else {
                    LOGGER.error("PositionOrder {} already taken.", positionOrder);
                    positionOrderAlreadyTakenReport(reporter, positionOrder);
                    if (throwException) {
                        throw new PowsyblException(String.format("PositionOrder %d already taken.", positionOrder));
                    }
                }
            } else {
                LOGGER.warn("No extensions found on voltageLevel {}. The extension is not created.", voltageLevel.getId());
                noConnectablePositionExtension(reporter, voltageLevel);
            }
            // create switches and a breaker linking the connectable to the busbar sections.
            int node = getNode(side, connectable);
            int forkNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
            createTopology(side, network, voltageLevel, node, forkNode, connectable, reporter);
        }
        if (createConnectablePosition) {
            connectablePositionAdder.add();
        }
    }

    private void createTopology(int side, Network network, VoltageLevel voltageLevel, int connectableNode, int forkNode, Connectable<?> connectable, Reporter reporter) {
        String baseId = connectable.getId() + (side == 0 ? "" : side);
        String bbsId = getBbsId(side);
        BusbarSection bbs = network.getBusbarSection(bbsId);
        int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();
        createNodeBreakerSwitches(connectableNode, forkNode, bbsNode, baseId, voltageLevel.getNodeBreakerView());
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
            createTopologyFromBusbarSectionList(voltageLevel, forkNode, baseId, bbsList);
        }
        LOGGER.info("New connectable {} was added to voltage level {} on busbar section {}", connectable.getId(), voltageLevel.getId(), bbs.getId());
        newConnectableAddedReport(reporter, voltageLevel.getId(), bbsId, connectable, parallelBbsNumber);
    }
}
