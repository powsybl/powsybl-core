/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class UpDownScalableTest {
    private static final double EPSILON = 1e-3;

    @Test
    public void checkWorksAsExpectedWhenGoingUpGeneratorConvention() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        Scalable upScalable = Scalable.onGenerator("g2");
        Scalable downScalable = Scalable.onLoad("l1");
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable);
        upDownScalable.setInitialInjectionToNetworkValue(testNetwork);

        assertEquals(0., testNetwork.getGenerator("g2").getTargetP(), EPSILON);
        assertEquals(100., testNetwork.getLoad("l1").getP0(), EPSILON);
        upDownScalable.scale(testNetwork, 10., Scalable.ScalingConvention.GENERATOR);
        assertEquals(10., testNetwork.getGenerator("g2").getTargetP(), EPSILON);
        assertEquals(100., testNetwork.getLoad("l1").getP0(), EPSILON);
    }

    @Test
    public void checkWorksAsExpectedWhenGoingUpLoadConvention() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        testNetwork.getGenerator("g2").setTargetP(50.);
        Scalable upScalable = Scalable.onGenerator("g2");
        Scalable downScalable = Scalable.onLoad("l1");
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable);
        upDownScalable.setInitialInjectionToNetworkValue(testNetwork);

        assertEquals(50., testNetwork.getGenerator("g2").getTargetP(), EPSILON);
        assertEquals(100., testNetwork.getLoad("l1").getP0(), EPSILON);
        upDownScalable.scale(testNetwork, 10., Scalable.ScalingConvention.LOAD);
        assertEquals(40., testNetwork.getGenerator("g2").getTargetP(), EPSILON);
        assertEquals(100., testNetwork.getLoad("l1").getP0(), EPSILON);
    }

    @Test
    public void checkWorksAsExpectedWhenGoingDownGeneratorConvention() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        Scalable upScalable = Scalable.onGenerator("g2");
        Scalable downScalable = Scalable.onLoad("l1");
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable);
        upDownScalable.setInitialInjectionToNetworkValue(testNetwork);

        assertEquals(0., testNetwork.getGenerator("g2").getTargetP(), EPSILON);
        assertEquals(100., testNetwork.getLoad("l1").getP0(), EPSILON);
        upDownScalable.scale(testNetwork, -10., Scalable.ScalingConvention.GENERATOR);
        assertEquals(0., testNetwork.getGenerator("g2").getTargetP(), EPSILON);
        assertEquals(110., testNetwork.getLoad("l1").getP0(), EPSILON);
    }

    @Test
    public void checkWorksAsExpectedWhenGoingDownLoadConvention() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        Scalable upScalable = Scalable.onGenerator("g2");
        Scalable downScalable = Scalable.onLoad("l1");
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable);
        upDownScalable.setInitialInjectionToNetworkValue(testNetwork);

        assertEquals(0., testNetwork.getGenerator("g2").getTargetP(), EPSILON);
        assertEquals(100., testNetwork.getLoad("l1").getP0(), EPSILON);
        upDownScalable.scale(testNetwork, -10., Scalable.ScalingConvention.LOAD);
        assertEquals(0., testNetwork.getGenerator("g2").getTargetP(), EPSILON);
        assertEquals(90., testNetwork.getLoad("l1").getP0(), EPSILON);
    }

    @Test
    public void checkComputesCorrectlyMinMaxAndInitialValues() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        testNetwork.getLoad("l1").getTerminal().setP(-100.);
        Scalable upScalable = Scalable.onGenerator("g2");
        Scalable downScalable = Scalable.onLoad("l1", 50, 200, Scalable.ScalingConvention.LOAD);
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable);
        upDownScalable.setInitialInjectionToNetworkValue(testNetwork);

        assertEquals(-100., upDownScalable.getCurrentInjection(testNetwork, Scalable.ScalingConvention.GENERATOR), EPSILON);
        assertEquals(100., upDownScalable.getCurrentInjection(testNetwork, Scalable.ScalingConvention.LOAD), EPSILON);
        assertEquals(-200., upDownScalable.getMinimumInjection(testNetwork, Scalable.ScalingConvention.GENERATOR), EPSILON);
        assertEquals(0., upDownScalable.getMaximumInjection(testNetwork, Scalable.ScalingConvention.GENERATOR), EPSILON);
        assertEquals(50., upDownScalable.getMinimumInjection(testNetwork, Scalable.ScalingConvention.LOAD), EPSILON);
        assertEquals(100., upDownScalable.getMaximumInjection(testNetwork, Scalable.ScalingConvention.LOAD), EPSILON);
    }

    @Test
    public void checkInjectionFilteringWorksAsExpected() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        Scalable upScalable = Scalable.proportional(50, Scalable.onGenerator("g2"), 50, Scalable.onGenerator("unknown generator"));
        Scalable downScalable = Scalable.onLoad("l1", 50, 200, Scalable.ScalingConvention.LOAD);
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable);
        upDownScalable.setInitialInjectionToNetworkValue(testNetwork);

        List<Injection> foundInjections = new ArrayList<>();
        List<String> notFoundIds = new ArrayList<>();
        upDownScalable.filterInjections(testNetwork, foundInjections, notFoundIds);

        assertEquals(2, foundInjections.size());
        assertTrue(foundInjections.contains(testNetwork.getLoad("l1")));
        assertTrue(foundInjections.contains(testNetwork.getGenerator("g2")));
        assertEquals(1, notFoundIds.size());
        assertTrue(notFoundIds.contains("unknown generator"));
    }

    @Test
    public void testUpDownWithDeactivatedScalables() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        Scalable upScalable = Scalable.onGenerator("g2");
        Scalable downScalable = Scalable.onLoad("l1");
        UpDownScalable upDownScalable = Scalable.upDown(upScalable, downScalable);
        upDownScalable.setInitialInjectionToNetworkValue(testNetwork);

        double done = upDownScalable.scale(testNetwork, 50, Scalable.ScalingConvention.GENERATOR);
        assertEquals(50, done, EPSILON);
        assertEquals(0, upDownScalable.getMaximumInjection(testNetwork, Scalable.ScalingConvention.GENERATOR), EPSILON);
        assertEquals(50, upScalable.getCurrentInjection(testNetwork, Scalable.ScalingConvention.GENERATOR), EPSILON);

        upDownScalable.deactivateScalables(Set.of(upScalable));
        done = upDownScalable.scale(testNetwork, 30, Scalable.ScalingConvention.GENERATOR);
        assertEquals(0, done, EPSILON);
        assertEquals(-50, upDownScalable.getMaximumInjection(testNetwork, Scalable.ScalingConvention.GENERATOR), EPSILON);
    }

    @Test
    public void testUpDownScalableShallowCopy() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        Scalable upScalable = Scalable.onGenerator("g2");
        Scalable downScalable = Scalable.onLoad("l1");
        UpDownScalable upDownScalable = Scalable.upDown(upScalable, downScalable);
        upDownScalable.setInitialInjectionToNetworkValue(testNetwork);

        UpDownScalable shallowCopyInitial = (UpDownScalable) upDownScalable.shallowCopy();
        Collection<Scalable> activeScalables = shallowCopyInitial.getActiveScalables();
        assertEquals(2, activeScalables.size());
        assertTrue(activeScalables.contains(upScalable) && activeScalables.contains(downScalable));

        upDownScalable.deactivateScalables(Set.of(upScalable));

        activeScalables = shallowCopyInitial.getActiveScalables();
        assertEquals(2, activeScalables.size());
        assertTrue(activeScalables.contains(upScalable) && activeScalables.contains(downScalable));

        UpDownScalable shallowCopyDeactivated = (UpDownScalable) upDownScalable.shallowCopy();
        activeScalables = shallowCopyDeactivated.getActiveScalables();
        assertEquals(1, activeScalables.size());
        assertTrue(activeScalables.contains(downScalable));
    }
}
