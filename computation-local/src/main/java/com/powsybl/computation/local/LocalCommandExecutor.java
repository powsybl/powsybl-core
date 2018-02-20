/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LocalCommandExecutor {

    int execute(String program, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException, InterruptedException;

    // kill -15
    void stop(Path workingDir);

    // kill -9
    void stopForcibly(Path workingDir) throws InterruptedException;
}
