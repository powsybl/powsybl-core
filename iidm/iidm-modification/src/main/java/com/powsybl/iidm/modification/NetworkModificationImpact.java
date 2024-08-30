/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public enum NetworkModificationImpact {
    CANNOT_BE_APPLIED,
    NO_IMPACT_ON_NETWORK,
    HAS_IMPACT_ON_NETWORK
}
