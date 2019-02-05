/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.exceptions;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UncheckedInstantiationException extends RuntimeException {

    private static final long serialVersionUID = -8312467463930833442L;

    public UncheckedInstantiationException(InstantiationException cause) {
        super(cause);
    }

    @Override
    public synchronized InstantiationException getCause() {
        return (InstantiationException) super.getCause();
    }
}
