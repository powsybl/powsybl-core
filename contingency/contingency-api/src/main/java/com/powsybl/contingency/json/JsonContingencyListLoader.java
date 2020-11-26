/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.contingency.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.ContingencyList;
import com.powsybl.contingency.ContingencyListLoader;
import com.powsybl.contingency.DefaultContingencyList;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
@AutoService(ContingencyListLoader.class)
public class JsonContingencyListLoader implements ContingencyListLoader {

    @Override
    public String getFormat() {
        return "json";
    }

    @Override
    public ContingencyList load(String name, InputStream stream) throws IOException {
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        objectMapper.registerModule(new ContingencyJsonModule());

        return objectMapper.readValue(stream, DefaultContingencyList.class);
    }
}
