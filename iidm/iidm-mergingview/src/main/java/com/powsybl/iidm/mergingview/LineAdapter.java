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
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LineAdapter extends AbstractIdentifiableAdapter<Line> implements Line {

    protected LineAdapter(final Line delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public List<? extends TerminalAdapter> getTerminals() {
        return getDelegate().getTerminals().stream().map(getIndex()::getTerminal).distinct().collect(Collectors.toList());
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
    public LineAdapter setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public double getX() {
        return getDelegate().getX();
    }

    @Override
    public LineAdapter setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public double getG1() {
        return getDelegate().getG1();
    }

    @Override
    public LineAdapter setG1(final double g1) {
        getDelegate().setG1(g1);
        return this;
    }

    @Override
    public double getG2() {
        return getDelegate().getG2();
    }

    @Override
    public LineAdapter setG2(final double g2) {
        getDelegate().setG2(g2);
        return this;
    }

    @Override
    public double getB1() {
        return getDelegate().getB1();
    }

    @Override
    public LineAdapter setB1(final double b1) {
        getDelegate().setB1(b1);
        return this;
    }

    @Override
    public double getB2() {
        return getDelegate().getB2();
    }

    @Override
    public LineAdapter setB2(final double b2) {
        getDelegate().setB2(b2);
        return this;
    }

    @Override
    public boolean isTieLine() {
        return getDelegate().isTieLine();
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
