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

    public ThreeWindingsTransformerFullConversion(PropertyBags ends,
            Map<String, PropertyBag> powerTransformerRatioTapChanger,
            Map<String, PropertyBag> powerTransformerPhaseTapChanger, Context context) {
        super("PowerTransformer", ends, context);
        PropertyBag winding1 = ends.get(0);
        PropertyBag winding2 = ends.get(1);
        PropertyBag winding3 = ends.get(2);
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
        String ratedU = "ratedU";
        ratedU1 = winding1.asDouble(ratedU);
        ratedU2 = winding2.asDouble(ratedU);
        ratedU3 = winding3.asDouble(ratedU);
        terminal1 = winding1.get(CgmesNames.TERMINAL);
        terminal2 = winding2.get(CgmesNames.TERMINAL);
        terminal3 = winding3.get(CgmesNames.TERMINAL);
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
        ratio0Outside2 = false;
        ratio0Outside3 = false;
        ratioPhaseOutside1 = true;
        ratioPhaseOutside2 = true;
        ratioPhaseOutside3 = true;
    }

    @Override
    public void convert() {

        TapChanger ratioTapChanger1 = getRatioTapChanger(rtc1, terminal1);
        TapChanger ratioTapChanger2 = getRatioTapChanger(rtc2, terminal2);
        TapChanger ratioTapChanger3 = getRatioTapChanger(rtc3, terminal3);
        TapChanger phaseTapChanger1 = getPhaseTapChanger(ptc1, terminal1, ratedU1, x1);
        TapChanger phaseTapChanger2 = getPhaseTapChanger(ptc2, terminal2, ratedU2, x2);
        TapChanger phaseTapChanger3 = getPhaseTapChanger(ptc3, terminal3, ratedU3, x3);

        // Move initial tapChangers
        TapChanger nRatioTapChanger1 = null;
        TapChanger nPhaseTapChanger1 = null;
        if (ratioPhaseOutside1) {
            nRatioTapChanger1 = moveTapChangerFrom1To2(ratioTapChanger1);
            nPhaseTapChanger1 = moveTapChangerFrom1To2(phaseTapChanger1);
        } else {
            nRatioTapChanger1 = ratioTapChanger1;
            nPhaseTapChanger1 = phaseTapChanger1;
        }

        TapChanger nRatioTapChanger2 = null;
        TapChanger nPhaseTapChanger2 = null;
        if (ratioPhaseOutside2) {
            nRatioTapChanger2 = ratioTapChanger2;
            nPhaseTapChanger2 = phaseTapChanger2;
        } else {
            nRatioTapChanger2 = moveTapChangerFrom2To1(ratioTapChanger2);
            nPhaseTapChanger2 = moveTapChangerFrom2To1(phaseTapChanger2);
        }

        TapChanger nRatioTapChanger3 = null;
        TapChanger nPhaseTapChanger3 = null;
        if (ratioPhaseOutside3) {
            nRatioTapChanger3 = ratioTapChanger3;
            nPhaseTapChanger3 = phaseTapChanger3;
        } else {
            nRatioTapChanger3 = moveTapChangerFrom2To1(ratioTapChanger3);
            nPhaseTapChanger3 = moveTapChangerFrom2To1(phaseTapChanger3);
        }

        // Fix nRatioTapChanger1 a neutral
        // Fix nPhaseTapChanger1 a neutral

        TapChanger23 neutralRatioTapChanger1 = neutral1TapChanger(nRatioTapChanger1);
        TapChanger23 neutralPhaseTapChanger1 = neutral1TapChanger(nPhaseTapChanger1);

        TapChanger ratioTapChanger2Rtc1 = neutralRatioTapChanger1.tapChanger2;
        TapChanger ratioTapChanger3Rtc1 = neutralRatioTapChanger1.tapChanger3;
        TapChanger phaseTapChanger2Rtc1 = neutralPhaseTapChanger1.tapChanger2;
        TapChanger phaseTapChanger3Rtc1 = neutralPhaseTapChanger1.tapChanger3;

        // Move outside the new tapChanger associated a tapChangers 1
        TapChanger nRatioTapChanger2Rtc1 = moveTapChangerFrom2To1(ratioTapChanger2Rtc1);
        TapChanger nPhaseTapChanger2Rtc1 = moveTapChangerFrom2To1(phaseTapChanger2Rtc1);
        TapChanger nRatioTapChanger3Rtc1 = moveTapChangerFrom2To1(ratioTapChanger3Rtc1);
        TapChanger nPhaseTapChanger3Rtc1 = moveTapChangerFrom2To1(phaseTapChanger3Rtc1);

        // Combine
        TapChanger ncRatioTapChanger2Rtc1 = combineTapChangers(nRatioTapChanger2,
                nRatioTapChanger2Rtc1);
        TapChanger ncPhaseTapChanger2Rtc1 = combineTapChangers(nPhaseTapChanger2,
                nPhaseTapChanger2Rtc1);
        TapChanger ncRatioTapChanger3Rtc1 = combineTapChangers(nRatioTapChanger3,
                nRatioTapChanger3Rtc1);
        TapChanger ncPhaseTapChanger3Rtc1 = combineTapChangers(nPhaseTapChanger3,
                nPhaseTapChanger3Rtc1);

        // Delete phase at phaseTapChagers
        TapChanger ncdPhaseTapChanger2Rtc1 = deletePhaseTapChanger(ncPhaseTapChanger2Rtc1);
        TapChanger ncdPhaseTapChanger3Rtc1 = deletePhaseTapChanger(ncPhaseTapChanger3Rtc1);

        // Combine

        TapChanger fRatioTapChanger2 = combineTapChangers(ncRatioTapChanger2Rtc1,
                ncdPhaseTapChanger2Rtc1);
        TapChanger fRatioTapChanger3 = combineTapChangers(ncRatioTapChanger3Rtc1,
                ncdPhaseTapChanger3Rtc1);

        double r1 = this.r1;
        double x1 = this.x1;
        double ratedUf = this.ratedU1;
        double r2 = this.r2;
        double x2 = this.x2;
        double g21 = this.g21;
        double b21 = this.b21;
        double g22 = this.g22;
        double b22 = this.b22;
        if (!ratio0Outside2) {
            double a0 = ratedUf / ratedU2;
            RatioConversion ratio = moveRatioFrom2To1(a0, 0.0, r2, x2, g21, b21, g22, b22);
            r2 = ratio.r;
            x2 = ratio.x;
            b21 = ratio.b1;
            g21 = ratio.g1;
            b22 = ratio.b2;
            g22 = ratio.g2;
        }
        double r3 = this.r3;
        double x3 = this.x3;
        double g31 = this.g31;
        double b31 = this.b31;
        double g32 = this.g32;
        double b32 = this.b32;
        if (!ratio0Outside3) {
            double a0 = ratedUf / ratedU3;
            RatioConversion ratio = moveRatioFrom2To1(a0, 0.0, r3, x3, g31, b31, g32, b32);
            r3 = ratio.r;
            x3 = ratio.x;
            b31 = ratio.b1;
            g31 = ratio.g1;
            b32 = ratio.b2;
            g32 = ratio.g2;
        }
        double g1 = this.g11 + this.g12 + g21 + g22 + g31 + g32;
        double b1 = this.b11 + this.b12 + b21 + b22 + b31 + b32;

        setToIidm(fRatioTapChanger2, fRatioTapChanger3, r1, x1, g1, b1, r2, x2, r3, x3);
    }

    private void setToIidm(TapChanger ratioTapChanger2, TapChanger ratioTapChanger3, double r1,
            double x1, double g1, double b1, double r2, double x2, double r3, double x3) {

        // Add shunts in g12 = g11 + g21 + g22 + g31 + g33;
        // Add shunts in b12 = b11 + b21 + b22 + b31 + b33;
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
    }

    @Override
    protected RatioTapChangerAdder newRatioTapChanger(Connectable<?> tx, String terminal) {
        if (terminal1.equals(terminal)) {
            // No supported in IIDM model
            return null;
        } else if (terminal2.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg2().newRatioTapChanger();
        } else if (terminal3.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg2().newRatioTapChanger();
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
    private final boolean     ratio0Outside2;
    private final boolean     ratio0Outside3;
    private final boolean     ratioPhaseOutside1;
    private final boolean     ratioPhaseOutside2;
    private final boolean     ratioPhaseOutside3;
}
