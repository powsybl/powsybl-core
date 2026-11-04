/**
 * Copyright (c) 2026, Kick Stage d.o.o.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineNonReciprocalImpedance;
import com.powsybl.iidm.network.extensions.LineNonReciprocalImpedanceAdder;
import com.powsybl.iidm.serde.IidmVersion;

/**
 * @author Pascal Ehlert {@literal <pascal.ehlert at kickstage.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LineNonReciprocalImpedanceSerDe extends AbstractVersionableNetworkExtensionSerDe<Line, LineNonReciprocalImpedance, LineNonReciprocalImpedanceSerDe.Version> {

    public enum Version implements SerDeVersion<Version> {
        V_1_0("/xsd/lineNonReciprocalImpedance_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/line_non_reciprocal_impedance/1_0",
            new VersionNumbers(1, 0), IidmVersion.V_1_18, null);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "lnri", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, LineNonReciprocalImpedance.NAME);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public LineNonReciprocalImpedanceSerDe() {
        super(LineNonReciprocalImpedance.NAME, LineNonReciprocalImpedance.class, Version.values());
    }

    @Override
    public void write(LineNonReciprocalImpedance extension, SerializerContext context) {
        context.getWriter().writeDoubleAttribute("r21", extension.getR21());
        context.getWriter().writeDoubleAttribute("x21", extension.getX21());
    }

    @Override
    public LineNonReciprocalImpedance read(Line line, DeserializerContext context) {
        double r21 = context.getReader().readDoubleAttribute("r21");
        double x21 = context.getReader().readDoubleAttribute("x21");
        context.getReader().readEndNode();
        return line.newExtension(LineNonReciprocalImpedanceAdder.class)
                .withR21(r21)
                .withX21(x21)
                .add();
    }
}
