/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

/**
 * A class to represent the result if the analysis has failed.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class FailedFaultResult extends AbstractFaultResult {

    public FailedFaultResult(Fault fault, Status status) {
        super(fault, status, Double.NaN, null, null, null, null);
    }
}
