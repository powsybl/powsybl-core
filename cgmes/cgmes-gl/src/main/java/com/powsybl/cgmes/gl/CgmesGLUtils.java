/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.cgmes.model.CgmesNamespace;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public final class CgmesGLUtils {

    /**
     * Coordinate system URN for WGS84
     */
    public static final String COORDINATE_SYSTEM_URN = "urn:ogc:def:crs:EPSG::4326";

    public static final String CIM_16_GL_PROFILE = "http://entsoe.eu/CIM/GeographicalLocation/2/1";
    public static final String CIM_100_GL_PROFILE = "http://iec.ch/TC57/ns/CIM/GeographicalLocation-EU/3.0";

    private CgmesGLUtils() {
    }

    public static String glProfileUri(CgmesNamespace.Cim cim) {
        return cim.getVersion() >= 100 ? CIM_100_GL_PROFILE : CIM_16_GL_PROFILE;
    }

    public static boolean checkCoordinateSystem(String crsUrn) {
        return COORDINATE_SYSTEM_URN.equals(crsUrn);
    }

}
