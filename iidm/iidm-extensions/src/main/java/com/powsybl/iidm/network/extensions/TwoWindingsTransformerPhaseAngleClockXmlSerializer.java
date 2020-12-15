/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.TwoWindingsTransformer;

import javax.xml.stream.XMLStreamException;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 */
@AutoService(ExtensionXmlSerializer.class)
public class TwoWindingsTransformerPhaseAngleClockXmlSerializer
        extends AbstractExtensionXmlSerializer<TwoWindingsTransformer, TwoWindingsTransformerPhaseAngleClock> {

    public TwoWindingsTransformerPhaseAngleClockXmlSerializer() {
        super("twoWindingsTransformerPhaseAngleClock", "network", TwoWindingsTransformerPhaseAngleClock.class,
                false, "twoWindingsTransformerPhaseAngleClock.xsd",
                "http://www.powsybl.org/schema/iidm/ext/two_windings_transformer_phase_angle_clock/1_0", "twowtpac");
    }

    @Override
    public void write(TwoWindingsTransformerPhaseAngleClock extension, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeOptionalInt("phaseAngleClock", extension.getPhaseAngleClock(), 0, context.getWriter());
    }

    @Override
    public TwoWindingsTransformerPhaseAngleClock read(TwoWindingsTransformer extendable, XmlReaderContext context) {
        int phaseAngleClock = XmlUtil.readOptionalIntegerAttribute(context.getReader(), "phaseAngleClock", 0);
        extendable.newExtension(TwoWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClock(phaseAngleClock).add();
        return extendable.getExtension(TwoWindingsTransformerPhaseAngleClock.class);
    }
}
