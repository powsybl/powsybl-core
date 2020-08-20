/**
 * Copyright (c) 2020, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.exceptions;

/**
 *
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class NetworkImportException extends RuntimeException {

    public NetworkImportException() {
    }

    public NetworkImportException(String msg) {
        super(msg);
    }

    public NetworkImportException(Throwable throwable) {
        super(throwable);
    }

    public NetworkImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
