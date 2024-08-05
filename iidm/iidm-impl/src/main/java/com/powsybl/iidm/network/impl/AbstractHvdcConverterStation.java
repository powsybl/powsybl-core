/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.commons.ref.Ref;

import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
abstract class AbstractHvdcConverterStation<T extends HvdcConverterStation<T>> extends AbstractConnectable<T> implements HvdcConverterStation<T> {

    private HvdcLine hvdcLine;

    private float lossFactor = Float.NaN;

    AbstractHvdcConverterStation(Ref<NetworkImpl> network, String id, String name, boolean fictitious, float lossFactor) {
        super(network, id, name, fictitious);
        this.hvdcLine = null;
        this.lossFactor = lossFactor;
    }

    @Override
    public HvdcLine getHvdcLine() {
        return hvdcLine;
    }

    T setHvdcLine(HvdcLine hvdcLine) {
        this.hvdcLine = hvdcLine;
        return (T) this;
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    public float getLossFactor() {
        return lossFactor;
    }

    @Override
    public T setLossFactor(float lossFactor) {
        ValidationUtil.checkLossFactor(this, lossFactor, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        float oldValue = this.lossFactor;
        this.lossFactor = lossFactor;
        notifyUpdate("lossFactor", oldValue, lossFactor);
        return (T) this;
    }

    @Override
    public Optional<? extends HvdcConverterStation<?>> getOtherConverterStation() {
        if (hvdcLine != null) {
            return hvdcLine.getConverterStation1() == this ? Optional.ofNullable(hvdcLine.getConverterStation2()) : Optional.ofNullable(hvdcLine.getConverterStation1());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void remove(boolean dryRun) {
        if (hvdcLine != null) {
            throw new ValidationException(this, "Impossible to remove this converter station (still attached to '" + hvdcLine.getId() + "')");
        }
        super.remove(dryRun);
    }

}
