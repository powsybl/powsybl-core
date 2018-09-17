/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import com.powsybl.commons.PowsyblException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class MpiResources {

    private static final Logger LOGGER = LoggerFactory.getLogger(MpiResources.class);

    private final int availableCores;
    private final Semaphore semaphore;
    private final CorePool idleCores = new CorePool();

    MpiResources(int communicatorSize, int coresPerRank) {
        if (communicatorSize < 2) {
            throw new IllegalArgumentException("MPI communicator size must be >= 2");
        }
        if (coresPerRank < 1) {
            throw new IllegalArgumentException("Cores per rank must be > 1");
        }
        this.availableCores = (communicatorSize - 1) * coresPerRank;
        semaphore = new Semaphore(availableCores, true);
        // skip rank 0 which is the master
        for (int i = 1; i < communicatorSize; i++) {
            MpiRank rank = new MpiRank(i);
            for (int thread = 0; thread < coresPerRank; thread++) {
                idleCores.returnCore(new Core(rank, thread));
            }
        }
        LOGGER.info("Slaves: {}, coresPerSlave: {}, availableCores: {}",
                communicatorSize - 1, coresPerRank, availableCores);
    }

    List<Core> reserveAllCoresOrFail() {
        if (semaphore.tryAcquire(availableCores)) {
            List<Core> reservedCores = idleCores.borrowCores(availableCores);
            LOGGER.debug("MPI cores {} reserved", reservedCores);
            return reservedCores;
        } else {
            throw new PowsyblException("Fail to reserve all cores");
        }
    }

    List<Core> reserveCores(int required, Set<Integer> preferedRanks) {
        int provided;
        if (semaphore.tryAcquire(required)) {
            provided = required;
        } else {
            // try to get all permits
            int all = semaphore.drainPermits();
            if (all == 0) {
                // not permits availables
                return null;
            } else {
                if (all > required) {
                    // all is too many, release unnecessary ones
                    semaphore.release(all - required);
                    provided = required;
                } else {
                    provided = all;
                }
            }
        }
        List<Core> reservedCores = idleCores.borrowCores(provided, preferedRanks);
        LOGGER.debug("MPI cores {} reserved", reservedCores);
        return reservedCores;
    }

    void releaseCore(Core reservedCore) {
        idleCores.returnCore(reservedCore);
        semaphore.release();
        LOGGER.debug("MPI core {} released", reservedCore);
    }

    void releaseCores(List<Core> reservedCores) {
        idleCores.returnCores(reservedCores);
        semaphore.release(reservedCores.size());
        LOGGER.debug("MPI cores {} released", reservedCores);
    }

    int getAvailableCores() {
        return availableCores;
    }

    int getIdleCores() {
        return semaphore.availablePermits();
    }

    int getBusyCores() {
        return availableCores - semaphore.availablePermits();
    }

}
