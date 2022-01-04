/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class MultiVariantMergingViewTest {

    @Test
    public void singleThreadTest() {
        final MergingView mergingView = MergingView.create("SingleThreadTest", "iidm");
        mergingView.merge(EurostagTutorialExample1Factory.create());
        final VariantManager manager = mergingView.getVariantManager();
        manager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "SecondVariant");
        manager.setWorkingVariant("SecondVariant");
        final Generator generator = mergingView.getGenerator("GEN");
        generator.setVoltageRegulatorOn(false);
        assertFalse(generator.isVoltageRegulatorOn().orElse(true));
        manager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertTrue(generator.isVoltageRegulatorOn().orElse(false));

        assertEquals(2, manager.getVariantIds().size());
        manager.removeVariant("SecondVariant");
        assertEquals(1, manager.getVariantIds().size());

        final List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        manager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);
        assertEquals(variantsToAdd.size() + 1, manager.getVariantIds().size());
    }

    @Test
    public void multiThreadTest() throws InterruptedException {
        final MergingView mergingView = MergingView.create("MultiThreadTest", "iidm");
        mergingView.merge(EurostagTutorialExample1Factory.create());
        final VariantManager manager = mergingView.getVariantManager();
        manager.allowVariantMultiThreadAccess(true);
        assertTrue(manager.isVariantMultiThreadAccessAllowed());

        manager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "SecondVariant");

        final Generator generator = mergingView.getGenerator("GEN");

        manager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        generator.setVoltageRegulatorOn(true);

        manager.setWorkingVariant("SecondVariant");
        generator.setVoltageRegulatorOn(false);

        final boolean[] voltageRegulatorOnInitialVariant = new boolean[1];
        final boolean[] voltageRegulatorOnSecondVariant = new boolean[1];
        final CountDownLatch latch = new CountDownLatch(2); // to sync threads after having set the working variant
        ExecutorService service = Executors.newFixedThreadPool(2);
        service.invokeAll(Arrays.asList(
            () -> {
                manager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
                latch.countDown();
                latch.await();
                voltageRegulatorOnInitialVariant[0] = generator.isVoltageRegulatorOn().orElseThrow(com.powsybl.iidm.network.ValidationUtil::createUndefinedValueGetterException);
                return null;
            },
            () -> {
                manager.setWorkingVariant("SecondVariant");
                latch.countDown();
                latch.await();
                voltageRegulatorOnSecondVariant[0] = generator.isVoltageRegulatorOn().orElseThrow(com.powsybl.iidm.network.ValidationUtil::createUndefinedValueGetterException);
                return null;
            })
        );
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
        assertTrue(voltageRegulatorOnInitialVariant[0]);
        assertFalse(voltageRegulatorOnSecondVariant[0]);
    }

    @Test
    public void multiVariantTopologyTest() throws InterruptedException {
        final MergingView mergingView = MergingView.create("MultiVariantTopologyTest", "iidm");
        mergingView.merge(EurostagTutorialExample1Factory.create());
        final VariantManager manager = mergingView.getVariantManager();
        manager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "NEW_VARIANT");
        VoltageLevel vlload = mergingView.getVoltageLevel("VLLOAD");
        Bus nload = vlload.getBusBreakerView().getBus("NLOAD");
        Load newLoad = vlload.newLoad()
                .setId("NEW_LOAD")
                .setP0(10)
                .setQ0(10)
                .setBus("NLOAD")
                .setConnectableBus("NLOAD")
            .add();
        manager.setWorkingVariant("NEW_VARIANT");
        assertNotNull(newLoad.getTerminal().getBusBreakerView().getBus());
        assertEquals(2, Iterables.size(nload.getLoads()));
        newLoad.getTerminal().disconnect();
        assertNull(newLoad.getTerminal().getBusBreakerView().getBus());
        assertEquals(2, Iterables.size(vlload.getLoads()));
        assertEquals(1, Iterables.size(nload.getLoads()));
        manager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertNotNull(newLoad.getTerminal().getBusBreakerView().getBus());
        assertEquals(2, Iterables.size(vlload.getLoads()));
        assertEquals(2, Iterables.size(nload.getLoads()));
    }

    @Test
    public void variantNotSetTest() throws InterruptedException {
        final MergingView mergingView = MergingView.create("VariantNotSetTest", "iidm");
        mergingView.merge(EurostagTutorialExample1Factory.create());
        final VariantManager manager = mergingView.getVariantManager();
        manager.allowVariantMultiThreadAccess(true);
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, manager.getWorkingVariantId());
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                mergingView.getGenerator("GEN").getTargetP();
                fail();
            } catch (Exception ignored) {
            }
        });
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
    }

    @Test
    public void variantSetTest() throws InterruptedException {
        final MergingView mergingView = MergingView.create("VariantNotSetTest", "iidm");
        mergingView.merge(EurostagTutorialExample1Factory.create());
        final VariantManager manager = mergingView.getVariantManager();
        manager.allowVariantMultiThreadAccess(true);
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, manager.getWorkingVariantId());
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                manager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
                mergingView.getGenerator("GEN").getTargetP();
            } catch (Exception e) {
                fail();
            }
        });
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
    }
}
