/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

import java.util.Optional;

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
    public CurrentLimits getCurrentLimits1(String id) {
        return getDelegate().getCurrentLimits1(id);
    }

    @Override
    public Optional<CurrentLimits> getActiveCurrentLimits1() {
        return getDelegate().getActiveCurrentLimits1();
    }

    @Override
    public void setActiveCurrentLimits1(String id) {
        getDelegate().setActiveCurrentLimits1(id);
    }

    @Override
    public CurrentLimitsSet getCurrentLimitsSet1() {
        return getDelegate().getCurrentLimitsSet1();
    }

    @Override
    public ActivePowerLimits getActivePowerLimits1(String id) {
        return getDelegate().getActivePowerLimits1(id);
    }

    @Override
    public Optional<ActivePowerLimits> getActiveActivePowerLimits1() {
        return getDelegate().getActiveActivePowerLimits1();
    }

    @Override
    public void setActiveActivePowerLimits1(String id) {
        getDelegate().setActiveActivePowerLimits1(id);
    }

    @Override
    public ActivePowerLimitsSet getActivePowerLimitsSet1() {
        return getDelegate().getActivePowerLimitsSet1();
    }

    @Override
    public ApparentPowerLimits getApparentPowerLimits1(String id) {
        return getDelegate().getApparentPowerLimits1(id);
    }

    @Override
    public Optional<ApparentPowerLimits> getActiveApparentPowerLimits1() {
        return getDelegate().getActiveApparentPowerLimits1();
    }

    @Override
    public void setActiveApparentPowerLimits1(String id) {
        getDelegate().setActiveApparentPowerLimits1(id);
    }

    @Override
    public ApparentPowerLimitsSet getApparentPowerLimitsSet1() {
        return getDelegate().getApparentPowerLimitsSet1();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return getDelegate().newCurrentLimits1();
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return getDelegate().newApparentPowerLimits1();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return getDelegate().newActivePowerLimits1();
    }

    @Override
    public CurrentLimits getCurrentLimits2(String id) {
        return getDelegate().getCurrentLimits2(id);
    }

    @Override
    public Optional<CurrentLimits> getActiveCurrentLimits2() {
        return getDelegate().getActiveCurrentLimits2();
    }

    @Override
    public void setActiveCurrentLimits2(String id) {
        getDelegate().setActiveCurrentLimits2(id);
    }

    @Override
    public CurrentLimitsSet getCurrentLimitsSet2() {
        return getCurrentLimitsSet2();
    }

    @Override
    public ActivePowerLimits getActivePowerLimits2(String id) {
        return getDelegate().getActivePowerLimits2(id);
    }

    @Override
    public Optional<ActivePowerLimits> getActiveActivePowerLimits2() {
        return getDelegate().getActiveActivePowerLimits2();
    }

    @Override
    public void setActiveActivePowerLimits2(String id) {
        getDelegate().setActiveActivePowerLimits2(id);
    }

    @Override
    public ActivePowerLimitsSet getActivePowerLimitsSet2() {
        return getDelegate().getActivePowerLimitsSet2();
    }

    @Override
    public ApparentPowerLimits getApparentPowerLimits2(String id) {
        return getDelegate().getApparentPowerLimits2(id);
    }

    @Override
    public Optional<ApparentPowerLimits> getActiveApparentPowerLimits2() {
        return getDelegate().getActiveApparentPowerLimits2();
    }

    @Override
    public void setActiveApparentPowerLimits2(String id) {
        getDelegate().setActiveApparentPowerLimits2(id);
    }

    @Override
    public ApparentPowerLimitsSet getApparentPowerLimitsSet2() {
        return getDelegate().getApparentPowerLimitsSet2();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return getDelegate().newCurrentLimits2();
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return getDelegate().newApparentPowerLimits2();
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
