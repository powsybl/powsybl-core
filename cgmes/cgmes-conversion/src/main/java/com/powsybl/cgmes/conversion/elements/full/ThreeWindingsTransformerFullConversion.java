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
        terminal1 = winding1.get(CgmesNames.TERMINAL);
        terminal2 = winding2.get(CgmesNames.TERMINAL);
        terminal3 = winding3.get(CgmesNames.TERMINAL);
        phaseAngleClock1 = winding1.asInt("phaseAngleClock", 0);
        phaseAngleClock2 = winding2.asInt("phaseAngleClock", 0);
        phaseAngleClock3 = winding3.asInt("phaseAngleClock", 0);
    }

    @Override
    public void convert() {

        LOG.info("MMC start three wind");
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

        TapChanger3 tapChanger3 = filterRatioPhaseRegulatingControl(ratioTapChanger1, phaseTapChanger1,
                ratioTapChanger2, phaseTapChanger2, ratioTapChanger3, phaseTapChanger3);

        CgmesModel cgmesModel = new CgmesModel();
        cgmesModel.winding1.r = r1;
        cgmesModel.winding1.x = x1;
        cgmesModel.winding1.g = g1;
        cgmesModel.winding1.b = b1;
        cgmesModel.winding1.ratioTapChanger = tapChanger3.ratioTapChanger1;
        cgmesModel.winding1.phaseTapChanger = tapChanger3.phaseTapChanger1;
        cgmesModel.winding1.ratedU = ratedU1;
        cgmesModel.winding1.phaseAngleClock = phaseAngleClock1;
        cgmesModel.winding1.terminal = terminal1;

        cgmesModel.winding2.r = r2;
        cgmesModel.winding2.x = x2;
        cgmesModel.winding2.g = g2;
        cgmesModel.winding2.b = b2;
        cgmesModel.winding2.ratioTapChanger = tapChanger3.ratioTapChanger2;
        cgmesModel.winding2.phaseTapChanger = tapChanger3.phaseTapChanger2;
        cgmesModel.winding2.ratedU = ratedU2;
        cgmesModel.winding2.phaseAngleClock = phaseAngleClock2;
        cgmesModel.winding2.terminal = terminal2;

        cgmesModel.winding3.r = r3;
        cgmesModel.winding3.x = x3;
        cgmesModel.winding3.g = g3;
        cgmesModel.winding3.b = b3;
        cgmesModel.winding3.ratioTapChanger = tapChanger3.ratioTapChanger3;
        cgmesModel.winding3.phaseTapChanger = tapChanger3.phaseTapChanger3;
        cgmesModel.winding3.ratedU = ratedU3;
        cgmesModel.winding3.phaseAngleClock = phaseAngleClock3;
        cgmesModel.winding3.terminal = terminal3;

        return cgmesModel;
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

    private InterpretedModel interpret(CgmesModel cgmesModel, Conversion.Config alternative) {

        TapChanger22 winding1InterpretedTapChanger = ratioPhaseAlternative(cgmesModel.winding1, alternative);
        TapChanger22 winding2InterpretedTapChanger = ratioPhaseAlternative(cgmesModel.winding2, alternative);
        TapChanger22 winding3InterpretedTapChanger = ratioPhaseAlternative(cgmesModel.winding3, alternative);

        Shunt22 winding1InterpretedShunt = shuntAlternative(cgmesModel.winding1, alternative);
        Shunt22 winding2InterpretedShunt = shuntAlternative(cgmesModel.winding2, alternative);
        Shunt22 winding3InterpretedShunt = shuntAlternative(cgmesModel.winding3, alternative);

        TapChanger02 winding1InterpretedClock = phaseAngleClockAlternative(cgmesModel.winding1, alternative);
        TapChanger02 winding2InterpretedClock = phaseAngleClockAlternative(cgmesModel.winding2, alternative);
        TapChanger02 winding3InterpretedClock = phaseAngleClockAlternative(cgmesModel.winding3, alternative);

        boolean winding1Ratio0AtEnd2 = ratio0Alternative(cgmesModel.winding1, alternative);
        boolean winding2Ratio0AtEnd2 = ratio0Alternative(cgmesModel.winding2, alternative);
        boolean winding3Ratio0AtEnd2 = ratio0Alternative(cgmesModel.winding3, alternative);

        TapChanger winding1PhaseTapChanger1 = combineTapChangers(winding1InterpretedTapChanger.phaseTapChanger1,
                winding1InterpretedClock.phaseTapChanger1);
        TapChanger winding1PhaseTapChanger2 = combineTapChangers(winding1InterpretedTapChanger.phaseTapChanger2,
                winding1InterpretedClock.phaseTapChanger2);

        TapChanger winding2PhaseTapChanger1 = combineTapChangers(winding2InterpretedTapChanger.phaseTapChanger1,
                winding2InterpretedClock.phaseTapChanger1);
        TapChanger winding2PhaseTapChanger2 = combineTapChangers(winding2InterpretedTapChanger.phaseTapChanger2,
                winding2InterpretedClock.phaseTapChanger2);

        TapChanger winding3PhaseTapChanger1 = combineTapChangers(winding3InterpretedTapChanger.phaseTapChanger1,
                winding3InterpretedClock.phaseTapChanger1);
        TapChanger winding3PhaseTapChanger2 = combineTapChangers(winding3InterpretedTapChanger.phaseTapChanger2,
                winding3InterpretedClock.phaseTapChanger2);

        InterpretedModel interpretedModel = new InterpretedModel();
        interpretedModel.winding1.r = cgmesModel.winding1.r;
        interpretedModel.winding1.x = cgmesModel.winding1.x;
        interpretedModel.winding1.g1 = winding1InterpretedShunt.g1;
        interpretedModel.winding1.b1 = winding1InterpretedShunt.b1;
        interpretedModel.winding1.g2 = winding1InterpretedShunt.g2;
        interpretedModel.winding1.b2 = winding1InterpretedShunt.b2;
        interpretedModel.winding1.ratioTapChanger1 = winding1InterpretedTapChanger.ratioTapChanger1;
        interpretedModel.winding1.phaseTapChanger1 = winding1PhaseTapChanger1;
        interpretedModel.winding1.ratioTapChanger2 = winding1InterpretedTapChanger.ratioTapChanger2;
        interpretedModel.winding1.phaseTapChanger2 = winding1PhaseTapChanger2;
        interpretedModel.winding1.ratedU = cgmesModel.winding1.ratedU;
        interpretedModel.winding1.terminal = cgmesModel.winding1.terminal;
        interpretedModel.winding1.ratio0AtEnd2 = winding1Ratio0AtEnd2;

        interpretedModel.winding2.r = cgmesModel.winding2.r;
        interpretedModel.winding2.x = cgmesModel.winding2.x;
        interpretedModel.winding2.g1 = winding2InterpretedShunt.g1;
        interpretedModel.winding2.b1 = winding2InterpretedShunt.b1;
        interpretedModel.winding2.g2 = winding2InterpretedShunt.g2;
        interpretedModel.winding2.b2 = winding2InterpretedShunt.b2;
        interpretedModel.winding2.ratioTapChanger1 = winding2InterpretedTapChanger.ratioTapChanger1;
        interpretedModel.winding2.phaseTapChanger1 = winding2PhaseTapChanger1;
        interpretedModel.winding2.ratioTapChanger2 = winding2InterpretedTapChanger.ratioTapChanger2;
        interpretedModel.winding2.phaseTapChanger2 = winding2PhaseTapChanger2;
        interpretedModel.winding2.ratedU = cgmesModel.winding2.ratedU;
        interpretedModel.winding2.terminal = cgmesModel.winding2.terminal;
        interpretedModel.winding2.ratio0AtEnd2 = winding2Ratio0AtEnd2;

        interpretedModel.winding3.r = cgmesModel.winding3.r;
        interpretedModel.winding3.x = cgmesModel.winding3.x;
        interpretedModel.winding3.g1 = winding3InterpretedShunt.g1;
        interpretedModel.winding3.b1 = winding3InterpretedShunt.b1;
        interpretedModel.winding3.g2 = winding3InterpretedShunt.g2;
        interpretedModel.winding3.b2 = winding3InterpretedShunt.b2;
        interpretedModel.winding3.ratioTapChanger1 = winding3InterpretedTapChanger.ratioTapChanger1;
        interpretedModel.winding3.phaseTapChanger1 = winding3PhaseTapChanger1;
        interpretedModel.winding3.ratioTapChanger2 = winding3InterpretedTapChanger.ratioTapChanger2;
        interpretedModel.winding3.phaseTapChanger2 = winding3PhaseTapChanger2;
        interpretedModel.winding3.ratedU = cgmesModel.winding3.ratedU;
        interpretedModel.winding3.terminal = cgmesModel.winding3.terminal;
        interpretedModel.winding3.ratio0AtEnd2 = winding3Ratio0AtEnd2;

        return interpretedModel;
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

    private TapChanger02 phaseAngleClockAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        TapChanger phaseTapChanger1 = null;
        TapChanger phaseTapChanger2 = null;

        if (cgmesWinding.phaseAngleClock != 0) {
            if (alternative.isXfmr3PhaseAngleClockNetworkSide()) {
                phaseTapChanger1 = createPhaseAngleClockTapChanger(cgmesWinding.phaseAngleClock);
            } else if (alternative.isXfmr3PhaseAngleClockStarBusSide()) {
                phaseTapChanger2 = createPhaseAngleClockTapChanger(cgmesWinding.phaseAngleClock);
            }
        }

        TapChanger02 tapChanger02 = new TapChanger02();
        tapChanger02.phaseTapChanger1 = phaseTapChanger1;
        tapChanger02.phaseTapChanger2 = phaseTapChanger2;

        return tapChanger02;
    }

    private boolean ratio0Alternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {

        boolean ratio0AtEnd2;
        if (alternative.isXfmr3Ratio0NetworkSide()) {
            ratio0AtEnd2 = false;
        } else {
            ratio0AtEnd2 = true;
        }
        LOG.info("ratio0AtEnd2 {}", ratio0AtEnd2);
        return ratio0AtEnd2;
    }

    private ConvertedModel convertToIidm(InterpretedModel interpretedModel) {

        TapChangerWinding winding1TapChanger = moveCombineTapChangerWinding(interpretedModel.winding1);
        TapChangerWinding winding2TapChanger = moveCombineTapChangerWinding(interpretedModel.winding2);
        TapChangerWinding winding3TapChanger = moveCombineTapChangerWinding(interpretedModel.winding3);

        double ratedUf = interpretedModel.winding1.ratedU;
        RatioConversion winding1Rc0 = rc0Winding(interpretedModel.winding1, ratedUf);
        RatioConversion winding2Rc0 = rc0Winding(interpretedModel.winding2, ratedUf);
        RatioConversion winding3Rc0 = rc0Winding(interpretedModel.winding3, ratedUf);

        ConvertedModel convertedModel = new ConvertedModel();
        convertedModel.winding1.r = winding1Rc0.r;
        convertedModel.winding1.x = winding1Rc0.x;
        convertedModel.winding1.g1 = winding1Rc0.g1;
        convertedModel.winding1.b1 = winding1Rc0.b1;
        convertedModel.winding1.g2 = winding1Rc0.g2;
        convertedModel.winding1.b2 = winding1Rc0.b2;
        convertedModel.winding1.ratioTapChanger = winding1TapChanger.ratioTapChanger;
        convertedModel.winding1.phaseTapChanger = winding1TapChanger.phaseTapChanger;
        convertedModel.winding1.ratedU = interpretedModel.winding1.ratedU;
        convertedModel.winding1.terminal = interpretedModel.winding1.terminal;

        convertedModel.winding2.r = winding2Rc0.r;
        convertedModel.winding2.x = winding2Rc0.x;
        convertedModel.winding2.g1 = winding2Rc0.g1;
        convertedModel.winding2.b1 = winding2Rc0.b1;
        convertedModel.winding2.g2 = winding2Rc0.g2;
        convertedModel.winding2.b2 = winding2Rc0.b2;
        convertedModel.winding2.ratioTapChanger = winding2TapChanger.ratioTapChanger;
        convertedModel.winding2.phaseTapChanger = winding2TapChanger.phaseTapChanger;
        convertedModel.winding2.ratedU = interpretedModel.winding2.ratedU;
        convertedModel.winding2.terminal = interpretedModel.winding2.terminal;

        convertedModel.winding3.r = winding3Rc0.r;
        convertedModel.winding3.x = winding3Rc0.x;
        convertedModel.winding3.g1 = winding3Rc0.g1;
        convertedModel.winding3.b1 = winding3Rc0.b1;
        convertedModel.winding3.g2 = winding3Rc0.g2;
        convertedModel.winding3.b2 = winding3Rc0.b2;
        convertedModel.winding3.ratioTapChanger = winding3TapChanger.ratioTapChanger;
        convertedModel.winding3.phaseTapChanger = winding3TapChanger.phaseTapChanger;
        convertedModel.winding3.ratedU = interpretedModel.winding3.ratedU;
        convertedModel.winding3.terminal = interpretedModel.winding3.terminal;

        convertedModel.ratedUf = ratedUf;

        return convertedModel;
    }

    private TapChangerWinding moveCombineTapChangerWinding(InterpretedWinding interpretedWinding) {

        TapChanger nRatioTapChanger = moveTapChangerFrom2To1(interpretedWinding.ratioTapChanger2);
        TapChanger nPhaseTapChanger = moveTapChangerFrom2To1(interpretedWinding.phaseTapChanger2);

        TapChanger cRatioTapChanger = combineTapChangers(interpretedWinding.ratioTapChanger1, nRatioTapChanger);
        TapChanger cPhaseTapChanger = combineTapChangers(interpretedWinding.phaseTapChanger1, nPhaseTapChanger);

        TapChangerWinding tapChangerWinding = new TapChangerWinding();
        tapChangerWinding.ratioTapChanger = cRatioTapChanger;
        tapChangerWinding.phaseTapChanger = cPhaseTapChanger;
        return tapChangerWinding;
    }

    private RatioConversion rc0Winding(InterpretedWinding interpretedWinding, double ratedUf) {
        RatioConversion rc0;
        if (interpretedWinding.ratio0AtEnd2) {
            double a0 = ratedUf / interpretedWinding.ratedU;
            rc0 = moveRatioFrom2To1(a0, 0.0, interpretedWinding.r, interpretedWinding.x,
                    interpretedWinding.g1, interpretedWinding.b1,
                    interpretedWinding.g2, interpretedWinding.b2);
        } else {
            rc0 = identityRatioConversion(interpretedWinding.r, interpretedWinding.x,
                    interpretedWinding.g1, interpretedWinding.b1,
                    interpretedWinding.g2, interpretedWinding.b2);
        }

        return rc0;

    }

    private void setToIidm(ConvertedModel convertedModel) {

        ThreeWindingsTransformerAdder txadder = substation().newThreeWindingsTransformer();
        identify(txadder);

        LegAdder<LegAdder> l1adder = txadder.newLeg1()
                .setR(convertedModel.winding1.r)
                .setX(convertedModel.winding1.x)
                .setG1(convertedModel.winding1.g1)
                .setB1(convertedModel.winding1.b1)
                .setG2(convertedModel.winding1.g2)
                .setB2(convertedModel.winding1.b2)
                .setRatedU(convertedModel.winding1.ratedU);
        LegAdder<LegAdder> l2adder = txadder.newLeg2()
                .setR(convertedModel.winding2.r)
                .setX(convertedModel.winding2.x)
                .setG1(convertedModel.winding2.g1)
                .setB1(convertedModel.winding2.b1)
                .setG2(convertedModel.winding2.g2)
                .setB2(convertedModel.winding2.b2)
                .setRatedU(convertedModel.winding2.ratedU);
        LegAdder<LegAdder> l3adder = txadder.newLeg3()
                .setR(convertedModel.winding3.r)
                .setX(convertedModel.winding3.x)
                .setG1(convertedModel.winding3.g1)
                .setB1(convertedModel.winding3.b1)
                .setG2(convertedModel.winding3.g2)
                .setB2(convertedModel.winding3.b2)
                .setRatedU(convertedModel.winding3.ratedU);

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

        setToIidmRatioTapChanger(convertedModel, convertedModel.winding1, tx, terminal1);
        setToIidmPhaseTapChanger(convertedModel, convertedModel.winding1, tx, terminal1);
        setToIidmRatioTapChanger(convertedModel, convertedModel.winding2, tx, terminal2);
        setToIidmPhaseTapChanger(convertedModel, convertedModel.winding2, tx, terminal2);
        setToIidmRatioTapChanger(convertedModel, convertedModel.winding3, tx, terminal3);
        setToIidmPhaseTapChanger(convertedModel, convertedModel.winding3, tx, terminal3);
    }

    private void setToIidmRatioTapChanger(ConvertedModel convertedModel, ConvertedWinding convertedWinding,
            Connectable<?> tx, String terminal) {

        TapChanger rtc = convertedWinding.ratioTapChanger;
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
        RatioTapChangerAdder rtca = newRatioTapChanger(convertedModel, tx, terminal);
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

    private void setToIidmPhaseTapChanger(ConvertedModel convertedModel,
            ConvertedWinding convertedWinding, Connectable<?> tx, String terminal) {
        TapChanger ptc = convertedWinding.phaseTapChanger;

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
        PhaseTapChangerAdder ptca = newPhaseTapChanger(convertedModel, tx, terminal);
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

    protected RatioTapChangerAdder newRatioTapChanger(ConvertedModel convertedModel, Connectable<?> tx, String terminal) {
        if (convertedModel.winding1.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg1().newRatioTapChanger();
        } else if (convertedModel.winding2.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg2().newRatioTapChanger();
        } else if (convertedModel.winding3.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg3().newRatioTapChanger();
        }
        return null;
    }

    protected PhaseTapChangerAdder newPhaseTapChanger(ConvertedModel convertedModel, Connectable<?> tx, String terminal) {
        if (convertedModel.winding1.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg1().newPhaseTapChanger();
        } else if (convertedModel.winding2.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg2().newPhaseTapChanger();
        } else if (convertedModel.winding3.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg3().newPhaseTapChanger();
        }
        return null;
    }

    protected Terminal terminal(ConvertedModel convertedModel, Connectable<?> tx, String terminal) {
        if (convertedModel.winding1.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg1().getTerminal();
        } else if (convertedModel.winding2.terminal.equals(terminal)) {
            return ((ThreeWindingsTransformer) tx).getLeg2().getTerminal();
        } else if (convertedModel.winding3.terminal.equals(terminal)) {
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
        double     r;
        double     x;
        double     g;
        double     b;
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
        double     ratedU;
        int        phaseAngleClock;
        String     terminal;
    }

    static class InterpretedModel {
        InterpretedWinding winding1 = new InterpretedWinding();
        InterpretedWinding winding2 = new InterpretedWinding();
        InterpretedWinding winding3 = new InterpretedWinding();
    }

    // 1 network side, 2 start bus side
    static class InterpretedWinding {
        double     r;
        double     x;
        double     g1;
        double     b1;
        double     g2;
        double     b2;
        TapChanger ratioTapChanger1;
        TapChanger phaseTapChanger1;
        TapChanger ratioTapChanger2;
        TapChanger phaseTapChanger2;
        double     ratedU;
        String     terminal;
        boolean    ratio0AtEnd2;
    }

    static class ConvertedModel {
        ConvertedWinding winding1 = new ConvertedWinding();
        ConvertedWinding winding2 = new ConvertedWinding();
        ConvertedWinding winding3 = new ConvertedWinding();
        double           ratedUf;
    }

    // 1 network side, 2 start bus side
    static class ConvertedWinding {
        double     r;
        double     x;
        double     g1;
        double     b1;
        double     g2;
        double     b2;
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
        double     ratedU;
        String     terminal;
    }

    static class TapChanger22 {
        TapChanger ratioTapChanger1;
        TapChanger phaseTapChanger1;
        TapChanger ratioTapChanger2;
        TapChanger phaseTapChanger2;
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

    static class TapChangerWinding {
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
    private final double      g1;
    private final double      b1;
    private final double      r2;
    private final double      x2;
    private final double      g2;
    private final double      b2;
    private final double      r3;
    private final double      x3;
    private final double      g3;
    private final double      b3;
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
    private final int         phaseAngleClock1;
    private final int         phaseAngleClock2;
    private final int         phaseAngleClock3;

    private static final Logger LOG = LoggerFactory.getLogger(ThreeWindingsTransformerFullConversion.class);
}
