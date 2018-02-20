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
public class SoutTaskListener implements TaskListener {

    private final PrintStream out;

    public SoutTaskListener(PrintStream out) {
        this.out = Objects.requireNonNull(out);
    }

    @Override
    public String getProjectId() {
        return null;
    }

    @Override
    public void onEvent(TaskEvent event) {
        if (event instanceof UpdateTaskMessageEvent) {
            out.println(((UpdateTaskMessageEvent) event).getMessage());
        }
    }
}
