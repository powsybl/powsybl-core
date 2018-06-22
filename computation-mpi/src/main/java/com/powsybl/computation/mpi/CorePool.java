/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CorePool {

    private final Deque<Core> cores = new ArrayDeque<>();

    private final Multimap<Integer, Core> coresPerRank = HashMultimap.create();

    List<Core> borrowCores(int n) {
        return borrowCores(n, null);
    }

    synchronized List<Core> borrowCores(int n, Set<Integer> preferedRanks) {
        checkCores(n);
        List<Core> borrowedCores = new ArrayList<>(n);
        if (preferedRanks != null && !preferedRanks.isEmpty()) {
            END: for (int preferedRank : preferedRanks) {
                for (Core preferedCore : coresPerRank.get(preferedRank)) {
                    if (borrowedCores.size() >= n) {
                        break END;
                    }
                    borrowedCores.add(preferedCore);
                }
            }
            for (Core borrowedCore : borrowedCores) {
                cores.remove(borrowedCore);
                coresPerRank.remove(borrowedCore.rank, borrowedCore);
            }
        }
        while (borrowedCores.size() < n) {
            Core borrowedCore = cores.poll();
            borrowedCores.add(borrowedCore);
            coresPerRank.remove(borrowedCore.rank, borrowedCore);
        }
        return borrowedCores;
    }

    private void checkCores(int n) {
        if (n > cores.size()) {
            throw new IllegalArgumentException("Not enough cores");
        }
    }

    synchronized void returnCore(Core borrowedCore) {
        cores.push(borrowedCore);
        coresPerRank.put(borrowedCore.rank.num, borrowedCore);
    }

    void returnCores(List<Core> borrowedCores) {
        for (Core borrowedCore : borrowedCores) {
            returnCore(borrowedCore);
        }
    }

    synchronized int availableCores() {
        return cores.size();
    }

}
