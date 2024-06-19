/**
 * Copyright (c) 2014, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

/**
 *
 * Class that collects data about an overload of a permanent limit
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public record PermanentLimitCheckResult(boolean isOverload, double limitReductionValue) {
}
