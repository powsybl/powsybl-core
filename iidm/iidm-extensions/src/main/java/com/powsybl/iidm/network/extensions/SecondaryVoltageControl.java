/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface SecondaryVoltageControl extends Extension<Network> {

    String NAME = "secondaryVoltageControl";

    class PilotPoint {

        private final List<String> busbarSectionsOrBusesIds;

        private double targetV;

        public PilotPoint(List<String> busbarSectionsOrBusesIds, double targetV) {
            this.busbarSectionsOrBusesIds = Objects.requireNonNull(busbarSectionsOrBusesIds);
            if (busbarSectionsOrBusesIds.isEmpty()) {
                throw new PowsyblException("Empty busbar section of bus ID list");
            }
            if (Double.isNaN(targetV)) {
                throw new PowsyblException("Invalid target voltage");
            }
            this.targetV = targetV;
        }

        public List<String> getBusbarSectionsOrBusesIds() {
            return busbarSectionsOrBusesIds;
        }

        public double getTargetV() {
            return targetV;
        }

        public void setTargetV(double targetV) {
            this.targetV = targetV;
        }
    }

    class Zone {

        private final String name;

        private final PilotPoint pilotPoint;

        private final List<String> generatorsOrVscsIds;

        public Zone(String name, PilotPoint pilotPoint, List<String> generatorsOrVscsIds) {
            this.name = Objects.requireNonNull(name);
            this.pilotPoint = Objects.requireNonNull(pilotPoint);
            this.generatorsOrVscsIds = Objects.requireNonNull(generatorsOrVscsIds);
        }

        public String getName() {
            return name;
        }

        public PilotPoint getPilotPoint() {
            return pilotPoint;
        }

        public List<String> getGeneratorsOrVscsIds() {
            return generatorsOrVscsIds;
        }
    }

    List<Zone> getZones();

    @Override
    default String getName() {
        return NAME;
    }
}
