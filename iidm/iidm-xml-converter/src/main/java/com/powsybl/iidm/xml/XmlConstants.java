/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface XmlConstants {

    String INDENT = "    ";

    List<Integer> VERSION_ARRAY = ImmutableList.of(1, 0);

    String VERSION = VERSION_ARRAY.stream().map(Object::toString).collect(Collectors.joining("."));

    String IIDM_BASE_URI = "http://www.itesla_project.eu/schema/iidm/";

    String IIDM_PREFIX = "iidm";

    String IIDM_URI = IIDM_BASE_URI + VERSION_ARRAY.stream().map(Object::toString).collect(Collectors.joining("_"));

}
