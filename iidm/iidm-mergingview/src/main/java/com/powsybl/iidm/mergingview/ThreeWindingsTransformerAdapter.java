/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This adaptation hide true implementation of {@link ThreeWindingsTransformer}.
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ThreeWindingsTransformerAdapter extends AbstractIdentifiableAdapter<ThreeWindingsTransformer> implements ThreeWindingsTransformer {

    private static class LegAdapter extends AbstractAdapter<ThreeWindingsTransformer.Leg> implements ThreeWindingsTransformer.Leg {

        LegAdapter(final ThreeWindingsTransformer.Leg delegate, final MergingViewIndex index) {
            super(delegate, index);
        }

        @Override
        public Terminal getTerminal() {
            return getIndex().getTerminal(getDelegate().getTerminal());
        }

        @Override
        public PhaseTapChangerAdder newPhaseTapChanger() {
            return new PhaseTapChangerAdderAdapter(getDelegate().newPhaseTapChanger(), getIndex());
        }

        @Override
        public PhaseTapChanger getPhaseTapChanger() {
            return getIndex().getPhaseTapChanger(getDelegate().getPhaseTapChanger());
        }

        @Override
        public RatioTapChangerAdder newRatioTapChanger() {
            return new RatioTapChangerAdderAdapter(getDelegate().newRatioTapChanger(), getIndex());
        }

        @Override
        public RatioTapChanger getRatioTapChanger() {
            return getIndex().getRatioTapChanger(getDelegate().getRatioTapChanger());
        }

        // -------------------------------
        // Simple delegated methods ------
        // -------------------------------
        @Override
        public double getR() {
            return getDelegate().getR();
        }

        @Override
        public ThreeWindingsTransformer.Leg setR(final double r) {
            getDelegate().setR(r);
            return this;
        }

        @Override
        public double getX() {
            return getDelegate().getX();
        }

        @Override
        public ThreeWindingsTransformer.Leg setX(final double x) {
            getDelegate().setX(x);
            return this;
        }

        @Override
        public double getRatedU() {
            return getDelegate().getRatedU();
        }

        @Override
        public ThreeWindingsTransformer.Leg setRatedU(final double ratedU) {
            getDelegate().setRatedU(ratedU);
            return this;
        }

        @Override
        public Collection<OperationalLimits> getOperationalLimits() {
            return getDelegate().getOperationalLimits();
        }

        @Override
        public CurrentLimits getCurrentLimits() {
            return getDelegate().getCurrentLimits();
        }

        @Override
        public CurrentLimitsAdder newCurrentLimits() {
            return getDelegate().newCurrentLimits();
        }

        @Override
        public ApparentPowerLimits getApparentPowerLimits() {
            return getDelegate().getApparentPowerLimits();
        }

        @Override
        public ApparentPowerLimitsAdder newApparentPowerLimits() {
            return getDelegate().newApparentPowerLimits();
        }

        @Override
        public ActivePowerLimits getActivePowerLimits() {
            return getDelegate().getActivePowerLimits();
        }

        @Override
        public ActivePowerLimitsAdder newActivePowerLimits() {
            return getDelegate().newActivePowerLimits();
        }

        @Override
        public double getG() {
            return getDelegate().getG();
        }

        @Override
        public ThreeWindingsTransformer.Leg setG(final double g) {
            getDelegate().setG(g);
            return this;
        }

        @Override
        public double getB() {
            return getDelegate().getB();
        }

        @Override
        public ThreeWindingsTransformer.Leg setB(final double b) {
            getDelegate().setB(b);
            return this;
        }

        @Override
        public double getRatedS() {
            return getDelegate().getRatedS();
        }

        @Override
        public ThreeWindingsTransformer.Leg setRatedS(double ratedS) {
            getDelegate().setRatedS(ratedS);
            return this;
        }
    }

    private LegAdapter leg1;

    private LegAdapter leg2;

    private LegAdapter leg3;

    ThreeWindingsTransformerAdapter(final ThreeWindingsTransformer delegate, final MergingViewIndex index) {
        super(delegate, index);
        // no need to store LegAdapter in MergingViewIndex
        leg1 = new LegAdapter(getDelegate().getLeg1(), getIndex());
        leg2 = new LegAdapter(getDelegate().getLeg2(), getIndex());
        leg3 = new LegAdapter(getDelegate().getLeg3(), getIndex());
    }

    @Override
    public ThreeWindingsTransformer.Leg getLeg1() {
        return leg1;
    }

    @Override
    public ThreeWindingsTransformer.Leg getLeg2() {
        return leg2;
    }

    @Override
    public ThreeWindingsTransformer.Leg getLeg3() {
        return leg3;
    }

    @Override
    public Stream<Leg> getLegStream() {
        return Stream.of(leg1, leg2, leg3);
    }

    @Override
    public List<Leg> getLegs() {
        return Arrays.asList(leg1, leg2, leg3);
    }

    @Override
    public List<? extends TerminalAdapter> getTerminals() {
        return getDelegate().getTerminals().stream()
                .map(getIndex()::getTerminal)
                .collect(Collectors.toList());
    }

    @Override
    public Terminal getTerminal(final Side side) {
        return getIndex().getTerminal(getDelegate().getTerminal(side));
    }

    @Override
    public Side getSide(final Terminal side) {
        Terminal terminal = side;
        if (terminal instanceof TerminalAdapter) {
            terminal = ((TerminalAdapter) terminal).getDelegate();
        }
        return getDelegate().getSide(terminal);
    }

    @Override
    public Substation getSubstation() {
        return getIndex().getSubstation(getDelegate().getSubstation());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ConnectableType getType() {
        return getDelegate().getType();
    }

    @Override
    public double getRatedU0() {
        return getDelegate().getRatedU0();
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.createNotImplementedException();
    }
}
