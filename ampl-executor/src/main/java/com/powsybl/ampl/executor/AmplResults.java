/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

/**
 * Basic class for Ampl solve information.
 *
 * @author Nicolas Pierre <nicolas.pierre@artelys.com>
 */
public class AmplResults {

    public static AmplResults ok() {
        return new AmplResults(true);
    }

    private final boolean success;

    public AmplResults(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

}
