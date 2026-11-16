/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class SshExportedIdentifiablesTest {

    private static final Pattern SSH_OBJECT_ID = Pattern.compile("<cim:\\w+ rdf:about=\"#_?([^\"]+)\">");

    private Network network;
    private NetworkEventRecorder recorder;

    @BeforeEach
    void setUp() {
        network = Network.read(CgmesConformity1Catalog.smallNodeBreaker().dataSource());
        recorder = new NetworkEventRecorder();
        network.addListener(recorder);
    }

    @Test
    void eventsGiveTheChangedIdentifiablesOnce() {
        Generator generator = network.getGenerators().iterator().next();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformerStream()
            .filter(TwoWindingsTransformer::hasRatioTapChanger)
            .findFirst()
            .orElseThrow();
        Load load = network.getLoads().iterator().next();

        generator.setTargetP(generator.getTargetP() + 1.0);
        RatioTapChanger rtc = transformer.getRatioTapChanger();
        rtc.setTapPosition(rtc.getTapPosition() == rtc.getHighTapPosition() ? rtc.getTapPosition() - 1 : rtc.getTapPosition() + 1);
        load.setP0(load.getP0() + 1.0);
        generator.setTargetP(generator.getTargetP() + 1.0);

        // A tap changer event has the id of its transformer
        assertEquals(List.of(generator, transformer, load), List.copyOf(SshExportedIdentifiables.fromEvents(network, recorder.getEvents())));
    }

    @Test
    void extensionEventsGiveTheirIdentifiable() {
        Generator generator = network.getGenerators().iterator().next();
        generator.removeExtension(ActivePowerControl.class);
        recorder.reset();

        generator.newExtension(ActivePowerControlAdder.class).withParticipate(true).withDroop(4.0).add();

        assertEquals(List.of(generator), List.copyOf(SshExportedIdentifiables.fromEvents(network, recorder.getEvents())));
    }

    @Test
    void changesOfOtherVariantsAreIgnored() {
        String initialVariant = network.getVariantManager().getWorkingVariantId();
        network.getVariantManager().cloneVariant(initialVariant, "other");
        network.getVariantManager().setWorkingVariant("other");
        Load load = network.getLoads().iterator().next();
        load.setP0(load.getP0() + 1.0);
        network.getVariantManager().setWorkingVariant(initialVariant);

        assertTrue(SshExportedIdentifiables.fromEvents(network, recorder.getEvents()).isEmpty());

        network.getVariantManager().setWorkingVariant("other");
        assertEquals(List.of(load), List.copyOf(SshExportedIdentifiables.fromEvents(network, recorder.getEvents())));
    }

    @Test
    void partialSshExportOfTheRecordedChanges() throws XMLStreamException {
        Load load = network.getLoads().iterator().next();
        load.setP0(load.getP0() + 1.0);

        CgmesExportContext context = new CgmesExportContext(network)
            .setPartialSshExport(true)
            .setSshExportedIdentifiables(SshExportedIdentifiables.fromEvents(network, recorder.getEvents()));
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", stringWriter);
        SteadyStateHypothesisExport.write(network, writer, context);

        // The load and its terminal
        List<String> ids = new ArrayList<>();
        Matcher m = SSH_OBJECT_ID.matcher(stringWriter.toString());
        while (m.find()) {
            ids.add(m.group(1));
        }
        assertEquals(List.of(load.getId(), CgmesExportUtil.getTerminalId(load.getTerminal(), context)), ids);
    }
}
