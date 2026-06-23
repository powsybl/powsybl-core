/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.generation.dispatch;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class GenerationDispatchBuilder {

    private double lossCoefficient;

    public GenerationDispatch build() {
        return new GenerationDispatch(lossCoefficient);
    }

    /**
     * @param lossCoefficient the loss coefficient
     */
    public GenerationDispatchBuilder withLossCoefficient(double lossCoefficient) {
        this.lossCoefficient = lossCoefficient;
        return this;
    }
}
