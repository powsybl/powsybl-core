package com.powsybl.cgmes.conversion.elements.full;

import java.util.Map;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.TransformerRegulatingControlMapping.RegulatingDataPhase;
import com.powsybl.cgmes.conversion.TransformerRegulatingControlMapping.RegulatingDataRatio;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformerAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public class TwoWindingsTransformerFullConversion extends AbstractTransformerFullConversion {

    public TwoWindingsTransformerFullConversion(PropertyBags ends,
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
        // ends = ps
        PropertyBag end1 = ps.get(0);
        PropertyBag end2 = ps.get(1);

        double x1 = end1.asDouble(STRING_X);
        double x2 = end2.asDouble(STRING_X);
        double r = end1.asDouble(STRING_R) + end2.asDouble(STRING_R);
        double x = x1 + x2;

        String terminal1 = end1.getId(CgmesNames.TERMINAL);
        String terminal2 = end2.getId(CgmesNames.TERMINAL);

        PropertyBag rtc1 = getTransformerTapChanger(end1, STRING_RATIO_TAP_CHANGER, powerTransformerRatioTapChanger);
        PropertyBag ptc1 = getTransformerTapChanger(end1, STRING_PHASE_TAP_CHANGER, powerTransformerPhaseTapChanger);
        PropertyBag rtc2 = getTransformerTapChanger(end2, STRING_RATIO_TAP_CHANGER, powerTransformerRatioTapChanger);
        PropertyBag ptc2 = getTransformerTapChanger(end2, STRING_PHASE_TAP_CHANGER, powerTransformerPhaseTapChanger);

        double ratedU1 = end1.asDouble(STRING_RATEDU);
        double ratedU2 = end2.asDouble(STRING_RATEDU);

        TapChanger ratioTapChanger1 = getRatioTapChanger(rtc1, terminal1, 1);
        TapChanger ratioTapChanger2 = getRatioTapChanger(rtc2, terminal2, 2);
        TapChanger phaseTapChanger1 = getPhaseTapChanger(ptc1, terminal1, ratedU1, x, 1);
        TapChanger phaseTapChanger2 = getPhaseTapChanger(ptc2, terminal2, ratedU2, x, 2);

        CgmesModel cgmesModel = new CgmesModel();
        cgmesModel.end1.g = end1.asDouble(STRING_G, 0);
        cgmesModel.end1.b = end1.asDouble(STRING_B);
        cgmesModel.end1.ratioTapChanger = ratioTapChanger1;
        cgmesModel.end1.phaseTapChanger = phaseTapChanger1;
        cgmesModel.end1.ratedU = ratedU1;
        cgmesModel.end1.phaseAngleClock = end1.asInt(STRING_PHASE_ANGLE_CLOCK, 0);
        cgmesModel.end1.terminal = terminal1;

        if (x1 == 0.0) {
            cgmesModel.end1.xIsZero = true;
        } else {
            cgmesModel.end1.xIsZero = false;
        }
        cgmesModel.end1.rtcDefined = rtc1 != null && rtc1.asDouble(STRING_STEP_VOLTAGE_INCREMENT) != 0.0;

        cgmesModel.end2.g = end2.asDouble(STRING_G, 0);
        cgmesModel.end2.b = end2.asDouble(STRING_B);
        cgmesModel.end2.ratioTapChanger = ratioTapChanger2;
        cgmesModel.end2.phaseTapChanger = phaseTapChanger2;
        cgmesModel.end2.ratedU = ratedU2;
        cgmesModel.end2.phaseAngleClock = end2.asInt(STRING_PHASE_ANGLE_CLOCK, 0);
        cgmesModel.end2.terminal = terminal2;

        if (x2 == 0.0) {
            cgmesModel.end2.xIsZero = true;
        } else {
            cgmesModel.end2.xIsZero = false;
        }
        cgmesModel.end2.rtcDefined = rtc2 != null && rtc2.asDouble(STRING_STEP_VOLTAGE_INCREMENT) != 0.0;

        cgmesModel.r = r;
        cgmesModel.x = x;

        return cgmesModel;
    }

    private InterpretedModel interpret(CgmesModel cgmesModel, Conversion.Config alternative) {

        TapChangerAll interpretedTapChanger = ratioPhaseAlternative(cgmesModel, alternative);
        ShuntAll interpretedShunt = shuntAlternative(cgmesModel, alternative);

        PhaseAngleClockAll interpretedClock = phaseAngleClockAlternative(cgmesModel, alternative);
        boolean ratio0AtEnd2 = ratio0Alternative(cgmesModel, alternative);

        InterpretedModel interpretedModel = new InterpretedModel();
        interpretedModel.r = cgmesModel.r;
        interpretedModel.x = cgmesModel.x;

        interpretedModel.end1.g = interpretedShunt.g1;
        interpretedModel.end1.b = interpretedShunt.b1;
        interpretedModel.end1.ratioTapChanger = interpretedTapChanger.ratioTapChanger1;
        interpretedModel.end1.phaseTapChanger = interpretedTapChanger.phaseTapChanger1;
        interpretedModel.end1.ratedU = cgmesModel.end1.ratedU;
        interpretedModel.end1.terminal = cgmesModel.end1.terminal;
        interpretedModel.end1.phaseAngleClock = interpretedClock.phaseAngleClock1;

        interpretedModel.end2.g = interpretedShunt.g2;
        interpretedModel.end2.b = interpretedShunt.b2;
        interpretedModel.end2.ratioTapChanger = interpretedTapChanger.ratioTapChanger2;
        interpretedModel.end2.phaseTapChanger = interpretedTapChanger.phaseTapChanger2;
        interpretedModel.end2.ratedU = cgmesModel.end2.ratedU;
        interpretedModel.end2.terminal = cgmesModel.end2.terminal;
        interpretedModel.end2.phaseAngleClock = interpretedClock.phaseAngleClock2;

        interpretedModel.ratio0AtEnd2 = ratio0AtEnd2;

        return interpretedModel;
    }

    private TapChangerAll ratioPhaseAlternative(CgmesModel cgmesModel, Conversion.Config alternative) {
        TapChanger ratioTapChanger1 = null;
        TapChanger phaseTapChanger1 = null;
        TapChanger ratioTapChanger2 = null;
        TapChanger phaseTapChanger2 = null;

        if (alternative.isXfmr2RatioPhaseEnd1()) {
            ratioTapChanger1 = combineTapChangers(cgmesModel.end1.ratioTapChanger, cgmesModel.end2.ratioTapChanger);
            phaseTapChanger1 = combineTapChangers(cgmesModel.end1.phaseTapChanger, cgmesModel.end2.phaseTapChanger);
        } else if (alternative.isXfmr2RatioPhaseEnd2()) {
            ratioTapChanger2 = combineTapChangers(cgmesModel.end2.ratioTapChanger, cgmesModel.end1.ratioTapChanger);
            phaseTapChanger2 = combineTapChangers(cgmesModel.end2.phaseTapChanger, cgmesModel.end1.phaseTapChanger);
        } else if (alternative.isXfmr2RatioPhaseEnd1End2()) {
            ratioTapChanger1 = cgmesModel.end1.ratioTapChanger;
            phaseTapChanger1 = cgmesModel.end1.phaseTapChanger;
            ratioTapChanger2 = cgmesModel.end2.ratioTapChanger;
            phaseTapChanger2 = cgmesModel.end2.phaseTapChanger;
        } else {
            if (cgmesModel.end1.xIsZero) {
                ratioTapChanger1 = combineTapChangers(cgmesModel.end1.ratioTapChanger, cgmesModel.end2.ratioTapChanger);
                phaseTapChanger1 = combineTapChangers(cgmesModel.end1.phaseTapChanger, cgmesModel.end2.phaseTapChanger);
            } else {
                ratioTapChanger2 = combineTapChangers(cgmesModel.end2.ratioTapChanger, cgmesModel.end1.ratioTapChanger);
                phaseTapChanger2 = combineTapChangers(cgmesModel.end2.phaseTapChanger, cgmesModel.end1.phaseTapChanger);
            }
        }

        if (alternative.isXfmr2Phase1Negate()) {
            negatePhaseTapChanger(phaseTapChanger1);
        }
        if (alternative.isXfmr2Phase2Negate()) {
            negatePhaseTapChanger(phaseTapChanger2);
        }

        TapChangerAll tapChanger22 = new TapChangerAll();
        tapChanger22.ratioTapChanger1 = ratioTapChanger1;
        tapChanger22.phaseTapChanger1 = phaseTapChanger1;
        tapChanger22.ratioTapChanger2 = ratioTapChanger2;
        tapChanger22.phaseTapChanger2 = phaseTapChanger2;

        return tapChanger22;
    }

    private ShuntAll shuntAlternative(CgmesModel cgmesModel, Conversion.Config alternative) {
        double g1 = 0.0;
        double b1 = 0.0;
        double g2 = 0.0;
        double b2 = 0.0;
        if (alternative.isXfmr2ShuntEnd1()) {
            g1 = cgmesModel.end1.g + cgmesModel.end2.g;
            b1 = cgmesModel.end1.b + cgmesModel.end2.b;
        } else if (alternative.isXfmr2ShuntEnd2()) {
            g2 = cgmesModel.end1.g + cgmesModel.end2.g;
            b2 = cgmesModel.end1.b + cgmesModel.end2.b;
        } else if (alternative.isXfmr2ShuntEnd1End2()) {
            g1 = cgmesModel.end1.g;
            b1 = cgmesModel.end1.b;
            g2 = cgmesModel.end2.g;
            b2 = cgmesModel.end2.b;
        } else {
            g1 = (cgmesModel.end1.g + cgmesModel.end2.g) * 0.5;
            b1 = (cgmesModel.end1.b + cgmesModel.end2.b) * 0.5;
            g2 = (cgmesModel.end1.g + cgmesModel.end2.g) * 0.5;
            b2 = (cgmesModel.end1.b + cgmesModel.end2.b) * 0.5;
        }

        ShuntAll shunt22 = new ShuntAll();
        shunt22.g1 = g1;
        shunt22.b1 = b1;
        shunt22.g2 = g2;
        shunt22.b2 = b2;

        return shunt22;
    }

    private PhaseAngleClockAll phaseAngleClockAlternative(CgmesModel cgmesModel, Conversion.Config alternative) {
        int phaseAngleClock1 = 0;
        int phaseAngleClock2 = 0;

        if (alternative.isXfmr2PhaseAngleClockEnd1End2()) {
            if (cgmesModel.end1.phaseAngleClock != 0) {
                if (alternative.isXfmr2PhaseAngleClock1Negate()) {
                    phaseAngleClock2 = cgmesModel.end1.phaseAngleClock;
                } else {
                    phaseAngleClock1 = cgmesModel.end1.phaseAngleClock;
                }
            }
            if (cgmesModel.end2.phaseAngleClock != 0) {
                if (alternative.isXfmr2PhaseAngleClock2Negate()) {
                    phaseAngleClock1 = cgmesModel.end2.phaseAngleClock;
                } else {
                    phaseAngleClock2 = cgmesModel.end2.phaseAngleClock;
                }
            }
        }

        PhaseAngleClockAll phaseAngleClock02 = new PhaseAngleClockAll();
        phaseAngleClock02.phaseAngleClock1 = phaseAngleClock1;
        phaseAngleClock02.phaseAngleClock2 = phaseAngleClock2;

        return phaseAngleClock02;
    }

    private boolean ratio0Alternative(CgmesModel cgmesModel, Conversion.Config alternative) {
        if (cgmesModel.end1.ratedU == cgmesModel.end2.ratedU) {
            return false;
        }

        boolean ratio0AtEnd2 = false;
        if (alternative.isXfmr2Ratio0End1()) {
            ratio0AtEnd2 = false;
        } else if (alternative.isXfmr2Ratio0End2()) {
            ratio0AtEnd2 = true;
        } else if (alternative.isXfmr2Ratio0Rtc()) {
            if (cgmesModel.end1.rtcDefined) {
                ratio0AtEnd2 = false;
            } else {
                ratio0AtEnd2 = true;
            }
        } else {
            if (cgmesModel.end1.xIsZero) {
                ratio0AtEnd2 = false;
            } else {
                ratio0AtEnd2 = true;
            }
        }
        return ratio0AtEnd2;
    }

    private ConvertedModel convertToIidm(InterpretedModel interpretedModel) {

        TapChanger nRatioTapChanger2 = moveTapChangerFrom2To1(interpretedModel.end2.ratioTapChanger);
        TapChanger nPhaseTapChanger2 = moveTapChangerFrom2To1(interpretedModel.end2.phaseTapChanger);

        TapChanger ratioTapChanger = combineTapChangers(interpretedModel.end1.ratioTapChanger, nRatioTapChanger2);
        TapChanger phaseTapChanger = combineTapChangers(interpretedModel.end1.phaseTapChanger, nPhaseTapChanger2);

        RatioConversion rc0;
        if (interpretedModel.ratio0AtEnd2) {
            double a0 = interpretedModel.end2.ratedU / interpretedModel.end1.ratedU;
            rc0 = moveRatioFrom2To1(a0, 0.0, interpretedModel.r, interpretedModel.x,
                interpretedModel.end1.g, interpretedModel.end1.b,
                interpretedModel.end2.g, interpretedModel.end2.b);
        } else {
            rc0 = identityRatioConversion(interpretedModel.r, interpretedModel.x,
                interpretedModel.end1.g, interpretedModel.end1.b,
                interpretedModel.end2.g, interpretedModel.end2.b);
        }
        ConvertedModel convertedModel = new ConvertedModel();

        convertedModel.r = rc0.r;
        convertedModel.x = rc0.x;

        convertedModel.end1.g = rc0.g1;
        convertedModel.end1.b = rc0.b1;
        convertedModel.end1.ratioTapChanger = ratioTapChanger;
        convertedModel.end1.phaseTapChanger = phaseTapChanger;
        convertedModel.end1.ratedU = interpretedModel.end1.ratedU;
        convertedModel.end1.terminal = interpretedModel.end1.terminal;
        convertedModel.end1.phaseAngleClock = interpretedModel.end1.phaseAngleClock;

        convertedModel.end2.g = rc0.g2;
        convertedModel.end2.b = rc0.b2;
        convertedModel.end2.ratedU = interpretedModel.end2.ratedU;
        convertedModel.end2.terminal = interpretedModel.end2.terminal;
        convertedModel.end2.phaseAngleClock = interpretedModel.end2.phaseAngleClock;

        return convertedModel;
    }

    private void setToIidm(ConvertedModel convertedModel) {
        TwoWindingsTransformerAdder adder = substation().newTwoWindingsTransformer()
            .setR(convertedModel.r)
            .setX(convertedModel.x)
            .setG(convertedModel.end1.g + convertedModel.end2.g)
            .setB(convertedModel.end1.b + convertedModel.end2.b)
            .setRatedU1(convertedModel.end1.ratedU)
            .setPhaseAngleClock1(convertedModel.end1.phaseAngleClock)
            .setRatedU2(convertedModel.end2.ratedU)
            .setPhaseAngleClock2(convertedModel.end2.phaseAngleClock);
        identify(adder);
        connect(adder);
        TwoWindingsTransformer tx = adder.add();
        convertedTerminals(tx.getTerminal1(), tx.getTerminal2());

        setToIidmRatioTapChanger(convertedModel, tx);
        setToIidmPhaseTapChanger(convertedModel, tx);

        setRegulatingControlContext(tx, convertedModel);
    }

    private void setToIidmRatioTapChanger(ConvertedModel convertedModel, Connectable<?> tx) {
        TapChanger rtc = convertedModel.end1.ratioTapChanger;
        if (rtc == null) {
            return;
        }

        if (rtc.getTapPosition() < rtc.getLowTapPosition() || rtc.getTapPosition() > rtc.getHighTapPosition()) {
            return;
        }

        RatioTapChangerAdder rtca = newRatioTapChanger(tx);
        setToIidmRatioTapChanger(rtc, rtca);
    }

    private void setToIidmPhaseTapChanger(ConvertedModel convertedModel, Connectable<?> tx) {
        TapChanger ptc = convertedModel.end1.phaseTapChanger;
        if (ptc == null) {
            return;
        }

        if (ptc.getTapPosition() < ptc.getLowTapPosition() || ptc.getTapPosition() > ptc.getHighTapPosition()) {
            return;
        }

        PhaseTapChangerAdder ptca = newPhaseTapChanger(tx);
        setToIidmPhaseTapChanger(ptc, ptca);
    }

    private void setRegulatingControlContext(Connectable<?> tx, ConvertedModel convertedModel) {
        RegulatingDataRatio rdRtc = buildContextRegulatingDataRatio(convertedModel.end1.ratioTapChanger);
        RegulatingDataPhase rdPtc = buildContextRegulatingDataPhase(convertedModel.end1.phaseTapChanger);
        context.transformerRegulatingControlMapping().add(tx.getId(), rdRtc, rdPtc);
    }

    protected RatioTapChangerAdder newRatioTapChanger(Connectable<?> tx) {
        return ((TwoWindingsTransformer) tx).newRatioTapChanger();
    }

    protected PhaseTapChangerAdder newPhaseTapChanger(Connectable<?> tx) {
        return ((TwoWindingsTransformer) tx).newPhaseTapChanger();
    }

    protected Terminal terminal(ConvertedModel convertedModel, Connectable<?> tx, String terminalId) {
        if (tx instanceof TwoWindingsTransformer) {
            TwoWindingsTransformer t2x = (TwoWindingsTransformer) tx;

            if (convertedModel.end1.terminal.equals(terminalId)) {
                return t2x.getTerminal1();
            } else if (convertedModel.end2.terminal.equals(terminalId)) {
                return t2x.getTerminal2();
            }
        }
        return null;
    }

    static class CgmesModel {
        double r;
        double x;
        CgmesEnd end1 = new CgmesEnd();
        CgmesEnd end2 = new CgmesEnd();
    }

    static class CgmesEnd {
        double g;
        double b;
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
        double ratedU;
        int phaseAngleClock;
        String terminal;
        boolean xIsZero;
        boolean rtcDefined;
    }

    static class InterpretedModel {
        double r;
        double x;
        InterpretedEnd end1 = new InterpretedEnd();
        InterpretedEnd end2 = new InterpretedEnd();
        boolean ratio0AtEnd2;
    }

    static class InterpretedEnd {
        double g;
        double b;
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
        double ratedU;
        String terminal;
        int phaseAngleClock;
    }

    static class ConvertedModel {
        double r;
        double x;
        ConvertedEnd1 end1 = new ConvertedEnd1();
        ConvertedEnd2 end2 = new ConvertedEnd2();
    }

    static class ConvertedEnd2 {
        double g;
        double b;
        double ratedU;
        String terminal;
        int phaseAngleClock;
    }

    private final Map<String, PropertyBag> powerTransformerRatioTapChanger;
    private final Map<String, PropertyBag> powerTransformerPhaseTapChanger;
}
