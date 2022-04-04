/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export.elements;

import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class LoadingLimitEq {

    public static void write(String id, Class<? extends LoadingLimits> loadingLimitClass, String name, double value, String operationalLimitTypeId, String operationalLimitSetId, String cimNamespace, String valueAttributeName, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, loadingLimitClassName(loadingLimitClass));
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(name);
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, loadingLimitClassName(loadingLimitClass) + "." + valueAttributeName);
        writer.writeCharacters(CgmesExportUtil.format(value));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "OperationalLimit.OperationalLimitSet");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + operationalLimitSetId);
        writer.writeEmptyElement(cimNamespace, "OperationalLimit.OperationalLimitType");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + operationalLimitTypeId);
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
