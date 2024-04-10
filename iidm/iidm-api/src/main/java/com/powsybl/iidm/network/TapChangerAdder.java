/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network;

/**
 * Generic adder interface of the tap changer.
 * @param <S> for SELF (itself)
 * @param <D> the step
 * @param <A> the step adder of the tap changer adder <S>
 * @param <B> the step adder of the steps replacer <R>
 * @param <R> the steps replacer
 * @param <C> the tap changer
 * @author Florent MILLOT {@literal <florent.millot at rte-france.com>}
 */
public interface TapChangerAdder<
    S extends TapChangerAdder<S, D, A, B, R, C>,
    D extends TapChangerStep<D>,
    A extends TapChangerStepAdder<A, S>,
    B extends TapChangerStepAdder<B, R>,
    R extends TapChangerStepsReplacer<R, B>,
    C extends TapChanger<C, D, R, B>> {

    S setLowTapPosition(int lowTapPosition);

    S setTapPosition(int tapPosition);

    S setInitialTapPosition(int initialTapPosition);

    S setRegulating(boolean regulating);

    S setRegulationTerminal(Terminal regulationTerminal);

    default S setTargetDeadband(double targetDeadband) {
        throw new UnsupportedOperationException();
    }

    A beginStep();

    C add();

}
