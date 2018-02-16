/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * @deprecated Use {@link AbstractExecutionHandler} instead.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @param <R>
 */
@Deprecated
public class DefaultExecutionHandler<R> extends AbstractExecutionHandler<R> {

    @Override
    public List<CommandExecution> before(Path workingDir) throws IOException {
        throw new IllegalStateException("before method has to be implemented");
    }
}
