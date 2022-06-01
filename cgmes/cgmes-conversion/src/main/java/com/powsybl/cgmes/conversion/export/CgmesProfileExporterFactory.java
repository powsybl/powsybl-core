/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.commons.PowsyblException;

import javax.xml.stream.XMLStreamWriter;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class CgmesProfileExporterFactory {

    private CgmesProfileExporterFactory() {
        // Empty constructor
    }

    public static AbstractCgmesExporter create(String profile, CgmesExportContext context, XMLStreamWriter xmlWriter) {
        if ("EQ".equals(profile)) {
            return new EquipmentExport(context, xmlWriter);
        } else if ("TP".equals(profile)) {
            return new TopologyExport(context, xmlWriter);
        } else if ("SSH".equals(profile)) {
            return new SteadyStateHypothesisExport(context, xmlWriter);
        } else if ("SV".equals(profile)) {
            return new StateVariablesExport(context, xmlWriter);
        }
        throw new PowsyblException("No CGMES profile writer for profile " + profile);
    }

}
