/**
 * Copyright (c) 2021-2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dataframe.network.extensions;

import com.powsybl.dataframe.network.NetworkDataframeMapper;

import java.util.*;

/**
 * @author Hugo Kulesza <hugo.kulesza@rte-france.com>
 */
public abstract class AbstractSingleDataframeNetworkExtension implements NetworkExtensionDataframeProvider {

    public List<String> getExtensionTableNames() {
        return Collections.emptyList();
    }

    public Map<String, NetworkDataframeMapper> createMappers() {
        HashMap<String, NetworkDataframeMapper> mapper = new HashMap<>();
        mapper.put(null, createMapper());
        return mapper;
    }

    public abstract NetworkDataframeMapper createMapper();

}
