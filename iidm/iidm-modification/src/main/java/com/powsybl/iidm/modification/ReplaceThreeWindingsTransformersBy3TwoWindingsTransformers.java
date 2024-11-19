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
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers extends AbstractNetworkModification {

    private static final String THREE_WINDINGS_TRANSFORMER = "ThreeWindingsTransformer";

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

        regulatedTerminalControllers.replaceRegulatedTerminal(t3w.getLeg1().getTerminal(), t2wLeg1.getTerminal1());
        regulatedTerminalControllers.replaceRegulatedTerminal(t3w.getLeg2().getTerminal(), t2wLeg2.getTerminal1());
        regulatedTerminalControllers.replaceRegulatedTerminal(t3w.getLeg3().getTerminal(), t2wLeg3.getTerminal1());

        // t2wLeg1, t2wLeg, and t2wLeg3 are not considered in regulatedTerminalControllers (created in the model later)
        Map<Terminal, Terminal> regulatedTerminalMapping = getRegulatedTerminalMapping(t3w, t2wLeg1, t2wLeg2, t2wLeg3);
        replaceRegulatedTerminal(t2wLeg1, regulatedTerminalMapping);
        replaceRegulatedTerminal(t2wLeg2, regulatedTerminalMapping);
        replaceRegulatedTerminal(t2wLeg3, regulatedTerminalMapping);

        copyTerminalActiveAndReactivePower(t2wLeg1.getTerminal1(), t3w.getLeg1().getTerminal());
        copyTerminalActiveAndReactivePower(t2wLeg2.getTerminal1(), t3w.getLeg2().getTerminal());
        copyTerminalActiveAndReactivePower(t2wLeg3.getTerminal1(), t3w.getLeg3().getTerminal());

        List<String> lostProperties = copyProperties(t3w, t2wLeg1, t2wLeg2, t2wLeg3, starVoltageLevel);
        List<String> lostExtensions = copyExtensions(t3w, t2wLeg1, t2wLeg2, t2wLeg3);

        // copy necessary data before removing the transformer
        String t3wId = t3w.getId();
        List<AliasR> t3wAliases = getAliases(t3w);
        t3w.remove();

        // after removing the threeWindingsTransformer
        List<AliasR> lostAliases = copyAliases(t3wAliases, t2wLeg1, t2wLeg2, t2wLeg3);

        // warnings
        if (!lostProperties.isEmpty()) {
            lostProperties.forEach(propertyName -> logOrThrow(throwException, THREE_WINDINGS_TRANSFORMER + "'" + t3wId + "' unexpected property '" + propertyName + "'"));
        }
        if (!lostExtensions.isEmpty()) {
            lostExtensions.forEach(extensionName -> logOrThrow(throwException, THREE_WINDINGS_TRANSFORMER + "'" + t3wId + "' unexpected extension '" + extensionName + '"'));
        }
        if (!lostAliases.isEmpty()) {
            lostAliases.forEach(aliasR -> logOrThrow(throwException, THREE_WINDINGS_TRANSFORMER + "'" + t3wId + "' unexpected alias '" + aliasR.alias + "' '" + aliasR.aliasType + "'"));
        }

        // report
        createReportNode(reportNode, t3wId, lostProperties, lostExtensions, lostAliases, starVoltageLevel.getId(), t2wLeg1.getId(), t2wLeg2.getId(), t2wLeg3.getId());
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
        if (startVoltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            return new ConnectivityR(node, null, null);
        } else {
            Bus bus = startVoltageLevel.getBusBreakerView().getBuses().iterator().next();
            return new ConnectivityR(null, bus, bus);
        }
    }

    private record ConnectivityR(Integer node, Bus bus, Bus connectableBus) {
    }

    private static Map<Terminal, Terminal> getRegulatedTerminalMapping(ThreeWindingsTransformer t3w, TwoWindingsTransformer t2w1, TwoWindingsTransformer t2w2, TwoWindingsTransformer t2w3) {
        Map<Terminal, Terminal> regulatedTerminalMapping = new HashMap<>();
        regulatedTerminalMapping.put(t3w.getLeg1().getTerminal(), t2w1.getTerminal1());
        regulatedTerminalMapping.put(t3w.getLeg2().getTerminal(), t2w2.getTerminal1());
        regulatedTerminalMapping.put(t3w.getLeg3().getTerminal(), t2w3.getTerminal1());
        return regulatedTerminalMapping;
    }

    private static void replaceRegulatedTerminal(TwoWindingsTransformer t2w, Map<Terminal, Terminal> regulatedTerminalMapping) {
        t2w.getOptionalRatioTapChanger().ifPresent(rtc -> {
            if (regulatedTerminalMapping.containsKey(rtc.getRegulationTerminal())) {
                rtc.setRegulationTerminal(regulatedTerminalMapping.get(rtc.getRegulationTerminal()));
            }
        });
        t2w.getOptionalPhaseTapChanger().ifPresent(ptc -> {
            if (regulatedTerminalMapping.containsKey(ptc.getRegulationTerminal())) {
                ptc.setRegulationTerminal(regulatedTerminalMapping.get(ptc.getRegulationTerminal()));
            }
        });
    }

    private static List<String> copyProperties(ThreeWindingsTransformer t3w, TwoWindingsTransformer t2wLeg1, TwoWindingsTransformer t2wLeg2, TwoWindingsTransformer t2wLeg3, VoltageLevel starVoltageLevel) {
        List<String> lostProperties = new ArrayList<>();
        t3w.getPropertyNames().forEach(propertyName -> {
            boolean copied = copyProperty(propertyName, t3w.getProperty(propertyName), t2wLeg1, t2wLeg2, t2wLeg3, starVoltageLevel);
            if (!copied) {
                lostProperties.add(propertyName);
            }
        });
        return lostProperties;
    }

    private static boolean copyProperty(String propertyName, String property, TwoWindingsTransformer t2wLeg1, TwoWindingsTransformer t2wLeg2, TwoWindingsTransformer t2wLeg3, VoltageLevel starVoltageLevel) {
        boolean copied = true;
        if (propertyName.equals("v")) {
            starVoltageLevel.getBusView().getBuses().iterator().next().setV(Double.parseDouble(property));
        } else if (propertyName.equals("angle")) {
            starVoltageLevel.getBusView().getBuses().iterator().next().setAngle(Double.parseDouble(property));
        } else if (propertyName.startsWith("CGMES.OperationalLimitSet_")) {
            if (t2wLeg1.getOperationalLimitsGroups1().stream().anyMatch(operationalLimitsGroup -> propertyName.contains(operationalLimitsGroup.getId()))) {
                t2wLeg1.setProperty(propertyName, property);
            } else if (t2wLeg2.getOperationalLimitsGroups1().stream().anyMatch(operationalLimitsGroup -> propertyName.contains(operationalLimitsGroup.getId()))) {
                t2wLeg2.setProperty(propertyName, property);
            } else if (t2wLeg3.getOperationalLimitsGroups1().stream().anyMatch(operationalLimitsGroup -> propertyName.contains(operationalLimitsGroup.getId()))) {
                t2wLeg3.setProperty(propertyName, property);
            } else {
                copied = false;
            }
        } else {
            copied = false;
        }
        return copied;
    }

    private static List<String> copyExtensions(ThreeWindingsTransformer t3w, TwoWindingsTransformer t2wLeg1, TwoWindingsTransformer t2wLeg2, TwoWindingsTransformer t2wLeg3) {
        List<String> lostExtensions = new ArrayList<>();
        t3w.getExtensions().stream().map(Extension::getName).forEach(extensionName -> {
            boolean copied = copyExtension(extensionName, t3w, t2wLeg1, t2wLeg2, t2wLeg3);
            if (!copied) {
                lostExtensions.add(extensionName);
            }
        });
        return lostExtensions;
    }

    private static boolean copyExtension(String extensionName, ThreeWindingsTransformer t3w, TwoWindingsTransformer t2w1, TwoWindingsTransformer t2w2, TwoWindingsTransformer t2w3) {
        boolean copied = true;
        switch (extensionName) {
            case "threeWindingsTransformerFortescue" -> {
                ThreeWindingsTransformerFortescue extension = t3w.getExtension(ThreeWindingsTransformerFortescue.class);
                copyAndAddFortescue(t2w1.newExtension(TwoWindingsTransformerFortescueAdder.class), extension.getLeg1());
                copyAndAddFortescue(t2w2.newExtension(TwoWindingsTransformerFortescueAdder.class), extension.getLeg2());
                copyAndAddFortescue(t2w3.newExtension(TwoWindingsTransformerFortescueAdder.class), extension.getLeg3());
            }
            case "threeWindingsTransformerPhaseAngleClock" -> {
                ThreeWindingsTransformerPhaseAngleClock extension = t3w.getExtension(ThreeWindingsTransformerPhaseAngleClock.class);
                copyAndAddPhaseAngleClock(t2w2.newExtension(TwoWindingsTransformerPhaseAngleClockAdder.class), extension.getPhaseAngleClockLeg2());
                copyAndAddPhaseAngleClock(t2w3.newExtension(TwoWindingsTransformerPhaseAngleClockAdder.class), extension.getPhaseAngleClockLeg3());
            }
            case "threeWindingsTransformerToBeEstimated" -> {
                ThreeWindingsTransformerToBeEstimated extension = t3w.getExtension(ThreeWindingsTransformerToBeEstimated.class);
                copyAndAddToBeEstimated(t2w1.newExtension(TwoWindingsTransformerToBeEstimatedAdder.class), extension.shouldEstimateRatioTapChanger1(), extension.shouldEstimatePhaseTapChanger1());
                copyAndAddToBeEstimated(t2w2.newExtension(TwoWindingsTransformerToBeEstimatedAdder.class), extension.shouldEstimateRatioTapChanger2(), extension.shouldEstimatePhaseTapChanger2());
                copyAndAddToBeEstimated(t2w3.newExtension(TwoWindingsTransformerToBeEstimatedAdder.class), extension.shouldEstimateRatioTapChanger3(), extension.shouldEstimatePhaseTapChanger3());
            }
            default -> copied = false;
        }
        return copied;
    }

    private static List<AliasR> getAliases(ThreeWindingsTransformer t3w) {
        return t3w.getAliases().stream().map(alias -> new AliasR(alias, t3w.getAliasType(alias).orElse(""))).toList();
    }

    private static List<AliasR> copyAliases(List<AliasR> t3wAliases, TwoWindingsTransformer t2wLeg1, TwoWindingsTransformer t2wLeg2, TwoWindingsTransformer t2wLeg3) {
        List<AliasR> lostAliases = new ArrayList<>();
        t3wAliases.forEach(aliasR -> {
            boolean copied = copyAlias(aliasR.alias, aliasR.aliasType, t2wLeg1, t2wLeg2, t2wLeg3);
            if (!copied) {
                lostAliases.add(aliasR);
            }
        });
        return lostAliases;
    }

    private static boolean copyAlias(String alias, String aliasType, TwoWindingsTransformer t2wLeg1, TwoWindingsTransformer t2wLeg2, TwoWindingsTransformer t2wLeg3) {
        return copyLegAlias(alias, aliasType, "1", t2wLeg1)
                || copyLegAlias(alias, aliasType, "2", t2wLeg2)
                || copyLegAlias(alias, aliasType, "3", t2wLeg3);
    }

    private static boolean copyLegAlias(String alias, String aliasType, String legEnd, TwoWindingsTransformer t2wLeg) {
        boolean copied = true;
        if (aliasType.equals("CGMES.TransformerEnd" + legEnd)) {
            t2wLeg.addAlias(alias, "CGMES.TransformerEnd1");
        } else if (aliasType.equals("CGMES.Terminal" + legEnd)) {
            t2wLeg.addAlias(alias, "CGMES.Terminal1");
        } else if (aliasType.equals("CGMES.RatioTapChanger" + legEnd)) {
            t2wLeg.addAlias(alias, "CGMES.RatioTapChanger1");
        } else if (aliasType.equals("CGMES.PhaseTapChanger" + legEnd)) {
            t2wLeg.addAlias(alias, "CGMES.PhaseTapChanger1");
        } else {
            copied = false;
        }
        return copied;
    }

    private record AliasR(String alias, String aliasType) {
    }

    private static void createReportNode(ReportNode reportNode, String t3wId, List<String> lostProperties, List<String> lostExtensions, List<AliasR> lostAliases,
                                         String starVoltageLevelId, String t2w1Id, String t2w2Id, String t2w3Id) {

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
        createdTwoWindingsTransformerReport(reportNodeReplacement, t2w1Id);
        createdTwoWindingsTransformerReport(reportNodeReplacement, t2w2Id);
        createdTwoWindingsTransformerReport(reportNodeReplacement, t2w3Id);
    }
}
