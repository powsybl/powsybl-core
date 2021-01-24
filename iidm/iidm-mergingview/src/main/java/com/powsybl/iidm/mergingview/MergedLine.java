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

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class MergedLine implements TieLine {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergedLine.class);

    private static final String UNEXPECTED_SIDE_VALUE = "Unexpected side value: ";

    private final MergingViewIndex index;

    private final HalfLineAdapter half1;

    private final HalfLineAdapter half2;

    private String id;

    private final String name;

    private final Properties properties = new Properties();

    MergedLine(final MergingViewIndex index, final DanglingLine dl1, final DanglingLine dl2, boolean ensureIdUnicity) {
        this.index = Objects.requireNonNull(index, "merging view index is null");
        this.half1 = new HalfLineAdapter(dl1);
        this.half2 = new HalfLineAdapter(dl2);
        this.id = ensureIdUnicity ? Identifiables.getUniqueId(buildIdOrName(dl1.getId(), dl2.getId()), index::contains) : buildIdOrName(dl1.getId(), dl2.getId());
        this.name = buildName(dl1, dl2);
        mergeProperties(dl1, dl2);
    }

    MergedLine(final MergingViewIndex index, final DanglingLine dl1, final DanglingLine dl2) {
        this(index, dl1, dl2, false);
    }

    private static String buildName(final DanglingLine dl1, final DanglingLine dl2) {
        return dl1.getOptionalName()
                .map(name1 -> dl2.getOptionalName()
                        .map(name2 -> buildIdOrName(name1, name2))
                        .orElse(name1))
                .orElseGet(() -> dl2.getOptionalName().orElse(null));
    }

    private static String buildIdOrName(String idOrName1, String idOrName2) {
        int compareResult = idOrName1.compareTo(idOrName2);
        if (compareResult == 0) {
            return idOrName1;
        } else if (compareResult < 0) {
            return idOrName1 + " + " + idOrName2;
        } else {
            return idOrName2 + " + " + idOrName1;
        }
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
        return true;
    }

    @Override
    public MergingView getNetwork() {
        return index.getView();
    }

    private DanglingLine getDanglingLine1() {
        return half1.getDanglingLine();
    }

    private DanglingLine getDanglingLine2() {
        return half2.getDanglingLine();
    }

    @Override
    public Terminal getTerminal(final Side side) {
        switch (side) {
            case ONE:
                return getTerminal1();
            case TWO:
                return getTerminal2();
            default:
                throw new AssertionError(UNEXPECTED_SIDE_VALUE + side);
        }
    }

    @Override
    public Terminal getTerminal1() {
        return index.getTerminal(half1.getDanglingLine().getTerminal());
    }

    @Override
    public Terminal getTerminal2() {
        return index.getTerminal(half2.getDanglingLine().getTerminal());
    }

    @Override
    public Collection<OperationalLimits> getOperationalLimits1() {
        return getDanglingLine1().getOperationalLimits();
    }

    @Override
    public CurrentLimits getCurrentLimits1() {
        return getDanglingLine1().getCurrentLimits();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return getDanglingLine1().newCurrentLimits();
    }

    @Override
    public ActivePowerLimits getActivePowerLimits1() {
        return getDanglingLine1().getActivePowerLimits();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return getDanglingLine1().newActivePowerLimits();
    }

    @Override
    public ApparentPowerLimits getApparentPowerLimits1() {
        return getDanglingLine1().getApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return getDanglingLine1().newApparentPowerLimits();
    }

    @Override
    public Collection<OperationalLimits> getOperationalLimits2() {
        return getDanglingLine2().getOperationalLimits();
    }

    @Override
    public CurrentLimits getCurrentLimits2() {
        return getDanglingLine2().getCurrentLimits();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return getDanglingLine2().newCurrentLimits();
    }

    @Override
    public ActivePowerLimits getActivePowerLimits2() {
        return getDanglingLine2().getActivePowerLimits();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        return getDanglingLine2().newActivePowerLimits();
    }

    @Override
    public ApparentPowerLimits getApparentPowerLimits2() {
        return getDanglingLine2().getApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return getDanglingLine2().newApparentPowerLimits();
    }

    @Override
    public String getId() {
        return id;
    }

    MergedLine setId(String id) {
        Objects.requireNonNull(id, "id is null");
        this.id = Identifiables.getUniqueId(id, index::contains);
        return this;
    }

    @Override
    public double getR() {
        return half1.getR() + half2.getR();
    }

    @Override
    public Line setR(final double r) {
        half1.setR(r / 2);
        half2.setR(r / 2);
        return this;
    }

    @Override
    public double getX() {
        return half1.getX() + half2.getX();
    }

    @Override
    public Line setX(final double x) {
        half1.setX(x / 2);
        half2.setX(x / 2);
        return this;
    }

    @Override
    public double getG1() {
        return getDanglingLine1().getG();
    }

    @Override
    public Line setG1(final double g1) {
        half1.setG(g1);
        return this;
    }

    @Override
    public double getG2() {
        return getDanglingLine2().getG();
    }

    @Override
    public Line setG2(final double g2) {
        half2.setG(g2);
        return this;
    }

    @Override
    public double getB1() {
        return getDanglingLine1().getB();
    }

    @Override
    public Line setB1(final double b1) {
        half1.setB(b1);
        return this;
    }

    @Override
    public double getB2() {
        return getDanglingLine2().getB();
    }

    @Override
    public Line setB2(final double b2) {
        half2.setB(b2);
        return this;
    }

    @Override
    public Terminal getTerminal(final String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);

        Terminal terminal1 = getDanglingLine1().getTerminal();
        Terminal terminal2 = getDanglingLine2().getTerminal();
        if (voltageLevelId.equals(terminal1.getVoltageLevel().getId())) {
            return terminal1;
        } else if (voltageLevelId.equals(terminal2.getVoltageLevel().getId())) {
            return terminal2;
        } else {
            throw new PowsyblException("No terminal connected to voltage level " + voltageLevelId);
        }
    }

    Side getSide(final DanglingLine dl) {
        Objects.requireNonNull(dl);
        return getSide(dl.getTerminal());
    }

    @Override
    public Side getSide(final Terminal terminal) {
        Objects.requireNonNull(terminal);

        Terminal term = terminal;
        if (term instanceof AbstractAdapter) {
            term = ((AbstractAdapter<Terminal>) term).getDelegate();
        }
        if (term == getDanglingLine1().getTerminal()) {
            return Side.ONE;
        } else if (term == getDanglingLine2().getTerminal()) {
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
                throw new AssertionError(UNEXPECTED_SIDE_VALUE + side);
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
                throw new AssertionError(UNEXPECTED_SIDE_VALUE + side);
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
        return Arrays.asList(getTerminal1(), getTerminal2());
    }

    @Override
    public Optional<String> getOptionalName() {
        return Optional.ofNullable(name);
    }

    @Override
    public String getNameOrId() {
        return getOptionalName().orElse(id);
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
    public boolean isFictitious() {
        return getDanglingLine1().isFictitious() || getDanglingLine2().isFictitious();
    }

    @Override
    public void setFictitious(boolean fictitious) {
        getDanglingLine1().setFictitious(fictitious);
        getDanglingLine2().setFictitious(fictitious);
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
        getDanglingLine1().setProperty(key, value);
        getDanglingLine2().setProperty(key, value);
        return (String) properties.setProperty(key, value);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<Line>> void addExtension(final Class<? super E> type, final E extension) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<Line>> E getExtension(final Class<? super E> type) {
        return null;
        // throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<Line>> E getExtensionByName(final String name) {
        // TODO(mathbagu): This method is used in the UCTE export so we prefer returning an empty list instead of throwing an exception
        // TODO(mathbagu): is it a good idea to extend AbstractExtendable?
        return null;
    }

    @Override
    public <E extends Extension<Line>> boolean removeExtension(final Class<E> type) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<Line>> Collection<E> getExtensions() {
        // TODO(mathbagu): This method is used in the UCTE export so we prefer returning an empty list instead of throwing an exception
        // TODO(mathbagu): is it a good idea to extend AbstractExtendable?
        return Collections.emptyList();
    }

    @Override
    public String getImplementationName() {
        return "MergingView";
    }

    @Override
    public <E extends Extension<Line>, B extends ExtensionAdder<Line, E>> B newExtension(Class<B> type) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public String getUcteXnodeCode() {
        return getDanglingLine1().getUcteXnodeCode();
    }

    @Override
    public HalfLine getHalf1() {
        return half1;
    }

    @Override
    public HalfLine getHalf2() {
        return half2;
    }

    @Override
    public HalfLine getHalf(Side side) {
        switch (side) {
            case ONE:
                return half1;
            case TWO:
                return half2;
            default:
                throw new AssertionError("Unknown branch side " + side);
        }
    }
}
