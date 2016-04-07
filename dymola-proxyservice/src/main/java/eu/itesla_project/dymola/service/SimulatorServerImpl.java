/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola.service;

import com.dassault_systemes.dymola.DymolaException;
import com.dassault_systemes.dymola.DymolaInterface;
import com.dassault_systemes.dymola.DymolaWrapper;
import com.sun.xml.internal.ws.developer.StreamingAttachment;
import com.sun.xml.internal.ws.developer.StreamingDataHandler;
import eu.itesla_project.dymola.utils.MapUtils;
import eu.itesla_project.dymola.utils.Pool;
import eu.itesla_project.dymola.utils.ZipFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.ZipFile;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;

import static eu.itesla_project.dymola.utils.MapUtils.entry;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@MTOM
@StreamingAttachment(parseEagerly = true, memoryThreshold = 5000000L, dir="/tmp")
@WebService(endpointInterface = "eu.itesla_project.dymola.service.SimulatorServer")
public class SimulatorServerImpl implements SimulatorServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulatorServerImpl.class);

    static final String DYMOLASERVICE_TEMP = "/temp/Dymola/server";
    static final String DYMOLASERVICE_INPUTFILENAME = "dyninput.zip";
    static final String DYMSERV_PREFIX = "dymserv_";
    private static final String DYMOLA_LOG_FILENAME = "log.txt";
    private static final int MSGERRLEN = 400;

    boolean debug;

    final Pool<Integer> portPool;

    final String serviceWorkDir;

    //testing only
    private String fakeSourceDir=null;

    public SimulatorServerImpl() {
        this(DYMOLASERVICE_TEMP, 9000, 20, false);
    }


    public SimulatorServerImpl(String serviceWorkDir, int start, int size) {
        this(serviceWorkDir, start, size, false);
    }

    public SimulatorServerImpl(String serviceWorkDir, int start, int size, boolean debug) {
        // Set this flag to false if you want Dymola to be visible.
        // By default Dymola is hidden.
        //DymolaWrapper.nowindow = false;
        DymolaWrapper.nowindow = true;
        LOGGER.info("Dymola proxy service");
        LOGGER.info(" API version: {}",DymolaWrapper.dymola_version);
        LOGGER.info(" service working directory: {}", serviceWorkDir);
        LOGGER.info(" ports pool starting from {}, size {}", start, size);
        ArrayList<Integer> portArray = new ArrayList<>();
        for (int i = start; i < start + size; i++) {
            portArray.add(i);
        }
        portPool = new Pool<>(portArray);

        this.serviceWorkDir = serviceWorkDir;
        this.debug = debug;
    }


    //testing only
    public void setFakeSourceDir(String fakeSourceDir) {
        this.fakeSourceDir=fakeSourceDir;
    }

    protected void prepareOutputFile(Path workingDir, Map<String,String> fileNamesMap, Path outFile) throws IOException, URISyntaxException {
        Map<String, String> zip_properties = new HashMap<>();
        zip_properties.put("create", "true");
        zip_properties.put("encoding", "UTF-8");
        URI zip_disk = new URI("jar", outFile.toUri().toString(), null);
        try (FileSystem zipfs = FileSystems.newFileSystem(zip_disk, zip_properties)) {
            for (String filename : fileNamesMap.keySet()) {
                Path sourcePath = workingDir.resolve(filename);
                if (Files.exists(sourcePath)) {
                    Files.copy(workingDir.resolve(filename), zipfs.getPath(fileNamesMap.get(filename)));
                } else {
                    LOGGER.warn("file " + sourcePath + " does not exist. It won't be in " + outFile);
                }
            }
        }
    }

    public
    @XmlMimeType("application/octet-stream")
    DataHandler simulate(String inputFileName, String problem, double startTime, double stopTime, int numberOfIntervals, double outputInterval, String method, double tolerance, double fixedstepsize, String resultsFileName, @XmlMimeType("application/octet-stream") DataHandler data) {
        Path workingDir = null;
        try {
            long startms=System.currentTimeMillis();
            Path inputZipFile;
            try (StreamingDataHandler inputDh = (StreamingDataHandler) data) {
                Files.createDirectories(Paths.get(serviceWorkDir));
                workingDir = Files.createTempDirectory(Paths.get(serviceWorkDir), DYMSERV_PREFIX);
                Files.createDirectories(workingDir);
                inputZipFile = workingDir.resolve(DYMOLASERVICE_INPUTFILENAME);
                inputDh.moveTo(inputZipFile.toFile());
            }
            try (ZipFile zipFile = new ZipFile(inputZipFile.toFile())) {
                ZipFileUtil.unzipFileIntoDirectory(zipFile, workingDir.toFile());
            }
            long endms=System.currentTimeMillis();
            LOGGER.info(" {} - dymola simulation started - inputFileName:{}, problem:{}, startTime:{}, stopTime:{}, numberOfIntervals:{}, outputInterval:{}, method:{}, tolerance:{}, fixedstepsize:{}, resultsFileName:{}, input data unzipped in: {} ms.", workingDir, inputFileName, problem, startTime, stopTime, numberOfIntervals,outputInterval,method,tolerance,fixedstepsize,resultsFileName,(endms - startms));
            startms=System.currentTimeMillis();
            if (fakeSourceDir==null) {
                simulateDymola(workingDir.toString(), inputFileName, problem, startTime, stopTime, numberOfIntervals, outputInterval, method, tolerance, fixedstepsize, resultsFileName);
            } else {
                simulateDymolaFake(workingDir.toString(), inputFileName, problem, startTime, stopTime, numberOfIntervals, outputInterval, method, tolerance, fixedstepsize, resultsFileName);
            }

            endms=System.currentTimeMillis();
            long simulationTime=endms - startms;

            startms=System.currentTimeMillis();
            String outputZipFile = workingDir.toAbsolutePath() + ".zip";
            Map<String,String> fileNamesToInclude= MapUtils.asUnmodifiableMap(entry("log.txt", resultsFileName+"_log.txt"), entry("dslog.txt",resultsFileName+"_dslog.txt"), entry(resultsFileName + ".mat",resultsFileName + ".mat"));
            prepareOutputFile(workingDir, fileNamesToInclude, Paths.get(outputZipFile));
            endms=System.currentTimeMillis();
            LOGGER.info(" {} - dymola simulation terminated - simulation time: {} ms., output file zipped in: {} ms.", workingDir, simulationTime, (endms - startms));

            TemporaryFileDataSource outDataSource = new TemporaryFileDataSource(Paths.get(outputZipFile).toFile());
            //FileDataSource outDataSource = new FileDataSource(Paths.get(outputZipFile).toFile());
            DataHandler outputFileDataHandler = new DataHandler(outDataSource);
            return outputFileDataHandler;
        } catch (Exception e) {
            LOGGER.error(" {} - dymola simulation failed - inputFileName:{}, problem:{}, startTime:{}, stopTime:{}, numberOfIntervals:{}, outputInterval:{}, method:{}, tolerance:{}, fixedstepsize:{}, resultsFileName:{}",
                     workingDir, inputFileName, problem, startTime, stopTime, numberOfIntervals,outputInterval,method,tolerance,fixedstepsize,resultsFileName,e);
            String errMessg=e.getMessage();
            errMessg=((errMessg != null) && (errMessg.length() > MSGERRLEN)) ? errMessg.substring(0,MSGERRLEN) +" ..." : errMessg;
            throw new WebServiceException("dymola simulation failed - remote working directory " + workingDir +", fileName: "+ inputFileName +", problem:"+ problem +", error message:" + errMessg, e);
        } finally {
            if (debug == false) {
                try {
                    deleteDirectoryRecursively(workingDir);
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
        }
    }

    //testing only, get results for a precomputed simulation
    protected void simulateDymolaFake(String workingDirectory, String inputFileName, String problem, double startTime, double stopTime, int numberOfIntervals, double outputInterval, String method, double tolerance, double fixedstepsize, String resultsFileName) throws DymolaException {
        DymolaInterface dymola = null;
        int port = -1;
        try {
            boolean result = false;
            // Instantiate the Dymola interface and start Dymola
            try {
                port = portPool.getItem();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOGGER.debug("   START {} - portnumber: {} - ThreadID: {} - @Dymolainst: {} - @SimulatorServerImpl {} ", workingDirectory, (dymola!=null)?((DymolaWrapper) dymola).portnumber:"FAKE", Thread.currentThread().getId(), (dymola!=null)?dymola.hashCode():"FAKE", this.hashCode());

            try {
                Files.copy(Paths.get(fakeSourceDir).resolve("dymolasim_0.mat"),Paths.get(workingDirectory).resolve(resultsFileName+".mat"));
                Files.copy(Paths.get(fakeSourceDir).resolve("log.txt"),Paths.get(workingDirectory).resolve("log.txt"));
                Files.copy(Paths.get(fakeSourceDir).resolve("dslog.txt"),Paths.get(workingDirectory).resolve("dslog.txt"));
                try {
                    int waiting_time=1000*60*1;
                    LOGGER.info("simulating a real computation: waiting "+ (waiting_time/60000) + " minutes" );
                    Thread.sleep(waiting_time);
                    LOGGER.info("simulating a real computation: ended." );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(),e);
            }

            LOGGER.debug("   END {} - portnumber: {} - ThreadID: {} - @Dymolainst: {} - @SimulatorServerImpl {} ", workingDirectory, (dymola!=null)?((DymolaWrapper) dymola).portnumber:"FAKE", Thread.currentThread().getId(), (dymola!=null)? dymola.hashCode():"FAKE", this.hashCode());
        } finally {
            // The connection to Dymola is closed and Dymola is terminated
            if (dymola != null) {
                dymola.exit();
            }
            dymola = null;
            //release this port number to the pool
            if (port != -1) {
                portPool.putItem(port);
            }
        }

    }


    private void deleteDirectoryRecursively(Path aPath) throws IOException {
        Files.walkFileTree(aPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    protected void simulateDymola(String workingDirectory, String inputFileName, String problem, double startTime, double stopTime, int numberOfIntervals, double outputInterval, String method, double tolerance, double fixedstepsize, String resultsFileName) throws DymolaException {
        DymolaInterface dymola = null;
        int port = -1;
        try {
            boolean result = false;
            // Instantiate the Dymola interface and start Dymola
            try {
                port = portPool.getItem();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dymola = DymolaWrapper.getInstance(true, port);
            LOGGER.debug("   START {} - portnumber: {} - ThreadID: {} - @Dymolainst: {} - @SimulatorServerImpl {} ", workingDirectory, ((DymolaWrapper) dymola).portnumber, Thread.currentThread().getId(), dymola.hashCode(), this.hashCode());

            result= dymola.clear(false);
            if (!result) {
                throw new RuntimeException("CLEAR : " + dymola.getLastError());
            }

            result = dymola.cd(workingDirectory);
            if (!result) {
                throw new RuntimeException("CD : " + dymola.getLastError());
            }

            result = dymola.openModel(workingDirectory + "\\" + inputFileName);
            if (!result) {
                throw new RuntimeException("openModel: " + dymola.getLastError());
            }

            // Simulate the model
            /*
            public boolean simulateModel(java.lang.String problem,
                             double startTime,
                             double stopTime,
                             int numberOfIntervals,
                             double outputInterval,
                             java.lang.String method,
                             double tolerance,
                             double fixedstepsize,
                             java.lang.String resultFile)
                      throws DymolaException
             e.g.
             result = dymola.simulateModel(problem, startTime, stopTime, 0, 0, "Dassl", 0.0001, 0, problem);
			 */
            result = dymola.simulateModel(problem, startTime, stopTime, numberOfIntervals, outputInterval, method, tolerance, fixedstepsize, resultsFileName);
            if (!result) {
                throw new RuntimeException("simulateModel: " + dymola.getLastError());
            }

            result = dymola.savelog(DYMOLA_LOG_FILENAME);
            if (!result) {
                throw new RuntimeException("saveLog: " + dymola.getLastError());
            }

            LOGGER.debug("   END {} - portnumber: {} - ThreadID: {} - @Dymolainst: {} - @SimulatorServerImpl {} ", workingDirectory, ((DymolaWrapper) dymola).portnumber, Thread.currentThread().getId(), dymola.hashCode(), this.hashCode());
        } finally {
            // The connection to Dymola is closed and Dymola is terminated
            if (dymola != null) {
                dymola.exit();
            }
            dymola = null;
            //release this port number to the pool
            if (port != -1) {
                portPool.putItem(port);
            }
        }

    }


    private class TemporaryFileDataSource extends FileDataSource {

        public TemporaryFileDataSource(File file) {
            super(file);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new TemporaryFileInputStream(this.getFile());
        }

    }

    private class TemporaryFileInputStream extends FileInputStream {
        File file;

        public TemporaryFileInputStream(File file) throws FileNotFoundException {
            super(file);
            this.file = file;
        }

        @Override
        public void close() throws IOException {
            super.close();
            boolean isDeleted = file.delete();
            LOGGER.trace(" **** " + file.getAbsoluteFile() + " :" + isDeleted);
        }
    }
}