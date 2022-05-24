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
import com.powsybl.iidm.network.util.TieLineUtil;
import com.powsybl.iidm.network.util.Identifiables;
import com.powsybl.iidm.network.util.LimitViolationUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 * @author José Antonio Marqués <marquesja at aia.es>
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
        this.half1 = new HalfLineAdapter(dl1, Side.ONE, index);
        // must be reoriented. TieLine is defined as networkNode1-boundaryNode--boundaryNode-networkNode2
        // and in danglingLines the networkNode is always at end1
        this.half2 = new HalfLineAdapter(dl2, Side.TWO, index, true);
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
    public boolean isTieLine() {
        return true;
    }

    @Override
    public MergingView getNetwork() {
        return index.getView();
    }

    DanglingLine getDanglingLine1() {
        return half1.getDanglingLine();
    }

    DanglingLine getDanglingLine2() {
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
    public CurrentLimitsAdder newCurrentLimits1() {
        return getDanglingLine1().newCurrentLimits();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return getDanglingLine1().newActivePowerLimits();
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return getDanglingLine1().newApparentPowerLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits2() {
        return getDanglingLine2().getCurrentLimits();
    }

    @Override
    public CurrentLimits getNullableCurrentLimits2() {
        return getDanglingLine2().getNullableCurrentLimits();
    }

    @Override
    public CurrentLimitsSet getCurrentLimitsSet2() {
        return getDanglingLine2().getCurrentLimitsSet();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits2() {
        return getDanglingLine2().getActivePowerLimits();
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits2() {
        return getDanglingLine2().getNullableActivePowerLimits();
    }

    @Override
    public ActivePowerLimitsSet getActivePowerLimitsSet2() {
        return getDanglingLine2().getActivePowerLimitsSet();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return getDanglingLine2().getApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits2() {
        return getDanglingLine2().getNullableApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimitsSet getApparentPowerLimitsSet2() {
        return getDanglingLine2().getApparentPowerLimitsSet();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return getDanglingLine2().newCurrentLimits();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        return getDanglingLine2().newActivePowerLimits();
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
        return TieLineUtil.getR(half1, half2);
    }

    @Override
    public Line setR(final double r) {
        throw createNotSupportedForMergedLines();
    }

    @Override
    public double getX() {
        return TieLineUtil.getX(half1, half2);
    }

    @Override
    public Line setX(final double x) {
        throw createNotSupportedForMergedLines();
    }

    @Override
    public double getG1() {
        return TieLineUtil.getG1(half1, half2);
    }

    @Override
    public Line setG1(final double g1) {
        throw createNotSupportedForMergedLines();
    }

    @Override
    public double getG2() {
        return TieLineUtil.getG2(half1, half2);
    }

    @Override
    public Line setG2(final double g2) {
        throw createNotSupportedForMergedLines();
    }

    @Override
    public double getB1() {
        return TieLineUtil.getB1(half1, half2);
    }

    @Override
    public Line setB1(final double b1) {
        throw createNotSupportedForMergedLines();
    }

    @Override
    public double getB2() {
        return TieLineUtil.getB2(half1, half2);
    }

    @Override
    public Line setB2(final double b2) {
        throw createNotSupportedForMergedLines();
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
    public Optional<CurrentLimits> getCurrentLimits1() {
        return getDanglingLine1().getCurrentLimits();
    }

    @Override
    public CurrentLimits getNullableCurrentLimits1() {
        return getDanglingLine1().getNullableCurrentLimits();
    }

    @Override
    public CurrentLimitsSet getCurrentLimitsSet1() {
        return getDanglingLine1().getCurrentLimitsSet();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits1() {
        return getDanglingLine1().getActivePowerLimits();
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits1() {
        return getDanglingLine1().getNullableActivePowerLimits();
    }

    @Override
    public ActivePowerLimitsSet getActivePowerLimitsSet1() {
        return getDanglingLine1().getActivePowerLimitsSet();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits1() {
        return getDanglingLine1().getApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits1() {
        return getDanglingLine1().getNullableApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimitsSet getApparentPowerLimitsSet1() {
        return getDanglingLine1().getApparentPowerLimitsSet();
    }

    @Override
    public boolean isOverloaded() {
        return isOverloaded(1.0f);
    }

    @Override
    public boolean isOverloaded(final float limitReduction) {
        return checkPermanentLimit1(limitReduction, LimitType.CURRENT) || checkPermanentLimit2(limitReduction, LimitType.CURRENT);
    }

    @Override
    public int getOverloadDuration() {
        Branch.Overload o1 = checkTemporaryLimits1(LimitType.CURRENT);
        Branch.Overload o2 = checkTemporaryLimits2(LimitType.CURRENT);
        int duration1 = o1 != null ? o1.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        int duration2 = o2 != null ? o2.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        return Math.min(duration1, duration2);
    }

    @Override
    public boolean checkPermanentLimit(final Side side, final float limitReduction, LimitType type) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkPermanentLimit1(limitReduction, type);

            case TWO:
                return checkPermanentLimit2(limitReduction, type);

            default:
                throw new AssertionError(UNEXPECTED_SIDE_VALUE + side);
        }
    }

    @Override
    public boolean checkPermanentLimit(final Side side, LimitType type) {
        return checkPermanentLimit(side, 1f, type);
    }

    @Override
    public boolean checkPermanentLimit1(final float limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.ONE, limitReduction, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public boolean checkPermanentLimit1(LimitType type) {
        return checkPermanentLimit1(1f, type);
    }

    @Override
    public boolean checkPermanentLimit2(final float limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.TWO, limitReduction, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public boolean checkPermanentLimit2(LimitType type) {
        return checkPermanentLimit2(1f, type);
    }

    @Override
    public Overload checkTemporaryLimits(final Side side, final float limitReduction, LimitType type) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkTemporaryLimits1(limitReduction, type);

            case TWO:
                return checkTemporaryLimits2(limitReduction, type);

            default:
                throw new AssertionError(UNEXPECTED_SIDE_VALUE + side);
        }
    }

    @Override
    public Overload checkTemporaryLimits(final Side side, LimitType type) {
        return checkTemporaryLimits(side, 1f, type);
    }

    @Override
    public Overload checkTemporaryLimits1(final float limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.ONE, limitReduction, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits1(LimitType type) {
        return checkTemporaryLimits1(1f, type);
    }

    @Override
    public Overload checkTemporaryLimits2(final float limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.TWO, limitReduction, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits2(LimitType type) {
        return checkTemporaryLimits2(1f, type);
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

    @Override
    public boolean removeProperty(String key) {
        boolean removed1 = getDanglingLine1().removeProperty(key);
        boolean removed2 = getDanglingLine2().removeProperty(key);
        properties.remove(key);
        return removed1 || removed2;
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove(boolean removeDanglingSwitches) {
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

    public double getValueForLimit(Terminal t, LimitType type) {
        switch (type) {
            case ACTIVE_POWER:
                return t.getP();
            case APPARENT_POWER:
                return Math.sqrt(t.getP() * t.getP() + t.getQ() * t.getQ());
            case CURRENT:
                return t.getI();
            case VOLTAGE:
            default:
                throw new UnsupportedOperationException(String.format("Getting %s limits is not supported.", type.name()));
        }
    }

    private static ValidationException createNotSupportedForMergedLines() {
        throw new PowsyblException("direct modification of characteristics not supported for MergedLines");
    }
}
