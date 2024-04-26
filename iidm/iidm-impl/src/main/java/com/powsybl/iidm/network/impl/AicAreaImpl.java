/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.AicArea;
import com.powsybl.iidm.network.AreaType;
import com.powsybl.iidm.network.impl.util.Ref;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AicAreaImpl extends AreaImpl implements AicArea {
    private final double acNetInterchangeTarget;
    private final double acNetInterchangeTolerance;

    public AicAreaImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious, AreaType areaType, double acNetInterchangeTarget,
                    double acNetInterchangeTolerance) {
        super(ref, id, name, fictitious, areaType);
        this.acNetInterchangeTarget = acNetInterchangeTarget;
        this.acNetInterchangeTolerance = acNetInterchangeTolerance;
    }

    @Override
    public double getAcNetInterchangeTarget() {
        return acNetInterchangeTarget;
    }

    @Override
    public double getAcNetInterchangeTolerance() {
        return acNetInterchangeTolerance;
    }
}

