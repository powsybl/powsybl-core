/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.events.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractNetworkEventTest {

    @Test
    void testNotif() {
        Network network = EurostagTutorialExample1Factory.create();
        NetworkEventRecorder eventRecorder = new NetworkEventRecorder();
        network.addListener(eventRecorder);

        var load2 = network.getVoltageLevel("VLLOAD").newLoad()
                .setId("LOAD2")
                .setBus("NLOAD")
                .setP0(0)
                .setQ0(0)
                .add();
        assertEquals(List.of(new CreationNetworkEvent("LOAD2")),
                eventRecorder.getEvents());

        eventRecorder.reset();
        load2.setP0(0.1);
        assertEquals(List.of(new UpdateNetworkEvent("LOAD2", "p0", "InitialState", 0.0, 0.1)),
                eventRecorder.getEvents());

        eventRecorder.reset();
        load2.remove();
        assertEquals(List.of(new RemovalNetworkEvent("LOAD2", false),
                             new RemovalNetworkEvent("LOAD2", true)),
                eventRecorder.getEvents());

        eventRecorder.reset();
        var gen = network.getGenerator("GEN");
        ActivePowerControl<?> apc = (ActivePowerControl<?>) gen.newExtension(ActivePowerControlAdder.class)
                .withDroop(1)
                .withParticipate(true)
                .add();
        assertEquals(List.of(new ExtensionCreationNetworkEvent("GEN", "activePowerControl")),
                eventRecorder.getEvents());

        eventRecorder.reset();
        apc.setParticipate(false);
        assertEquals(List.of(new ExtensionUpdateNetworkEvent("GEN", "activePowerControl", "participate", "InitialState", true, false)),
                eventRecorder.getEvents());

        eventRecorder.reset();
        gen.removeExtension(ActivePowerControl.class);
        assertEquals(List.of(new ExtensionRemovalNetworkEvent("GEN", "activePowerControl", false),
                             new ExtensionRemovalNetworkEvent("GEN", "activePowerControl", true)),
                eventRecorder.getEvents());

        eventRecorder.reset();
        gen.setProperty("p1", "v1");
        assertEquals(List.of(new PropertiesUpdateNetworkEvent("GEN", "properties[p1]", PropertiesUpdateNetworkEvent.PropertyUpdateType.ADDED, null, "v1")),
                eventRecorder.getEvents());

        eventRecorder.reset();
        gen.setProperty("p1", "v2");
        assertEquals(List.of(new PropertiesUpdateNetworkEvent("GEN", "properties[p1]", PropertiesUpdateNetworkEvent.PropertyUpdateType.REPLACED, "v1", "v2")),
                eventRecorder.getEvents());

        eventRecorder.reset();
        gen.removeProperty("p1");
        assertEquals(List.of(new PropertiesUpdateNetworkEvent("GEN", "properties[p1]", PropertiesUpdateNetworkEvent.PropertyUpdateType.REMOVED, "v2", null)),
                eventRecorder.getEvents());

        eventRecorder.reset();
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "new_variant");
        assertEquals(List.of(new VariantNetworkEvent("InitialState", "new_variant", VariantNetworkEvent.VariantEventType.CREATED)),
                eventRecorder.getEvents());

        eventRecorder.reset();
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "new_variant", true);
        assertEquals(List.of(new VariantNetworkEvent("InitialState", "new_variant", VariantNetworkEvent.VariantEventType.OVERWRITTEN)),
                eventRecorder.getEvents());

        eventRecorder.reset();
        network.getVariantManager().removeVariant("new_variant");
        assertEquals(List.of(new VariantNetworkEvent("new_variant", null, VariantNetworkEvent.VariantEventType.REMOVED)),
                eventRecorder.getEvents());
    }
}
