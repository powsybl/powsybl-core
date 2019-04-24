/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.update;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.xml.IncrementalIidmFiles;

import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */

public final class VscConverterStationUpdaterXml {
    public static void updateVscConverterStationControlValues(XMLStreamReader reader, Network network, IncrementalIidmFiles targetFile) {
        if (targetFile != IncrementalIidmFiles.CONTROL) {
            return;
        }
        String id = reader.getAttributeValue(null, "id");
        boolean voltageRegulatorOn = XmlUtil.readOptionalBoolAttribute(reader, "voltageRegulatorOn", false);
        double voltageSetpoint = XmlUtil.readOptionalDoubleAttribute(reader, "voltageSetpoint");
        double reactivePowerSetPoint = XmlUtil.readOptionalDoubleAttribute(reader, "reactivePowerSetpoint");
        VscConverterStation cs = (VscConverterStation) network.getIdentifiable(id);
        if (voltageRegulatorOn) {
            cs.setVoltageSetpoint(voltageSetpoint);
        } else {
            cs.setReactivePowerSetpoint(reactivePowerSetPoint);
        }
    }
}
