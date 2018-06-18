/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Collectors;

public final class IidmXmlConstants {

    public static final String INDENT = "    ";

    private static final List<Integer> VERSION_ARRAY = ImmutableList.of(1, 0);

    public static final String VERSION = VERSION_ARRAY.stream().map(Object::toString).collect(Collectors.joining("."));

    public static final String IIDM_BASE_URI = "http://www.itesla_project.eu/schema/iidm/";

    public static final String IIDM_PREFIX = "iidm";

    public static final String IIDM_URI = IIDM_BASE_URI + VERSION_ARRAY.stream().map(Object::toString).collect(Collectors.joining("_"));

    private IidmXmlConstants() {
    }
}
