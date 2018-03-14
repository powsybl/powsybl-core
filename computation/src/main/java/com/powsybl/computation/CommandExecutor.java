/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.nio.file.Path;

/**
 * @deprecated Use ExecutionHandler instead.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Deprecated
public interface CommandExecutor extends AutoCloseable {

    @Deprecated
    default Path getWorkingDir() {
        throw new UnsupportedOperationException("deprecated");
    }

    @Deprecated
    default void start(CommandExecution execution, ExecutionListener listener) throws Exception {
        throw new UnsupportedOperationException("deprecated");
    }

    @Deprecated
    default ExecutionReport start(CommandExecution execution) throws Exception {
        throw new UnsupportedOperationException("deprecated");
    }

}
