/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface LineCharacteristicsSetters<T> {

    /**
     * Set the series resistance in &#937;.
     */
    T setR(double r);

    /**
     * Set the series reactance in &#937;.
     */
    T setX(double x);

    /**
     * Set the first side shunt conductance in S.
     */
    T setG1(double g1);

    /**
     * Set the second side shunt conductance in S.
     */
    T setG2(double g2);

    /**
     * Set the first side shunt susceptance in S.
     */
    T setB1(double b1);

    /**
     * Set the second side shunt susceptance in S.
     */
    T setB2(double b2);
}
