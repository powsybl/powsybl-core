/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.network.adders.AbstractSimpleAdder;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;

import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class SlackTerminalDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("voltage_level_id"),
        SeriesMetadata.strings("element_id"),
        SeriesMetadata.strings("bus_id"));

    private static class SlackTerminalSeries {
        private final StringSeries voltageLevelId;
        private final StringSeries elementId;
        private final StringSeries busId;

        SlackTerminalSeries(UpdatingDataframe dataframe) {
            this.voltageLevelId = dataframe.getStrings("voltage_level_id");
            this.elementId = dataframe.getStrings("element_id");
            this.busId = dataframe.getStrings("bus_id");
        }

        void create(Network network, int row) {
            VoltageLevel voltageLevel = network.getVoltageLevel(this.voltageLevelId.get(row));
            if (voltageLevel == null) {
                throw new PowsyblException("voltage_level_id : " + this.voltageLevelId.get(row) +
                    " does not correspond to any voltage level");
            }
            if (this.elementId != null && this.busId == null) {
                Identifiable identifiable = network.getIdentifiable(this.elementId.get(row));
                if (identifiable instanceof Injection) {
                    Terminal terminal = ((Injection) identifiable).getTerminal();
                    SlackTerminal.reset(terminal.getVoltageLevel(), terminal);
                } else if (identifiable instanceof Bus) {
                    SlackTerminal.attach((Bus) identifiable);
                } else {
                    throw new PowsyblException("only injections or configured buses are handled as an \"element_id\"");
                }
            } else if (this.busId != null && this.elementId == null) {
                String busId = this.busId.get(row);
                Bus bus = network.getBusView().getBus(busId);
                if (bus == null) {
                    throw new PowsyblException("bus id : " + busId + " does not match any existing bus");
                }
                SlackTerminal.attach(bus);
            } else {
                throw new PowsyblException("only one of element_id or bus_id must be filled");
            }
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        SlackTerminalSeries series =
            new SlackTerminalSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }
}
