/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class AreaInterchangeTargetUpdateActionBuilder implements ActionBuilder<AreaInterchangeTargetUpdateActionBuilder> {

    private String id;

    private String areaId;

    private Double target = Double.NaN;

    @Override
    public String getType() {
        return AreaInterchangeTargetUpdateAction.NAME;
    }

    @Override
    public AreaInterchangeTargetUpdateActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public AreaInterchangeTargetUpdateActionBuilder withNetworkElementId(String elementId) {
        this.areaId = elementId;
        return this;
    }

    public AreaInterchangeTargetUpdateActionBuilder withTarget(double target) {
        this.target = target;
        return this;
    }

    public AreaInterchangeTargetUpdateActionBuilder withAreaId(String areaId) {
        this.withNetworkElementId(areaId);
        return this;
    }

    @Override
    public AreaInterchangeTargetUpdateAction build() {
        return new AreaInterchangeTargetUpdateAction(id, areaId, target);
    }
}
