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
public interface TapChangerAdder<S extends TapChangerAdder<S, A, C>, A extends TapChangerStepAdder<A, S>, C extends TapChanger<C, ?, ?, ?>> {

    S setLowTapPosition(int lowTapPosition);

    S setTapPosition(int tapPosition);

    S setRegulating(boolean regulating);

    S setRegulationTerminal(Terminal regulationTerminal);

    default S setTargetDeadband(double targetDeadband) {
        throw new UnsupportedOperationException();
    }

    A beginStep();

    C add();

}
