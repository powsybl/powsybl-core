/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.*;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.psse.model.pf.*;
import org.apache.commons.math3.complex.Complex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.converter.PsseImporter.PerUnitContext;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;

import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.*;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TransformerConverter extends AbstractConverter {

    private static final double TOLERANCE = 0.00001;

    TransformerConverter(PsseTransformer psseTransformer, ContainersMapping containersMapping,
                         PerUnitContext perUnitContext, Network network, Map<Integer, PsseBus> busNumToPsseBus, double sbase,
                         PsseVersion version, NodeBreakerImport nodeBreakerImport) {
        super(containersMapping, network);
        this.psseTransformer = Objects.requireNonNull(psseTransformer);
        this.busNumToPsseBus = Objects.requireNonNull(busNumToPsseBus);
        this.sbase = sbase;
        this.perUnitContext = Objects.requireNonNull(perUnitContext);
        this.version = Objects.requireNonNull(version);
        this.nodeBreakerImport = Objects.requireNonNull(nodeBreakerImport);
    }

    void create() {
        if (psseTransformer.getK() == 0) {
            createTwoWindingsTransformer();
        } else {
            createThreeWindingsTransformer();
        }
    }

    private void createTwoWindingsTransformer() {

        String id = getTransformerId(psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getCkt());

        String bus1Id = getBusId(psseTransformer.getI());
        String voltageLevel1Id = getContainersMapping().getVoltageLevelId(psseTransformer.getI());
        VoltageLevel voltageLevel1 = getNetwork().getVoltageLevel(voltageLevel1Id);
        double baskv1 = busNumToPsseBus.get(psseTransformer.getI()).getBaskv();

        String bus2Id = getBusId(psseTransformer.getJ());
        String voltageLevel2Id = getContainersMapping().getVoltageLevelId(psseTransformer.getJ());
        VoltageLevel voltageLevel2 = getNetwork().getVoltageLevel(voltageLevel2Id);
        double baskv2 = busNumToPsseBus.get(psseTransformer.getJ()).getBaskv();

        double sbase12 = psseTransformer.getSbase12();
        double nomV1 = getNomV(psseTransformer.getWinding1(), voltageLevel1);
        double nomV2 = getNomV(psseTransformer.getWinding2(), voltageLevel2);

        Complex z = defineImpedanceBetweenWindings(psseTransformer.getR12(), psseTransformer.getX12(), sbase, sbase12, psseTransformer.getCz());

        // Handling terminal ratios
        ComplexRatio w1 = defineComplexRatio(psseTransformer.getWinding1().getWindv(), psseTransformer.getWinding1().getAng(), baskv1, nomV1, psseTransformer.getCw());
        double w2 = defineRatio(psseTransformer.getWinding2().getWindv(), baskv2, nomV2, psseTransformer.getCw());
        TapChanger tapChanger = defineTapChanger(w1, psseTransformer.getWinding1(), baskv1, nomV1, psseTransformer.getCw());

        // Handling magnetizing admittance Gm and Bm
        Complex ysh = defineShuntAdmittance(id, psseTransformer.getMag1(), psseTransformer.getMag2(), sbase, sbase12, baskv1, nomV1, psseTransformer.getCm());

        // To engineering units
        z = impedanceToEngineeringUnits(z, voltageLevel2.getNominalV(), perUnitContext.sb());
        ysh = admittanceToEngineeringUnits(ysh, voltageLevel2.getNominalV(), perUnitContext.sb());

        // move w2 to side 1
        z = impedanceAdjustmentAfterMovingRatio(z, w2);
        TapChanger tapChangerAdjustedRatio = tapChangerAdjustmentAfterMovingRatio(tapChanger, w2);

        // move ysh between w1 and z
        // Ysh_eu = Ysh_pu * sbase / (vn1 *vn1) Convert Ysh per unit to engineering units
        // Ysh_eu_moved = Ysh_eu * (ratedU1 / ratedU2) * (ratedU1 / ratedU2) Apply the structural ratio when moving
        // Ysh_eu_moved = Ysh_eu * sbase / (vn2 * vn2) as ratedU1 = vn1 and ratedU2 = vn2
        // As vn2 is used to convert to eu, only the ratio remains to be applied
        TapChanger tapChangerAdjustedYsh = tapChangerAdjustmentAfterMovingShuntAdmittanceBetweenRatioAndTransmissionImpedance(tapChangerAdjustedRatio);

        TwoWindingsTransformerAdder adder = voltageLevel2.getSubstation()
                .orElseThrow(() -> new PowsyblException("Substation null! Transformer must be within a substation"))
                .newTwoWindingsTransformer()
                .setId(id)
                .setEnsureIdUnicity(true)
                .setVoltageLevel1(voltageLevel1Id)
                .setVoltageLevel2(voltageLevel2Id)
                .setRatedU1(voltageLevel1.getNominalV())
                .setRatedU2(voltageLevel2.getNominalV())
                .setR(z.getReal())
                .setX(z.getImaginary())
                .setG(ysh.getReal())
                .setB(ysh.getImaginary());

        String equipmentId = getNodeBreakerEquipmentId(PSSE_TWO_WINDING, psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getCkt());
        OptionalInt node1 = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentId, psseTransformer.getI()));
        if (node1.isPresent()) {
            adder.setNode1(node1.getAsInt());
        } else {
            adder.setConnectableBus1(bus1Id);
            adder.setBus1(psseTransformer.getStat() == 1 ? bus1Id : null);
        }
        OptionalInt node2 = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentId, psseTransformer.getJ()));
        if (node2.isPresent()) {
            adder.setNode2(node2.getAsInt());
        } else {
            adder.setConnectableBus2(bus2Id);
            adder.setBus2(psseTransformer.getStat() == 1 ? bus2Id : null);
        }

        TwoWindingsTransformer twt = adder.add();

        tapChangerToIidm(tapChangerAdjustedYsh, twt);
        defineOperationalLimits(twt, voltageLevel1.getNominalV(), voltageLevel2.getNominalV());
    }

    private void createThreeWindingsTransformer() {
        String id = getTransformerId(psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getK(), psseTransformer.getCkt());

        String bus1Id = getBusId(psseTransformer.getI());
        String voltageLevel1Id = getContainersMapping().getVoltageLevelId(psseTransformer.getI());
        VoltageLevel voltageLevel1 = getNetwork().getVoltageLevel(voltageLevel1Id);
        double baskv1 = busNumToPsseBus.get(psseTransformer.getI()).getBaskv();

        String bus2Id = getBusId(psseTransformer.getJ());
        String voltageLevel2Id = getContainersMapping().getVoltageLevelId(psseTransformer.getJ());
        VoltageLevel voltageLevel2 = getNetwork().getVoltageLevel(voltageLevel2Id);
        double baskv2 = busNumToPsseBus.get(psseTransformer.getJ()).getBaskv();

        String bus3Id = getBusId(psseTransformer.getK());
        String voltageLevel3Id = getContainersMapping().getVoltageLevelId(psseTransformer.getK());
        VoltageLevel voltageLevel3 = getNetwork().getVoltageLevel(voltageLevel3Id);
        double baskv3 = busNumToPsseBus.get(psseTransformer.getK()).getBaskv();

        double sbase12 = psseTransformer.getSbase12();
        double sbase23 = psseTransformer.getSbase23();
        double sbase31 = psseTransformer.getSbase31();

        double nomV1 = getNomV(psseTransformer.getWinding1(), voltageLevel1);
        double nomV2 = getNomV(psseTransformer.getWinding2(), voltageLevel2);
        double nomV3 = getNomV(psseTransformer.getWinding3(), voltageLevel3);

        Complex z12 = defineImpedanceBetweenWindings(psseTransformer.getR12(), psseTransformer.getX12(), sbase, sbase12, psseTransformer.getCz());
        Complex z23 = defineImpedanceBetweenWindings(psseTransformer.getR23(), psseTransformer.getX23(), sbase, sbase23, psseTransformer.getCz());
        Complex z31 = defineImpedanceBetweenWindings(psseTransformer.getR31(), psseTransformer.getX31(), sbase, sbase31, psseTransformer.getCz());

        // Transform impedances between windings to star
        Complex z1 = z12.add(z31).subtract(z23).multiply(0.5);
        Complex z2 = z12.add(z23).subtract(z31).multiply(0.5);
        Complex z3 = z23.add(z31).subtract(z12).multiply(0.5);

        // Handling terminal ratios
        ComplexRatio w1 = defineComplexRatio(psseTransformer.getWinding1().getWindv(), psseTransformer.getWinding1().getAng(), baskv1, nomV1, psseTransformer.getCw());
        ComplexRatio w2 = defineComplexRatio(psseTransformer.getWinding2().getWindv(), psseTransformer.getWinding2().getAng(), baskv2, nomV2, psseTransformer.getCw());
        ComplexRatio w3 = defineComplexRatio(psseTransformer.getWinding3().getWindv(), psseTransformer.getWinding3().getAng(), baskv3, nomV3, psseTransformer.getCw());

        TapChanger tapChanger1 = defineTapChanger(w1, psseTransformer.getWinding1(), baskv1, nomV1, psseTransformer.getCw());
        TapChanger tapChanger2 = defineTapChanger(w2, psseTransformer.getWinding2(), baskv2, nomV2, psseTransformer.getCw());
        TapChanger tapChanger3 = defineTapChanger(w3, psseTransformer.getWinding3(), baskv3, nomV3, psseTransformer.getCw());

        // Handling magnetizing admittance Gm and Bm
        Complex ysh = defineShuntAdmittance(id, psseTransformer.getMag1(), psseTransformer.getMag2(), sbase, sbase12, baskv1, nomV1, psseTransformer.getCm());

        // set a voltage base at star node with the associated Zbase
        double v0 = 1.0;

        // To engineering units
        z1 = impedanceToEngineeringUnits(z1, v0, perUnitContext.sb());
        z2 = impedanceToEngineeringUnits(z2, v0, perUnitContext.sb());
        z3 = impedanceToEngineeringUnits(z3, v0, perUnitContext.sb());
        ysh = admittanceToEngineeringUnits(ysh, v0, perUnitContext.sb());

        // move ysh between w1 and z
        TapChanger tapChanger1AdjustedYsh = tapChangerAdjustmentAfterMovingShuntAdmittanceBetweenRatioAndTransmissionImpedance(tapChanger1);

        ThreeWindingsTransformerAdder adder = voltageLevel1.getSubstation()
                .orElseThrow(() -> new PowsyblException("Substation null! Transformer must be within a substation"))
                .newThreeWindingsTransformer()
                .setRatedU0(v0)
                .setEnsureIdUnicity(true)
                .setId(id);
        ThreeWindingsTransformerAdder.LegAdder legAdder1 = adder
                .newLeg1()
                .setR(z1.getReal())
                .setX(z1.getImaginary())
                .setG(ysh.getReal())
                .setB(ysh.getImaginary())
                .setRatedU(voltageLevel1.getNominalV())
                .setVoltageLevel(voltageLevel1Id);
        ThreeWindingsTransformerAdder.LegAdder legAdder2 = adder
                .newLeg2()
                .setR(z2.getReal())
                .setX(z2.getImaginary())
                .setG(0)
                .setB(0)
                .setRatedU(voltageLevel2.getNominalV())
                .setVoltageLevel(voltageLevel2Id);
        ThreeWindingsTransformerAdder.LegAdder legAdder3 = adder
                .newLeg3()
                .setR(z3.getReal())
                .setX(z3.getImaginary())
                .setG(0)
                .setB(0)
                .setRatedU(voltageLevel3.getNominalV())
                .setVoltageLevel(voltageLevel3Id);

        String equipmentId = getNodeBreakerEquipmentId(PSSE_THREE_WINDING, psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getK(), psseTransformer.getCkt());
        legConnectivity(legAdder1, equipmentId, psseTransformer.getI(), bus1Id, leg1IsConnected());
        legAdder1.add();
        legConnectivity(legAdder2, equipmentId, psseTransformer.getJ(), bus2Id, leg2IsConnected());
        legAdder2.add();
        legConnectivity(legAdder3, equipmentId, psseTransformer.getK(), bus3Id, leg3IsConnected());
        legAdder3.add();
        ThreeWindingsTransformer twt = adder.add();

        twt.setProperty("v", Double.toString(psseTransformer.getVmstar() * v0));
        twt.setProperty("angle", Double.toString(psseTransformer.getAnstar()));

        tapChangersToIidm(tapChanger1AdjustedYsh, tapChanger2, tapChanger3, twt);
        defineOperationalLimits(twt, voltageLevel1.getNominalV(), voltageLevel2.getNominalV(), voltageLevel3.getNominalV());
    }

    private void legConnectivity(ThreeWindingsTransformerAdder.LegAdder legAdder, String equipmentId, int bus, String busId, boolean isLegConnected) {
        OptionalInt node1 = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentId, bus));
        if (node1.isPresent()) {
            legAdder.setNode(node1.getAsInt());
        } else {
            legAdder.setConnectableBus(busId);
            legAdder.setBus(isLegConnected ? busId : null);
        }
    }

    private static double getNomV(PsseTransformerWinding winding, VoltageLevel voltageLevel) {
        double nomV = winding.getNomv();
        if (nomV == 0.0) {
            nomV = voltageLevel.getNominalV();
        }
        return nomV;
    }

    private boolean leg1IsConnected() {
        return psseTransformer.getStat() == 1 || psseTransformer.getStat() == 2 || psseTransformer.getStat() == 3;
    }

    private boolean leg2IsConnected() {
        return psseTransformer.getStat() == 1 || psseTransformer.getStat() == 3 || psseTransformer.getStat() == 4;
    }

    private boolean leg3IsConnected() {
        return psseTransformer.getStat() == 1 || psseTransformer.getStat() == 2 || psseTransformer.getStat() == 4;
    }

    private static Complex defineImpedanceBetweenWindings(double r, double x, double sbase, double windingSbase, int cz) {
        double rw;
        double xw;
        switch (cz) {
            case 1:
                rw = r;
                xw = x;
                break;
            case 2:
                // change to right Sbase pu
                rw = r * sbase / windingSbase;
                xw = x * sbase / windingSbase;
                break;
            case 3:
                // convert load loss power and current into pu impedances
                rw = r / windingSbase / 1000000;
                xw = Math.sqrt(x * x - rw * rw);

                rw = rw * sbase / windingSbase;
                xw = xw * sbase / windingSbase;
                break;
            default:
                throw new PsseException("Unexpected CZ = " + cz);
        }
        return new Complex(rw, xw);
    }

    private static Complex defineShuntAdmittance(String id, double magG, double magB, double sbase, double windingSbase,
                                                 double baskv, double nomV, int cm) {
        double g;
        double b;
        switch (cm) {
            case 1:
                // g and b represent the values of the magnetizing admittance at the i end in pu at 1/Zb1 base where Zb1 = Vb1*Vb1/Sb1
                // Vb1 is the bus i voltage base (BASKV) and Sb1 is the system MVA base which is SBASE
                g = magG;
                b = magB;
                break;
            case 2:
                g = magG / (1000000 * sbase) * (baskv / nomV) * (baskv / nomV);
                double y = magB * (windingSbase / sbase) * (baskv / nomV) * (baskv / nomV);
                double b2 = y * y - g * g;
                if (b2 >= 0) {
                    b = -Math.sqrt(b2);
                } else {
                    b = 0.0;
                    LOGGER.warn("Magnetizing susceptance of Transformer ({}) set to 0 because admittance module is ({}) and conductance is ({})  ", id, y, g);
                }
                break;
            default:
                throw new PsseException("Unexpected CM = " + cm);
        }
        return new Complex(g, b);
    }

    private static ComplexRatio defineComplexRatio(double windV, double ang, double baskv, double nomV, int cw) {
        return new ComplexRatio(defineRatio(windV, baskv, nomV, cw), ang);
    }

    private static double defineRatio(double windV, double baskv, double nomV, int cw) {
        double ratio;
        switch (cw) {
            case 1:
                ratio = windV;
                break;
            case 2:
                ratio = windV / baskv;
                break;
            case 3:
                ratio = windV * nomV / baskv;
                break;
            default:
                throw new PsseException("Unexpected CW = " + cw);
        }
        return ratio;
    }

    private static double defineWindV(double ratio, double baskv, double nomV, int cw) {
        double windV;
        switch (cw) {
            case 1:
                windV = ratio;
                break;
            case 2:
                windV = ratio * baskv;
                break;
            case 3:
                windV = ratio * baskv / nomV;
                break;
            default:
                throw new PsseException("Unexpected CW = " + cw);
        }
        return windV;
    }

    private static TapChanger defineTapChanger(ComplexRatio complexRatio, PsseTransformerWinding winding, double baskv,
                                               double nomv, int cw) {

        TapChanger tapChanger = defineRawTapChanger(complexRatio, winding.getRma(), winding.getRmi(),
                winding.getNtp(), baskv, nomv, cw, winding.getCod());
        tapChanger.setTapPosition(defineTapPositionAndAdjustTapChangerToCurrentRatio(complexRatio, tapChanger));

        return tapChanger;
    }

    private static TapChanger defineRawTapChanger(ComplexRatio complexRatio, double rma, double rmi,
                                                  int ntp, double baskv, double nomv, int cw, int cod) {
        TapChanger tapChanger = new TapChanger();

        if (ntp <= 1) {
            tapChanger.getSteps().add(new TapChangerStep(complexRatio.getRatio(), complexRatio.getAngle()));
            return tapChanger;
        }

        // RatioTapChanger
        if (complexRatio.getAngle() == 0.0 && (isVoltageControl(cod) || isReactivePowerControl(cod))) {
            double stepRatioIncrement = (rma - rmi) / (ntp - 1);
            for (int i = 0; i < ntp; i++) {
                double ratio = defineRatio(rmi + stepRatioIncrement * i, baskv, nomv, cw);
                tapChanger.getSteps().add(new TapChangerStep(ratio, complexRatio.getAngle()));
            }
            return tapChanger;
        } else if (isActivePowerControl(cod)) { // PhaseTapChanger
            double stepAngleIncrement = (rma - rmi) / (ntp - 1);
            for (int i = 0; i < ntp; i++) {
                double angle = rmi + stepAngleIncrement * i;
                tapChanger.getSteps().add(new TapChangerStep(complexRatio.getRatio(), angle));
            }
            return tapChanger;
        } else {
            tapChanger.getSteps().add(new TapChangerStep(complexRatio.getRatio(), complexRatio.getAngle()));
            return tapChanger;
        }
    }

    private static boolean isVoltageControl(int cod) {
        return Math.abs(cod) == 1;
    }

    private static boolean isReactivePowerControl(int cod) {
        return Math.abs(cod) == 2;
    }

    private static boolean isActivePowerControl(int cod) {
        return Math.abs(cod) == 3;
    }

    private static int defineTapPositionAndAdjustTapChangerToCurrentRatio(ComplexRatio complexRatio, TapChanger tapChanger) {
        List<TapChangerStep> steps = tapChanger.getSteps();

        for (int i = 0; i < steps.size(); i++) {
            TapChangerStep step = steps.get(i);
            double distanceRatio = distance(step.getRatio(), complexRatio.getRatio());
            double distanceAngle = distance(step.getAngle(), complexRatio.getAngle());

            if (distanceRatio == 0.0 && distanceAngle == 0.0) {
                return i;
            }
            if (distanceAngle > 0.0
                    || distanceAngle == 0.0 && distanceRatio > 0.0) {
                tapChanger.getSteps().add(i, new TapChangerStep(complexRatio.getRatio(), complexRatio.getAngle()));
                return i;
            }
        }

        tapChanger.getSteps().add(new TapChangerStep(complexRatio.getRatio(), complexRatio.getAngle()));
        return tapChanger.getSteps().size() - 1;
    }

    private static double distance(double stepValue, double currentValue) {
        double distance = stepValue - currentValue;
        if (Math.abs(distance) <= TOLERANCE) {
            return 0.0;
        }
        return distance;
    }

    private static void tapChangerToIidm(TapChanger tapChanger, TwoWindingsTransformer twt) {
        if (isPhaseTapChanger(tapChanger)) {
            PhaseTapChangerAdder ptc = twt.newPhaseTapChanger();
            tapChangerToPhaseTapChanger(tapChanger, ptc);
        } else if (isRatioTapChanger(tapChanger)) {
            RatioTapChangerAdder rtc = twt.newRatioTapChanger();
            tapChangerToRatioTapChanger(tapChanger, rtc);
        }
    }

    private static void tapChangersToIidm(TapChanger tapChanger1, TapChanger tapChanger2, TapChanger tapChanger3, ThreeWindingsTransformer twt) {
        tapChangerToIidmLeg(tapChanger1, twt.getLeg1());
        tapChangerToIidmLeg(tapChanger2, twt.getLeg2());
        tapChangerToIidmLeg(tapChanger3, twt.getLeg3());
    }

    private static void tapChangerToIidmLeg(TapChanger tapChanger, Leg leg) {
        if (isPhaseTapChanger(tapChanger)) {
            PhaseTapChangerAdder ptc = leg.newPhaseTapChanger();
            tapChangerToPhaseTapChanger(tapChanger, ptc);
        } else if (isRatioTapChanger(tapChanger)) {
            RatioTapChangerAdder rtc = leg.newRatioTapChanger();
            tapChangerToRatioTapChanger(tapChanger, rtc);
        }
    }

    private static boolean isPhaseTapChanger(TapChanger tapChanger) {
        return tapChanger.getSteps().stream().anyMatch(step -> step.getAngle() != 0.0);
    }

    private static boolean isRatioTapChanger(TapChanger tapChanger) {
        return tapChanger.getSteps().stream().anyMatch(step -> step.getRatio() != 1.0);
    }

    private static void tapChangerToRatioTapChanger(TapChanger tapChanger, RatioTapChangerAdder rtc) {
        rtc.setLoadTapChangingCapabilities(false)
                .setLowTapPosition(0)
                .setTapPosition(tapChanger.getTapPosition());

        tapChanger.getSteps().forEach(step ->
                rtc.beginStep()
                        .setRho(1 / step.getRatio())
                        .setR(step.getR())
                        .setX(step.getX())
                        .setB(step.getB1())
                        .setG(step.getG1())
                        .endStep());
        rtc.add();

    }

    private static void tapChangerToPhaseTapChanger(TapChanger tapChanger, PhaseTapChangerAdder ptc) {
        ptc.setLowTapPosition(0)
                .setTapPosition(tapChanger.getTapPosition());

        tapChanger.getSteps().forEach(step ->
                ptc.beginStep()
                        .setRho(1 / step.getRatio())
                        .setAlpha(-step.getAngle())
                        .setR(step.getR())
                        .setX(step.getX())
                        .setB(step.getB1())
                        .setG(step.getG1())
                        .endStep());
        ptc.setRegulating(false).setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP).add();
    }

    private static Complex impedanceAdjustmentAfterMovingRatio(Complex impedance, double a) {
        return impedance.multiply(a * a);
    }

    private static double admittanceAdjustmentAfterMovingItBetweenRatioAndTransmissionImpedance(double admittance, Complex a) {
        return admittance * a.abs() * a.abs();
    }

    private TapChanger tapChangerAdjustmentAfterMovingRatio(TapChanger tapChanger, double a) {
        tapChanger.getSteps().forEach(step -> step.setRatio(step.getRatio() / a));
        return tapChanger;
    }

    private TapChanger tapChangerAdjustmentAfterMovingShuntAdmittanceBetweenRatioAndTransmissionImpedance(TapChanger tapChanger) {
        tapChanger.getSteps().forEach(step -> {

            Complex a = new Complex(step.getRatio() * Math.cos(Math.toRadians(step.getAngle())), step.getRatio() * Math.sin(Math.toRadians(step.getAngle())));
            step.setG1(100 * (admittanceAdjustmentAfterMovingItBetweenRatioAndTransmissionImpedance(1 + step.getG1() / 100, a) - 1));
            step.setB1(100 * (admittanceAdjustmentAfterMovingItBetweenRatioAndTransmissionImpedance(1 + step.getB1() / 100, a) - 1));
        });

        return tapChanger;
    }

    private void defineOperationalLimits(TwoWindingsTransformer twt, double vnom1, double vnom2) {
        double rateMva = getRateWinding1();

        double currentLimit1 = rateMva / (Math.sqrt(3.0) * vnom1);
        double currentLimit2 = rateMva / (Math.sqrt(3.0) * vnom2);

        // CurrentPermanentLimit in A
        if (currentLimit1 > 0) {
            CurrentLimitsAdder currentLimitFrom = twt.newCurrentLimits1();
            currentLimitFrom.setPermanentLimit(currentLimit1 * 1000);
            currentLimitFrom.add();
        }

        if (currentLimit2 > 0) {
            CurrentLimitsAdder currentLimitTo = twt.newCurrentLimits2();
            currentLimitTo.setPermanentLimit(currentLimit2 * 1000);
            currentLimitTo.add();
        }
    }

    private void defineOperationalLimits(ThreeWindingsTransformer twt, double vnom1, double vnom2, double vnom3) {
        double rateMva1 = getRateWinding1();
        double rateMva2 = getRateWinding2();
        double rateMva3 = getRateWinding3();

        double currentLimit1 = rateMva1 / (Math.sqrt(3.0) * vnom1);
        double currentLimit2 = rateMva2 / (Math.sqrt(3.0) * vnom2);
        double currentLimit3 = rateMva3 / (Math.sqrt(3.0) * vnom3);

        // CurrentPermanentLimit in A
        if (currentLimit1 > 0) {
            CurrentLimitsAdder currentLimitFrom = twt.getLeg1().newCurrentLimits();
            currentLimitFrom.setPermanentLimit(currentLimit1 * 1000);
            currentLimitFrom.add();
        }
        if (currentLimit2 > 0) {
            CurrentLimitsAdder currentLimitFrom = twt.getLeg2().newCurrentLimits();
            currentLimitFrom.setPermanentLimit(currentLimit2 * 1000);
            currentLimitFrom.add();
        }
        if (currentLimit3 > 0) {
            CurrentLimitsAdder currentLimitFrom = twt.getLeg3().newCurrentLimits();
            currentLimitFrom.setPermanentLimit(currentLimit3 * 1000);
            currentLimitFrom.add();
        }
    }

    private double getRateWinding1() {
        double rateMva;
        if (version.major() == V35) {
            rateMva = psseTransformer.getWinding1Rates().getRate1();
        } else {
            rateMva = psseTransformer.getWinding1Rates().getRatea();
        }
        return rateMva;
    }

    private double getRateWinding2() {
        double rateMva;
        if (version.major() == V35) {
            rateMva = psseTransformer.getWinding2Rates().getRate1();
        } else {
            rateMva = psseTransformer.getWinding2Rates().getRatea();
        }
        return rateMva;
    }

    private double getRateWinding3() {
        double rateMva;
        if (version.major() == V35) {
            rateMva = psseTransformer.getWinding3Rates().getRate1();
        } else {
            rateMva = psseTransformer.getWinding3Rates().getRatea();
        }
        return rateMva;
    }

    static class TapChanger {
        int tapPosition;
        List<TapChangerStep> steps;

        TapChanger() {
            steps = new ArrayList<>();
        }

        void setTapPosition(int tapPosition) {
            this.tapPosition = tapPosition;
        }

        int getTapPosition() {
            return tapPosition;
        }

        List<TapChangerStep> getSteps() {
            return steps;
        }
    }

    // angle in degrees
    static class TapChangerStep {
        double ratio;
        double angle;
        double r;
        double x;
        double g1;
        double b1;

        TapChangerStep(double ratio, double angle) {
            this.ratio = ratio;
            this.angle = angle;
            this.r = 0.0;
            this.x = 0.0;
            this.g1 = 0.0;
            this.b1 = 0.0;
        }

        TapChangerStep(double ratio, double angle, double r, double x, double g1, double b1) {
            this.ratio = ratio;
            this.angle = angle;
            this.r = r;
            this.x = x;
            this.g1 = g1;
            this.b1 = b1;
        }

        void setRatio(double ratio) {
            this.ratio = ratio;
        }

        double getRatio() {
            return ratio;
        }

        void setAngle(double angle) {
            this.angle = angle;
        }

        double getAngle() {
            return angle;
        }

        double getR() {
            return r;
        }

        double getX() {
            return x;
        }

        void setG1(double g1) {
            this.g1 = g1;
        }

        double getG1() {
            return g1;
        }

        void setB1(double b1) {
            this.b1 = b1;
        }

        double getB1() {
            return b1;
        }
    }

    // angle in degrees
    static class ComplexRatio {
        double ratio;
        double angle;

        ComplexRatio(double ratio, double angle) {
            this.ratio = ratio;
            this.angle = angle;
        }

        double getRatio() {
            return ratio;
        }

        double getAngle() {
            return angle;
        }
    }

    public void addControl() {
        if (isTwoWindingsTransformer(psseTransformer)) {
            addControlTwoWindingsTransformer();
        } else {
            addControlThreeWindingsTransformer();
        }
    }

    private static boolean isTwoWindingsTransformer(PsseTransformer psseTransformer) {
        return psseTransformer.getK() == 0;
    }

    private void addControlTwoWindingsTransformer() {
        String id = getTransformerId(psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getCkt());
        String equipmentId = getNodeBreakerEquipmentId(PSSE_TWO_WINDING, psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getCkt());
        TwoWindingsTransformer twt = getNetwork().getTwoWindingsTransformer(id);
        if (twt == null) {
            return;
        }
        boolean regulatingForcedToOff = false;
        if (twt.hasRatioTapChanger()) {
            boolean regulating = defineVoltageControl(getNetwork(), twt.getId(), equipmentId, psseTransformer.getWinding1(), twt.getRatioTapChanger(), regulatingForcedToOff, nodeBreakerImport);
            regulatingForcedToOff = forceRegulatingToOff(regulatingForcedToOff, regulating);
        }
        if (twt.hasPhaseTapChanger()) {
            defineActivePowerControl(getNetwork(), twt.getId(), equipmentId, psseTransformer.getWinding1(), twt.getPhaseTapChanger(), regulatingForcedToOff, nodeBreakerImport);
        }
    }

    private void addControlThreeWindingsTransformer() {
        String id = getTransformerId(psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getK(), psseTransformer.getCkt());
        String equipmentId = getNodeBreakerEquipmentId(PSSE_THREE_WINDING, psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getK(), psseTransformer.getCkt());
        ThreeWindingsTransformer twt = getNetwork().getThreeWindingsTransformer(id);
        if (twt == null) {
            return;
        }
        boolean regulatingForcedToOff = false;
        regulatingForcedToOff = addControlThreeWindingsTransformerLeg(getNetwork(), twt.getId(), equipmentId, twt.getLeg1(), psseTransformer.getWinding1(), regulatingForcedToOff, nodeBreakerImport);
        regulatingForcedToOff = addControlThreeWindingsTransformerLeg(getNetwork(), twt.getId(), equipmentId, twt.getLeg2(), psseTransformer.getWinding2(), regulatingForcedToOff, nodeBreakerImport);
        addControlThreeWindingsTransformerLeg(getNetwork(), twt.getId(), equipmentId, twt.getLeg3(), psseTransformer.getWinding3(), regulatingForcedToOff, nodeBreakerImport);
    }

    private static boolean addControlThreeWindingsTransformerLeg(Network network, String id, String equipmentId, Leg leg,
                                                                 PsseTransformerWinding winding, boolean regulatingForcedToOffInput, NodeBreakerImport nodeBreakerImport) {
        boolean regulatingForcedToOff = regulatingForcedToOffInput;
        if (leg.hasRatioTapChanger()) {
            boolean regulating = defineVoltageControl(network, id, equipmentId, winding, leg.getRatioTapChanger(), regulatingForcedToOff, nodeBreakerImport);
            regulatingForcedToOff = forceRegulatingToOff(regulatingForcedToOff, regulating);
        }
        if (leg.hasPhaseTapChanger()) {
            boolean regulating = defineActivePowerControl(network, id, equipmentId, winding, leg.getPhaseTapChanger(), regulatingForcedToOff, nodeBreakerImport);
            regulatingForcedToOff = forceRegulatingToOff(regulatingForcedToOff, regulating);
        }
        return regulatingForcedToOff;
    }

    private static boolean defineVoltageControl(Network network, String id, String equipmentId, PsseTransformerWinding winding, RatioTapChanger rtc,
                                                boolean regulatingForcedToOff, NodeBreakerImport nodeBreakerImport) {
        if (isReactivePowerControl(winding.getCod())) {
            LOGGER.warn("Transformer {}. Reactive power control not supported", id);
            return false;
        }
        if (!isVoltageControl(winding.getCod())) {
            return false;
        }

        Terminal regulatingTerminal = defineRegulatingTerminal(network, id, equipmentId, winding, nodeBreakerImport);
        // Discard control if the transformer is controlling an isolated bus
        if (regulatingTerminal == null) {
            return false;
        }
        double vnom = regulatingTerminal.getVoltageLevel().getNominalV();
        double vmin = winding.getVmi() * vnom;
        double vmax = winding.getVma() * vnom;
        double targetV = (vmin + vmax) * 0.5;
        double targetDeadBand = vmax - vmin;

        boolean regulating = true;
        if (targetV <= 0.0 || targetDeadBand < 0.0) {
            regulating = false;
        }
        if (regulating && regulatingForcedToOff) {
            LOGGER.warn("Transformer {}. Regulating control forced to off. Only one control is supported", id);
            regulating = false;
        }
        rtc.setTargetV(targetV)
                .setTargetDeadband(targetDeadBand)
                .setRegulationTerminal(regulatingTerminal)
                .setRegulating(regulating);

        return regulating;
    }

    private static boolean defineActivePowerControl(Network network, String id, String equipmentId, PsseTransformerWinding winding, PhaseTapChanger ptc, boolean regulatingForcedToOff, NodeBreakerImport nodeBreakerImport) {
        if (!isActivePowerControl(winding.getCod())) {
            return false;
        }

        Terminal regulatingTerminal = defineRegulatingTerminal(network, id, equipmentId, winding, nodeBreakerImport);
        // Discard control if the transformer is controlling an isolated bus
        if (regulatingTerminal == null) {
            return false;
        }
        double activePowerMin = winding.getVmi();
        double activePowerMax = winding.getVma();
        double targetValue = 0.5 * (activePowerMin + activePowerMax);
        double targetDeadBand = activePowerMax - activePowerMin;
        boolean regulating = true;
        if (targetDeadBand < 0.0) {
            regulating = false;
        }
        if (regulating && regulatingForcedToOff) {
            LOGGER.warn("Transformer {}. Regulating control forced to off. Only one control is supported", id);
            regulating = false;
        }

        ptc.setRegulationValue(targetValue)
                .setTargetDeadband(targetDeadBand)
                .setRegulationTerminal(regulatingTerminal)
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulating(regulating);

        return regulating;
    }

    private static boolean forceRegulatingToOff(boolean regulatingForcedToOff, boolean regulating) {
        return regulatingForcedToOff || regulating;
    }

    private static Terminal defineRegulatingTerminal(Network network, String id, String equipmentId, PsseTransformerWinding winding, NodeBreakerImport nodeBreakerImport) {
        Terminal regulatingTerminal = null;

        int busI = Math.abs(winding.getCont());
        Optional<NodeBreakerImport.NodeBreakerControlNode> controlNode = nodeBreakerImport.getControlNode(getNodeBreakerEquipmentIdBus(equipmentId, busI));
        if (controlNode.isPresent()) {
            regulatingTerminal = findTerminalNode(network, controlNode.get().getVoltageLevelId(), controlNode.get().getNode());
        } else {
            String regulatingBusId = getBusId(busI);
            Bus bus = network.getBusBreakerView().getBus(regulatingBusId);
            if (bus != null) {
                regulatingTerminal = bus.getConnectedTerminalStream().findFirst().orElse(null);
            }
        }
        if (regulatingTerminal == null) {
            LOGGER.warn("Transformer {}. Regulating terminal is not assigned as the bus is isolated", id);
        }
        return regulatingTerminal;
    }

    static void updateAndCreateTransformers(Network network, PssePowerFlowModel psseModel, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        Map<String, PsseTransformer> transformersToPsseTransformer = new HashMap<>();
        psseModel.getTransformers().forEach(psseTransformer -> transformersToPsseTransformer.put(findTransformerId(psseTransformer), psseTransformer));

        network.getTwoWindingsTransformers().forEach(t2w -> {
            if (transformersToPsseTransformer.containsKey(t2w.getId())) {
                updateTwoWindingsTransformer(t2w, transformersToPsseTransformer.get(t2w.getId()), contextExport);
            } else {
                psseModel.addTransformers(Collections.singletonList(createTwoWindingsTransformer(t2w, contextExport, perUnitContext)));
            }
        });
        network.getThreeWindingsTransformers().forEach(t3w -> {
            if (transformersToPsseTransformer.containsKey(t3w.getId())) {
                updateThreeWindingsTransformer(t3w, transformersToPsseTransformer.get(t3w.getId()), contextExport);
            } else {
                psseModel.addTransformers(Collections.singletonList(createThreeWindingsTransformer(t3w, contextExport, perUnitContext)));
            }
        });
        psseModel.replaceAllTransformers(psseModel.getTransformers().stream().sorted(Comparator.comparingInt(PsseTransformer::getI).thenComparingInt(PsseTransformer::getJ).thenComparingInt(PsseTransformer::getK).thenComparing(PsseTransformer::getCkt)).toList());
    }

    private static String findTransformerId(PsseTransformer psseTransformer) {
        if (isTwoWindingsTransformer(psseTransformer)) {
            return getTransformerId(psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getCkt());
        } else {
            return getTransformerId(psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getK(), psseTransformer.getCkt());
        }
    }

    private static void updateTwoWindingsTransformer(TwoWindingsTransformer t2w, PsseTransformer psseTransformer, ContextExport contextExport) {
        double baskv1 = t2w.getTerminal1().getVoltageLevel().getNominalV();
        double nomV1 = getNomV(psseTransformer.getWinding1(), t2w.getTerminal1().getVoltageLevel());
        int busI = getTerminalBusI(t2w.getTerminal1(), contextExport);
        int busJ = getTerminalBusI(t2w.getTerminal2(), contextExport);
        int regulatingBus = getRegulatingTerminalBusI(findRegulatingTerminal(t2w, psseTransformer.getWinding1().getCod()), contextExport);

        psseTransformer.getWinding1().setWindv(defineWindV(getRatio(t2w.getRatioTapChanger(), t2w.getPhaseTapChanger()), baskv1, nomV1, psseTransformer.getCw()));
        psseTransformer.getWinding1().setAng(getAngle(t2w.getPhaseTapChanger()));
        psseTransformer.setI(busI);
        psseTransformer.setJ(busJ);
        psseTransformer.getWinding1().setCont(regulatingBus);
        psseTransformer.setStat(getStatus(t2w));
    }

    private static PsseTransformer createTwoWindingsTransformer(TwoWindingsTransformer t2w, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        PsseTransformer psseTransformer = new PsseTransformer();

        int busI = getTerminalBusI(t2w.getTerminal1(), contextExport);
        int busJ = getTerminalBusI(t2w.getTerminal2(), contextExport);
        Complex ysh = admittanceToPerUnit(new Complex(getG(t2w), getB(t2w)), t2w.getTerminal2().getVoltageLevel().getNominalV(), perUnitContext.sBase());

        psseTransformer.setI(busI);
        psseTransformer.setJ(busJ);
        psseTransformer.setK(0);
        psseTransformer.setCkt(contextExport.getEquipmentCkt(t2w.getId(), IdentifiableType.TWO_WINDINGS_TRANSFORMER, busI, busJ));
        psseTransformer.setCw(1);
        psseTransformer.setCz(1);
        psseTransformer.setCm(1);
        psseTransformer.setMag1(ysh.getReal());
        psseTransformer.setMag2(ysh.getImaginary());
        psseTransformer.setNmetr(2);
        psseTransformer.setName(t2w.getNameOrId().substring(0, Math.min(40, t2w.getNameOrId().length())));
        psseTransformer.setStat(getStatus(t2w));
        psseTransformer.setOwnership(findDefaultOwnership());
        psseTransformer.setVecgrp("            ");
        psseTransformer.setZcod(0);

        Complex z = impedanceToPerUnit(new Complex(getR(t2w), getX(t2w)), t2w.getTerminal2().getVoltageLevel().getNominalV(), perUnitContext.sBase());
        PsseTransformer.TransformerImpedances impedances = new PsseTransformer.TransformerImpedances();
        psseTransformer.setImpedances(impedances);
        psseTransformer.setR12(z.getReal());
        psseTransformer.setX12(z.getImaginary());
        psseTransformer.setSbase12(perUnitContext.sBase());
        psseTransformer.setR23(0.0);
        psseTransformer.setX23(0.0);
        psseTransformer.setSbase23(0.0);
        psseTransformer.setR31(0.0);
        psseTransformer.setX31(0.0);
        psseTransformer.setSbase31(0.0);
        psseTransformer.setVmstar(0.0);
        psseTransformer.setAnstar(0.0);

        psseTransformer.setWinding1(findWinding(t2w, contextExport), findRates(t2w));

        PsseTransformerWinding winding2 = new PsseTransformerWinding();
        winding2.setWindv(1.0);
        winding2.setNomv(0.0);
        psseTransformer.setWinding2(winding2, new PsseRates());
        psseTransformer.setWinding3(new PsseTransformerWinding(), new PsseRates());

        return psseTransformer;
    }

    private static PsseTransformerWinding findWinding(TwoWindingsTransformer t2w, ContextExport contextExport) {
        PsseTransformerWinding winding = new PsseTransformerWinding();
        RatioR ratioR = findRatioData(t2w.getRatioTapChanger(), t2w.getPhaseTapChanger(), getRatio0(t2w), contextExport);
        winding.setWindv(ratioR.windv);
        winding.setNomv(0.0);
        winding.setAng(ratioR.ang);
        winding.setCod(ratioR.cod);
        winding.setCont(ratioR.cont);
        winding.setNode(ratioR.node);
        winding.setRma(ratioR.rma);
        winding.setRmi(ratioR.rmi);
        winding.setVma(1.1);
        winding.setVmi(0.9);
        winding.setNtp(ratioR.ntp);
        winding.setTab(0);
        winding.setCr(0.0);
        winding.setCx(0.0);
        winding.setCnxa(0.0);

        return winding;
    }

    private static double getRatio0(TwoWindingsTransformer t2w) {
        return t2w.getRatedU1() / t2w.getRatedU2();
    }

    private static double getR(TwoWindingsTransformer t2w) {
        return getValue(t2w.getR(),
                t2w.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getR()).orElse(0d),
                t2w.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getR()).orElse(0d));
    }

    private static double getX(TwoWindingsTransformer t2w) {
        return getValue(t2w.getX(),
                t2w.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getX()).orElse(0d),
                t2w.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getX()).orElse(0d));
    }

    private static double getG(TwoWindingsTransformer t2w) {
        return getValue(t2w.getG(),
                t2w.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getG()).orElse(0d),
                t2w.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getG()).orElse(0d));
    }

    private static double getB(TwoWindingsTransformer t2w) {
        return getValue(t2w.getB(),
                t2w.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getB()).orElse(0d),
                t2w.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getB()).orElse(0d));
    }

    private static PsseRates findRates(TwoWindingsTransformer t2w) {
        PsseRates windingRates = findDefaultWindingRates();
        if (t2w.getApparentPowerLimits1().isPresent()) {
            setSortedRatesToPsseRates(getSortedRates(t2w.getApparentPowerLimits1().get()), windingRates);
        } else if (t2w.getApparentPowerLimits2().isPresent()) {
            setSortedRatesToPsseRates(getSortedRates(t2w.getApparentPowerLimits2().get()), windingRates);
        } else if (t2w.getCurrentLimits1().isPresent()) {
            setSortedRatesToPsseRates(getSortedRates(t2w.getCurrentLimits1().get(), t2w.getTerminal1().getVoltageLevel().getNominalV()), windingRates);
        } else if (t2w.getCurrentLimits2().isPresent()) {
            setSortedRatesToPsseRates(getSortedRates(t2w.getCurrentLimits2().get(), t2w.getTerminal2().getVoltageLevel().getNominalV()), windingRates);
        } else if (t2w.getActivePowerLimits1().isPresent()) {
            setSortedRatesToPsseRates(getSortedRates(t2w.getActivePowerLimits1().get()), windingRates);
        } else if (t2w.getActivePowerLimits2().isPresent()) {
            setSortedRatesToPsseRates(getSortedRates(t2w.getActivePowerLimits2().get()), windingRates);
        }
        return windingRates;
    }

    private static Terminal findRegulatingTerminal(TwoWindingsTransformer t2w, int cod) {
        if (isActivePowerControl(cod)) {
            return t2w.getOptionalPhaseTapChanger().map(PhaseTapChanger::getRegulationTerminal).orElse(null);
        }
        return t2w.getOptionalRatioTapChanger().map(RatioTapChanger::getRegulationTerminal).orElse(null);
    }

    private static int getStatus(TwoWindingsTransformer t2w) {
        if (t2w.getTerminal1().isConnected() && t2w.getTerminal1().getBusBreakerView().getBus() != null
                && t2w.getTerminal2().isConnected() && t2w.getTerminal2().getBusBreakerView().getBus() != null) {
            return 1;
        } else {
            return 0;
        }
    }

    private static void updateThreeWindingsTransformer(ThreeWindingsTransformer t3w, PsseTransformer psseTransformer, ContextExport contextExport) {
        double baskv1 = t3w.getLeg1().getTerminal().getVoltageLevel().getNominalV();
        double nomV1 = getNomV(psseTransformer.getWinding1(), t3w.getLeg1().getTerminal().getVoltageLevel());
        double baskv2 = t3w.getLeg2().getTerminal().getVoltageLevel().getNominalV();
        double nomV2 = getNomV(psseTransformer.getWinding2(), t3w.getLeg2().getTerminal().getVoltageLevel());
        double baskv3 = t3w.getLeg3().getTerminal().getVoltageLevel().getNominalV();
        double nomV3 = getNomV(psseTransformer.getWinding3(), t3w.getLeg3().getTerminal().getVoltageLevel());

        int busI = getTerminalBusI(t3w.getLeg1().getTerminal(), contextExport);
        int busJ = getTerminalBusI(t3w.getLeg2().getTerminal(), contextExport);
        int busK = getTerminalBusI(t3w.getLeg3().getTerminal(), contextExport);

        int regulatingBus1 = getRegulatingTerminalBusI(findRegulatingTerminal(t3w.getLeg1(), psseTransformer.getWinding1().getCod()), contextExport);
        int regulatingBus2 = getRegulatingTerminalBusI(findRegulatingTerminal(t3w.getLeg2(), psseTransformer.getWinding2().getCod()), contextExport);
        int regulatingBus3 = getRegulatingTerminalBusI(findRegulatingTerminal(t3w.getLeg3(), psseTransformer.getWinding3().getCod()), contextExport);

        psseTransformer.getWinding1().setWindv(defineWindV(getRatio(t3w.getLeg1().getRatioTapChanger(), t3w.getLeg1().getPhaseTapChanger()), baskv1, nomV1, psseTransformer.getCw()));
        psseTransformer.getWinding1().setAng(getAngle(t3w.getLeg1().getPhaseTapChanger()));
        psseTransformer.getWinding2().setWindv(defineWindV(getRatio(t3w.getLeg2().getRatioTapChanger(), t3w.getLeg2().getPhaseTapChanger()), baskv2, nomV2, psseTransformer.getCw()));
        psseTransformer.getWinding2().setAng(getAngle(t3w.getLeg2().getPhaseTapChanger()));
        psseTransformer.getWinding3().setWindv(defineWindV(getRatio(t3w.getLeg3().getRatioTapChanger(), t3w.getLeg3().getPhaseTapChanger()), baskv3, nomV3, psseTransformer.getCw()));
        psseTransformer.getWinding3().setAng(getAngle(t3w.getLeg3().getPhaseTapChanger()));

        psseTransformer.setI(busI);
        psseTransformer.setJ(busJ);
        psseTransformer.setK(busK);

        psseTransformer.getWinding1().setCont(regulatingBus1);
        psseTransformer.getWinding2().setCont(regulatingBus2);
        psseTransformer.getWinding3().setCont(regulatingBus3);

        psseTransformer.setStat(getStatus(t3w));
    }

    private static PsseTransformer createThreeWindingsTransformer(ThreeWindingsTransformer t3w, ContextExport contextExport, PsseExporter.PerUnitContext perUnitContext) {
        PsseTransformer psseTransformer = new PsseTransformer();

        int busI = getTerminalBusI(t3w.getLeg1().getTerminal(), contextExport);
        int busJ = getTerminalBusI(t3w.getLeg2().getTerminal(), contextExport);
        int busK = getTerminalBusI(t3w.getLeg3().getTerminal(), contextExport);
        Complex ysh = admittanceToPerUnit(new Complex(getG(t3w.getLeg1()), getB(t3w.getLeg1())), t3w.getRatedU0(), perUnitContext.sBase());

        psseTransformer.setI(busI);
        psseTransformer.setJ(busJ);
        psseTransformer.setK(busK);
        psseTransformer.setCkt(contextExport.getEquipmentCkt(t3w.getId(), IdentifiableType.THREE_WINDINGS_TRANSFORMER, busI, busJ, busK));
        psseTransformer.setCw(1);
        psseTransformer.setCz(1);
        psseTransformer.setCm(1);
        psseTransformer.setMag1(ysh.getReal());
        psseTransformer.setMag2(ysh.getImaginary());
        psseTransformer.setNmetr(2);
        psseTransformer.setName(t3w.getNameOrId().substring(0, Math.min(40, t3w.getNameOrId().length())));
        psseTransformer.setStat(getStatus(t3w));
        psseTransformer.setOwnership(findDefaultOwnership());
        psseTransformer.setVecgrp("            ");
        psseTransformer.setZcod(0);

        Complex z1 = new Complex(getR(t3w.getLeg1()), getX(t3w.getLeg1()));
        Complex z2 = new Complex(getR(t3w.getLeg2()), getX(t3w.getLeg2()));
        Complex z3 = new Complex(getR(t3w.getLeg3()), getX(t3w.getLeg3()));

        Complex z12 = impedanceToPerUnit(z1.add(z2), t3w.getRatedU0(), perUnitContext.sBase());
        Complex z23 = impedanceToPerUnit(z2.add(z3), t3w.getRatedU0(), perUnitContext.sBase());
        Complex z31 = impedanceToPerUnit(z3.add(z1), t3w.getRatedU0(), perUnitContext.sBase());
        PsseTransformer.TransformerImpedances impedances = new PsseTransformer.TransformerImpedances();
        psseTransformer.setImpedances(impedances);
        psseTransformer.setR12(z12.getReal());
        psseTransformer.setX12(z12.getImaginary());
        psseTransformer.setSbase12(perUnitContext.sBase());
        psseTransformer.setR23(z23.getReal());
        psseTransformer.setX23(z23.getImaginary());
        psseTransformer.setSbase23(perUnitContext.sBase());
        psseTransformer.setR31(z31.getReal());
        psseTransformer.setX31(z31.getImaginary());
        psseTransformer.setSbase31(perUnitContext.sBase());
        psseTransformer.setVmstar(0.0);
        psseTransformer.setAnstar(0.0);

        psseTransformer.setOwnership(findDefaultOwnership());

        psseTransformer.setWinding1(findWinding(t3w.getLeg1(), t3w.getRatedU0(), contextExport), findRates(t3w.getLeg1()));
        psseTransformer.setWinding2(findWinding(t3w.getLeg2(), t3w.getRatedU0(), contextExport), findRates(t3w.getLeg2()));
        psseTransformer.setWinding3(findWinding(t3w.getLeg3(), t3w.getRatedU0(), contextExport), findRates(t3w.getLeg3()));

        return psseTransformer;
    }

    private static Terminal findRegulatingTerminal(Leg leg, int cod) {
        if (isActivePowerControl(cod)) {
            return leg.getOptionalPhaseTapChanger().map(PhaseTapChanger::getRegulationTerminal).orElse(null);
        }
        return leg.getOptionalRatioTapChanger().map(RatioTapChanger::getRegulationTerminal).orElse(null);
    }

    private static int getStatus(ThreeWindingsTransformer t3w) {
        if (t3w.getLeg1().getTerminal().isConnected() && t3w.getLeg1().getTerminal().getBusBreakerView().getBus() != null
                && t3w.getLeg2().getTerminal().isConnected() && t3w.getLeg2().getTerminal().getBusBreakerView().getBus() != null
                && t3w.getLeg3().getTerminal().isConnected() && t3w.getLeg3().getTerminal().getBusBreakerView().getBus() != null) {
            return 1;
        } else if (t3w.getLeg1().getTerminal().isConnected() && t3w.getLeg1().getTerminal().getBusBreakerView().getBus() != null
                && t3w.getLeg2().getTerminal().isConnected() && t3w.getLeg2().getTerminal().getBusBreakerView().getBus() != null) {
            return 3;
        } else if (t3w.getLeg1().getTerminal().isConnected() && t3w.getLeg1().getTerminal().getBusBreakerView().getBus() != null
                && t3w.getLeg3().getTerminal().isConnected() && t3w.getLeg3().getTerminal().getBusBreakerView().getBus() != null) {
            return 2;
        } else if (t3w.getLeg2().getTerminal().isConnected() && t3w.getLeg2().getTerminal().getBusBreakerView().getBus() != null
                && t3w.getLeg3().getTerminal().isConnected() && t3w.getLeg3().getTerminal().getBusBreakerView().getBus() != null) {
            return 4;
        } else {
            return 0;
        }
    }

    private static PsseTransformerWinding findWinding(Leg leg, double ratedU0, ContextExport contextExport) {
        PsseTransformerWinding winding = new PsseTransformerWinding();
        RatioR ratioR = findRatioData(leg.getRatioTapChanger(), leg.getPhaseTapChanger(), getRatio0(leg, ratedU0), contextExport);
        winding.setWindv(ratioR.windv);
        winding.setNomv(0.0);
        winding.setAng(ratioR.ang);
        winding.setCod(ratioR.cod);
        winding.setCont(ratioR.cont);
        winding.setNode(ratioR.node);
        winding.setRma(ratioR.rma);
        winding.setRmi(ratioR.rmi);
        winding.setVma(1.1);
        winding.setVmi(0.9);
        winding.setNtp(ratioR.ntp);
        winding.setTab(0);
        winding.setCr(0.0);
        winding.setCx(0.0);
        winding.setCnxa(0.0);

        return winding;
    }

    private static double getRatio0(Leg leg, double ratedU0) {
        return leg.getRatedU() / ratedU0;
    }

    private static double getR(Leg leg) {
        return getValue(leg.getR(),
                leg.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getR()).orElse(0d),
                leg.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getR()).orElse(0d));
    }

    private static double getX(Leg leg) {
        return getValue(leg.getX(),
                leg.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getX()).orElse(0d),
                leg.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getX()).orElse(0d));
    }

    private static double getG(Leg leg) {
        return getValue(leg.getG(),
                leg.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getG()).orElse(0d),
                leg.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getG()).orElse(0d));
    }

    private static double getB(Leg leg) {
        return getValue(leg.getB(),
                leg.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getB()).orElse(0d),
                leg.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getB()).orElse(0d));
    }

    private static double getValue(double initialValue, double rtcStepValue, double ptcStepValue) {
        return initialValue * (1 + rtcStepValue / 100) * (1 + ptcStepValue / 100);
    }

    private static double getRatio(RatioTapChanger rtc, PhaseTapChanger ptc) {
        if (rtc != null && ptc != null) {
            return getRatio(rtc) * getRatio(ptc);
        } else if (rtc != null) {
            return getRatio(rtc);
        } else if (ptc != null) {
            return getRatio(ptc);
        } else {
            return 1.0;
        }
    }

    private static PsseRates findRates(Leg leg) {
        PsseRates windingRates = findDefaultWindingRates();
        if (leg.getApparentPowerLimits().isPresent()) {
            setSortedRatesToPsseRates(getSortedRates(leg.getApparentPowerLimits().get()), windingRates);
        } else if (leg.getCurrentLimits().isPresent()) {
            setSortedRatesToPsseRates(getSortedRates(leg.getCurrentLimits().get(), leg.getTerminal().getVoltageLevel().getNominalV()), windingRates);
        } else if (leg.getActivePowerLimits().isPresent()) {
            setSortedRatesToPsseRates(getSortedRates(leg.getActivePowerLimits().get()), windingRates);
        }
        return windingRates;
    }

    private static RatioR findRatioData(RatioTapChanger rtc, PhaseTapChanger ptc, double a0, ContextExport contextExport) {
        if (rtc != null && ptc != null) {
            if (ptc.isRegulating() && !rtc.isRegulating()) {
                return new RatioR(getSteps(ptc) > 1 ? 3 : 0, a0 * getRatio(rtc) * getRatio(ptc), getAngle(ptc),
                        getRegulatingTerminalBusI(ptc.getRegulationTerminal(), contextExport),
                        getRegulatingTerminalNode(ptc.getRegulationTerminal(), contextExport),
                        getMaxAngle(ptc), getMinAngle(ptc), getSteps(ptc));
            } else {
                return new RatioR(getSteps(rtc) > 1 ? 1 : 0, a0 * getRatio(rtc) * getRatio(ptc), getAngle(ptc),
                        getRegulatingTerminalBusI(rtc.getRegulationTerminal(), contextExport),
                        getRegulatingTerminalNode(rtc.getRegulationTerminal(), contextExport),
                        getMaxRatio(rtc) * getRatio(ptc), getMinRatio(rtc) * getRatio(ptc), getSteps(rtc));
            }
        } else if (rtc != null) {
            return new RatioR(getSteps(rtc) > 1 ? 1 : 0, a0 * getRatio(rtc), 0.0,
                    getRegulatingTerminalBusI(rtc.getRegulationTerminal(), contextExport),
                    getRegulatingTerminalNode(rtc.getRegulationTerminal(), contextExport),
                    getMaxRatio(rtc), getMinRatio(rtc), getSteps(rtc));
        } else if (ptc != null) {
            return new RatioR(getSteps(ptc) > 1 ? 3 : 0, a0 * getRatio(ptc), getAngle(ptc),
                    getRegulatingTerminalBusI(ptc.getRegulationTerminal(), contextExport),
                    getRegulatingTerminalNode(ptc.getRegulationTerminal(), contextExport),
                    getMaxAngle(ptc), getMinAngle(ptc), getSteps(ptc));
        } else {
            return new RatioR(0, a0, 0.0, 0, 0, 1.1, 0.9, 33);
        }
    }

    private static double getRatio(RatioTapChanger rtc) {
        return rtc != null ? 1.0 / rtc.getCurrentStep().getRho() : 1.0;
    }

    private static double getMinRatio(RatioTapChanger rtc) {
        double firstRatio = 1.0 / rtc.getStep(rtc.getLowTapPosition()).getRho();
        double lastRatio = 1.0 / rtc.getStep(rtc.getHighTapPosition()).getRho();
        return Math.min(firstRatio, lastRatio);
    }

    private static double getMaxRatio(RatioTapChanger rtc) {
        double firstRatio = 1.0 / rtc.getStep(rtc.getLowTapPosition()).getRho();
        double lastRatio = 1.0 / rtc.getStep(rtc.getHighTapPosition()).getRho();
        return Math.max(firstRatio, lastRatio);
    }

    private static int getSteps(RatioTapChanger rtc) {
        int steps = rtc.getHighTapPosition() - rtc.getLowTapPosition();
        return rtc.getLowTapPosition() < 0 ? steps : steps + 1;
    }

    private static double getRatio(PhaseTapChanger ptc) {
        return ptc != null ? 1.0 / ptc.getCurrentStep().getRho() : 1.0;
    }

    private static double getAngle(PhaseTapChanger ptc) {
        return ptc != null ? convertToAngle(ptc.getCurrentStep().getAlpha()) : 0.0;
    }

    private static double getMinAngle(PhaseTapChanger ptc) {
        double firstAngle = convertToAngle(ptc.getStep(ptc.getLowTapPosition()).getAlpha());
        double lastAngle = convertToAngle(ptc.getStep(ptc.getHighTapPosition()).getAlpha());
        return Math.min(firstAngle, lastAngle);
    }

    private static double getMaxAngle(PhaseTapChanger ptc) {
        double firstAngle = convertToAngle(ptc.getStep(ptc.getLowTapPosition()).getAlpha());
        double lastAngle = convertToAngle(ptc.getStep(ptc.getHighTapPosition()).getAlpha());
        return Math.max(firstAngle, lastAngle);
    }

    private static int getSteps(PhaseTapChanger ptc) {
        int steps = ptc.getHighTapPosition() - ptc.getLowTapPosition();
        return ptc.getLowTapPosition() < 0 ? steps : steps + 1;
    }

    private static double convertToAngle(double alpha) {
        return alpha != 0.0 ? -alpha : alpha; // To avoid - 0.0
    }

    private record RatioR(int cod, double windv, double ang, int cont, int node, double rma, double rmi, int ntp) {
    }

    private static PsseOwnership findDefaultOwnership() {
        PsseOwnership psseOwnership = new PsseOwnership();
        psseOwnership.setO1(1);
        psseOwnership.setF1(1.0);
        psseOwnership.setO2(0);
        psseOwnership.setF2(0.0);
        psseOwnership.setO3(0);
        psseOwnership.setF3(0.0);
        psseOwnership.setO4(0);
        psseOwnership.setF4(0.0);
        return psseOwnership;
    }

    private static PsseRates findDefaultWindingRates() {
        PsseRates windingRates = new PsseRates();
        windingRates.setRate1(0.0);
        windingRates.setRate2(0.0);
        windingRates.setRate3(0.0);
        windingRates.setRate4(0.0);
        windingRates.setRate5(0.0);
        windingRates.setRate6(0.0);
        windingRates.setRate7(0.0);
        windingRates.setRate8(0.0);
        windingRates.setRate9(0.0);
        windingRates.setRate10(0.0);
        windingRates.setRate11(0.0);
        windingRates.setRate12(0.0);
        return windingRates;
    }

    private final PsseTransformer psseTransformer;
    private final Map<Integer, PsseBus> busNumToPsseBus;
    private final double sbase;
    private final PerUnitContext perUnitContext;
    private final PsseVersion version;
    private final NodeBreakerImport nodeBreakerImport;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerConverter.class);
}
