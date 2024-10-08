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
import com.powsybl.iidm.modification.util.ControlledRegulatingTerminals;
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
    private static final String WITH_FICTITIOUS_TERMINAL_USED_AS_REGULATING_TERMINAL = "with fictitious terminal used as regulating terminal";

    @Override
    public String getName() {
        return "Replace3TwoWindingsTransformersByThreeWindingsTransformers";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        ControlledRegulatingTerminals controlledRegulatingTerminals = new ControlledRegulatingTerminals(network);
        List<TwoR> twoWindingsTransformers = find3TwoWindingsTransformers(network);
        twoWindingsTransformers.forEach(twoR -> replace3TwoWindingsTransformerByThreeWindingsTransformer(twoR, controlledRegulatingTerminals, throwException, reportNode));
    }

    List<TwoR> find3TwoWindingsTransformers(Network network) {
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
        return twoWindingTransformersByBus.keySet().stream().sorted(Comparator.comparing(Identifiable::getId)).toList()
                .stream().filter(bus -> isStarBus(bus, twoWindingTransformersByBus.get(bus)))
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

    private record TwoR(Bus starBus, TwoWindingsTransformer t2w1, TwoWindingsTransformer t2w2, TwoWindingsTransformer t2w3) {
    }

    // if the twoWindingsTransformer is not well oriented, and it has non-zero shunt admittance (G != 0 or B != 0)
    // the obtained model is not equivalent to the initial one as the shunt admittance must be moved to the other side
    private void replace3TwoWindingsTransformerByThreeWindingsTransformer(TwoR twoR, ControlledRegulatingTerminals controlledRegulatingTerminals, boolean throwException, ReportNode reportNode) {
        Substation substation = findSubstation(twoR, throwException);
        if (substation == null) {
            return;
        }
        if (anyTwoWindingsTransformerRegulatingOnFictitiousSide(twoR, controlledRegulatingTerminals, throwException)) {
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
        ThreeWindingsTransformerAdder.LegAdder leg1Adder = t3wAdder.newLeg1()
                .setVoltageLevel(findVoltageLevel(twoR.t2w1, isWellOrientedT2w1).getId())
                .setR(findImpedance(twoR.t2w1.getR(), getStructuralRatio(twoR.t2w1), isWellOrientedT2w1))
                .setX(findImpedance(twoR.t2w1.getX(), getStructuralRatio(twoR.t2w1), isWellOrientedT2w1))
                .setG(findAdmittance(twoR.t2w1.getG(), getStructuralRatio(twoR.t2w1), isWellOrientedT2w1))
                .setB(findAdmittance(twoR.t2w1.getB(), getStructuralRatio(twoR.t2w1), isWellOrientedT2w1))
                .setRatedU(getRatedU1(twoR.t2w1, ratedU0, isWellOrientedT2w1));
        connectAfterCreatingInternalConnection(leg1Adder, twoR.t2w1, isWellOrientedT2w1);
        leg1Adder.add();

        ThreeWindingsTransformerAdder.LegAdder leg2Adder = t3wAdder.newLeg2()
                .setVoltageLevel(findVoltageLevel(twoR.t2w2, isWellOrientedT2w2).getId())
                .setR(findImpedance(twoR.t2w2.getR(), getStructuralRatio(twoR.t2w2), isWellOrientedT2w2))
                .setX(findImpedance(twoR.t2w2.getX(), getStructuralRatio(twoR.t2w2), isWellOrientedT2w2))
                .setG(findAdmittance(twoR.t2w2.getG(), getStructuralRatio(twoR.t2w2), isWellOrientedT2w2))
                .setB(findAdmittance(twoR.t2w2.getB(), getStructuralRatio(twoR.t2w2), isWellOrientedT2w2))
                .setRatedU(getRatedU1(twoR.t2w2, ratedU0, isWellOrientedT2w2));
        connectAfterCreatingInternalConnection(leg2Adder, twoR.t2w2, isWellOrientedT2w2);
        leg2Adder.add();

        ThreeWindingsTransformerAdder.LegAdder leg3Adder = t3wAdder.newLeg3()
                .setVoltageLevel(findVoltageLevel(twoR.t2w3, isWellOrientedT2w3).getId())
                .setR(findImpedance(twoR.t2w3.getR(), getStructuralRatio(twoR.t2w3), isWellOrientedT2w3))
                .setX(findImpedance(twoR.t2w3.getX(), getStructuralRatio(twoR.t2w3), isWellOrientedT2w3))
                .setG(findAdmittance(twoR.t2w3.getG(), getStructuralRatio(twoR.t2w3), isWellOrientedT2w3))
                .setB(findAdmittance(twoR.t2w3.getB(), getStructuralRatio(twoR.t2w3), isWellOrientedT2w3))
                .setRatedU(getRatedU1(twoR.t2w3, ratedU0, isWellOrientedT2w3));
        connectAfterCreatingInternalConnection(leg3Adder, twoR.t2w3, isWellOrientedT2w3);
        leg3Adder.add();
        ThreeWindingsTransformer t3w = t3wAdder.add();

        twoR.t2w1.getOptionalRatioTapChanger().ifPresent(rtc -> copyOrMoveRatioTapChanger(t3w.getLeg1().newRatioTapChanger(), rtc, isWellOrientedT2w1));
        twoR.t2w1.getOptionalPhaseTapChanger().ifPresent(ptc -> copyOrMovePhaseTapChanger(t3w.getLeg1().newPhaseTapChanger(), ptc, isWellOrientedT2w1));
        twoR.t2w2.getOptionalRatioTapChanger().ifPresent(rtc -> copyOrMoveRatioTapChanger(t3w.getLeg2().newRatioTapChanger(), rtc, isWellOrientedT2w2));
        twoR.t2w2.getOptionalPhaseTapChanger().ifPresent(ptc -> copyOrMovePhaseTapChanger(t3w.getLeg2().newPhaseTapChanger(), ptc, isWellOrientedT2w2));
        twoR.t2w3.getOptionalRatioTapChanger().ifPresent(rtc -> copyOrMoveRatioTapChanger(t3w.getLeg3().newRatioTapChanger(), rtc, isWellOrientedT2w3));
        twoR.t2w3.getOptionalPhaseTapChanger().ifPresent(ptc -> copyOrMovePhaseTapChanger(t3w.getLeg3().newPhaseTapChanger(), ptc, isWellOrientedT2w3));

        getOperationalLimitsGroups1(twoR.t2w1, isWellOrientedT2w1)
                .forEach(operationalLimitGroup -> copyOperationalLimitsGroup(t3w.getLeg1().newOperationalLimitsGroup(operationalLimitGroup.getId()), operationalLimitGroup));
        getOperationalLimitsGroups1(twoR.t2w2, isWellOrientedT2w2)
                .forEach(operationalLimitGroup -> copyOperationalLimitsGroup(t3w.getLeg2().newOperationalLimitsGroup(operationalLimitGroup.getId()), operationalLimitGroup));
        getOperationalLimitsGroups1(twoR.t2w3, isWellOrientedT2w3)
                .forEach(operationalLimitGroup -> copyOperationalLimitsGroup(t3w.getLeg3().newOperationalLimitsGroup(operationalLimitGroup.getId()), operationalLimitGroup));

        controlledRegulatingTerminals.replaceRegulatingTerminal(getTerminal1(twoR.t2w1, isWellOrientedT2w1), t3w.getLeg1().getTerminal());
        controlledRegulatingTerminals.replaceRegulatingTerminal(getTerminal1(twoR.t2w2, isWellOrientedT2w2), t3w.getLeg2().getTerminal());
        controlledRegulatingTerminals.replaceRegulatingTerminal(getTerminal1(twoR.t2w3, isWellOrientedT2w3), t3w.getLeg3().getTerminal());

        // t3w is not considered in controlledRegulatingTerminals (created in the model later)
        Map<Terminal, Terminal> regulatingTerminalMapping = getRegulatingTerminalMapping(twoR, t3w);
        replaceRegulatingTerminal(t3w.getLeg1(), regulatingTerminalMapping);
        replaceRegulatingTerminal(t3w.getLeg2(), regulatingTerminalMapping);
        replaceRegulatingTerminal(t3w.getLeg3(), regulatingTerminalMapping);

        copyTerminalActiveAndReactivePower(getTerminal1(twoR.t2w1, isWellOrientedT2w1), t3w.getLeg1().getTerminal());
        copyTerminalActiveAndReactivePower(getTerminal1(twoR.t2w2, isWellOrientedT2w2), t3w.getLeg2().getTerminal());
        copyTerminalActiveAndReactivePower(getTerminal1(twoR.t2w3, isWellOrientedT2w3), t3w.getLeg3().getTerminal());

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

        // report
        ReportNode reportNodeReplacement = reportNode.newReportNode().withMessageTemplate("replaced-3t2w-by-t3w", "Replaced 3 TwoWindingsTransformers by ThreeWindingsTransformer").add();
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
        createdThreeWindingsTransformerReport(reportNodeReplacement, t3w.getId());
    }

    private Substation findSubstation(TwoR twoR, boolean throwException) {
        Optional<Substation> substation = twoR.t2w1.getTerminal1().getVoltageLevel().getSubstation();
        if (substation.isEmpty()) {
            logOrThrow(throwException, TWO_WINDINGS_TRANSFORMER + "'" + twoR.t2w1.getId() + "' without substation");
            return null;
        } else {
            return substation.get();
        }
    }

    private boolean anyTwoWindingsTransformerRegulatingOnFictitiousSide(TwoR twoR, ControlledRegulatingTerminals controlledRegulatingTerminals, boolean throwException) {
        if (controlledRegulatingTerminals.usedAsRegulatingTerminal(getTerminal2(twoR.t2w1, isWellOriented(twoR.starBus, twoR.t2w1)))) {
            logOrThrow(throwException, TWO_WINDINGS_TRANSFORMER + "'" + twoR.t2w1.getId() + "' " + WITH_FICTITIOUS_TERMINAL_USED_AS_REGULATING_TERMINAL);
            return true;
        }
        if (controlledRegulatingTerminals.usedAsRegulatingTerminal(getTerminal2(twoR.t2w2, isWellOriented(twoR.starBus, twoR.t2w2)))) {
            logOrThrow(throwException, TWO_WINDINGS_TRANSFORMER + "'" + twoR.t2w2.getId() + "' " + WITH_FICTITIOUS_TERMINAL_USED_AS_REGULATING_TERMINAL);
            return true;
        }
        if (controlledRegulatingTerminals.usedAsRegulatingTerminal(getTerminal2(twoR.t2w3, isWellOriented(twoR.starBus, twoR.t2w3)))) {
            logOrThrow(throwException, TWO_WINDINGS_TRANSFORMER + "'" + twoR.t2w3.getId() + "' " + WITH_FICTITIOUS_TERMINAL_USED_AS_REGULATING_TERMINAL);
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

    // is well oriented when the fictitious side is at end2
    private static boolean isWellOriented(Bus starBus, TwoWindingsTransformer t2w) {
        return starBus.equals(t2w.getTerminal2().getBusView().getBus());
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
        if (terminal.getVoltageLevel().getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
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

    private static Map<Terminal, Terminal> getRegulatingTerminalMapping(TwoR twoR, ThreeWindingsTransformer t3w) {
        Map<Terminal, Terminal> regulatingTerminalMapping = new HashMap<>();
        regulatingTerminalMapping.put(getTerminal1(twoR.t2w1, isWellOriented(twoR.starBus, twoR.t2w1)), t3w.getLeg1().getTerminal());
        regulatingTerminalMapping.put(getTerminal1(twoR.t2w2, isWellOriented(twoR.starBus, twoR.t2w2)), t3w.getLeg2().getTerminal());
        regulatingTerminalMapping.put(getTerminal1(twoR.t2w3, isWellOriented(twoR.starBus, twoR.t2w3)), t3w.getLeg3().getTerminal());
        return regulatingTerminalMapping;
    }

    private static void replaceRegulatingTerminal(ThreeWindingsTransformer.Leg t3wLeg, Map<Terminal, Terminal> regulatingTerminalMapping) {
        t3wLeg.getOptionalRatioTapChanger().ifPresent(rtc -> {
            if (regulatingTerminalMapping.containsKey(rtc.getRegulationTerminal())) {
                rtc.setRegulationTerminal(regulatingTerminalMapping.get(rtc.getRegulationTerminal()));
            }
        });
        t3wLeg.getOptionalPhaseTapChanger().ifPresent(ptc -> {
            if (regulatingTerminalMapping.containsKey(ptc.getRegulationTerminal())) {
                ptc.setRegulationTerminal(regulatingTerminalMapping.get(ptc.getRegulationTerminal()));
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
                    copyAndAddFortescue(twoR.t2w1.getExtension(TwoWindingsTransformerFortescue.class), isWellOriented(twoR.starBus, twoR.t2w1),
                            twoR.t2w2.getExtension(TwoWindingsTransformerFortescue.class), isWellOriented(twoR.starBus, twoR.t2w2),
                            twoR.t2w3.getExtension(TwoWindingsTransformerFortescue.class), isWellOriented(twoR.starBus, twoR.t2w3),
                            t3w.newExtension(ThreeWindingsTransformerFortescueAdder.class));
            case "twoWindingsTransformerPhaseAngleClock" ->
                    copyAndAddPhaseAngleClock(twoR.t2w2.getExtension(TwoWindingsTransformerPhaseAngleClock.class),
                            twoR.t2w3.getExtension(TwoWindingsTransformerPhaseAngleClock.class),
                            t3w.newExtension(ThreeWindingsTransformerPhaseAngleClockAdder.class));
            case "twoWindingsTransformerToBeEstimated" ->
                    copyAndAddToBeEstimated(twoR.t2w1.getExtension(TwoWindingsTransformerToBeEstimated.class),
                            twoR.t2w2.getExtension(TwoWindingsTransformerToBeEstimated.class),
                            twoR.t2w3.getExtension(TwoWindingsTransformerToBeEstimated.class),
                            t3w.newExtension(ThreeWindingsTransformerToBeEstimatedAdder.class));
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
        twoR.starBus.getConnectedTerminalStream().toList().forEach(terminal -> terminal.getConnectable().remove());
        voltageLevel.remove();
    }
}
