/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface LineCharacteristics {

    /**
     * Get the series resistance in &#937;.
     */
    double getR();

    /**
     * Get the series reactance in &#937;.
     */
    double getX();

    /**
     * Get the first side shunt conductance in S.
     */
    double getG1();

    /**
     * Get the second side shunt conductance in S.
     */
    double getG2();

    /**
     * Get the first side shunt susceptance in S.
     */
    double getB1();

    /**
     * Get the second side shunt susceptance in S.
     */
    double getB2();
}
