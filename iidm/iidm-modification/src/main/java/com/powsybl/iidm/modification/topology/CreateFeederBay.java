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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.powsybl.iidm.modification.ModificationUtils.throwExceptionAndLogError;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

/**
 * This method adds a new injection bay on an existing voltage level. The voltage level should be described
 * in node/breaker topology.
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CreateFeederBay implements NetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateFeederBay.class);

    private final InjectionAdder<?> injectionAdder;
    private final String voltageLevelId;
    private String bbsId;
    private final int injectionPositionOrder;
    private final ConnectablePosition.Direction injectionDirection;

    /**
     * Constructor.
     *
     * @param injectionAdder           The injection adder.
     * @param voltageLevelId           The voltage level with the given ID that we want to connect to.
     * @param bbsId                    The ID of the existing bus bar section of the voltage level voltageLevelId where we want to connect the injection.
     *                                 Please note that there will be switches between this bus bar section and the connection point of the injeciton. This switch will be closed.
     * @param injectionPositionOrder        The order of the injection to be attached from its extension {@link com.powsybl.iidm.network.extensions.ConnectablePosition}.
     * @param injectionDirection            The direction of the injection to be attached from its extension {@link com.powsybl.iidm.network.extensions.ConnectablePosition}.
     */
    public CreateFeederBay(InjectionAdder<?> injectionAdder, String voltageLevelId, String bbsId, int injectionPositionOrder, ConnectablePosition.Direction injectionDirection) {
        this.injectionAdder = injectionAdder;
        this.voltageLevelId = voltageLevelId;
        this.bbsId = bbsId;
        this.injectionPositionOrder = injectionPositionOrder;
        this.injectionDirection = injectionDirection;
    }

    public CreateFeederBay(InjectionAdder<?> injectionAdder, String voltageLevelId, String bbsId, int injectionPositionOrder) {
        this(injectionAdder, voltageLevelId, bbsId, injectionPositionOrder, ConnectablePosition.Direction.BOTTOM);
    }

    /**
     * Constructor.
     *
     * @param injectionAdder           The injection adder.
     * @param voltageLevelId           The voltage level with the given ID that we want to connect to. The injection will be connected on the first bus bar section.
     * @param injectionPositionOrder        The order of the injection to be attached from its extension {@link com.powsybl.iidm.network.extensions.ConnectablePosition}.
     * @param injectionDirection            The direction of the injection to be attached from its extension {@link com.powsybl.iidm.network.extensions.ConnectablePosition}.
     */
    public CreateFeederBay(InjectionAdder<?> injectionAdder, String voltageLevelId, int injectionPositionOrder, ConnectablePosition.Direction injectionDirection) {
        this.injectionAdder = injectionAdder;
        this.voltageLevelId = voltageLevelId;
        this.injectionPositionOrder = injectionPositionOrder;
        this.injectionDirection = injectionDirection;
    }

    public CreateFeederBay(InjectionAdder<?> injectionAdder, String voltageLevelId, int injectionPositionOrder) {
        this(injectionAdder, voltageLevelId, injectionPositionOrder, ConnectablePosition.Direction.BOTTOM);
    }

    public InjectionAdder getInjectionAdder() {
        return injectionAdder;
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

    public int getInjectionPositionOrder() {
        return injectionPositionOrder;
    }

    public ConnectablePosition.Direction getInjectionDirection() {
        return injectionDirection;
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
            LOGGER.error("Voltage level {} is not found", voltageLevelId);
            reporter.report(Report.builder()
                    .withKey("missingVoltageLevel")
                    .withDefaultMessage("Voltage level ${voltageLevelId} is not found")
                    .withValue("voltageLevelId", voltageLevelId)
                    .withSeverity(TypedValue.ERROR_SEVERITY)
                    .build());
            if (throwException) {
                throw new PowsyblException(String.format("Voltage level %s is not found", voltageLevelId));
            }
            return;
        }
        TopologyKind topologyKind = voltageLevel.getTopologyKind();
        if (topologyKind != TopologyKind.NODE_BREAKER) {
            LOGGER.error("Voltage level {} is not in node/breaker.", voltageLevelId);
            reporter.report(Report.builder()
                    .withKey("notNodeBreakerVoltageLevel")
                    .withDefaultMessage("Voltage level ${voltageLevelId} is not in node/breaker")
                    .withValue("voltageLevelId", voltageLevelId)
                    .withSeverity(TypedValue.ERROR_SEVERITY)
                    .build());
            if (throwException) {
                throw new PowsyblException(String.format("Voltage level %s is not in node/breaker.", voltageLevelId));
            }
            return;
        }
        BusbarSection bbs;
        if (bbsId != null) {
            bbs = network.getBusbarSection(bbsId);
            if (bbs == null) {
                LOGGER.error("Bus bar section {} not found.", bbsId);
                reporter.report(Report.builder()
                        .withKey("notFoundBusbarSection")
                        .withDefaultMessage("Bus bar section ${busbarSectionId} not found")
                        .withValue("busbarSectionId", bbsId)
                        .withSeverity(TypedValue.ERROR_SEVERITY)
                        .build());
                if (throwException) {
                    throw new PowsyblException(String.format("Bus bar section %s not found.", bbsId));
                }
                return;
            }
            if (bbs.getTerminal().getVoltageLevel() != voltageLevel) {
                LOGGER.error("Bus bar section {} is not in voltageLevel {}.", bbsId, voltageLevelId);
                reporter.report(Report.builder()
                        .withKey("busbarSectionNotInVoltageLevel")
                        .withDefaultMessage("Bus bar section ${busbarSectionId} is not in voltageLevel ${voltageLevelId}.")
                        .withValue("busbarSectionId", bbsId)
                        .withValue("voltageLevelId", voltageLevelId)
                        .withSeverity(TypedValue.ERROR_SEVERITY)
                        .build());
                if (throwException) {
                    throw new PowsyblException(String.format("Bus bar section %s is not in voltageLevel %s.", bbsId, voltageLevelId));
                }
                return;
            }
        } else {
            bbs = voltageLevel.getNodeBreakerView().getBusbarSectionStream().findFirst().orElse(null);
            if (bbs == null) {
                LOGGER.error("Voltage level {} has no bus bar section.", voltageLevelId);
                reporter.report(Report.builder()
                        .withKey("noBusbarSectionInVoltageLevel")
                        .withDefaultMessage("Voltage level ${voltageLevelId} has no bus bar section.")
                        .withValue("voltageLevelId", voltageLevelId)
                        .withSeverity(TypedValue.ERROR_SEVERITY)
                        .build());
                if (throwException) {
                    throw new PowsyblException(String.format("Voltage level %s has no bus bar section.", voltageLevelId));
                }
                return;
            }
            bbsId = bbs.getId();
        }

        int injectionNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int forkNode = injectionNode + 1;
        injectionAdder.setNode(injectionNode);
        Injection<?> injection = add(injectionAdder);
        if (injection.getNetwork() != network) {
            injection.remove();
            throwExceptionAndLogError("Network given in parameters and in injectionAdder are different. The network might be corrupted", "networkMismatch", throwException, reporter);
            return;
        }
        String injectionId = injection.getId();

        BusbarSectionPosition busbarSectionPosition = bbs.getExtension(BusbarSectionPosition.class);
        if (busbarSectionPosition != null) {
            Map<Integer, List<Integer>> allOrders = getSliceOrdersMap(voltageLevel);
            if (allOrders.get(busbarSectionPosition.getSectionIndex()).contains(injectionPositionOrder)) {
                LOGGER.error("InjectionPositionOrder {} already taken.", injectionPositionOrder);
                reporter.report(Report.builder()
                        .withKey("injectionPositionOrderAlreadyTaken")
                        .withDefaultMessage("InjectionPositionOrder ${injectionPositionOrder} already taken.")
                        .withValue("injectionPositionOrder", injectionPositionOrder)
                        .withSeverity(TypedValue.ERROR_SEVERITY)
                        .build());
                if (throwException) {
                    throw new PowsyblException(String.format("InjectionPositionOrder %d already taken.", injectionPositionOrder));
                }
                return;
            }

            injection.newExtension(ConnectablePositionAdder.class)
                    .newFeeder()
                    .withDirection(injectionDirection)
                    .withOrder(injectionPositionOrder)
                    .withName(injectionId)
                    .add()
                    .add();
        }

        // create switches and a breaker linking the injection to the bus bar sections.
        createTopology(network, voltageLevel, injectionNode, forkNode, injectionId, reporter);

        LOGGER.info("New injection {} was added to voltage level {} on busbar section {}", injection.getId(), voltageLevel.getId(), bbs.getId());
        reporter.report(Report.builder()
                .withKey("newInjectionAdded")
                .withDefaultMessage("New injection ${injectionId} was added to voltage level ${voltageLevelId} on busbar section ${bbsId}")
                .withValue("injectionId", injection.getId())
                .withValue("voltageLevelId", voltageLevel.getId())
                .withValue("bbsId", bbs.getId())
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    private void createTopology(Network network, VoltageLevel voltageLevel, int injectionNode, int forkNode, String injectionId, Reporter reporter) {
        BusbarSection bbs = network.getBusbarSection(bbsId);
        int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();
        createNodeBreakerSwitches(injectionNode, forkNode, bbsNode, injectionId, voltageLevel.getNodeBreakerView());
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
            createTopologyFromBusbarSectionList(voltageLevel, forkNode, injectionId, voltageLevel.getNodeBreakerView().getBusbarSectionStream()
                    .filter(b -> b.getExtension(BusbarSectionPosition.class) != null)
                    .filter(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex() == position.getSectionIndex())
                    .filter(b -> !b.getId().equals(bbsId)));
        }
    }

    private void createTopologyFromBusbarSectionList(VoltageLevel voltageLevel, int forkNode, String injectionId, Stream<BusbarSection> bbsStream) {
        bbsStream.forEach(b -> {
            int bbsNode = b.getTerminal().getNodeBreakerView().getNode();
            createNBDisconnector(forkNode, bbsNode, String.valueOf(bbsNode), injectionId, voltageLevel.getNodeBreakerView(), true);
        });
    }

    private Injection<?> add(InjectionAdder<? extends InjectionAdder> injectionAdder) {
        if (injectionAdder instanceof LoadAdder) {
            return ((LoadAdder) injectionAdder).add();
        } else if (injectionAdder instanceof BatteryAdder) {
            return ((BatteryAdder) injectionAdder).add();
        } else if (injectionAdder instanceof DanglingLineAdder) {
            return ((DanglingLineAdder) injectionAdder).add();
        } else if (injectionAdder instanceof GeneratorAdder) {
            return ((GeneratorAdder) injectionAdder).add();
        } else if (injectionAdder instanceof ShuntCompensatorAdder) {
            return ((ShuntCompensatorAdder) injectionAdder).add();
        } else if (injectionAdder instanceof StaticVarCompensator) {
            return ((StaticVarCompensatorAdder) injectionAdder).add();
        } else if (injectionAdder instanceof LccConverterStationAdder) {
            return ((LccConverterStationAdder) injectionAdder).add();
        } else if (injectionAdder instanceof VscConverterStationAdder) {
            return ((VscConverterStationAdder) injectionAdder).add();
        } else {
            throw new AssertionError();
        }
    }
}
