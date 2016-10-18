/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import java.nio.file.Path;

import eu.itesla_project.loadflow.api.LoadFlowFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DymolaConfig {

/*

#example of dymola-simulation.properties

dymolaSeviceWSDL=http://127.0.0.1:8888/dymservice?wsdl
indexesBinDir=/home/itesla/itesla/bin
modelicaVersion=3.2
sourceEngine=psse
sourceEngineVersion=32.1
modelicaPowerSystemLibraryFile=/home/itesla/modelica/Nordic44/dymolainputs/PowerSystems.mo
slackId=
debug=true
indexNames=smallsignal,overload,underovervoltage,transient
*/

    private static final Logger LOGGER = LoggerFactory.getLogger(DymolaConfig.class);

    private static final int SIM_DEFAULT_TIMEOUT = 3 * 60;
    private static final int IDX_DEFAULT_TIMEOUT = 2 * 60;

    private static DymolaConfig INSTANCE;

    private final String dymolaSeviceWSDL;

    private final Path indexesBinDir;

    private final int simTimeout;

    private final int idxTimeout;

    private final boolean debug;

    private String modelicaVersion;

    private String sourceEngine;

    private String sourceEngineVersion;

    private String modelicaPowerSystemLibraryFile;

    private final boolean fakeDymolaExecution;

    private String indexesNames;

    private String slackId;

    private final Class<? extends LoadFlowFactory> loadFlowFactoryClass;

    public synchronized static DymolaConfig load() {
        if (INSTANCE == null) {
            ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("dymola");
            String dymolaSeviceWSDL = config.getStringProperty("dymolaSeviceWSDL", null);
            Path indexesBinDir = config.getPathProperty("indexesBinDir", null);
            int simTimeout = config.getIntProperty("simTimeout", SIM_DEFAULT_TIMEOUT);
            int idxTimeout = config.getIntProperty("idxTimeout", IDX_DEFAULT_TIMEOUT);
            boolean debug = config.getBooleanProperty("debug", false);
            String modelicaVersion = config.getStringProperty("modelicaVersion");;
            String sourceEngine = config.getStringProperty("sourceEngine");;
            String sourceEngineVersion = config.getStringProperty("sourceEngineVersion");;
            String modelicaPowerSystemLibraryFile = config.getStringProperty("modelicaPowerSystemLibraryFile");
            boolean fakeDymolaExecution = config.getBooleanProperty("fakeDymolaExecution", false);
            String indexesNames = config.getStringProperty("indexesNames");
            String slackId = config.getStringProperty("slackId",null);
            Class<? extends LoadFlowFactory> loadFlowFactoryClass = config.getClassProperty("loadFlowFactoryClass", LoadFlowFactory.class);

            INSTANCE = new DymolaConfig(dymolaSeviceWSDL, indexesBinDir, simTimeout, idxTimeout, debug, modelicaVersion, sourceEngine, sourceEngineVersion, modelicaPowerSystemLibraryFile, fakeDymolaExecution, indexesNames, slackId, loadFlowFactoryClass);
        }
        return INSTANCE;
    }


    public DymolaConfig(String dymolaSeviceWSDL, Path indexesBinDir, int simTimeout, int idxTimeout, boolean debug,String modelicaVersion,String sourceEngine, String sourceEngineVersion, String modelicaPowerSystemLibraryFile, boolean fakeDymolaExecution, String indexesNames, String slackId, Class<? extends LoadFlowFactory> loadFlowFactoryClass) {
        if (simTimeout < -1 || simTimeout == 0) {
            throw new IllegalArgumentException("invalid simulation timeout value " + simTimeout);
        }
        if (idxTimeout < -1 || idxTimeout == 0) {
            throw new IllegalArgumentException("invalid indexes timeout value " + idxTimeout);
        }
        this.dymolaSeviceWSDL = dymolaSeviceWSDL;
        this.indexesBinDir = indexesBinDir;
        this.simTimeout = simTimeout;
        this.idxTimeout = idxTimeout;
        this.debug = debug;
        this.modelicaVersion=modelicaVersion;
        this.sourceEngine=sourceEngine;
        this.sourceEngineVersion=sourceEngineVersion;
        this.modelicaPowerSystemLibraryFile=modelicaPowerSystemLibraryFile;
        this.fakeDymolaExecution=fakeDymolaExecution;
        this.indexesNames=indexesNames;
        this.slackId=slackId;
        this.loadFlowFactoryClass = loadFlowFactoryClass;
    }

    public String getDymolaSeviceWSDL() { return dymolaSeviceWSDL; }

    public Path getIndexesBinDir() {
        return indexesBinDir;
    }

    public int getSimTimeout() {
        return simTimeout;
    }

    public int getIdxTimeout() {
        return idxTimeout;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getSourceEngineVersion() { return sourceEngineVersion; }

    public String getSourceEngine() { return sourceEngine; }

    public String getModelicaVersion() { return modelicaVersion; }

    public String getModelicaPowerSystemLibraryFile() { return modelicaPowerSystemLibraryFile; }

    public boolean isFakeDymolaExecution() { return fakeDymolaExecution; }

    public String[] getIndexesNames() {return indexesNames.split("\\s*,\\s*");}

    public String getSlackId() { return slackId; }

    public Class<? extends LoadFlowFactory> getLoadFlowFactoryClass() {
        return loadFlowFactoryClass;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                " [dymolaSeviceWSDL=" + dymolaSeviceWSDL +
                ", indexesBinDir=" + indexesBinDir +
                ", simTimeout=" + simTimeout +
                ", idxTimeout=" + idxTimeout +
                ", modelicaVersion=" + modelicaVersion +
                ", modelicaPowerSystemLibraryFile=" + modelicaPowerSystemLibraryFile +
                ", sourceEngine=" + sourceEngine +
                ", sourceEngineVersion=" + sourceEngineVersion +
                ", indexesNames=" + indexesNames +
                ", slackID=" + slackId +
                ", loadFlowFactoryClass=" + loadFlowFactoryClass +
                ", debug=" + debug +
                "]";
    }

}
