/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.distributed;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.computation.Partition;
import com.powsybl.computation.SimpleCommand;
import com.powsybl.security.LimitViolationType;
import org.apache.commons.lang3.SystemUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class SecurityAnalysisCommandOptionsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAnalysisCommandOptionsTest.class);

    private FileSystem fileSystem;

    @BeforeEach
    void createFileSystem() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    void closeFileSystem() throws IOException {
        fileSystem.close();
    }

    @Test
    void test() {
        SecurityAnalysisCommandOptions options = new SecurityAnalysisCommandOptions();

        Assertions.assertThatNullPointerException().isThrownBy(options::toCommand);

        options.caseFile(fileSystem.getPath("test.xiidm"));

        SimpleCommand cmd = options.toCommand();
        String expectedDefaultProgram = SystemUtils.IS_OS_WINDOWS ? "itools.bat" : "itools";
        assertEquals(expectedDefaultProgram, cmd.getProgram());
        assertEquals("security-analysis", cmd.getId());
        List<String> args = cmd.getArgs(0);
        Assertions.assertThat(args)
                .containsExactly("security-analysis", "--case-file=test.xiidm");

        options.id("my-id");
        assertEquals("my-id", options.toCommand().getId());

        options.itoolsCommand("/path/to/itools");
        assertEquals("/path/to/itools", options.toCommand().getProgram());

        options.absolutePaths(true);

        Assertions.assertThat(options.toCommand().getArgs(0))
                .containsExactly("security-analysis", "--case-file=/work/test.xiidm");

        options.contingenciesFile(fileSystem.getPath("contingencies.groovy"))
                .parametersFile(fileSystem.getPath("parameters.json"))
                .taskCount(5)
                .violationType(LimitViolationType.CURRENT)
                .resultExtension("ext")
                .outputFile(fileSystem.getPath("result.txt"), "TXT")
                .logFile(fileSystem.getPath("log.zip"));

        Assertions.assertThat(options.toCommand().getArgs(0))
                .containsExactlyInAnyOrder("security-analysis",
                        "--case-file=/work/test.xiidm",
                        "--parameters-file=/work/parameters.json",
                        "--contingencies-file=/work/contingencies.groovy",
                        "--task-count=5",
                        "--limit-types=CURRENT",
                        "--with-extensions=ext",
                        "--output-file=/work/result.txt",
                        "--output-format=TXT",
                        "--log-file=/work/log.zip");

        options.resultExtension("ext2")
                .violationType(LimitViolationType.HIGH_VOLTAGE);

        Assertions.assertThat(options.toCommand().getArgs(0))
                .contains("--limit-types=CURRENT,HIGH_VOLTAGE",
                        "--with-extensions=ext,ext2");

        options.task(new Partition(1, 2));
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(options::toCommand);
    }

    @Test
    void testIndexed() {
        SecurityAnalysisCommandOptions options = new SecurityAnalysisCommandOptions()
                    .caseFile(fileSystem.getPath("test.xiidm"))
                    .taskBasedOnIndex(24)
                    .outputFile(i -> fileSystem.getPath(String.format("output_%d.json", i)), "JSON")
                    .logFile(i -> fileSystem.getPath(String.format("log_%d.zip", i)));

        Assertions.assertThat(options.toCommand().getArgs(0))
                .contains("--task=1/24",
                        "--output-file=output_0.json",
                        "--output-format=JSON",
                        "--log-file=log_0.zip");
        Assertions.assertThat(options.toCommand().getArgs(4))
                .contains("--task=5/24",
                        "--output-file=output_4.json",
                        "--output-format=JSON",
                        "--log-file=log_4.zip");
    }

    @Test
    void testStrategies() {
        SecurityAnalysisCommandOptions options = new SecurityAnalysisCommandOptions()
                .caseFile(fileSystem.getPath("test.xiidm"))
                .parametersFile(fileSystem.getPath("params.json"))
                .actionsFile(fileSystem.getPath("actions.json"))
                .strategiesFile(fileSystem.getPath("strategies.json"))
                .limitReductionsFile(fileSystem.getPath("limit-reductions.json"));

        SimpleCommand cmd = options.toCommand();
        String expectedDefaultProgram = SystemUtils.IS_OS_WINDOWS ? "itools.bat" : "itools";
        assertEquals(expectedDefaultProgram, cmd.getProgram());
        assertEquals("security-analysis", cmd.getId());
        List<String> args = cmd.getArgs(0);
        Assertions.assertThat(args)
                .containsExactly("security-analysis",
                        "--case-file=test.xiidm",
                        "--parameters-file=params.json",
                        "--actions-file=actions.json",
                        "--strategies-file=strategies.json",
                        "--limit-reductions-file=limit-reductions.json");

    }

}
