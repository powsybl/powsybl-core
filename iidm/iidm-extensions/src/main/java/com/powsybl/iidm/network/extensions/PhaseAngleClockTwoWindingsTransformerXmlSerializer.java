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
import com.powsybl.iidm.network.TwoWindingsTransformer;

import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 */
@AutoService(ExtensionXmlSerializer.class)
public class PhaseAngleClockTwoWindingsTransformerXmlSerializer
    implements ExtensionXmlSerializer<TwoWindingsTransformer, PhaseAngleClockTwoWindingsTransformer> {

    @Override
    public boolean hasSubElements() {
        return false;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/phaseAngleClockTwoWindingsTransformer.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.powsybl.org/schema/iidm/ext/phase_angle_clock_two_windings_transformer/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "pac2wt";
    }

    @Override
    public void write(PhaseAngleClockTwoWindingsTransformer extension, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeOptionalInt("phaseAngleClock", extension.getPhaseAngleClock(), 0, context.getExtensionsWriter());
    }

    @Override
    public PhaseAngleClockTwoWindingsTransformer read(TwoWindingsTransformer extendable, XmlReaderContext context) throws XMLStreamException {
        int phaseAngleClock = XmlUtil.readOptionalIntegerAttribute(context.getReader(), "phaseAngleClock", 0);
        return new PhaseAngleClockTwoWindingsTransformer(extendable, phaseAngleClock);
    }

    @Override
    public String getExtensionName() {
        return "phaseAngleClockTwoWindingsTransformer";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super PhaseAngleClockTwoWindingsTransformer> getExtensionClass() {
        return PhaseAngleClockTwoWindingsTransformer.class;
    }
}
