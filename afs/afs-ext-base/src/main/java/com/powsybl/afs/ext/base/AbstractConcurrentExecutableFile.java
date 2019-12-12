/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.afs.ext.base;

import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.ProjectFileCreationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public abstract class AbstractConcurrentExecutableFile<T> extends ProjectFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConcurrentExecutableFile.class);
    private static final String RUN_LOCK_DB = "ExecutableFileRunLock";

    protected AbstractConcurrentExecutableFile(ProjectFileCreationContext context, int codeVersion) {
        super(context, codeVersion);
    }

    protected abstract void doRun(T configurationParameters);

    public void run(T configurationParameters) throws ConcurrentExecutionException {
        if (tryLock()) {
            try {
                doRun(configurationParameters);
            } finally {
                unlock();
            }
        } else {
            throw new ConcurrentExecutionException();
        }
    }

    public boolean tryLock() {
        storage.flush();
        if (storage.dataExists(info.getId(), RUN_LOCK_DB)) {
            return false;
        }
        try {
            storage.writeBinaryData(info.getId(), RUN_LOCK_DB).close();
            storage.flush();
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to write lock for executable file {}", this.info, e);
            return false;
        }
    }

    private void unlock() {
        storage.removeData(info.getId(), RUN_LOCK_DB);
        storage.flush();
    }
}
