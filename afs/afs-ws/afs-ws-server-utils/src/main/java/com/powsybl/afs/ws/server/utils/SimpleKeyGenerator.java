/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.server.utils;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * @author Ali Tahanout <ali.tahanout at rte-france.com>
 */
public class SimpleKeyGenerator  implements KeyGenerator {

    private static final String SIMPLE_KEY = "simplekey";

    @Override
    public Key generateKey() {
        return new SecretKeySpec(SIMPLE_KEY.getBytes(), 0, SIMPLE_KEY.getBytes().length, "DES");
    }
}
