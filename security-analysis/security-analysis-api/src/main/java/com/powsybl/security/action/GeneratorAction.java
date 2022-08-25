/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

import java.util.Objects;

/**
 * An action increasing or decreasing the active power generation of a generator.
 *
 * @author Hadrien Godard <hadrien.godard@artelys.com>
 */
public class GeneratorAction extends AbstractAction {

    public static final String NAME = "GENERATOR";

    private final String generatorId;
    private final Boolean delta; // true if it is a relative variation, false if it is a new targetP
    private final double value;

    public GeneratorAction(String id, String generatorId, Boolean delta, double value) {
        super(id);
        this.generatorId = Objects.requireNonNull(generatorId);
        this.delta = delta;
        this.value = value;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public String getGeneratorId() {
        return generatorId;
    }

    public Boolean isRelativeVariation() {
        return delta;
    }

    public double getValue() {
        return value;
    }
}
