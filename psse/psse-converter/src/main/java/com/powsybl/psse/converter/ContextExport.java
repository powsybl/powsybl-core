/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.psse.model.pf.PsseSubstation;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
final class ContextExport {
    private final LinkExport linkExport;
    private final UpdateExport updateExport;

    ContextExport() {
        this.linkExport = new LinkExport();
        this.updateExport = new UpdateExport();
    }

    LinkExport getLinkExport() {
        return this.linkExport;
    }

    UpdateExport getUpdateExport() {
        return this.updateExport;
    }

    static class LinkExport {
        private final Map<Integer, Bus> busIToBusView;
        private final Map<String, Bus> voltageLevelNodeIdToBusView;

        LinkExport() {
            this.busIToBusView = new HashMap<>();
            this.voltageLevelNodeIdToBusView = new HashMap<>();
        }

        void addBusIBusViewLink(int busI, Bus busView) {
            this.busIToBusView.put(busI, busView);
        }

        Optional<Bus> getBusView(int busI) {
            return Optional.ofNullable(this.busIToBusView.get(busI));
        }

        void addNodeBusViewLink(VoltageLevel voltageLevel, int node, Bus busView) {
            this.voltageLevelNodeIdToBusView.put(AbstractConverter.getNodeId(voltageLevel, node), busView);
        }

        Optional<Bus> getBusView(VoltageLevel voltageLevel, int node) {
            return Optional.ofNullable(this.voltageLevelNodeIdToBusView.get(AbstractConverter.getNodeId(voltageLevel, node)));
        }
    }

    static class UpdateExport {
        private final Map<VoltageLevel, PsseSubstation> voltageLevelPsseSubstation;

        UpdateExport() {
            this.voltageLevelPsseSubstation = new HashMap<>();
        }

        void addVoltageLevelPsseSubstation(VoltageLevel voltageLevel, PsseSubstation psseSubstation) {
            voltageLevelPsseSubstation.put(voltageLevel, psseSubstation);
        }

        Optional<PsseSubstation> getPsseSubstation(VoltageLevel voltageLevel) {
            return Optional.ofNullable(voltageLevelPsseSubstation.get(voltageLevel));
        }
    }
}
