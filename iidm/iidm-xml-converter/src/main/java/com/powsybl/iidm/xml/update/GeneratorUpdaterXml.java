/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.update;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.IncrementalIidmFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */

public final class GeneratorUpdaterXml {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorUpdaterXml.class);

    private GeneratorUpdaterXml() { }

    public static void updateGeneratorControlValues(XMLStreamReader reader, Network network, IncrementalIidmFiles targetFile) {
        if (targetFile == IncrementalIidmFiles.CONTROL) {
            String id = reader.getAttributeValue(null, "id");
            boolean voltageRegulatorOn = XmlUtil.readOptionalBoolAttribute(reader, "voltageRegulatorOn", false);
            double targetP = XmlUtil.readOptionalDoubleAttribute(reader, "targetP");
            double targetQ = XmlUtil.readOptionalDoubleAttribute(reader, "targetQ");
            double targetV = XmlUtil.readOptionalDoubleAttribute(reader, "targetV");
            Generator generator = (Generator) network.getIdentifiable(id);
            if (generator == null) {
                LOGGER.warn("Generator {} not found", id);
                return;
            }
            generator.setTargetP(targetP).setTargetQ(targetQ).setTargetV(targetV).setVoltageRegulatorOn(voltageRegulatorOn);
        }
    }
}
