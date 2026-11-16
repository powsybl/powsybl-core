/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.exceptions;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
public class UncheckedClassCastExceptionException extends RuntimeException {

    private static final long serialVersionUID = 154490417885926671L;

    public UncheckedClassCastExceptionException(String message) {
        super(message);
    }

    public UncheckedClassCastExceptionException(ClassCastException cause) {
        super(cause);
    }

    @Override
    public synchronized ClassCastException getCause() {
        return (ClassCastException) super.getCause();
    }
}
