package com.powsybl.cgmes.conversion.test;

import java.util.function.Consumer;

import com.powsybl.cgmes.PowerFlow;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.util.BranchData;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class DebugPhaseTapChanger {

    public DebugPhaseTapChanger(TwoWindingsTransformer tx, int side, PowerFlow expected) {
        this.tx = tx;
        this.side = side;
        this.expected = expected;
    }

    public void debug() {
        Consumer<TwoWindingsTransformer> nop = tx -> {
        };
        Consumer<TwoWindingsTransformer> minusAngle = tx -> {
            tx.getPhaseTapChanger().getCurrentStep()
                    .setAlpha(-tx.getPhaseTapChanger().getCurrentStep().getAlpha());
        };
        Consumer<TwoWindingsTransformer> invRho = tx -> {
            tx.getPhaseTapChanger().getCurrentStep()
                    .setRho(1 / tx.getPhaseTapChanger().getCurrentStep().getRho());
        };
        explorePhaseTapPositions("nop", nop, nop);
        explorePhaseTapPositions("-angle", minusAngle, minusAngle);
        explorePhaseTapPositions("1/rho", invRho, invRho);
    }

    private void explorePhaseTapPositions(
            String option,
            Consumer<TwoWindingsTransformer> pre,
            Consumer<TwoWindingsTransformer> post) {
        System.err.printf("current = %d%n", tx.getPhaseTapChanger().getTapPosition());
        System.err.printf("ratedU1 = %10.4f%n", tx.getRatedU1());
        System.err.printf("ratedU2 = %10.4f%n", tx.getRatedU2());
        System.err.printf("v,a1    = %10.4f %10.4f%n",
                tx.getTerminal1().getBusView().getBus().getV(),
                tx.getTerminal1().getBusView().getBus().getAngle());
        System.err.printf("v,a2    = %10.4f %10.4f%n",
                tx.getTerminal2().getBusView().getBus().getV(),
                tx.getTerminal2().getBusView().getBus().getAngle());
        int backup = tx.getPhaseTapChanger().getTapPosition();
        for (int k = tx.getPhaseTapChanger().getLowTapPosition(); k <= tx.getPhaseTapChanger()
                .getHighTapPosition(); k++) {
            tx.getPhaseTapChanger().setTapPosition(k);
            pre.accept(tx);
            BranchData b = new BranchData(tx, (float) 0.0, false, false);
            debugTapPosition(option, k, b);
            post.accept(tx);
        }
        tx.getPhaseTapChanger().setTapPosition(backup);
    }

    private void debugTapPosition(String option, int tap, BranchData b) {
        Branch.Side bside = Branch.Side.values()[side - 1];
        PowerFlow actual = new PowerFlow(b.getComputedP(bside), b.getComputedQ(bside));
        double d = Math.abs(actual.p() - expected.p()) + Math.abs(actual.q() - expected.q());
        double alpha = tx.getPhaseTapChanger().getCurrentStep().getAlpha();
        double rho = tx.getPhaseTapChanger().getCurrentStep().getRho();
        boolean header = tap == tx.getPhaseTapChanger().getLowTapPosition();
        if (header) {
            System.err.printf(
                    "    option tap    alpha         rho           p%d            q%d          d%d%n",
                    side, side, side);
            System.err.printf(
                    "    ------ ---    ----------    ----------    ----------    ----------    ----------%n");
        }
        System.err.printf(
                "    %6s %3d    %10.4f    %10.4f    %10.4f    %10.4f    %10.4f%n",
                option,
                tap,
                alpha,
                rho,
                actual.p(),
                actual.q(),
                d);
        if (d < 20.0) {
            System.err.println("GOOD CANDIDATE");
        }
    }

    private final TwoWindingsTransformer tx;
    private final int                    side;
    private final PowerFlow              expected;
}
