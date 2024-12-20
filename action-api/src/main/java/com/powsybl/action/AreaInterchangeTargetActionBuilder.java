/*
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class AreaInterchangeTargetActionBuilder implements ActionBuilder<AreaInterchangeTargetActionBuilder> {

    private String id;

    private String areaId;

    private double target = Double.NaN;

    @Override
    public String getType() {
        return AreaInterchangeTargetAction.NAME;
    }

    @Override
    public AreaInterchangeTargetActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public AreaInterchangeTargetActionBuilder withNetworkElementId(String elementId) {
        this.areaId = elementId;
        return this;
    }

    public AreaInterchangeTargetActionBuilder withTarget(double target) {
        this.target = target;
        return this;
    }

    public AreaInterchangeTargetActionBuilder withAreaId(String areaId) {
        this.withNetworkElementId(areaId);
        return this;
    }

    @Override
    public AreaInterchangeTargetAction build() {
        return new AreaInterchangeTargetAction(id, areaId, target);
    }
}
