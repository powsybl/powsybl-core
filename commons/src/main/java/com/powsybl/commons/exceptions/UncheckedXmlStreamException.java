/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.exceptions;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UncheckedXmlStreamException extends RuntimeException {

    private static final long serialVersionUID = 5629417731266724826L;

    public UncheckedXmlStreamException(XMLStreamException cause) {
        super(cause);
    }

    @Override
    public XMLStreamException getCause() {
        return (XMLStreamException) super.getCause();
    }
}
