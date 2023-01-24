/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.action;

import com.powsybl.iidm.network.HvdcLine;

import java.util.OptionalDouble;

/**
 * an action to modify hvdc parameters
 * parameter enabled corresponds to the regulation mode true correspond to ac emulation, false to fix target
 * droop and p0 are parameters used for ac emulation only.
 * targetP is for fix target mode only.
 * ac emulation works only with vsc technology.
 *
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class HvdcAction extends AbstractAction {

    public static final String NAME = "HVDC";

    private final String hvdcId;
    private final boolean enabled;
    private final Double targetP;
    private final HvdcLine.ConvertersMode converterMode;
    private final Double droop;
    private final Double p0;

    public HvdcAction(String id, String hvdcId, Double targetP, HvdcLine.ConvertersMode converterMode) {
        this(id, hvdcId, false, targetP, converterMode, null, null);
    }

    public HvdcAction(String id, String hvdcId, HvdcLine.ConvertersMode converterMode, Double droop, Double p0) {
        this(id, hvdcId, true, null, converterMode, droop, p0);
    }

    public HvdcAction(String id, String hvdcId, boolean enabled, Double targetP, HvdcLine.ConvertersMode converterMode, Double droop, Double p0) {
        super(id);
        this.hvdcId = hvdcId;
        this.enabled = enabled;
        this.targetP = targetP;
        this.converterMode = converterMode;
        this.droop = droop;
        this.p0 = p0;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public String getHvdcId() {
        return hvdcId;
    }

    public HvdcLine.ConvertersMode getConverterMode() {
        return converterMode;
    }

    public OptionalDouble getDroop() {
        return droop == null ? OptionalDouble.empty() : OptionalDouble.of(droop);
    }

    public OptionalDouble getTargetP() {
        return targetP == null ? OptionalDouble.empty() : OptionalDouble.of(targetP);
    }

    public OptionalDouble getP0() {
        return p0 == null ? OptionalDouble.empty() : OptionalDouble.of(p0);
    }

    public boolean isEnabled() {
        return enabled;
    }
}
