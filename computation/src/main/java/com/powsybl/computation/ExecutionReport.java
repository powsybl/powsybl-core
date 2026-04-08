/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
public interface ExecutionReport {

    List<ExecutionError> getErrors();

    void log();

    default Optional<InputStream> getStdOut(Command command, int index) {
        return Optional.empty();
    }

    default Optional<InputStream> getStdErr(Command command, int index) {
        return Optional.empty();
    }
}
