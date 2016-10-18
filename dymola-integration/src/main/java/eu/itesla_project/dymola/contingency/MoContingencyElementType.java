/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola.contingency;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum MoContingencyElementType {
    MO_BUS_FAULT,
    MO_LINE_FAULT,
    MO_LINE_OPEN_REC,
    MO_BANK_MODIF,
    MO_LOAD_MODIF,
    MO_LINE_2_OPEN,
    MO_BREAKER,
    MO_SETPOINT_MODIF
}
