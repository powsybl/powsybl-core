/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClockAdder;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClockAdder;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.PowerFactoryException;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

class TransformerConverter extends AbstractConverter {

    private enum WindingType {
        HIGH, MEDIUM, LOW
    }

    TransformerConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    void createTwoWindings(DataObject elmTr2) {

        DataObject typTr2 = elmTr2.getObjectAttributeValue(DataAttributeNames.TYP_ID).resolve().orElseThrow();

        List<NodeRef> nodeRefs = checkNodes(elmTr2, 2);
        NodeRef nodeRef1 = nodeRefs.get(0);
        NodeRef nodeRef2 = nodeRefs.get(1);

        VoltageLevel vl1 = getNetwork().getVoltageLevel(nodeRef1.voltageLevelId);
        VoltageLevel vl2 = getNetwork().getVoltageLevel(nodeRef2.voltageLevelId);

        boolean highAtEnd1 = highVoltageAtEnd1(vl1, vl2);
        boolean tapChangerAtEnd1 = tapChangerAtEnd1(typTr2, highAtEnd1);

        RatedModel ratedModel = RatedModel.create(typTr2, highAtEnd1);
        double nominalVoltageEnd2 = vl2.getNominalV();
        TransformerModel transformerModel = TransformerModel.create(typTr2, ratedModel.ratedS, nominalVoltageEnd2);

        if (!tapChangerAtEnd1) {
            // Structural ratio at end2 = ratedU2 / vn2
            transformerModel.moveStructuralRatioFromEnd2ToEnd1(ratedModel.ratedU2 / vl2.getNominalV());
        }

        PowerFactoryTapChanger powerFactoryTapChanger = PowerFactoryTapChanger.create(elmTr2, typTr2);
        Optional<TapChangerModel> tc = TapChangerModel.create(powerFactoryTapChanger, tapChangerAtEnd1);

        Substation substation = vl1.getSubstation().orElseThrow();
        TwoWindingsTransformer t2wt = substation.newTwoWindingsTransformer()
            .setId(elmTr2.getLocName())
            .setEnsureIdUnicity(true)
            .setVoltageLevel1(nodeRef1.voltageLevelId)
            .setVoltageLevel2(nodeRef2.voltageLevelId)
            .setNode1(nodeRef1.node)
            .setNode2(nodeRef2.node)
            .setRatedU1(ratedModel.ratedU1)
            .setRatedU2(ratedModel.ratedU2)
            .setRatedS(ratedModel.ratedS)
            .setR(transformerModel.r)
            .setX(transformerModel.x)
            .setG(transformerModel.g)
            .setB(transformerModel.b)
            .add();

        tc.ifPresent(t -> tapChangerToIidm(t, t2wt));

        Optional<PhaseAngleClockModel> pacModel = PhaseAngleClockModel.create(typTr2);
        pacModel.ifPresent(phaseAngleClockModel -> t2wt.newExtension(TwoWindingsTransformerPhaseAngleClockAdder.class)
            .withPhaseAngleClock(phaseAngleClockModel.pac).add());
    }

    void createThreeWindings(DataObject elmTr3) {

        DataObject typTr3 = elmTr3.getObjectAttributeValue(DataAttributeNames.TYP_ID).resolve().orElseThrow();

        List<NodeRef> nodeRefs = checkNodes(elmTr3, 3);
        NodeRef nodeRef1 = nodeRefs.get(0);
        NodeRef nodeRef2 = nodeRefs.get(1);
        NodeRef nodeRef3 = nodeRefs.get(2);

        // The three connection buses of the transformer are defined in power factory
        // using the attribute busIndexIn: 0, 1, 2
        // But all the characteristics are given by winding type: high, medium or low
        // The order of busIndexIn may not always follow high, medium, low
        // So we need to map the busIndexIn to its winding type

        // Additionally, IIDM model will respect the order defined by busIndexIn
        // So IIDM Leg 1, 2, 3 will correspond to busIndexIn 0, 1, 2

        VoltageLevel vl1 = getNetwork().getVoltageLevel(nodeRef1.voltageLevelId);
        VoltageLevel vl2 = getNetwork().getVoltageLevel(nodeRef2.voltageLevelId);
        VoltageLevel vl3 = getNetwork().getVoltageLevel(nodeRef3.voltageLevelId);
        List<WindingType> windingTypeEnds = createWindingTypeEnds(vl1, vl2, vl3);

        double vn0 = 1.0;
        double ratedU0 = vn0;
        Rated3WModel rated3WModel = Rated3WModel.create(typTr3, ratedU0);
        RatedModel ratedModel1 = rated3WModel.getEnd(windingTypeEnds.get(0));
        RatedModel ratedModel2 = rated3WModel.getEnd(windingTypeEnds.get(1));
        RatedModel ratedModel3 = rated3WModel.getEnd(windingTypeEnds.get(2));
        double ratedU1 = ratedModel1.ratedU1;
        double ratedU2 = ratedModel2.ratedU1;
        double ratedU3 = ratedModel3.ratedU1;

        Transformer3WModel transformer3WModel = Transformer3WModel.create(typTr3, rated3WModel, vn0);
        TransformerModel transformerModel1 = transformer3WModel.getEnd(windingTypeEnds.get(0));
        TransformerModel transformerModel2 = transformer3WModel.getEnd(windingTypeEnds.get(1));
        TransformerModel transformerModel3 = transformer3WModel.getEnd(windingTypeEnds.get(2));

        Substation substation = vl1.getSubstation().orElseThrow();
        ThreeWindingsTransformerAdder adder = substation.newThreeWindingsTransformer()
            .setRatedU0(ratedU0)
            .setEnsureIdUnicity(true)
            .setId(elmTr3.getLocName())
            .newLeg1()
            .setR(transformerModel1.r)
            .setX(transformerModel1.x)
            .setG(transformerModel1.g)
            .setB(transformerModel1.b)
            .setRatedU(ratedU1)
            .setRatedS(ratedModel1.ratedS)
            .setVoltageLevel(nodeRef1.voltageLevelId)
            .setNode(nodeRef1.node)
            .add()
            .newLeg2()
            .setR(transformerModel2.r)
            .setX(transformerModel2.x)
            .setG(transformerModel2.g)
            .setB(transformerModel2.b)
            .setRatedU(ratedU2)
            .setRatedS(ratedModel2.ratedS)
            .setVoltageLevel(nodeRef2.voltageLevelId)
            .setNode(nodeRef2.node)
            .add()
            .newLeg3()
            .setR(transformerModel3.r)
            .setX(transformerModel3.x)
            .setG(transformerModel3.g)
            .setB(transformerModel3.b)
            .setRatedU(ratedU3)
            .setRatedS(ratedModel3.ratedS)
            .setVoltageLevel(nodeRef3.voltageLevelId)
            .setNode(nodeRef3.node)
            .add();

        ThreeWindingsTransformer t3wt = adder.add();

        PowerFactoryTapChangers3W powerFactoryTapChangers3W = PowerFactoryTapChangers3W.create(elmTr3, typTr3);
        TapChanger3W tapChanger3w = TapChanger3W.create(powerFactoryTapChangers3W);
        Optional<TapChangerModel> tc1 = tapChanger3w.getEnd(windingTypeEnds.get(0));
        Optional<TapChangerModel> tc2 = tapChanger3w.getEnd(windingTypeEnds.get(1));
        Optional<TapChangerModel> tc3 = tapChanger3w.getEnd(windingTypeEnds.get(2));

        tc1.ifPresent(tc -> tapChangerToIidm(tc, t3wt.getLeg1()));
        tc2.ifPresent(tc -> tapChangerToIidm(tc, t3wt.getLeg2()));
        tc3.ifPresent(tc -> tapChangerToIidm(tc, t3wt.getLeg3()));

        PhaseAngleClock3WModel pac3WModel = PhaseAngleClock3WModel.create(typTr3);
        Optional<PhaseAngleClockModel> pac2 = pac3WModel.getEnd(windingTypeEnds.get(1));
        Optional<PhaseAngleClockModel> pac3 = pac3WModel.getEnd(windingTypeEnds.get(2));
        if (pac2.isPresent() || pac3.isPresent()) {
            t3wt.newExtension(ThreeWindingsTransformerPhaseAngleClockAdder.class)
                .withPhaseAngleClockLeg2(pac2.isPresent() ? pac2.get().pac : 0)
                .withPhaseAngleClockLeg3(pac3.isPresent() ? pac3.get().pac : 0).add();
        }
    }

    private static boolean highVoltageAtEnd1(VoltageLevel vl1, VoltageLevel vl2) {
        return vl1.getNominalV() >= vl2.getNominalV();
    }

    private static boolean tapChangerAtEnd1(DataObject typTr2, boolean highAtEnd1) {
        int tapSide = typTr2.getIntAttributeValue("tap_side");
        // tap_side = 0 then tap_side = High voltage winding, tap_side = 1 then tap_side = Low voltage
        // tap_side is not an bus index
        return tapSide == 0 && highAtEnd1 || tapSide == 1 && !highAtEnd1;
    }

    private static void tapChangerToIidm(TapChangerModel tapChangerModel, TwoWindingsTransformer twt) {
        if (isPhaseTapChanger(tapChangerModel)) {
            PhaseTapChangerAdder ptc = twt.newPhaseTapChanger();
            tapChangerToPhaseTapChanger(tapChangerModel, ptc);
        } else if (isRatioTapChanger(tapChangerModel)) {
            RatioTapChangerAdder rtc = twt.newRatioTapChanger();
            tapChangerToRatioTapChanger(tapChangerModel, rtc);
        }
    }

    private static void tapChangerToIidm(TapChangerModel tapChangerModel, Leg leg) {
        if (isPhaseTapChanger(tapChangerModel)) {
            PhaseTapChangerAdder ptc = leg.newPhaseTapChanger();
            tapChangerToPhaseTapChanger(tapChangerModel, ptc);
        } else if (isRatioTapChanger(tapChangerModel)) {
            RatioTapChangerAdder rtc = leg.newRatioTapChanger();
            tapChangerToRatioTapChanger(tapChangerModel, rtc);
        }
    }

    private static boolean isPhaseTapChanger(TapChangerModel tapChangerModel) {
        return tapChangerModel.steps.stream().anyMatch(step -> step.angle != 0.0);
    }

    private static boolean isRatioTapChanger(TapChangerModel tapChangerModel) {
        return tapChangerModel.steps.stream().anyMatch(step -> step.ratio != 1.0);
    }

    private static void tapChangerToRatioTapChanger(TapChangerModel tapChangerModel, RatioTapChangerAdder rtc) {
        rtc.setLoadTapChangingCapabilities(false)
            .setLowTapPosition(tapChangerModel.lowTapPosition)
            .setTapPosition(tapChangerModel.tapPosition);

        tapChangerModel.steps.forEach(step ->
            rtc.beginStep()
                .setRho(1 / step.ratio)
                .setR(step.r)
                .setX(step.x)
                .setG(step.g1)
                .setB(step.b1)
                .endStep());
        rtc.add();
    }

    private static void tapChangerToPhaseTapChanger(TapChangerModel tapChangerModel, PhaseTapChangerAdder ptc) {
        ptc.setLowTapPosition(tapChangerModel.lowTapPosition)
            .setTapPosition(tapChangerModel.tapPosition);

        tapChangerModel.steps.forEach(step ->
            ptc.beginStep()
                .setRho(1 / step.ratio)
                .setAlpha(-step.angle)
                .setR(step.r)
                .setX(step.x)
                .setG(step.g1)
                .setB(step.b1)
                .endStep());
        ptc.setRegulating(false).setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP).add();
    }

    static final class TransformerModel {
        private double r;
        private double x;
        private double g;
        private double b;

        private TransformerModel(Complex impedance, Complex shuntAdmittance) {
            this.r = impedance.getReal();
            this.x = impedance.getImaginary();
            this.g = shuntAdmittance.getReal();
            this.b = shuntAdmittance.getImaginary();
        }

        private static TransformerModel create(DataObject typTr2, double ratedApparentPower, double nominalVoltageEnd2) {

            Complex impedance = createImpedance("uktr", "pcutr", typTr2, ratedApparentPower, nominalVoltageEnd2);
            Complex shuntAdmittance = createShuntAdmittance("curmg", "pfe", typTr2, ratedApparentPower, nominalVoltageEnd2);
            Complex proportion = createProportion("itrdr", "itrdl", typTr2);

            if (isProportionDefined(proportion) && shuntAdmittance.abs() != 0.0) {
                return transformerTModelToPiModel(impedance, shuntAdmittance, proportion);
            } else {
                return aproximatePiModel(impedance, shuntAdmittance);
            }
        }

        private static Complex createImpedance(String uktrT, String pcutrT, DataObject typTr2, double ratedApparentPower, double nominalVoltage) {
            float uktr = typTr2.getFloatAttributeValue(uktrT);
            float pcutr = typTr2.getFloatAttributeValue(pcutrT);

            return createImpedanceFromMeasures(uktr, pcutr, ratedApparentPower, nominalVoltage);
        }

        private static Complex createShuntAdmittance(String curmgT, String pfeT, DataObject typTr2, double ratedApparentPower, double nominalVoltage) {
            float curmg = typTr2.getFloatAttributeValue(curmgT);
            float pfe = typTr2.getFloatAttributeValue(pfeT);

            return createShuntAdmittanceFromMeasures(curmg, pfe, ratedApparentPower, nominalVoltage);
        }

        private static Complex createProportion(String itrdrT, String itrdlT, DataObject typTr2) {
            Optional<Float> itrdr = typTr2.findFloatAttributeValue(itrdrT);
            Optional<Float> itrdl = typTr2.findFloatAttributeValue(itrdlT);
            return new Complex(itrdr.isPresent() ? itrdr.get() : Double.NaN, itrdl.isPresent() ? itrdl.get() : Double.NaN);
        }

        private static boolean isProportionDefined(Complex proportion) {
            return !Double.isNaN(proportion.getReal()) && !Double.isNaN(proportion.getImaginary());
        }

        private static TransformerModel transformerTModelToPiModel(Complex z, Complex ym, Complex proportion) {
            Complex zh = new Complex(z.getReal() * proportion.getReal(), z.getImaginary() * proportion.getImaginary());
            Complex zl = new Complex(z.getReal() * (1 - proportion.getReal()), z.getImaginary() * (1 - proportion.getImaginary()));

            Complex y11h = zh.reciprocal();
            Complex y12h = zh.reciprocal().negate();
            Complex y21h = zh.reciprocal().negate();
            Complex y22h = zh.reciprocal().add(ym);

            Complex y11l = zl.reciprocal();
            Complex y12l = zl.reciprocal().negate();
            Complex y21l = zl.reciprocal().negate();
            Complex y22l = zl.reciprocal();

            Complex y11pi = y11h.subtract(y12h.multiply(y21h).divide(y22h.add(y11l)));
            Complex y12pi = y12h.multiply(y12l).divide(y22h.add(y11l)).negate();
            Complex y21pi = y21l.multiply(y21h).divide(y22h.add(y11l)).negate();
            Complex y22pi = y22l.subtract(y21l.multiply(y12l).divide(y22h.add(y11l)));

            return new TransformerModel(
                y12pi.reciprocal().negate().add(y21pi.reciprocal().negate()).multiply(0.5),
                y11pi.add(y12pi).add(y22pi.add(y21pi)));
        }

        private static TransformerModel aproximatePiModel(Complex z, Complex ym) {
            return new TransformerModel(z, ym);
        }

        /**
         * Create a transformer model from measures.
         * <p>
         * shortCircuitVoltage short-circuit voltage in %
         * copperLosses copper loss in KWh
         * openCircuitCurrent open circuit in %
         * coreLosses core (or iron) losses in KWh
         * ratedApparentPower rated apparent power in MVA
         * nominalVoltage nominal voltage in Kv
         */
        static Complex createImpedanceFromMeasures(double shortCircuitVoltage, double copperLosses,
            double ratedApparentPower, double nominalVoltage) {

            // calculate leakage impedance from short-circuit measurements
            double zpu = shortCircuitVoltage / 100;
            double rpu = copperLosses / (1000 * ratedApparentPower);
            double xpu = Math.sqrt(zpu * zpu - rpu * rpu) * Math.signum(shortCircuitVoltage);

            double r = impedanceFromPerUnitToEngineeringUnits(rpu, nominalVoltage, ratedApparentPower);
            double x = impedanceFromPerUnitToEngineeringUnits(xpu, nominalVoltage, ratedApparentPower);
            return new Complex(r, x);
        }

        static Complex createShuntAdmittanceFromMeasures(double openCircuitCurrent, double coreLosses,
            double ratedApparentPower, double nominalVoltage) {

            // calculate exciting branch admittance from open circuit measures
            // Ym = gfe - jbm
            double ypu = openCircuitCurrent / 100.0;
            double gpu = coreLosses / (1000.0 * ratedApparentPower);
            double bpu = -Math.sqrt(ypu * ypu - gpu * gpu);

            double g = admittanceFromPerUnitToEngineeringUnits(gpu, nominalVoltage, ratedApparentPower);
            double b = admittanceFromPerUnitToEngineeringUnits(bpu, nominalVoltage, ratedApparentPower);
            return new Complex(g, b);
        }

        private void moveStructuralRatioFromEnd2ToEnd1(double a02) {
            Complex a0 = new Complex(a02, 0.0);
            r = TapChangerModel.impedanceConversion(r, a0);
            x = TapChangerModel.impedanceConversion(x, a0);
            g = TapChangerModel.admittanceConversion(g, a0);
            b = TapChangerModel.admittanceConversion(b, a0);
        }
    }

    private static final class Transformer3WModel {
        private final Map<WindingType, TransformerModel> transformerModels = new EnumMap<>(WindingType.class);

        private TransformerModel getEnd(WindingType windingType) {
            return transformerModels.get(windingType);
        }

        private static Transformer3WModel create(DataObject typTr3, Rated3WModel rated3WModel, double nominalVoltage) {
            double ratedSH = rated3WModel.getEnd(WindingType.HIGH).ratedS;
            double ratedSM = rated3WModel.getEnd(WindingType.MEDIUM).ratedS;
            double ratedSL = rated3WModel.getEnd(WindingType.LOW).ratedS;
            double apparentPowerH = Math.min(ratedSH, ratedSM);
            double apparentPowerM = Math.min(ratedSM, ratedSL);
            double apparentPowerL = Math.min(ratedSL, ratedSH);
            Complex zHM = TransformerModel.createImpedance("uktr3_h", "pcut3_h", typTr3, apparentPowerH, nominalVoltage);
            Complex zML = TransformerModel.createImpedance("uktr3_m", "pcut3_m", typTr3, apparentPowerM, nominalVoltage);
            Complex zLH = TransformerModel.createImpedance("uktr3_l", "pcut3_l", typTr3, apparentPowerL, nominalVoltage);

            Complex zH = zHM.add(zLH).subtract(zML).multiply(0.5);
            Complex zM = zHM.add(zML).subtract(zLH).multiply(0.5);
            Complex zL = zML.add(zLH).subtract(zHM).multiply(0.5);

            Complex ysh = TransformerModel.createShuntAdmittance("curm3", "pfe", typTr3, ratedSH, nominalVoltage);
            int i3loc = typTr3.findIntAttributeValue("i3loc").orElse(0);

            Complex yshH = assignShuntAdmittanceToWinding(ysh, i3loc, WindingType.HIGH);
            Complex yshM = assignShuntAdmittanceToWinding(ysh, i3loc, WindingType.MEDIUM);
            Complex yshL = assignShuntAdmittanceToWinding(ysh, i3loc, WindingType.LOW);

            Transformer3WModel transformer3WModel = new Transformer3WModel();
            transformer3WModel.transformerModels.put(WindingType.HIGH, new TransformerModel(zH, yshH));
            transformer3WModel.transformerModels.put(WindingType.MEDIUM, new TransformerModel(zM, yshM));
            transformer3WModel.transformerModels.put(WindingType.LOW, new TransformerModel(zL, yshL));
            return transformer3WModel;
        }

        private static Complex assignShuntAdmittanceToWinding(Complex ysh, int i3loc, WindingType windingType) {
            // location here is not a busIndexIn, it is not a bus index
            // loc = 0 refers to high voltage winding,
            // loc = 1 means medium,
            // loc = 2 means low
            return windingType.equals(positionToWindingType(i3loc)) ? ysh : Complex.ZERO;
        }
    }

    private static final class RatedModel {
        private final double ratedU1;
        private final double ratedU2;
        private final double ratedS;

        private RatedModel(double ratedU1, double ratedU2, double ratedS) {
            this.ratedU1 = ratedU1;
            this.ratedU2 = ratedU2;
            this.ratedS = ratedS;
        }

        private static RatedModel create(DataObject typTr2, boolean highAtEnd1) {

            float strn = typTr2.getFloatAttributeValue("strn");
            float utrnL = typTr2.getFloatAttributeValue("utrn_l");
            float utrnH = typTr2.getFloatAttributeValue("utrn_h");

            double ratedU1;
            double ratedU2;
            if (highAtEnd1) {
                ratedU1 = utrnH;
                ratedU2 = utrnL;
            } else {
                ratedU1 = utrnL;
                ratedU2 = utrnH;
            }
            return new RatedModel(ratedU1, ratedU2, strn);
        }
    }

    private static final class Rated3WModel {
        private final Map<WindingType, RatedModel> ratedModels = new EnumMap<>(WindingType.class);

        private RatedModel getEnd(WindingType windingType) {
            return ratedModels.get(windingType);
        }

        private static Rated3WModel create(DataObject typTr3, double ratedU0) {

            float strnL = typTr3.getFloatAttributeValue("strn3_l");
            float strnM = typTr3.getFloatAttributeValue("strn3_m");
            float strnH = typTr3.getFloatAttributeValue("strn3_h");
            float utrnL = typTr3.getFloatAttributeValue("utrn3_l");
            float utrnM = typTr3.getFloatAttributeValue("utrn3_m");
            float utrnH = typTr3.getFloatAttributeValue("utrn3_h");

            Rated3WModel rated3WModel = new Rated3WModel();
            rated3WModel.ratedModels.put(WindingType.HIGH, new RatedModel(utrnH, ratedU0, strnH));
            rated3WModel.ratedModels.put(WindingType.MEDIUM, new RatedModel(utrnM, ratedU0, strnM));
            rated3WModel.ratedModels.put(WindingType.LOW, new RatedModel(utrnL, ratedU0, strnL));
            return rated3WModel;
        }
    }

    private static final class PhaseAngleClockModel {
        private final int pac;

        private PhaseAngleClockModel(int pac) {
            this.pac = pac;
        }

        private static Optional<PhaseAngleClockModel> create(DataObject typTr2) {
            float nt2ag = typTr2.findFloatAttributeValue("nt2ag").orElse(0f);
            if (nt2ag > 0) {
                return Optional.of(new PhaseAngleClockModel((int) nt2ag));
            } else {
                return Optional.empty();
            }
        }
    }

    private static final class PhaseAngleClock3WModel {
        private final Map<WindingType, Optional<PhaseAngleClockModel>> phaseAngleClockModels = new EnumMap<>(WindingType.class);

        private Optional<PhaseAngleClockModel> getEnd(WindingType windingType) {
            return phaseAngleClockModels.get(windingType);
        }

        private static PhaseAngleClock3WModel create(DataObject typTr3) {
            float nt3agL = typTr3.findFloatAttributeValue("nt3ag_l").orElse(0f);
            float nt3agM = typTr3.findFloatAttributeValue("nt3ag_m").orElse(0f);
            float nt3agH = typTr3.findFloatAttributeValue("nt3ag_h").orElse(0f);

            PhaseAngleClock3WModel phaseAngleClockModel = new PhaseAngleClock3WModel();
            phaseAngleClockModel.phaseAngleClockModels.put(WindingType.LOW, nt3agL > 0 ? Optional.of(new PhaseAngleClockModel((int) nt3agL)) : Optional.empty());
            phaseAngleClockModel.phaseAngleClockModels.put(WindingType.MEDIUM, nt3agM > 0 ? Optional.of(new PhaseAngleClockModel((int) nt3agM)) : Optional.empty());
            phaseAngleClockModel.phaseAngleClockModels.put(WindingType.HIGH, nt3agH > 0 ? Optional.of(new PhaseAngleClockModel((int) nt3agH)) : Optional.empty());
            return phaseAngleClockModel;
        }
    }

    private static final class PowerFactoryTapChanger {
        private final int nntap;
        private final int nntap0;
        private final int ntpmn;
        private final int ntpmx;
        private final double dutap;
        private final double phitr;
        private RealMatrix mTaps = null;

        private PowerFactoryTapChanger(int nntap, int nntap0, int ntpmn, int ntpmx, double dutap, double phitr) {
            this.nntap = nntap;
            this.nntap0 = nntap0;
            this.ntpmn = ntpmn;
            this.ntpmx = ntpmx;
            this.dutap = dutap;
            this.phitr = phitr;
        }

        private static PowerFactoryTapChanger create(DataObject elmTr2, DataObject typTr2) {
            PowerFactoryTapChanger powerFactoryTapChanger = create("nntap", "nntap0", "ntpmn", "ntpmx", "dutap", "phitr", elmTr2, typTr2);

            powerFactoryTapChanger.mTaps = elmTr2.findDoubleMatrixAttributeValue("mTaps").orElse(null);
            return powerFactoryTapChanger;
        }

        private static PowerFactoryTapChanger create(String nntapT, String nntap0T, String ntpmnT, String ntpmxT, String duTapT,
                                                     String phitrT, DataObject elmTr2, DataObject typTr2) {
            int nntap = elmTr2.getIntAttributeValue(nntapT);

            int nntap0 = typTr2.getIntAttributeValue(nntap0T);
            int ntpmn = typTr2.getIntAttributeValue(ntpmnT);
            int ntpmx = typTr2.getIntAttributeValue(ntpmxT);

            nntap = fixTapInsideLimits(nntap, ntpmn, ntpmx, elmTr2);

            Optional<Float> opdutap = typTr2.findFloatAttributeValue(duTapT);
            Optional<Float> opphitr = typTr2.findFloatAttributeValue(phitrT);

            double dutap = opdutap.isPresent() ? opdutap.get() : 0.0;
            double phitr = opphitr.isPresent() ? opphitr.get() : 0.0;

            return new PowerFactoryTapChanger(nntap, nntap0, ntpmn, ntpmx, dutap, phitr);
        }

        private static int fixTapInsideLimits(int nntap, int ntpmn, int ntpmx, DataObject elementObj) {
            if (nntap < ntpmn) {
                LOGGER.warn("{}: Tap {} has been fixed to the minimum tap {} '{}'", elementObj.getDataClassName(), nntap, ntpmn, elementObj);
                return ntpmn;
            } else if (nntap > ntpmx) {
                LOGGER.warn("{}: Tap {} has been fixed to the maximum tap {} '{}'", elementObj.getDataClassName(), nntap, ntpmx, elementObj);
                return ntpmx;
            } else {
                return nntap;
            }
        }
    }

    private static final class PowerFactoryTapChangers3W {
        private PowerFactoryTapChanger high;
        private PowerFactoryTapChanger medium;
        private PowerFactoryTapChanger low;

        private static PowerFactoryTapChangers3W create(DataObject elmTr3, DataObject typTr3) {
            PowerFactoryTapChangers3W pft = new PowerFactoryTapChangers3W();

            pft.high = PowerFactoryTapChanger.create("n3tap_h", "n3tp0_h", "n3tmn_h", "n3tmx_h", "du3tp_h", "ph3tr_h", elmTr3, typTr3);
            pft.medium = PowerFactoryTapChanger.create("n3tap_m", "n3tp0_m", "n3tmn_m", "n3tmx_m", "du3tp_m", "ph3tr_m", elmTr3, typTr3);
            pft.low = PowerFactoryTapChanger.create("n3tap_l", "n3tp0_l", "n3tmn_l", "n3tmx_l", "du3tp_l", "ph3tr_l", elmTr3, typTr3);

            int iMeasTap = elmTr3.findIntAttributeValue("iMeasTap").orElse(0);
            elmTr3.findDoubleMatrixAttributeValue("mTaps").ifPresent(mTaps -> {
                switch (positionToWindingType(iMeasTap)) {
                    case HIGH:
                        pft.high.mTaps = mTaps;
                        break;
                    case MEDIUM:
                        pft.medium.mTaps = mTaps;
                        break;
                    case LOW:
                        pft.low.mTaps = mTaps;
                        break;
                }
            });

            return pft;
        }
    }

    private static final class TapChangerModel {
        private final int lowTapPosition;
        private final int tapPosition;
        private final List<TapChangerStep> steps;

        private TapChangerModel(int lowTapPosition, int tapPosition) {
            this.lowTapPosition = lowTapPosition;
            this.tapPosition = tapPosition;
            steps = new ArrayList<>();
        }

        // angle in degrees
        private static final class TapChangerStep {
            private double ratio;
            private double angle;
            private double r;
            private double x;
            private double g1;
            private double b1;

            private TapChangerStep(double ratio, double angle) {
                this(ratio, angle, 0.0, 0.0, 0.0, 0.0);
            }

            private TapChangerStep(double ratio, double angle, double r, double x, double g1, double b1) {
                this.ratio = ratio;
                this.angle = angle;
                this.r = r;
                this.x = x;
                this.g1 = g1;
                this.b1 = b1;
            }
        }

        private static Optional<TapChangerModel> create(PowerFactoryTapChanger powerFactoryTapChanger, boolean tapChangerAtEnd1) {
            Optional<TapChangerModel> tapChanger = TapChangerModel.create(powerFactoryTapChanger);
            if (tapChanger.isPresent()) {
                if (tapChangerAtEnd1) {
                    return tapChanger;
                } else {
                    return Optional.of(moveTapChanger(tapChanger.get()));
                }
            }
            return tapChanger;
        }

        private static Optional<TapChangerModel> create(PowerFactoryTapChanger powerFactoryTapChanger) {
            if (powerFactoryTapChanger.dutap == 0.0 && powerFactoryTapChanger.phitr == 0.0 && powerFactoryTapChanger.mTaps == null) {
                return Optional.empty();
            }

            PowerFactoryTapChanger fixedTapchangerPar = fixAndCheckTapChangerPar(powerFactoryTapChanger);

            if (fixedTapchangerPar.mTaps != null) {
                return Optional.of(createTapChangerFromResourceTable(fixedTapchangerPar));
            }
            return Optional.of(createTapChangerFromAtributes(fixedTapchangerPar));
        }

        private static PowerFactoryTapChanger fixAndCheckTapChangerPar(PowerFactoryTapChanger powerFactoryTapChanger) {

            // In IIDM always minTap = 0
            int nntap = powerFactoryTapChanger.nntap - powerFactoryTapChanger.ntpmn;
            int nntap0 = powerFactoryTapChanger.nntap0 - powerFactoryTapChanger.ntpmn;
            int ntpmn = 0;
            int ntpmx = powerFactoryTapChanger.ntpmx - powerFactoryTapChanger.ntpmn;

            PowerFactoryTapChanger fixedPowerFactoryTapChanger = new PowerFactoryTapChanger(nntap, nntap0, ntpmn, ntpmx, powerFactoryTapChanger.dutap, powerFactoryTapChanger.phitr);
            fixedPowerFactoryTapChanger.mTaps = powerFactoryTapChanger.mTaps;

            return fixedPowerFactoryTapChanger;
        }

        private static TapChangerModel createTapChangerFromResourceTable(PowerFactoryTapChanger powerFactoryTapChanger) {
            if (powerFactoryTapChanger.mTaps.getColumnDimension() == 5) {
                return createTapChangerFromResourceTableForTwoWindingsTansformer(powerFactoryTapChanger);
            }
            if (powerFactoryTapChanger.mTaps.getColumnDimension() == 8) {
                return createTapChangerFromResourceTableForThreeWindingsTansformer(powerFactoryTapChanger);
            }
            throw new PowerFactoryException("Unexpected number of columns in mTaps");
        }

        private static TapChangerModel createTapChangerFromResourceTableForTwoWindingsTansformer(PowerFactoryTapChanger powerFactoryTapChanger) {

            int rows = powerFactoryTapChanger.mTaps.getRowDimension();
            if (rows != powerFactoryTapChanger.ntpmx - powerFactoryTapChanger.ntpmn + 1) {
                throw new PowerFactoryException("Unexpected number of rows in mTaps");
            }
            TapChangerModel tapChangerModel = new TapChangerModel(powerFactoryTapChanger.ntpmn, powerFactoryTapChanger.nntap);
            for (int row = 0; row < rows; row++) {
                double ratio = powerFactoryTapChanger.mTaps.getEntry(row, 4);
                double angle = powerFactoryTapChanger.mTaps.getEntry(row, 1);

                tapChangerModel.steps.add(new TapChangerStep(ratio, angle));
            }
            return tapChangerModel;
        }

        private static TapChangerModel createTapChangerFromResourceTableForThreeWindingsTansformer(PowerFactoryTapChanger powerFactoryTapChanger) {

            int rows = powerFactoryTapChanger.mTaps.getRowDimension();
            if (rows != powerFactoryTapChanger.ntpmx - powerFactoryTapChanger.ntpmn + 1) {
                throw new PowerFactoryException("Unexpected mTaps dimension");
            }
            double ratio = 1.0;
            TapChangerModel tapChangerModel = new TapChangerModel(powerFactoryTapChanger.ntpmn, powerFactoryTapChanger.nntap);
            for (int row = 0; row < rows; row++) {
                double angle = powerFactoryTapChanger.mTaps.getEntry(row, 1);

                tapChangerModel.steps.add(new TapChangerStep(ratio, angle));
            }
            return tapChangerModel;
        }

        private static TapChangerModel createTapChangerFromAtributes(PowerFactoryTapChanger powerFactoryTapChanger) {

            TapChangerModel tapChangerModel = new TapChangerModel(powerFactoryTapChanger.ntpmn, powerFactoryTapChanger.nntap);
            for (int tap = powerFactoryTapChanger.ntpmn; tap <= powerFactoryTapChanger.ntpmx; tap++) {
                TapChangerStep tapChangerStep = createTapChangerStep(tap, powerFactoryTapChanger.nntap0, powerFactoryTapChanger.dutap, powerFactoryTapChanger.phitr);
                tapChangerModel.steps.add(tapChangerStep);
            }
            return tapChangerModel;
        }

        private static TapChangerStep createTapChangerStep(int tap, int nntap0, double dutap, double phitr) {
            double ratio = 1 + (tap - nntap0) * dutap / 100.0;
            double angle = (tap - nntap0) * phitr;
            return new TapChangerStep(ratio, angle);
        }

        /**
         * Step corrections are updated to obtain an equivalent tapChanger in the other side.
         * Step r, x, g, b are already percentage deviations of nominal values
         * R = R * (1 + r / 100)
         * X = X * (1 + x / 100)
         * G = G * (1 + g / 100)
         * B = B * (1 + b / 100)
         */
        private static TapChangerModel moveTapChanger(TapChangerModel tc) {
            tc.steps.forEach(step -> {
                double ratio = step.ratio;
                double angle = step.angle;
                double r = step.r;
                double x = step.x;
                double g1 = step.g1;
                double b1 = step.b1;
                calculateConversionStep(step, ratio, angle, r, x, g1, b1);
            });
            return tc;
        }

        private static void calculateConversionStep(TapChangerStep step, double ratio, double angle, double r, double x, double g1, double b1) {
            Complex a = new Complex(ratio * Math.cos(Math.toRadians(angle)), ratio * Math.sin(Math.toRadians(angle)));

            Complex na = a.reciprocal();
            step.ratio = na.abs();
            step.angle = Math.toDegrees(na.getArgument());
            step.r = 100 * (impedanceConversion(1 + r / 100, a) - 1);
            step.x = 100 * (impedanceConversion(1 + x / 100, a) - 1);
            step.g1 = 100 * (admittanceConversion(1 + g1 / 100, a) - 1);
            step.b1 = 100 * (admittanceConversion(1 + b1 / 100, a) - 1);
        }

        private static double admittanceConversion(double correction, Complex a) {
            double a2 = a.abs() * a.abs();
            return correction / a2;
        }

        private static double impedanceConversion(double correction, Complex a) {
            double a2 = a.abs() * a.abs();
            return correction * a2;
        }
    }

    private static final class TapChanger3W {
        private final Map<WindingType, Optional<TapChangerModel>> tapChangers = new EnumMap<>(WindingType.class);

        private Optional<TapChangerModel> getEnd(WindingType windingType) {
            return tapChangers.get(windingType);
        }

        private static TapChanger3W create(PowerFactoryTapChangers3W tapChangerTap3w) {
            TapChanger3W tapChanger3W = new TapChanger3W();
            tapChanger3W.tapChangers.put(WindingType.HIGH, TapChangerModel.create(tapChangerTap3w.high));
            tapChanger3W.tapChangers.put(WindingType.MEDIUM, TapChangerModel.create(tapChangerTap3w.medium));
            tapChanger3W.tapChangers.put(WindingType.LOW, TapChangerModel.create(tapChangerTap3w.low));
            return tapChanger3W;
        }
    }

    private static List<WindingType> createWindingTypeEnds(VoltageLevel vl1, VoltageLevel vl2, VoltageLevel vl3) {
        double vn1 = vl1.getNominalV();
        double vn2 = vl2.getNominalV();
        double vn3 = vl3.getNominalV();

        if (vn1 >= vn2 && vn2 >= vn3) {
            return List.of(WindingType.HIGH, WindingType.MEDIUM, WindingType.LOW);
        } else if (vn1 >= vn3 && vn3 >= vn2) {
            return List.of(WindingType.HIGH, WindingType.LOW, WindingType.MEDIUM);
        } else if (vn2 >= vn1 && vn1 >= vn3) {
            return List.of(WindingType.MEDIUM, WindingType.HIGH, WindingType.LOW);
        } else if (vn1 >= vn2) {
            return List.of(WindingType.MEDIUM, WindingType.LOW, WindingType.HIGH);
        } else if (vn2 >= vn3) {
            return List.of(WindingType.LOW, WindingType.HIGH, WindingType.MEDIUM);
        } else {
            return List.of(WindingType.LOW, WindingType.MEDIUM, WindingType.HIGH);
        }
    }

    private static WindingType positionToWindingType(int position) {
        switch (position) {
            case 0:
                return WindingType.HIGH;
            case 1:
                return WindingType.MEDIUM;
            case 2:
                return WindingType.LOW;
            default:
                throw new PowerFactoryException("Unexpected position: " + position);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerConverter.class);
}
