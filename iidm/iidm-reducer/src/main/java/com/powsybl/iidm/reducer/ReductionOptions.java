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

    private boolean withDanglingLines = false;

    boolean isWithDanglingLines() {
        return withDanglingLines;
    }

    ReductionOptions withDanglingLlines(boolean withDanglingLines) {
        this.withDanglingLines = withDanglingLines;
        return this;
    }

}
