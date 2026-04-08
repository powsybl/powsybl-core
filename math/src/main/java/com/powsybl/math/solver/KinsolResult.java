/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.solver;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class KinsolResult {

    private final KinsolStatus status;

    private final long iterations;

    public KinsolResult(int status, long iterations) {
        this.status = KinsolStatus.fromValue(status);
        this.iterations = iterations;
    }

    public KinsolStatus getStatus() {
        return status;
    }

    public long getIterations() {
        return iterations;
    }
}
