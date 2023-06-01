/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.*;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TieLineAdapter extends AbstractIdentifiableAdapter<TieLine> implements TieLine {

    private final BoundaryLine boundaryLine1;
    private final BoundaryLine boundaryLine2;

    TieLineAdapter(final TieLine delegate, final MergingViewIndex index) {
        super(delegate, index);
        this.boundaryLine1 = index.getBoundaryLine(delegate.getBoundaryLine1());
        this.boundaryLine2 = index.getBoundaryLine(delegate.getBoundaryLine2());
    }

    @Override
    public final void remove() {
        throw MergingView.createNotImplementedException();
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public String getUcteXnodeCode() {
        return getDelegate().getUcteXnodeCode();
    }

    @Override
    public BoundaryLine getBoundaryLine1() {
        return boundaryLine1;
    }

    @Override
    public BoundaryLine getBoundaryLine2() {
        return boundaryLine2;
    }

    @Override
    public BoundaryLine getBoundaryLine(Branch.Side side) {
        switch (side) {
            case ONE:
                return boundaryLine1;
            case TWO:
                return boundaryLine2;
            default:
                throw new IllegalStateException("Unexpected side: " + side);
        }
    }

    @Override
    public BoundaryLine getBoundaryLine(String voltageLevelId) {

        if (boundaryLine1.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return boundaryLine1;
        }
        if (boundaryLine2.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return boundaryLine2;
        }
        return null;
    }

    @Override
    public double getR() {
        return getDelegate().getR();
    }

    @Override
    public double getX() {
        return getDelegate().getX();
    }

    @Override
    public double getG1() {
        return getDelegate().getG1();
    }

    @Override
    public double getG2() {
        return getDelegate().getG2();
    }

    @Override
    public double getB1() {
        return getDelegate().getB1();
    }

    @Override
    public double getB2() {
        return getDelegate().getB2();
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

    @Override
    public Collection<OperationalLimits> getOperationalLimits1() {
        return getDelegate().getOperationalLimits1();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits1() {
        return getDelegate().getCurrentLimits1();
    }

    @Override
    public CurrentLimits getNullableCurrentLimits1() {
        return getDelegate().getNullableCurrentLimits1();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits1() {
        return getDelegate().getActivePowerLimits1();
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits1() {
        return getDelegate().getNullableActivePowerLimits1();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits1() {
        return getDelegate().getApparentPowerLimits1();
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits1() {
        return getDelegate().getNullableApparentPowerLimits1();
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
    public Collection<OperationalLimits> getOperationalLimits2() {
        return getDelegate().getOperationalLimits2();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return getDelegate().newActivePowerLimits1();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits2() {
        return getDelegate().getCurrentLimits2();
    }

    @Override
    public CurrentLimits getNullableCurrentLimits2() {
        return getDelegate().getNullableCurrentLimits2();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits2() {
        return getDelegate().getActivePowerLimits2();
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits2() {
        return getDelegate().getNullableActivePowerLimits2();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return getDelegate().getApparentPowerLimits2();
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits2() {
        return getDelegate().getNullableApparentPowerLimits2();
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
