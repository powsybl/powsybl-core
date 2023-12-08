/**
 * Copyright (c) 2023, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.network;

/**
 * Interface to build a single step.
 * @param <S> for SELF (itself)
 * @param <T> the return type when building the step by calling {@link TapChangerStepAdder#endStep()}
 * @author Florent MILLOT {@literal <florent.millot at rte-france.com>}
 */
public interface RatioTapChangerStepAdder<S extends RatioTapChangerStepAdder<S, T>, T> extends TapChangerStepAdder<S, T> {
}
