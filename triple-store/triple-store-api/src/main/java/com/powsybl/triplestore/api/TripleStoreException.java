/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.api;

import com.powsybl.commons.PowsyblException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreException extends PowsyblException {

    public TripleStoreException(String message) {
        super(message);
    }

    public TripleStoreException(String message, Throwable t) {
        super(message, t);
    }
}
