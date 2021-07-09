/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.InjectionObservability;
import com.powsybl.iidm.network.extensions.InjectionObservabilityAdder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
@AutoService(ExtensionXmlSerializer.class)
public class InjectionObservabilityXmlSerializer<T extends Injection<T>> extends AbstractExtensionXmlSerializer<T, InjectionObservability<T>> {

    public InjectionObservabilityXmlSerializer() {
        super("injectionObservability", "network", InjectionObservability.class, false, "injectionObservability.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/injection_observability/1_0", "io");
    }

    @Override
    public void write(InjectionObservability injectionObservability, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("observable", Boolean.toString(injectionObservability.isObservable()));
    }

    @Override
    public InjectionObservability<T> read(T identifiable, XmlReaderContext context) {
        boolean observable = XmlUtil.readBoolAttribute(context.getReader(), "observable");
        identifiable.newExtension(InjectionObservabilityAdder.class)
                .withObservable(observable).add();
        return identifiable.getExtension(InjectionObservability.class);
    }
}
