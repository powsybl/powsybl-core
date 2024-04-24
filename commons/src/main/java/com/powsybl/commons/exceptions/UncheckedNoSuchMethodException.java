/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.exceptions;

import java.io.Serial;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class UncheckedNoSuchMethodException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6199102529457589212L;

    public UncheckedNoSuchMethodException(NoSuchMethodException cause) {
        super(cause);
    }

    @Override
    public synchronized InstantiationException getCause() {
        return (InstantiationException) super.getCause();
    }
}
