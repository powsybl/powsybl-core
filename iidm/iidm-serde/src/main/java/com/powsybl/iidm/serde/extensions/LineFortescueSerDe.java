/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.extensions.LineFortescue;
import com.powsybl.iidm.network.extensions.LineFortescueAdder;
import com.powsybl.iidm.serde.IidmVersion;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class LineFortescueSerDe extends AbstractVersionableNetworkExtensionSerDe<Line, LineFortescue, LineFortescueSerDe.Version> {

    public enum Version implements SerDeVersion<Version> {
        V_1_0("/xsd/lineFortescue_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/line_fortescue/1_0",
            new VersionNumbers(1, 0), IidmVersion.V_1_0, IidmVersion.V_1_15),
        V_1_1("/xsd/lineFortescue_V1_1.xsd", "http://www.powsybl.org/schema/iidm/ext/line_fortescue/1_1",
            new VersionNumbers(1, 1), IidmVersion.V_1_0, null);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "lf", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, LineFortescue.NAME);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public LineFortescueSerDe() {
        super("lineFortescue", LineFortescue.class, Version.values());
    }

    @Override
    public void write(LineFortescue lineFortescue, SerializerContext context) {
        context.getWriter().writeDoubleAttribute("rz", lineFortescue.getRz(), Double.NaN);
        context.getWriter().writeDoubleAttribute("xz", lineFortescue.getXz(), Double.NaN);
        Version extVersion = getExtensionVersionToExport(context);
        if (extVersion.isGreaterThan(Version.V_1_0)) {
            context.getWriter().writeDoubleAttribute("g1z", lineFortescue.getG1z(), Double.NaN);
            context.getWriter().writeDoubleAttribute("g2z", lineFortescue.getG2z(), Double.NaN);
            context.getWriter().writeDoubleAttribute("b1z", lineFortescue.getB1z(), Double.NaN);
            context.getWriter().writeDoubleAttribute("b2z", lineFortescue.getB2z(), Double.NaN);
        }
        context.getWriter().writeBooleanAttribute("openPhaseA", lineFortescue.isOpenPhaseA(), false);
        context.getWriter().writeBooleanAttribute("openPhaseB", lineFortescue.isOpenPhaseB(), false);
        context.getWriter().writeBooleanAttribute("openPhaseC", lineFortescue.isOpenPhaseC(), false);
    }

    @Override
    public LineFortescue read(Line line, DeserializerContext context) {
        double rz = context.getReader().readDoubleAttribute("rz");
        double xz = context.getReader().readDoubleAttribute("xz");
        double g1z = Double.NaN;
        double g2z = Double.NaN;
        double b1z = Double.NaN;
        double b2z = Double.NaN;
        Version extVersion = getExtensionVersionImported(context);
        if (extVersion.isGreaterThan(Version.V_1_0)) {
            g1z = context.getReader().readDoubleAttribute("g1z");
            g2z = context.getReader().readDoubleAttribute("g2z");
            b1z = context.getReader().readDoubleAttribute("b1z");
            b2z = context.getReader().readDoubleAttribute("b2z");
        }
        boolean openPhaseA = context.getReader().readBooleanAttribute("openPhaseA", false);
        boolean openPhaseB = context.getReader().readBooleanAttribute("openPhaseB", false);
        boolean openPhaseC = context.getReader().readBooleanAttribute("openPhaseC", false);
        context.getReader().readEndNode();
        return line.newExtension(LineFortescueAdder.class)
                .withRz(rz)
                .withXz(xz)
                .withG1z(g1z)
                .withG2z(g2z)
                .withB1z(b1z)
                .withB2z(b2z)
                .withOpenPhaseA(openPhaseA)
                .withOpenPhaseB(openPhaseB)
                .withOpenPhaseC(openPhaseC)
                .add();
    }
}
