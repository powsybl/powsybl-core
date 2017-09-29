/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.mpi.util;

import eu.itesla_project.computation.mpi.MpiExecutorContext;
import eu.itesla_project.computation.mpi.MpiExecutorContextFactory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MultiStateNetworkAwareMpiExecutorContextFactory implements MpiExecutorContextFactory {
    @Override
    public MpiExecutorContext create() {
        return new MultiStateNetworkAwareMpiExecutorContext();
    }
}
