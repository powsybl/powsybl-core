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
            ratio0AtEnd1 = true;
        }
    }

    @Override
    public void convert() {
        double r = r1 + r2;
        double x = x1 + x2;
        double b1 = this.b1;
        double b2 = this.b2;
        double g1 = this.g1;
        double g2 = this.g2;

        TapChanger ratioTapChanger1 = getRatioTapChanger(rtc1, terminal1);
        TapChanger ratioTapChanger2 = getRatioTapChanger(rtc2, terminal2);
        TapChanger phaseTapChanger1 = getPhaseTapChanger(ptc1, terminal1, ratedU1, x);
        TapChanger phaseTapChanger2 = getPhaseTapChanger(ptc2, terminal2, ratedU2, x);

        TapChanger nratioTapChanger2 = moveTapChangerFrom2To1(ratioTapChanger2);
        TapChanger nphaseTapChanger2 = moveTapChangerFrom2To1(phaseTapChanger2);

        TapChanger ratioTapChanger = combineTapChangers(ratioTapChanger1, nratioTapChanger2);
        TapChanger phaseTapChanger = combineTapChangers(phaseTapChanger1, nphaseTapChanger2);

        if (!ratio0AtEnd1) {
            double a0 = ratedU2 / ratedU1;
            RatioConversion ratio = moveRatioFrom2To1(a0, 0.0, r, x, g1, b1, g2, b2);
            r = ratio.r;
            x = ratio.x;
            b1 = ratio.b1;
            g1 = ratio.g1;
            b2 = ratio.b2;
            g2 = ratio.g2;
        }

        setToIidm(ratioTapChanger, phaseTapChanger, r, x, g1, b1, g2, b2);
    }

    private void setToIidm(TapChanger ratioTapChanger, TapChanger phaseTapChanger, double r,
            double x, double g1, double b1, double g2, double b2) {
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
        PhaseTapChangerAdder ptca = newPhaseTapChanger(tx);
        ptca.setLowTapPosition(lowStep).setTapPosition((int) position)
                .setRegulating(isRegulating).setRegulationTerminal(regulationTerminal)
                .setRegulationMode(regulationMode).setRegulationValue(regulationValue);
        ptc.getSteps().forEach(step -> {
            double ratio0 = step.getRatio();
            double angle0 = step.getAngle();
            double r0 = step.getR();
            double x0 = step.getX();
            double b0 = step.getB1();
            double g0 = step.getG1();
            ptca.beginStep()
                    .setRho(1 / ratio0)
                    .setAlpha(angle0)
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

    private final double        r1;
    private final double        x1;
    private final double        b1;
    private final double        g1;
    private final double        r2;
    private final double        x2;
    private final double        b2;
    private final double        g2;
    private final double        ratedU1;
    private final double        ratedU2;
    private final boolean       ratio0AtEnd1;
    private final String        terminal1;
    private final String        terminal2;
    private final PropertyBag   rtc1;
    private final PropertyBag   rtc2;
    private final PropertyBag   ptc1;
    private final PropertyBag   ptc2;
}
