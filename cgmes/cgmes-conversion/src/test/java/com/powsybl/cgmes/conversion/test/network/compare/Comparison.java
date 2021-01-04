/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.network.compare;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.powsybl.cgmes.conversion.extensions.CimCharacteristics;
import com.powsybl.cgmes.conversion.extensions.CgmesSvMetadata;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ReactiveCapabilityCurve.Point;
import com.powsybl.iidm.network.extensions.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Comparison {

    public Comparison(
            Network expected,
            Network actual,
            ComparisonConfig config) {
        this.expected = expected;
        this.actual = actual;
        this.config = config;
        this.networkMapping = config.networkMappingFactory.create(expected, actual);
        this.diff = config.differences;
    }

    public void compare() {
        diff.current(expected);
        if (config.checkNetworkId) {
            compare("networkId", expected.getId(), actual.getId());
        }

        // Compare CIM characteristics
        compareCIMCharacteristics(expected.getExtension(CimCharacteristics.class), actual.getExtension(CimCharacteristics.class));

        // Compare SV metadata
        compareCgmesSvMetadata(expected.getExtension(CgmesSvMetadata.class), actual.getExtension(CgmesSvMetadata.class));

        // TODO Consider other attributes of network (name, caseData, forecastDistance, ...)
        compare(
                expected.getSubstationStream(),
                actual.getSubstationStream(),
                this::compareSubstations);
        compare(
                expected.getVoltageLevelStream(),
                actual.getVoltageLevelStream(),
                this::compareVoltageLevels);
        compareBuses(
                expected.getBusBreakerView().getBusStream(),
                actual.getBusBreakerView().getBusStream(),
                this::compareBuses);
        compare(
                expected.getLoadStream(),
                actual.getLoadStream(),
                this::compareLoads);
        compare(
                expected.getShuntCompensatorStream(),
                actual.getShuntCompensatorStream(),
                this::compareShunts);
        compare(
                expected.getStaticVarCompensatorStream(),
                actual.getStaticVarCompensatorStream(),
                this::compareStaticVarCompensators);
        compare(
                expected.getGeneratorStream(),
                actual.getGeneratorStream(),
                this::compareGenerators);
        compare(
                expected.getSwitchStream(),
                actual.getSwitchStream(),
                this::compareSwitches);
        compare(
                expected.getLineStream(),
                actual.getLineStream(),
                this::testLines);
        compare(
                expected.getTwoWindingsTransformerStream(),
                actual.getTwoWindingsTransformerStream(),
                this::compareTwoWindingTransformers);
        compare(
                expected.getThreeWindingsTransformerStream(),
                actual.getThreeWindingsTransformerStream(),
                this::compareThreeWindingsTransformers);
        compare(
                expected.getDanglingLineStream(),
                actual.getDanglingLineStream(),
                this::compareDanglingLines);
        diff.end();
    }

    // The actual network does not contain unexpected identifiables
    // All identifiables in expected stream exist in actual network and have same attributes
    private <T extends Identifiable<T>> void compare(
            Stream<T> expecteds,
            Stream<T> actuals,
            BiConsumer<T, T> testAttributes) {
        actuals.forEach(actual -> {
            Identifiable expected = networkMapping.findExpected(actual);
            if (expected == null) {
                diff.unexpected(actual);
                return;
            }
            String context = className(actual);
            compare(context, expected.getClass(), actual.getClass());
        });
        expecteds.forEach(expected -> {
            Identifiable<?> actual = networkMapping.findActual(expected);
            if (actual == null) {
                diff.missing(expected);
                return;
            }
            diff.match(expected);
            diff.current(expected);
            String context = className(actual);
            compare(context, expected.getClass(), actual.getClass());
            context = context + ".name";
            compareNames(context, expected.getOptionalName().orElse(""), actual.getOptionalName().orElse(""));
            // Obtained identifiable in actual must be of type T
            @SuppressWarnings("unchecked")
            T tactual = (T) actual;
            testAttributes.accept((T) expected, tactual);
        });
    }

    private void compareCIMCharacteristics(CimCharacteristics expected, CimCharacteristics actual) {
        if (expected == null && actual != null) {
            diff.unexpected(actual.getExtendable().getId() + "_cimCharacteristics_extension");
            return;
        }
        if (expected != null) {
            if (actual == null) {
                diff.missing(expected.getExtendable().getId() + "_cimCharacteristics_extension");
                return;
            }
            compare("topologyKind", expected.getTopologyKind().toString(), actual.getTopologyKind().toString());
            compare("cimVersion", expected.getCimVersion(), actual.getCimVersion());
        }
    }

    private void compareCgmesSvMetadata(CgmesSvMetadata expected, CgmesSvMetadata actual) {
        if (expected == null && actual != null) {
            diff.unexpected(actual.getExtendable().getId() + "_cgmesSvMetadata_extension");
            return;
        }
        if (expected != null) {
            if (actual == null) {
                diff.missing(expected.getExtendable().getId() + "_cgmesSvMetadata_extension");
                return;
            }
            compare("description", expected.getDescription(), actual.getDescription());
            compare("svVersion", expected.getSvVersion(), actual.getSvVersion());
            compare("modelingAuthoritySet", expected.getModelingAuthoritySet(), actual.getModelingAuthoritySet());
            for (String dep : expected.getDependencies()) {
                if (!actual.getDependencies().contains(dep)) {
                    diff.missing("dependentOn: " + dep);
                }
            }
            for (String dep : actual.getDependencies()) {
                if (!expected.getDependencies().contains(dep)) {
                    diff.unexpected("dependentOn: " + dep);
                }
            }
        }
    }

    // Buses in bus breaker view are not inserted in the index for Network Identifiables
    // We prepare an index external to the network for comparing the two lists
    private void compareBuses(
            Stream<Bus> expecteds,
            Stream<Bus> actuals,
            BiConsumer<Bus, Bus> testAttributes) {
        Map<String, Bus> actualsById = new HashMap<>();
        actuals.forEach(b -> actualsById.put(b.getId(), b));
        Map<String, Bus> expectedsById = new HashMap<>();
        expecteds.forEach(b -> expectedsById.put(b.getId(), b));

        actualsById.values().forEach(actual -> {
            Bus expected = expectedsById.get(actual.getId());
            if (expected == null) {
                diff.unexpected(actual);
                return;
            }
            String context = className(actual);
            compare(context, expected.getClass(), actual.getClass());
        });
        expectedsById.values().forEach(expected -> {
            Bus actual = actualsById.get(expected.getId());
            if (actual == null) {
                diff.missing(expected);
                return;
            }
            diff.match(expected);
            diff.current(expected);
            String context = className(actual);
            compare(context, expected.getClass(), actual.getClass());
            context = context + ".name";
            compareNames(context, expected.getOptionalName().orElse(""), actual.getOptionalName().orElse(""));
            testAttributes.accept((Bus) expected, actual);
        });
    }

    private void compareSubstations(Substation expected, Substation actual) {
        compare("country", expected.getCountry(), actual.getCountry());
        Set<String> mappedActualGeoTags = actual.getGeographicalTags().stream()
                .map(networkMapping::applyPrefixToActual)
                .collect(Collectors.toSet());
        compare("geographicalTags",
                expected.getGeographicalTags(),
                mappedActualGeoTags);
    }

    private void compareVoltageLevels(VoltageLevel expected, VoltageLevel actual) {
        equivalent("Substation", expected.getSubstation(), actual.getSubstation());
        compare("nominalV", expected.getNominalV(), actual.getNominalV());
        if (config.checkVoltageLevelLimits) {
            compare("lowVoltageLimit",
                    expected.getLowVoltageLimit(),
                    actual.getLowVoltageLimit());
            compare("highVoltageLimit",
                    expected.getHighVoltageLimit(),
                    actual.getHighVoltageLimit());
        }
        SlackTerminal expectedSlackTerminal = expected.getExtension(SlackTerminal.class);
        SlackTerminal actualSlackTerminal = actual.getExtension(SlackTerminal.class);
        if (expectedSlackTerminal == null) {
            if (actualSlackTerminal != null) {
                diff.unexpected("slackTerminal");
            }
        } else {
            if (actualSlackTerminal == null) {
                diff.missing("slackTerminal");
            } else {
                equivalent("slackTerminal", expectedSlackTerminal.getTerminal().getConnectable(),
                        actualSlackTerminal.getTerminal().getConnectable());
            }
        }
    }

    private void compareBuses(Bus expected, Bus actual) {
        equivalent("VoltageLevel", expected.getVoltageLevel(), actual.getVoltageLevel());
        compareAliases(expected, actual);
        compare("v", expected.getV(), actual.getV());
        compare("angle", expected.getAngle(), actual.getAngle());
    }

    private void compareLoads(Load expected, Load actual) {
        equivalent("VoltageLevel",
                expected.getTerminal().getVoltageLevel(),
                actual.getTerminal().getVoltageLevel());
        compareAliases(expected, actual);
        compare("p0", expected.getP0(), actual.getP0());
        compare("q0", expected.getQ0(), actual.getQ0());
        // TODO Should we check terminals ? (we are not setting terminal id)
        compare("p", expected.getTerminal().getP(), actual.getTerminal().getP());
        compare("q", expected.getTerminal().getQ(), actual.getTerminal().getQ());
        compareLoadDetails(expected.getExtension(LoadDetail.class), actual.getExtension(LoadDetail.class));
    }

    private void compareLoadDetails(LoadDetail expected, LoadDetail actual) {
        if (expected == null) {
            if (actual != null) {
                diff.unexpected("expected conform or not conform load (is energyConsumer)");
                return;
            }
            return;
        }
        if (actual == null) {
            diff.unexpected("expected energyConsumer (is conform or not conform load)");
            return;
        }
        diff.compare("fixedActivePower", expected.getFixedActivePower(), actual.getFixedActivePower());
        diff.compare("fixedReactivePower", expected.getFixedReactivePower(), actual.getFixedReactivePower());
        diff.compare("variableActivePower", expected.getVariableActivePower(), actual.getVariableActivePower());
        diff.compare("variableReactivePower", expected.getVariableReactivePower(), actual.getVariableReactivePower());
    }

    private void compareShunts(ShuntCompensator expected, ShuntCompensator actual) {
        equivalent("VoltageLevel",
                expected.getTerminal().getVoltageLevel(),
                actual.getTerminal().getVoltageLevel());
        compareAliases(expected, actual);
        compare("sectionCount",
                expected.getSectionCount(),
                actual.getSectionCount());
        compare("voltageRegulationOn",
                expected.isVoltageRegulatorOn(),
                actual.isVoltageRegulatorOn());
        compare("targetV",
                expected.getTargetV(),
                actual.getTargetV());
        compare("targetDeadband",
                expected.getTargetDeadband(),
                actual.getTargetDeadband());
        sameIdentifier("regulationTerminal",
                expected.getRegulatingTerminal().getBusBreakerView().getBus(),
                actual.getRegulatingTerminal().getBusBreakerView().getBus());
        compareShuntModels(expected, actual);
    }

    private void compareShuntModels(ShuntCompensator expected, ShuntCompensator actual) {
        switch (expected.getModelType()) {
            case LINEAR:
                compare("maximumSectionCount",
                        expected.getMaximumSectionCount(),
                        actual.getMaximumSectionCount());
                compare("bPerSection",
                        expected.getModel(ShuntCompensatorLinearModel.class).getBPerSection(),
                        actual.getModel(ShuntCompensatorLinearModel.class).getBPerSection());
                compare("gPerSection",
                        expected.getModel(ShuntCompensatorLinearModel.class).getGPerSection(),
                        actual.getModel(ShuntCompensatorLinearModel.class).getGPerSection());
                break;
            case NON_LINEAR:
                ShuntCompensatorNonLinearModel expectedModel = expected.getModel(ShuntCompensatorNonLinearModel.class);
                ShuntCompensatorNonLinearModel actualModel = actual.getModel(ShuntCompensatorNonLinearModel.class);
                if (expectedModel.getAllSections().size() > actualModel.getAllSections().size()) {
                    for (int i = actualModel.getAllSections().size(); i < expectedModel.getAllSections().size(); i++) {
                        diff.missing("section" + i);
                    }
                }
                if (expectedModel.getAllSections().size() < actualModel.getAllSections().size()) {
                    for (int i = expectedModel.getAllSections().size(); i < actualModel.getAllSections().size(); i++) {
                        diff.unexpected("section" + i);
                    }
                }
                List<ShuntCompensatorNonLinearModel.Section> expectedSections = expectedModel.getAllSections();
                List<ShuntCompensatorNonLinearModel.Section> actualSections = actualModel.getAllSections();
                for (int i = 0; i < expectedSections.size(); i++) {
                    ShuntCompensatorNonLinearModel.Section expectedSection = expectedSections.get(i);
                    ShuntCompensatorNonLinearModel.Section actualSection = actualSections.get(i);
                    compare("section" + i + 1 + ".b", expectedSection.getB(), actualSection.getB());
                    compare("section" + i + 1 + ".g", expectedSection.getG(), actualSection.getG());
                }
                break;
            default:
                throw new AssertionError("Unexpected shunt model type: " + expected.getModelType());
        }
    }

    private void compareStaticVarCompensators(
            StaticVarCompensator expected,
            StaticVarCompensator actual) {
        equivalent("VoltageLevel",
                expected.getTerminal().getVoltageLevel(),
                actual.getTerminal().getVoltageLevel());
        compareAliases(expected, actual);
        compare("Bmin",
                expected.getBmin(),
                actual.getBmin());
        compare("Bmax",
                expected.getBmax(),
                actual.getBmax());
        compare("voltageSetpoint",
                expected.getVoltageSetpoint(),
                actual.getVoltageSetpoint());
        compare("reactivePowerSetpoint",
                expected.getReactivePowerSetpoint(),
                actual.getReactivePowerSetpoint());
        compare("regulationMode",
                expected.getRegulationMode(),
                actual.getRegulationMode());
        sameIdentifier("regulationTerminal",
                expected.getRegulatingTerminal().getBusBreakerView().getBus(),
                actual.getRegulatingTerminal().getBusBreakerView().getBus());
    }

    private void compareGenerators(Generator expected, Generator actual) {
        equivalent("VoltageLevel",
                expected.getTerminal().getVoltageLevel(),
                actual.getTerminal().getVoltageLevel());
        compareAliases(expected, actual);
        sameIdentifier("ConnectableBus",
                expected.getTerminal().getBusBreakerView().getConnectableBus(),
                actual.getTerminal().getBusBreakerView().getConnectableBus());
        Bus be = expected.getTerminal().getBusBreakerView().getBus();
        Bus ba = actual.getTerminal().getBusBreakerView().getBus();
        if (be == null) {
            if (ba != null) {
                diff.unexpected(ba);
                return;
            }
        } else {
            if (ba == null) {
                diff.missing(be);
                return;
            }
            sameIdentifier("Bus", be, ba);
        }
        compare("minP", expected.getMinP(), actual.getMinP());
        compare("maxP", expected.getMaxP(), actual.getMaxP());
        compareGeneratorReactiveLimits(expected.getReactiveLimits(), actual.getReactiveLimits());
        compare("targetP", expected.getTargetP(), actual.getTargetP());
        compare("targetQ", expected.getTargetQ(), actual.getTargetQ());
        compare("targetV", expected.getTargetV(), actual.getTargetV());
        compare("isVoltageRegulatorOn",
                expected.isVoltageRegulatorOn(),
                actual.isVoltageRegulatorOn());
        if (config.checkGeneratorRegulatingTerminal
                && (expected.getRegulatingTerminal() != null
                || actual.getRegulatingTerminal() != null)) {
            sameIdentifier("RegulatingTerminalBus",
                    expected.getRegulatingTerminal().getBusBreakerView().getBus(),
                    actual.getRegulatingTerminal().getBusBreakerView().getBus());
        }

        compare("energySource", expected.getEnergySource(), actual.getEnergySource());
        compare("ratedS", expected.getRatedS(), actual.getRatedS());
        compare("terminalP", expected.getTerminal().getP(), actual.getTerminal().getP());
        compare("terminalQ", expected.getTerminal().getQ(), actual.getTerminal().getQ());
        compareQPercents(expected.getExtension(CoordinatedReactiveControl.class), actual.getExtension(CoordinatedReactiveControl.class));
    }

    private void compareQPercents(CoordinatedReactiveControl expected, CoordinatedReactiveControl actual) {
        if (expected == null) {
            if (actual != null) {
                diff.unexpected("qPercent");
                return;
            }
            return;
        }
        if (actual == null) {
            diff.unexpected("qPercent");
            return;
        }
        diff.compare("qPercent", expected.getQPercent(), actual.getQPercent());
    }

    private void compareGeneratorReactiveLimits(ReactiveLimits expected, ReactiveLimits actual) {
        switch (expected.getKind()) {
            case MIN_MAX:
                compareGeneratorMinMaxReactiveLimits(
                        (MinMaxReactiveLimits) expected,
                        (MinMaxReactiveLimits) actual);
                break;

            case CURVE:
                if (config.checkGeneratorReactiveCapabilityCurve) {
                    compareGeneratorReactiveCapabilityCurve(
                            (ReactiveCapabilityCurve) expected,
                            (ReactiveCapabilityCurve) actual);
                }
                break;

            default:
                throw new AssertionError("Unexpected ReactiveLimitsKing value: " + expected.getKind());
        }
    }

    private void compareGeneratorMinMaxReactiveLimits(
            MinMaxReactiveLimits expected,
            MinMaxReactiveLimits actual) {
        compare("minQ", expected.getMinQ(), actual.getMinQ());
        compare("maxQ", expected.getMaxQ(), actual.getMaxQ());
    }

    private void compareGeneratorReactiveCapabilityCurve(
            ReactiveCapabilityCurve expected,
            ReactiveCapabilityCurve actual) {
        // From the IIDM API we don't know if the collection of points is sorted,
        // so we sort points by active power, then compare resulting lists point by point
        Comparator<Point> comparePoints = (p0, p1) -> Double.compare(p0.getP(), p1.getP());
        List<Point> e = expected.getPoints().stream().sorted(comparePoints)
                .collect(Collectors.toList());
        List<Point> a = actual.getPoints().stream().sorted(comparePoints)
                .collect(Collectors.toList());
        compare("reactiveCapabilityCurve.size", e.size(), a.size());
        for (int k = 0; k < e.size(); k++) {
            Point pe = e.get(k);
            Point pa = a.get(k);
            compare("reactiveCapabilityCurvePoint.p", pe.getP(), pa.getP());
            compare("reactiveCapabilityCurvePoint.minQ", pe.getMinQ(), pa.getMinQ());
            compare("reactiveCapabilityCurvePoint.maxQ", pe.getMaxQ(), pa.getMaxQ());
        }
    }

    private void compareSwitches(Switch expected, Switch actual) {
        equivalent("VoltageLevel", expected.getVoltageLevel(), actual.getVoltageLevel());
        compareAliases(expected, actual);
        // No additional properties to check
    }

    private void testLines(Line expected, Line actual) {
        equivalent("VoltageLevel1",
                expected.getTerminal1().getVoltageLevel(),
                actual.getTerminal1().getVoltageLevel());
        equivalent("VoltageLevel2",
                expected.getTerminal2().getVoltageLevel(),
                actual.getTerminal2().getVoltageLevel());
        compareAliases(expected, actual);
        compare("r", expected.getR(), actual.getR());
        compare("x", expected.getX(), actual.getX());
        compare("g1", expected.getG1(), actual.getG1());
        compare("b1", expected.getB1(), actual.getB1());
        compare("g2", expected.getG2(), actual.getG2());
        compare("b2", expected.getB2(), actual.getB2());
        compareCurrentLimits(expected, actual,
                expected.getCurrentLimits1(),
                actual.getCurrentLimits1());
        compareCurrentLimits(expected, actual,
                expected.getCurrentLimits2(),
                actual.getCurrentLimits2());
    }

    private void compareDanglingLines(DanglingLine expected, DanglingLine actual) {
        equivalent("VoltageLevel",
                expected.getTerminal().getVoltageLevel(),
                actual.getTerminal().getVoltageLevel());
        compareAliases(expected, actual);
        compare("r", expected.getR(), actual.getR());
        compare("x", expected.getX(), actual.getX());
        compare("g", expected.getG(), actual.getG());
        compare("b", expected.getB(), actual.getB());
        compare("p0", expected.getP0(), actual.getP0());
        compare("q0", expected.getQ0(), actual.getQ0());
        compare("UcteXnodeCode", expected.getUcteXnodeCode(), actual.getUcteXnodeCode());
        compareCurrentLimits(expected, actual,
                expected.getCurrentLimits(),
                actual.getCurrentLimits());
        compareCurrentLimits(expected, actual,
                expected.getCurrentLimits(),
                actual.getCurrentLimits());
    }

    private void compareCurrentLimits(
            Identifiable bexpected,
            Identifiable bactual,
            CurrentLimits expected,
            CurrentLimits actual) {
        if (expected == null) {
            if (actual != null) {
                diff.unexpected(bactual);
                return;
            }
        } else {
            if (actual == null) {
                diff.missing(bexpected);
                return;
            }
            compare("permanentLimit", expected.getPermanentLimit(), actual.getPermanentLimit());
            compareTemporaryLimits(bactual, expected.getTemporaryLimits(), actual.getTemporaryLimits());
        }
    }

    private void compareTwoWindingTransformers(TwoWindingsTransformer expected,
                                               TwoWindingsTransformer actual) {
        equivalent("VoltageLevel1",
                expected.getTerminal1().getVoltageLevel(),
                actual.getTerminal1().getVoltageLevel());
        equivalent("VoltageLevel2",
                expected.getTerminal2().getVoltageLevel(),
                actual.getTerminal2().getVoltageLevel());
        compareAliases(expected, actual);
        compare("r", expected.getR(), actual.getR());
        compare("x", expected.getX(), actual.getX());
        compare("g", expected.getG(), actual.getG());
        compare("b", expected.getB(), actual.getB());
        compare("ratedU1", expected.getRatedU1(), actual.getRatedU1());
        compare("ratedU2", expected.getRatedU2(), actual.getRatedU2());
        compareCurrentLimits(expected, actual,
                expected.getCurrentLimits1(),
                actual.getCurrentLimits1());
        compareCurrentLimits(expected, actual,
                expected.getCurrentLimits2(),
                actual.getCurrentLimits2());

        compareRatioTapChanger(expected.getRatioTapChanger(), actual.getRatioTapChanger());
        comparePhaseTapChanger(expected.getPhaseTapChanger(), actual.getPhaseTapChanger());
        comparePhaseAngleClock2(expected.getExtension(TwoWindingsTransformerPhaseAngleClock.class), actual.getExtension(TwoWindingsTransformerPhaseAngleClock.class));
    }

    private void comparePhaseAngleClock2(TwoWindingsTransformerPhaseAngleClock expected, TwoWindingsTransformerPhaseAngleClock actual) {
        if (expected == null && actual == null) {
            return;
        } else if (expected == null && actual != null) {
            diff.unexpected("phaseAngleClock2wt");
            return;
        } else if (expected != null && actual == null) {
            diff.unexpected("phaseAngleClock2wt");
            return;
        } else {
            diff.compare("phaseAngleClock", expected.getPhaseAngleClock(), actual.getPhaseAngleClock());
        }
    }

    private void compareThreeWindingsTransformers(ThreeWindingsTransformer expected,
                                                  ThreeWindingsTransformer actual) {
        compareAliases(expected, actual);
        compareLeg(expected.getLeg1(), actual.getLeg1(), expected, actual);
        compareLeg(expected.getLeg2(), actual.getLeg2(), expected, actual);
        compareLeg(expected.getLeg3(), actual.getLeg3(), expected, actual);
        comparePhaseAngleClock3(expected.getExtension(ThreeWindingsTransformerPhaseAngleClock.class), actual.getExtension(ThreeWindingsTransformerPhaseAngleClock.class));
    }

    private void comparePhaseAngleClock3(ThreeWindingsTransformerPhaseAngleClock expected, ThreeWindingsTransformerPhaseAngleClock actual) {
        if (expected == null && actual == null) {
            return;
        } else if (expected == null && actual != null) {
            diff.unexpected("phaseAngleClock3wt");
            return;
        } else if (expected != null && actual == null) {
            diff.unexpected("phaseAngleClock3wt");
            return;
        } else {
            diff.compare("phaseAngleClockLeg2", expected.getPhaseAngleClockLeg2(), actual.getPhaseAngleClockLeg2());
            diff.compare("phaseAngleClockLeg3", expected.getPhaseAngleClockLeg3(), actual.getPhaseAngleClockLeg3());
        }
    }

    private void compareLeg(ThreeWindingsTransformer.Leg expected, ThreeWindingsTransformer.Leg actual,
                            ThreeWindingsTransformer expectedt, ThreeWindingsTransformer actualt) {
        equivalent("VoltageLevel",
                expected.getTerminal().getVoltageLevel(),
                actual.getTerminal().getVoltageLevel());
        compare("r", expected.getR(), actual.getR());
        compare("x", expected.getX(), actual.getX());
        compare("g", expected.getG(), actual.getG());
        compare("b", expected.getB(), actual.getB());

        compare("ratedU", expected.getRatedU(), actual.getRatedU());
        compareCurrentLimits(expectedt, actualt,
                expected.getCurrentLimits(),
                actual.getCurrentLimits());
        compareRatioTapChanger(expected.getRatioTapChanger(), actual.getRatioTapChanger());
        comparePhaseTapChanger(expected.getPhaseTapChanger(), actual.getPhaseTapChanger());
    }

    private void compareRatioTapChanger(
            RatioTapChanger expected,
            RatioTapChanger actual) {
        compareTapChanger(expected, actual, this::compareRatioTapChangerStep);
        if (expected == null) {
            return;
        }
        compare("ratioTapChanger.hasLoadTapChangingCapabilities",
                expected.hasLoadTapChangingCapabilities(),
                actual.hasLoadTapChangingCapabilities());
        compare("ratioTapChanger.targetV", expected.getTargetV(), actual.getTargetV());
    }

    private void comparePhaseTapChanger(
            PhaseTapChanger expected,
            PhaseTapChanger actual) {
        compareTapChanger(expected, actual, this::comparePhaseTapChangerStep);
        if (expected == null) {
            return;
        }
        compare("phaseTapChanger.regulationMode",
                expected.getRegulationMode(),
                actual.getRegulationMode());
        compare("phaseTapChanger.regulationValue",
                expected.getRegulationValue(),
                actual.getRegulationValue());
    }

    private <TC extends TapChanger<TC, TCS>, TCS extends TapChangerStep<TCS>> void compareTapChanger(
            TapChanger<TC, TCS> expected,
            TapChanger<TC, TCS> actual,
            BiConsumer<TCS, TCS> testTapChangerStep1) {
        if (expected == null) {
            if (actual != null) {
                diff.unexpected("TapChanger");
                return;
            }
        } else {
            if (actual == null) {
                diff.missing("TapChanger");
                return;
            }
            compare("tapChanger.lowTapPosition",
                    expected.getLowTapPosition(),
                    actual.getLowTapPosition());
            compare("tapChanger.highTapPosition",
                    expected.getHighTapPosition(),
                    actual.getHighTapPosition());
            compare("tapChanger.tapPosition",
                    expected.getTapPosition(),
                    actual.getTapPosition());
            compare("tapChanger.targetDeadband",
                    expected.getTargetDeadband(),
                    actual.getTargetDeadband());
            compare("tapChanger.stepCount", expected.getStepCount(), actual.getStepCount());
            // Check steps
            for (int k = expected.getLowTapPosition(); k <= expected.getHighTapPosition(); k++) {
                TCS stepExpected = expected.getStep(k);
                TCS stepActual = actual.getStep(k);
                compareTapChangerStep(stepExpected, stepActual, testTapChangerStep1);
            }
            // Check regulation
            compare("tapChanger.isRegulating", expected.isRegulating(),
                    actual.isRegulating());
            if (expected.getRegulationTerminal() == null
                    || actual.getRegulationTerminal() == null) {
                // TODO We are not checking regulation terminals if one of them is null
            } else {
                equivalent(
                        "tapChanger.RegulationTerminalConnectable",
                        expected.getRegulationTerminal().getConnectable(),
                        actual.getRegulationTerminal().getConnectable());
            }

            if (expected.getRegulationTerminal() != null || actual.getRegulationTerminal() != null) {
                if (expected.getRegulationTerminal() == null) {
                    diff.unexpected("Tap changer regulating terminal");
                    return;
                }
                if (actual.getRegulationTerminal() == null) {
                    diff.missing("TapChanger regulating terminal");
                    return;
                }
                sameIdentifier("tapChanger.getRegulationTerminal",
                        expected.getRegulationTerminal().getBusBreakerView().getBus(),
                        actual.getRegulationTerminal().getBusBreakerView().getBus());
            }
        }
    }

    private <TC extends TapChanger<TC, TCS>, TCS extends TapChangerStep<TCS>> void compareTapChangerStep(
            TCS expected,
            TCS actual,
            BiConsumer<TCS, TCS> testTapChangerStep1) {
        compare("tapChangerStep.r", expected.getR(), actual.getR());
        compare("tapChangerStep.x", expected.getX(), actual.getX());
        compare("tapChangerStep.g", expected.getG(), actual.getG());
        compare("tapChangerStep.b", expected.getB(), actual.getB());
        compare("tapChangerStep.rho", expected.getRho(), actual.getRho());
        if (testTapChangerStep1 != null) {
            testTapChangerStep1.accept(expected, actual);
        }
    }

    private void compareRatioTapChangerStep(RatioTapChangerStep expected, RatioTapChangerStep actual) {
        // No additional attributes to test
    }

    private void comparePhaseTapChangerStep(PhaseTapChangerStep expected, PhaseTapChangerStep actual) {
        compare("phaseTapChangerStep.alpha", expected.getAlpha(), actual.getAlpha());
    }

    //

    private void compareNames(String context, String expected, String actual) {
        // The names could be different only in trailing whitespace
        // Blazegraph does not preserve whitespace in input XML text
        String expected1 = expected.trim();
        String actual1 = actual.trim();
        if (config.compareNamesAllowSuffixes) {
            int endIndex = Math.min(expected.length(), actual.length());
            compare(context,
                    expected1.substring(0, endIndex),
                    actual1.substring(0, endIndex));
        } else {
            compare(context, expected1, actual1);
        }
    }

    private void compareTemporaryLimits(Identifiable bactual,
                                        Collection<CurrentLimits.TemporaryLimit> expected,
                                        Collection<CurrentLimits.TemporaryLimit> actual) {
        if (expected.size() != actual.size()) {
            diff.unexpected(bactual);
            return;
        }
        Iterator<CurrentLimits.TemporaryLimit> actualIt = actual.iterator();
        for (CurrentLimits.TemporaryLimit e : expected) {
            CurrentLimits.TemporaryLimit a = actualIt.next();
            diff.compare("temporaryLimit", e.getName(), a.getName());
            diff.compare("temporaryLimit", e.getAcceptableDuration(), a.getAcceptableDuration());
            diff.compare("temporaryLimit", e.getValue(), a.getValue());
        }
    }

    private void compare(String context, double expected, double actual) {
        diff.compare(context, expected, actual, config.tolerance);
    }

    private void compare(String context, Object expected, Object actual) {
        diff.compare(context, expected, actual);
    }

    private <I extends Identifiable<I>> void compareAliases(I expected, I actual) {
        for (String alias : expected.getAliases()) {
            if (!actual.getAliases().contains(alias)) {
                diff.missing(alias);
                break;
            }
            Optional<String> type = expected.getAliasType(alias);
            if (!type.isPresent()) {
                actual.getAliasType(alias).ifPresent(diff::unexpected);
            } else {
                if (!actual.getAliasType(alias).isPresent()) {
                    diff.missing(type.get());
                }
                compare("alias", type.get(), actual.getAliasType(alias).get());
            }
        }
        for (String alias : actual.getAliases()) {
            if (!expected.getAliases().contains(alias)) {
                diff.unexpected(alias);
            }
        }
    }

    private void equivalent(
            String context,
            Identifiable expected,
            Identifiable actual) {
        if (!networkMapping.equivalent(expected, actual)) {
            diff.notEquivalent(context, expected, actual);
        }
    }

    private void sameIdentifier(
            String context,
            Identifiable expected,
            Identifiable actual) {
        boolean sameIdentifier;
        if (expected == null) {
            sameIdentifier = actual == null;
        } else if (actual == null) {
            sameIdentifier = false;
        } else {
            sameIdentifier = expected.getId().equals(actual.getId());
        }
        if (!sameIdentifier) {
            diff.notSameIdentifier(context, expected, actual);
        }
    }

    public static String className(Identifiable o) {
        String s = o.getClass().getName();
        int dot = s.lastIndexOf('.');
        if (dot >= 0) {
            s = s.substring(dot + 1);
        }
        s = s.replace("Impl", "");
        return s;
    }

    private final Network expected;
    private final Network actual;
    private final NetworkMapping networkMapping;
    private final Differences diff;
    private final ComparisonConfig config;
}
