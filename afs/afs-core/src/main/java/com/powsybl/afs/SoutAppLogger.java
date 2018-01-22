/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SoutAppLogger implements AppLogger {

    private final PrintStream out;

    public SoutAppLogger(PrintStream out) {
        this.out = Objects.requireNonNull(out);
    }

    @Override
    public void log(String message, Object... args) {
        out.println(String.format(message, args));
    }

    @Override
    public AppLogger tagged(String tag) {
        return this;
    }
}
