/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.TreeDataWriter;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.InjectionObservability;
import com.powsybl.iidm.network.extensions.InjectionObservabilityAdder;
import com.powsybl.iidm.network.extensions.ObservabilityQuality;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
@AutoService(ExtensionXmlSerializer.class)
public class InjectionObservabilityXmlSerializer<T extends Injection<T>> extends AbstractExtensionXmlSerializer<T, InjectionObservability<T>> {

    private static final String QUALITY_P = "qualityP";
    private static final String QUALITY_Q = "qualityQ";
    private static final String QUALITY_V = "qualityV";

    private static final String STANDARD_DEVIATION = "standardDeviation";
    private static final String REDUNDANT = "redundant";

    public InjectionObservabilityXmlSerializer() {
        super("injectionObservability", "network", InjectionObservability.class, true, "injectionObservability.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/injection_observability/1_0", "io");
    }

    @Override
    public void write(InjectionObservability<T> injectionObservability, XmlWriterContext context) {
        context.getWriter().writeBooleanAttribute("observable", injectionObservability.isObservable(), false);
        writeOptionalQuality(QUALITY_P, injectionObservability.getQualityP(), context.getWriter());
        writeOptionalQuality(QUALITY_Q, injectionObservability.getQualityQ(), context.getWriter());
        writeOptionalQuality(QUALITY_V, injectionObservability.getQualityV(), context.getWriter());
    }

    private void writeOptionalQuality(String elementName, ObservabilityQuality<T> quality, TreeDataWriter writer) {
        if (quality != null) {
            writer.writeStartNode(getNamespaceUri(), elementName);
            writer.writeDoubleAttribute(STANDARD_DEVIATION, quality.getStandardDeviation());
            writer.writeBooleanAttribute(REDUNDANT, quality.isRedundant(), writer);
        }
    }

    @Override
    public InjectionObservability<T> read(T identifiable, XmlReaderContext context) {
        boolean observable = context.getReader().readBooleanAttribute("observable", false);

        InjectionObservabilityAdder<T> adder = identifiable.newExtension(InjectionObservabilityAdder.class)
                .withObservable(observable);

        context.getReader().readUntilEndNode(getExtensionName(), () -> {
            switch (context.getReader().getNodeName()) {
                case QUALITY_P: {
                    var standardDeviation = context.getReader().readDoubleAttribute(STANDARD_DEVIATION);
                    var redundant = context.getReader().readBooleanAttribute(REDUNDANT, false);
                    adder.withStandardDeviationP(standardDeviation)
                            .withRedundantP(redundant);
                    break;
                }
                case QUALITY_Q: {
                    var standardDeviation = context.getReader().readDoubleAttribute(STANDARD_DEVIATION);
                    var redundant = context.getReader().readBooleanAttribute(REDUNDANT, false);
                    adder.withStandardDeviationQ(standardDeviation)
                            .withRedundantQ(redundant);
                    break;
                }
                case QUALITY_V: {
                    var standardDeviation = context.getReader().readDoubleAttribute(STANDARD_DEVIATION);
                    var redundant = context.getReader().readBooleanAttribute(REDUNDANT, false);
                    adder.withStandardDeviationV(standardDeviation)
                            .withRedundantV(redundant);
                    break;
                }
                default: {
                    throw new PowsyblException("Unexpected element: " + context.getReader().getNodeName());
                }
            }
        });

        return adder.add();
    }
}
