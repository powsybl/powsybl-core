/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BoundaryFlowLimits;
import com.powsybl.iidm.network.impl.ActivePowerLimitsAdderImpl;
import com.powsybl.iidm.network.impl.ApparentPowerLimitsAdderImpl;
import com.powsybl.iidm.network.impl.CurrentLimitsAdderImpl;
import com.powsybl.iidm.network.impl.OperationalLimitsOwner;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class BoundaryFlowLimitsImpl extends AbstractExtension<DanglingLine> implements BoundaryFlowLimits, OperationalLimitsOwner {

    private final EnumMap<LimitType, OperationalLimits> operationalLimits = new EnumMap<>(LimitType.class);

    BoundaryFlowLimitsImpl(DanglingLine dl) {
        super(dl);
    }

    @Override
    public boolean isEmpty() {
        return operationalLimits.isEmpty();
    }

    @Override
    public Collection<OperationalLimits> getOperationalLimits() {
        return Collections.unmodifiableCollection(operationalLimits.values());
    }

    @Override
    public CurrentLimits getCurrentLimits() {
        OperationalLimits ol = operationalLimits.get(LimitType.CURRENT);
        if (ol == null || ol instanceof CurrentLimits) {
            return (CurrentLimits) ol;
        }
        throw new AssertionError(assertionErrorMessage(LimitType.CURRENT, ol.getClass(), "CurrentLimits"));
    }

    @Override
    public ActivePowerLimits getActivePowerLimits() {
        OperationalLimits ol = operationalLimits.get(LimitType.ACTIVE_POWER);
        if (ol == null || ol instanceof ActivePowerLimits) {
            return (ActivePowerLimits) ol;
        }
        throw new AssertionError(assertionErrorMessage(LimitType.ACTIVE_POWER, ol.getClass(), "ActivePowerLimits"));
    }

    @Override
    public ApparentPowerLimits getApparentPowerLimits() {
        OperationalLimits ol = operationalLimits.get(LimitType.APPARENT_POWER);
        if (ol == null || ol instanceof ApparentPowerLimits) {
            return (ApparentPowerLimits) ol;
        }
        throw new AssertionError(assertionErrorMessage(LimitType.APPARENT_POWER, ol.getClass(), "ApparentPowerLimits"));
    }

    private static String assertionErrorMessage(LimitType type, Class<?> clazz, String expectedClass) {
        return String.format("Unexpected class for operational limits of type %s. Expected: %s, actual: %s", type, clazz.getName(), expectedClass);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAdderImpl(this);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        return new ApparentPowerLimitsAdderImpl(this);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        return new ActivePowerLimitsAdderImpl(this);
    }

    @Override
    public void setOperationalLimits(LimitType limitType, OperationalLimits operationalLimits) {
        this.operationalLimits.put(limitType, operationalLimits);
    }

    @Override
    public void notifyUpdate(LimitType limitType, String attribute, double oldValue, double newValue) {
        // does nothing
    }

    @Override
    public String getMessageHeader() {
        return "Boundary flow limits of '" + getExtendable().getId() + "':";
    }
}
