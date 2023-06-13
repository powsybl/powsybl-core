/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import java.io.IOException;

/**
 * @author Nicolas Pierre < nicolas.pierre@artelys.com >
 */
@FunctionalInterface
public interface AmplElementReader {
    void read(AmplNetworkReader reader) throws IOException;

}
