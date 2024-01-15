/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network;

/**
 * @author Florent MILLOT {@literal <florent.millot at rte-france.com>}
 */
public interface TapChangerStepAdder<S extends TapChangerStepAdder<S, A>, A extends TapChangerAdder<A, S, ?>> {
    S setRho(double rho);

    S setR(double r);

    S setX(double x);

    S setG(double g);

    S setB(double b);

    A endStep();
}
