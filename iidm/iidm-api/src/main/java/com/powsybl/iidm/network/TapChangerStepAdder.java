/**
 * Copyright (c) 2023, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.network;

/**
 * Set of methods to build a new step for a tap changer.
 * Can be used both when creating a new tap changer with {@link TapChangerAdder}
 * or when replacing steps of an existing tap changer with {@link TapChangerStepsReplacer}.
 * @param <S> for SELF (itself)
 * @param <T> the return type when building the step
 * @author Florent MILLOT {@literal <florent.millot at rte-france.com>}
 */
public interface TapChangerStepAdder<S extends TapChangerStepAdder<S, T>, T> {
    S setRho(double rho);

    S setR(double r);

    S setX(double x);

    S setG(double g);

    S setB(double b);

    T endStep();
}
