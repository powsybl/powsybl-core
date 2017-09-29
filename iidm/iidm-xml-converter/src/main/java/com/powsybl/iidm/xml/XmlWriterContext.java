/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import javax.xml.stream.XMLStreamWriter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XmlWriterContext extends XmlContext {

    private final XMLStreamWriter writer;
    private final XMLExportOptions options;
    private final BusFilter filter;

    XmlWriterContext(Anonymizer anonymizer, XMLStreamWriter writer, XMLExportOptions options, BusFilter filter) {
        super(anonymizer);
        this.writer = writer;
        this.options = options;
        this.filter = filter;
    }

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
