/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public enum BalanceType {
    PROPORTIONAL_TO_GENERATION_P,
    PROPORTIONAL_TO_GENERATION_P_MAX,
    PROPORTIONAL_TO_GENERATION_HEADROOM,
    NONE
}
