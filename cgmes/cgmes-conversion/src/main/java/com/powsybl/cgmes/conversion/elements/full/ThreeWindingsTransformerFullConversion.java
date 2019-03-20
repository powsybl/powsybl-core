package com.powsybl.cgmes.conversion.elements.full;

import java.util.Map;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.Leg1Adder;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.Leg2or3Adder;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class ThreeWindingsTransformerFullConversion extends AbstractTransformerFullConversion {

    private enum FinalPosition {
        OUTSIDE, INSIDE
    }

    public ThreeWindingsTransformerFullConversion(PropertyBags ends,
            Map<String, PropertyBag> powerTransformerRatioTapChanger,
            Map<String, PropertyBag> powerTransformerPhaseTapChanger, Context context) {
        super("PowerTransformer", ends, context);
        PropertyBag winding1 = ends.get(0);
        PropertyBag winding2 = ends.get(1);
        PropertyBag winding3 = ends.get(2);
        String ratedU = "ratedU";

        rtc1 = getTransformerTapChanger(winding1, "RatioTapChanger",
                powerTransformerRatioTapChanger);
        ptc1 = getTransformerTapChanger(winding1, "PhaseTapChanger",
                powerTransformerPhaseTapChanger);
        rtc2 = getTransformerTapChanger(winding2, "RatioTapChanger",
                powerTransformerRatioTapChanger);
        ptc2 = getTransformerTapChanger(winding2, "PhaseTapChanger",
                powerTransformerPhaseTapChanger);
        rtc3 = getTransformerTapChanger(winding3, "RatioTapChanger",
                powerTransformerRatioTapChanger);
        ptc3 = getTransformerTapChanger(winding3, "PhaseTapChanger",
                powerTransformerPhaseTapChanger);

        boolean ratio0Outside1 = false;
        boolean ratio0Outside2 = false;
        boolean ratio0Outside3 = false;
        boolean ratioPhaseOutside1 = true;
        boolean ratioPhaseOutside2 = false;
        boolean ratioPhaseOutside3 = false;

        TapChanger ratioTapChanger1 = getRatioTapChanger(rtc1, winding1.get(CgmesNames.TERMINAL));
        TapChanger ratioTapChanger2 = getRatioTapChanger(rtc2, winding2.get(CgmesNames.TERMINAL));
        TapChanger ratioTapChanger3 = getRatioTapChanger(rtc3, winding3.get(CgmesNames.TERMINAL));
        TapChanger phaseTapChanger1 = getPhaseTapChanger(ptc1, winding1.get(CgmesNames.TERMINAL),
                winding1.asDouble(ratedU), winding1.asDouble("x"));
        TapChanger phaseTapChanger2 = getPhaseTapChanger(ptc2, winding2.get(CgmesNames.TERMINAL),
                winding2.asDouble(ratedU), winding2.asDouble("x"));
        TapChanger phaseTapChanger3 = getPhaseTapChanger(ptc3, winding3.get(CgmesNames.TERMINAL),
                winding3.asDouble(ratedU), winding3.asDouble("x"));

        T3xRotate t3xRotate = rotateThreeWindingsTransformer(winding1, ratioTapChanger1,
                phaseTapChanger1, ratio0Outside1, ratioPhaseOutside1,
                winding2, ratioTapChanger2, phaseTapChanger2, ratio0Outside2, ratioPhaseOutside2,
                winding3, ratioTapChanger3, phaseTapChanger3, ratio0Outside3, ratioPhaseOutside3);
        winding1 = t3xRotate.winding1;
        this.ratioTapChanger1 = t3xRotate.rtc1;
        this.phaseTapChanger1 = t3xRotate.ptc1;
        winding2 = t3xRotate.winding2;
        this.ratioTapChanger2 = t3xRotate.rtc2;
        this.phaseTapChanger2 = t3xRotate.ptc2;
        winding3 = t3xRotate.winding3;
        this.ratioTapChanger3 = t3xRotate.rtc3;
        this.phaseTapChanger3 = t3xRotate.ptc3;
        this.ratio0Outside2 = t3xRotate.ratio0Outside2;
        this.ratio0Outside3 = t3xRotate.ratio0Outside3;
        this.ratioPhaseOutside1 = t3xRotate.ratioPhaseOutside1;
        this.ratioPhaseOutside2 = t3xRotate.ratioPhaseOutside2;
        this.ratioPhaseOutside3 = t3xRotate.ratioPhaseOutside3;

        r1 = winding1.asDouble("r");
        x1 = winding1.asDouble("x");
        g11 = winding1.asDouble("g", 0);
        b11 = winding1.asDouble("b");
        g12 = 0.0;
        b12 = 0.0;
        r2 = winding2.asDouble("r");
        x2 = winding2.asDouble("x");
        g21 = winding2.asDouble("g", 0);
        b21 = winding2.asDouble("b");
        g22 = 0.0;
        b22 = 0.0;
        r3 = winding3.asDouble("r");
        x3 = winding3.asDouble("x");
        g31 = winding3.asDouble("g", 0);
        b31 = winding3.asDouble("b");
        g32 = 0.0;
        b32 = 0.0;
        ratedU1 = winding1.asDouble(ratedU);
        ratedU2 = winding2.asDouble(ratedU);
        ratedU3 = winding3.asDouble(ratedU);
        terminal1 = winding1.get(CgmesNames.TERMINAL);
        terminal2 = winding2.get(CgmesNames.TERMINAL);
        terminal3 = winding3.get(CgmesNames.TERMINAL);
    }

    @Override
    public void convert() {

        // Move initial tapChangers
        TapChangerEnd tcEnd1 = moveTapChangerEnd(ratioTapChanger1, phaseTapChanger1,
                ratioPhaseOutside1, FinalPosition.INSIDE);
        TapChanger nRatioTapChanger1 = tcEnd1.ratioTapChanger;
        TapChanger nPhaseTapChanger1 = tcEnd1.phaseTapChanger;

        TapChangerEnd tcEnd2 = moveTapChangerEnd(ratioTapChanger2, phaseTapChanger2,
                ratioPhaseOutside2, FinalPosition.OUTSIDE);
        TapChanger nRatioTapChanger2 = tcEnd2.ratioTapChanger;
        TapChanger nPhaseTapChanger2 = tcEnd2.phaseTapChanger;

        TapChangerEnd tcEnd3 = moveTapChangerEnd(ratioTapChanger3, phaseTapChanger3,
                ratioPhaseOutside3, FinalPosition.OUTSIDE);
        TapChanger nRatioTapChanger3 = tcEnd3.ratioTapChanger;
        TapChanger nPhaseTapChanger3 = tcEnd3.phaseTapChanger;

        // Fix nRatioTapChanger1 a neutral
        // Fix nPhaseTapChanger1 a neutral

        TapChanger23 neutralRatioTapChanger1 = neutral1TapChanger(nRatioTapChanger1);
        TapChanger23 neutralPhaseTapChanger1 = neutral1TapChanger(nPhaseTapChanger1);

        TapChanger ratioTapChanger2Rtc1 = neutralRatioTapChanger1.tapChanger2;
        TapChanger ratioTapChanger3Rtc1 = neutralRatioTapChanger1.tapChanger3;
        TapChanger phaseTapChanger2Rtc1 = neutralPhaseTapChanger1.tapChanger2;
        TapChanger phaseTapChanger3Rtc1 = neutralPhaseTapChanger1.tapChanger3;

        // Move outside the new tapChanger associated with tapChangers 1
        TapChanger nRatioTapChanger2Rtc1 = moveTapChangerFrom2To1(ratioTapChanger2Rtc1);
        TapChanger nPhaseTapChanger2Rtc1 = moveTapChangerFrom2To1(phaseTapChanger2Rtc1);
        TapChanger nRatioTapChanger3Rtc1 = moveTapChangerFrom2To1(ratioTapChanger3Rtc1);
        TapChanger nPhaseTapChanger3Rtc1 = moveTapChangerFrom2To1(phaseTapChanger3Rtc1);

        // Combine the two ratios at end2 and also the two phases
        TapChanger ncRatioTapChanger2Rtc1 = combineTapChangers(nRatioTapChanger2,
                nRatioTapChanger2Rtc1);
        TapChanger ncPhaseTapChanger2Rtc1 = combineTapChangers(nPhaseTapChanger2,
                nPhaseTapChanger2Rtc1);
        TapChanger ncRatioTapChanger3Rtc1 = combineTapChangers(nRatioTapChanger3,
                nRatioTapChanger3Rtc1);
        TapChanger ncPhaseTapChanger3Rtc1 = combineTapChangers(nPhaseTapChanger3,
                nPhaseTapChanger3Rtc1);

        // Delete phase at phaseTapChangers
        TapChanger ncdPhaseTapChanger2Rtc1 = deletePhaseTapChanger(ncPhaseTapChanger2Rtc1);
        TapChanger ncdPhaseTapChanger3Rtc1 = deletePhaseTapChanger(ncPhaseTapChanger3Rtc1);

        // Combine ratio and phase at end2 and end3

        TapChanger fRatioTapChanger2 = combineTapChangers(ncRatioTapChanger2Rtc1,
                ncdPhaseTapChanger2Rtc1);
        TapChanger fRatioTapChanger3 = combineTapChangers(ncRatioTapChanger3Rtc1,
                ncdPhaseTapChanger3Rtc1);

        double ratedUf = ratedU1;
        RatioConversion rc02 = identityRatioConversion(r2, x2, g21, b21, g22, b22);
        if (!ratio0Outside2) {
            double a0 = ratedUf / ratedU2;
            rc02 = moveRatioFrom2To1(a0, 0.0, r2, x2, g21, b21, g22, b22);
        }
        RatioConversion rc03 = identityRatioConversion(r3, x3, g31, b31, g32, b32);
        if (!ratio0Outside3) {
            double a0 = ratedUf / ratedU3;
            rc03 = moveRatioFrom2To1(a0, 0.0, r3, x3, g31, b31, g32, b32);
        }

        setToIidm(fRatioTapChanger2, fRatioTapChanger3, r1, x1, g11, b11, g12, b12,
                rc02.r, rc02.x, rc02.g1, rc02.b1, rc02.g2, rc02.b2,
                rc03.r, rc03.x, rc03.g1, rc03.b1, rc03.g2, rc03.b2);
    }

    private T3xRotate rotateThreeWindingsTransformer(PropertyBag winding1, TapChanger rtc1,
            TapChanger ptc1,
            boolean ratio0Outside1, boolean ratioPhaseOutside1,
            PropertyBag winding2, TapChanger rtc2, TapChanger ptc2, boolean ratio0Outside2,
            boolean ratioPhaseOutside2,
            PropertyBag winding3, TapChanger rtc3, TapChanger ptc3, boolean ratio0Outside3,
            boolean ratioPhaseOutside3) {
        T3xRotate tx3Rotate = new T3xRotate();

        boolean rotateIsAllowed = false;
        if (rotateIsAllowed && initialCandidateToRotate(rtc1, ptc1)
                && finalCandidateToRotate(rtc2, ptc2)) {

            tx3Rotate.winding1 = winding2;
            tx3Rotate.rtc1 = rtc2;
            tx3Rotate.ptc1 = ptc2;
            tx3Rotate.ratioPhaseOutside1 = ratioPhaseOutside2;

            tx3Rotate.winding2 = winding1;
            tx3Rotate.rtc2 = rtc1;
            tx3Rotate.ptc2 = ptc1;
            tx3Rotate.ratio0Outside2 = ratio0Outside1;
            tx3Rotate.ratioPhaseOutside2 = ratioPhaseOutside1;

            tx3Rotate.winding3 = winding3;
            tx3Rotate.rtc3 = rtc3;
            tx3Rotate.ptc3 = ptc3;
            tx3Rotate.ratio0Outside3 = ratio0Outside3;
            tx3Rotate.ratioPhaseOutside3 = ratioPhaseOutside3;

            return tx3Rotate;
        }
        if (rotateIsAllowed && initialCandidateToRotate(rtc1, ptc1)
                && finalCandidateToRotate(rtc3, ptc3)) {

            tx3Rotate.winding1 = winding3;
            tx3Rotate.rtc1 = rtc3;
            tx3Rotate.ptc1 = ptc3;
            tx3Rotate.ratioPhaseOutside1 = ratioPhaseOutside3;

            tx3Rotate.winding2 = winding2;
            tx3Rotate.rtc2 = rtc2;
            tx3Rotate.ptc2 = ptc2;
            tx3Rotate.ratio0Outside2 = ratio0Outside2;
            tx3Rotate.ratioPhaseOutside2 = ratioPhaseOutside2;

            tx3Rotate.winding3 = winding1;
            tx3Rotate.rtc3 = rtc1;
            tx3Rotate.ptc3 = ptc1;
            tx3Rotate.ratio0Outside3 = ratio0Outside1;
            tx3Rotate.ratioPhaseOutside3 = ratioPhaseOutside1;

            return tx3Rotate;
        }

        // Do not rotate

        tx3Rotate.winding1 = winding1;
        tx3Rotate.rtc1 = rtc1;
        tx3Rotate.ptc1 = ptc1;
        tx3Rotate.ratioPhaseOutside1 = ratioPhaseOutside1;

        tx3Rotate.winding2 = winding2;
        tx3Rotate.rtc2 = rtc2;
        tx3Rotate.ptc2 = ptc2;
        tx3Rotate.ratio0Outside2 = ratio0Outside2;
        tx3Rotate.ratioPhaseOutside2 = ratioPhaseOutside2;

        tx3Rotate.winding3 = winding3;
        tx3Rotate.rtc3 = rtc3;
        tx3Rotate.ptc3 = ptc3;
        tx3Rotate.ratio0Outside3 = ratio0Outside3;
        tx3Rotate.ratioPhaseOutside3 = ratioPhaseOutside3;

        return tx3Rotate;
    }

    private boolean initialCandidateToRotate(TapChanger rtc, TapChanger ptc) {
        switch (tapChangerType(rtc)) {
            case NULL:
                break;
            case FIXED:
            case NON_REGULATING:
            case REGULATING:
                return true;
        }
        switch (tapChangerType(ptc)) {
            case NULL:
                break;
            case FIXED:
            case NON_REGULATING:
            case REGULATING:
                return true;
        }

        return false;
    }

    private boolean finalCandidateToRotate(TapChanger rtc, TapChanger ptc) {
        switch (tapChangerType(rtc)) {
            case NULL:
                break;
            case FIXED:
            case NON_REGULATING:
            case REGULATING:
                return false;
        }
        switch (tapChangerType(ptc)) {
            case NULL:
                break;
            case FIXED:
            case NON_REGULATING:
            case REGULATING:
                return false;
        }

        return true;
    }

    private TapChangerEnd moveTapChangerEnd(TapChanger ratioTapChanger, TapChanger phaseTapChanger,
            boolean ratioPhaseOutside, FinalPosition finalPosition) {
        TapChanger nRatioTapChanger = null;
        TapChanger nPhaseTapChanger = null;
        if (ratioPhaseOutside && finalPosition == FinalPosition.INSIDE) {
            nRatioTapChanger = moveTapChangerFrom1To2(ratioTapChanger);
            nPhaseTapChanger = moveTapChangerFrom1To2(phaseTapChanger);
        } else if (!ratioPhaseOutside && finalPosition == FinalPosition.OUTSIDE) {
            nRatioTapChanger = moveTapChangerFrom2To1(ratioTapChanger);
            nPhaseTapChanger = moveTapChangerFrom2To1(phaseTapChanger);

        } else {
            nRatioTapChanger = ratioTapChanger;
            nPhaseTapChanger = phaseTapChanger;
        }

        TapChangerEnd tapChangerEnd = new TapChangerEnd();
        tapChangerEnd.ratioTapChanger = nRatioTapChanger;
        tapChangerEnd.phaseTapChanger = nPhaseTapChanger;
        return tapChangerEnd;
    }

    private void setToIidm(TapChanger ratioTapChanger2, TapChanger ratioTapChanger3,
            double r1, double x1, double g11, double b11, double g12, double b12,
            double r2, double x2, double g21, double b21, double g22, double b22,
            double r3, double x3, double g31, double b31, double g32, double b32) {

        // Add shunts g1 = g11 + g21 + g22 + g31 + g33;
        // Add shunts b1 = b11 + b21 + b22 + b31 + b33;

        double g1 = g11 + g12 + g21 + g22 + g31 + g32;
        double b1 = b11 + b12 + b21 + b22 + b31 + b32;

        cleanTapChanger(ratioTapChanger2, false, false, true, true, true, true);
        cleanTapChanger(ratioTapChanger3, false, false, true, true, true, true);

        ThreeWindingsTransformerAdder txadder = substation().newThreeWindingsTransformer();
        identify(txadder);

        LegAdder<Leg1Adder> l1adder = txadder.newLeg1()
                .setR(r1)
                .setX(x1)
                .setG(g1)
                .setB(b1)
                .setRatedU(ratedU1);
        LegAdder<Leg2or3Adder> l2adder = txadder.newLeg2()
                .setR(r2)
                .setX(x2)
                .setRatedU(ratedU2);
        LegAdder<Leg2or3Adder> l3adder = txadder.newLeg3()
                .setR(r3)
                .setX(x3)
                .setRatedU(ratedU3);
        connect(l1adder, 1);
        connect(l2adder, 2);
        connect(l3adder, 3);
        l1adder.add();
        l2adder.add();
        l3adder.add();
        ThreeWindingsTransformer tx = txadder.add();

        convertedTerminals(
                tx.getLeg1().getTerminal(),
                tx.getLeg2().getTerminal(),
                tx.getLeg3().getTerminal());

        setToIidmRatioTapChanger(ratioTapChanger2, tx, terminal2);
        setToIidmRatioTapChanger(ratioTapChanger3, tx, terminal3);
    }

    private void setToIidmRatioTapChanger(TapChanger rtc, Connectable<?> tx, String rtcTerminal) {
        if (rtc == null) {
            return;
        }
        boolean isRegulating = rtc.isRegulating();
        String terminal = rtc.getRegulationTerminal();
        Terminal regulationTerminal = terminal(tx, terminal);
        double regulationValue = rtc.getRegulationValue();
        boolean isLoadTapChangingCapabilities = rtc.isLoadTapChangingCapabilities();
        int lowStep = rtc.getLowTapPosition();
        int position = rtc.getTapPosition();
        int highStep = lowStep + rtc.getSteps().size() - 1;
        if (position < lowStep || position > highStep) {
            return;
        }
        RatioTapChangerAdder rtca = newRatioTapChanger(tx, rtcTerminal);
        rtca.setLowTapPosition(lowStep).setTapPosition((int) position)
                .setLoadTapChangingCapabilities(isLoadTapChangingCapabilities)
                .setRegulating(isRegulating).setRegulationTerminal(regulationTerminal)
                .setTargetV(regulationValue);
        rtc.getSteps().forEach(step -> {
            double ratio0 = step.getRatio();
            double r0 = step.getR();
            double x0 = step.getX();
            double b0 = step.getB1();
            double g0 = step.getG1();
            rtca.beginStep()
                    .setRho(1 / ratio0)
                    .setR(r0)
                    .setX(x0)
                    .setB(b0)
                    .setG(g0)
                    .endStep();
        });
        rtca.add();
    }

    @Override
    protected RatioTapChangerAdder newRatioTapChanger(Connectable<?> tx, String terminal) {
        if (terminal1.equals(terminal)) {
            // No supported in IIDM model
            return null;
        } else if (terminal2.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg2().newRatioTapChanger();
        } else if (terminal3.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg3().newRatioTapChanger();
        }
        return null;
    }

    @Override
    protected PhaseTapChangerAdder newPhaseTapChanger(Connectable<?> tx) {
        return null;
    }

    @Override
    protected Terminal terminal(Connectable<?> tx, String terminal) {
        if (terminal1.equals(terminal)) {
            // invalid
        } else if (terminal2.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg2().getTerminal();
        } else if (terminal3.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg3().getTerminal();
        }
        return null;
    }

    static class T3xRotate {
        PropertyBag winding1;
        TapChanger  rtc1;
        TapChanger  ptc1;
        PropertyBag winding2;
        TapChanger  rtc2;
        TapChanger  ptc2;
        PropertyBag winding3;
        TapChanger  rtc3;
        TapChanger  ptc3;
        boolean     ratio0Outside2;
        boolean     ratio0Outside3;
        boolean     ratioPhaseOutside1;
        boolean     ratioPhaseOutside2;
        boolean     ratioPhaseOutside3;
    }

    static class TapChangerEnd {
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
    }

    private final double      r1;
    private final double      x1;
    private final double      b11;
    private final double      g11;
    private final double      b12;
    private final double      g12;
    private final double      r2;
    private final double      x2;
    private final double      b21;
    private final double      g21;
    private final double      b22;
    private final double      g22;
    private final double      r3;
    private final double      x3;
    private final double      b31;
    private final double      g31;
    private final double      b32;
    private final double      g32;
    private final double      ratedU1;
    private final double      ratedU2;
    private final double      ratedU3;
    private final String      terminal1;
    private final String      terminal2;
    private final String      terminal3;
    private final PropertyBag rtc1;
    private final PropertyBag rtc2;
    private final PropertyBag rtc3;
    private final PropertyBag ptc1;
    private final PropertyBag ptc2;
    private final PropertyBag ptc3;
    private final TapChanger  ratioTapChanger1;
    private final TapChanger  ratioTapChanger2;
    private final TapChanger  ratioTapChanger3;
    private final TapChanger  phaseTapChanger1;
    private final TapChanger  phaseTapChanger2;
    private final TapChanger  phaseTapChanger3;
    private final boolean     ratio0Outside2;
    private final boolean     ratio0Outside3;
    private final boolean     ratioPhaseOutside1;
    private final boolean     ratioPhaseOutside2;
    private final boolean     ratioPhaseOutside3;
}
