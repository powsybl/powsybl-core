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
import com.powsybl.math.graph.TraverseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

import static com.powsybl.iidm.modification.ModificationUtils.throwExceptionOrLogError;
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
        if (voltageLevel == null) {
            throwExceptionOrLogError(String.format("Voltage level %s is not found", voltageLevelId), "missingVoltageLevel", throwException, reporter);
            return;
        }
        TopologyKind topologyKind = voltageLevel.getTopologyKind();
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

        BusbarSectionPosition busbarSectionPosition = bbs.getExtension(BusbarSectionPosition.class);
        if (busbarSectionPosition != null) {
            Map<Integer, List<Integer>> allOrders = getSliceOrdersMap(voltageLevel);
            if (loadPositionInsideSection != PositionInsideSection.SPECIFIC) {
                if (loadPositionInsideSection == PositionInsideSection.FIRST) {
                    loadPositionOrder = allOrders.get(busbarSectionPosition.getSectionIndex()).stream().min(Comparator.naturalOrder()).orElse(1) - 1;
                } else if (loadPositionInsideSection == PositionInsideSection.LAST) {
                    loadPositionOrder = allOrders.get(busbarSectionPosition.getSectionIndex()).stream().max(Comparator.naturalOrder()).orElse(Integer.MAX_VALUE) + 1;
                }
            }
            load.newExtension(ConnectablePositionAdder.class)
                    .newFeeder()
                    .withDirection(loadDirection)
                    .withOrder(loadPositionOrder)
                    .withName(loadId)
                    .add()
                    .add();
        }

        // create switches and a breaker linking the load to the bus bar sections.
        createTopologyAutomatically(network, voltageLevel, loadNode, forkNode, loadId, reporter);
    }

    private Map<Integer, List<Integer>> getSliceOrdersMap(VoltageLevel voltageLevel) {
        Map<Integer, List<Integer>> sliceIndexOrdersMap = new TreeMap<>();
        Map<BusbarSection, List<Integer>> busbarSectionsOrdersMap = new HashMap<>();
        voltageLevel.getConnectableStream(BusbarSection.class)
                .forEach(bbs -> fillConnectableOrders(bbs, busbarSectionsOrdersMap));
        busbarSectionsOrdersMap.forEach((bbs, orders) -> {
            BusbarSectionPosition bbPosition = bbs.getExtension(BusbarSectionPosition.class);
            sliceIndexOrdersMap.putIfAbsent(bbPosition.getSectionIndex(), orders);
        });
        return sliceIndexOrdersMap;
    }

    private void fillConnectableOrders(BusbarSection bbs, Map<BusbarSection, List<Integer>> busbarSectionsOrdersMap) {
        BusbarSectionPosition bbPosition = bbs.getExtension(BusbarSectionPosition.class);
        int bbSection = bbPosition.getSectionIndex();

        if (busbarSectionsOrdersMap.containsKey(bbs)) {
            return;
        }
        List<Integer> orders = busbarSectionsOrdersMap.compute(bbs, (k, v) -> new ArrayList<>());

        bbs.getTerminal().traverse(new Terminal.TopologyTraverser() {
            @Override
            public TraverseResult traverse(Terminal terminal, boolean connected) {
                if (terminal.getVoltageLevel() != bbs.getTerminal().getVoltageLevel()) {
                    return TraverseResult.TERMINATE_PATH;
                }
                Connectable<?> connectable = terminal.getConnectable();
                if (connectable instanceof BusbarSection) {
                    BusbarSection otherBbs = (BusbarSection) connectable;
                    BusbarSectionPosition otherBbPosition = otherBbs.getExtension(BusbarSectionPosition.class);
                    if (otherBbPosition.getSectionIndex() == bbSection) {
                        busbarSectionsOrdersMap.put(otherBbs, orders);
                    } else {
                        return TraverseResult.TERMINATE_PATH;
                    }
                }
                ConnectablePosition<?> position = (ConnectablePosition<?>) (connectable.getExtension(ConnectablePosition.class));
                if (position != null) {
                    addOrders(position, orders);
                }
                return TraverseResult.CONTINUE;
            }

            @Override
            public TraverseResult traverse(Switch aSwitch) {
                return TraverseResult.CONTINUE;
            }
        });
    }

    private void addOrders(ConnectablePosition<?> position, List<Integer> orders) {
        if (position.getFeeder() != null) {
            position.getFeeder().getOrder().ifPresent(orders::add);
        } else if (position.getFeeder1() != null) {
            position.getFeeder1().getOrder().ifPresent(orders::add);
            if (position.getFeeder2() != null) {
                position.getFeeder2().getOrder().ifPresent(orders::add);
                if (position.getFeeder3() != null) {
                    position.getFeeder3().getOrder().ifPresent(orders::add);
                }
            }
        }
    }
}
