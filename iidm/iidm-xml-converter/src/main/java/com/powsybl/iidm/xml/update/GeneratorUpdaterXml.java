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

import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */

public final class GeneratorUpdaterXml {

    private GeneratorUpdaterXml() { }

    public static void updateGeneratorControlValues(XMLStreamReader reader, Network network, IncrementalIidmFiles targetFile) {
        if (targetFile != IncrementalIidmFiles.CONTROL) {
            return;
        }
        String id = reader.getAttributeValue(null, "id");
        boolean voltageRegulatorOn = XmlUtil.readOptionalBoolAttribute(reader, "voltageRegulatorOn", false);
        double targetP = XmlUtil.readOptionalDoubleAttribute(reader, "targetP");
        double targetQ = XmlUtil.readOptionalDoubleAttribute(reader, "targetQ");
        double targetV = XmlUtil.readOptionalDoubleAttribute(reader, "targetV");
        Generator generator = (Generator) network.getIdentifiable(id);
        generator.setTargetP(targetP).setTargetQ(targetQ).setTargetV(targetV).setVoltageRegulatorOn(voltageRegulatorOn);
    }
}
