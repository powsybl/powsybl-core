/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import java.io.InputStream;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public interface LoadFlowDefaultParametersLoader {

    String getSourceName();

    InputStream loadDefaultParametersFromFile();
}
