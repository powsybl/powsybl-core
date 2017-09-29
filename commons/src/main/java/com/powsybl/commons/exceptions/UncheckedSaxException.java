/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.exceptions;

import org.xml.sax.SAXException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UncheckedSaxException extends RuntimeException {

    private static final long serialVersionUID = 7704057374205330013L;

    public UncheckedSaxException(SAXException cause) {
        super(cause);
    }

    @Override
    public SAXException getCause() {
        return (SAXException) super.getCause();
    }
}
