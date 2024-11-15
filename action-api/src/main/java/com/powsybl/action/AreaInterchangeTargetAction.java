/*
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.modification.AreaInterchangeTargetModification;
import com.powsybl.iidm.modification.NetworkModification;

import java.util.Objects;

/**
 *  An action to:
 *  <ul>
 *      <li>Change the interchange target of an Area by specifying a new interchange target in MW.</li>
 *  </ul>
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class AreaInterchangeTargetAction extends AbstractAction {

    public static final String NAME = "AREA_INTERCHANGE_TARGET_ACTION";

    private final String areaId;
    private final double interchangeTarget;

    public AreaInterchangeTargetAction(String id, String areaId, double interchangeTarget) {
        super(id);
        this.areaId = Objects.requireNonNull(areaId);
        this.interchangeTarget = interchangeTarget;
    }

    public double getInterchangeTarget() {
        return interchangeTarget;
    }

    public String getAreaId() {
        return areaId;
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AreaInterchangeTargetAction that = (AreaInterchangeTargetAction) o;
        return Objects.equals(areaId, that.areaId) && (interchangeTarget == that.interchangeTarget || (Double.isNaN(interchangeTarget) && Double.isNaN(that.interchangeTarget)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), interchangeTarget, areaId);
    }

    @Override
    public NetworkModification toModification() {
        return new AreaInterchangeTargetModification(areaId, interchangeTarget);
    }
}
