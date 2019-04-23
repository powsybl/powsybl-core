package com.powsybl.cgmes.conversion.elements.full;

import java.util.Map;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformerAdder;
import com.powsybl.iidm.network.PhaseTapChanger.RegulationMode;
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
        if (x1 != 0.0) {
            ratio0AtEnd1 = false;
        } else {
            ratio0AtEnd1 = context.config().xfmr2Ratio0AtEnd1();
        }
        invertIidmAngle = context.config().invertXfmr2IidmAngle();
    }

    @Override
    public void convert() {
        double r = r1 + r2;
        double x = x1 + x2;

        TapChanger ratioTapChanger1 = getRatioTapChanger(rtc1, terminal1);
        TapChanger ratioTapChanger2 = getRatioTapChanger(rtc2, terminal2);
        TapChanger phaseTapChanger1 = getPhaseTapChanger(ptc1, terminal1, ratedU1, x);
        TapChanger phaseTapChanger2 = getPhaseTapChanger(ptc2, terminal2, ratedU2, x);

        TapChanger22 tapChanger22 = filterRatioPhaseRegulatingControl(ratioTapChanger1, phaseTapChanger1,
                ratioTapChanger2, phaseTapChanger2);

        TapChanger xRatioTapChanger1 = tapChanger22.ratioTapChanger1;
        TapChanger xPhaseTapChanger1 = tapChanger22.phaseTapChanger1;
        TapChanger xRatioTapChanger2 = tapChanger22.ratioTapChanger2;
        TapChanger xPhaseTapChanger2 = tapChanger22.phaseTapChanger2;

        TapChanger nRatioTapChanger2 = moveTapChangerFrom2To1(xRatioTapChanger2);
        TapChanger nPhaseTapChanger2 = moveTapChangerFrom2To1(xPhaseTapChanger2);

        TapChanger ratioTapChanger = combineTapChangers(xRatioTapChanger1, nRatioTapChanger2);
        TapChanger phaseTapChanger = combineTapChangers(xPhaseTapChanger1, nPhaseTapChanger2);

        RatioConversion rc0 = identityRatioConversion(r, x, g1, b1, g2, b2);
        if (!ratio0AtEnd1) {
            double a0 = ratedU2 / ratedU1;
            rc0 = moveRatioFrom2To1(a0, 0.0, r, x, g1, b1, g2, b2);
        }

        setToIidm(ratioTapChanger, phaseTapChanger, rc0.r, rc0.x, rc0.g1, rc0.b1, rc0.g2, rc0.b2);
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

    private void setToIidm(TapChanger ratioTapChanger, TapChanger phaseTapChanger, double r,
            double x, double g1, double b1, double g2, double b2) {
        // JAM_TODO. Split shunts una vez aprobada la modificacion del modelo
        TwoWindingsTransformerAdder adder = substation().newTwoWindingsTransformer()
                .setR(r)
                .setX(x)
                .setG(g1 + g2)
                .setB(b1 + b2)
                .setRatedU1(ratedU1)
                .setRatedU2(ratedU2);
        identify(adder);
        connect(adder);
        TwoWindingsTransformer tx = adder.add();
        convertedTerminals(tx.getTerminal1(), tx.getTerminal2());

        // as g2 and b2 are added at end1 g2 and b2 corrections can be deleted
        cleanTapChanger(ratioTapChanger, false, false, false, false, true, true);
        cleanTapChanger(phaseTapChanger, false, false, false, false, true, true);

        setToIidmRatioTapChanger(ratioTapChanger, tx);
        setToIidmPhaseTapChanger(phaseTapChanger, tx);
    }

    private void setToIidmRatioTapChanger(TapChanger rtc, Connectable<?> tx) {
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
        RatioTapChangerAdder rtca = newRatioTapChanger(tx, terminal);
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

    private void setToIidmPhaseTapChanger(TapChanger ptc, Connectable<?> tx) {
        if (ptc == null) {
            return;
        }
        boolean isRegulating = ptc.isRegulating();
        RegulationMode regulationMode = ptc.getRegulationMode();
        String terminal = ptc.getRegulationTerminal();
        Terminal regulationTerminal = terminal(tx, terminal);
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
            if (invertIidmAngle) {
                angle0 = -1 * angle0;
            }
            double r0 = step.getR();
            double x0 = step.getX();
            double b0 = step.getB1();
            double g0 = step.getG1();
            ptca.beginStep()
                    .setRho(1 / ratio0)
                    .setAlpha(-angle0)
                    .setR(r0)
                    .setX(x0)
                    .setB(b0)
                    .setG(g0)
                    .endStep();
        });
        ptca.add();
    }

    @Override
    protected RatioTapChangerAdder newRatioTapChanger(Connectable<?> tx, String terminal) {
        return ((TwoWindingsTransformer) tx).newRatioTapChanger();
    }

    @Override
    protected PhaseTapChangerAdder newPhaseTapChanger(Connectable<?> tx) {
        return ((TwoWindingsTransformer) tx).newPhaseTapChanger();
    }

    @Override
    protected Terminal terminal(Connectable<?> tx, String terminal) {
        if (tx instanceof TwoWindingsTransformer) {
            TwoWindingsTransformer t2x = (TwoWindingsTransformer) tx;
            if (terminal1.equals(terminal)) {
                return t2x.getTerminal1();
            } else if (terminal2.equals(terminal)) {
                return t2x.getTerminal2();
            }
        }
        return null;
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
    private final boolean     ratio0AtEnd1;
    private final boolean     invertIidmAngle;
    private final String      terminal1;
    private final String      terminal2;
    private final PropertyBag rtc1;
    private final PropertyBag rtc2;
    private final PropertyBag ptc1;
    private final PropertyBag ptc2;
}
