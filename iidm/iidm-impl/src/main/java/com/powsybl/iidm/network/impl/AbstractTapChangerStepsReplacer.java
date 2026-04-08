/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florent MILLOT {@literal <florent.millot at rte-france.com>}
 */
abstract class AbstractTapChangerStepsReplacer<S extends AbstractTapChangerStepsReplacer<S, C>, C extends TapChangerStepImpl<C>> {

    private final AbstractTapChanger<?, ?, C> stepHolder;

    protected List<C> steps = new ArrayList<>();

    protected AbstractTapChangerStepsReplacer(AbstractTapChanger<?, ?, C> stepHolder) {
        this.stepHolder = stepHolder;
    }

    public void replaceSteps() {
        stepHolder.setSteps(steps);
    }
}
