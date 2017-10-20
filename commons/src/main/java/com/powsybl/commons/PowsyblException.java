/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PowsyblException extends RuntimeException {

    public PowsyblException() {
    }

    public PowsyblException(String msg) {
        super(msg);
    }

    public PowsyblException(Throwable throwable) {
        super(throwable);
    }

    public PowsyblException(String message, Throwable cause) {
        super(message, cause);
    }
}
