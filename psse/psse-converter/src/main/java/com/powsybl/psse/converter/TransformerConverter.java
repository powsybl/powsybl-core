/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.apache.commons.math3.complex.Complex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.converter.PsseImporter.PerUnitContext;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import com.powsybl.psse.model.pf.PsseTransformer;
import com.powsybl.psse.model.pf.PsseTransformerWinding;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TransformerConverter extends AbstractConverter {

    private static final double TOLERANCE = 0.00001;

    TransformerConverter(PsseTransformer psseTransformer, ContainersMapping containersMapping,
        PerUnitContext perUnitContext, Network network, Map<Integer, PsseBus> busNumToPsseBus, double sbase,
        PsseVersion version) {
        super(containersMapping, network);
        this.psseTransformer = Objects.requireNonNull(psseTransformer);
        this.busNumToPsseBus = Objects.requireNonNull(busNumToPsseBus);
        this.sbase = sbase;
        this.perUnitContext = Objects.requireNonNull(perUnitContext);
        this.version = Objects.requireNonNull(version);
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
        z = impedanceToEngineeringUnits(z, voltageLevel2.getNominalV(), perUnitContext.getSb());
        ysh = admittanceToEngineeringUnits(ysh, voltageLevel2.getNominalV(), perUnitContext.getSb());

        // move w2 to side 1
        z = impedanceAdjustmentAfterMovingRatio(z, w2);
        TapChanger tapChangerAdjustedRatio = tapChangerAdjustmentAfterMovingRatio(tapChanger, w2);

        // move ysh between w1 and z
        // Ysh_eu = Ysh_pu * sbase / (vn1 *vn1) Convert Ysh per unit to engineering units
        // Ysh_eu_moved = Ysh_eu * (ratedU1 / ratedU2) * (ratedU1 / ratedU2) Apply the structural ratio when moving
        // Ysh_eu_moved = Ysh_eu * sbase / (vn2 * vn2) as ratedU1 = vn1 and ratedU2 ) vn2
        // As vn2 is used to convert to eu, only the ratio remains to be applied
        TapChanger tapChangerAdjustedYsh = tapChangerAdjustmentAfterMovingShuntAdmittanceBetweenRatioAndTransmissionImpedance(tapChangerAdjustedRatio);

        TwoWindingsTransformerAdder adder = voltageLevel2.getSubstation()
            .orElseThrow(() -> new PowsyblException("Substation null! Transformer must be within a substation"))
            .newTwoWindingsTransformer()
            .setId(id)
            .setEnsureIdUnicity(true)
            .setConnectableBus1(bus1Id)
            .setVoltageLevel1(voltageLevel1Id)
            .setConnectableBus2(bus2Id)
            .setVoltageLevel2(voltageLevel2Id)
            .setRatedU1(voltageLevel1.getNominalV())
            .setRatedU2(voltageLevel2.getNominalV())
            .setR(z.getReal())
            .setX(z.getImaginary())
            .setG(ysh.getReal())
            .setB(ysh.getImaginary());

        adder.setBus1(psseTransformer.getStat() == 1 ? bus1Id : null);
        adder.setBus2(psseTransformer.getStat() == 1 ? bus2Id : null);
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
        z1 = impedanceToEngineeringUnits(z1, v0, perUnitContext.getSb());
        z2 = impedanceToEngineeringUnits(z2, v0, perUnitContext.getSb());
        z3 = impedanceToEngineeringUnits(z3, v0, perUnitContext.getSb());
        ysh = admittanceToEngineeringUnits(ysh, v0, perUnitContext.getSb());

        // move ysh between w1 and z
        TapChanger tapChanger1AdjustedYsh = tapChangerAdjustmentAfterMovingShuntAdmittanceBetweenRatioAndTransmissionImpedance(tapChanger1);

        ThreeWindingsTransformerAdder adder = voltageLevel1.getSubstation()
            .orElseThrow(() -> new PowsyblException("Substation null! Transformer must be within a substation"))
            .newThreeWindingsTransformer()
            .setRatedU0(v0)
            .setEnsureIdUnicity(true)
            .setId(id)
            .newLeg1()
            .setR(z1.getReal())
            .setX(z1.getImaginary())
            .setG(ysh.getReal())
            .setB(ysh.getImaginary())
            .setRatedU(voltageLevel1.getNominalV())
            .setConnectableBus(bus1Id)
            .setVoltageLevel(voltageLevel1Id)
            .setBus(leg1IsConnected() ? bus1Id : null)
            .add()
            .newLeg2()
            .setR(z2.getReal())
            .setX(z2.getImaginary())
            .setG(0)
            .setB(0)
            .setRatedU(voltageLevel2.getNominalV())
            .setConnectableBus(bus2Id)
            .setVoltageLevel(voltageLevel2Id)
            .setBus(leg2IsConnected() ? bus2Id : null)
            .add()
            .newLeg3()
            .setR(z3.getReal())
            .setX(z3.getImaginary())
            .setG(0)
            .setB(0)
            .setRatedU(voltageLevel3.getNominalV())
            .setConnectableBus(bus3Id)
            .setVoltageLevel(voltageLevel3Id)
            .setBus(leg3IsConnected() ? bus3Id : null)
            .add();

        ThreeWindingsTransformer twt = adder.add();

        twt.setProperty("v", Double.toString(psseTransformer.getVmstar() * v0));
        twt.setProperty("angle", Double.toString(psseTransformer.getAnstar()));

        tapChangersToIidm(tapChanger1AdjustedYsh, tapChanger2, tapChanger3, twt);
        defineOperationalLimits(twt, voltageLevel1.getNominalV(), voltageLevel2.getNominalV(), voltageLevel3.getNominalV());
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
        if (complexRatio.getAngle() == 0.0 && (cod == 1 || cod == 2)) {
            double stepRatioIncrement = (rma - rmi) / (ntp - 1);
            for (int i = 0; i < ntp; i++) {
                double ratio = defineRatio(rmi + stepRatioIncrement * i, baskv, nomv, cw);
                tapChanger.getSteps().add(new TapChangerStep(ratio, complexRatio.getAngle()));
            }
            return tapChanger;
        } else if (cod == 3) { // PhaseTapChanger
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
        TwoWindingsTransformer twt = getNetwork().getTwoWindingsTransformer(id);
        if (twt == null) {
            return;
        }
        boolean regulatingForcedToOff = false;
        if (twt.hasRatioTapChanger()) {
            boolean regulating = defineVoltageControl(getNetwork(), twt.getId(), psseTransformer.getWinding1(), twt.getRatioTapChanger(), regulatingForcedToOff);
            regulatingForcedToOff = forceRegulatingToOff(regulatingForcedToOff, regulating);
        }
        if (twt.hasPhaseTapChanger()) {
            defineActivePowerControl(getNetwork(), twt.getId(), psseTransformer.getWinding1(), twt.getPhaseTapChanger(), regulatingForcedToOff);
        }
    }

    private void addControlThreeWindingsTransformer() {
        String id = getTransformerId(psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getK(), psseTransformer.getCkt());
        ThreeWindingsTransformer twt = getNetwork().getThreeWindingsTransformer(id);
        if (twt == null) {
            return;
        }
        boolean regulatingForcedToOff = false;
        regulatingForcedToOff = addControlThreeWindingsTransformerLeg(getNetwork(), twt.getId(), twt.getLeg1(), psseTransformer.getWinding1(), regulatingForcedToOff);
        regulatingForcedToOff = addControlThreeWindingsTransformerLeg(getNetwork(), twt.getId(), twt.getLeg2(), psseTransformer.getWinding2(), regulatingForcedToOff);
        addControlThreeWindingsTransformerLeg(getNetwork(), twt.getId(), twt.getLeg3(), psseTransformer.getWinding3(), regulatingForcedToOff);
    }

    private static boolean addControlThreeWindingsTransformerLeg(Network network, String id, Leg leg,
        PsseTransformerWinding winding, boolean regulatingForcedToOffInput) {
        boolean regulatingForcedToOff = regulatingForcedToOffInput;
        if (leg.hasRatioTapChanger()) {
            boolean regulating = defineVoltageControl(network, id, winding, leg.getRatioTapChanger(), regulatingForcedToOff);
            regulatingForcedToOff = forceRegulatingToOff(regulatingForcedToOff, regulating);
        }
        if (leg.hasPhaseTapChanger()) {
            boolean regulating = defineActivePowerControl(network, id, winding, leg.getPhaseTapChanger(), regulatingForcedToOff);
            regulatingForcedToOff = forceRegulatingToOff(regulatingForcedToOff, regulating);
        }
        return regulatingForcedToOff;
    }

    private static boolean defineVoltageControl(Network network, String id, PsseTransformerWinding winding, RatioTapChanger rtc,
        boolean regulatingForcedToOff) {
        if (Math.abs(winding.getCod()) == 2) {
            LOGGER.warn("Transformer {}. Reactive power control not supported", id);
            return false;
        }
        if (Math.abs(winding.getCod()) != 1) {
            return false;
        }

        Terminal regulatingTerminal = defineRegulatingTerminal(network, id, winding);
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

    private static boolean defineActivePowerControl(Network network, String id, PsseTransformerWinding winding, PhaseTapChanger ptc, boolean regulatingForcedToOff) {
        if (Math.abs(winding.getCod()) != 3) {
            return false;
        }

        Terminal regulatingTerminal = defineRegulatingTerminal(network, id, winding);
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

    private static Terminal defineRegulatingTerminal(Network network, String id, PsseTransformerWinding winding) {
        Terminal regulatingTerminal = null;

        String regulatingBusId = getBusId(Math.abs(winding.getCont()));
        Bus bus = network.getBusBreakerView().getBus(regulatingBusId);
        if (bus != null) {
            regulatingTerminal = bus.getConnectedTerminalStream().findFirst().orElse(null);
        }
        if (regulatingTerminal == null) {
            LOGGER.warn("Transformer {}. Regulating terminal is not assigned as the bus is isolated", id);
        }
        return regulatingTerminal;
    }

    private static String getTransformerId(int i, int j, String ckt) {
        return "T-" + i + "-" + j + "-" + ckt;
    }

    private static String getTransformerId(int i, int j, int k, String ckt) {
        return "T-" + i + "-" + j + "-" + k + "-" + ckt;
    }

    // At the moment we do not consider new transformers and antenna twoWindingsTransformers are exported as open
    static void updateTransformers(Network network, PssePowerFlowModel psseModel) {
        psseModel.getTransformers().forEach(psseTransformer -> {
            if (isTwoWindingsTransformer(psseTransformer)) {
                updateTwoWindingsTransformer(network, psseTransformer);
            } else {
                updateThreeWindingsTransformer(network, psseTransformer);
            }
        });
    }

    private static void updateTwoWindingsTransformer(Network network, PsseTransformer psseTransformer) {
        String transformerId = getTransformerId(psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getCkt());
        TwoWindingsTransformer tw2t = network.getTwoWindingsTransformer(transformerId);
        if (tw2t == null) {
            psseTransformer.setStat(0);
        } else {
            double baskv1 = tw2t.getTerminal1().getVoltageLevel().getNominalV();
            double nomV1 = getNomV(psseTransformer.getWinding1(), tw2t.getTerminal1().getVoltageLevel());
            psseTransformer.getWinding1().setWindv(defineWindV(getRatio(tw2t.getRatioTapChanger(), tw2t.getPhaseTapChanger()), baskv1, nomV1, psseTransformer.getCw()));
            psseTransformer.getWinding1().setAng(getAngle(tw2t.getPhaseTapChanger()));

            psseTransformer.setStat(getStatus(tw2t));
        }
    }

    private static int getStatus(TwoWindingsTransformer tw2t) {
        if (tw2t.getTerminal1().isConnected() && tw2t.getTerminal2().isConnected()) {
            return 1;
        } else {
            return 0;
        }
    }

    private static void updateThreeWindingsTransformer(Network network, PsseTransformer psseTransformer) {
        String transformerId = getTransformerId(psseTransformer.getI(), psseTransformer.getJ(), psseTransformer.getK(), psseTransformer.getCkt());
        ThreeWindingsTransformer tw3t = network.getThreeWindingsTransformer(transformerId);
        if (tw3t == null) {
            psseTransformer.setStat(0);
        } else {
            double baskv1 = tw3t.getLeg1().getTerminal().getVoltageLevel().getNominalV();
            double nomV1 = getNomV(psseTransformer.getWinding1(), tw3t.getLeg1().getTerminal().getVoltageLevel());
            psseTransformer.getWinding1().setWindv(defineWindV(getRatio(tw3t.getLeg1().getRatioTapChanger(), tw3t.getLeg1().getPhaseTapChanger()), baskv1, nomV1, psseTransformer.getCw()));
            psseTransformer.getWinding1().setAng(getAngle(tw3t.getLeg1().getPhaseTapChanger()));

            double baskv2 = tw3t.getLeg2().getTerminal().getVoltageLevel().getNominalV();
            double nomV2 = getNomV(psseTransformer.getWinding2(), tw3t.getLeg2().getTerminal().getVoltageLevel());
            psseTransformer.getWinding2().setWindv(defineWindV(getRatio(tw3t.getLeg2().getRatioTapChanger(), tw3t.getLeg2().getPhaseTapChanger()), baskv2, nomV2, psseTransformer.getCw()));
            psseTransformer.getWinding2().setAng(getAngle(tw3t.getLeg2().getPhaseTapChanger()));

            double baskv3 = tw3t.getLeg3().getTerminal().getVoltageLevel().getNominalV();
            double nomV3 = getNomV(psseTransformer.getWinding3(), tw3t.getLeg3().getTerminal().getVoltageLevel());
            psseTransformer.getWinding3().setWindv(defineWindV(getRatio(tw3t.getLeg3().getRatioTapChanger(), tw3t.getLeg3().getPhaseTapChanger()), baskv3, nomV3, psseTransformer.getCw()));
            psseTransformer.getWinding3().setAng(getAngle(tw3t.getLeg3().getPhaseTapChanger()));

            psseTransformer.setStat(getStatus(tw3t));
        }
    }

    private static int getStatus(ThreeWindingsTransformer tw3t) {
        if (tw3t.getLeg1().getTerminal().isConnected() && tw3t.getLeg2().getTerminal().isConnected()
            && tw3t.getLeg3().getTerminal().isConnected()) {
            return 1;
        } else if (tw3t.getLeg1().getTerminal().isConnected() && tw3t.getLeg2().getTerminal().isConnected()) {
            return 3;
        } else if (tw3t.getLeg1().getTerminal().isConnected() && tw3t.getLeg3().getTerminal().isConnected()) {
            return 2;
        } else if (tw3t.getLeg2().getTerminal().isConnected() && tw3t.getLeg3().getTerminal().isConnected()) {
            return 4;
        } else {
            return 0;
        }
    }

    private static double getRatio(RatioTapChanger rtc, PhaseTapChanger ptc) {
        double rho = 1.0;
        if (rtc != null) {
            rho = rho * rtc.getCurrentStep().getRho();
        }
        if (ptc != null) {
            rho = rho * ptc.getCurrentStep().getRho();
        }
        return 1.0 / rho;
    }

    private static double getAngle(PhaseTapChanger ptc) {
        double alpha = 0.0;
        if (ptc != null) {
            alpha = ptc.getCurrentStep().getAlpha();
        }
        // To avoid - 0.0
        if (alpha != 0.0) {
            return -alpha;
        }
        return alpha;
    }

    private final PsseTransformer psseTransformer;
    private final Map<Integer, PsseBus> busNumToPsseBus;
    private final double sbase;
    private final PerUnitContext perUnitContext;
    private final PsseVersion version;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerConverter.class);
}
