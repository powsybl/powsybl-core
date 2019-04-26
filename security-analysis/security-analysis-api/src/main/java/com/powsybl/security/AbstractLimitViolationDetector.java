/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

/**
 * Provides implementations for aggregation methods of {@link LimitViolationDetector}.
 * Actual implementations will only have to focus on detecting violations element-wise.
 *
 * @deprecated Moved to package {@link com.powsybl.security.detectors}.
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
@Deprecated
public abstract class AbstractLimitViolationDetector extends com.powsybl.security.detectors.AbstractLimitViolationDetector {

}
