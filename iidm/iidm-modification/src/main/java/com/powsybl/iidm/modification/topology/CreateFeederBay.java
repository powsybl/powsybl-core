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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.topology.ModificationReports.*;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createNBDisconnector;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createNodeBreakerSwitches;

/**
 * This method adds a new injection bay on an existing busbar section. The voltage level containing the
 * busbar section should be described in node/breaker topology. The injection is created and connected to
 * the busbar section with a breaker and a closed disconnector. The injection is also connected to all
 * the parallel busbar sections, if any, with an open disconnector.
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CreateFeederBay implements NetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateFeederBay.class);

    private final InjectionAdder<?> injectionAdder;
    private final String bbsId;
    private final int injectionPositionOrder;
    private final ConnectablePosition.Direction injectionDirection;

    /**
     * Constructor.
     *
     * @param injectionAdder         The injection adder.
     * @param bbsId                  The ID of the existing busbar section where we want to connect the injection.
     *                               Please note that there will be switches between this busbar section and the connection point of the injection. This switch will be closed.
     * @param injectionPositionOrder The order of the injection to be attached from its extension {@link ConnectablePosition}.
     * @param injectionDirection     The direction of the injection to be attached from its extension {@link ConnectablePosition}.
     */
    public CreateFeederBay(InjectionAdder<?> injectionAdder, String bbsId, int injectionPositionOrder, ConnectablePosition.Direction injectionDirection) {
        this.injectionAdder = Objects.requireNonNull(injectionAdder);
        this.bbsId = Objects.requireNonNull(bbsId);
        this.injectionPositionOrder = injectionPositionOrder;
        this.injectionDirection = Objects.requireNonNull(injectionDirection);
    }

    public CreateFeederBay(InjectionAdder<?> injectionAdder, String bbsId, int injectionPositionOrder) {
        this(injectionAdder, bbsId, injectionPositionOrder, ConnectablePosition.Direction.BOTTOM);
    }

    @Override
    public void apply(Network network, ComputationManager computationManager) {
        apply(network);
    }

    @Override
    public void apply(Network network) {
        apply(network, false, Reporter.NO_OP);
    }

    /**
     * Applies the modification to the given network. If throwException is set to true, then in case of error, an
     * exception will be thrown. Otherwise, computation will continue but the injection will not be added to the network
     * in case of error.
     */
    @Override
    public void apply(Network network, boolean throwException, Reporter reporter) {
        BusbarSection bbs = network.getBusbarSection(bbsId);
        if (bbs == null) {
            LOGGER.error("Busbar section {} not found.", bbsId);
            notFoundBusbarSectionReport(reporter, bbsId);
            if (throwException) {
                throw new PowsyblException(String.format("Busbar section %s not found.", bbsId));
            }
            return;
        }

        VoltageLevel voltageLevel = bbs.getTerminal().getVoltageLevel();
        TopologyKind topologyKind = voltageLevel.getTopologyKind();
        if (topologyKind != TopologyKind.NODE_BREAKER) {
            LOGGER.error("Voltage level {} is not in node/breaker.", voltageLevel.getId());
            notNodeBreakerVoltageLevelReport(reporter, voltageLevel.getId());
            if (throwException) {
                throw new PowsyblException(String.format("Voltage level %s is not in node/breaker.", voltageLevel.getId()));
            }
            return;
        }

        int injectionNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int forkNode = injectionNode + 1;
        injectionAdder.setNode(injectionNode);
        Injection<?> injection = add(injectionAdder);
        if (injection.getNetwork() != network) {
            injection.remove();
            LOGGER.error("Network given in parameters and in injectionAdder are different. Injection '{}' of type {} was added then removed",
                    injection.getId(), injection.getType());
            networkMismatchReport(reporter, injection.getId(), injection.getType());
            if (throwException) {
                throw new PowsyblException("Network given in parameters and in injectionAdder are different. Injection was added then removed");
            }
            return;
        }
        String injectionId = injection.getId();

        Set<Integer> takenFeederPositions = TopologyModificationUtils.getFeederPositions(voltageLevel);
        if (takenFeederPositions.contains(injectionPositionOrder)) {
            LOGGER.error("InjectionPositionOrder {} already taken.", injectionPositionOrder);
            injectionPositionOrderAlreadyTakenReport(reporter, injectionPositionOrder);
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

        // create switches and a breaker linking the injection to the busbar sections.
        createTopology(network, voltageLevel, injectionNode, forkNode, injection, reporter);
    }

    private void createTopology(Network network, VoltageLevel voltageLevel, int injectionNode, int forkNode, Injection<?> injection, Reporter reporter) {
        String injectionId = injection.getId();
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
        newInjectionAddedReport(reporter, voltageLevel.getId(), bbsId, injection, parallelBbsNumber);
    }

    private void createTopologyFromBusbarSectionList(VoltageLevel voltageLevel, int forkNode, String injectionId, List<BusbarSection> bbsList) {
        bbsList.forEach(b -> {
            int bbsNode = b.getTerminal().getNodeBreakerView().getNode();
            createNBDisconnector(forkNode, bbsNode, String.valueOf(bbsNode), injectionId, voltageLevel.getNodeBreakerView(), true);
        });
    }

    private Injection<?> add(InjectionAdder<?> injectionAdder) {
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
        } else if (injectionAdder instanceof StaticVarCompensatorAdder) {
            return ((StaticVarCompensatorAdder) injectionAdder).add();
        } else if (injectionAdder instanceof LccConverterStationAdder) {
            return ((LccConverterStationAdder) injectionAdder).add();
        } else if (injectionAdder instanceof VscConverterStationAdder) {
            return ((VscConverterStationAdder) injectionAdder).add();
        } else {
            throw new AssertionError("Given InjectionAdder not supported: " + injectionAdder.getClass().getName());
        }
    }
}
