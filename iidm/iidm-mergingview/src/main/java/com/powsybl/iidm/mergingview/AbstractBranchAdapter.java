/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

import java.util.Collection;

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
    public Collection<OperationalLimits> getOperationalLimits1() {
        return getDelegate().getOperationalLimits1();
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
    public ApparentPowerLimits getApparentPowerLimits1() {
        return getDelegate().getApparentPowerLimits1();
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return getDelegate().newApparentPowerLimits1();
    }

    @Override
    public ActivePowerLimits getActivePowerLimits1() {
        return getDelegate().getActivePowerLimits1();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return getDelegate().newActivePowerLimits1();
    }

    @Override
    public Collection<OperationalLimits> getOperationalLimits2() {
        return getDelegate().getOperationalLimits2();
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
    public ApparentPowerLimits getApparentPowerLimits2() {
        return getDelegate().getApparentPowerLimits2();
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return getDelegate().newApparentPowerLimits2();
    }

    @Override
    public ActivePowerLimits getActivePowerLimits2() {
        return getDelegate().getActivePowerLimits2();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        return getDelegate().newActivePowerLimits2();
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
    public boolean checkPermanentLimit(final Side side, final float limitReduction, LimitType type) {
        return getDelegate().checkPermanentLimit(side, limitReduction, type);
    }

    @Override
    public boolean checkPermanentLimit(final Side side, LimitType type) {
        return getDelegate().checkPermanentLimit(side, type);
    }

    @Override
    public boolean checkPermanentLimit1(final float limitReduction, LimitType type) {
        return getDelegate().checkPermanentLimit1(limitReduction, type);
    }

    @Override
    public boolean checkPermanentLimit1(LimitType type) {
        return getDelegate().checkPermanentLimit1(type);
    }

    @Override
    public boolean checkPermanentLimit2(final float limitReduction, LimitType type) {
        return getDelegate().checkPermanentLimit2(limitReduction, type);
    }

    @Override
    public boolean checkPermanentLimit2(LimitType type) {
        return getDelegate().checkPermanentLimit2(type);
    }

    @Override
    public Overload checkTemporaryLimits(final Side side, final float limitReduction, LimitType type) {
        return getDelegate().checkTemporaryLimits(side, limitReduction, type);
    }

    @Override
    public Overload checkTemporaryLimits(final Side side, LimitType type) {
        return getDelegate().checkTemporaryLimits(side, type);
    }

    @Override
    public Overload checkTemporaryLimits1(final float limitReduction, LimitType type) {
        return getDelegate().checkTemporaryLimits1(limitReduction, type);
    }

    @Override
    public Overload checkTemporaryLimits1(LimitType type) {
        return getDelegate().checkTemporaryLimits1(type);
    }

    @Override
    public Overload checkTemporaryLimits2(final float limitReduction, LimitType type) {
        return getDelegate().checkTemporaryLimits2(limitReduction, type);
    }

    @Override
    public Overload checkTemporaryLimits2(LimitType type) {
        return getDelegate().checkTemporaryLimits2(type);
    }
}
