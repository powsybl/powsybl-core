/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.TwtData;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.powsybl.iidm.modification.util.TransformerUtils.copyAndAddPhaseTapChanger;
import static com.powsybl.iidm.modification.util.TransformerUtils.copyAndAddRatioTapChanger;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

final class TransformersTestUtils {

    private TransformersTestUtils() {
    }

    static void addPhaseTapChanger(TwoWindingsTransformer t2w) {
        PhaseTapChangerAdder ptcAdder = t2w.newPhaseTapChanger();
        fillTapChangerAdder(ptcAdder, t2w.getTerminal1());
        ptcAdder.add();
    }

    static void addPhaseTapChanger(ThreeWindingsTransformer.Leg leg) {
        PhaseTapChangerAdder ptcAdder = leg.newPhaseTapChanger();
        fillTapChangerAdder(ptcAdder, leg.getTerminal());
        ptcAdder.add();
    }

    private static void fillTapChangerAdder(PhaseTapChangerAdder ptcAdder, Terminal terminal) {
        ptcAdder.setLowTapPosition(0)
                .setTapPosition(2)
                .setRegulationTerminal(terminal)
                .setRegulationValue(10.0)
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
                .setTargetDeadband(0.5)
                .setRegulating(false)
                .beginStep()
                .setRho(0.99)
                .setAlpha(-2.0)
                .setR(1.01)
                .setX(1.02)
                .setG(1.03)
                .setB(1.04)
                .endStep()
                .beginStep()
                .setRho(1.00)
                .setAlpha(0.0)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setRho(1.01)
                .setAlpha(2.0)
                .setR(0.99)
                .setX(0.98)
                .setG(0.97)
                .setB(0.96)
                .endStep()
                .add();
    }

    static void addLoadingLimits(TwoWindingsTransformer t2w) {
        OperationalLimitsGroup summer = t2w.newOperationalLimitsGroup1("OperationalLimitsGroup-summer");
        OperationalLimitsGroup winter = t2w.newOperationalLimitsGroup1("OperationalLimitsGroup-winter");
        addSummerLoadingLimits(summer);
        addWinterLoadingLimits(winter);
    }

    static void addLoadingLimits(ThreeWindingsTransformer.Leg leg) {
        OperationalLimitsGroup summer = leg.newOperationalLimitsGroup("OperationalLimitsGroup-summer");
        OperationalLimitsGroup winter = leg.newOperationalLimitsGroup("OperationalLimitsGroup-winter");
        addSummerLoadingLimits(summer);
        addWinterLoadingLimits(winter);
    }

    private static void addSummerLoadingLimits(OperationalLimitsGroup summer) {
        summer.newActivePowerLimits()
                .setPermanentLimit(100.0)
                .beginTemporaryLimit()
                .setName("TemporaryActivePowerLimit-1-summer")
                .setAcceptableDuration(2)
                .setValue(110.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TemporaryActivePowerLimit-2-summer")
                .setAcceptableDuration(1)
                .setValue(120.0)
                .endTemporaryLimit().add();
        summer.newApparentPowerLimits()
                .setPermanentLimit(105.0)
                .beginTemporaryLimit()
                .setName("TemporaryApparentPowerLimit-1")
                .setAcceptableDuration(2)
                .setValue(115.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TemporaryApparentPowerLimit-2-summer")
                .setAcceptableDuration(1)
                .setValue(125.0)
                .endTemporaryLimit().add();
        summer.newCurrentLimits()
                .setPermanentLimit(1050.0)
                .beginTemporaryLimit()
                .setName("TemporaryCurrentLimit-1-summer")
                .setAcceptableDuration(2)
                .setValue(1150.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TemporaryCurrentLimit-2-summer")
                .setAcceptableDuration(1)
                .setValue(1250.0)
                .endTemporaryLimit().add();
    }

    private static void addWinterLoadingLimits(OperationalLimitsGroup winter) {
        winter.newActivePowerLimits()
                .setPermanentLimit(125.0)
                .beginTemporaryLimit()
                .setName("TemporaryActivePowerLimit-1-winter")
                .setAcceptableDuration(3)
                .setValue(135.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TemporaryActivePowerLimit-2-winter")
                .setAcceptableDuration(2)
                .setValue(145.0)
                .endTemporaryLimit().add();
        winter.newApparentPowerLimits()
                .setPermanentLimit(130.0)
                .beginTemporaryLimit()
                .setName("TemporaryApparentPowerLimit-1-winter")
                .setAcceptableDuration(3)
                .setValue(140.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TemporaryApparentPowerLimit-2-winter")
                .setAcceptableDuration(2)
                .setValue(150.0)
                .endTemporaryLimit().add();
        winter.newCurrentLimits()
                .setPermanentLimit(130.0)
                .beginTemporaryLimit()
                .setName("TemporaryCurrentLimit-1-winter")
                .setAcceptableDuration(3)
                .setValue(140.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TemporaryCurrentLimit-2-winter")
                .setAcceptableDuration(2)
                .setValue(150.0)
                .endTemporaryLimit().add();
    }

    static boolean compareRatioTapChanger(RatioTapChanger expectedRtc, RatioTapChanger rtc) {
        String expected = ratioTapChangerToString(expectedRtc);
        String actual = ratioTapChangerToString(rtc);
        return expected.equals(actual);
    }

    private static String ratioTapChangerToString(RatioTapChanger rtc) {
        List<String> strings = new ArrayList<>();
        strings.add(String.valueOf(rtc.getLowTapPosition()));
        strings.add(String.valueOf(rtc.getTapPosition()));
        strings.add(rtc.getRegulationTerminal().getBusView().getBus().getId());
        strings.add(String.valueOf(rtc.getTargetV()));
        strings.add(String.valueOf(rtc.getRegulationValue()));
        strings.add(String.valueOf(rtc.getRegulationMode()));
        strings.add(String.valueOf(rtc.getTargetDeadband()));
        strings.add(String.valueOf(rtc.isRegulating()));
        strings.add(String.valueOf(rtc.getStepCount()));
        rtc.getAllSteps().forEach((step, rtcStep) -> {
            strings.add(String.valueOf(step));
            strings.add(String.valueOf(rtcStep.getRho()));
            strings.add(String.valueOf(rtcStep.getR()));
            strings.add(String.valueOf(rtcStep.getX()));
            strings.add(String.valueOf(rtcStep.getG()));
            strings.add(String.valueOf(rtcStep.getB()));
        });

        return String.join(",", strings);
    }

    static boolean comparePhaseTapChanger(PhaseTapChanger expectedPtc, PhaseTapChanger ptc) {
        String expected = phaseTapChangerToString(expectedPtc);
        String actual = phaseTapChangerToString(ptc);
        return expected.equals(actual);
    }

    private static String phaseTapChangerToString(PhaseTapChanger ptc) {
        List<String> strings = new ArrayList<>();
        strings.add(String.valueOf(ptc.getLowTapPosition()));
        strings.add(String.valueOf(ptc.getTapPosition()));
        strings.add(ptc.getRegulationTerminal().getBusView().getBus().getId());
        strings.add(String.valueOf(ptc.getRegulationValue()));
        strings.add(String.valueOf(ptc.getRegulationMode()));
        strings.add(String.valueOf(ptc.getTargetDeadband()));
        strings.add(String.valueOf(ptc.isRegulating()));
        strings.add(String.valueOf(ptc.getStepCount()));
        ptc.getAllSteps().forEach((step, rtcStep) -> {
            strings.add(String.valueOf(step));
            strings.add(String.valueOf(rtcStep.getRho()));
            strings.add(String.valueOf(rtcStep.getAlpha()));
            strings.add(String.valueOf(rtcStep.getR()));
            strings.add(String.valueOf(rtcStep.getX()));
            strings.add(String.valueOf(rtcStep.getG()));
            strings.add(String.valueOf(rtcStep.getB()));
        });

        return String.join(",", strings);
    }

    static boolean compareOperationalLimitsGroups(Collection<OperationalLimitsGroup> expected, Collection<OperationalLimitsGroup> actual) {
        String expectedString = operationalLimitsToString(expected);
        String actualString = operationalLimitsToString(actual);
        return expectedString.equals(actualString);
    }

    private static String operationalLimitsToString(Collection<OperationalLimitsGroup> operationalLimitsGroups) {
        List<String> strings = new ArrayList<>();
        operationalLimitsGroups.forEach(operationalLimitGroup -> {
            strings.add(operationalLimitGroup.getId());
            operationalLimitGroup.getActivePowerLimits().ifPresent(activePowerLimits -> {
                strings.add(activePowerLimits.getLimitType().name());
                add(activePowerLimits, strings);
            });
            operationalLimitGroup.getApparentPowerLimits().ifPresent(apparentPowerLimits -> {
                strings.add(apparentPowerLimits.getLimitType().name());
                add(apparentPowerLimits, strings);
            });
            operationalLimitGroup.getCurrentLimits().ifPresent(currentLimits -> {
                strings.add(currentLimits.getLimitType().name());
                add(currentLimits, strings);
            });
        });
        return String.join(",", strings);
    }

    private static void add(LoadingLimits loadingLimits, List<String> strings) {
        strings.add(String.valueOf(loadingLimits.getPermanentLimit()));
        loadingLimits.getTemporaryLimits().forEach(temporaryLimit -> {
            strings.add(temporaryLimit.getName());
            strings.add(String.valueOf(temporaryLimit.getAcceptableDuration()));
            strings.add(String.valueOf(temporaryLimit.getValue()));
            strings.add(String.valueOf(temporaryLimit.isFictitious()));
        });
    }

    static void addVoltages(Bus bus1, Bus bus2, Bus bus3) {
        bus1.setV(bus1.getVoltageLevel().getNominalV() * 1.01);
        bus1.setAngle(2.0);

        bus2.setV(bus2.getVoltageLevel().getNominalV() * 0.99);
        bus2.setAngle(4.0);

        bus3.setV(bus3.getVoltageLevel().getNominalV() * 0.98);
        bus3.setAngle(3.0);
    }

    static void setStarBusVoltage(TwtData twtData, Bus starBus) {
        starBus.setV(twtData.getStarU());
        starBus.setAngle(Math.toDegrees(twtData.getStarTheta()));
    }

    static void reOrientedTwoWindingsTransformer(TwoWindingsTransformer t2w) {
        TwoWindingsTransformer t2wNotWellOriented = t2w.getTerminal1().getVoltageLevel().getSubstation().orElseThrow().newTwoWindingsTransformer()
                .setId(t2w.getId() + "-" + "notWellOriented")
                .setName(t2w.getNameOrId() + "-" + "notWellOriented")
                .setRatedU1(t2w.getRatedU2())
                .setRatedU2(t2w.getRatedU1())
                .setR(t2w.getR())
                .setX(t2w.getX())
                .setG(t2w.getG())
                .setB(t2w.getB())
                .setRatedS(t2w.getRatedS())
                .setVoltageLevel1(t2w.getTerminal2().getVoltageLevel().getId())
                .setConnectableBus1(t2w.getTerminal2().getBusBreakerView().getBus().getId())
                .setBus1(t2w.getTerminal2().getBusBreakerView().getBus().getId())
                .setVoltageLevel2(t2w.getTerminal1().getVoltageLevel().getId())
                .setConnectableBus2(t2w.getTerminal1().getBusBreakerView().getBus().getId())
                .setBus2(t2w.getTerminal1().getBusBreakerView().getBus().getId())
                .add();

        copyAndAddRatioTapChanger(t2wNotWellOriented.newRatioTapChanger(), t2w.getRatioTapChanger());
        copyAndAddPhaseTapChanger(t2wNotWellOriented.newPhaseTapChanger(), t2w.getPhaseTapChanger());

        t2w.remove();
    }

    static Network createThreeWindingsTransformerNodeBreakerNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("three-windings-transformer-nodeBreaker", "test");
        network.setCaseDate(ZonedDateTime.parse("2018-03-05T13:30:30.486+01:00"));
        Substation substation = network.newSubstation()
                .setId("SUBSTATION")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = substation.newVoltageLevel()
                .setId("VL_132")
                .setNominalV(132.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel vl2 = substation.newVoltageLevel()
                .setId("VL_33")
                .setNominalV(33.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel vl3 = substation.newVoltageLevel()
                .setId("VL_11")
                .setNominalV(11.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        ThreeWindingsTransformer twt = substation.newThreeWindingsTransformer()
                .setId("3WT")
                .setRatedU0(132.0)
                .newLeg1()
                .setR(17.424)
                .setX(1.7424)
                .setG(0.00573921028466483)
                .setB(0.000573921028466483)
                .setRatedU(132.0)
                .setVoltageLevel(vl1.getId())
                .setNode(1)
                .add()
                .newLeg2()
                .setR(1.089)
                .setX(0.1089)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(33.0)
                .setVoltageLevel(vl2.getId())
                .setNode(1)
                .add()
                .newLeg3()
                .setR(0.121)
                .setX(0.0121)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(11.0)
                .setVoltageLevel(vl3.getId())
                .setNode(1)
                .add()
                .add();
        return network;
    }
}