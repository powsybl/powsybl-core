/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LoadingLimitsAdder<L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> extends OperationalLimitsAdder<L> {

    interface TemporaryLimitAdder<A> {

        TemporaryLimitAdder<A> setName(String name);

        TemporaryLimitAdder<A> setValue(double value);

        TemporaryLimitAdder<A> setAcceptableDuration(int duration);

        TemporaryLimitAdder<A> setFictitious(boolean fictitious);

        TemporaryLimitAdder<A> ensureNameUnicity();

        A endTemporaryLimit();
    }

    A setPermanentLimit(double limit);

    TemporaryLimitAdder<A> beginTemporaryLimit();

    double getPermanentLimit();

    double getTemporaryLimitValue(int acceptableDuration);

    boolean hasTemporaryLimits();
}
