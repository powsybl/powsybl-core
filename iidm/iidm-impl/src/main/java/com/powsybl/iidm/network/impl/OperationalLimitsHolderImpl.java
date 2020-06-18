/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class OperationalLimitsHolderImpl implements OperationalLimitsOwner {

    private final EnumMap<LimitType, OperationalLimits> operationalLimits = new EnumMap<>(LimitType.class);
    private final AbstractIdentifiable<?> identifiable;
    private final String attributeName;

    OperationalLimitsHolderImpl(AbstractIdentifiable<?> identifiable, String attributeName) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.attributeName = Objects.requireNonNull(attributeName);
    }

    @Override
    public void setOperationalLimits(LimitType limitType, OperationalLimits operationalLimits) {
        OperationalLimits oldValue = this.operationalLimits.put(limitType, operationalLimits);
        identifiable.getNetwork().getListeners().notifyUpdate(identifiable, attributeName + "_" + limitType, oldValue, operationalLimits);
    }

    List<OperationalLimits> getOperationalLimits() {
        return new ArrayList<>(operationalLimits.values());
    }

    <L extends OperationalLimits> L getOperationalLimits(LimitType type, Class<L> limitClazz) {
        if (type == null) {
            throw new IllegalArgumentException("limit type is null");
        }
        OperationalLimits ol = this.operationalLimits.get(type);
        if (ol == null || limitClazz.isInstance(ol)) {
            return (L) ol;
        }
        throw new AssertionError("Unexpected class for operational limits of type " + type + ". Expected: " + ol.getClass().getName() + ", actual: " + limitClazz.getName() + ".");
    }

    CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAdderImpl(this);
    }

    ApparentPowerLimitsAdder newApparentPowerLimits() {
        return new ApparentPowerLimitsAdderImpl(this);
    }

    ActivePowerLimitsAdder newActivePowerLimitsAdder() {
        return new ActivePowerLimitsAdderImpl(this);
    }

    VoltageLimitsAdder newVoltageLimits() {
        return new VoltageLimitsAdderImpl(this);
    }

    @Override
    public String getMessageHeader() {
        return identifiable.getMessageHeader();
    }
}
