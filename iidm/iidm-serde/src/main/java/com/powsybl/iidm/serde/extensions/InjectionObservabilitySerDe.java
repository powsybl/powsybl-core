/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.InjectionObservability;
import com.powsybl.iidm.network.extensions.InjectionObservabilityAdder;
import com.powsybl.iidm.network.extensions.ObservabilityQuality;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
@AutoService(ExtensionSerDe.class)
public class InjectionObservabilitySerDe<T extends Injection<T>> extends AbstractExtensionSerDe<T, InjectionObservability<T>> {

    private static final String QUALITY_P = "qualityP";
    private static final String QUALITY_Q = "qualityQ";
    private static final String QUALITY_V = "qualityV";

    private static final String STANDARD_DEVIATION = "standardDeviation";
    private static final String REDUNDANT = "redundant";

    public InjectionObservabilitySerDe() {
        super("injectionObservability", "network", InjectionObservability.class, "injectionObservability.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/injection_observability/1_0", "io");
    }

    @Override
    public void write(InjectionObservability<T> injectionObservability, SerializerContext context) {
        context.getWriter().writeBooleanAttribute("observable", injectionObservability.isObservable(), false);
        writeOptionalQuality(QUALITY_P, injectionObservability.getQualityP(), context.getWriter());
        writeOptionalQuality(QUALITY_Q, injectionObservability.getQualityQ(), context.getWriter());
        writeOptionalQuality(QUALITY_V, injectionObservability.getQualityV(), context.getWriter());
    }

    private void writeOptionalQuality(String elementName, ObservabilityQuality<T> quality, TreeDataWriter writer) {
        if (quality != null) {
            writer.writeStartNode(getNamespaceUri(), elementName);
            writer.writeDoubleAttribute(STANDARD_DEVIATION, quality.getStandardDeviation());
            writer.writeOptionalBooleanAttribute(REDUNDANT, quality.isRedundant().orElse(null));
            writer.writeEndNode();
        }
    }

    @Override
    public InjectionObservability<T> read(T identifiable, DeserializerContext context) {
        boolean observable = context.getReader().readBooleanAttribute("observable", false);

        InjectionObservabilityAdder<T> adder = identifiable.newExtension(InjectionObservabilityAdder.class)
                .withObservable(observable);

        context.getReader().readChildNodes(elementName -> {
            var standardDeviation = context.getReader().readDoubleAttribute(STANDARD_DEVIATION);
            boolean redundant = context.getReader().readOptionalBooleanAttribute(REDUNDANT).orElse(false);
            context.getReader().readEndNode();
            switch (elementName) {
                case QUALITY_P -> adder.withStandardDeviationP(standardDeviation).withRedundantP(redundant);
                case QUALITY_Q -> adder.withStandardDeviationQ(standardDeviation).withRedundantQ(redundant);
                case QUALITY_V -> adder.withStandardDeviationV(standardDeviation).withRedundantV(redundant);
                default -> throw new PowsyblException("Unknown element name '" + elementName + "' in 'injectionObservability'");
            }
        });

        return adder.add();
    }
}
