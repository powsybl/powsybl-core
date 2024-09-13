/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

/**
 * This enum represents the potential impact of a network modification on a network.
 * <p>
 *     The possible values for the enum are:
 *     <ul>
 *         <li>{@code CANNOT_BE_APPLIED}: the network modification cannot be applied on this network. If it applied,
 *         an exception will be thrown.</li>
 *         <li>{@code NO_IMPACT_ON_NETWORK}: the network modification can be applied to the network but, once the
 *         network modification is applied, the network will still be the same as before.</li>
 *         <li>{@code HAS_IMPACT_ON_NETWORK}: the network modification can be applied to the network and, once the
 *         network modification is applied, the network will be different in its topology or physical characteristics.</li>
 *     </ul>
 * </p>
 *
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public enum NetworkModificationImpact {
    CANNOT_BE_APPLIED,
    NO_IMPACT_ON_NETWORK,
    HAS_IMPACT_ON_NETWORK
}
