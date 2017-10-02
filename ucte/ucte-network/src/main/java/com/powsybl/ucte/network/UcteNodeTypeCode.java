/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

/**
 * Node type code.
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum UcteNodeTypeCode {

    /**
     * 0 = P and Q constant (PQ node).
     */
    PQ,

    /**
     * 1 = Q and θ constant.
     */
    QT,

    /**
     * 2 = P and U constant (PU node).
     */
    PU,

    /**
     * 3 = U and θ constant (global slack node, only one in the whole network).
     */
    UT
}
