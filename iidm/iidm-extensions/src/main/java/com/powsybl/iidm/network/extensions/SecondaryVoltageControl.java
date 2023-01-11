/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface SecondaryVoltageControl extends Extension<Network> {

    String NAME = "secondaryVoltageControl";

    class PilotPoint {

        private final List<String> busbarSectionsOrBusesIds = new ArrayList<>();

        private double targetV;

        public PilotPoint(List<String> busbarSectionsOrBusesIds, double targetV) {
            Objects.requireNonNull(busbarSectionsOrBusesIds).forEach(Objects::requireNonNull);
            if (busbarSectionsOrBusesIds.isEmpty()) {
                throw new PowsyblException("Empty busbar section of bus ID list");
            }
            this.busbarSectionsOrBusesIds.addAll(busbarSectionsOrBusesIds);
            if (Double.isNaN(targetV)) {
                throw new PowsyblException("Invalid target voltage");
            }
            this.targetV = targetV;
        }

        /**
         * Get pilot point busbar section ID or bus ID of the bus/breaker view.
         */
        public List<String> getBusbarSectionsOrBusesIds() {
            return Collections.unmodifiableList(busbarSectionsOrBusesIds);
        }

        public double getTargetV() {
            return targetV;
        }

        public void setTargetV(double targetV) {
            this.targetV = targetV;
        }
    }

    class ControlUnit {

        private final String id;

        private boolean participate;

        public ControlUnit(String id) {
            this(id, true);
        }

        public ControlUnit(String id, boolean participate) {
            this.id = Objects.requireNonNull(id);
            this.participate = participate;
        }

        public String getId() {
            return id;
        }

        public boolean isParticipate() {
            return participate;
        }

        public void setParticipate(boolean participate) {
            this.participate = participate;
        }
    }

    class ControlZone {

        private final String name;

        private final PilotPoint pilotPoint;

        private final List<ControlUnit> controlUnits;

        public ControlZone(String name, PilotPoint pilotPoint, List<ControlUnit> controlUnits) {
            this.name = Objects.requireNonNull(name);
            this.pilotPoint = Objects.requireNonNull(pilotPoint);
            Objects.requireNonNull(controlUnits).forEach(Objects::requireNonNull);
            if (controlUnits.isEmpty()) {
                throw new PowsyblException("Empty control unit list");
            }
            this.controlUnits = controlUnits;
        }

        public String getName() {
            return name;
        }

        public PilotPoint getPilotPoint() {
            return pilotPoint;
        }

        public List<ControlUnit> getControlUnits() {
            return Collections.unmodifiableList(controlUnits);
        }
    }

    List<ControlZone> getControlZones();

    @Override
    default String getName() {
        return NAME;
    }
}
