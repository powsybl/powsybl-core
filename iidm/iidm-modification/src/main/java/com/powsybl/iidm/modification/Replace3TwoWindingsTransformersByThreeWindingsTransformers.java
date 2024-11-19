/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

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

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class Replace3TwoWindingsTransformersByThreeWindingsTransformers extends AbstractNetworkModification {

    private static final String TWO_WINDINGS_TRANSFORMER = "TwoWindingsTransformer";
    private static final String WITH_FICTITIOUS_TERMINAL_USED_AS_REGULATED_TERMINAL = "with fictitious terminal used as regulated terminal";

    @Override
    public String getName() {
        return "Replace3TwoWindingsTransformersByThreeWindingsTransformers";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        RegulatedTerminalControllers regulatedTerminalControllers = new RegulatedTerminalControllers(network);
        List<TwoR> twoWindingsTransformers = find3TwoWindingsTransformers(network);
        twoWindingsTransformers.forEach(twoR -> replace3TwoWindingsTransformerByThreeWindingsTransformer(twoR, regulatedTerminalControllers, throwException, reportNode));
    }

    private static List<TwoR> find3TwoWindingsTransformers(Network network) {
        Map<Bus, List<TwoWindingsTransformer>> twoWindingTransformersByBus = new HashMap<>();
        network.getTwoWindingsTransformers().forEach(t2w -> {
            Bus bus1 = t2w.getTerminal1().getBusView().getBus();
            Bus bus2 = t2w.getTerminal2().getBusView().getBus();
            if (bus1 != null) {
                twoWindingTransformersByBus.computeIfAbsent(bus1, k -> new ArrayList<>()).add(t2w);
            }
            if (bus2 != null) {
                twoWindingTransformersByBus.computeIfAbsent(bus2, k -> new ArrayList<>()).add(t2w);
            }
        });
        return twoWindingTransformersByBus.keySet().stream()
                .filter(bus -> isStarBus(bus, twoWindingTransformersByBus.get(bus)))
                .sorted(Comparator.comparing(Identifiable::getId))
                .map(bus -> buildTwoR(bus, twoWindingTransformersByBus.get(bus))).toList();
    }

    private static boolean isStarBus(Bus bus, List<TwoWindingsTransformer> t2ws) {
        return t2ws.size() == 3 && bus.getConnectedTerminalStream().filter(connectedTerminal -> !connectedTerminal.getConnectable().getType().equals(IdentifiableType.BUSBAR_SECTION)).count() == 3;
    }

    private static TwoR buildTwoR(Bus bus, List<TwoWindingsTransformer> t2ws) {
        List<TwoWindingsTransformer> sortedT2ws = t2ws.stream()
                .sorted(Comparator.comparingDouble((TwoWindingsTransformer t2w) -> getNominalV(bus, t2w))
                        .reversed()
                        .thenComparing(Identifiable::getId))
                .toList();

        return new TwoR(bus, sortedT2ws.get(0), sortedT2ws.get(1), sortedT2ws.get(2));
    }

    private static double getNominalV(Bus bus, TwoWindingsTransformer t2w) {
        return bus.equals(t2w.getTerminal1().getBusView().getBus()) ? t2w.getTerminal2().getVoltageLevel().getNominalV() : t2w.getTerminal1().getVoltageLevel().getNominalV();
    }

    private record TwoR(Bus starBus, TwoWindingsTransformer t2w1, TwoWindingsTransformer t2w2,
                        TwoWindingsTransformer t2w3) {
    }

    // if the twoWindingsTransformer is not well oriented, and it has non-zero shunt admittance (G != 0 or B != 0)
    // the obtained model is not equivalent to the initial one as the shunt admittance must be moved to the other side
    private void replace3TwoWindingsTransformerByThreeWindingsTransformer(TwoR twoR, RegulatedTerminalControllers regulatedTerminalControllers, boolean throwException, ReportNode reportNode) {
        Substation substation = findSubstation(twoR, throwException);
        if (substation == null) {
            return;
        }
        if (anyTwoWindingsTransformerRegulatedOnFictitiousSide(twoR, regulatedTerminalControllers, throwException)) {
            return;
        }
        double ratedU0 = twoR.starBus.getVoltageLevel().getNominalV();
        boolean isWellOrientedT2w1 = isWellOriented(twoR.starBus, twoR.t2w1);
        boolean isWellOrientedT2w2 = isWellOriented(twoR.starBus, twoR.t2w2);
        boolean isWellOrientedT2w3 = isWellOriented(twoR.starBus, twoR.t2w3);

        ThreeWindingsTransformerAdder t3wAdder = substation.newThreeWindingsTransformer()
                .setId(getId(twoR))
                .setName(getName(twoR))
                .setRatedU0(ratedU0);

        addLegAdder(t3wAdder.newLeg1(), twoR.t2w1, isWellOrientedT2w1, ratedU0);
        addLegAdder(t3wAdder.newLeg2(), twoR.t2w2, isWellOrientedT2w2, ratedU0);
        addLegAdder(t3wAdder.newLeg3(), twoR.t2w3, isWellOrientedT2w3, ratedU0);
        ThreeWindingsTransformer t3w = t3wAdder.add();

        // t3w is not considered in regulatedTerminalControllers (created in the model later)
        Map<Terminal, Terminal> regulatedTerminalMapping = getRegulatedTerminalMapping(twoR, t3w);
        setLegData(t3w.getLeg1(), twoR.t2w1, isWellOrientedT2w1, regulatedTerminalControllers, regulatedTerminalMapping);
        setLegData(t3w.getLeg2(), twoR.t2w2, isWellOrientedT2w2, regulatedTerminalControllers, regulatedTerminalMapping);
        setLegData(t3w.getLeg3(), twoR.t2w3, isWellOrientedT2w3, regulatedTerminalControllers, regulatedTerminalMapping);

        copyStarBusVoltageAndAngle(twoR.starBus, t3w);
        List<PropertyR> lostProperties = new ArrayList<>();
        lostProperties.addAll(copyProperties(twoR.t2w1, t3w));
        lostProperties.addAll(copyProperties(twoR.t2w2, t3w));
        lostProperties.addAll(copyProperties(twoR.t2w3, t3w));

        List<ExtensionR> lostExtensions = copyExtensions(twoR, t3w);

        // copy necessary data before removing
        List<AliasR> t2wAliases = new ArrayList<>();
        t2wAliases.addAll(getAliases(twoR.t2w1, "1", getEnd1(isWellOrientedT2w1)));
        t2wAliases.addAll(getAliases(twoR.t2w2, "2", getEnd1(isWellOrientedT2w2)));
        t2wAliases.addAll(getAliases(twoR.t2w3, "3", getEnd1(isWellOrientedT2w3)));

        String t2w1Id = twoR.t2w1.getId();
        String t2w2Id = twoR.t2w2.getId();
        String t2w3Id = twoR.t2w3.getId();
        String starVoltageId = twoR.starBus.getVoltageLevel().getId();
        List<LimitsR> lostLimits = findLostLimits(twoR);

        remove(twoR);

        // after removing
        List<AliasR> lostAliases = copyAliases(t2wAliases, t3w);

        // warnings
        if (!lostProperties.isEmpty()) {
            lostProperties.forEach(propertyR -> logOrThrow(throwException, TWO_WINDINGS_TRANSFORMER + "'" + propertyR.t2wId + "' unexpected property '" + propertyR.propertyName + "'"));
        }
        if (!lostExtensions.isEmpty()) {
            lostExtensions.forEach(extensionR -> logOrThrow(throwException, TWO_WINDINGS_TRANSFORMER + "'" + extensionR.t2wId + "' unexpected extension '" + extensionR.extensionName + '"'));
        }
        if (!lostAliases.isEmpty()) {
            lostAliases.forEach(aliasR -> logOrThrow(throwException, TWO_WINDINGS_TRANSFORMER + "'" + aliasR.t2wId + "' unexpected alias '" + aliasR.alias + "' '" + aliasR.aliasType + "'"));
        }
        if (!lostLimits.isEmpty()) {
            lostLimits.forEach(limitsR -> logOrThrow(throwException, TWO_WINDINGS_TRANSFORMER + "'" + limitsR.t2wId + "' operationalLimitsGroup '" + limitsR.operationalLimitsGroupName + "' is lost"));
        }

        // report
        createReportNode(reportNode, t2w1Id, t2w2Id, t2w3Id, starVoltageId, lostProperties, lostExtensions, lostAliases, lostLimits, t3w.getId());
    }

    private static void addLegAdder(ThreeWindingsTransformerAdder.LegAdder legAdder, TwoWindingsTransformer t2w, boolean isWellOriented, double ratedU0) {
        legAdder.setVoltageLevel(findVoltageLevel(t2w, isWellOriented).getId())
                .setR(findImpedance(t2w.getR(), getStructuralRatio(t2w), isWellOriented))
                .setX(findImpedance(t2w.getX(), getStructuralRatio(t2w), isWellOriented))
                .setG(findAdmittance(t2w.getG(), getStructuralRatio(t2w), isWellOriented))
                .setB(findAdmittance(t2w.getB(), getStructuralRatio(t2w), isWellOriented))
                .setRatedU(getRatedU1(t2w, ratedU0, isWellOriented));
        connectAfterCreatingInternalConnection(legAdder, t2w, isWellOriented);
        legAdder.add();
    }

    private static void setLegData(ThreeWindingsTransformer.Leg leg, TwoWindingsTransformer t2w, boolean isWellOriented, RegulatedTerminalControllers regulatedTerminalControllers, Map<Terminal, Terminal> regulatedTerminalMapping) {
        t2w.getOptionalRatioTapChanger().ifPresent(rtc -> copyOrMoveRatioTapChanger(leg.newRatioTapChanger(), rtc, isWellOriented));
        t2w.getOptionalPhaseTapChanger().ifPresent(ptc -> copyOrMovePhaseTapChanger(leg.newPhaseTapChanger(), ptc, isWellOriented));

        getOperationalLimitsGroups1(t2w, isWellOriented)
                .forEach(operationalLimitGroup -> copyOperationalLimitsGroup(leg.newOperationalLimitsGroup(operationalLimitGroup.getId()), operationalLimitGroup));

        regulatedTerminalControllers.replaceRegulatedTerminal(getTerminal1(t2w, isWellOriented), leg.getTerminal());
        replaceRegulatedTerminal(leg, regulatedTerminalMapping);

        copyTerminalActiveAndReactivePower(leg.getTerminal(), getTerminal1(t2w, isWellOriented));
    }

    private Substation findSubstation(TwoR twoR, boolean throwException) {
        Optional<Substation> substation = twoR.t2w1.getSubstation();
        if (substation.isEmpty()) {
            logOrThrow(throwException, TWO_WINDINGS_TRANSFORMER + "'" + twoR.t2w1.getId() + "' without substation");
            return null;
        } else {
            return substation.get();
        }
    }

    private boolean anyTwoWindingsTransformerRegulatedOnFictitiousSide(TwoR twoR, RegulatedTerminalControllers regulatedTerminalControllers, boolean throwException) {
        if (regulatedTerminalControllers.usedAsRegulatedTerminal(getTerminal2(twoR.t2w1, isWellOriented(twoR.starBus, twoR.t2w1)))) {
            logOrThrow(throwException, TWO_WINDINGS_TRANSFORMER + "'" + twoR.t2w1.getId() + "' " + WITH_FICTITIOUS_TERMINAL_USED_AS_REGULATED_TERMINAL);
            return true;
        }
        if (regulatedTerminalControllers.usedAsRegulatedTerminal(getTerminal2(twoR.t2w2, isWellOriented(twoR.starBus, twoR.t2w2)))) {
            logOrThrow(throwException, TWO_WINDINGS_TRANSFORMER + "'" + twoR.t2w2.getId() + "' " + WITH_FICTITIOUS_TERMINAL_USED_AS_REGULATED_TERMINAL);
            return true;
        }
        if (regulatedTerminalControllers.usedAsRegulatedTerminal(getTerminal2(twoR.t2w3, isWellOriented(twoR.starBus, twoR.t2w3)))) {
            logOrThrow(throwException, TWO_WINDINGS_TRANSFORMER + "'" + twoR.t2w3.getId() + "' " + WITH_FICTITIOUS_TERMINAL_USED_AS_REGULATED_TERMINAL);
            return true;
        }
        return false;
    }

    private static String getId(TwoR twoR) {
        return twoR.t2w1.getId() + "-" + twoR.t2w2.getId() + "-" + twoR.t2w3.getId();
    }

    private static String getName(TwoR twoR) {
        return twoR.t2w1.getNameOrId() + "-" + twoR.t2w2.getNameOrId() + "-" + twoR.t2w3.getNameOrId();
    }

    // is well oriented when the star side is at end2
    private static boolean isWellOriented(Bus starBus, TwoWindingsTransformer t2w) {
        return starBus.getId().equals(t2w.getTerminal2().getBusView().getBus().getId());
    }

    private static VoltageLevel findVoltageLevel(TwoWindingsTransformer t2w, boolean isWellOriented) {
        return isWellOriented ? t2w.getTerminal1().getVoltageLevel() : t2w.getTerminal2().getVoltageLevel();
    }

    private static double getStructuralRatio(TwoWindingsTransformer twt) {
        return twt.getRatedU1() / twt.getRatedU2();
    }

    private static double findImpedance(double impedance, double a, boolean isWellOriented) {
        return isWellOriented ? impedance : impedanceConversion(impedance, a);
    }

    private static double findAdmittance(double admittance, double a, boolean isWellOriented) {
        return isWellOriented ? admittance : admittanceConversion(admittance, a);
    }

    private static double getRatedU1(TwoWindingsTransformer t2w, double ratedU0, boolean isWellOriented) {
        return isWellOriented ? getStructuralRatio(t2w) * ratedU0 : ratedU0 / getStructuralRatio(t2w);
    }

    private static void connectAfterCreatingInternalConnection(ThreeWindingsTransformerAdder.LegAdder legAdder, TwoWindingsTransformer t2w, boolean isWellOriented) {
        Terminal terminal = getTerminal1(t2w, isWellOriented);
        if (terminal.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            int newNode = terminal.getVoltageLevel().getNodeBreakerView().getMaximumNodeIndex() + 1;
            terminal.getVoltageLevel().getNodeBreakerView()
                    .newInternalConnection()
                    .setNode1(terminal.getNodeBreakerView().getNode())
                    .setNode2(newNode).add();
            legAdder.setNode(newNode);
        } else {
            legAdder.setConnectableBus(terminal.getBusBreakerView().getConnectableBus().getId());
            Bus bus = terminal.getBusBreakerView().getBus();
            if (bus != null) {
                legAdder.setBus(bus.getId());
            }
        }
    }

    private static void copyOrMoveRatioTapChanger(RatioTapChangerAdder rtcAdder, RatioTapChanger rtc, boolean isWellOriented) {
        if (isWellOriented) {
            copyAndAddRatioTapChanger(rtcAdder, rtc);
        } else {
            copyAndMoveAndAddRatioTapChanger(rtcAdder, rtc);
        }
    }

    private static void copyOrMovePhaseTapChanger(PhaseTapChangerAdder ptcAdder, PhaseTapChanger ptc, boolean isWellOriented) {
        if (isWellOriented) {
            copyAndAddPhaseTapChanger(ptcAdder, ptc);
        } else {
            copyAndMoveAndAddPhaseTapChanger(ptcAdder, ptc);
        }
    }

    private static Collection<OperationalLimitsGroup> getOperationalLimitsGroups1(TwoWindingsTransformer t2w, boolean isWellOriented) {
        return isWellOriented ? t2w.getOperationalLimitsGroups1() : t2w.getOperationalLimitsGroups2();
    }

    private static Terminal getTerminal1(TwoWindingsTransformer t2w, boolean isWellOriented) {
        return isWellOriented ? t2w.getTerminal1() : t2w.getTerminal2();
    }

    private static Terminal getTerminal2(TwoWindingsTransformer t2w, boolean isWellOriented) {
        return isWellOriented ? t2w.getTerminal2() : t2w.getTerminal1();
    }

    private static String getEnd1(boolean isWellOriented) {
        return isWellOriented ? "1" : "2";
    }

    private static Map<Terminal, Terminal> getRegulatedTerminalMapping(TwoR twoR, ThreeWindingsTransformer t3w) {
        Map<Terminal, Terminal> regulatedTerminalMapping = new HashMap<>();
        regulatedTerminalMapping.put(getTerminal1(twoR.t2w1, isWellOriented(twoR.starBus, twoR.t2w1)), t3w.getLeg1().getTerminal());
        regulatedTerminalMapping.put(getTerminal1(twoR.t2w2, isWellOriented(twoR.starBus, twoR.t2w2)), t3w.getLeg2().getTerminal());
        regulatedTerminalMapping.put(getTerminal1(twoR.t2w3, isWellOriented(twoR.starBus, twoR.t2w3)), t3w.getLeg3().getTerminal());
        return regulatedTerminalMapping;
    }

    private static void replaceRegulatedTerminal(ThreeWindingsTransformer.Leg t3wLeg, Map<Terminal, Terminal> regulatedTerminalMapping) {
        t3wLeg.getOptionalRatioTapChanger().ifPresent(rtc -> {
            if (regulatedTerminalMapping.containsKey(rtc.getRegulationTerminal())) {
                rtc.setRegulationTerminal(regulatedTerminalMapping.get(rtc.getRegulationTerminal()));
            }
        });
        t3wLeg.getOptionalPhaseTapChanger().ifPresent(ptc -> {
            if (regulatedTerminalMapping.containsKey(ptc.getRegulationTerminal())) {
                ptc.setRegulationTerminal(regulatedTerminalMapping.get(ptc.getRegulationTerminal()));
            }
        });
    }

    private static void copyStarBusVoltageAndAngle(Bus starBus, ThreeWindingsTransformer t3w) {
        if (Double.isFinite(starBus.getV()) && starBus.getV() > 0.0 && Double.isFinite(starBus.getAngle())) {
            t3w.setProperty("v", String.valueOf(starBus.getV()));
            t3w.setProperty("angle", String.valueOf(starBus.getAngle()));
        }
    }

    private static List<PropertyR> copyProperties(TwoWindingsTransformer t2w, ThreeWindingsTransformer t3w) {
        List<PropertyR> lostProperties = new ArrayList<>();
        t2w.getPropertyNames().forEach(propertyName -> {
            boolean copied = copyProperty(propertyName, t2w.getProperty(propertyName), t3w);
            if (!copied) {
                lostProperties.add(new PropertyR(t2w.getId(), propertyName));
            }
        });
        return lostProperties;
    }

    private static boolean copyProperty(String propertyName, String property, ThreeWindingsTransformer t3w) {
        boolean copied = true;
        if (propertyName.startsWith("CGMES.OperationalLimitSet_")) {
            if (t3w.getLeg1().getOperationalLimitsGroups().stream().anyMatch(operationalLimitsGroup -> propertyName.contains(operationalLimitsGroup.getId()))) {
                t3w.setProperty(propertyName, property);
            } else if (t3w.getLeg2().getOperationalLimitsGroups().stream().anyMatch(operationalLimitsGroup -> propertyName.contains(operationalLimitsGroup.getId()))) {
                t3w.setProperty(propertyName, property);
            } else if (t3w.getLeg3().getOperationalLimitsGroups().stream().anyMatch(operationalLimitsGroup -> propertyName.contains(operationalLimitsGroup.getId()))) {
                t3w.setProperty(propertyName, property);
            } else {
                copied = false;
            }
        } else {
            copied = false;
        }
        return copied;
    }

    private record PropertyR(String t2wId, String propertyName) {
    }

    private static List<ExtensionR> copyExtensions(TwoR twoR, ThreeWindingsTransformer t3w) {
        List<ExtensionR> extensions = new ArrayList<>();
        extensions.addAll(twoR.t2w1.getExtensions().stream().map(extension -> new ExtensionR(twoR.t2w1.getId(), extension.getName())).toList());
        extensions.addAll(twoR.t2w2.getExtensions().stream().map(extension -> new ExtensionR(twoR.t2w2.getId(), extension.getName())).toList());
        extensions.addAll(twoR.t2w3.getExtensions().stream().map(extension -> new ExtensionR(twoR.t2w3.getId(), extension.getName())).toList());

        List<ExtensionR> lostExtensions = new ArrayList<>();
        extensions.stream().map(extensionR -> extensionR.extensionName).collect(Collectors.toSet()).forEach(extensionName -> {
            boolean copied = copyExtension(extensionName, twoR, t3w);
            if (!copied) {
                lostExtensions.addAll(extensions.stream().filter(extensionR -> extensionR.extensionName.equals(extensionName)).toList());
            }
        });
        return lostExtensions;
    }

    private record ExtensionR(String t2wId, String extensionName) {
    }

    private static boolean copyExtension(String extensionName, TwoR twoR, ThreeWindingsTransformer t3w) {
        boolean copied = true;
        switch (extensionName) {
            case "twoWindingsTransformerFortescue" ->
                    copyAndAddFortescue(t3w.newExtension(ThreeWindingsTransformerFortescueAdder.class),
                            twoR.t2w1.getExtension(TwoWindingsTransformerFortescue.class), isWellOriented(twoR.starBus, twoR.t2w1),
                            twoR.t2w2.getExtension(TwoWindingsTransformerFortescue.class), isWellOriented(twoR.starBus, twoR.t2w2),
                            twoR.t2w3.getExtension(TwoWindingsTransformerFortescue.class), isWellOriented(twoR.starBus, twoR.t2w3));
            case "twoWindingsTransformerPhaseAngleClock" ->
                    copyAndAddPhaseAngleClock(t3w.newExtension(ThreeWindingsTransformerPhaseAngleClockAdder.class),
                            twoR.t2w2.getExtension(TwoWindingsTransformerPhaseAngleClock.class),
                            twoR.t2w3.getExtension(TwoWindingsTransformerPhaseAngleClock.class));
            case "twoWindingsTransformerToBeEstimated" ->
                    copyAndAddToBeEstimated(t3w.newExtension(ThreeWindingsTransformerToBeEstimatedAdder.class),
                            twoR.t2w1.getExtension(TwoWindingsTransformerToBeEstimated.class),
                            twoR.t2w2.getExtension(TwoWindingsTransformerToBeEstimated.class),
                            twoR.t2w3.getExtension(TwoWindingsTransformerToBeEstimated.class));
            default -> copied = false;
        }
        return copied;
    }

    private static List<AliasR> getAliases(TwoWindingsTransformer t2w, String leg, String end) {
        return t2w.getAliases().stream().map(alias -> new AliasR(t2w.getId(), alias, t2w.getAliasType(alias).orElse(""), leg, end)).toList();
    }

    private static List<AliasR> copyAliases(List<AliasR> t2wAliases, ThreeWindingsTransformer t3w) {
        List<AliasR> lostAliases = new ArrayList<>();
        t2wAliases.forEach(aliasR -> {
            boolean copied = copyAlias(aliasR.alias, aliasR.aliasType, aliasR.leg, aliasR.end, t3w);
            if (!copied) {
                lostAliases.add(aliasR);
            }
        });
        return lostAliases;
    }

    private static boolean copyAlias(String alias, String aliasType, String leg, String end, ThreeWindingsTransformer t3w) {
        boolean copied = true;
        if (aliasType.equals("CGMES.TransformerEnd" + end)) {
            t3w.addAlias(alias, "CGMES.TransformerEnd" + leg);
        } else if (aliasType.equals("CGMES.Terminal" + end)) {
            t3w.addAlias(alias, "CGMES.Terminal" + leg);
        } else if (aliasType.equals("CGMES.RatioTapChanger1")) {
            t3w.addAlias(alias, "CGMES.RatioTapChanger" + leg);
        } else if (aliasType.equals("CGMES.PhaseTapChanger1")) {
            t3w.addAlias(alias, "CGMES.PhaseTapChanger" + leg);
        } else {
            copied = false;
        }
        return copied;
    }

    private record AliasR(String t2wId, String alias, String aliasType, String leg, String end) {
    }

    private static void remove(TwoR twoR) {
        VoltageLevel voltageLevel = twoR.starBus.getVoltageLevel();
        twoR.starBus.getConnectedTerminalStream().toList() // toList is required to create a temporary list since the threeWindingsTransformer is removed during the replacement
                .forEach(terminal -> terminal.getConnectable().remove());
        voltageLevel.remove();
    }

    private static List<LimitsR> findLostLimits(TwoR twoR) {
        List<LimitsR> lostLimits = new ArrayList<>();
        getOperationalLimitsGroups2(twoR.t2w1, isWellOriented(twoR.starBus, twoR.t2w1)).forEach(operationalLimitsGroup -> lostLimits.add(new LimitsR(twoR.t2w1.getId(), operationalLimitsGroup.getId())));
        getOperationalLimitsGroups2(twoR.t2w2, isWellOriented(twoR.starBus, twoR.t2w2)).forEach(operationalLimitsGroup -> lostLimits.add(new LimitsR(twoR.t2w2.getId(), operationalLimitsGroup.getId())));
        getOperationalLimitsGroups2(twoR.t2w3, isWellOriented(twoR.starBus, twoR.t2w3)).forEach(operationalLimitsGroup -> lostLimits.add(new LimitsR(twoR.t2w3.getId(), operationalLimitsGroup.getId())));
        return lostLimits;
    }

    private static Collection<OperationalLimitsGroup> getOperationalLimitsGroups2(TwoWindingsTransformer t2w, boolean isWellOriented) {
        return isWellOriented ? t2w.getOperationalLimitsGroups2() : t2w.getOperationalLimitsGroups1();
    }

    private record LimitsR(String t2wId, String operationalLimitsGroupName) {
    }

    private static void createReportNode(ReportNode reportNode, String t2w1Id, String t2w2Id, String t2w3Id, String starVoltageId,
                                         List<PropertyR> lostProperties, List<ExtensionR> lostExtensions, List<AliasR> lostAliases,
                                         List<LimitsR> lostLimits, String t3wId) {

        ReportNode reportNodeReplacement = replace3TwoWindingsTransformersByThreeWindingsTransformersReport(reportNode);

        removedTwoWindingsTransformerReport(reportNodeReplacement, t2w1Id);
        removedTwoWindingsTransformerReport(reportNodeReplacement, t2w2Id);
        removedTwoWindingsTransformerReport(reportNodeReplacement, t2w3Id);
        removedVoltageLevelReport(reportNodeReplacement, starVoltageId);

        if (!lostProperties.isEmpty()) {
            Set<String> t2wIds = lostProperties.stream().map(propertyR -> propertyR.t2wId).collect(Collectors.toSet());
            t2wIds.stream().sorted().forEach(t2wId -> {
                String properties = String.join(",", lostProperties.stream().filter(propertyR -> propertyR.t2wId.equals(t2wId)).map(propertyR -> propertyR.propertyName).toList());
                lostTwoWindingsTransformerProperties(reportNodeReplacement, properties, t2wId);
            });
        }
        if (!lostExtensions.isEmpty()) {
            Set<String> t2wIds = lostExtensions.stream().map(extensionR -> extensionR.t2wId).collect(Collectors.toSet());
            t2wIds.stream().sorted().forEach(t2wId -> {
                String extensions = String.join(",", lostExtensions.stream().filter(extensionR -> extensionR.t2wId.equals(t2wId)).map(extensionR -> extensionR.extensionName).toList());
                lostTwoWindingsTransformerExtensions(reportNodeReplacement, extensions, t2wId);
            });
        }
        if (!lostAliases.isEmpty()) {
            Set<String> t2wIds = lostAliases.stream().map(aliasR -> aliasR.t2wId).collect(Collectors.toSet());
            t2wIds.stream().sorted().forEach(t2wId -> {
                String aliases = lostAliases.stream().filter(aliasR -> aliasR.t2wId.equals(t2wId)).map(AliasR::alias).collect(Collectors.joining(","));
                lostTwoWindingsTransformerAliases(reportNodeReplacement, aliases, t2wId);
            });
        }
        if (!lostLimits.isEmpty()) {
            Set<String> t2wIds = lostLimits.stream().map(limitsR -> limitsR.t2wId).collect(Collectors.toSet());
            t2wIds.stream().sorted().forEach(t2wId -> {
                String limits = lostLimits.stream().filter(limitsR -> limitsR.t2wId.equals(t2wId)).map(LimitsR::operationalLimitsGroupName).collect(Collectors.joining(","));
                lostTwoWindingsTransformerOperationalLimitsGroups(reportNodeReplacement, limits, t2wId);
            });
        }

        createdThreeWindingsTransformerReport(reportNodeReplacement, t3wId);
    }
}
