/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.LimitViolationUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class MergedLine implements Line {

    private final MergingViewIndex index;

    private final DanglingLine dl1;

    private final DanglingLine dl2;

    private String id;

    MergedLine(final MergingViewIndex index, final DanglingLine dl1, final DanglingLine dl2) {
        this.index = Objects.requireNonNull(index, "merging view index is null");
        this.dl1 = Objects.requireNonNull(dl1, "dangling line 1 is null");
        this.dl2 = Objects.requireNonNull(dl2, "dangling line 2 is null");
        this.id = dl1.getId().compareTo(dl2.getId()) < 0 ? dl1.getId() + " + " + dl2.getId() : dl2.getId() + " + " + dl1.getId();
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.LINE;
    }

    @Override
    public boolean isTieLine() {
        return false;
    }

    @Override
    public MergingView getNetwork() {
        return index.getView();
    }

    @Override
    public Terminal getTerminal(final Side side) {
        switch (side) {
            case ONE:
                return getTerminal1();
            case TWO:
                return getTerminal2();
            default:
                throw new AssertionError();
        }
    }

    @Override
    public Terminal getTerminal1() {
        return index.getTerminal(dl1.getTerminal());
    }

    @Override
    public Terminal getTerminal2() {
        return index.getTerminal(dl2.getTerminal());
    }

    @Override
    public CurrentLimits getCurrentLimits(final Side side) {
        switch (side) {
            case ONE:
                return getCurrentLimits1();
            case TWO:
                return getCurrentLimits2();
            default:
                throw new AssertionError();
        }
    }

    @Override
    public CurrentLimits getCurrentLimits1() {
        return dl1.getCurrentLimits();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return dl1.newCurrentLimits();
    }

    @Override
    public CurrentLimits getCurrentLimits2() {
        return dl2.getCurrentLimits();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return dl2.newCurrentLimits();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public double getR() {
        return dl1.getR() + dl2.getR();
    }

    @Override
    public Line setR(final double r) {
        dl1.setR(r / 2.0d);
        dl2.setR(r / 2.0d);
        return this;
    }

    @Override
    public double getX() {
        return dl1.getX() + dl2.getX();
    }

    @Override
    public Line setX(final double x) {
        dl1.setX(x / 2.0d);
        dl2.setX(x / 2.0d);
        return this;
    }

    @Override
    public double getG1() {
        return dl1.getG();
    }

    @Override
    public Line setG1(final double g1) {
        dl1.setG(g1);
        return this;
    }

    @Override
    public double getG2() {
        return dl2.getG();
    }

    @Override
    public Line setG2(final double g2) {
        dl2.setG(g2);
        return this;
    }

    @Override
    public double getB1() {
        return dl1.getB();
    }

    @Override
    public Line setB1(final double b1) {
        dl1.setB(b1);
        return this;
    }

    @Override
    public double getB2() {
        return dl2.getB();
    }

    @Override
    public Line setB2(final double b2) {
        dl2.setB(b2);
        return this;
    }

    @Override
    public Terminal getTerminal(final String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);

        Terminal termDl1 = dl1.getTerminal();
        Terminal termDl2 = dl2.getTerminal();
        if (voltageLevelId.equals(termDl1.getVoltageLevel().getId())) {
            return termDl1;
        } else if (voltageLevelId.equals(termDl2.getVoltageLevel().getId())) {
            return termDl2;
        } else {
            throw new PowsyblException("No terminal connected to voltage level " + voltageLevelId);
        }
    }

    @Override
    public Side getSide(final Terminal terminal) {
        Objects.requireNonNull(terminal);

        Terminal term = terminal;
        if (term instanceof AbstractAdapter) {
            term = ((AbstractAdapter<Terminal>) term).getDelegate();
        }
        if (term == dl1.getTerminal()) {
            return Side.ONE;
        } else if (term == dl2.getTerminal()) {
            return Side.TWO;
        } else {
            throw new PowsyblException("The terminal is not connected to this branch");
        }
    }

    @Override
    public boolean isOverloaded() {
        return isOverloaded(1.0f);
    }

    @Override
    public boolean isOverloaded(final float limitReduction) {
        return checkPermanentLimit1(limitReduction) || checkPermanentLimit2(limitReduction);
    }

    @Override
    public int getOverloadDuration() {
        Branch.Overload o1 = checkTemporaryLimits1();
        Branch.Overload o2 = checkTemporaryLimits2();
        int duration1 = o1 != null ? o1.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        int duration2 = o2 != null ? o2.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        return Math.min(duration1, duration2);
    }

    @Override
    public boolean checkPermanentLimit(final Side side, final float limitReduction) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkPermanentLimit1(limitReduction);

            case TWO:
                return checkPermanentLimit2(limitReduction);

            default:
                throw new AssertionError();
        }
    }

    @Override
    public boolean checkPermanentLimit(final Side side) {
        return checkPermanentLimit(side, 1f);
    }

    @Override
    public boolean checkPermanentLimit1(final float limitReduction) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.ONE, limitReduction, getTerminal1().getI());
    }

    @Override
    public boolean checkPermanentLimit1() {
        return checkPermanentLimit1(1f);
    }

    @Override
    public boolean checkPermanentLimit2(final float limitReduction) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.TWO, limitReduction, getTerminal2().getI());
    }

    @Override
    public boolean checkPermanentLimit2() {
        return checkPermanentLimit2(1f);
    }

    @Override
    public Overload checkTemporaryLimits(final Side side, final float limitReduction) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkTemporaryLimits1(limitReduction);

            case TWO:
                return checkTemporaryLimits2(limitReduction);

            default:
                throw new AssertionError();
        }
    }

    @Override
    public Overload checkTemporaryLimits(final Side side) {
        return checkTemporaryLimits(side, 1f);
    }

    @Override
    public Overload checkTemporaryLimits1(final float limitReduction) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.ONE, limitReduction, getTerminal1().getI());
    }

    @Override
    public Overload checkTemporaryLimits1() {
        return checkTemporaryLimits1(1f);
    }

    @Override
    public Overload checkTemporaryLimits2(final float limitReduction) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.TWO, limitReduction, getTerminal2().getI());
    }

    @Override
    public Overload checkTemporaryLimits2() {
        return checkTemporaryLimits2(1f);
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        return Stream.concat(dl1.getTerminals().stream(),
                             dl2.getTerminals().stream())
                     .map(index::getTerminal)
                     .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return this.id;
    }

    @Override
    public boolean hasProperty() {
        return dl1.hasProperty() || dl2.hasProperty();
    }

    @Override
    public boolean hasProperty(final String key) {
        return dl1.hasProperty(key) || dl2.hasProperty(key);
    }

    @Override
    public Set<String> getPropertyNames() {
        final Set<String> names = dl1.getPropertyNames();
        names.addAll(dl2.getPropertyNames());
        return names;
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public String setProperty(final String key, final String value) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public String getProperty(final String key) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public String getProperty(final String key, final String defaultValue) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public <E extends Extension<Line>> void addExtension(final Class<? super E> type, final E extension) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public <E extends Extension<Line>> E getExtension(final Class<? super E> type) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public <E extends Extension<Line>> E getExtensionByName(final String name) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public <E extends Extension<Line>> boolean removeExtension(final Class<E> type) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public <E extends Extension<Line>> Collection<E> getExtensions() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
