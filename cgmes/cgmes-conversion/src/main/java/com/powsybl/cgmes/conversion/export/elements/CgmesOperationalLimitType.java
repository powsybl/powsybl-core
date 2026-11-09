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
public final class CgmesOperationalLimitType {

    private static final String OPERATIONAL_LIMIT_DIRECTION_KIND_ABSOLUTE_VALUE = "OperationalLimitDirectionKind.absoluteValue";
    private static final String OPERATIONAL_LIMIT_TYPE_DIRECTION = OPERATIONAL_LIMIT_TYPE + ".direction";
    private static final String OPERATIONAL_LIMIT_TYPE_ACCEPTABLE_DURATION = OPERATIONAL_LIMIT_TYPE + ".acceptableDuration";
    private static final String OPERATIONAL_LIMIT_TYPE_IS_INFINITE_DURATION = OPERATIONAL_LIMIT_TYPE + ".isInfiniteDuration";

    private CgmesOperationalLimitType() {
    }

    public static void writeEq(OperationalLimitTypeEq limitType, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId(OPERATIONAL_LIMIT_TYPE, limitType.rdfId, true, writer, context);
        CgmesExportUtil.writeAttribute(IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(limitType.name), writer, context);
        CgmesExportUtil.writeResource(OPERATIONAL_LIMIT_TYPE_DIRECTION, context.getCim().getNamespace(),
            context.getCim().getNamespace() + OPERATIONAL_LIMIT_DIRECTION_KIND_ABSOLUTE_VALUE, writer);
        CgmesExportUtil.writeResource(getLimitTypeAttributeName(context), context.getCim().getEuNamespace(),
            context.getCim().getEuNamespace() + getLimitKindClassName(context) + "." + limitType.kind, writer);
        if (context.getCimVersion() == 100) {
            CgmesExportUtil.writeAttribute(OPERATIONAL_LIMIT_TYPE_IS_INFINITE_DURATION, Boolean.toString(limitType.isInfiniteDuration), writer, context);
        }
        if (!limitType.isInfiniteDuration) {
            CgmesExportUtil.writeAttribute(OPERATIONAL_LIMIT_TYPE_ACCEPTABLE_DURATION, CgmesExportUtil.format(limitType.acceptableDuration), writer, context);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    private static String getLimitTypeAttributeName(CgmesExportContext context) {
        if (context.getCimVersion() == 16) {
            return OPERATIONAL_LIMIT_TYPE + ".limitType";
        } else {
            return OPERATIONAL_LIMIT_TYPE + ".kind";
        }
    }

    private static String getLimitKindClassName(CgmesExportContext context) {
        if (context.getCimVersion() == 16) {
            return "LimitTypeKind";
        } else {
            return "LimitKind";
        }
    }

    public record OperationalLimitTypeEq(
        String rdfId,
        String name,
        int acceptableDuration,
        boolean isInfiniteDuration,
        String kind
    ) {
    }

}
