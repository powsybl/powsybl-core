/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.AicArea;
import com.powsybl.iidm.network.AicAreaAdder;
import com.powsybl.iidm.network.impl.util.Ref;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AicAreaAdderImpl extends AbstractAreaAdder<AicAreaAdderImpl> implements AicAreaAdder {

    private double acNetInterchangeTarget;

    private double acNetInterchangeTolerance;

    AicAreaAdderImpl(Ref<NetworkImpl> networkRef) {
        super(networkRef);
    }

    @Override
    public AicAreaAdder setAcNetInterchangeTarget(double acNetInterchangeTarget) {
        this.acNetInterchangeTarget = acNetInterchangeTarget;
        return this;
    }

    @Override
    public AicAreaAdder setAcNetInterchangeTolerance(double acNetInterchangeTolerance) {
        this.acNetInterchangeTolerance = acNetInterchangeTolerance;
        return this;
    }

    @Override
    public AicArea add() {
        String id = checkAndGetUniqueId();
        AicAreaImpl aicArea = new AicAreaImpl(getNetworkRef(), id, getName(), isFictitious(), getAreaType(), acNetInterchangeTarget, acNetInterchangeTolerance);
        getNetwork().getIndex().checkAndAdd(aicArea);
        getNetwork().getListeners().notifyCreation(aicArea);
        return aicArea;
    }

    @Override
    protected String getTypeDescription() {
        return "AicArea";
    }
}
