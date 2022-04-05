/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.iidm.network.ActivePowerLimits;
import com.powsybl.iidm.network.ApparentPowerLimits;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.LoadingLimits;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class LoadingLimitEq {

    public static void write(String id, Class<? extends LoadingLimits> loadingLimitClass, String name, double value, String operationalLimitTypeId, String operationalLimitSetId, String cimNamespace, String valueAttributeName, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(loadingLimitClassName(loadingLimitClass), id, name, cimNamespace, writer);
        writer.writeStartElement(cimNamespace, loadingLimitClassName(loadingLimitClass) + "." + valueAttributeName);
        writer.writeCharacters(CgmesExportUtil.format(value));
        writer.writeEndElement();
        CgmesExportUtil.writeReference("OperationalLimit.OperationalLimitSet", operationalLimitSetId, cimNamespace, writer);
        CgmesExportUtil.writeReference("OperationalLimit.OperationalLimitType", operationalLimitTypeId, cimNamespace, writer);
        writer.writeEndElement();
    }

    private static String loadingLimitClassName(Class<? extends LoadingLimits> loadingLimitClass) {
        if (CurrentLimits.class.equals(loadingLimitClass)) {
            return "CurrentLimit";
        } else if (ActivePowerLimits.class.equals(loadingLimitClass)) {
            return "ActivePowerLimit";
        } else if (ApparentPowerLimits.class.equals(loadingLimitClass)) {
            return "ApparentPowerLimit";
        }
        return "CurrentLimit";
    }

    private LoadingLimitEq() {
    }
}
