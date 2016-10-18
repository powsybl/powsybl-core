/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import java.io.PrintStream;
import java.io.Serializable;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineWorkflowStartParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int sampleQueueSize;

    private final int samplingThreads;

    private final int samplesPerThread;

    private final int stateQueueSize;

    private final int duration;

    private final int maxProcessedSamples;

    public static OfflineWorkflowStartParameters load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("offline-default-start-parameters");
        int duration = config.getIntProperty("duration", -1);
        int sampleQueueSize = config.getIntProperty("sampleQueueSize");
        int samplingThreads = config.getIntProperty("samplingThreads");
        int samplesPerThread = config.getIntProperty("samplesPerThread");
        int stateQueueSize = config.getIntProperty("stateQueueSize", -1);
        int maxProcessedSamples = config.getIntProperty("maxProcessedSamples", -1);
        return new OfflineWorkflowStartParameters(sampleQueueSize, samplingThreads, samplesPerThread, stateQueueSize, duration, maxProcessedSamples);
    }

    public OfflineWorkflowStartParameters(int sampleQueueSize, int samplingThreads, int samplesPerThread,
                                     int stateQueueSize, int duration, int maxProcessedSamples) {
        if (samplesPerThread > sampleQueueSize) {
            throw new IllegalArgumentException("samplesPerThread > sampleQueueSize");
        }
        this.sampleQueueSize = sampleQueueSize;
        this.samplingThreads = samplingThreads;
        this.samplesPerThread = samplesPerThread;
        this.stateQueueSize = stateQueueSize;
        this.duration = duration;
        this.maxProcessedSamples = maxProcessedSamples;
    }

    public int getSampleQueueSize() {
        return sampleQueueSize;
    }

    public int getSamplingThreads() {
        return samplingThreads;
    }

    public int getSamplesPerThread() {
        return samplesPerThread;
    }

    public int getStateQueueSize() {
        return stateQueueSize;
    }

    public int getDuration() {
        return duration;
    }

    public int getMaxProcessedSamples() {
        return maxProcessedSamples;
    }

    public void print(PrintStream out) {
        out.println("duration: " + duration);
        out.println("sample queue size: " + sampleQueueSize);
        out.println("sampling threads: " + samplingThreads);
        out.println("samples per thread: " + samplesPerThread);
        out.println("state queue size: " + stateQueueSize);
        out.println("max processed samples: " + maxProcessedSamples);
    }
    
    @Override
    public String toString() {
        return "{sampleQueueSize=" + sampleQueueSize + ", samplingThreads=" + samplingThreads
                + ", samplesPerThread=" + samplesPerThread + ", stateQueueSize=" + stateQueueSize
                + ", duration=" + duration + ", maxProcessedSamples=" + maxProcessedSamples + "}";
    }

}
