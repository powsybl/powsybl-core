/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Identifiables;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class MergedLine implements Line {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergedLine.class);

    private final MergingViewIndex index;

    private final DanglingLine dl1;

    private final DanglingLine dl2;

    private final String id;

    private final String name;

    private final Properties properties = new Properties();

    MergedLine(final MergingViewIndex index, final DanglingLine dl1, final DanglingLine dl2, boolean ensureIdUnicity) {
        this.index = Objects.requireNonNull(index, "merging view index is null");
        this.dl1 = Objects.requireNonNull(dl1, "dangling line 1 is null");
        this.dl2 = Objects.requireNonNull(dl2, "dangling line 2 is null");
        this.id = ensureIdUnicity ? Identifiables.getUniqueId(buildId(dl1, dl2), index::contains) : buildId(dl1, dl2);
        this.name = buildName(dl1, dl2);
        mergeProperties(dl1, dl2);
    }

    MergedLine(final MergingViewIndex index, final DanglingLine dl1, final DanglingLine dl2) {
        this(index, dl1, dl2, false);
    }

    private static String buildId(final DanglingLine dl1, final DanglingLine dl2) {
        String id;
        if (dl1.getId().compareTo(dl2.getId()) < 0) {
            id = dl1.getId() + " + " + dl2.getId();
        } else {
            id = dl2.getId() + " + " + dl1.getId();
        }
        return id;
    }

    private static String buildName(final DanglingLine dl1, final DanglingLine dl2) {
        String name;
        int compareResult = dl1.getName().compareTo(dl2.getName());
        if (compareResult == 0) {
            name = dl1.getName();
        } else if (compareResult < 0) {
            name = dl1.getName() + " + " + dl2.getName();
        } else {
            name = dl2.getName() + " + " + dl1.getName();
        }
        return name;
    }

    private void mergeProperties(DanglingLine dl1, DanglingLine dl2) {
        Set<String> dl1Properties = dl1.getPropertyNames();
        Set<String> dl2Properties = dl2.getPropertyNames();
        Set<String> commonProperties = Sets.intersection(dl1Properties, dl2Properties);
        Sets.difference(dl1Properties, commonProperties).forEach(prop -> properties.setProperty(prop, dl1.getProperty(prop)));
        Sets.difference(dl2Properties, commonProperties).forEach(prop -> properties.setProperty(prop, dl2.getProperty(prop)));
        commonProperties.forEach(prop -> {
            if (dl1.getProperty(prop).equals(dl2.getProperty(prop))) {
                properties.setProperty(prop, dl1.getProperty(prop));
            } else if (dl1.getProperty(prop).isEmpty()) {
                LOGGER.warn("Inconsistencies of property '{}' between both sides of merged line. Side 1 is empty, keeping side 2 value '{}'", prop, dl2.getProperty(prop));
                properties.setProperty(prop, dl2.getProperty(prop));
            } else if (dl2.getProperty(prop).isEmpty()) {
                LOGGER.warn("Inconsistencies of property '{}' between both sides of merged line. Side 2 is empty, keeping side 1 value '{}'", prop, dl1.getProperty(prop));
                properties.setProperty(prop, dl1.getProperty(prop));
            } else {
                LOGGER.error("Inconsistencies of property '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Removing the property of merged line", prop, dl1.getProperty(prop), dl2.getProperty(prop));
            }
        });
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
                throw new AssertionError("Unexpected side value: " + side);
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
                throw new AssertionError("Unexpected side value: " + side);
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

        Terminal terminal1 = dl1.getTerminal();
        Terminal terminal2 = dl2.getTerminal();
        if (voltageLevelId.equals(terminal1.getVoltageLevel().getId())) {
            return terminal1;
        } else if (voltageLevelId.equals(terminal2.getVoltageLevel().getId())) {
            return terminal2;
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
                throw new AssertionError("Unexpected side value: " + side);
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
                throw new AssertionError("Unexpected side value: " + side);
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
        return name;
    }

    @Override
    public boolean hasProperty() {
        return !properties.isEmpty();
    }

    @Override
    public boolean hasProperty(final String key) {
        return properties.containsKey(key);
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet().stream().map(Object::toString).collect(Collectors.toSet());
    }

    @Override
    public String getProperty(final String key) {
        Object val = properties.get(key);
        return val != null ? val.toString() : null;
    }

    @Override
    public String getProperty(final String key, final String defaultValue) {
        Object val = properties.getOrDefault(key, defaultValue);
        return val != null ? val.toString() : null;
    }

    @Override
    public String setProperty(final String key, final String value) {
        dl1.setProperty(key, value);
        dl2.setProperty(key, value);
        return (String) properties.setProperty(key, value);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
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

    @Override
    public <E extends Extension<Line>, B extends ExtensionAdder<Line, E>> B newExtension(Class<B> type) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
