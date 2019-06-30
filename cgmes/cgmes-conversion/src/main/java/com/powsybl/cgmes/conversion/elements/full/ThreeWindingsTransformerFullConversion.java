package com.powsybl.cgmes.conversion.elements.full;

import java.util.Map;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
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

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public class ThreeWindingsTransformerFullConversion extends AbstractTransformerFullConversion {

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

        r1 = winding1.asDouble("r");
        x1 = winding1.asDouble("x");
        g1 = winding1.asDouble("g", 0);
        b1 = winding1.asDouble("b");
        r2 = winding2.asDouble("r");
        x2 = winding2.asDouble("x");
        g2 = winding2.asDouble("g", 0);
        b2 = winding2.asDouble("b");
        r3 = winding3.asDouble("r");
        x3 = winding3.asDouble("x");
        g3 = winding3.asDouble("g", 0);
        b3 = winding3.asDouble("b");
        ratedU1 = winding1.asDouble(ratedU);
        ratedU2 = winding2.asDouble(ratedU);
        ratedU3 = winding3.asDouble(ratedU);
        terminal1 = winding1.getId(CgmesNames.TERMINAL);
        terminal2 = winding2.getId(CgmesNames.TERMINAL);
        terminal3 = winding3.getId(CgmesNames.TERMINAL);
        phaseAngleClock1 = winding1.asInt("phaseAngleClock", 0);
        phaseAngleClock2 = winding2.asInt("phaseAngleClock", 0);
        phaseAngleClock3 = winding3.asInt("phaseAngleClock", 0);
    }

    @Override
    public void convert() {

        CgmesModel cgmesModel = load();
        InterpretedModel interpretedModel = interpret(cgmesModel, context.config());
        ConvertedModel convertedModel = convertToIidm(interpretedModel);

        setToIidm(convertedModel);
    }

    private CgmesModel load() {

        TapChanger ratioTapChanger1 = getRatioTapChanger(rtc1, terminal1);
        TapChanger ratioTapChanger2 = getRatioTapChanger(rtc2, terminal2);
        TapChanger ratioTapChanger3 = getRatioTapChanger(rtc3, terminal3);
        TapChanger phaseTapChanger1 = getPhaseTapChanger(ptc1, terminal1, ratedU1, x1);
        TapChanger phaseTapChanger2 = getPhaseTapChanger(ptc2, terminal2, ratedU2, x2);
        TapChanger phaseTapChanger3 = getPhaseTapChanger(ptc3, terminal3, ratedU3, x3);

        CgmesModel cgmesModel = new CgmesModel();
        cgmesModel.winding1.r = r1;
        cgmesModel.winding1.x = x1;
        cgmesModel.winding1.g = g1;
        cgmesModel.winding1.b = b1;
        cgmesModel.winding1.ratioTapChanger = ratioTapChanger1;
        cgmesModel.winding1.phaseTapChanger = phaseTapChanger1;
        cgmesModel.winding1.ratedU = ratedU1;
        cgmesModel.winding1.phaseAngleClock = phaseAngleClock1;
        cgmesModel.winding1.terminal = terminal1;

        cgmesModel.winding2.r = r2;
        cgmesModel.winding2.x = x2;
        cgmesModel.winding2.g = g2;
        cgmesModel.winding2.b = b2;
        cgmesModel.winding2.ratioTapChanger = ratioTapChanger2;
        cgmesModel.winding2.phaseTapChanger = phaseTapChanger2;
        cgmesModel.winding2.ratedU = ratedU2;
        cgmesModel.winding2.phaseAngleClock = phaseAngleClock2;
        cgmesModel.winding2.terminal = terminal2;

        cgmesModel.winding3.r = r3;
        cgmesModel.winding3.x = x3;
        cgmesModel.winding3.g = g3;
        cgmesModel.winding3.b = b3;
        cgmesModel.winding3.ratioTapChanger = ratioTapChanger3;
        cgmesModel.winding3.phaseTapChanger = phaseTapChanger3;
        cgmesModel.winding3.ratedU = ratedU3;
        cgmesModel.winding3.phaseAngleClock = phaseAngleClock3;
        cgmesModel.winding3.terminal = terminal3;

        return cgmesModel;
    }

    private InterpretedModel interpret(CgmesModel cgmesModel, Conversion.Config alternative) {

        InterpretedModel interpretedModel = new InterpretedModel();
        double ratedUf = ratedUfAlternative(cgmesModel, alternative);
        interpretedModel.ratedUf = ratedUf;

        interpretWinding(cgmesModel.winding1, alternative, ratedUf, interpretedModel.winding1);
        interpretWinding(cgmesModel.winding2, alternative, ratedUf, interpretedModel.winding2);
        interpretWinding(cgmesModel.winding3, alternative, ratedUf, interpretedModel.winding3);

        return interpretedModel;
    }

    private void interpretWinding(CgmesWinding cgmesModelWinding, Conversion.Config alternative,
        double ratedUf, InterpretedWinding interpretedModelWinding) {

        TapChanger22 windingInterpretedTapChanger = ratioPhaseAlternative(cgmesModelWinding, alternative);
        Shunt22 windingInterpretedShunt = shuntAlternative(cgmesModelWinding, alternative);
        PhaseAngleClock02 windingInterpretedClock = phaseAngleClockAlternative(cgmesModelWinding, alternative);
        boolean windingRatio0AtEnd2 = ratio0Alternative(cgmesModelWinding, alternative);

        interpretedModelWinding.r = cgmesModelWinding.r;
        interpretedModelWinding.x = cgmesModelWinding.x;
        interpretedModelWinding.end1.g = windingInterpretedShunt.g1;
        interpretedModelWinding.end1.b = windingInterpretedShunt.b1;
        interpretedModelWinding.end1.ratioTapChanger = windingInterpretedTapChanger.ratioTapChanger1;
        interpretedModelWinding.end1.phaseTapChanger = windingInterpretedTapChanger.phaseTapChanger1;
        interpretedModelWinding.end1.phaseAngleClock = windingInterpretedClock.phaseAngleClock1;
        interpretedModelWinding.end1.ratedU = cgmesModelWinding.ratedU;
        interpretedModelWinding.end1.terminal = cgmesModelWinding.terminal;

        interpretedModelWinding.end2.g = windingInterpretedShunt.g2;
        interpretedModelWinding.end2.b = windingInterpretedShunt.b2;
        interpretedModelWinding.end2.ratioTapChanger = windingInterpretedTapChanger.ratioTapChanger2;
        interpretedModelWinding.end2.phaseTapChanger = windingInterpretedTapChanger.phaseTapChanger2;
        interpretedModelWinding.end2.phaseAngleClock = windingInterpretedClock.phaseAngleClock2;

        interpretedModelWinding.ratio0AtEnd2 = windingRatio0AtEnd2;
    }

    private TapChanger22 ratioPhaseAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        TapChanger ratioTapChanger1 = null;
        TapChanger phaseTapChanger1 = null;
        TapChanger ratioTapChanger2 = null;
        TapChanger phaseTapChanger2 = null;

        if (alternative.isXfmr3RatioPhaseNetworkSide()) {
            ratioTapChanger1 = cgmesWinding.ratioTapChanger;
            phaseTapChanger1 = cgmesWinding.phaseTapChanger;
        } else {
            ratioTapChanger2 = cgmesWinding.ratioTapChanger;
            phaseTapChanger2 = cgmesWinding.phaseTapChanger;
        }

        TapChanger22 tapChanger22 = new TapChanger22();
        tapChanger22.ratioTapChanger1 = ratioTapChanger1;
        tapChanger22.phaseTapChanger1 = phaseTapChanger1;
        tapChanger22.ratioTapChanger2 = ratioTapChanger2;
        tapChanger22.phaseTapChanger2 = phaseTapChanger2;

        return tapChanger22;
    }

    private Shunt22 shuntAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        double g1 = 0.0;
        double b1 = 0.0;
        double g2 = 0.0;
        double b2 = 0.0;
        if (alternative.isXfmr3ShuntNetworkSide()) {
            g1 = cgmesWinding.g;
            b1 = cgmesWinding.b;
        } else if (alternative.isXfmr3ShuntStarBusSide()) {
            g2 = cgmesWinding.g;
            b2 = cgmesWinding.b;
        } else {
            g1 = cgmesWinding.g * 0.5;
            b1 = cgmesWinding.b * 0.5;
            g2 = cgmesWinding.g * 0.5;
            b2 = cgmesWinding.b * 0.5;
        }

        Shunt22 shunt22 = new Shunt22();
        shunt22.g1 = g1;
        shunt22.b1 = b1;
        shunt22.g2 = g2;
        shunt22.b2 = b2;

        return shunt22;
    }

    private PhaseAngleClock02 phaseAngleClockAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        int phaseAngleClock1 = 0;
        int phaseAngleClock2 = 0;

        if (cgmesWinding.phaseAngleClock != 0) {
            if (alternative.isXfmr3PhaseAngleClockNetworkSide()) {
                phaseAngleClock1 = cgmesWinding.phaseAngleClock;
            } else if (alternative.isXfmr3PhaseAngleClockStarBusSide()) {
                phaseAngleClock2 = cgmesWinding.phaseAngleClock;
            }
        }

        PhaseAngleClock02 phaseAngleClock02 = new PhaseAngleClock02();
        phaseAngleClock02.phaseAngleClock1 = phaseAngleClock1;
        phaseAngleClock02.phaseAngleClock2 = phaseAngleClock2;

        return phaseAngleClock02;
    }

    private double ratedUfAlternative(CgmesModel cgmesModel, Conversion.Config alternative) {

        double ratedUf = 1.0;

        if (alternative.isXfmr3Ratio0End1()) {
            ratedUf = cgmesModel.winding1.ratedU;
        } else if (alternative.isXfmr3Ratio0End2()) {
            ratedUf = cgmesModel.winding2.ratedU;
        } else if (alternative.isXfmr3Ratio0End3()) {
            ratedUf = cgmesModel.winding3.ratedU;
        }
        return ratedUf;
    }

    private boolean ratio0Alternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {

        boolean ratio0AtEnd2;
        if (alternative.isXfmr3Ratio0StarBusSide()) {
            ratio0AtEnd2 = true;
        } else {
            ratio0AtEnd2 = false;
        }
        return ratio0AtEnd2;
    }

    private ConvertedModel convertToIidm(InterpretedModel interpretedModel) {

        double ratedUf = interpretedModel.ratedUf;
        ConvertedModel convertedModel = new ConvertedModel();

        convertToIidmWinding(interpretedModel.winding1, convertedModel.winding1, ratedUf);
        convertToIidmWinding(interpretedModel.winding2, convertedModel.winding2, ratedUf);
        convertToIidmWinding(interpretedModel.winding3, convertedModel.winding3, ratedUf);

        convertedModel.ratedUf = ratedUf;

        return convertedModel;
    }

    private void convertToIidmWinding(InterpretedWinding interpretedWinding, ConvertedWinding convertedWinding,
        double ratedUf) {
        TapChangerWinding windingTapChanger = moveCombineTapChangerWinding(interpretedWinding);

        RatioConversion windingRc0 = rc0Winding(interpretedWinding, ratedUf);

        convertedWinding.r = windingRc0.r;
        convertedWinding.x = windingRc0.x;
        convertedWinding.end1.g = windingRc0.g1;
        convertedWinding.end1.b = windingRc0.b1;
        convertedWinding.end1.ratioTapChanger = windingTapChanger.ratioTapChanger;
        convertedWinding.end1.phaseTapChanger = windingTapChanger.phaseTapChanger;
        convertedWinding.end1.phaseAngleClock = interpretedWinding.end1.phaseAngleClock;
        convertedWinding.end1.ratedU = interpretedWinding.end1.ratedU;
        convertedWinding.end1.terminal = interpretedWinding.end1.terminal;
        convertedWinding.end2.g = windingRc0.g2;
        convertedWinding.end2.b = windingRc0.b2;
        convertedWinding.end2.phaseAngleClock = interpretedWinding.end2.phaseAngleClock;
    }

    private TapChangerWinding moveCombineTapChangerWinding(InterpretedWinding interpretedWinding) {

        TapChanger nRatioTapChanger = moveTapChangerFrom2To1(interpretedWinding.end2.ratioTapChanger);
        TapChanger nPhaseTapChanger = moveTapChangerFrom2To1(interpretedWinding.end2.phaseTapChanger);

        TapChanger cRatioTapChanger = combineTapChangers(interpretedWinding.end1.ratioTapChanger, nRatioTapChanger);
        TapChanger cPhaseTapChanger = combineTapChangers(interpretedWinding.end1.phaseTapChanger, nPhaseTapChanger);

        TapChangerWinding tapChangerWinding = new TapChangerWinding();
        tapChangerWinding.ratioTapChanger = cRatioTapChanger;
        tapChangerWinding.phaseTapChanger = cPhaseTapChanger;
        return tapChangerWinding;
    }

    private RatioConversion rc0Winding(InterpretedWinding interpretedWinding, double ratedUf) {
        RatioConversion rc0;
        if (interpretedWinding.ratio0AtEnd2) {
            double a0 = ratedUf / interpretedWinding.end1.ratedU;
            rc0 = moveRatioFrom2To1(a0, 0.0, interpretedWinding.r, interpretedWinding.x,
                interpretedWinding.end1.g, interpretedWinding.end1.b,
                interpretedWinding.end2.g, interpretedWinding.end2.b);
        } else {
            rc0 = identityRatioConversion(interpretedWinding.r, interpretedWinding.x,
                interpretedWinding.end1.g, interpretedWinding.end1.b,
                interpretedWinding.end2.g, interpretedWinding.end2.b);
        }

        return rc0;

    }

    private void setToIidm(ConvertedModel convertedModel) {
        ThreeWindingsTransformerAdder txadder = substation().newThreeWindingsTransformer()
            .setRatedU0(convertedModel.ratedUf);
        identify(txadder);

        LegAdder l1adder = txadder.newLeg1();
        setToIidmWindingAdder(convertedModel.winding1, l1adder);
        connect(l1adder, 1);
        l1adder.add();

        LegAdder l2adder = txadder.newLeg2();
        setToIidmWindingAdder(convertedModel.winding2, l2adder);
        connect(l2adder, 2);
        l2adder.add();

        LegAdder l3adder = txadder.newLeg3();
        setToIidmWindingAdder(convertedModel.winding3, l3adder);
        connect(l3adder, 3);
        l3adder.add();

        ThreeWindingsTransformer tx = txadder.add();

        convertedTerminals(
            tx.getLeg1().getTerminal(),
            tx.getLeg2().getTerminal(),
            tx.getLeg3().getTerminal());

        setToIidmWindingTapChanger(convertedModel, convertedModel.winding1, tx);
        setToIidmWindingTapChanger(convertedModel, convertedModel.winding2, tx);
        setToIidmWindingTapChanger(convertedModel, convertedModel.winding3, tx);
    }

    private void setToIidmWindingAdder(ConvertedWinding convertedModelWinding,
        LegAdder ladder) {

        ladder.setR(convertedModelWinding.r)
            .setX(convertedModelWinding.x)
            .setG(convertedModelWinding.end1.g + convertedModelWinding.end2.g)
            .setB(convertedModelWinding.end1.b + convertedModelWinding.end2.b)
            .setPhaseAngleClock1(convertedModelWinding.end1.phaseAngleClock)
            .setPhaseAngleClock2(convertedModelWinding.end2.phaseAngleClock)
            .setRatedU(convertedModelWinding.end1.ratedU);
    }

    private void setToIidmWindingTapChanger(ConvertedModel convertedModel, ConvertedWinding convertedModelWinding,
        ThreeWindingsTransformer tx) {
        setToIidmRatioTapChanger(convertedModel, convertedModelWinding, tx);
        setToIidmPhaseTapChanger(convertedModel, convertedModelWinding, tx);
    }

    private void setToIidmRatioTapChanger(ConvertedModel convertedModel, ConvertedWinding convertedWinding,
        Connectable<?> tx) {
        TapChanger rtc = convertedWinding.end1.ratioTapChanger;
        if (rtc == null) {
            return;
        }

        if (rtc.getTapPosition() < rtc.getLowTapPosition() || rtc.getTapPosition() > rtc.getHighTapPosition()) {
            return;
        }

        RatioTapChangerAdder rtca = newRatioTapChanger(convertedModel, tx, convertedWinding.end1.terminal);
        setToIidmRatioTapChanger(rtc, rtca);
    }

    private void setToIidmPhaseTapChanger(ConvertedModel convertedModel, ConvertedWinding convertedWinding,
        Connectable<?> tx) {
        TapChanger ptc = convertedWinding.end1.phaseTapChanger;
        if (ptc == null) {
            return;
        }

        if (ptc.getTapPosition() < ptc.getLowTapPosition() || ptc.getTapPosition() > ptc.getHighTapPosition()) {
            return;
        }

        PhaseTapChangerAdder ptca = newPhaseTapChanger(convertedModel, tx, convertedWinding.end1.terminal);
        setToIidmPhaseTapChanger(ptc, ptca);
    }

    protected RatioTapChangerAdder newRatioTapChanger(ConvertedModel convertedModel, Connectable<?> tx,
        String terminal) {
        if (convertedModel.winding1.end1.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg1().newRatioTapChanger();
        } else if (convertedModel.winding2.end1.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg2().newRatioTapChanger();
        } else if (convertedModel.winding3.end1.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg3().newRatioTapChanger();
        }
        return null;
    }

    protected PhaseTapChangerAdder newPhaseTapChanger(ConvertedModel convertedModel, Connectable<?> tx,
        String terminal) {
        if (convertedModel.winding1.end1.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg1().newPhaseTapChanger();
        } else if (convertedModel.winding2.end1.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg2().newPhaseTapChanger();
        } else if (convertedModel.winding3.end1.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg3().newPhaseTapChanger();
        }
        return null;
    }

    protected Terminal terminal(ConvertedModel convertedModel, Connectable<?> tx, String terminal) {
        if (convertedModel.winding1.end1.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg1().getTerminal();
        } else if (convertedModel.winding2.end1.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg2().getTerminal();
        } else if (convertedModel.winding3.end1.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg3().getTerminal();
        }
        return null;
    }

    static class CgmesModel {
        CgmesWinding winding1 = new CgmesWinding();
        CgmesWinding winding2 = new CgmesWinding();
        CgmesWinding winding3 = new CgmesWinding();
    }

    static class CgmesWinding {
        double r;
        double x;
        double g;
        double b;
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
        double ratedU;
        int phaseAngleClock;
        String terminal;
    }

    static class InterpretedModel {
        InterpretedWinding winding1 = new InterpretedWinding();
        InterpretedWinding winding2 = new InterpretedWinding();
        InterpretedWinding winding3 = new InterpretedWinding();
        double ratedUf;
    }

    // 1 network side, 2 start bus side
    static class InterpretedWinding {
        double r;
        double x;
        InterpretedEnd1 end1 = new InterpretedEnd1();
        InterpretedEnd2 end2 = new InterpretedEnd2();
        boolean ratio0AtEnd2;
    }

    static class InterpretedEnd1 {
        double g;
        double b;
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
        double ratedU;
        String terminal;
        int phaseAngleClock;
    }

    static class InterpretedEnd2 {
        double g;
        double b;
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
        int phaseAngleClock;
    }

    static class ConvertedModel {
        ConvertedWinding winding1 = new ConvertedWinding();
        ConvertedWinding winding2 = new ConvertedWinding();
        ConvertedWinding winding3 = new ConvertedWinding();
        double ratedUf;
    }

    // 1 network side, 2 start bus side
    static class ConvertedWinding {
        double r;
        double x;
        ConvertedEnd1 end1 = new ConvertedEnd1();
        ConvertedEnd2 end2 = new ConvertedEnd2();
    }

    static class ConvertedEnd2 {
        double g;
        double b;
        int phaseAngleClock;
    }

    static class TapChangerWinding {
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
    }

    private final double r1;
    private final double x1;
    private final double g1;
    private final double b1;
    private final double r2;
    private final double x2;
    private final double g2;
    private final double b2;
    private final double r3;
    private final double x3;
    private final double g3;
    private final double b3;
    private final double ratedU1;
    private final double ratedU2;
    private final double ratedU3;
    private final String terminal1;
    private final String terminal2;
    private final String terminal3;
    private final PropertyBag rtc1;
    private final PropertyBag rtc2;
    private final PropertyBag rtc3;
    private final PropertyBag ptc1;
    private final PropertyBag ptc2;
    private final PropertyBag ptc3;
    private final int phaseAngleClock1;
    private final int phaseAngleClock2;
    private final int phaseAngleClock3;
}
