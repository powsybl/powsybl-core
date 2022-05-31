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
public abstract class AbstractCgmesProfileWriter {
    protected final CgmesExportContext context;
    protected XMLStreamWriter xmlWriter;

    protected AbstractCgmesProfileWriter(CgmesExportContext context) {
        this.context = context;
    }

    public static AbstractCgmesProfileWriter create(String profile, CgmesExportContext context) {
        if ("EQ".equals(profile)) {
            return new EquipmentExport(context);
        } else if ("TP".equals(profile)) {
            return new TopologyExport(context);
        } else if ("SSH".equals(profile)) {
            return new SteadyStateHypothesisExport(context);
        } else if ("SV".equals(profile)) {
            return new StateVariablesExport(context);
        }
        throw new PowsyblException("No CGMES profile writer for profile " + profile);
    }

    public abstract String getProfile();

    public abstract void write();

    public void setXmlWriter(XMLStreamWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    public String getFileName(String baseName) {
        return baseName + "_" + getProfile() + ".xml";
    }
}
