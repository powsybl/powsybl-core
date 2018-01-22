/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlWriterContext;

import javax.xml.stream.XMLStreamWriter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkXmlWriterContext extends XmlContext implements XmlWriterContext {

    private final XMLStreamWriter writer;
    private final XMLExportOptions options;
    private final BusFilter filter;

    NetworkXmlWriterContext(Anonymizer anonymizer, XMLStreamWriter writer, XMLExportOptions options, BusFilter filter) {
        super(anonymizer);
        this.writer = writer;
        this.options = options;
        this.filter = filter;
    }

    @Override
    public XMLStreamWriter getWriter() {
        return writer;
    }

    public XMLExportOptions getOptions() {
        return options;
    }

    public BusFilter getFilter() {
        return filter;
    }
}
