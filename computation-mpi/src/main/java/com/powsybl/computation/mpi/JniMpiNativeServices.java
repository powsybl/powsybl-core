/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class JniMpiNativeServices implements MpiNativeServices {

    static {
        System.loadLibrary("master");
    }

    public native void initMpi(int coresPerRank, boolean verbose);

    public native void terminateMpi();

    public native String getMpiVersion();

    public native int getMpiCommSize();

    public native void sendCommonFile(byte[] message);

    public native void startTasks(List<MpiTask> tasks);

    public native void checkTasksCompletion(List<MpiTask> runningTasks, List<MpiTask> completedTasks);

}
