/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.tools;

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

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
class DynamicSecurityAnalysisCommandOptionsTest {

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
        DynamicSecurityAnalysisCommandOptions options = new DynamicSecurityAnalysisCommandOptions();

        assertEquals("dynamic-security-analysis", options.getCommandName());
        assertThatNullPointerException().isThrownBy(options::toCommand);

        options.caseFile(fileSystem.getPath("test.xiidm"));
        options.dynamicModelsFile(fileSystem.getPath("dynamic_models.groovy"));

        SimpleCommand cmd = options.toCommand();
        String expectedDefaultProgram = SystemUtils.IS_OS_WINDOWS ? "itools.bat" : "itools";
        assertEquals(expectedDefaultProgram, cmd.getProgram());
        assertEquals("dynamic-security-analysis", cmd.getId());
        List<String> args = cmd.getArgs(0);
        assertThat(args)
                .containsExactly("dynamic-security-analysis", "--case-file=test.xiidm",
                        "--dynamic-models-file=dynamic_models.groovy");

        options.id("my-id");
        assertEquals("my-id", options.toCommand().getId());

        options.itoolsCommand("/path/to/itools");
        assertEquals("/path/to/itools", options.toCommand().getProgram());

        options.absolutePaths(true);

        assertThat(options.toCommand().getArgs(0))
                .containsExactly("dynamic-security-analysis", "--case-file=/work/test.xiidm",
                        "--dynamic-models-file=/work/dynamic_models.groovy");

        options.contingenciesFile(fileSystem.getPath("contingencies.groovy"))
                .eventModelsFile(fileSystem.getPath("event_models.groovy"))
                .parametersFile(fileSystem.getPath("parameters.json"))
                .taskCount(5)
                .violationType(LimitViolationType.CURRENT)
                .resultExtension("ext")
                .outputFile(fileSystem.getPath("result.txt"), "TXT")
                .logFile(fileSystem.getPath("log.zip"));

        assertThat(options.toCommand().getArgs(0))
                .containsExactlyInAnyOrder("dynamic-security-analysis",
                        "--case-file=/work/test.xiidm",
                        "--dynamic-models-file=/work/dynamic_models.groovy",
                        "--parameters-file=/work/parameters.json",
                        "--contingencies-file=/work/contingencies.groovy",
                        "--event-models-file=/work/event_models.groovy",
                        "--task-count=5",
                        "--limit-types=CURRENT",
                        "--with-extensions=ext",
                        "--output-file=/work/result.txt",
                        "--output-format=TXT",
                        "--log-file=/work/log.zip");

        options.resultExtension("ext2")
                .violationType(LimitViolationType.HIGH_VOLTAGE);

        assertThat(options.toCommand().getArgs(0))
                .contains("--limit-types=CURRENT,HIGH_VOLTAGE",
                        "--with-extensions=ext,ext2");

        options.task(new Partition(1, 2));
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(options::toCommand);
    }

    @Test
    void testIndexed() {
        DynamicSecurityAnalysisCommandOptions options = new DynamicSecurityAnalysisCommandOptions()
                    .caseFile(fileSystem.getPath("test.xiidm"))
                    .dynamicModelsFile(fileSystem.getPath("dynamic_models.groovy"))
                    .taskBasedOnIndex(24)
                    .outputFile(i -> fileSystem.getPath(String.format("output_%d.json", i)), "JSON")
                    .logFile(i -> fileSystem.getPath(String.format("log_%d.zip", i)));

        assertThat(options.toCommand().getArgs(0))
                .contains("--task=1/24",
                        "--output-file=output_0.json",
                        "--output-format=JSON",
                        "--log-file=log_0.zip");
        assertThat(options.toCommand().getArgs(4))
                .contains("--task=5/24",
                        "--output-file=output_4.json",
                        "--output-format=JSON",
                        "--log-file=log_4.zip");
    }

    @Test
    void testStrategies() {
        DynamicSecurityAnalysisCommandOptions options = new DynamicSecurityAnalysisCommandOptions()
                .caseFile(fileSystem.getPath("test.xiidm"))
                .dynamicModelsFile(fileSystem.getPath("dynamic_models.groovy"))
                .parametersFile(fileSystem.getPath("params.json"))
                .actionsFile(fileSystem.getPath("actions.json"))
                .strategiesFile(fileSystem.getPath("strategies.json"))
                .limitReductionsFile(fileSystem.getPath("limit-reductions.json"));

        SimpleCommand cmd = options.toCommand();
        String expectedDefaultProgram = SystemUtils.IS_OS_WINDOWS ? "itools.bat" : "itools";
        assertEquals(expectedDefaultProgram, cmd.getProgram());
        assertEquals("dynamic-security-analysis", cmd.getId());
        List<String> args = cmd.getArgs(0);
        assertThat(args)
                .containsExactly("dynamic-security-analysis",
                        "--case-file=test.xiidm",
                        "--parameters-file=params.json",
                        "--actions-file=actions.json",
                        "--strategies-file=strategies.json",
                        "--limit-reductions-file=limit-reductions.json",
                        "--dynamic-models-file=dynamic_models.groovy");

    }

}
