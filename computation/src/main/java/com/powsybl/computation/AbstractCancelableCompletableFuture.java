/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractCancelableCompletableFuture extends CompletableFuture {

    protected volatile boolean cancel = false;
    protected Thread thread;

    protected void setThread(Thread t) {
        thread = Objects.requireNonNull(t);
    }

    @Override
    abstract public boolean cancel(boolean mayInterruptIfRunning); // stop thread, command send stop(ex: ctrl+c)

    @Override
    public boolean isCancelled() {
        return cancel;
    }
}
