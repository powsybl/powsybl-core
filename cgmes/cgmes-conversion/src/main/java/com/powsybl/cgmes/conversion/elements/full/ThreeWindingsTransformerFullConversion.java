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
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class ThreeWindingsTransformerFullConversion extends AbstractTransformerFullConversion {

    private enum FinalPosition {
        NETWORK_SIDE, STAR_BUS_SIDE
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

        this.ratio0Outside1 = context.config().xfmr3Ratio0Outside();
        // this.ratio0Outside2 = context.config().xfmr3Ratio0Outside();
        this.ratio0Outside3 = context.config().xfmr3Ratio0Outside();
        this.ratioPhaseOutside1 = context.config().xfmr3RatioPhaseOutside();
        this.ratioPhaseOutside2 = context.config().xfmr3RatioPhaseOutside();
        this.ratioPhaseOutside3 = context.config().xfmr3RatioPhaseOutside();

        this.ratioTapChanger1 = getRatioTapChanger(rtc1, winding1.get(CgmesNames.TERMINAL));
        this.ratioTapChanger2 = getRatioTapChanger(rtc2, winding2.get(CgmesNames.TERMINAL));
        this.ratioTapChanger3 = getRatioTapChanger(rtc3, winding3.get(CgmesNames.TERMINAL));
        this.phaseTapChanger1 = getPhaseTapChanger(ptc1, winding1.get(CgmesNames.TERMINAL),
                winding1.asDouble(ratedU), winding1.asDouble("x"));
        this.phaseTapChanger2 = getPhaseTapChanger(ptc2, winding2.get(CgmesNames.TERMINAL),
                winding2.asDouble(ratedU), winding2.asDouble("x"));
        this.phaseTapChanger3 = getPhaseTapChanger(ptc3, winding3.get(CgmesNames.TERMINAL),
                winding3.asDouble(ratedU), winding3.asDouble("x"));

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

        // Regulating control filtering

        TapChanger3 tapChanger3 = filterRatioPhaseRegulatingControl(ratioTapChanger1, phaseTapChanger1,
                ratioTapChanger2, phaseTapChanger2, ratioTapChanger3, phaseTapChanger3);

        TapChanger xRatioTapChanger1 = tapChanger3.ratioTapChanger1;
        TapChanger xPhaseTapChanger1 = tapChanger3.phaseTapChanger1;
        TapChanger xRatioTapChanger2 = tapChanger3.ratioTapChanger2;
        TapChanger xPhaseTapChanger2 = tapChanger3.phaseTapChanger2;
        TapChanger xRatioTapChanger3 = tapChanger3.ratioTapChanger3;
        TapChanger xPhaseTapChanger3 = tapChanger3.phaseTapChanger3;

        // Move initial tapChangers

        TapChangerEnd tcEnd1 = moveTapChangerEnd(xRatioTapChanger1, xPhaseTapChanger1,
                ratioPhaseOutside1, FinalPosition.NETWORK_SIDE);
        TapChanger fRatioTapChanger1 = tcEnd1.ratioTapChanger;
        TapChanger fPhaseTapChanger1 = tcEnd1.phaseTapChanger;

        TapChangerEnd tcEnd2 = moveTapChangerEnd(xRatioTapChanger2, xPhaseTapChanger2,
                ratioPhaseOutside2, FinalPosition.NETWORK_SIDE);
        TapChanger fRatioTapChanger2 = tcEnd2.ratioTapChanger;
        TapChanger fPhaseTapChanger2 = tcEnd2.phaseTapChanger;

        TapChangerEnd tcEnd3 = moveTapChangerEnd(xRatioTapChanger3, xPhaseTapChanger3,
                ratioPhaseOutside3, FinalPosition.NETWORK_SIDE);
        TapChanger fRatioTapChanger3 = tcEnd3.ratioTapChanger;
        TapChanger fPhaseTapChanger3 = tcEnd3.phaseTapChanger;

        // ratio0 ratedUf and ratedU2 same nominal voltage

        double ratedUf = ratedU2;
        RatioConversion rc01 = identityRatioConversion(r1, x1, g11, b11, g12, b12);
        if (!ratio0Outside1) {
            double a0 = ratedUf / ratedU1;
            rc01 = moveRatioFrom2To1(a0, 0.0, r1, x1, g11, b11, g12, b12);
        }
        RatioConversion rc03 = identityRatioConversion(r3, x3, g31, b31, g32, b32);
        if (!ratio0Outside3) {
            double a0 = ratedUf / ratedU3;
            rc03 = moveRatioFrom2To1(a0, 0.0, r3, x3, g31, b31, g32, b32);
        }

        setToIidm(fRatioTapChanger1, fPhaseTapChanger1, fRatioTapChanger2, fPhaseTapChanger2,
                fRatioTapChanger3, fPhaseTapChanger3,
                rc01.r, rc01.x, rc01.g1, rc01.b1, rc01.g2, rc01.b2,
                r2, x2, g21, b21, g22, b22,
                rc03.r, rc03.x, rc03.g1, rc03.b1, rc03.g2, rc03.b2);
    }

    private TapChangerEnd moveTapChangerEnd(TapChanger ratioTapChanger, TapChanger phaseTapChanger,
            boolean ratioPhaseOutside, FinalPosition finalPosition) {
        TapChanger nRatioTapChanger = null;
        TapChanger nPhaseTapChanger = null;
        if (ratioPhaseOutside && finalPosition == FinalPosition.STAR_BUS_SIDE) {
            nRatioTapChanger = moveTapChangerFrom1To2(ratioTapChanger);
            nPhaseTapChanger = moveTapChangerFrom1To2(phaseTapChanger);
        } else if (!ratioPhaseOutside && finalPosition == FinalPosition.NETWORK_SIDE) {
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

    private TapChanger3 filterRatioPhaseRegulatingControl(TapChanger ratioTapChanger1,
            TapChanger phaseTapChanger1, TapChanger ratioTapChanger2, TapChanger phaseTapChanger2,
            TapChanger ratioTapChanger3, TapChanger phaseTapChanger3) {

        TapChanger3 tapChanger3 = new TapChanger3();

        if (tapChangerType(ratioTapChanger1) == TapChangerType.REGULATING) {
            tapChanger3.ratioTapChanger1 = ratioTapChanger1;
            tapChanger3.phaseTapChanger1 = fixTapChangerRegulation(phaseTapChanger1);
            tapChanger3.ratioTapChanger2 = fixTapChangerRegulation(ratioTapChanger2);
            tapChanger3.phaseTapChanger2 = fixTapChangerRegulation(phaseTapChanger2);
            tapChanger3.ratioTapChanger3 = fixTapChangerRegulation(ratioTapChanger3);
            tapChanger3.phaseTapChanger3 = fixTapChangerRegulation(phaseTapChanger3);
        } else if (tapChangerType(phaseTapChanger1) == TapChangerType.REGULATING) {
            tapChanger3.ratioTapChanger1 = ratioTapChanger1;
            tapChanger3.phaseTapChanger1 = phaseTapChanger1;
            tapChanger3.ratioTapChanger2 = fixTapChangerRegulation(ratioTapChanger2);
            tapChanger3.phaseTapChanger2 = fixTapChangerRegulation(phaseTapChanger2);
            tapChanger3.ratioTapChanger3 = fixTapChangerRegulation(ratioTapChanger3);
            tapChanger3.phaseTapChanger3 = fixTapChangerRegulation(phaseTapChanger3);
        } else if (tapChangerType(ratioTapChanger2) == TapChangerType.REGULATING) {
            tapChanger3.ratioTapChanger1 = ratioTapChanger1;
            tapChanger3.phaseTapChanger1 = phaseTapChanger1;
            tapChanger3.ratioTapChanger2 = ratioTapChanger2;
            tapChanger3.phaseTapChanger2 = fixTapChangerRegulation(phaseTapChanger2);
            tapChanger3.ratioTapChanger3 = fixTapChangerRegulation(ratioTapChanger3);
            tapChanger3.phaseTapChanger3 = fixTapChangerRegulation(phaseTapChanger3);
        } else if (tapChangerType(phaseTapChanger2) == TapChangerType.REGULATING) {
            tapChanger3.ratioTapChanger1 = ratioTapChanger1;
            tapChanger3.phaseTapChanger1 = phaseTapChanger1;
            tapChanger3.ratioTapChanger2 = ratioTapChanger2;
            tapChanger3.phaseTapChanger2 = phaseTapChanger2;
            tapChanger3.ratioTapChanger3 = fixTapChangerRegulation(ratioTapChanger3);
            tapChanger3.phaseTapChanger3 = fixTapChangerRegulation(phaseTapChanger3);
        } else if (tapChangerType(ratioTapChanger3) == TapChangerType.REGULATING) {
            tapChanger3.ratioTapChanger1 = ratioTapChanger1;
            tapChanger3.phaseTapChanger1 = phaseTapChanger1;
            tapChanger3.ratioTapChanger2 = ratioTapChanger2;
            tapChanger3.phaseTapChanger2 = phaseTapChanger2;
            tapChanger3.ratioTapChanger3 = ratioTapChanger3;
            tapChanger3.phaseTapChanger3 = fixTapChangerRegulation(phaseTapChanger3);
        } else {
            tapChanger3.ratioTapChanger1 = ratioTapChanger1;
            tapChanger3.phaseTapChanger1 = phaseTapChanger1;
            tapChanger3.ratioTapChanger2 = ratioTapChanger2;
            tapChanger3.phaseTapChanger2 = phaseTapChanger2;
            tapChanger3.ratioTapChanger3 = ratioTapChanger3;
            tapChanger3.phaseTapChanger3 = phaseTapChanger3;
        }

        return tapChanger3;
    }

    private void setToIidm(TapChanger ratioTapChanger1, TapChanger phaseTapChanger1,
            TapChanger ratioTapChanger2, TapChanger phaseTapChanger2,
            TapChanger ratioTapChanger3, TapChanger phaseTapChanger3,
            double r1, double x1, double g11, double b11, double g12, double b12,
            double r2, double x2, double g21, double b21, double g22, double b22,
            double r3, double x3, double g31, double b31, double g32, double b32) {

        // JAM_TODO Ajustar shunts y TapChanger al nuevo modelo propuesto
        // Add shunts g1 = g11 + g21 + g22 + g31 + g33;
        // Add shunts b1 = b11 + b21 + b22 + b31 + b33;

        double g1 = g11 + g12 + g21 + g22 + g31 + g32;
        double b1 = b11 + b12 + b21 + b22 + b31 + b32;

        cleanTapChanger(ratioTapChanger2, false, false, true, true, true, true);
        cleanTapChanger(ratioTapChanger3, false, false, true, true, true, true);

        ThreeWindingsTransformerAdder txadder = substation().newThreeWindingsTransformer();
        identify(txadder);

        LegAdder<LegAdder> l1adder = txadder.newLeg1()
                .setR(r1)
                .setX(x1)
                .setG1(g1)
                .setB1(b1)
                .setG2(0.0)
                .setB2(0.0)
                .setRatedU(ratedU1);
        LegAdder<LegAdder> l2adder = txadder.newLeg2()
                .setR(r2)
                .setX(x2)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setRatedU(ratedU2);
        LegAdder<LegAdder> l3adder = txadder.newLeg3()
                .setR(r3)
                .setX(x3)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
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

    protected PhaseTapChangerAdder newPhaseTapChanger(Connectable<?> tx) {
        return null;
    }

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

    static class TapChangerEnd {
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
    }

    static class TapChanger3 {
        TapChanger ratioTapChanger1;
        TapChanger phaseTapChanger1;
        TapChanger ratioTapChanger2;
        TapChanger phaseTapChanger2;
        TapChanger ratioTapChanger3;
        TapChanger phaseTapChanger3;
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
    private final boolean     ratio0Outside1;
    // private final boolean ratio0Outside2;
    private final boolean     ratio0Outside3;
    private final boolean     ratioPhaseOutside1;
    private final boolean     ratioPhaseOutside2;
    private final boolean     ratioPhaseOutside3;
}
