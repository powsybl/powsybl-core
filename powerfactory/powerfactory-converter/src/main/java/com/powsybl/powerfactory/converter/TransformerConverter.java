/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.NodeRef;
import com.powsybl.powerfactory.model.DataObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

class TransformerConverter extends AbstractConverter {

    private static final String UNEXPECTED_WINDING_TYPE = "Unexpected windingType: '";

    private enum WindingType {
        HV, MV, LV;
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

        boolean hvAtEnd1 = highVoltageAtEnd1(vl1, vl2);
        boolean tapChangerAtEnd1 = tapChangerAtEnd1(typTr2, hvAtEnd1);

        RatedModel ratedModel = RatedModel.create(typTr2, hvAtEnd1);
        double nominalVoltageEnd2 = vl2.getNominalV();
        TransformerModel transformerModel = TransformerModel.create(typTr2, ratedModel.ratedS, nominalVoltageEnd2);

        if (!tapChangerAtEnd1) {
            // Structural ratio at end2 = ratedU2 / vn2
            transformerModel.moveStructuralRatioFromEnd2ToEnd1(ratedModel.ratedU2 / vl2.getNominalV());
        }

        TapChangerPar tapChangerPar = TapChangerPar.create(elmTr2, typTr2);
        Optional<TapChanger> tc = TapChanger.create(tapChangerPar, tapChangerAtEnd1);

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

        tapChangerToIidm(tc, t2wt);
    }

    void createThreeWindings(DataObject elmTr3) {

        DataObject typTr3 = elmTr3.getObjectAttributeValue(DataAttributeNames.TYP_ID).resolve().orElseThrow();

        List<NodeRef> nodeRefs = checkNodes(elmTr3, 3);
        NodeRef nodeRef1 = nodeRefs.get(0);
        NodeRef nodeRef2 = nodeRefs.get(1);
        NodeRef nodeRef3 = nodeRefs.get(2);

        VoltageLevel vl1 = getNetwork().getVoltageLevel(nodeRef1.voltageLevelId);
        VoltageLevel vl2 = getNetwork().getVoltageLevel(nodeRef2.voltageLevelId);
        VoltageLevel vl3 = getNetwork().getVoltageLevel(nodeRef3.voltageLevelId);
        List<WindingType> windingTypeEnds = createWindingTypeEnds(vl1, vl2, vl3);

        double vn0 = 1.0;
        double ratedU0 = vn0;
        RatedModel3w ratedModel3w = RatedModel3w.create(typTr3, ratedU0);
        RatedModel ratedModel1 = ratedModel3w.getEnd(windingTypeEnds.get(0));
        RatedModel ratedModel2 = ratedModel3w.getEnd(windingTypeEnds.get(1));
        RatedModel ratedModel3 = ratedModel3w.getEnd(windingTypeEnds.get(2));
        double ratedU1 = ratedModel1.ratedU1;
        double ratedU2 = ratedModel2.ratedU1;
        double ratedU3 = ratedModel3.ratedU1;

        Transformer3wModel transformer3wModel = Transformer3wModel.create(typTr3, ratedModel3w, vn0);
        TransformerModel transformerModel1 = transformer3wModel.getEnd(windingTypeEnds.get(0));
        TransformerModel transformerModel2 = transformer3wModel.getEnd(windingTypeEnds.get(1));
        TransformerModel transformerModel3 = transformer3wModel.getEnd(windingTypeEnds.get(2));

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

        TapChangerPar3w tapChangerPar3w = TapChangerPar3w.create(elmTr3, typTr3);
        TapChanger3w tapChanger3w = TapChanger3w.create(tapChangerPar3w);
        Optional<TapChanger> tc1 = tapChanger3w.getEnd(windingTypeEnds.get(0));
        Optional<TapChanger> tc2 = tapChanger3w.getEnd(windingTypeEnds.get(1));
        Optional<TapChanger> tc3 = tapChanger3w.getEnd(windingTypeEnds.get(2));

        tapChangerToIidm(tc1, t3wt.getLeg1());
        tapChangerToIidm(tc2, t3wt.getLeg2());
        tapChangerToIidm(tc3, t3wt.getLeg3());
    }

    private static boolean highVoltageAtEnd1(VoltageLevel vl1, VoltageLevel vl2) {
        return vl1.getNominalV() >= vl2.getNominalV();
    }

    // tap_side = 0 then tap_side = Hv, tap_side = 1 then tap_side = Lv
    private static boolean tapChangerAtEnd1(DataObject typTr2, boolean hvAtEnd1) {
        int tapside = typTr2.getIntAttributeValue("tap_side");
        return tapside == 0 && hvAtEnd1 || tapside == 1 && !hvAtEnd1;
    }

    private static void tapChangerToIidm(Optional<TapChanger> opTapChanger, TwoWindingsTransformer twt) {
        if (opTapChanger.isEmpty()) {
            return;
        }
        TapChanger tapChanger = opTapChanger.get();
        if (isPhaseTapChanger(tapChanger)) {
            PhaseTapChangerAdder ptc = twt.newPhaseTapChanger();
            tapChangerToPhaseTapChanger(tapChanger, ptc);
        } else if (isRatioTapChanger(tapChanger)) {
            RatioTapChangerAdder rtc = twt.newRatioTapChanger();
            tapChangerToRatioTapChanger(tapChanger, rtc);
        }
    }

    private static void tapChangerToIidm(Optional<TapChanger> opTapChanger, Leg leg) {
        if (opTapChanger.isEmpty()) {
            return;
        }
        TapChanger tapChanger = opTapChanger.get();
        if (isPhaseTapChanger(tapChanger)) {
            PhaseTapChangerAdder ptc = leg.newPhaseTapChanger();
            tapChangerToPhaseTapChanger(tapChanger, ptc);
        } else if (isRatioTapChanger(tapChanger)) {
            RatioTapChangerAdder rtc = leg.newRatioTapChanger();
            tapChangerToRatioTapChanger(tapChanger, rtc);
        }
    }

    private static boolean isPhaseTapChanger(TapChanger tapChanger) {
        return tapChanger.steps.stream().anyMatch(step -> step.angle != 0.0);
    }

    private static boolean isRatioTapChanger(TapChanger tapChanger) {
        return tapChanger.steps.stream().anyMatch(step -> step.ratio != 1.0);
    }

    private static void tapChangerToRatioTapChanger(TapChanger tapChanger, RatioTapChangerAdder rtc) {
        rtc.setLoadTapChangingCapabilities(false)
            .setLowTapPosition(tapChanger.lowTapPosition)
            .setTapPosition(tapChanger.tapPosition);

        tapChanger.steps.forEach(step ->
            rtc.beginStep()
                .setRho(1 / step.ratio)
                .setR(step.r)
                .setX(step.x)
                .setG(step.g1)
                .setB(step.b1)
                .endStep());
        rtc.add();
    }

    private static void tapChangerToPhaseTapChanger(TapChanger tapChanger, PhaseTapChangerAdder ptc) {
        ptc.setLowTapPosition(tapChanger.lowTapPosition)
            .setTapPosition(tapChanger.tapPosition);

        tapChanger.steps.forEach(step ->
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
            return new TransformerModel(impedance, shuntAdmittance);
        }

        private static Complex createImpedance(String uktrT, String pcutrT, DataObject typTr2,  double ratedApparentPower, double nominalVoltage) {
            float uktr = typTr2.getFloatAttributeValue(uktrT);
            float pcutr = typTr2.getFloatAttributeValue(pcutrT);

            return createImpedanceFromMeasures(uktr, pcutr, ratedApparentPower, nominalVoltage);
        }

        private static Complex createShuntAdmittance(String curmgT, String pfeT, DataObject typTr2, double ratedApparentPower, double nominalVoltage) {
            float curmg = typTr2.getFloatAttributeValue(curmgT);
            float pfe = typTr2.getFloatAttributeValue(pfeT);

            return createShuntAdmittanceFromMeasures(curmg, pfe, ratedApparentPower, nominalVoltage);
        }

        /**
         * Create a transformer model from measures.
         *
         * shortCircuitVoltage short circuit voltage in %
         * copperLosses copper loss in KWh
         * openCircuitCurrent open circuit in %
         * coreLosses core (or iron) losses in KWh
         * ratedApparentPower rated apparent power in MVA
         * nominalVoltage nominal voltage in Kv
         */
        static Complex createImpedanceFromMeasures(double shortCircuitVoltage, double copperLosses,
            double ratedApparentPower, double nominalVoltage) {

            // calculate leakage impedance from short circuit measures
            double zpu = shortCircuitVoltage / 100;
            double rpu = copperLosses / (1000 * ratedApparentPower);
            double xpu = Math.sqrt(zpu * zpu - rpu * rpu);

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
            r = TapChanger.impedanceConversion(r, a0);
            x = TapChanger.impedanceConversion(x, a0);
            g = TapChanger.admittanceConversion(g, a0);
            b = TapChanger.admittanceConversion(b, a0);
        }
    }

    private static final class Transformer3wModel {
        private final TransformerModel hv;
        private final TransformerModel mv;
        private final TransformerModel lv;

        private Transformer3wModel(TransformerModel hv, TransformerModel mv, TransformerModel lv) {
            this.hv = hv;
            this.mv = mv;
            this.lv = lv;
        }

        private TransformerModel getEnd(WindingType windingType) {
            switch (windingType) {
                case HV:
                    return hv;
                case MV:
                    return mv;
                case LV:
                    return lv;
                default:
                    throw new PowsyblException(UNEXPECTED_WINDING_TYPE + windingType + "'");
            }
        }

        private static Transformer3wModel create(DataObject typTr3, RatedModel3w ratedModel, double nominalVoltage) {
            Complex zHvMv = TransformerModel.createImpedance("uktr3_h", "pcut3_h", typTr3, Math.min(ratedModel.hv.ratedS, ratedModel.mv.ratedS), nominalVoltage);
            Complex zMvLv = TransformerModel.createImpedance("uktr3_m", "pcut3_m", typTr3, Math.min(ratedModel.mv.ratedS, ratedModel.lv.ratedS), nominalVoltage);
            Complex zLvHv = TransformerModel.createImpedance("uktr3_l", "pcut3_l", typTr3, Math.min(ratedModel.lv.ratedS, ratedModel.hv.ratedS), nominalVoltage);

            Complex zHv = zHvMv.add(zLvHv).subtract(zMvLv).multiply(0.5);
            Complex zMv = zHvMv.add(zMvLv).subtract(zLvHv).multiply(0.5);
            Complex zLv = zMvLv.add(zLvHv).subtract(zHvMv).multiply(0.5);

            Complex ysh = TransformerModel.createShuntAdmittance("curm3", "pfe", typTr3, ratedModel.hv.ratedS, nominalVoltage);
            return new Transformer3wModel(new TransformerModel(zHv, ysh), new TransformerModel(zMv, Complex.ZERO), new TransformerModel(zLv, Complex.ZERO));
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

        private static RatedModel create(DataObject typTr2, boolean hvAtEnd1) {

            float strn = typTr2.getFloatAttributeValue("strn");
            float utrnL = typTr2.getFloatAttributeValue("utrn_l");
            float utrnH = typTr2.getFloatAttributeValue("utrn_h");

            double ratedU1;
            double ratedU2;
            if (hvAtEnd1) {
                ratedU1 = utrnH;
                ratedU2 = utrnL;
            } else {
                ratedU1 = utrnL;
                ratedU2 = utrnH;
            }
            return new RatedModel(ratedU1, ratedU2, strn);
        }
    }

    private static final class RatedModel3w {
        private final RatedModel hv;
        private final RatedModel mv;
        private final RatedModel lv;

        private RatedModel3w(RatedModel hv, RatedModel mv, RatedModel lv) {
            this.hv = hv;
            this.mv = mv;
            this.lv = lv;
        }

        private RatedModel getEnd(WindingType windingType) {
            switch (windingType) {
                case HV:
                    return hv;
                case MV:
                    return mv;
                case LV:
                    return lv;
                default:
                    throw new PowsyblException(UNEXPECTED_WINDING_TYPE + windingType + "'");
            }
        }

        private static RatedModel3w create(DataObject typTr3, double ratedU0) {

            float strnL = typTr3.getFloatAttributeValue("strn3_l");
            float strnM = typTr3.getFloatAttributeValue("strn3_m");
            float strnH = typTr3.getFloatAttributeValue("strn3_h");
            float utrnL = typTr3.getFloatAttributeValue("utrn3_l");
            float utrnM = typTr3.getFloatAttributeValue("utrn3_m");
            float utrnH = typTr3.getFloatAttributeValue("utrn3_h");

            return new RatedModel3w(new RatedModel(utrnH, ratedU0, strnH), new RatedModel(utrnM, ratedU0, strnM), new RatedModel(utrnL, ratedU0, strnL));
        }
    }

    private static final class TapChangerPar {
        private final int nntap;
        private final int nntap0;
        private final int ntpmn;
        private final int ntpmx;
        private final double dutap;
        private final double phitr;
        private Optional<RealMatrix> mTaps;

        private TapChangerPar(int nntap, int nntap0, int ntpmn, int ntpmx, double dutap, double phitr) {
            this.nntap = nntap;
            this.nntap0 = nntap0;
            this.ntpmn = ntpmn;
            this.ntpmx = ntpmx;
            this.dutap = dutap;
            this.phitr = phitr;
        }

        private static TapChangerPar create(DataObject elmTr2, DataObject typTr2) {
            TapChangerPar tapChangerPar = create("nntap", "nntap0", "ntpmn", "ntpmx", "dutap", "phitr", elmTr2, typTr2);

            tapChangerPar.mTaps = elmTr2.findAndParseDoubleMatrixAttributeValue("mTaps");
            return tapChangerPar;
        }

        private static TapChangerPar create(String nntapT, String nntap0T, String ntpmnT, String ntpmxT, String duTapT,
            String phitrT, DataObject elmTr2, DataObject typTr2) {
            int nntap = elmTr2.getIntAttributeValue(nntapT);

            int nntap0 = typTr2.getIntAttributeValue(nntap0T);
            int ntpmn = typTr2.getIntAttributeValue(ntpmnT);
            int ntpmx = typTr2.getIntAttributeValue(ntpmxT);

            Optional<Float> opdutap = typTr2.findFloatAttributeValue(duTapT);
            Optional<Float> opphitr = typTr2.findFloatAttributeValue(phitrT);

            double dutap = opdutap.isPresent() ? opdutap.get() : 0.0;
            double phitr = opphitr.isPresent() ? opphitr.get() : 0.0;

            return new TapChangerPar(nntap, nntap0, ntpmn, ntpmx, dutap, phitr);
        }
    }

    private static final class TapChangerPar3w {
        private final TapChangerPar hv;
        private final TapChangerPar mv;
        private final TapChangerPar lv;

        private TapChangerPar3w(TapChangerPar hv, TapChangerPar mv, TapChangerPar lv) {
            this.hv = hv;
            this.mv = mv;
            this.lv = lv;
        }

        private static TapChangerPar3w create(DataObject elmTr3, DataObject typTr3) {
            TapChangerPar hv = TapChangerPar.create("n3tap_h", "n3tp0_h", "n3tmn_h", "n3tmx_h", "du3tp_h", "ph3tr_h", elmTr3, typTr3);
            TapChangerPar mv = TapChangerPar.create("n3tap_m", "n3tp0_m", "n3tmn_m", "n3tmx_m", "du3tp_m", "ph3tr_m", elmTr3, typTr3);
            TapChangerPar lv = TapChangerPar.create("n3tap_l", "n3tp0_l", "n3tmn_l", "n3tmx_l", "du3tp_l", "ph3tr_l", elmTr3, typTr3);

            hv.mTaps = Optional.empty();
            mv.mTaps = Optional.empty();
            lv.mTaps = Optional.empty();

            return new TapChangerPar3w(hv, mv, lv);
        }
    }

    private static final class TapChanger {
        private final int lowTapPosition;
        private final int tapPosition;
        private final List<TapChangerStep> steps;

        private TapChanger(int lowTapPosition, int tapPosition) {
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

        private static Optional<TapChanger> create(TapChangerPar tapChangerPar, boolean tapChangerAtEnd1) {
            Optional<TapChanger> tapChanger = TapChanger.create(tapChangerPar);
            if (tapChanger.isPresent()) {
                if (tapChangerAtEnd1) {
                    return tapChanger;
                } else {
                    return Optional.of(moveTapChanger(tapChanger.get()));
                }
            }
            return tapChanger;
        }

        private static Optional<TapChanger> create(TapChangerPar tapChangerPar) {
            if (tapChangerPar.dutap == 0.0 && tapChangerPar.phitr == 0.0 && tapChangerPar.mTaps.isEmpty()) {
                return Optional.empty();
            }

            TapChangerPar fixedTapchangerPar = fixAndCheckTapChangerPar(tapChangerPar);

            if (fixedTapchangerPar.mTaps.isPresent()) {
                return Optional.of(createTapChangerFromResourceTable(fixedTapchangerPar));
            }
            return Optional.of(createTapChangerFromAtributes(fixedTapchangerPar));
        }

        private static TapChangerPar fixAndCheckTapChangerPar(TapChangerPar tapChangerPar) {

            // In IIDM always minTap = 0
            int nntap = tapChangerPar.nntap - tapChangerPar.ntpmn;
            int nntap0 = tapChangerPar.nntap0 - tapChangerPar.ntpmn;
            int ntpmn = 0;
            int ntpmx = tapChangerPar.ntpmx - tapChangerPar.ntpmn;

            TapChangerPar fixedTapChangerPar = new TapChangerPar(nntap, nntap0, ntpmn, ntpmx, tapChangerPar.dutap, tapChangerPar.phitr);
            fixedTapChangerPar.mTaps = tapChangerPar.mTaps;

            return fixedTapChangerPar;
        }

        private static TapChanger createTapChangerFromResourceTable(TapChangerPar tapChangerPar) {

            int rows = tapChangerPar.mTaps.get().getRowDimension();
            if (rows != tapChangerPar.ntpmx - tapChangerPar.ntpmn + 1 || tapChangerPar.mTaps.get().getColumnDimension() != 5) {
                throw new PowsyblException("Unexpected mTaps dimension");
            }
            TapChanger tapChanger = new TapChanger(tapChangerPar.ntpmn, tapChangerPar.nntap);
            for (int row = 0; row < rows; row++) {
                double ratio = tapChangerPar.mTaps.get().getEntry(row, 4);
                double angle = tapChangerPar.mTaps.get().getEntry(row, 1);

                tapChanger.steps.add(new TapChangerStep(ratio, angle));
            }
            return tapChanger;
        }

        private static TapChanger createTapChangerFromAtributes(TapChangerPar tapChangerPar) {

            TapChanger tapChanger = new TapChanger(tapChangerPar.ntpmn, tapChangerPar.nntap);
            for (int tap = tapChangerPar.ntpmn; tap <= tapChangerPar.ntpmx; tap++) {
                TapChangerStep tapChangerStep = createTapChangerStep(tap, tapChangerPar.nntap0, tapChangerPar.dutap, tapChangerPar.phitr);
                tapChanger.steps.add(tapChangerStep);
            }
            return tapChanger;
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
        private static TapChanger moveTapChanger(TapChanger tc) {
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

    private static final class TapChanger3w {
        private final Optional<TapChanger> hv;
        private final Optional<TapChanger> mv;
        private final Optional<TapChanger> lv;

        private TapChanger3w(Optional<TapChanger> hv, Optional<TapChanger> mv, Optional<TapChanger> lv) {
            this.hv = hv;
            this.mv = mv;
            this.lv = lv;
        }

        private Optional<TapChanger> getEnd(WindingType windingType) {
            switch (windingType) {
                case HV:
                    return hv;
                case MV:
                    return mv;
                case LV:
                    return lv;
                default:
                    throw new PowsyblException(UNEXPECTED_WINDING_TYPE + windingType + "'");
            }
        }

        private static TapChanger3w create(TapChangerPar3w tapChangerTap3w) {
            Optional<TapChanger> tcHv = TapChanger.create(tapChangerTap3w.hv);
            Optional<TapChanger> tcMv = TapChanger.create(tapChangerTap3w.mv);
            Optional<TapChanger> tcLv = TapChanger.create(tapChangerTap3w.lv);
            return new TapChanger3w(tcHv, tcMv, tcLv);
        }
    }

    private static List<WindingType> createWindingTypeEnds(VoltageLevel vl1, VoltageLevel vl2, VoltageLevel vl3) {
        double vn1 = vl1.getNominalV();
        double vn2 = vl2.getNominalV();
        double vn3 = vl3.getNominalV();

        List<WindingType> windingTypeEnds = new ArrayList<>(3);
        if (vn1 >= vn2 && vn2 >= vn3) {
            Collections.addAll(windingTypeEnds, WindingType.HV, WindingType.MV, WindingType.LV);
        } else if (vn1 >= vn3 && vn3 >= vn2) {
            Collections.addAll(windingTypeEnds, WindingType.HV, WindingType.LV, WindingType.MV);
        } else if (vn2 >= vn1 && vn1 >= vn3) {
            Collections.addAll(windingTypeEnds, WindingType.MV, WindingType.HV, WindingType.LV);
        } else if (vn1 >= vn2) {
            Collections.addAll(windingTypeEnds, WindingType.MV, WindingType.LV, WindingType.HV);
        } else if (vn2 >= vn3) {
            Collections.addAll(windingTypeEnds, WindingType.LV, WindingType.HV, WindingType.MV);
        } else {
            Collections.addAll(windingTypeEnds, WindingType.LV, WindingType.MV, WindingType.HV);
        }
        return windingTypeEnds;
    }
}

