/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CorePoolTest {

    public CorePoolTest() {
    }

    private CorePool pool;

    @Before
    public void setUp() {
        pool = new CorePool();
        MpiRank rank0 = new MpiRank(0);
        MpiRank rank1 = new MpiRank(1);
        pool.returnCore(new Core(rank0, 0));
        pool.returnCore(new Core(rank0, 1));
        pool.returnCore(new Core(rank1, 0));
        pool.returnCore(new Core(rank1, 1));
    }

    @After
    public void tearDown() {
        pool = null;
    }

    @Test
    public void testBorrowReturn() {
        assertTrue(pool.availableCores() == 4);
        List<Core> cores = pool.borrowCores(1);
        assertTrue(cores.size() == 1);
        assertTrue(pool.availableCores() == 3);
        pool.returnCore(cores.get(0));
        assertTrue(pool.availableCores() == 4);
    }

    @Test
    public void testBorrowAll() {
        assertTrue(pool.availableCores() == 4);
        List<Core> allCores = pool.borrowCores(4);
        assertTrue(allCores.size() == 4);
        assertTrue(pool.availableCores() == 0);
        pool.returnCores(allCores);
        assertTrue(pool.availableCores() == 4);
    }

    @Test
    public void testBorrowMoreThanSizePool() {
        assertTrue(pool.availableCores() == 4);
        try {
            pool.borrowCores(5);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void testPreferedCores() {
        assertTrue(pool.availableCores() == 4);
        List<Core> cores = pool.borrowCores(1, Collections.singleton(0));
        assertTrue(cores.size() == 1);
        assertTrue(pool.availableCores() == 3);
        pool.returnCore(cores.get(0));
        assertTrue(pool.availableCores() == 4);
    }

    @Test
    public void testPreferedCoresMixed() {
        assertTrue(pool.availableCores() == 4);
        List<Core> cores = pool.borrowCores(3, Collections.singleton(0));
        assertTrue(cores.size() == 3);
        assertTrue(cores.get(0).rank.num == 0);
        assertTrue(cores.get(1).rank.num == 0);
        assertTrue(cores.get(2).rank.num == 1);
        assertTrue(pool.availableCores() == 1);
        pool.returnCores(cores);
        assertTrue(pool.availableCores() == 4);
    }
}
