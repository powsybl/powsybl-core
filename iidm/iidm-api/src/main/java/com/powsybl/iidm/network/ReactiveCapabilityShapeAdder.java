/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public interface ReactiveCapabilityShapeAdder {

    /**
     * Add the reactive capability shape to the element
     * @return the ReactiveCapabilityShape
     */
    ReactiveCapabilityShape add();

    /**
     * Add a reactive capability shape plane to the reactive capability shape
     * @param alpha the alpha coefficient
     * @param beta the beta coefficient
     * @param gamma the gamme right hand side
     * @param isGreaterOrEqual true if the inequality is greater or equal, false if the inequality is less or equal
     * @return this
     */
    ReactiveCapabilityShapeAdder addPlane(double alpha, double beta, double gamma, boolean isGreaterOrEqual);

}
