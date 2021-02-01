/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.List;

/**
 * Sensitivity factors provider
 *
 * <p>
 *     Used to generate a list of sensitivity factors to be used has inputs
 *     for sensitivity analysis.
 * </p>
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 * @see SensitivityFactor
 */
public interface SensitivityFactorsProvider {

    /**
     * Get the list of factors generated based on the given network
     *
     * @param network Base IIDM network of provision method
     * @return A list of sensitivity factors
     */
    List<SensitivityFactor> getFactors(Network network);

    /**
     * Get the list of factors generated based on the given network and a contingency
     *
     * @param network Base IIDM network of provision method
     * @param contingencyId Id of the contingency for which we want the factors. Set to null for base case factors.
     * @return A list of sensitivity factors
     */
    default List<SensitivityFactor> getFactors(Network network, String contingencyId) {
        return Collections.emptyList();
    }
}
