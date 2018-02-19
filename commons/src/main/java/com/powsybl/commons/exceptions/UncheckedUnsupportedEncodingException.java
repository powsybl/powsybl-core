/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.exceptions;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UncheckedUnsupportedEncodingException extends RuntimeException {

    private static final long serialVersionUID = -2355543773275682762L;

    public UncheckedUnsupportedEncodingException(UnsupportedEncodingException cause) {
        super(cause);
    }

    @Override
    public UnsupportedEncodingException getCause() {
        return (UnsupportedEncodingException) super.getCause();
    }
}
