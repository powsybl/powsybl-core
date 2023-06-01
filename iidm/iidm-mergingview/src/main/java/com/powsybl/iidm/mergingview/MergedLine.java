/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.iidm.network.util.TieLineUtil;
import com.powsybl.iidm.network.util.Identifiables;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.util.TieLineUtil.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class MergedLine implements TieLine {

    private final MergingViewIndex index;

    private final BoundaryLineAdapter boundaryLine1;

    private final BoundaryLineAdapter boundaryLine2;

    private String id;

    private final String name;

    private final Properties properties = new Properties();

    MergedLine(final MergingViewIndex index, final BoundaryLine bl1, final BoundaryLine bl2, boolean ensureIdUnicity) {
        this.index = Objects.requireNonNull(index, "merging view index is null");
        this.boundaryLine1 = index.getBoundaryLine(bl1);
        // must be reoriented. TieLine is defined as networkNode1-boundaryNode--boundaryNode-networkNode2
        // and in boundaryLines the networkNode is always at end1
        this.boundaryLine2 = index.getBoundaryLine(bl2);
        this.id = ensureIdUnicity ? Identifiables.getUniqueId(buildMergedId(bl1.getId(), bl2.getId()), index::contains) : buildMergedId(bl1.getId(), bl2.getId());
        this.name = buildMergedName(bl1.getId(), bl2.getId(), bl1.getOptionalName().orElse(null), bl2.getOptionalName().orElse(null));
        mergeProperties(bl1, bl2, properties);
    }

    MergedLine(final MergingViewIndex index, final BoundaryLine bl1, final BoundaryLine bl2) {
        this(index, bl1, bl2, false);
    }

    @Override
    public MergingView getNetwork() {
        return index.getView();
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
        return TieLineUtil.getR(boundaryLine1, boundaryLine2);
    }

    @Override
    public double getX() {
        return TieLineUtil.getX(boundaryLine1, boundaryLine2);
    }

    @Override
    public double getG1() {
        return TieLineUtil.getG1(boundaryLine1, boundaryLine2);
    }

    @Override
    public double getG2() {
        return TieLineUtil.getG2(boundaryLine1, boundaryLine2);
    }

    @Override
    public double getB1() {
        return TieLineUtil.getB1(boundaryLine1, boundaryLine2);
    }

    @Override
    public double getB2() {
        return TieLineUtil.getB2(boundaryLine1, boundaryLine2);
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
        return getBoundaryLine1().isFictitious() || getBoundaryLine2().isFictitious();
    }

    @Override
    public void setFictitious(boolean fictitious) {
        getBoundaryLine1().setFictitious(fictitious);
        getBoundaryLine2().setFictitious(fictitious);
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
        getBoundaryLine1().setProperty(key, value);
        getBoundaryLine2().setProperty(key, value);
        return (String) properties.setProperty(key, value);
    }

    @Override
    public boolean removeProperty(String key) {
        boolean removed1 = getBoundaryLine1().removeProperty(key);
        boolean removed2 = getBoundaryLine2().removeProperty(key);
        properties.remove(key);
        return removed1 || removed2;
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<TieLine>> void addExtension(final Class<? super E> type, final E extension) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<TieLine>> E getExtension(final Class<? super E> type) {
        return null;
        // throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<TieLine>> E getExtensionByName(final String name) {
        // TODO(mathbagu): This method is used in the UCTE export so we prefer returning an empty list instead of throwing an exception
        // TODO(mathbagu): is it a good idea to extend AbstractExtendable?
        return null;
    }

    @Override
    public <E extends Extension<TieLine>> boolean removeExtension(final Class<E> type) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<TieLine>> Collection<E> getExtensions() {
        // TODO(mathbagu): This method is used in the UCTE export so we prefer returning an empty list instead of throwing an exception
        // TODO(mathbagu): is it a good idea to extend AbstractExtendable?
        return Collections.emptyList();
    }

    @Override
    public String getImplementationName() {
        return "MergingView";
    }

    @Override
    public <E extends Extension<TieLine>, B extends ExtensionAdder<TieLine, E>> B newExtension(Class<B> type) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public String getUcteXnodeCode() {
        return Optional.ofNullable(getBoundaryLine1().getUcteXnodeCode()).orElseGet(() -> getBoundaryLine2().getUcteXnodeCode());
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
                throw new IllegalStateException("Unknown branch side " + side);
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
    public Terminal getTerminal1() {
        return boundaryLine1.getTerminal();
    }

    @Override
    public Terminal getTerminal2() {
        return boundaryLine2.getTerminal();
    }

    @Override
    public Terminal getTerminal(Side side) {
        switch (side) {
            case ONE:
                return getTerminal1();
            case TWO:
                return getTerminal2();
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public Terminal getTerminal(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        boolean side1 = getTerminal1().getVoltageLevel().getId().equals(voltageLevelId);
        boolean side2 = getTerminal2().getVoltageLevel().getId().equals(voltageLevelId);
        if (side1 && side2) {
            throw new PowsyblException("Both terminals are connected to voltage level " + voltageLevelId);
        } else if (side1) {
            return getTerminal1();
        } else if (side2) {
            return getTerminal2();
        } else {
            throw new PowsyblException("No terminal connected to voltage level " + voltageLevelId);
        }
    }

    public Side getSide(Terminal terminal) {
        Objects.requireNonNull(terminal);

        if (boundaryLine1.getTerminal() == terminal) {
            return Side.ONE;
        } else if (boundaryLine2.getTerminal() == terminal) {
            return Side.TWO;
        } else {
            throw new IllegalStateException("The terminal is not connected to this branch");
        }
    }

    @Override
    public Collection<OperationalLimits> getOperationalLimits1() {
        return boundaryLine1.getOperationalLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits1() {
        return boundaryLine1.getCurrentLimits();
    }

    @Override
    public CurrentLimits getNullableCurrentLimits1() {
        return getCurrentLimits1().orElse(null);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return boundaryLine1.newCurrentLimits();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits1() {
        return boundaryLine1.getApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits1() {
        return getApparentPowerLimits1().orElse(null);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return boundaryLine1.newApparentPowerLimits();
    }

    @Override
    public Collection<OperationalLimits> getOperationalLimits2() {
        return boundaryLine2.getOperationalLimits();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits1() {
        return boundaryLine1.getActivePowerLimits();
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits1() {
        return boundaryLine1.getNullableActivePowerLimits();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return boundaryLine1.newActivePowerLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits2() {
        return boundaryLine2.getCurrentLimits();
    }

    @Override
    public CurrentLimits getNullableCurrentLimits2() {
        return getCurrentLimits2().orElse(null);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return boundaryLine2.newCurrentLimits();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return boundaryLine2.getApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits2() {
        return getApparentPowerLimits2().orElse(null);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return boundaryLine2.newApparentPowerLimits();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits2() {
        return boundaryLine2.getActivePowerLimits();
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits2() {
        return getActivePowerLimits2().orElse(null);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        return boundaryLine2.newActivePowerLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return getCurrentLimits1();
        } else if (side == Branch.Side.TWO) {
            return getCurrentLimits2();
        }
        throw new IllegalStateException("Unexpected side: " + side);
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return getActivePowerLimits1();
        } else if (side == Branch.Side.TWO) {
            return getActivePowerLimits2();
        }
        throw new IllegalStateException("Unexpected side: " + side);
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return getApparentPowerLimits1();
        } else if (side == Branch.Side.TWO) {
            return getApparentPowerLimits2();
        }
        throw new IllegalStateException("Unexpected side: " + side);
    }

    @Override
    public boolean isOverloaded() {
        return isOverloaded(1.0f);
    }

    @Override
    public boolean isOverloaded(float limitReduction) {
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
    public boolean checkPermanentLimit(Side side, float limitReduction, LimitType type) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkPermanentLimit1(limitReduction, type);
            case TWO:
                return checkPermanentLimit2(limitReduction, type);
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public boolean checkPermanentLimit(Side side, LimitType type) {
        return checkPermanentLimit(side, 1f, type);
    }

    @Override
    public boolean checkPermanentLimit1(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.ONE, limitReduction, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public boolean checkPermanentLimit1(LimitType type) {
        return checkPermanentLimit1(1f, type);
    }

    @Override
    public boolean checkPermanentLimit2(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.TWO, limitReduction, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public boolean checkPermanentLimit2(LimitType type) {
        return checkPermanentLimit2(1f, type);
    }

    @Override
    public Branch.Overload checkTemporaryLimits(Side side, float limitReduction, LimitType type) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkTemporaryLimits1(limitReduction, type);
            case TWO:
                return checkTemporaryLimits2(limitReduction, type);
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public Branch.Overload checkTemporaryLimits(Side side, LimitType type) {
        return checkTemporaryLimits(side, 1f, type);
    }

    @Override
    public Branch.Overload checkTemporaryLimits1(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.ONE, limitReduction, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public Branch.Overload checkTemporaryLimits1(LimitType type) {
        return checkTemporaryLimits1(1f, type);
    }

    @Override
    public Branch.Overload checkTemporaryLimits2(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.TWO, limitReduction, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public Branch.Overload checkTemporaryLimits2(LimitType type) {
        return checkTemporaryLimits2(1f, type);
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
}
