/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.CurrentLimitsAdder;

import java.util.function.Supplier;

/**
 * @deprecated Use {@link LoadingLimitsMapping} instead.
 *
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@Deprecated
public class CurrentLimitsMapping extends LoadingLimitsMapping {
    CurrentLimitsMapping(Context context) {
        super(context);
    }

    /**
     * @deprecated Use {@link LoadingLimitsMapping#getLoadingLimitsAdder(String, Supplier)} instead.
     */
    @Deprecated
    public CurrentLimitsAdder getCurrentLimitsAdder(String id, Supplier<CurrentLimitsAdder> supplier) {
        return (CurrentLimitsAdder) adders.computeIfAbsent(id, s -> supplier.get());
    }
}
