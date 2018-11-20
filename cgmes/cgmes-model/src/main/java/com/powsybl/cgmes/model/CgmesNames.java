/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

public final class CgmesNames {

    public static final String CIM_14_NAMESPACE = "http://iec.ch/TC57/2009/CIM-schema-cim14#";
    public static final String CIM_16_NAMESPACE = "http://iec.ch/TC57/2013/CIM-schema-cim16#";
    public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    public static final String SUBSTATION = "Substation";
    public static final String VOLTAGE_LEVEL = "VoltageLevel";
    public static final String TERMINAL = "Terminal";
    public static final String AC_LINE_SEGMENT = "ACLineSegment";

    public static final String TRANSFORMER_WINDING_RATED_U = "transformerWindingRatedU";
    public static final String TRANSFORMER_END = "TransformerEnd";
    public static final String TAP_CHANGER = "TapChanger";
    public static final String CONTINUOUS_POSITION = "continuousPosition";
    public static final String POSITION = "position";
    public static final String LOW_STEP = "lowStep";
    public static final String HIGH_STEP = "highStep";

    public static final String DC_TERMINAL = "DCTerminal";
    public static final String RATED_UDC = "ratedUdc";

    private CgmesNames() {
    }
}
