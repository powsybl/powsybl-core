/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
abstract class AbstractBranchAdapter<I extends Branch<I>> extends AbstractConnectableAdapter<I> implements Branch<I> {

    protected AbstractBranchAdapter(I delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Terminal getTerminal1() {
        return getIndex().getTerminal(getDelegate().getTerminal1());
    }

    @Override
    public Terminal getTerminal2() {
        return getIndex().getTerminal(getDelegate().getTerminal2());
    }

    @Override
    public Terminal getTerminal(final Side side) {
        return getIndex().getTerminal(getDelegate().getTerminal(side));
    }

    @Override
    public Terminal getTerminal(final String voltageLevelId) {
        return getIndex().getTerminal(getDelegate().getTerminal(voltageLevelId));
    }

    @Override
    public Side getSide(final Terminal terminal) {
        Terminal terminalCopied = terminal;
        if (terminalCopied instanceof TerminalAdapter) {
            terminalCopied = ((TerminalAdapter) terminalCopied).getDelegate();
        }
        return getDelegate().getSide(terminalCopied);
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
}
