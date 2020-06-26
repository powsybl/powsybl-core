/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import java.util.List;

import com.google.common.collect.ImmutableList;
/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
import com.powsybl.commons.datastore.DataFormat;
import com.powsybl.commons.datastore.DataResolver;

public enum UcteDataFormat implements DataFormat {

    INSTANCE();

    private static final List<String> EXTENSIONS = ImmutableList.of("uct", "UCT");

    @Override
    public String getId() {
        return "UCTE";
    }

    @Override
    public String getDescription() {
        return "UCTE format";
    }

    @Override
    public DataResolver newDataResolver() {
        return new UcteDataResolver();
    }

    @Override
    public List<String> getExtensions() {
        return EXTENSIONS;
    }

}
