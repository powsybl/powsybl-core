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

    private static final String INCONSISTENCY_PROPERTY = "Inconsistencies of property '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Removing the property of merged line";

    MergedLine(final MergingViewIndex index, final DanglingLine dl1, final DanglingLine dl2, boolean ensureIdUnicity) {
        this.index = Objects.requireNonNull(index, "merging view index is null");
        this.half1 = new HalfLineAdapter(dl1);
        this.half2 = new HalfLineAdapter(dl2);
        this.id = ensureIdUnicity ? Identifiables.getUniqueId(buildId(dl1, dl2), index::contains) : buildId(dl1, dl2);
        this.name = buildName(dl1, dl2);
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
        return dl1.getOptionalName()
                .map(name1 -> dl2.getOptionalName()
                        .map(name2 -> buildName(name1, name2))
                        .orElse(name1))
                .orElseGet(() -> dl2.getOptionalName().orElse(null));
    }

    private static String buildName(String name1, String name2) {
        int compareResult = name1.compareTo(name2);
        if (compareResult == 0) {
            return name1;
        } else if (compareResult < 0) {
            return name1 + " + " + name2;
        } else {
            return name2 + " + " + name1;
        }
    }

    void computeAndSetP0() {
        // TODO(mathbagu): depending on the b/g in the middle of the MergedLine, this computation is not correct
        double p1 = getTerminal1().getP();
        double p2 = getTerminal2().getP();
        if (!Double.isNaN(p1) && !Double.isNaN(p2)) {
            double losses = p1 + p2;
            half1.setXnodeP((p1 + losses / 2.0) * sign(p2));
            half2.setXnodeP((p2 + losses / 2.0) * sign(p1));
        }
    }

    void computeAndSetQ0() {
        // TODO(mathbagu): depending on the b/g in the middle of the MergedLine, this computation is not correct
        double q1 = getTerminal1().getQ();
        double q2 = getTerminal2().getQ();
        if (!Double.isNaN(q1) && !Double.isNaN(q2)) {
            double losses = q1 + q2;
            half1.setXnodeQ((q1 + losses / 2.0) * sign(q2));
            half2.setXnodeQ((q2 + losses / 2.0) * sign(q1));
        }
    }

    private static int sign(double value) {
        // Sign depends on the transit flow:
        // P1 ---->-----DL1.P0 ---->----- DL2.P0 ---->---- P2
        // The sign of DL1.P0 is the same as P2, and respectively the sign of DL2.P0 is the same than P1
        return (value >= 0) ? 1 : -1;
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
    public CurrentLimits getCurrentLimits(final Side side) {
        switch (side) {
            case ONE:
                return getCurrentLimits1();
            case TWO:
                return getCurrentLimits2();
            default:
                throw new AssertionError(UNEXPECTED_SIDE_VALUE + side);
        }
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
    public CurrentLimits getCurrentLimits2() {
        return getDanglingLine2().getCurrentLimits();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return getDanglingLine2().newCurrentLimits();
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

    private <P> boolean isMergedProperty(P prop1, P prop2) {
        if ((prop1 != null && prop2 == null) || (prop1 != null && prop2 instanceof String && ((String) prop2).isEmpty())) {
            return true;
        }
        if ((prop1 == null && prop2 != null) || (prop2 != null && prop1 instanceof String && ((String) prop1).isEmpty())) {
            return true;
        }
        if (prop1 == null) {
            return false;
        }
        return prop1.equals(prop2);
    }

    @Override
    public boolean hasProperty() {
        boolean prop1 = getDanglingLine1().hasProperty();
        boolean prop2 = getDanglingLine2().hasProperty();
        if (prop1 && !prop2) {
            return true;
        }
        if (!prop1 && prop2) {
            return true;
        }
        if (prop1) {
            for (String key : getPropertyNames()) {
                if (hasProperty(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasProperty(String key) {
        return getPropertyNames().contains(key);
    }

    @Override
    public Set<String> getPropertyNames() {
        Set<String> dl1PropertyNames = getDanglingLine1().getPropertyNames();
        Set<String> dl2PropertyNames = getDanglingLine2().getPropertyNames();

        Set<String> commonProperties = Sets.intersection(dl1PropertyNames, dl2PropertyNames);
        Set<String> properties = new HashSet<>(Sets.difference(dl1PropertyNames, commonProperties));
        properties.addAll(Sets.difference(dl2PropertyNames, commonProperties));
        for (String key : commonProperties) {
            PropertyType type = getDanglingLine1().getPropertyType(key);
            if (type == getDanglingLine2().getPropertyType(key)) {
                switch (type) {
                    case STRING:
                        if (isMergedProperty(getDanglingLine1().getStringProperty(key), getDanglingLine2().getStringProperty(key))) {
                            properties.add(key);
                        }
                        break;
                    case INTEGER:
                        if (isMergedProperty(getDanglingLine1().getIntegerProperty(key), getDanglingLine2().getIntegerProperty(key))) {
                            properties.add(key);
                        }
                        break;
                    case DOUBLE:
                        if (isMergedProperty(getDanglingLine1().getDoubleProperty(key), getDanglingLine2().getDoubleProperty(key))) {
                            properties.add(key);
                        }
                        break;
                    case BOOLEAN:
                        if (isMergedProperty(getDanglingLine1().getBooleanProperty(key), getDanglingLine2().getBooleanProperty(key))) {
                            properties.add(key);
                        }
                        break;
                }
            }
        }
        return properties;
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
    public PropertyType getPropertyType(String key) {
        PropertyType type1 = getDanglingLine1().getPropertyType(key);
        PropertyType type2 = getDanglingLine2().getPropertyType(key);
        if (type1 != null && type2 == null) {
            return type1;
        }
        if (type2 != null && type1 == null) {
            return type2;
        }
        if (type1 != null && type1.equals(type2)) {
            boolean isMergedProperty = false;
            switch (type1) {
                case STRING:
                    isMergedProperty = getDanglingLine1().getStringProperty(key).equals(getDanglingLine2().getStringProperty(key));
                    break;
                case INTEGER:
                    isMergedProperty = getDanglingLine1().getIntegerProperty(key) == getDanglingLine2().getIntegerProperty(key);
                    break;
                case DOUBLE:
                    isMergedProperty = getDanglingLine1().getDoubleProperty(key) == getDanglingLine2().getDoubleProperty(key);
                    break;
                case BOOLEAN:
                    isMergedProperty = getDanglingLine1().getBooleanProperty(key) == getDanglingLine2().getBooleanProperty(key);
                    break;
            }
            if (isMergedProperty) {
                return type1;
            }
        }
        return null;
    }

    @Override
    public String getStringProperty(String key) {
        String prop1 = getDanglingLine1().getStringProperty(key);
        String prop2 = getDanglingLine2().getStringProperty(key);
        if ((prop1 != null && prop2 == null) || (prop1 != null && prop2.isEmpty())) {
            return prop1;
        }
        if ((prop1 == null && prop2 != null) || (prop2 != null && prop1.isEmpty())) {
            return prop2;
        }
        if (prop1 != null) {
            if (prop1.equals(prop2)) {
                return prop1;
            }
            LOGGER.error(INCONSISTENCY_PROPERTY, key, prop1, prop2);
        }
        return null;
    }

    @Override
    public String getStringProperty(String key, String defaultValue) {
        String value = getStringProperty(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public Optional<String> getOptionalStringProperty(String key) {
        return Optional.ofNullable(getStringProperty(key));
    }

    @Override
    public String setStringProperty(String key, String value) {
        getDanglingLine1().setStringProperty(key, value);
        return getDanglingLine2().setStringProperty(key, value);
    }

    @Override
    public int getIntegerProperty(String key) {
        int prop1 = getDanglingLine1().getIntegerProperty(key);
        int prop2 = getDanglingLine2().getIntegerProperty(key);
        if (getDanglingLine1().hasProperty(key) && !getDanglingLine2().hasProperty(key)) {
            return prop1;
        }
        if (getDanglingLine2().hasProperty(key) && !getDanglingLine1().hasProperty(key)) {
            return prop2;
        }
        if (getDanglingLine1().hasProperty(key)) {
            if (prop1 == prop2) {
                return prop1;
            }
            LOGGER.error(INCONSISTENCY_PROPERTY, key, prop1, prop2);
        }
        return 0;
    }

    @Override
    public int getIntegerProperty(String key, int defaultValue) {
        int value = getIntegerProperty(key);
        return value == 0 ? defaultValue : value;
    }

    @Override
    public OptionalInt getOptionalIntegerProperty(String key) {
        return OptionalInt.of(getIntegerProperty(key));
    }

    @Override
    public int setIntegerProperty(String key, int value) {
        getDanglingLine1().setIntegerProperty(key, value);
        return getDanglingLine2().setIntegerProperty(key, value);
    }

    @Override
    public double getDoubleProperty(String key) {
        double prop1 = getDanglingLine1().getDoubleProperty(key);
        double prop2 = getDanglingLine2().getDoubleProperty(key);
        if (!Double.isNaN(prop1) && Double.isNaN(prop2)) {
            return prop1;
        }
        if (!Double.isNaN(prop2) && Double.isNaN(prop1)) {
            return prop2;
        }
        if (!Double.isNaN(prop1)) {
            if (prop1 == prop2) {
                return prop1;
            }
            LOGGER.error(INCONSISTENCY_PROPERTY, key, prop1, prop2);
        }
        return Double.NaN;
    }

    @Override
    public double getDoubleProperty(String key, double defaultValue) {
        double value = getDoubleProperty(key);
        return Double.isNaN(value) ? defaultValue : value;
    }

    @Override
    public OptionalDouble getOptionalDoubleProperty(String key) {
        return OptionalDouble.of(getDoubleProperty(key));
    }

    @Override
    public double setDoubleProperty(String key, double value) {
        getDanglingLine1().setDoubleProperty(key, value);
        return getDanglingLine2().setDoubleProperty(key, value);
    }

    @Override
    public boolean getBooleanProperty(String key) {
        boolean prop1 = getDanglingLine1().getBooleanProperty(key);
        boolean prop2 = getDanglingLine2().getBooleanProperty(key);
        if (getDanglingLine1().hasProperty(key) && !getDanglingLine2().hasProperty(key)) {
            return prop1;
        }
        if (getDanglingLine2().hasProperty(key) && !getDanglingLine1().hasProperty(key)) {
            return prop2;
        }
        if (getDanglingLine1().hasProperty(key)) {
            if (prop1 == prop2) {
                return prop1;
            }
            LOGGER.error(INCONSISTENCY_PROPERTY, key, prop1, prop2);
        }
        return false;
    }

    @Override
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        boolean value = getBooleanProperty(key);
        return value ? value : defaultValue;
    }

    @Override
    public Optional<Boolean> getOptionalBooleanProperty(String key) {
        return Optional.of(getBooleanProperty(key));
    }

    @Override
    public boolean setBooleanProperty(String key, boolean value) {
        return getDanglingLine1().setBooleanProperty(key, value) && getDanglingLine2().setBooleanProperty(key, value);
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
