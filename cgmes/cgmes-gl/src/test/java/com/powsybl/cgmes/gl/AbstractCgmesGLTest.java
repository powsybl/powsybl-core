/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.gl;

import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;

import static com.powsybl.cgmes.gl.GLTestUtils.*;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
abstract class AbstractCgmesGLTest {

    protected final String namespace = "http://network#";
    protected PropertyBags substationsPropertyBags;
    protected PropertyBags linesPropertyBags;

    @BeforeEach
    void setUp() {
        substationsPropertyBags = new PropertyBags(Arrays.asList(createSubstationPropertyBag(namespace + "Substation1", "Substation1", CgmesGLUtils.COORDINATE_SYSTEM_NAME,
                                                                                             CgmesGLUtils.COORDINATE_SYSTEM_URN, SUBSTATION_1.getLongitude(), SUBSTATION_1.getLatitude()),
                                                                 createSubstationPropertyBag(namespace + "Substation2", "Substation2", CgmesGLUtils.COORDINATE_SYSTEM_NAME,
                                                                                             CgmesGLUtils.COORDINATE_SYSTEM_URN, SUBSTATION_2.getLongitude(), SUBSTATION_2.getLatitude())));
        linesPropertyBags = new PropertyBags(Arrays.asList(createLinePropertyBag(namespace + "Line", "Line", CgmesGLUtils.COORDINATE_SYSTEM_NAME, CgmesGLUtils.COORDINATE_SYSTEM_URN,
                                                                                 SUBSTATION_1.getLongitude(), SUBSTATION_1.getLatitude(), 1),
                                                           createLinePropertyBag(namespace + "Line", "Line", CgmesGLUtils.COORDINATE_SYSTEM_NAME, CgmesGLUtils.COORDINATE_SYSTEM_URN,
                                                                                 LINE_1.getLongitude(), LINE_1.getLatitude(), 2),
                                                           createLinePropertyBag(namespace + "Line", "Line", CgmesGLUtils.COORDINATE_SYSTEM_NAME, CgmesGLUtils.COORDINATE_SYSTEM_URN,
                                                                                 LINE_2.getLongitude(), LINE_2.getLatitude(), 3),
                                                           createLinePropertyBag(namespace + "Line", "Line", CgmesGLUtils.COORDINATE_SYSTEM_NAME, CgmesGLUtils.COORDINATE_SYSTEM_URN,
                                                                                 SUBSTATION_2.getLongitude(), SUBSTATION_2.getLatitude(), 4)));
    }

    protected PropertyBag createSubstationPropertyBag(String powerSystemResource, String name, String crsName, String crsUrn, double x, double y) {
        PropertyBag propertyBag = new PropertyBag(Arrays.asList("powerSystemResource", "name", "crsName", "crsUrn", "x", "y"));
        propertyBag.put("powerSystemResource", powerSystemResource);
        propertyBag.put("name", name);
        propertyBag.put("crsName", crsName);
        propertyBag.put("crsUrn", crsUrn);
        propertyBag.put("x", Double.toString(x));
        propertyBag.put("y", Double.toString(y));
        return propertyBag;
    }

    protected PropertyBag createLinePropertyBag(String powerSystemResource, String name, String crsName, String crsUrn, double x, double y, int seq) {
        PropertyBag propertyBag = new PropertyBag(Arrays.asList("powerSystemResource", "name", "crsName", "crsUrn", "x", "y", "seq"));
        propertyBag.put("powerSystemResource", powerSystemResource);
        propertyBag.put("name", name);
        propertyBag.put("crsName", crsName);
        propertyBag.put("crsUrn", crsUrn);
        propertyBag.put("x", Double.toString(x));
        propertyBag.put("y", Double.toString(y));
        propertyBag.put("seq", Integer.toString(seq));
        return propertyBag;
    }

}
