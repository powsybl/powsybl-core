/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public final class CgmesNamingStrategyNames {
    public static final String TOPOLOGICAL_ISLAND_SUFFIX = "_TI";

    public static final String GEOGRAPHICAL_REGION_SUFFIX = "_GR";

    public static final String SUB_GEOGRAPHICAL_REGION_SUFFIX = "_SGR";

    public static final String AC_LINE_SEGMENT_SUFFIX = "_ACLS";

    public static final String BASE_VOLTAGE_SUFFIX = "_BV";

    public static final String TERMINAL_SUFFIX = "_T";

    public static final String BOUNDARY_TERMINAL_SUFFIX = "_BT";

    public static final String DCNODE_SUFFIX = "_DCNODE";

    public static final String ACDC_CONVERTER_DC_TERMINAL_SUFFIX = "_ACDCCDCT";

    public static final String GENERATING_UNIT_SUFFIX = "_GU";

    public static final String REGULATING_CONTROL_SUFFIX = "_RC";

    public static final String TRANSFORMER_END_SUFFIX = "_TE";

    public static final String PHASE_TAP_CHANGER_SUFFIX = "_PTC";

    public static final String RATIO_TAP_CHANGER_SUFFIX = "_RTC";

    public static final String EQUIVALENT_INJECTION_SUFFIX = "_EI";

    public static final String CONTROL_AREA_SUFFIX = "_CA";

    public static final String LOAD_AREA_SUFFIX = "_LA";

    public static final String SUB_LOAD_AREA_SUFFIX = "_SLA";

    public static final String LOAD_RESPONSE_CHARACTERISTICS_SUFFIX = "_LRC";

    public static final String SHUNT_COMPENSATOR_SUFFIX = "_SC";

    public static final String PHASE_TAP_CHANGER_TABLE_SUFFIX = "_PTCT";

    public static final String PHASE_TAP_CHANGER_STEP_SUFFIX = "_PTCS";

    public static final String RATIO_TAP_CHANGER_TABLE_SUFFIX = "_RTCT";

    public static final String RATIO_TAP_CHANGER_STEP_SUFFIX = "_RTCS";

    public static final String SUBSTATION_SUFFIX = "_S";

    public static final String VOLTAGE_LEVEL_SUFFIX = "_VL";

    public static final String OPERATIONAL_LIMIT_TYPE_SUFFIX = "_OLT";

    public static final String PATL_SUFFIX = "_PATL";

    public static final String TATL_SUFFIX = "_TATL";

    public static final String CONNECTIVITY_NODE_SUFFIX = "_CN";

    public static final String REACTIVE_CAPABILITY_CURVE_SUFFIX = "_SM_RCC";

    public static final String REACTIVE_CAPABIILITY_CURVE_POINT_SUFFIX = "_RCC_CP";

    public static final String DC_CONVERTER_UNIT_SUFFIX = "_DCCU";

    public static final String CONVERTER_STATION_SUFFIX = "_CS";

    public static final String TIE_FLOW_SUFFIX = "_TF";

    public static final String TOPOLOGICAL_NODE_SUFFIX = "_TN";

    public static final String LOAD_GROUP_SUFFIX = "_LG";

    public static final String PREFIX = "_";

    private CgmesNamingStrategyNames() {
    }
}
