/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import javax.xml.stream.XMLStreamWriter;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public interface XmlWriterContext {

    XMLStreamWriter getWriter();
}
