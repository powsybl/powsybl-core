/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public enum PsseDataClass {
    BUS_DATA,
    LOAD_DATA,
    FIXED_BUS_SHUNT_DATA,
    GENERATOR_DATA,
    NON_TRANSFORMER_BRANCH_DATA,
    TRANSFORMER_DATA,
    AREA_INTERCHANGE_DATA,
    TWO_TERMINAL_DC_TRANSMISSION_LINE_DATA,
    VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_DATA,
    TRANSFORMER_IMPEDANCE_CORRECTION_TABLES_DATA,
    MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA,
    MULTI_SECTION_LINE_GROUPING_DATA,
    ZONE_DATA, INTERAREA_TRANSFER_DATA,
    OWNER_DATA,
    FACTS_DEVICE_DATA,
    SWITCHED_SHUNT_DATA,
    GNE_DEVICE_DATA,
    INDUCTION_MACHINE_DATA,
    SUBSTATION_DATA
}
