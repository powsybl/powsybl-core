/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network;

/**
 * Contains methods to build and replace the list of steps of an existing tap changer.
 * @param <S> for SELF (itself)
 * @param <A> the step adder to build and add one step
 * @author Florent MILLOT {@literal <florent.millot at rte-france.com>}
 */
public interface TapChangerStepsReplacer<S extends TapChangerStepsReplacer<S, A>, A extends TapChangerStepAdder<A, S>> {
    A beginStep();

    void replaceSteps();
}
