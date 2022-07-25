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

import java.util.*;
import java.util.stream.Stream;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

/**
 * This method adds a new load on an existing voltage level. The voltage level should be described
 * in node/breaker topology.
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class AttachLoad implements NetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachLoad.class);

    private final LoadAdder loadAdder;
    private final String voltageLevelId;
    private String bbsId;
    private int loadPositionOrder;
    private PositionInsideSection loadPositionInsideSection = PositionInsideSection.SPECIFIC;
    private final ConnectablePosition.Direction loadDirection;

    public enum PositionInsideSection {
        FIRST,
        LAST,
        SPECIFIC
    }

    /**
     * Constructor.
     *
     * @param loadAdder                The load adder.
     * @param voltageLevelId           The voltage level with the given ID that we want to connect to the initial line.
     * @param bbsId                    The ID of the existing bus bar section of the voltage level voltageLevelId where we want to connect the load.
     *                                 This switch will be closed.
     *                                 Please note that there will be switches between this bus or bus bar section and the connection point of the line.
     * @param loadPositionOrder        The order of the load to be attached from its extension {@link com.powsybl.iidm.network.extensions.ConnectablePosition}.
     * @param loadDirection            The direction of the load to be attached from its extension {@link com.powsybl.iidm.network.extensions.ConnectablePosition}.
     */
    public AttachLoad(LoadAdder loadAdder, String voltageLevelId, String bbsId, int loadPositionOrder, ConnectablePosition.Direction loadDirection) {
        this.loadAdder = loadAdder;
        this.voltageLevelId = voltageLevelId;
        this.bbsId = bbsId;
        this.loadPositionOrder = loadPositionOrder;
        this.loadDirection = loadDirection;
    }

    public AttachLoad(LoadAdder loadAdder, String voltageLevelId, String bbsId, int loadPositionOrder) {
        this(loadAdder, voltageLevelId, bbsId, loadPositionOrder, ConnectablePosition.Direction.BOTTOM);
    }

    /**
     * Constructor.
     *
     * @param loadAdder                The load adder.
     * @param voltageLevelId           The voltage level with the given ID that we want to connect to the initial line.
     * @param bbsId                    The ID of the existing bus bar section of the voltage level voltageLevelId where we want to connect the load.
     *                                 Please note that there will be switches between this bus or bus bar section and the connection point of the line.
     * @param loadPositionInsideSection  The load position inside the section bbsId, only {@link PositionInsideSection} FIRST and {@link PositionInsideSection} LAST are supported here.
     * @param loadDirection            The direction of the load to be attached from its extension {@link com.powsybl.iidm.network.extensions.ConnectablePosition}.
     */
    public AttachLoad(LoadAdder loadAdder, String voltageLevelId, String bbsId, PositionInsideSection loadPositionInsideSection, ConnectablePosition.Direction loadDirection) {
        this.loadAdder = loadAdder;
        this.voltageLevelId = voltageLevelId;
        this.bbsId = bbsId;
        if (loadPositionInsideSection == PositionInsideSection.SPECIFIC) {
            throw new PowsyblException("Load position inside section SPECIFIC is not compatible with this constructor");
        }
        this.loadPositionInsideSection = loadPositionInsideSection;
        this.loadDirection = loadDirection;
    }

    public AttachLoad(LoadAdder loadAdder, String voltageLevelId, String bbsId, PositionInsideSection loadPositionInsideSection) {
        this(loadAdder, voltageLevelId, bbsId, loadPositionInsideSection, ConnectablePosition.Direction.BOTTOM);
    }

    /**
     * Constructor.
     *
     * @param loadAdder                The load adder.
     * @param voltageLevelId           The voltage level with the given ID that we want to connect to the initial line. The load will be connected on the first bus bar section.
     * @param loadPositionInsideSection  The load position inside the section bbsId, only {@link PositionInsideSection} FIRST and {@link PositionInsideSection} LAST are supported here.
     * @param loadDirection            The direction of the load to be attached from its extension {@link com.powsybl.iidm.network.extensions.ConnectablePosition}.
     */
    public AttachLoad(LoadAdder loadAdder, String voltageLevelId, PositionInsideSection loadPositionInsideSection, ConnectablePosition.Direction loadDirection) {
        this.loadAdder = loadAdder;
        this.voltageLevelId = voltageLevelId;
        if (loadPositionInsideSection == PositionInsideSection.SPECIFIC) {
            throw new PowsyblException("Load position inside section SPECIFIC is not compatible with this constructor");
        }
        this.loadPositionInsideSection = loadPositionInsideSection;
        this.loadDirection = loadDirection;
    }

    public AttachLoad(LoadAdder loadAdder, String voltageLevelId, PositionInsideSection loadPositionInsideSection) {
        this(loadAdder, voltageLevelId, loadPositionInsideSection, ConnectablePosition.Direction.BOTTOM);
    }

    public AttachLoad(LoadAdder loadAdder, String voltageLevelId) {
        this(loadAdder, voltageLevelId, PositionInsideSection.FIRST, ConnectablePosition.Direction.BOTTOM);
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
            LOGGER.warn("No bus bar section position extension found on {}, only one disconnector is created.", bbs.getId());
            reporter.report(Report.builder()
                    .withKey("noBusbarSectionPositionExtension")
                    .withDefaultMessage("No bus bar section position extension found on ${bbsId}, only one disconnector is created")
                    .withValue("bbsId", bbs.getId())
                    .withSeverity(TypedValue.WARN_SEVERITY)
                    .build());
        } else {
            createTopologyFromBusbarSectionList(voltageLevel, forkNode, loadId, voltageLevel.getNodeBreakerView().getBusbarSectionStream()
                    .filter(b -> b.getExtension(BusbarSectionPosition.class) != null)
                    .filter(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex() == position.getSectionIndex())
                    .filter(b -> !b.getId().equals(bbsId)));
        }
    }

    private void createTopologyFromBusbarSectionList(VoltageLevel voltageLevel, int forkNode, String loadId, Stream<BusbarSection> bbsStream) {
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

    @Override
    public void apply(Network network, boolean throwException, Reporter reporter) {
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        TopologyKind topologyKind = voltageLevel.getTopologyKind();
        if (voltageLevel == null) {
            throwExceptionOrLogError(String.format("Voltage level %s is not found", voltageLevelId), "missingVoltageLevel", throwException, reporter);
            return;
        }
        if (topologyKind != TopologyKind.NODE_BREAKER) {
            throwExceptionOrLogError(String.format("Voltage level %s is not in node/breaker.", voltageLevelId), "notNodeBreakerVoltageLevel", throwException, reporter);
            return;
        }

        BusbarSection bbs = null;
        if (bbsId != null) {
            bbs = network.getBusbarSection(bbsId);
            if (bbs == null) {
                throwExceptionOrLogError(String.format("Bus bar section %s not found.", bbsId), "notFoundBusbarSection", throwException, reporter);
                return;
            }
            if (bbs.getTerminal().getVoltageLevel() != voltageLevel) {
                throwExceptionOrLogError(String.format("Bus bar section %s is not in voltageLevel %s", bbsId, voltageLevelId), "busbarSectionNotInVoltageLevel", throwException, reporter);
                return;
            }
        }

        if (bbs == null) {
            bbs = voltageLevel.getNodeBreakerView().getBusbarSectionStream().findFirst().orElse(null);
            if (bbs == null) {
                throwExceptionOrLogError(String.format("Voltage level %s has no bus bar section.", voltageLevelId), "noBusbarSectionInVoltageLevel", throwException, reporter);
                return;
            }
            bbsId = bbs.getId();
        }

        int loadNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int forkNode = loadNode + 1;
        loadAdder.setNode(loadNode);
        Load load = loadAdder.add();
        String loadId = load.getId();

        if (loadPositionInsideSection != PositionInsideSection.SPECIFIC) {
            if (loadPositionInsideSection == PositionInsideSection.FIRST) {
                loadPositionOrder = 0; // FIXME: should be computed.
            } else if (loadPositionInsideSection == PositionInsideSection.LAST) {
                loadPositionOrder = Integer.MAX_VALUE; // FIXME: should be computed.
            }
        }

        // TODO: add a check for SPECIFIC position inside section.

        load.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withDirection(loadDirection)
                    .withOrder(loadPositionOrder)
                    .withName(loadId)
                .add()
                .add();

        // create switches and a breaker linking the load to the bus bar sections.
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
}
