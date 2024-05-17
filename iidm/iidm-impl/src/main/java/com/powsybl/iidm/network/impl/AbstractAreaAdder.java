/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.AreaType;
import com.powsybl.iidm.network.impl.util.Ref;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
abstract class AbstractAreaAdder<T extends AbstractAreaAdder<T>> extends AbstractIdentifiableAdder<T> {

    private final Ref<NetworkImpl> networkRef;

    private AreaType areaType;

    private Double acNetInterchangeTarget;

    private Double acNetInterchangeTolerance;

    AbstractAreaAdder(Ref<NetworkImpl> networkRef) {
        this.networkRef = networkRef;
    }

    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    public T setAreaType(AreaType areaType) {
        this.areaType = areaType;
        return (T) this;
    }

    public T setAcNetInterchangeTarget(Double acNetInterchangeTarget) {
        this.acNetInterchangeTarget = acNetInterchangeTarget;
        return (T) this;
    }

    public T setAcNetInterchangeTolerance(Double acNetInterchangeTolerance) {
        this.acNetInterchangeTolerance = acNetInterchangeTolerance;
        return (T) this;
    }

    protected Ref<NetworkImpl> getNetworkRef() {
        return networkRef;
    }

    protected AreaType getAreaType() {
        return areaType;
    }

    protected Double getAcNetInterchangeTarget() {
        return acNetInterchangeTarget;
    }

    protected Double getAcNetInterchangeTolerance() {
        return acNetInterchangeTolerance;
    }
}
