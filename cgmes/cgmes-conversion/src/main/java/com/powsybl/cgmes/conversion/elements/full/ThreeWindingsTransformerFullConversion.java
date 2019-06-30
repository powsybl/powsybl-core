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
        super(STRING_POWER_TRANSFORMER, ends, context);
        this.powerTransformerRatioTapChanger = powerTransformerRatioTapChanger;
        this.powerTransformerPhaseTapChanger = powerTransformerPhaseTapChanger;
    }

    @Override
    public void convert() {

        CgmesModel cgmesModel = load();
        InterpretedModel interpretedModel = interpret(cgmesModel, context.config());
        ConvertedModel convertedModel = convertToIidm(interpretedModel);

        setToIidm(convertedModel);
    }

    private CgmesModel load() {
        CgmesModel cgmesModel = new CgmesModel();

        // ends = ps
        loadWinding(ps.get(0), cgmesModel.winding1);
        loadWinding(ps.get(1), cgmesModel.winding2);
        loadWinding(ps.get(2), cgmesModel.winding3);

        return cgmesModel;
    }

    private void loadWinding(PropertyBag winding, CgmesWinding cgmesModelWinding) {
        PropertyBag rtc = getTransformerTapChanger(winding, STRING_RATIO_TAP_CHANGER,
            powerTransformerRatioTapChanger);
        PropertyBag ptc = getTransformerTapChanger(winding, STRING_PHASE_TAP_CHANGER,
            powerTransformerPhaseTapChanger);

        String terminal = winding.getId(CgmesNames.TERMINAL);
        double ratedU = winding.asDouble(STRING_RATEDU);
        double x = winding.asDouble(STRING_X);

        TapChanger ratioTapChanger = getRatioTapChanger(rtc, terminal);
        TapChanger phaseTapChanger = getPhaseTapChanger(ptc, terminal, ratedU, x);

        cgmesModelWinding.r = winding.asDouble(STRING_R);
        cgmesModelWinding.x = x;
        cgmesModelWinding.g = winding.asDouble(STRING_G, 0);
        cgmesModelWinding.b = winding.asDouble(STRING_B);
        cgmesModelWinding.ratioTapChanger = ratioTapChanger;
        cgmesModelWinding.phaseTapChanger = phaseTapChanger;
        cgmesModelWinding.ratedU = ratedU;
        cgmesModelWinding.phaseAngleClock = winding.asInt(STRING_PHASE_ANGLE_CLOCK, 0);
        cgmesModelWinding.terminal = terminal;
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

        TapChangerAll windingInterpretedTapChanger = ratioPhaseAlternative(cgmesModelWinding, alternative);
        ShuntAll windingInterpretedShunt = shuntAlternative(cgmesModelWinding, alternative);
        PhaseAngleClockAll windingInterpretedClock = phaseAngleClockAlternative(cgmesModelWinding, alternative);
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

    private TapChangerAll ratioPhaseAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
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

        TapChangerAll tapChanger22 = new TapChangerAll();
        tapChanger22.ratioTapChanger1 = ratioTapChanger1;
        tapChanger22.phaseTapChanger1 = phaseTapChanger1;
        tapChanger22.ratioTapChanger2 = ratioTapChanger2;
        tapChanger22.phaseTapChanger2 = phaseTapChanger2;

        return tapChanger22;
    }

    private ShuntAll shuntAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
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

        ShuntAll shunt22 = new ShuntAll();
        shunt22.g1 = g1;
        shunt22.b1 = b1;
        shunt22.g2 = g2;
        shunt22.b2 = b2;

        return shunt22;
    }

    private PhaseAngleClockAll phaseAngleClockAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        int phaseAngleClock1 = 0;
        int phaseAngleClock2 = 0;

        if (cgmesWinding.phaseAngleClock != 0) {
            if (alternative.isXfmr3PhaseAngleClockNetworkSide()) {
                phaseAngleClock1 = cgmesWinding.phaseAngleClock;
            } else if (alternative.isXfmr3PhaseAngleClockStarBusSide()) {
                phaseAngleClock2 = cgmesWinding.phaseAngleClock;
            }
        }

        PhaseAngleClockAll phaseAngleClock02 = new PhaseAngleClockAll();
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

    private final Map<String, PropertyBag> powerTransformerRatioTapChanger;
    private final Map<String, PropertyBag> powerTransformerPhaseTapChanger;
}
