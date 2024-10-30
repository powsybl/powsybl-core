/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.action.PercentChangeLoadAction.QModificationStrategy;

import java.util.Objects;

/**
 * @author Benoît Chiquet {@literal <benoit.chiquet@rte-france.com>}
 */
public class PercentChangeLoadActionBuilder implements ActionBuilder<PercentChangeLoadActionBuilder> {
    private String id;
    private String loadId;
    private double p0PercentChange;
    private QModificationStrategy qModificationStrategy;

    @Override
    public String getType() {
        return PercentChangeLoadAction.NAME;
    }

    @Override
    public PercentChangeLoadActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public PercentChangeLoadActionBuilder withNetworkElementId(String elementId) {
        this.loadId = elementId;
        return this;
    }

    public PercentChangeLoadActionBuilder withLoadId(String loadId) {
        return this.withNetworkElementId(loadId);
    }

    @Override
    public Action build() {
        if (p0PercentChange < -100) {
            throw new IllegalArgumentException("The load can't be reduced by more than 100%.");
        }
        return new PercentChangeLoadAction(Objects.requireNonNull(id), Objects.requireNonNull(loadId), p0PercentChange, Objects.requireNonNull(qModificationStrategy));
    }

    public PercentChangeLoadActionBuilder withPercentP0Change(double p0PercentChange) {
        this.p0PercentChange = p0PercentChange;
        return this;
    }

    public PercentChangeLoadActionBuilder withQModificationStrategy(QModificationStrategy strategy) {
        this.qModificationStrategy = strategy;
        return this;
    }
}
