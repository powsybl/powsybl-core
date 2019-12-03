/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 */
@AutoService(ExtensionXmlSerializer.class)
public class ThreeWindingsTransformerPhaseAngleClockXmlSerializer
    implements ExtensionXmlSerializer<ThreeWindingsTransformer, ThreeWindingsTransformerPhaseAngleClock> {

    @Override
    public boolean hasSubElements() {
        return false;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/threeWindingsTransformerPhaseAngleClock.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.powsybl.org/schema/iidm/ext/three_windings_transformer_phase_angle_clock/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "threewtpac";
    }

    @Override
    public void write(ThreeWindingsTransformerPhaseAngleClock extension, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeOptionalInt("phaseAngleClockLeg2", extension.getPhaseAngleClockLeg2(), 0, context.getExtensionsWriter());
        XmlUtil.writeOptionalInt("phaseAngleClockLeg3", extension.getPhaseAngleClockLeg3(), 0, context.getExtensionsWriter());
    }

    @Override
    public ThreeWindingsTransformerPhaseAngleClock read(ThreeWindingsTransformer extendable, XmlReaderContext context) throws XMLStreamException {
        int phaseAngleClockLeg2 = XmlUtil.readOptionalIntegerAttribute(context.getReader(), "phaseAngleClockLeg2", 0);
        int phaseAngleClockLeg3 = XmlUtil.readOptionalIntegerAttribute(context.getReader(), "phaseAngleClockLeg3", 0);
        return new ThreeWindingsTransformerPhaseAngleClock(extendable, phaseAngleClockLeg2, phaseAngleClockLeg3);
    }

    @Override
    public String getExtensionName() {
        return "threeWindingsTransformerPhaseAngleClock";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super ThreeWindingsTransformerPhaseAngleClock> getExtensionClass() {
        return ThreeWindingsTransformerPhaseAngleClock.class;
    }
}
