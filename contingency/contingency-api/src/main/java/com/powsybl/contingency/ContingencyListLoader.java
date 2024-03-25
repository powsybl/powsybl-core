/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.contingency;

import com.powsybl.contingency.contingency.list.ContingencyList;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public interface ContingencyListLoader {

    String getFormat();

    ContingencyList load(String name, InputStream stream) throws IOException;
}
