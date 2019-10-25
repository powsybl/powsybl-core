/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.List;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TwoWindingsTransformerAdapter extends AbstractIdentifiableAdapter<TwoWindingsTransformer> implements TwoWindingsTransformer {

    protected TwoWindingsTransformerAdapter(final TwoWindingsTransformer delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public PhaseTapChangerAdapter getPhaseTapChanger() {
        return getIndex().getPhaseTapChanger(getDelegate().getPhaseTapChanger());
    }

    @Override
    public RatioTapChangerAdapter getRatioTapChanger() {
        return getIndex().getRatioTapChanger(getDelegate().getRatioTapChanger());
    }

    @Override
    public TerminalAdapter getTerminal1() {
        return getIndex().getTerminal(getDelegate().getTerminal1());
    }

    @Override
    public TerminalAdapter getTerminal2() {
        return getIndex().getTerminal(getDelegate().getTerminal2());
    }

    @Override
    public TerminalAdapter getTerminal(final Side side) {
        return getIndex().getTerminal(getDelegate().getTerminal(side));
    }

    @Override
    public TerminalAdapter getTerminal(final String voltageLevelId) {
        return getIndex().getTerminal(getDelegate().getTerminal(voltageLevelId));
    }

    @Override
    public Side getSide(final Terminal terminal) {
        Terminal param = terminal;
        if (terminal instanceof AbstractAdapter<?>) {
            param = ((AbstractAdapter<Terminal>) terminal).getDelegate();
        }
        return getDelegate().getSide(param);
    }

    @Override
    public List<? extends TerminalAdapter> getTerminals() {
        return getDelegate().getTerminals().stream()
                .map(getIndex()::getTerminal)
                .collect(Collectors.toList());
    }

    @Override
    public RatioTapChangerAdderAdapter newRatioTapChanger() {
        return new RatioTapChangerAdderAdapter(getDelegate().newRatioTapChanger(), getIndex());
    }

    @Override
    public PhaseTapChangerAdderAdapter newPhaseTapChanger() {
        return new PhaseTapChangerAdderAdapter(getDelegate().newPhaseTapChanger(), getIndex());
    }

    @Override
    public SubstationAdapter getSubstation() {
        return getIndex().getSubstation(getDelegate().getSubstation());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public CurrentLimits getCurrentLimits(final Side side) {
        return getDelegate().getCurrentLimits(side);
    }

    @Override
    public CurrentLimits getCurrentLimits1() {
        return getDelegate().getCurrentLimits1();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return getDelegate().newCurrentLimits1();
    }

    @Override
    public CurrentLimits getCurrentLimits2() {
        return getDelegate().getCurrentLimits2();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return getDelegate().newCurrentLimits2();
    }

    @Override
    public boolean isOverloaded() {
        return getDelegate().isOverloaded();
    }

    @Override
    public boolean isOverloaded(final float limitReduction) {
        return getDelegate().isOverloaded(limitReduction);
    }

    @Override
    public int getOverloadDuration() {
        return getDelegate().getOverloadDuration();
    }

    @Override
    public boolean checkPermanentLimit(final Side side, final float limitReduction) {
        return getDelegate().checkPermanentLimit(side, limitReduction);
    }

    @Override
    public boolean checkPermanentLimit(final Side side) {
        return getDelegate().checkPermanentLimit(side);
    }

    @Override
    public boolean checkPermanentLimit1(final float limitReduction) {
        return getDelegate().checkPermanentLimit1(limitReduction);
    }

    @Override
    public boolean checkPermanentLimit1() {
        return getDelegate().checkPermanentLimit1();
    }

    @Override
    public boolean checkPermanentLimit2(final float limitReduction) {
        return getDelegate().checkPermanentLimit2(limitReduction);
    }

    @Override
    public boolean checkPermanentLimit2() {
        return getDelegate().checkPermanentLimit2();
    }

    @Override
    public Overload checkTemporaryLimits(final Side side, final float limitReduction) {
        return getDelegate().checkTemporaryLimits(side, limitReduction);
    }

    @Override
    public Overload checkTemporaryLimits(final Side side) {
        return getDelegate().checkTemporaryLimits(side);
    }

    @Override
    public Overload checkTemporaryLimits1(final float limitReduction) {
        return getDelegate().checkTemporaryLimits1(limitReduction);
    }

    @Override
    public Overload checkTemporaryLimits1() {
        return getDelegate().checkTemporaryLimits1();
    }

    @Override
    public Overload checkTemporaryLimits2(final float limitReduction) {
        return getDelegate().checkTemporaryLimits2(limitReduction);
    }

    @Override
    public Overload checkTemporaryLimits2() {
        return getDelegate().checkTemporaryLimits2();
    }

    @Override
    public ConnectableType getType() {
        return getDelegate().getType();
    }

    @Override
    public double getR() {
        return getDelegate().getR();
    }

    @Override
    public TwoWindingsTransformerAdapter setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public double getX() {
        return getDelegate().getX();
    }

    @Override
    public TwoWindingsTransformerAdapter setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public double getG() {
        return getDelegate().getG();
    }

    @Override
    public TwoWindingsTransformerAdapter setG(final double g) {
        getDelegate().setG(g);
        return this;
    }

    @Override
    public double getB() {
        return getDelegate().getB();
    }

    @Override
    public TwoWindingsTransformerAdapter setB(final double b) {
        getDelegate().setB(b);
        return this;
    }

    @Override
    public double getRatedU1() {
        return getDelegate().getRatedU1();
    }

    @Override
    public TwoWindingsTransformerAdapter setRatedU1(final double ratedU1) {
        getDelegate().setRatedU1(ratedU1);
        return this;
    }

    @Override
    public double getRatedU2() {
        return getDelegate().getRatedU2();
    }

    @Override
    public TwoWindingsTransformerAdapter setRatedU2(final double ratedU2) {
        getDelegate().setRatedU2(ratedU2);
        return this;
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
