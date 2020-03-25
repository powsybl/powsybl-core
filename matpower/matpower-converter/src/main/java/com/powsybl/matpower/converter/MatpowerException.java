/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.converter;

import com.powsybl.commons.PowsyblException;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MatpowerException extends PowsyblException {

    public MatpowerException(String msg) {
        super(msg);
    }

    public MatpowerException(Throwable throwable) {
        super(throwable);
    }
}
