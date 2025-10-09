/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class LoadFlowResultsCompletionPostProcessorTest extends AbstractLoadFlowResultsCompletionTest {

    private InMemoryPlatformConfig platformConfig;
    private FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        super.setUp();
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void process() throws Exception {
        setNanValues();

        LoadFlowResultsCompletionPostProcessor processor = new LoadFlowResultsCompletionPostProcessor(platformConfig);
        assertEquals(LoadFlowResultsCompletionPostProcessor.NAME, processor.getName());
        processor.process(network, null);

        checkResultsCompletion();
    }
}
