/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.update;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.IncrementalIidmFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */

public final class TwoWindingsTransformerUpdaterXml {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwoWindingsTransformerUpdaterXml.class);

    private TwoWindingsTransformerUpdaterXml() { }

    public static void updateTwoWindingsTransformer(XMLStreamReader reader, Network network, TwoWindingsTransformer[] twt) {
        String id = reader.getAttributeValue(null, "id");
        twt[0] = network.getTwoWindingsTransformer(id);
        if (twt[0] == null) {
            LOGGER.warn("Two Windings Transformer '{}' not found", id);
        }
    }

    public static void updatePhaseTapChangerControlValues(XMLStreamReader reader, TwoWindingsTransformer[] twtTab, IncrementalIidmFiles targetFile) {
        if (targetFile == IncrementalIidmFiles.CONTROL) {
            String regulationMode = reader.getAttributeValue(null, "regulationMode");
            double regulatingValue = XmlUtil.readOptionalDoubleAttribute(reader, "regulationValue");
            boolean regulating = XmlUtil.readOptionalBoolAttribute(reader, "regulating", false);
            TwoWindingsTransformer twt = twtTab[0];
            if (twt ==  null) {
                LOGGER.warn("Two Windings Transformer not found");
                return;
            }
            PhaseTapChanger rpc = twt.getPhaseTapChanger();
            if (rpc == null) {
                LOGGER.warn("'{}' two winding transformer's phaseTapChanger not found", twt.getId());
                return;
            }
            rpc.setRegulationValue(regulatingValue).setRegulating(regulating).setRegulationMode(PhaseTapChanger.RegulationMode.valueOf(regulationMode));
        }
    }

    public static void updateRatioTapChangerControlValues(XMLStreamReader reader, TwoWindingsTransformer[] twtTab, IncrementalIidmFiles targetFile) {
        if (targetFile == IncrementalIidmFiles.CONTROL) {
            boolean regulating = XmlUtil.readOptionalBoolAttribute(reader, "regulating", false);
            double targetV = XmlUtil.readOptionalDoubleAttribute(reader, "targetV");
            double tapPosition = XmlUtil.readOptionalDoubleAttribute(reader, "tapPosition");
            TwoWindingsTransformer twt = twtTab[0];
            if (twt == null) {
                LOGGER.warn("Two Windings Transformer not found");
                return;
            }
            RatioTapChanger rtc = twt.getRatioTapChanger();
            if (rtc == null) {
                LOGGER.warn("'{}' two winding transformer's ratioTapChanger not found", twt.getId());
                return;
            }
            rtc.setTargetV(targetV).setRegulating(regulating).setTapPosition((int) tapPosition);
        }
    }
}
