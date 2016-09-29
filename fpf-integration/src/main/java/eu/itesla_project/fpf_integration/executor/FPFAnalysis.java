/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.fpf_integration.executor;

import eu.itesla_project.computation.*;
import eu.itesla_project.fpf_integration.Converter;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.mcla.ForecastErrorsDataStorageImpl;
import eu.itesla_project.mcla.Utils;
import eu.itesla_project.mcla.montecarlo.StatsCondCalculator;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.modules.mcla.ForecastErrorsStatistics;
import eu.itesla_project.modules.mcla.MontecarloSamplerParameters;
import eu.itesla_project.modules.online.TimeHorizon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class FPFAnalysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(FPFAnalysis.class);
    private static final String FPF_CLASSIC_INPUT_FILE = "FPF_Classic_input.txt";
    private static final String FPF_CLASSIC_INPUT_FILE_CONT_MAPPING = "FPF_Classic_input_cont_map.txt";
    private static final String FPFCLASSIC_OUTPUT_FILE_ZIP = "fpfclassic_output.zip";
    private static final String FPFCLASSIC_COMMAND_ID = "fpfclassic";
    private static final String WRAPPERFPFSCRIPTNAME = "runFPFClassic.sh";
    private static final String FPFCLASSIC_EXEC_PREFIX = "itesla_fpf_classic_";


    private Network network;
    private ComputationManager computationManager;
    private ForecastErrorsDataStorageImpl feDataStorage;


    public void init(Network network, ComputationManager computationManager, ForecastErrorsDataStorageImpl feDataStorage) {
        this.network = network;
        this.computationManager = computationManager;
        this.feDataStorage = feDataStorage;
    }

    private static final int BUFFER_SIZE = 4096;
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
    private void extractFPFOutputs(String analysysId, Path fpfZipOutputFile, Path mappingFile, Path outDir) throws IOException {
        String outPrefix=analysysId+"_"+new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        File destDir = outDir.toFile();
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(fpfZipOutputFile.toFile()));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {

            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                if (entry.getName().endsWith(".txt")) {
                    String path0=entry.getName();
                    if (path0.lastIndexOf("\\") != -1) {
                        path0=path0.substring(path0.lastIndexOf("\\")+1);
                    }
                    String filePath = outDir + File.separator + outPrefix+"_"+path0;
                    extractFile(zipIn, filePath);
                    Files.copy(mappingFile, outDir.resolve(outPrefix+"_mapping.txt"), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                // if the entry is a directory, make the directory
//                File dir = new File(filePath);
//                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();

    }

    private Command createFPFClassicCommand(Path outputDir, Path inputFile) {
        List<String> args1 = new ArrayList<>();
        args1.add("./");
        args1.add(FPF_CLASSIC_INPUT_FILE);
        return new SimpleCommandBuilder()
                .id(FPFCLASSIC_COMMAND_ID)
                .program(Paths.get(WRAPPERFPFSCRIPTNAME).toAbsolutePath().toString())
                .args(args1)
                .inputFiles(new InputFile(FPF_CLASSIC_INPUT_FILE))
                .outputFiles(new OutputFile(FPFCLASSIC_OUTPUT_FILE_ZIP))
                .build();
    }


    public void runFPFCLASSIC(ComputationManager computationManager, ForecastErrorsStatistics fes, List<Contingency> lc, String analysysId, Path destPath) throws Exception {
        Map<String, String> env = new HashMap<>();
        try (CommandExecutor executor = computationManager.newCommandExecutor(
                env, FPFCLASSIC_EXEC_PREFIX, true)) {

            final Path workingDir = executor.getWorkingDir();
            final Path fpfInputFile = workingDir.resolve(FPF_CLASSIC_INPUT_FILE);
            final Path fpfInputFileContMapping = workingDir.resolve(FPF_CLASSIC_INPUT_FILE_CONT_MAPPING);

            LOGGER.info(" - preparing FPFClassic input file");
            //convert to, and put FPFClassic input file in the working dir
            Converter.convert(network, lc, fes, fpfInputFile,fpfInputFileContMapping);

            Command cmd = createFPFClassicCommand(destPath, fpfInputFile);
            ExecutionReport report = executor.start(new CommandExecution(cmd, 1, Integer.MAX_VALUE));
            report.log();
            if (report.getErrors().size()==0) {
                LOGGER.info(" -- output files, here: {}", destPath.toFile().getAbsolutePath());
                //Files.copy(workingDir.resolve("fpfclassic_output.zip"), destPath.resolve("fpfclassic_output.zip"), StandardCopyOption.REPLACE_EXISTING);
                extractFPFOutputs(analysysId, workingDir.resolve(FPFCLASSIC_OUTPUT_FILE_ZIP), fpfInputFileContMapping, destPath);
            } else {
                LOGGER.error(" -- error details, here: {}", workingDir.resolve("fpfclassic_0.out").toFile().getAbsolutePath());
            }
        }
    }


    public void run(String analysisId, TimeHorizon th, List<Contingency> lc, Path outputDirPath) throws Exception {
        LOGGER.info(" - retrieving stats data");
        StatsCondCalculator sc = new StatsCondCalculator(network, computationManager, feDataStorage);
        sc.init(new MontecarloSamplerParameters(th, analysisId, 1));
        Path statsCondTempFilePath = Files.createTempFile("statsCondFprFPF", ".tmp");
        sc.run(statsCondTempFilePath);
        ForecastErrorsStatistics fes = Utils.readStatisticsFromFile(statsCondTempFilePath);
        LOGGER.info(fes.toString());
        runFPFCLASSIC(computationManager, fes, lc, analysisId, outputDirPath);
    }


/*
    public static void main(String[] args) throws Exception {
        FPFAnalysis fpfa= new FPFAnalysis();
        fpfa.extractFPFOutputs("analysisID00", Paths.get("/root/tempo/todelete3/fpfclassic_output.zip"), Paths.get("/root/tempo/todelete3/FPF_Classic_input_cont_map.txt") , Paths.get("/root/tempo/todelete3/outtest1"));
    }
*/
}
