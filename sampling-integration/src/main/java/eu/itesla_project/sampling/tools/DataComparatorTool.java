/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.sampling.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import eu.itesla_project.computation.*;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.sampling.SamplerWp41Config;
import org.apache.commons.cli.CommandLine;

import com.google.auto.service.AutoService;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class DataComparatorTool implements Tool {

    private static final String M1INPUTFILENAME = "m1input.mat";
    private static final String CONCATSAMPLESFILENAME = "concat_m3output.mat";
    private static final String WORKING_DIR_PREFIX = "itesla_wp41validator_";
    private static final String WP41_CAT_MAT_FILES = "wp41_cat_mats";
    private static final String WP41_DATACOMPARATOR = "wp41_datacomparator";
    private static final String MOD3FILES_PATTERN = "MOD3*.mat";
    private static final String DATA_COMPARATOR_OUT_PNG = "dataComparatorOut.png";
    private static final String DATA_COMPARATOR_OUT_FIG = "dataComparatorOut.fig";

    @Override
	public Command getCommand() {
		return DataComparatorCommand.INSTANCE;
	}

    private Map<String, String> createEnv(SamplerWp41Config config) {
        Map<String, String> env = new HashMap<>();
        env.put("LD_LIBRARY_PATH", config.getRuntimeHomeDir().resolve("runtime").resolve("glnxa64").toString()
                        + ":" + config.getRuntimeHomeDir().resolve("bin").resolve("glnxa64").toString()
                        + ":" + config.getRuntimeHomeDir().resolve("sys").resolve("os").resolve("glnxa64").toString()
                        + ":" + config.getRuntimeHomeDir().resolve("sys").resolve("java").resolve("jre").resolve("glnxa64").resolve("jre").resolve("lib").resolve("amd64").resolve("native_threads").toString()
                        + ":" + config.getRuntimeHomeDir().resolve("sys").resolve("java").resolve("jre").resolve("glnxa64").resolve("jre").resolve("lib").resolve("amd64").resolve("server").toString()
                        + ":" + config.getRuntimeHomeDir().resolve("sys").resolve("java").resolve("jre").resolve("glnxa64").resolve("jre").resolve("lib").resolve("amd64").resolve("client").toString()
                        + ":" + config.getRuntimeHomeDir().resolve("sys").resolve("java").resolve("jre").resolve("glnxa64").resolve("jre").resolve("lib").resolve("amd64").toString()
        );
        return env;
    }

    private eu.itesla_project.computation.Command createConcatMatFilesCmd(Path dataPath, String pattern, Path outFile, SamplerWp41Config config) throws IOException {
        List<String> args1 = new ArrayList<>();
        args1.add(dataPath.toFile().getAbsolutePath()+"/");
        args1.add(pattern);
        args1.add(outFile.toFile().getAbsolutePath());

        String wp41CatMatFiles;
        if (config.getBinariesDir() != null) {
            wp41CatMatFiles = config.getBinariesDir().resolve(WP41_CAT_MAT_FILES).toAbsolutePath().toString();
        } else {
            wp41CatMatFiles = WP41_CAT_MAT_FILES;
        }

        return new SimpleCommandBuilder()
                .id(WP41_CAT_MAT_FILES)
                .program(wp41CatMatFiles)
                .args(args1)
//                .inputFiles(m1InputFilesList)
//                .outputFiles(new OutputFile(outFile.toString()))
                .build();
    }

    private eu.itesla_project.computation.Command createDataComparatorCmd(String file1, String file2, String set1, String set2, SamplerWp41Config config) {
        List<String> args1 = new ArrayList<>();
        args1.add(file1);
        args1.add(file2);
        if (!"".equals(set1+set2)) {
            args1.add(set1);
            args1.add(set2);
        }

        String wp41DataComparator;
        if (config.getBinariesDir() != null) {
            wp41DataComparator = config.getBinariesDir().resolve(WP41_DATACOMPARATOR).toAbsolutePath().toString();
        } else {
            wp41DataComparator = WP41_DATACOMPARATOR;
        }

        List<OutputFile> lout=new ArrayList<>();
        lout.add(new OutputFile(DATA_COMPARATOR_OUT_PNG));
        lout.add(new OutputFile(DATA_COMPARATOR_OUT_FIG));

        return new SimpleCommandBuilder()
                .id(WP41_DATACOMPARATOR)
                .program(wp41DataComparator)
                .args(args1)
//                .inputFiles(m1InputFilesList)
                .outputFiles(lout)
                .build();

    }

    @Override
    public void run(CommandLine line) throws Exception {
        try (ComputationManager computationManager = new LocalComputationManager()) {
            SamplerWp41Config config = SamplerWp41Config.load();

            //String dataDir = line.getOptionValue(DataComparatorCommand.DATA_DIR);
            String dataDir = config.getValidationDir().toFile().getAbsolutePath();
            if (!Files.exists(config.getValidationDir())) {
                throw new RuntimeException("validation data directory not found:  " + config.getValidationDir());
            }

            String oFilePrefix = line.getOptionValue("ofile");
            String set1 = line.hasOption("set1") ? line.getOptionValue("set1") : "";
            String set2 = line.hasOption("set2") ? line.getOptionValue("set2") : "";

            if ((!"".equals(set1 + set2)) && (("".equals(set1)) || ("".equals(set2)))) {
                throw new RuntimeException("either specify both set1 and set2 parameters, or none of them");
            }

            try (CommandExecutor executor = computationManager.newCommandExecutor(createEnv(config), WORKING_DIR_PREFIX, config.isDebug())) {
                Path workingDir = executor.getWorkingDir();
                eu.itesla_project.computation.Command cmd = createConcatMatFilesCmd(config.getValidationDir(), MOD3FILES_PATTERN, config.getValidationDir().resolve(CONCATSAMPLESFILENAME), config);
                int priority = 1;
                ExecutionReport report = executor.start(new CommandExecution(cmd, 1, priority));
                report.log();
                if (report.getErrors().isEmpty()) {
                    report = executor.start(new CommandExecution(createDataComparatorCmd(config.getValidationDir().resolve(M1INPUTFILENAME).toFile().getAbsolutePath(), config.getValidationDir().resolve(CONCATSAMPLESFILENAME).toFile().getAbsolutePath(), set1, set2, config), 1, priority));
                    report.log();
                    Files.copy(workingDir.resolve(DATA_COMPARATOR_OUT_FIG), Paths.get(oFilePrefix + ".fig"), REPLACE_EXISTING);
                    Files.copy(workingDir.resolve(DATA_COMPARATOR_OUT_PNG), Paths.get(oFilePrefix + ".png"), REPLACE_EXISTING);
                }
            }
        }
    }


}
