/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.action.PctLoadAction.QModificationStrategy;

import java.util.Objects;

/**
 * @author Benoît Chiquet {@literal <benoit.chiquet@rte-france.com>}
 */
public class PctLoadActionBuilder implements ActionBuilder<PctLoadActionBuilder> {
    private String id;
    private String loadId;
    private double pctPChange;
    private QModificationStrategy strategy;

    @Override
    public String getType() {
        return PctLoadAction.NAME;
    }

    @Override
    public PctLoadActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public PctLoadActionBuilder withNetworkElementId(String elementId) {
        this.loadId = elementId;
        return this;
    }

    public PctLoadActionBuilder withLoadId(String loadId) {
        return this.withNetworkElementId(loadId);
    }

    @Override
    public Action build() {
        if (pctPChange < -100) {
            throw new IllegalArgumentException("The load can't be reduced by more than 100%.");
        }
        return new PctLoadAction(Objects.requireNonNull(id), Objects.requireNonNull(loadId), pctPChange, Objects.requireNonNull(strategy));
    }

    public PctLoadActionBuilder withPctPChange(double pctPChange) {
        this.pctPChange = pctPChange;
        return this;
    }

    public PctLoadActionBuilder withQStrategy(QModificationStrategy strategy) {
        this.strategy = strategy;
        return this;
    }
}
