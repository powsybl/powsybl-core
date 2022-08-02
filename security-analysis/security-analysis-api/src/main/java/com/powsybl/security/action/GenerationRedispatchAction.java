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
public class GenerationRedispatchAction extends AbstractAction {

    public static final String NAME = "GENERATION_REDISPATCH";

    private final String generatorId;
    private final boolean increasing;
    private final double value;

    public GenerationRedispatchAction(String id, String generatorId, boolean increasing, double value) {
        super(id);
        this.generatorId = Objects.requireNonNull(generatorId);
        this.increasing = increasing;
        this.value = value;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public String getGeneratorId() {
        return generatorId;
    }

    /**
     * If {@code true}, applying the action will increase the generator active power target by the value,
     * else it will decrease it.
     */
    public boolean isIncreasing() {
        return increasing;
    }

    public double getValue() {
        return value;
    }
}
