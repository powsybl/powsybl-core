/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;

import static com.powsybl.cgmes.gl.GLTestUtils.*;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
abstract class AbstractCgmesGLTest {

    protected final String namespace = "http://network#";
    protected PropertyBags substationsPropertyBags;
    protected PropertyBags linesPropertyBags;

    @BeforeEach
    void setUp() {
        substationsPropertyBags = new PropertyBags(Arrays.asList(
                createSubstationPropertyBag(namespace + "Substation1", "Substation1", SUBSTATION_1.getLongitude(), SUBSTATION_1.getLatitude()),
                createSubstationPropertyBag(namespace + "Substation2", "Substation2", SUBSTATION_2.getLongitude(), SUBSTATION_2.getLatitude())));
        String lineName = "Line";
        linesPropertyBags = new PropertyBags(Arrays.asList(
                createLinePropertyBag(lineName, SUBSTATION_1.getLongitude(), SUBSTATION_1.getLatitude(), 1),
                createLinePropertyBag(lineName, LINE_1.getLongitude(), LINE_1.getLatitude(), 2),
                createLinePropertyBag(lineName, LINE_2.getLongitude(), LINE_2.getLatitude(), 3),
                createLinePropertyBag(lineName, SUBSTATION_2.getLongitude(), SUBSTATION_2.getLatitude(), 4)));
    }

    protected PropertyBag createSubstationPropertyBag(String powerSystemResource, String substationName, double x, double y) {
        PropertyBag propertyBag = new PropertyBag(Arrays.asList("powerSystemResource", "name", "crsName", "crsUrn", "x", "y"), true);
        propertyBag.put("powerSystemResource", powerSystemResource);
        propertyBag.put("name", substationName);
        propertyBag.put("crsUrn", CgmesGLUtils.COORDINATE_SYSTEM_URN);
        propertyBag.put("x", Double.toString(x));
        propertyBag.put("y", Double.toString(y));
        return propertyBag;
    }

    protected PropertyBag createLinePropertyBag(String lineName, double x, double y, int seq) {
        PropertyBag propertyBag = new PropertyBag(Arrays.asList("powerSystemResource", "name", "crsName", "crsUrn", "x", "y", "seq"), true);
        propertyBag.put("powerSystemResource", "http://network#Line");
        propertyBag.put("name", lineName);
        propertyBag.put("crsUrn", CgmesGLUtils.COORDINATE_SYSTEM_URN);
        propertyBag.put("x", Double.toString(x));
        propertyBag.put("y", Double.toString(y));
        propertyBag.put("seq", Integer.toString(seq));
        return propertyBag;
    }

}
