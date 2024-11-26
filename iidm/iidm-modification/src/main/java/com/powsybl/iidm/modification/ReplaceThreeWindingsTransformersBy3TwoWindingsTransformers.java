/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.modification.util.RegulatedTerminalControllers;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.util.ModificationReports.*;
import static com.powsybl.iidm.modification.util.TransformerUtils.*;
import static com.powsybl.iidm.modification.util.TransformerUtils.copyAndAddPhaseAngleClock;

/**
 * <p>This network modification is used to replace all threeWindingsTransformers by 3 twoWindingsTransformers.</p>
 * <p>For each threeWindingsTransformer:</p>
 * <ul>
 *     <li>A new voltage level is created for the star node with nominal voltage of ratedU0.</li>
 *     <li>Three new TwoWindingsTransformers are created, one for each leg of the removed ThreeWindingsTransformer.</li>
 *     <li>The following attributes are copied from each leg to the new associated twoWindingsTransformer:</li>
 *     <ul>
 *         <li>Electrical characteristics, ratioTapChangers, and phaseTapChangers. No adjustments are required.</li>
 *         <li>Operational Loading Limits are copied to the non-star end of the twoWindingsTransformers.</li>
 *         <li>Active and reactive power at the terminal are copied to the non-star terminal of the twoWindingsTransformer.</li>
 *     </ul>
 *     <li>Aliases:</li>
 *     <ul>
 *         <li>Aliases for known CGMES identifiers (terminal, transformer end, ratio, and phase tap changer) are copied to the right twoWindingsTransformer after adjusting the aliasType.</li>
 *         <li>Aliases that are not mapped are recorded in the functional log.</li>
 *     </ul>
 *     <li>Properties:</li>
 *     <ul>
 *         <li>Star bus voltage and angle are set to the bus created for the star node.</li>
 *         <li>The names of the operationalLimitsSet are copied to the right twoWindingsTransformer.</li>
 *         <li>Properties that are not mapped are recorded in the functional log.</li>
 *     </ul>
 *     <li>Extensions:</li>
 *     <ul>
 *         <li>Only IIDM extensions are copied: TransformerFortescueData, PhaseAngleClock, and TransformerToBeEstimated.</li>
 *         <li>CGMES extensions can not be copied, as they cause circular dependencies.</li>
 *         <li>Extensions that are not copied are recorded in the functional log.</li>
 *     </ul>
 *     <li>All the controllers using any of the threeWindingsTransformer terminals as regulated terminal are updated.</li>
 *     <li>New and removed equipment is also recorded in the functional log.</li>
 *     <li>Internal connections are created to manage the replacement.</li>
 *</ul>
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers extends AbstractNetworkModification {

    private static final String THREE_WINDINGS_TRANSFORMER = "ThreeWindingsTransformer";
    private static final String WAS_NOT_TRANSFERRED = "was not transferred.";
    private static final String CGMES_OPERATIONAL_LIMIT_SET = "CGMES.OperationalLimitSet_";

    @Override
    public String getName() {
        return "ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        RegulatedTerminalControllers regulatedTerminalControllers = new RegulatedTerminalControllers(network);
        network.getThreeWindingsTransformerStream().toList() // toList is required to create a temporary list since the threeWindingsTransformer is removed during the replacement
                .forEach(t3w -> replaceThreeWindingsTransformerBy3TwoWindingsTransformer(t3w, regulatedTerminalControllers, throwException, reportNode));
    }

    private void replaceThreeWindingsTransformerBy3TwoWindingsTransformer(ThreeWindingsTransformer t3w, RegulatedTerminalControllers regulatedTerminalControllers, boolean throwException, ReportNode reportNode) {
        VoltageLevel starVoltageLevel = createStarVoltageLevel(t3w, throwException);
        if (starVoltageLevel == null) {
            return;
        }
        createTopologyInsideStarVoltageLevel(t3w, starVoltageLevel);

        TwoWindingsTransformer t2wLeg1 = createTwoWindingsTransformer(t3w, t3w.getLeg1(), starVoltageLevel);
        TwoWindingsTransformer t2wLeg2 = createTwoWindingsTransformer(t3w, t3w.getLeg2(), starVoltageLevel);
        TwoWindingsTransformer t2wLeg3 = createTwoWindingsTransformer(t3w, t3w.getLeg3(), starVoltageLevel);
        ThreeT2wsR threeT2ws = new ThreeT2wsR(t2wLeg1, t2wLeg2, t2wLeg3);

        regulatedTerminalControllers.replaceRegulatedTerminal(t3w.getLeg1().getTerminal(), threeT2ws.t2wOne.getTerminal1());
        regulatedTerminalControllers.replaceRegulatedTerminal(t3w.getLeg2().getTerminal(), threeT2ws.t2wTwo.getTerminal1());
        regulatedTerminalControllers.replaceRegulatedTerminal(t3w.getLeg3().getTerminal(), threeT2ws.t2wThree.getTerminal1());

        // t2wLeg1, t2wLeg, and t2wLeg3 are not considered in regulatedTerminalControllers (created later in the model)
        replaceRegulatedTerminal(threeT2ws.t2wOne, t3w, threeT2ws);
        replaceRegulatedTerminal(threeT2ws.t2wTwo, t3w, threeT2ws);
        replaceRegulatedTerminal(threeT2ws.t2wThree, t3w, threeT2ws);

        copyTerminalActiveAndReactivePower(threeT2ws.t2wOne.getTerminal1(), t3w.getLeg1().getTerminal());
        copyTerminalActiveAndReactivePower(threeT2ws.t2wTwo.getTerminal1(), t3w.getLeg2().getTerminal());
        copyTerminalActiveAndReactivePower(threeT2ws.t2wThree.getTerminal1(), t3w.getLeg3().getTerminal());

        List<String> lostProperties = copyProperties(t3w, threeT2ws, starVoltageLevel);
        List<String> lostExtensions = copyExtensions(t3w, threeT2ws);

        // copy necessary data before removing the transformer
        String t3wId = t3w.getId();
        List<AliasR> t3wAliases = getAliases(t3w);
        t3w.remove();

        // after removing the threeWindingsTransformer
        List<AliasR> lostAliases = copyAliases(t3wAliases, threeT2ws);

        // warnings
        if (!lostProperties.isEmpty()) {
            lostProperties.forEach(propertyName -> logOrThrow(throwException, THREE_WINDINGS_TRANSFORMER + "'" + t3wId + "' property '" + propertyName + "' " + WAS_NOT_TRANSFERRED));
        }
        if (!lostExtensions.isEmpty()) {
            lostExtensions.forEach(extensionName -> logOrThrow(throwException, THREE_WINDINGS_TRANSFORMER + "'" + t3wId + "' extension '" + extensionName + "' " + WAS_NOT_TRANSFERRED));
        }
        if (!lostAliases.isEmpty()) {
            lostAliases.forEach(aliasR -> logOrThrow(throwException, THREE_WINDINGS_TRANSFORMER + "'" + t3wId + "' alias '" + aliasR.alias + "' '" + aliasR.aliasType + "' " + WAS_NOT_TRANSFERRED));
        }

        // report
        createReportNode(reportNode, t3wId, lostProperties, lostExtensions, lostAliases, starVoltageLevel.getId(), threeT2ws);
    }

    // It is a fictitious bus, then we do not set voltage limits
    private VoltageLevel createStarVoltageLevel(ThreeWindingsTransformer t3w, boolean throwException) {
        Optional<Substation> substation = t3w.getSubstation();
        if (substation.isEmpty()) {
            logOrThrow(throwException, THREE_WINDINGS_TRANSFORMER + "'" + t3w.getId() + "' without substation");
            return null;
        }
        TopologyKind topologykind = t3w.getLeg1().getTerminal().getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER
                && t3w.getLeg2().getTerminal().getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER
                && t3w.getLeg3().getTerminal().getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER
                ? TopologyKind.BUS_BREAKER : TopologyKind.NODE_BREAKER;
        return substation.get().newVoltageLevel()
                .setId(t3w.getId() + "-Star-VL")
                .setName(t3w.getNameOrId() + "-Star-VL")
                .setNominalV(t3w.getRatedU0())
                .setTopologyKind(topologykind)
                .add();
    }

    private static void createTopologyInsideStarVoltageLevel(ThreeWindingsTransformer t3w, VoltageLevel starVoltageLevel) {
        if (starVoltageLevel.getTopologyKind() == TopologyKind.BUS_BREAKER) {
            starVoltageLevel.getBusBreakerView().newBus()
                    .setId(t3w.getId() + "-Star-Bus")
                    .setName(t3w.getNameOrId() + "-Star-Bus")
                    .add();
        } else {
            starVoltageLevel.getNodeBreakerView().newInternalConnection().setNode1(1).setNode2(0).add();
            starVoltageLevel.getNodeBreakerView().newInternalConnection().setNode1(2).setNode2(0).add();
            starVoltageLevel.getNodeBreakerView().newInternalConnection().setNode1(3).setNode2(0).add();
        }
    }

    private static TwoWindingsTransformer createTwoWindingsTransformer(ThreeWindingsTransformer t3w, ThreeWindingsTransformer.Leg leg, VoltageLevel starVoltageLevel) {
        TwoWindingsTransformerAdder t2wAdder = starVoltageLevel.getSubstation().orElseThrow()
                .newTwoWindingsTransformer()
                .setEnsureIdUnicity(true)
                .setId(t3w.getId() + "-Leg" + leg.getSide().getNum())
                .setName(t3w.getNameOrId() + "-Leg" + leg.getSide().getNum())
                .setRatedU1(leg.getRatedU())
                .setRatedU2(starVoltageLevel.getNominalV())
                .setR(leg.getR())
                .setX(leg.getX())
                .setG(leg.getG())
                .setB(leg.getB())
                .setRatedS(leg.getRatedS())
                .setVoltageLevel1(leg.getTerminal().getVoltageLevel().getId())
                .setVoltageLevel2(starVoltageLevel.getId());

        connect(t2wAdder, getConnectivityLegAfterCreatingInternalConnection(leg), getConnectivityStar(leg.getSide().getNum(), starVoltageLevel));
        TwoWindingsTransformer t2w = t2wAdder.add();

        leg.getOptionalRatioTapChanger().ifPresent(rtc -> copyAndAddRatioTapChanger(t2w.newRatioTapChanger(), rtc));
        leg.getOptionalPhaseTapChanger().ifPresent(rtc -> copyAndAddPhaseTapChanger(t2w.newPhaseTapChanger(), rtc));
        leg.getOperationalLimitsGroups().forEach(operationalLimitGroup -> copyOperationalLimitsGroup(t2w.newOperationalLimitsGroup1(operationalLimitGroup.getId()), operationalLimitGroup));

        return t2w;
    }

    private static void connect(TwoWindingsTransformerAdder t2wAdder, ConnectivityR connectivityEnd1, ConnectivityR connectivityEnd2) {
        if (connectivityEnd1.node != null) {
            t2wAdder.setNode1(connectivityEnd1.node);
        } else {
            t2wAdder.setConnectableBus1(connectivityEnd1.connectableBus.getId());
            if (connectivityEnd1.bus != null) {
                t2wAdder.setBus1(connectivityEnd1.bus.getId());
            }
        }

        if (connectivityEnd2.node != null) {
            t2wAdder.setNode2(connectivityEnd2.node);
        } else {
            t2wAdder.setConnectableBus2(connectivityEnd2.connectableBus.getId());
            if (connectivityEnd2.bus != null) {
                t2wAdder.setBus2(connectivityEnd2.bus.getId());
            }
        }
    }

    private static ConnectivityR getConnectivityLegAfterCreatingInternalConnection(ThreeWindingsTransformer.Leg leg) {
        if (leg.getTerminal().getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            int newNode = leg.getTerminal().getVoltageLevel().getNodeBreakerView().getMaximumNodeIndex() + 1;
            leg.getTerminal().getVoltageLevel().getNodeBreakerView()
                    .newInternalConnection()
                    .setNode1(leg.getTerminal().getNodeBreakerView().getNode())
                    .setNode2(newNode).add();
            return new ConnectivityR(newNode, null, null);
        } else {
            return new ConnectivityR(null, leg.getTerminal().getBusBreakerView().getBus(), leg.getTerminal().getBusBreakerView().getConnectableBus());
        }
    }

    private static ConnectivityR getConnectivityStar(int node, VoltageLevel startVoltageLevel) {
        if (startVoltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            return new ConnectivityR(node, null, null);
        } else {
            Bus bus = startVoltageLevel.getBusBreakerView().getBuses().iterator().next();
            return new ConnectivityR(null, bus, bus);
        }
    }

    private record ConnectivityR(Integer node, Bus bus, Bus connectableBus) {
    }

    private static void replaceRegulatedTerminal(TwoWindingsTransformer t2w, ThreeWindingsTransformer t3w, ThreeT2wsR threeT2ws) {
        t2w.getOptionalRatioTapChanger().ifPresent(rtc -> findNewRegulatedTerminal(rtc.getRegulationTerminal(), t3w, threeT2ws).ifPresent(rtc::setRegulationTerminal));
        t2w.getOptionalPhaseTapChanger().ifPresent(ptc -> findNewRegulatedTerminal(ptc.getRegulationTerminal(), t3w, threeT2ws).ifPresent(ptc::setRegulationTerminal));
    }

    private static Optional<Terminal> findNewRegulatedTerminal(Terminal regulatedTerminal, ThreeWindingsTransformer t3w, ThreeT2wsR threeT2ws) {
        if (regulatedTerminal != null && regulatedTerminal.getConnectable().getId().equals(t3w.getId())) {
            return switch (regulatedTerminal.getSide()) {
                case ONE -> Optional.of(threeT2ws.t2wOne.getTerminal1());
                case TWO -> Optional.of(threeT2ws.t2wTwo.getTerminal1());
                case THREE -> Optional.of(threeT2ws.t2wThree.getTerminal1());
            };
        } else {
            return Optional.empty();
        }
    }

    private static List<String> copyProperties(ThreeWindingsTransformer t3w, ThreeT2wsR threeT2ws, VoltageLevel starVoltageLevel) {
        List<String> lostProperties = new ArrayList<>();
        t3w.getPropertyNames().forEach(propertyName -> {
            boolean copied = copyProperty(propertyName, t3w.getProperty(propertyName), threeT2ws, starVoltageLevel);
            if (!copied) {
                lostProperties.add(propertyName);
            }
        });
        return lostProperties;
    }

    private static boolean copyProperty(String propertyName, String property, ThreeT2wsR threeT2ws, VoltageLevel starVoltageLevel) {
        boolean copied = true;
        if ("v".equals(propertyName)) {
            starVoltageLevel.getBusView().getBuses().iterator().next().setV(Double.parseDouble(property));
        } else if ("angle".equals(propertyName)) {
            starVoltageLevel.getBusView().getBuses().iterator().next().setAngle(Double.parseDouble(property));
        } else if (propertyName.startsWith(CGMES_OPERATIONAL_LIMIT_SET)) {
            if (threeT2ws.t2wOne.getOperationalLimitsGroups1().stream().anyMatch(operationalLimitsGroup -> propertyName.equals(CGMES_OPERATIONAL_LIMIT_SET + operationalLimitsGroup.getId()))) {
                threeT2ws.t2wOne.setProperty(propertyName, property);
            } else if (threeT2ws.t2wTwo.getOperationalLimitsGroups1().stream().anyMatch(operationalLimitsGroup -> propertyName.equals(CGMES_OPERATIONAL_LIMIT_SET + operationalLimitsGroup.getId()))) {
                threeT2ws.t2wTwo.setProperty(propertyName, property);
            } else if (threeT2ws.t2wThree.getOperationalLimitsGroups1().stream().anyMatch(operationalLimitsGroup -> propertyName.equals(CGMES_OPERATIONAL_LIMIT_SET + operationalLimitsGroup.getId()))) {
                threeT2ws.t2wThree.setProperty(propertyName, property);
            } else {
                copied = false;
            }
        } else {
            copied = false;
        }
        return copied;
    }

    // TODO For now, only a few extensions are supported. But a wider mechanism should be developed to support custom extensions.
    private static List<String> copyExtensions(ThreeWindingsTransformer t3w, ThreeT2wsR threeT2w) {
        List<String> lostExtensions = new ArrayList<>();
        t3w.getExtensions().stream().map(Extension::getName).forEach(extensionName -> {
            boolean copied = copyExtension(extensionName, t3w, threeT2w);
            if (!copied) {
                lostExtensions.add(extensionName);
            }
        });
        return lostExtensions;
    }

    private static boolean copyExtension(String extensionName, ThreeWindingsTransformer t3w, ThreeT2wsR threeT2ws) {
        boolean copied = true;
        switch (extensionName) {
            case "threeWindingsTransformerFortescue" -> {
                ThreeWindingsTransformerFortescue extension = t3w.getExtension(ThreeWindingsTransformerFortescue.class);
                copyAndAddFortescue(threeT2ws.t2wOne.newExtension(TwoWindingsTransformerFortescueAdder.class), extension.getLeg1());
                copyAndAddFortescue(threeT2ws.t2wTwo.newExtension(TwoWindingsTransformerFortescueAdder.class), extension.getLeg2());
                copyAndAddFortescue(threeT2ws.t2wThree.newExtension(TwoWindingsTransformerFortescueAdder.class), extension.getLeg3());
            }
            case "threeWindingsTransformerPhaseAngleClock" -> {
                ThreeWindingsTransformerPhaseAngleClock extension = t3w.getExtension(ThreeWindingsTransformerPhaseAngleClock.class);
                copyAndAddPhaseAngleClock(threeT2ws.t2wTwo.newExtension(TwoWindingsTransformerPhaseAngleClockAdder.class), extension.getPhaseAngleClockLeg2());
                copyAndAddPhaseAngleClock(threeT2ws.t2wThree.newExtension(TwoWindingsTransformerPhaseAngleClockAdder.class), extension.getPhaseAngleClockLeg3());
            }
            case "threeWindingsTransformerToBeEstimated" -> {
                ThreeWindingsTransformerToBeEstimated extension = t3w.getExtension(ThreeWindingsTransformerToBeEstimated.class);
                copyAndAddToBeEstimated(threeT2ws.t2wOne.newExtension(TwoWindingsTransformerToBeEstimatedAdder.class), extension.shouldEstimateRatioTapChanger1(), extension.shouldEstimatePhaseTapChanger1());
                copyAndAddToBeEstimated(threeT2ws.t2wTwo.newExtension(TwoWindingsTransformerToBeEstimatedAdder.class), extension.shouldEstimateRatioTapChanger2(), extension.shouldEstimatePhaseTapChanger2());
                copyAndAddToBeEstimated(threeT2ws.t2wThree.newExtension(TwoWindingsTransformerToBeEstimatedAdder.class), extension.shouldEstimateRatioTapChanger3(), extension.shouldEstimatePhaseTapChanger3());
            }
            default -> copied = false;
        }
        return copied;
    }

    private static List<AliasR> getAliases(ThreeWindingsTransformer t3w) {
        return t3w.getAliases().stream().map(alias -> new AliasR(alias, t3w.getAliasType(alias).orElse(""))).toList();
    }

    private static List<AliasR> copyAliases(List<AliasR> t3wAliases, ThreeT2wsR threeT2w) {
        List<AliasR> lostAliases = new ArrayList<>();
        t3wAliases.forEach(aliasR -> {
            boolean copied = copyAlias(aliasR.alias, aliasR.aliasType, threeT2w);
            if (!copied) {
                lostAliases.add(aliasR);
            }
        });
        return lostAliases;
    }

    private static boolean copyAlias(String alias, String aliasType, ThreeT2wsR threeT2ws) {
        return copyLegAlias(alias, aliasType, "1", threeT2ws.t2wOne)
                || copyLegAlias(alias, aliasType, "2", threeT2ws.t2wTwo)
                || copyLegAlias(alias, aliasType, "3", threeT2ws.t2wThree);
    }

    private static boolean copyLegAlias(String alias, String aliasType, String legEnd, TwoWindingsTransformer t2wLeg) {
        boolean copied = true;
        if (aliasType.equals("CGMES.TransformerEnd" + legEnd)) {
            t2wLeg.addAlias(alias, "CGMES.TransformerEnd1", true);
        } else if (aliasType.equals("CGMES.Terminal" + legEnd)) {
            t2wLeg.addAlias(alias, "CGMES.Terminal1", true);
        } else if (aliasType.equals("CGMES.RatioTapChanger" + legEnd)) {
            t2wLeg.addAlias(alias, "CGMES.RatioTapChanger1", true);
        } else if (aliasType.equals("CGMES.PhaseTapChanger" + legEnd)) {
            t2wLeg.addAlias(alias, "CGMES.PhaseTapChanger1", true);
        } else {
            copied = false;
        }
        return copied;
    }

    private record AliasR(String alias, String aliasType) {
    }

    private static void createReportNode(ReportNode reportNode, String t3wId, List<String> lostProperties, List<String> lostExtensions,
                                         List<AliasR> lostAliases, String starVoltageLevelId, ThreeT2wsR threeT2ws) {

        ReportNode reportNodeReplacement = replaceThreeWindingsTransformersBy3TwoWindingsTransformersReport(reportNode);

        removedThreeWindingsTransformerReport(reportNodeReplacement, t3wId);
        if (!lostProperties.isEmpty()) {
            String properties = String.join(",", lostProperties);
            lostThreeWindingsTransformerProperties(reportNodeReplacement, properties, t3wId);
        }
        if (!lostExtensions.isEmpty()) {
            String extensions = String.join(",", lostExtensions);
            lostThreeWindingsTransformerExtensions(reportNodeReplacement, extensions, t3wId);
        }
        if (!lostAliases.isEmpty()) {
            String aliases = lostAliases.stream().map(AliasR::alias).collect(Collectors.joining(","));
            lostThreeWindingsTransformerAliases(reportNodeReplacement, aliases, t3wId);
        }

        createdVoltageLevelReport(reportNodeReplacement, starVoltageLevelId);
        createdTwoWindingsTransformerReport(reportNodeReplacement, threeT2ws.t2wOne.getId());
        createdTwoWindingsTransformerReport(reportNodeReplacement, threeT2ws.t2wTwo.getId());
        createdTwoWindingsTransformerReport(reportNodeReplacement, threeT2ws.t2wThree.getId());
    }

    private record ThreeT2wsR(TwoWindingsTransformer t2wOne, TwoWindingsTransformer t2wTwo, TwoWindingsTransformer t2wThree) {
    }
}
