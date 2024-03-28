/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ActivePowerLimits;
import com.powsybl.iidm.network.ApparentPowerLimits;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.LoadingLimits;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class LoadingLimitEq {

    public static void write(String id, LoadingLimits loadingLimits, String name, double value,
                             String operationalLimitTypeId, String operationalLimitSetId, String cimNamespace, String valueAttributeName, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cgmesClass = loadingLimitClassName(loadingLimits);
        CgmesExportUtil.writeStartIdName(cgmesClass, id, name, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, cgmesClass + "." + valueAttributeName);
        writer.writeCharacters(CgmesExportUtil.format(value));
        writer.writeEndElement();
        CgmesExportUtil.writeReference("OperationalLimit.OperationalLimitSet", operationalLimitSetId, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("OperationalLimit.OperationalLimitType", operationalLimitTypeId, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    public static String loadingLimitClassName(LoadingLimits loadingLimits) {
        if (loadingLimits instanceof CurrentLimits) {
            return "CurrentLimit";
        } else if (loadingLimits instanceof ActivePowerLimits) {
            return "ActivePowerLimit";
        } else if (loadingLimits instanceof ApparentPowerLimits) {
            return "ApparentPowerLimit";
        }
        throw new PowsyblException("Unsupported loading limits " + loadingLimits.getClass().getSimpleName());
    }

    private LoadingLimitEq() {
    }
}
