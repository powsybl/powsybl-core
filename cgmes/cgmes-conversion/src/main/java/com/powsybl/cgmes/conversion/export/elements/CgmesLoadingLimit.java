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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesLoadingLimit {

    private static final String OPERATIONAL_LIMIT_OPERATIONAL_LIMIT_SET = OPERATIONAL_LIMIT + "." + OPERATIONAL_LIMIT_SET;
    private static final String OPERATIONAL_LIMIT_OPERATIONAL_LIMIT_TYPE = OPERATIONAL_LIMIT + "." + OPERATIONAL_LIMIT_TYPE;

    private CgmesLoadingLimit() {
    }

    public static void writeEq(LoadingLimitEq loadingLimit, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String namespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(loadingLimit.className, loadingLimit.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(namespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(loadingLimit.name), writer);
        CgmesExportUtil.writeAttribute(namespace, loadingLimit.className + "." + getLimitValueAttributeName(context), CgmesExportUtil.format(loadingLimit.value), writer);
        CgmesExportUtil.writeResource(OPERATIONAL_LIMIT_OPERATIONAL_LIMIT_SET, loadingLimit.operationalLimitSet, writer, context);
        CgmesExportUtil.writeResource(OPERATIONAL_LIMIT_OPERATIONAL_LIMIT_TYPE, loadingLimit.operationalLimitType, writer, context);
        CgmesExportUtil.writeEnd(writer);
    }

    private static String getLimitValueAttributeName(CgmesExportContext context) {
        if (context.getCimVersion() == 16) {
            return VALUE;
        } else {
            return NORMAL_VALUE;
        }
    }

    public record LoadingLimitEq(
        String className,
        String rdfId,
        String name,
        double value,
        String operationalLimitType,
        String operationalLimitSet
    ) {
    }

}
