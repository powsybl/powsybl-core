/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.exceptions;

import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UncheckedParserConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 3556294891839231296L;

    public UncheckedParserConfigurationException(ParserConfigurationException cause) {
        super(cause);
    }

    @Override
    public synchronized ParserConfigurationException getCause() {
        return (ParserConfigurationException) super.getCause();
    }
}
