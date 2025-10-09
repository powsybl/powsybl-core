/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.executor;

import java.util.Map;
import java.util.Objects;

/**
 * Basic class for Ampl solve information.
 *
 * @author Nicolas Pierre {@literal <nicolas.pierre@artelys.com>}
 */
public class AmplResults {

    private final boolean success;
    private final Map<String, String> indicators;

    public AmplResults(boolean success, Map<String, String> indicators) {
        Objects.requireNonNull(indicators);
        this.success = success;
        this.indicators = indicators;
    }

    public boolean isSuccess() {
        return success;
    }

    public Map<String, String> getIndicators() {
        return indicators;
    }
}
