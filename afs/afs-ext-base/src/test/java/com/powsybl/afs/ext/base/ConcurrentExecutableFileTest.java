/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.afs.ext.base;

import com.powsybl.afs.AbstractProjectFileTest;
import com.powsybl.afs.Folder;
import com.powsybl.afs.Project;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.InMemoryEventsBus;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public class ConcurrentExecutableFileTest extends AbstractProjectFileTest{
    static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    @Override
    protected AppStorage createStorage() {
        return MapDbAppStorage.createMem("mem", new InMemoryEventsBus());
    }

    @Test
    public void tesRunConcurrent() throws InterruptedException, ConcurrentExecutionException {
        storage.createRootNodeIfNotExists(storage.getFileSystemName(), Folder.PSEUDO_CLASS);
        Project project = afs.getRootFolder().createProject("project");
        NodeInfo info = storage.createNode(project.getRootFolder().getId(), "test", FooFileConcurrent.PSEUDO_CLASS, "", 0, new NodeGenericMetadata());
        storage.setConsistent(info.getId());
        FooFileConcurrent fooFile = new FooFileConcurrent(new ProjectFileCreationContext(info, storage, project), 0);

        FooRunConfig runConfig1 = new FooRunConfig();
        FooRunConfig runConfig2 = new FooRunConfig();

        THREAD_POOL.submit(() -> {
            fooFile.run(runConfig1);
        });
        runConfig1.started.await();
        THREAD_POOL.submit(() -> {
            assertThatCode(() -> fooFile.run(runConfig2)).isInstanceOf(ConcurrentExecutionException.class);
            runConfig1.wait.countDown();
        });

        runConfig1.ended.await();
        assertThat(runConfig1.ended.getCount()).isEqualTo(0);
        assertThat(runConfig2.started.getCount()).isEqualTo(1);

        THREAD_POOL.submit(() -> {
            fooFile.run(runConfig2);
        });

        runConfig2.started.await();
        runConfig2.wait.countDown();
        runConfig1.ended.await();
        assertThat(runConfig2.ended.getCount()).isEqualTo(0);
    }

    static class FooRunConfig {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch wait = new CountDownLatch(1);
        CountDownLatch ended = new CountDownLatch(1);
    }

    static class FooFileConcurrent extends ConcurrentExecutableFile<FooRunConfig> {
        static String PSEUDO_CLASS = "FooFile";

        protected FooFileConcurrent(ProjectFileCreationContext context, int codeVersion) {
            super(context, codeVersion);
        }

        @Override
        protected void doRun(FooRunConfig configurationParameters) {
            try {
                configurationParameters.started.countDown();
                configurationParameters.wait.await();
            } catch (InterruptedException e) {
                // ignored
            } finally {
                configurationParameters.ended.countDown();
            }
        }
    }
}