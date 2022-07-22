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
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final String voltageLevelId;
    private String bbsId; //Id of the busBar where the switch will be closed

    // Position of the load for the connectablePosition extension
    private final int loadPositionOrder;
    private final ConnectablePosition.Direction loadDirection;

    public AttachLoad(LoadAdder loadAdder, String voltageLevelId, String bbsId, int loadPositionOrder) {
        this(loadAdder, voltageLevelId, bbsId, loadPositionOrder, ConnectablePosition.Direction.BOTTOM);
    }

    public AttachLoad(LoadAdder loadAdder, String voltageLevelId, String bbsId, int loadPositionOrder, ConnectablePosition.Direction loadDirection) {
        this.loadAdder = loadAdder;
        this.voltageLevelId = voltageLevelId;
        this.bbsId = bbsId;
        this.loadPositionOrder = loadPositionOrder;
        this.loadDirection = loadDirection;
    }

    public AttachLoad(LoadAdder loadAdder, String voltageLevelId, int loadPositionOrder) {
        this(loadAdder, voltageLevelId, loadPositionOrder, ConnectablePosition.Direction.BOTTOM);
    }

    public AttachLoad(LoadAdder loadAdder, String voltageLevelId, int loadPositionOrder, ConnectablePosition.Direction loadDirection) {
        this.loadAdder = loadAdder;
        this.voltageLevelId = voltageLevelId;
        this.loadPositionOrder = loadPositionOrder;
        this.loadDirection = loadDirection;
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

    // TODO : javadoc
    public int getLoadPositionOrder() {
        return loadPositionOrder;
    }

    public ConnectablePosition.Direction getLoadDirection() {
        return loadDirection;
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
            bbsId = bbs.getId();
            if (bbs == null) {
                throwExceptionOrLogError(String.format("Voltage level %s has no busbar section.", voltageLevelId), "noBusbarSectionInVoltageLevel", throwException, reporter);
                return;
            }
        }

        //TODO: Add check on ConnectablePosition : ask Florian if the method already exists

        int loadNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int forkNode = loadNode + 1;
        loadAdder.setNode(loadNode);
        Load load = loadAdder.add();
        String loadId = load.getId();
        load.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withDirection(loadDirection)
                    .withOrder(loadPositionOrder)
                    .withName(loadId)
                .add()
                .add();

        //Create switches and a breaker linking the load to the busbar sections
        createTopologyAutomatically(network, voltageLevel, loadNode, forkNode, loadId, reporter);

    }

    public static List<Pair<String, Integer>> getFeederPositions(VoltageLevel voltageLevel) {
        List<Pair<String, Integer>> feederPositionsOrders = new ArrayList<>();
        voltageLevel.getConnectables().forEach(connectable -> {
            ConnectablePosition<?> position = (ConnectablePosition<?>) connectable.getExtension(ConnectablePosition.class);
            if (position != null) {
                Optional<Integer> order;
                switch (connectable.getType()) {
                    case BUSBAR_SECTION:
                        break;
                    case LOAD:
                    case GENERATOR:
                    case SHUNT_COMPENSATOR:
                    case STATIC_VAR_COMPENSATOR:
                    case HVDC_CONVERTER_STATION:
                    case BATTERY:
                    case DANGLING_LINE:
                    case SWITCH:
                        order = position.getFeeder().getOrder();
                        if (order.isPresent()) {
                            feederPositionsOrders.add(Pair.of(connectable.getId(), order.get()));
                        }
                        break;
                    case LINE:
                    case TWO_WINDINGS_TRANSFORMER:
                        Branch<?> branch = (Branch<?>) connectable;
                        if (branch.getTerminal1().getVoltageLevel() == voltageLevel) {
                            order = position.getFeeder1().getOrder();
                            if (order.isPresent()) {
                                feederPositionsOrders.add(Pair.of(connectable.getId() + "_terminal1", order.get()));
                            }
                        } else if (branch.getTerminal1().getVoltageLevel() == voltageLevel) {
                            order = position.getFeeder2().getOrder();
                            if (order.isPresent()) {
                                feederPositionsOrders.add(Pair.of(connectable.getId() + "_terminal2", order.get()));
                            }
                        }
                        break;
                    case THREE_WINDINGS_TRANSFORMER:
                        ThreeWindingsTransformer twt = (ThreeWindingsTransformer) connectable;
                        if (twt.getLeg1().getTerminal().getVoltageLevel() == voltageLevel) {
                            order = position.getFeeder1().getOrder();
                            if (order.isPresent()) {
                                feederPositionsOrders.add(Pair.of(connectable.getId() + "_terminal1", order.get()));
                            }
                        }
                        if (twt.getLeg2().getTerminal().getVoltageLevel() == voltageLevel) {
                            order = position.getFeeder2().getOrder();
                            if (order.isPresent()) {
                                feederPositionsOrders.add(Pair.of(connectable.getId() + "_terminal2", order.get()));
                            }
                        }
                        if (twt.getLeg3().getTerminal().getVoltageLevel() == voltageLevel) {
                            order = position.getFeeder3().getOrder();
                            if (order.isPresent()) {
                                feederPositionsOrders.add(Pair.of(connectable.getId() + "_terminal3", order.get()));
                            }
                        }
                }
            }
        });
        return feederPositionsOrders;
    }
}
