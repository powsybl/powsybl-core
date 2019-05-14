package com.powsybl.cgmes.conversion.elements.full;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.PhaseTapChanger.RegulationMode;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformerAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

public class TwoWindingsTransformerFullConversion extends AbstractTransformerFullConversion {

    public TwoWindingsTransformerFullConversion(PropertyBags ends,
            Map<String, PropertyBag> powerTransformerRatioTapChanger,
            Map<String, PropertyBag> powerTransformerPhaseTapChanger, Context context) {
        super("PowerTransformer", ends, context);
        PropertyBag end1 = ends.get(0);
        PropertyBag end2 = ends.get(1);
        r1 = end1.asDouble("r");
        x1 = end1.asDouble("x");
        g1 = end1.asDouble("g", 0);
        b1 = end1.asDouble("b");
        r2 = end2.asDouble("r");
        x2 = end2.asDouble("x");
        g2 = end2.asDouble("g", 0);
        b2 = end2.asDouble("b");
        String ratedU = "ratedU";
        ratedU1 = end1.asDouble(ratedU);
        ratedU2 = end2.asDouble(ratedU);
        terminal1 = end1.get(CgmesNames.TERMINAL);
        terminal2 = end2.get(CgmesNames.TERMINAL);
        rtc1 = getTransformerTapChanger(end1, "RatioTapChanger", powerTransformerRatioTapChanger);
        ptc1 = getTransformerTapChanger(end1, "PhaseTapChanger", powerTransformerPhaseTapChanger);
        rtc2 = getTransformerTapChanger(end2, "RatioTapChanger", powerTransformerRatioTapChanger);
        ptc2 = getTransformerTapChanger(end2, "PhaseTapChanger", powerTransformerPhaseTapChanger);
        phaseAngleClock1 = end1.asInt("phaseAngleClock", 0);
        phaseAngleClock2 = end2.asInt("phaseAngleClock", 0);
    }

    @Override
    public void convert() {

        CgmesModel cgmesModel = load();
        InterpretedModel interpretedModel = interpret(cgmesModel, context.config());
        ConvertedModel convertedModel = convertToIidm(interpretedModel);

        setToIidm(convertedModel);
    }

    private CgmesModel load() {

        double r = r1 + r2;
        double x = x1 + x2;

        TapChanger ratioTapChanger1 = getRatioTapChanger(rtc1, terminal1);
        TapChanger ratioTapChanger2 = getRatioTapChanger(rtc2, terminal2);
        TapChanger phaseTapChanger1 = getPhaseTapChanger(ptc1, terminal1, ratedU1, x);
        TapChanger phaseTapChanger2 = getPhaseTapChanger(ptc2, terminal2, ratedU2, x);

        TapChanger22 tapChanger22 = filterRatioPhaseRegulatingControl(ratioTapChanger1,
                phaseTapChanger1, ratioTapChanger2, phaseTapChanger2);

        CgmesModel cgmesModel = new CgmesModel();
        cgmesModel.end1.g = g1;
        cgmesModel.end1.b = b1;
        cgmesModel.end1.ratioTapChanger = tapChanger22.ratioTapChanger1;
        cgmesModel.end1.phaseTapChanger = tapChanger22.phaseTapChanger1;
        cgmesModel.end1.ratedU = ratedU1;
        cgmesModel.end1.phaseAngleClock = phaseAngleClock1;
        cgmesModel.end1.terminal = terminal1;

        if (x1 == 0.0) {
            cgmesModel.end1.xIsZero = true;
        } else {
            cgmesModel.end1.xIsZero = false;
        }
        cgmesModel.end1.rtcDefined = rtc1 != null && rtc1.asDouble("stepVoltageIncrement") != 0.0;

        cgmesModel.end2.g = g2;
        cgmesModel.end2.b = b2;
        cgmesModel.end2.ratioTapChanger = tapChanger22.ratioTapChanger2;
        cgmesModel.end2.phaseTapChanger = tapChanger22.phaseTapChanger2;
        cgmesModel.end2.ratedU = ratedU2;
        cgmesModel.end2.phaseAngleClock = phaseAngleClock2;
        cgmesModel.end2.terminal = terminal2;

        if (x2 == 0.0) {
            cgmesModel.end2.xIsZero = true;
        } else {
            cgmesModel.end2.xIsZero = false;
        }
        cgmesModel.end2.rtcDefined = rtc2 != null && rtc2.asDouble("stepVoltageIncrement") != 0.0;

        cgmesModel.r = r;
        cgmesModel.x = x;

        return cgmesModel;
    }

    private TapChanger22 filterRatioPhaseRegulatingControl(TapChanger ratioTapChanger1, TapChanger phaseTapChanger1,
            TapChanger ratioTapChanger2, TapChanger phaseTapChanger2) {

        TapChanger22 tapChanger22 = new TapChanger22();

        if (tapChangerType(ratioTapChanger1) == TapChangerType.REGULATING) {
            tapChanger22.ratioTapChanger1 = ratioTapChanger1;
            tapChanger22.phaseTapChanger1 = fixTapChangerRegulation(phaseTapChanger1);
            tapChanger22.ratioTapChanger2 = fixTapChangerRegulation(ratioTapChanger2);
            tapChanger22.phaseTapChanger2 = fixTapChangerRegulation(phaseTapChanger2);
        } else if (tapChangerType(ratioTapChanger2) == TapChangerType.REGULATING) {
            tapChanger22.ratioTapChanger1 = ratioTapChanger1;
            tapChanger22.phaseTapChanger1 = fixTapChangerRegulation(phaseTapChanger1);
            tapChanger22.ratioTapChanger2 = ratioTapChanger2;
            tapChanger22.phaseTapChanger2 = fixTapChangerRegulation(phaseTapChanger2);
        } else if (tapChangerType(phaseTapChanger1) == TapChangerType.REGULATING) {
            tapChanger22.ratioTapChanger1 = ratioTapChanger1;
            tapChanger22.phaseTapChanger1 = phaseTapChanger1;
            tapChanger22.ratioTapChanger2 = ratioTapChanger2;
            tapChanger22.phaseTapChanger2 = fixTapChangerRegulation(phaseTapChanger2);
        } else {
            tapChanger22.ratioTapChanger1 = ratioTapChanger1;
            tapChanger22.phaseTapChanger1 = phaseTapChanger1;
            tapChanger22.ratioTapChanger2 = ratioTapChanger2;
            tapChanger22.phaseTapChanger2 = phaseTapChanger2;
        }

        return tapChanger22;
    }

    private InterpretedModel interpret(CgmesModel cgmesModel, Conversion.Config alternative) {

        TapChanger22 interpretedTapChanger = ratioPhaseAlternative(cgmesModel, alternative);
        Shunt22 interpretedShunt = shuntAlternative(cgmesModel, alternative);

        TapChanger02 interpretedClock = phaseAngleClockAlternative(cgmesModel, alternative);
        boolean ratio0AtEnd2 = ratio0Alternative(cgmesModel, alternative);

        TapChanger fPhaseTapChanger1 = combineTapChangers(interpretedTapChanger.phaseTapChanger1, interpretedClock.phaseTapChanger1);
        TapChanger fPhaseTapChanger2 = combineTapChangers(interpretedTapChanger.phaseTapChanger2, interpretedClock.phaseTapChanger2);

        InterpretedModel interpretedModel = new InterpretedModel();
        interpretedModel.r = cgmesModel.r;
        interpretedModel.x = cgmesModel.x;
        interpretedModel.end1.g = interpretedShunt.g1;
        interpretedModel.end1.b = interpretedShunt.b1;
        interpretedModel.end1.ratioTapChanger = interpretedTapChanger.ratioTapChanger1;
        interpretedModel.end1.phaseTapChanger = fPhaseTapChanger1;
        interpretedModel.end1.ratedU = cgmesModel.end1.ratedU;
        interpretedModel.end1.terminal = cgmesModel.end1.terminal;
        interpretedModel.end2.g = interpretedShunt.g2;
        interpretedModel.end2.b = interpretedShunt.b2;
        interpretedModel.end2.ratioTapChanger = interpretedTapChanger.ratioTapChanger2;
        interpretedModel.end2.phaseTapChanger = fPhaseTapChanger2;
        interpretedModel.end2.ratedU = cgmesModel.end2.ratedU;
        interpretedModel.end2.terminal = cgmesModel.end2.terminal;
        interpretedModel.ratio0AtEnd2 = ratio0AtEnd2;

        return interpretedModel;
    }

    private TapChanger22 ratioPhaseAlternative(CgmesModel cgmesModel, Conversion.Config alternative) {
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

        TapChanger22 tapChanger22 = new TapChanger22();
        tapChanger22.ratioTapChanger1 = ratioTapChanger1;
        tapChanger22.phaseTapChanger1 = phaseTapChanger1;
        tapChanger22.ratioTapChanger2 = ratioTapChanger2;
        tapChanger22.phaseTapChanger2 = phaseTapChanger2;

        return tapChanger22;
    }

    private Shunt22 shuntAlternative(CgmesModel cgmesModel, Conversion.Config alternative) {
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

        Shunt22 shunt22 = new Shunt22();
        shunt22.g1 = g1;
        shunt22.b1 = b1;
        shunt22.g2 = g2;
        shunt22.b2 = b2;

        return shunt22;
    }

    private TapChanger02 phaseAngleClockAlternative(CgmesModel cgmesModel, Conversion.Config alternative) {
        TapChanger phaseTapChanger1 = null;
        TapChanger phaseTapChanger2 = null;

        if (alternative.isXfmr2PhaseAngleClockEnd1End2()) {
            if (cgmesModel.end1.phaseAngleClock != 0) {
                phaseTapChanger1 = createPhaseAngleClockTapChanger(cgmesModel.end1.phaseAngleClock);

                if (alternative.isXfmr2PhaseAngleClock1Negate()) {
                    negatePhaseTapChanger(phaseTapChanger1);
                }
            }
            if (cgmesModel.end2.phaseAngleClock != 0) {
                phaseTapChanger2 = createPhaseAngleClockTapChanger(cgmesModel.end2.phaseAngleClock);

                if (alternative.isXfmr2PhaseAngleClock2Negate()) {
                    negatePhaseTapChanger(phaseTapChanger2);
                }
            }
        }

        TapChanger02 tapChanger02 = new TapChanger02();
        tapChanger02.phaseTapChanger1 = phaseTapChanger1;
        tapChanger02.phaseTapChanger2 = phaseTapChanger2;

        return tapChanger02;
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
            rc0 = moveRatioFrom2To1(a0, 0.0, interpretedModel.r, interpretedModel.x, interpretedModel.end1.g, interpretedModel.end1.b,
                    interpretedModel.end2.g, interpretedModel.end2.b);
        } else {
            rc0 = identityRatioConversion(interpretedModel.r, interpretedModel.x, interpretedModel.end1.g, interpretedModel.end1.b,
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

        convertedModel.end2.g = rc0.g2;
        convertedModel.end2.b = rc0.b2;
        convertedModel.end2.ratedU = interpretedModel.end2.ratedU;
        convertedModel.end2.terminal = interpretedModel.end2.terminal;

        return convertedModel;
    }

    private void setToIidm(ConvertedModel convertedModel) {
        TwoWindingsTransformerAdder adder = substation().newTwoWindingsTransformer()
                .setR(convertedModel.r)
                .setX(convertedModel.x)
                .setG1(convertedModel.end1.g)
                .setB1(convertedModel.end1.b)
                .setG2(convertedModel.end2.g)
                .setB2(convertedModel.end2.b)
                .setRatedU1(convertedModel.end1.ratedU)
                .setRatedU2(convertedModel.end2.ratedU);
        identify(adder);
        connect(adder);
        TwoWindingsTransformer tx = adder.add();
        convertedTerminals(tx.getTerminal1(), tx.getTerminal2());

        /*if (id.equals("_f9aec7ee-396b-4401-aebf-31644eb4b06d")) {
            LOG.info("rtc1 {} ptc1 {}", convertedModel.end1.ratioTapChanger, convertedModel.end1.phaseTapChanger);
        }*/

        setToIidmRatioTapChanger(convertedModel, tx);
        setToIidmPhaseTapChanger(convertedModel, tx);
    }

    private void setToIidmRatioTapChanger(ConvertedModel convertedModel, Connectable<?> tx) {

        TapChanger rtc = convertedModel.end1.ratioTapChanger;
        if (rtc == null) {
            return;
        }
        boolean isRegulating = rtc.isRegulating();
        String regulatingTerminalId = rtc.getRegulationTerminal();
        Terminal regulationTerminal = terminal(convertedModel, tx, regulatingTerminalId);
        double regulationValue = rtc.getRegulationValue();
        boolean isLoadTapChangingCapabilities = rtc.isLoadTapChangingCapabilities();
        int lowStep = rtc.getLowTapPosition();
        int position = rtc.getTapPosition();
        int highStep = lowStep + rtc.getSteps().size() - 1;
        if (position < lowStep || position > highStep) {
            return;
        }
        RatioTapChangerAdder rtca = newRatioTapChanger(tx);
        rtca.setLowTapPosition(lowStep).setTapPosition((int) position)
                .setLoadTapChangingCapabilities(isLoadTapChangingCapabilities)
                .setRegulating(isRegulating).setRegulationTerminal(regulationTerminal)
                .setTargetV(regulationValue);
        rtc.getSteps().forEach(step -> {
            double ratio0 = step.getRatio();
            double r0 = step.getR();
            double x0 = step.getX();
            double b01 = step.getB1();
            double g01 = step.getG1();
            double b02 = step.getB2();
            double g02 = step.getG2();
            rtca.beginStep()
                    .setRho(1 / ratio0)
                    .setR(r0)
                    .setX(x0)
                    .setB1(b01)
                    .setG1(g01)
                    .setB2(b02)
                    .setG2(g02)
                    .endStep();
        });
        rtca.add();
    }

    private void setToIidmPhaseTapChanger(ConvertedModel convertedModel, Connectable<?> tx) {
        TapChanger ptc = convertedModel.end1.phaseTapChanger;

        if (ptc == null) {
            return;
        }
        boolean isRegulating = ptc.isRegulating();
        RegulationMode regulationMode = ptc.getRegulationMode();
        String regulationTerminalId = ptc.getRegulationTerminal();
        Terminal regulationTerminal = terminal(convertedModel, tx, regulationTerminalId);
        double regulationValue = ptc.getRegulationValue();
        int lowStep = ptc.getLowTapPosition();
        int position = ptc.getTapPosition();
        int highStep = lowStep + ptc.getSteps().size() - 1;
        if (position < lowStep || position > highStep) {
            return;
        }
        PhaseTapChangerAdder ptca = newPhaseTapChanger(tx);
        ptca.setLowTapPosition(lowStep).setTapPosition((int) position)
                .setRegulating(isRegulating).setRegulationTerminal(regulationTerminal)
                .setRegulationMode(regulationMode).setRegulationValue(regulationValue);
        ptc.getSteps().forEach(step -> {
            double ratio0 = step.getRatio();
            double angle0 = step.getAngle();
            double r0 = step.getR();
            double x0 = step.getX();
            double b01 = step.getB1();
            double g01 = step.getG1();
            double b02 = step.getB2();
            double g02 = step.getG2();
            ptca.beginStep()
                    .setRho(1 / ratio0)
                    .setAlpha(-angle0)
                    .setR(r0)
                    .setX(x0)
                    .setB1(b01)
                    .setG1(g01)
                    .setB2(b02)
                    .setG2(g02)
                    .endStep();
        });
        ptca.add();
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
        double   r;
        double   x;
        CgmesEnd end1 = new CgmesEnd();
        CgmesEnd end2 = new CgmesEnd();
    }

    static class CgmesEnd {
        double     g;
        double     b;
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
        double     ratedU;
        int        phaseAngleClock;
        String     terminal;
        boolean    xIsZero;
        boolean    rtcDefined;
    }

    static class InterpretedModel {
        double         r;
        double         x;
        InterpretedEnd end1 = new InterpretedEnd();
        InterpretedEnd end2 = new InterpretedEnd();
        boolean        ratio0AtEnd2;
    }

    static class InterpretedEnd {
        double     g;
        double     b;
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
        double     ratedU;
        String     terminal;
    }

    static class ConvertedModel {
        double        r;
        double        x;
        ConvertedEnd1 end1 = new ConvertedEnd1();
        ConvertedEnd2 end2 = new ConvertedEnd2();
    }

    static class ConvertedEnd1 {
        double     g;
        double     b;
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
        double     ratedU;
        String     terminal;
    }

    static class ConvertedEnd2 {
        double g;
        double b;
        double ratedU;
        String terminal;
    }

    static class Shunt22 {
        double g1;
        double b1;
        double g2;
        double b2;
    }

    static class TapChanger02 {
        TapChanger phaseTapChanger1;
        TapChanger phaseTapChanger2;
    }

    static class TapChanger22 {
        TapChanger ratioTapChanger1;
        TapChanger phaseTapChanger1;
        TapChanger ratioTapChanger2;
        TapChanger phaseTapChanger2;
    }

    private final double      r1;
    private final double      x1;
    private final double      b1;
    private final double      g1;
    private final double      r2;
    private final double      x2;
    private final double      b2;
    private final double      g2;
    private final double      ratedU1;
    private final double      ratedU2;
    private final String      terminal1;
    private final String      terminal2;
    private final PropertyBag rtc1;
    private final PropertyBag rtc2;
    private final PropertyBag ptc1;
    private final PropertyBag ptc2;
    private final int         phaseAngleClock1;
    private final int         phaseAngleClock2;
    private static final Logger LOG = LoggerFactory.getLogger(TwoWindingsTransformerFullConversion.class);

}
