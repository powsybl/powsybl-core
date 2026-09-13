/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model;

import com.powsybl.commons.PowsyblException;

import java.io.Serial;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public class NcException extends PowsyblException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NcException(String message) {
        super(message);
    }

    public NcException(String message, Throwable cause) {
        super(message, cause);
    }
}
