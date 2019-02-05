/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.client.utils;

import javax.websocket.DeploymentException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UncheckedDeploymentException extends RuntimeException {

    private static final long serialVersionUID = 2352048425774058434L;

    public UncheckedDeploymentException(DeploymentException cause) {
        super(cause);
    }

    @Override
    public synchronized DeploymentException getCause() {
        return (DeploymentException) super.getCause();
    }
}
