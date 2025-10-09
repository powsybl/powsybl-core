/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public interface Observability<T> {

    /**
     * boolean that says if the equipment is observable or not.
     * The definition of observable regroups active power, reactive power, voltage and angle (one for all).
     */
    boolean isObservable();

    Observability<T> setObservable(boolean observable);
}
