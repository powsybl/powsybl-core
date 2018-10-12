/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class RegulatingControlConversion {

    private RegulatingControlConversion() {
    }

    public static class Data {
        public boolean on() {
            return on;
        }

        public double targetV() {
            return targetV;
        }

        public Terminal terminal() {
            return terminal;
        }

        private boolean  on;
        private double   targetV = Double.NaN;
        private Terminal terminal;
    }

    public static Data convert(PropertyBag p, VoltageLevel vl, Conversion.Context context) {
        Data control = new Data();

        boolean regulatingControl = p.containsKey("RegulatingControl");
        boolean regulatingControlEnabled = p.asBoolean("regulatingControlEnabled", true);
        if (regulatingControl) {
            String regulatingMode = p.get("regulatingControlMode");
            double regulatingTargetValue = p.asDouble("regulatingControlTargetValue");
            String regulatingTerminalId = p.getId("regulatingControlTerminal");
            // TODO A better way to identify regulating control modes
            if (regulatingMode != null && regulatingMode.toLowerCase().endsWith("voltage")) {
                control.targetV = regulatingTargetValue;
                if (control.targetV == 0 || Double.isNaN(control.targetV)) {
                    String reg = p.getId("RegulatingControl");
                    context.fixed(reg, "Invalid value for regulating target value",
                            control.targetV, vl.getNominalV());
                    control.targetV = vl.getNominalV();
                }

                if (regulatingControlEnabled) {
                    control.on = true;
                }
                // TODO How to find terminal in Network that corresponds
                // to the given regulating control terminal in CGMES model
                control.terminal = context.terminalMapping().find(regulatingTerminalId);
            }
        }

        return control;
    }
}
