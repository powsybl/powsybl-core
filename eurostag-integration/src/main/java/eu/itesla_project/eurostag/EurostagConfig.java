/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;

import java.nio.file.Path;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagConfig {

    private static final int LF_DEFAULT_TIMEOUT = 60;
    private static final int SIM_DEFAULT_TIMEOUT = 3 * 60;
    private static final int IDX_DEFAULT_TIMEOUT = 2 * 60;
    private static final int DEFAULT_LF_MAX_NUM_ITERATION = 20;
    private static final double DEFAULT_MINIMUM_STEP = 0.000001;
    private static final boolean DEFAULT_LF_WARM_START = false;
    private static final boolean DEFAULT_USE_BROADCAST = true;
    private static final boolean DEFAULT_DDB_CACHING = true;
    private static final double DEFAULT_MIN_STEP_AT_END_OF_STABILIZATION = 1;

    private final Path eurostagHomeDir;

    private final Path indexesBinDir;

    private boolean lfNoGeneratorMinMaxQ;

    private final int lfTimeout;

    private final int simTimeout;

    private final int idxTimeout;

    private int lfMaxNumIteration;

    private double minimumStep;

    private boolean lfWarmStart;

    private boolean useBroadcast;

    private boolean ddbCaching;

    private double minStepAtEndOfStabilization;

    private boolean debug;

    public synchronized static EurostagConfig load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("eurostag");
        Path eurostagHomeDir = config.getPathProperty("eurostagHomeDir", null);
        Path indexesBinDir = config.getPathProperty("indexesBinDir", null);
        boolean lfNoGeneratorMinMaxQ = config.getBooleanProperty("lfNoGeneratorMinMaxQ", false);
        int lfTimeout = config.getIntProperty("lfTimeout", LF_DEFAULT_TIMEOUT);
        int simTimeout = config.getIntProperty("simTimeout", SIM_DEFAULT_TIMEOUT);
        int idxTimeout = config.getIntProperty("idxTimeout", IDX_DEFAULT_TIMEOUT);
        int lfMaxNumIteration = config.getIntProperty("lfMaxNumIteration", DEFAULT_LF_MAX_NUM_ITERATION);
        double minimumStep = config.getDoubleProperty("minimumStep", DEFAULT_MINIMUM_STEP);
        boolean lfWarmStart = config.getBooleanProperty("lfWarmStart", DEFAULT_LF_WARM_START);
        boolean useBroadcast = config.getBooleanProperty("useBroadcast", DEFAULT_USE_BROADCAST);
        boolean ddbCaching = config.getBooleanProperty("ddbCaching", DEFAULT_DDB_CACHING);
        double minStepAtEndOfStabilization = config.getDoubleProperty("minStepAtEndOfStabilization", DEFAULT_MIN_STEP_AT_END_OF_STABILIZATION);
        boolean debug = config.getBooleanProperty("debug", false);
        return new EurostagConfig(eurostagHomeDir, indexesBinDir, lfNoGeneratorMinMaxQ, lfTimeout, simTimeout, idxTimeout,
                                  lfMaxNumIteration, minimumStep, lfWarmStart, useBroadcast, ddbCaching, minStepAtEndOfStabilization,
                                  debug);
    }

    public EurostagConfig() {
        this(null, null, false, LF_DEFAULT_TIMEOUT, SIM_DEFAULT_TIMEOUT, IDX_DEFAULT_TIMEOUT, DEFAULT_LF_MAX_NUM_ITERATION,
                DEFAULT_MINIMUM_STEP, DEFAULT_LF_WARM_START, DEFAULT_USE_BROADCAST, DEFAULT_DDB_CACHING, DEFAULT_MIN_STEP_AT_END_OF_STABILIZATION,
                false);
    }

    public EurostagConfig(Path eurostagHomeDir, Path indexesBinDir, boolean lfNoGeneratorMinMaxQ, int lfTimeout, int simTimeout,
                          int idxTimeout, int lfMaxNumIteration, double minimumStep, boolean lfWarmStart, boolean useBroadcast,
                          boolean ddbCaching, double minStepAtEndOfStabilization, boolean debug) {
        if (lfTimeout < -1 || lfTimeout == 0) {
            throw new IllegalArgumentException("invalid load flow timeout value " + lfTimeout);
        }
        if (simTimeout < -1 || simTimeout == 0) {
            throw new IllegalArgumentException("invalid simulation timeout value " + simTimeout);
        }
        if (idxTimeout < -1 || idxTimeout == 0) {
            throw new IllegalArgumentException("invalid indexes timeout value " + idxTimeout);
        }
        if (lfMaxNumIteration <= 0) {
            throw new IllegalArgumentException("invalid load flow max number of iteration " + lfMaxNumIteration);
        }
        this.eurostagHomeDir = eurostagHomeDir;
        this.indexesBinDir = indexesBinDir;
        this.lfNoGeneratorMinMaxQ = lfNoGeneratorMinMaxQ;
        this.lfTimeout = lfTimeout;
        this.simTimeout = simTimeout;
        this.idxTimeout = idxTimeout;
        this.lfMaxNumIteration = lfMaxNumIteration;
        this.minimumStep = minimumStep;
        this.lfWarmStart = lfWarmStart;
        this.useBroadcast = useBroadcast;
        this.ddbCaching = ddbCaching;
        this.minStepAtEndOfStabilization = minStepAtEndOfStabilization;
        this.debug = debug;
    }

    public Path getEurostagHomeDir() {
        return eurostagHomeDir;
    }

    public Path getIndexesBinDir() {
        return indexesBinDir;
    }

    public boolean isLfNoGeneratorMinMaxQ() {
        return lfNoGeneratorMinMaxQ;
    }

    public void setLfNoGeneratorMinMaxQ(boolean lfNoGeneratorMinMaxQ) {
        this.lfNoGeneratorMinMaxQ = lfNoGeneratorMinMaxQ;
    }

    public int getLfTimeout() {
        return lfTimeout;
    }

    public int getSimTimeout() {
        return simTimeout;
    }

    public int getIdxTimeout() {
        return idxTimeout;
    }

    public int getLfMaxNumIteration() {
        return lfMaxNumIteration;
    }

    public void setLfMaxNumIteration(int lfMaxNumIteration) {
        this.lfMaxNumIteration = lfMaxNumIteration;
    }

    public double getMinimumStep() {
        return minimumStep;
    }

    public void setMinimumStep(double minimumStep) {
        this.minimumStep = minimumStep;
    }

    public boolean isLfWarmStart() {
        return lfWarmStart;
    }

    public void setLfWarmStart(boolean lfWarmStart) {
        this.lfWarmStart = lfWarmStart;
    }

    public boolean isUseBroadcast() {
        return useBroadcast;
    }

    public void setUseBroadcast(boolean useBroadcast) {
        this.useBroadcast = useBroadcast;
    }

    public boolean isDdbCaching() {
        return ddbCaching;
    }

    public void setDdbCaching(boolean ddbCaching) {
        this.ddbCaching = ddbCaching;
    }

    public double getMinStepAtEndOfStabilization() {
        return minStepAtEndOfStabilization;
    }

    public void setMinStepAtEndOfStabilization(double minStepAtEndOfStabilization) {
        this.minStepAtEndOfStabilization = minStepAtEndOfStabilization;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [eurostagHomeDir=" + eurostagHomeDir +
                                            ", indexesBinDir=" + indexesBinDir +
                                            ", lfNoGeneratorMinMaxQ=" + lfNoGeneratorMinMaxQ +
                                            ", lfTimeout=" + lfTimeout +
                                            ", simTimeout=" + simTimeout +
                                            ", idxTimeout=" + idxTimeout +
                                            ", lfMaxNumIteration=" + lfMaxNumIteration +
                                            ", minimumStep=" + minimumStep +
                                            ", lfWarmStart=" + lfWarmStart +
                                            ", useBroadcast=" + useBroadcast +
                                            ", ddbCaching=" + ddbCaching +
                                            ", minStepAtEndOfStabilization=" + minStepAtEndOfStabilization +
                                            ", debug=" + debug +
                                            "]";
    }

}
