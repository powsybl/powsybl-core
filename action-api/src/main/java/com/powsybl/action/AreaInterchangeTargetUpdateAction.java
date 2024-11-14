/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import java.util.Objects;

/**
 *  An action to:
 *  <ul>
 *      <li>Change the interchange target of an Area by specifying a new interchange target in MW.</li>
 *  </ul>
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class AreaInterchangeTargetUpdateAction extends AbstractAction {

    public static final String NAME = "AREA_INTERCHANGE_TARGET_UPDATE_ACTION";

    private final String areaId;
    private final double interchangeTarget;

    public AreaInterchangeTargetUpdateAction(String id, String areaId, double interchangeTarget) {
        super(id);
        this.areaId = Objects.requireNonNull(areaId);
        this.interchangeTarget = interchangeTarget ;
    }

    public double getInterchangeTarget () {
        return interchangeTarget ;
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
        AreaInterchangeTargetUpdateAction that = (AreaInterchangeTargetUpdateAction) o;
        return interchangeTarget  == that.interchangeTarget  && Objects.equals(areaId, that.areaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), interchangeTarget, areaId);
    }
}
