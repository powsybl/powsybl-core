/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import javax.xml.stream.XMLStreamWriter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class XmlWriterContext {

    private final XMLStreamWriter writer;
    private final XMLExportOptions options;
    private final BusFilter filter;

    XmlWriterContext(XMLStreamWriter writer, XMLExportOptions options, BusFilter filter) {
        this.writer = writer;
        this.options = options;
        this.filter = filter;
    }

    XMLStreamWriter getWriter() {
        return writer;
    }

    XMLExportOptions getOptions() {
        return options;
    }

    BusFilter getFilter() {
        return filter;
    }
}
