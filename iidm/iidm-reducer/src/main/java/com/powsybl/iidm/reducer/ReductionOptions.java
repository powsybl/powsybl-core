/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ReductionOptions {

    private boolean withBoundaryLines = false;

    public boolean isWithBoundaryLines() {
        return withBoundaryLines;
    }

    public ReductionOptions withDanglingLlines(boolean withBoundaryLines) {
        this.withBoundaryLines = withBoundaryLines;
        return this;
    }

}
