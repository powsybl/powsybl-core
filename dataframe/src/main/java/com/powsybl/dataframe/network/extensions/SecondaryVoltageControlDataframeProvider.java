/**
 * Copyright (c) 2021-2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dataframe.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.BaseDataframeMapperBuilder;
import com.powsybl.dataframe.network.ExtensionInformation;
import com.powsybl.dataframe.network.NetworkDataframeMapper;
import com.powsybl.dataframe.network.NetworkDataframeMapperBuilder;
import com.powsybl.dataframe.network.adders.NetworkElementAdder;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.ControlZone;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Hugo Kulesza <hugo.kulesza@rte-france.com>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class SecondaryVoltageControlDataframeProvider implements NetworkExtensionDataframeProvider {

    @Override
    public String getExtensionName() {
        return SecondaryVoltageControl.NAME;
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation(SecondaryVoltageControl.NAME,
            "Provides information about the secondary voltage control zones and units, in two distinct dataframes.",
            "[dataframe \"zones\"] index : name (str), target_v (float), bus_ids (str) / [dataframe \"units\"] index : unit_id (str), participate (bool), zone_name (str)");
    }

    @Override
    public List<String> getExtensionTableNames() {
        return List.of("zones", "units");
    }

    private Stream<ControlZone> zonesStream(Network network) {
        return network.getExtension(SecondaryVoltageControl.class)
            .getControlZones().stream();
    }

    private Stream<ControlUnitWithZone> unitsStream(Network network) {
        List<ControlUnitWithZone> units = new ArrayList<>();
        network.getExtension(SecondaryVoltageControl.class)
            .getControlZones()
            .forEach(zone -> units.addAll(zone.getControlUnits()
                .stream()
                .map(unit -> new ControlUnitWithZone(unit, zone.getName()))
                .toList()
            ));
        return units.stream();
    }

    private static class ControlZoneGetter implements BaseDataframeMapperBuilder.ItemGetter<Network, ControlZone> {

        @Override
        public ControlZone getItem(Network network, UpdatingDataframe updatingDataframe, int lineNumber) {
            SecondaryVoltageControl ext = network.getExtension(SecondaryVoltageControl.class);
            if (ext == null) {
                throw new PowsyblException("Network " + network.getId() + " has no SecondaryVoltageControl extension.");
            }
            String name = updatingDataframe.getStringValue("name", lineNumber).orElse(null);
            ControlZone zone = ext.getControlZones().stream()
                .filter(controlZone -> controlZone.getName().equals(name))
                .findAny()
                .orElse(null);
            if (zone == null) {
                throw new PowsyblException("No secondary voltage control zone named " + name + " found.");
            }
            return zone;
        }
    }

    private static class ControlUnitGetter
        implements BaseDataframeMapperBuilder.ItemGetter<Network, ControlUnitWithZone> {

        @Override
        public ControlUnitWithZone getItem(Network network, UpdatingDataframe updatingDataframe, int lineNumber) {
            SecondaryVoltageControl ext = network.getExtension(SecondaryVoltageControl.class);
            if (ext == null) {
                throw new PowsyblException("Network " + network.getId() + " has no SecondaryVoltageControl extension.");
            }
            String id = updatingDataframe.getStringValue("unit_id", lineNumber).orElse(null);
            ControlZone zone = ext.getControlZones().stream()
                .filter(controlZone -> controlZone.getControlUnits().stream()
                    .anyMatch(controlUnit -> controlUnit.getId().equals(id)))
                .findAny()
                .orElse(null);
            if (zone == null) {
                throw new PowsyblException(
                    "No secondary voltage control zone containing control unit " + id + " found.");
            }

            Optional<SecondaryVoltageControl.ControlUnit> optControlUnit = zone.getControlUnits()
                .stream()
                .filter(controlUnit -> controlUnit.getId().equals(id))
                .findAny();
            return new ControlUnitWithZone(
                optControlUnit.orElseThrow(() -> new PowsyblException("Cannot find ControlUnit with id :" + id)),
                zone.getName());
        }
    }

    @Override
    public Map<String, NetworkDataframeMapper> createMappers() {
        Map<String, NetworkDataframeMapper> mappers = new HashMap<>();
        mappers.put("zones",
            NetworkDataframeMapperBuilder.ofStream(this::zonesStream, new ControlZoneGetter())
                .stringsIndex("name", ControlZone::getName)
                .doubles("target_v", zone -> zone.getPilotPoint().getTargetV(),
                    (zone, v) -> zone.getPilotPoint().setTargetV(v))
                .strings("bus_ids", zone -> String.join(",", zone.getPilotPoint().getBusbarSectionsOrBusesIds()))
                .build()
        );
        mappers.put("units",
            NetworkDataframeMapperBuilder.ofStream(this::unitsStream, new ControlUnitGetter())
                .stringsIndex("unit_id", unit -> unit.getUnit().getId())
                .booleans("participate", unit -> unit.getUnit().isParticipate(),
                    (unit, b) -> unit.getUnit().setParticipate(b))
                .strings("zone_name", ControlUnitWithZone::getZoneName)
                .build()
        );
        return mappers;
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        network.removeExtension(SecondaryVoltageControl.class);
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new SecondaryVoltageControlDataframeAdder();
    }

    private static class ControlUnitWithZone {
        private final SecondaryVoltageControl.ControlUnit unit;
        private final String zoneName;

        public ControlUnitWithZone(SecondaryVoltageControl.ControlUnit unit, String zoneName) {
            this.unit = Objects.requireNonNull(unit);
            this.zoneName = Objects.requireNonNull(zoneName);
        }

        public SecondaryVoltageControl.ControlUnit getUnit() {
            return unit;
        }

        public String getZoneName() {
            return zoneName;
        }
    }
}
