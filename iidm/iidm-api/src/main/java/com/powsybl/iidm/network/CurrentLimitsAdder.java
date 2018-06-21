/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface CurrentLimitsAdder {

    interface TemporaryLimitAdder {

        TemporaryLimitAdder setName(String name);

        TemporaryLimitAdder setValue(double value);

        TemporaryLimitAdder setAcceptableDuration(int duration);

        TemporaryLimitAdder setFictitious(boolean fictitious);

        CurrentLimitsAdder endTemporaryLimit();
    }

    CurrentLimitsAdder setPermanentLimit(double limit);

    TemporaryLimitAdder beginTemporaryLimit();

    CurrentLimits add();

}
