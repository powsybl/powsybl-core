/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.Network.BusBreakerView;
import com.powsybl.iidm.network.Network.BusView;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusAdapterTest {
    private MergingView mergingView;
    private Bus bus;

    @Before
    public void setup() {
        mergingView = MergingView.create("BatteryAdapterTest", "iidm");
        mergingView.merge(EurostagTutorialExample1Factory.create());
        mergingView.merge(HvdcTestNetwork.createVsc());

        bus = mergingView.getBusBreakerView().getBus("NGEN");
        assertNotNull(bus);
        assertTrue(bus instanceof BusAdapter);
        assertSame(mergingView, bus.getNetwork());
    }

    @Test
    public void testSetterGetter() {
        // Not implemented yet !
        TestUtil.notImplemented(bus::getVoltageLevel);
        TestUtil.notImplemented(bus::getV);
        TestUtil.notImplemented(() -> bus.setV(0.0d));
        TestUtil.notImplemented(bus::getAngle);
        TestUtil.notImplemented(() -> bus.setAngle(0.0d));
        TestUtil.notImplemented(bus::getP);
        TestUtil.notImplemented(bus::getQ);
        TestUtil.notImplemented(bus::isInMainConnectedComponent);
        TestUtil.notImplemented(bus::isInMainSynchronousComponent);
        TestUtil.notImplemented(bus::getConnectedTerminalCount);
        TestUtil.notImplemented(bus::getTwoWindingsTransformers);
        TestUtil.notImplemented(bus::getTwoWindingsTransformerStream);
        TestUtil.notImplemented(() -> bus.visitConnectedEquipments(null));
        TestUtil.notImplemented(() -> bus.visitConnectedOrConnectableEquipments(null));
        TestUtil.notImplemented(bus::getLines);
        TestUtil.notImplemented(bus::getLineStream);
        TestUtil.notImplemented(bus::getThreeWindingsTransformers);
        TestUtil.notImplemented(bus::getThreeWindingsTransformerStream);
        TestUtil.notImplemented(bus::getGenerators);
        TestUtil.notImplemented(bus::getGeneratorStream);
        TestUtil.notImplemented(bus::getBatteries);
        TestUtil.notImplemented(bus::getBatteryStream);
        TestUtil.notImplemented(bus::getLoads);
        TestUtil.notImplemented(bus::getLoadStream);
        TestUtil.notImplemented(bus::getShuntCompensators);
        TestUtil.notImplemented(bus::getShuntCompensatorStream);
        TestUtil.notImplemented(bus::getDanglingLines);
        TestUtil.notImplemented(bus::getDanglingLineStream);
        TestUtil.notImplemented(bus::getStaticVarCompensators);
        TestUtil.notImplemented(bus::getStaticVarCompensatorStream);
        TestUtil.notImplemented(bus::getLccConverterStations);
        TestUtil.notImplemented(bus::getLccConverterStationStream);
        TestUtil.notImplemented(bus::getVscConverterStations);
        TestUtil.notImplemented(bus::getVscConverterStationStream);
    }

    @Test
    public void testComponentSetterGetter() {
        final Component syncComponent = bus.getSynchronousComponent();
        assertNotNull(syncComponent);
        assertTrue(syncComponent instanceof ComponentAdapter);

        // Not implemented yet !
        TestUtil.notImplemented(syncComponent::getNum);
        TestUtil.notImplemented(syncComponent::getSize);
        TestUtil.notImplemented(syncComponent::getBuses);
        TestUtil.notImplemented(syncComponent::getBusStream);

        final Component connectedComponent = bus.getConnectedComponent();
        assertNotNull(connectedComponent);
        assertTrue(connectedComponent instanceof ComponentAdapter);
    }

    @Test
    public void testBusViewSetterGetter() {
        final BusView busView = mergingView.getBusView();
        assertNotNull(busView);
        assertTrue(busView instanceof BusViewAdapter);

        // Not implemented yet !
        TestUtil.notImplemented(busView::getBuses);
        TestUtil.notImplemented(busView::getBusStream);
        TestUtil.notImplemented(busView::getConnectedComponents);
    }

    @Test
    public void testBusBreakerViewSetterGetter() {
        final BusBreakerView busBreakerView = mergingView.getBusBreakerView();
        assertNotNull(busBreakerView);
        assertTrue(busBreakerView instanceof BusBreakerViewAdapter);

        // Not implemented yet !
        TestUtil.notImplemented(busBreakerView::getBuses);
        TestUtil.notImplemented(busBreakerView::getBusStream);
        TestUtil.notImplemented(busBreakerView::getSwitches);
        TestUtil.notImplemented(busBreakerView::getSwitchStream);
        TestUtil.notImplemented(busBreakerView::getSwitchCount);
    }
}
