/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class DcMapping {

    public DcMapping(Conversion.Context context) {
        this.context = context;
        terminals = new HashMap<>();
        converters = new HashMap<>();
        cgmesConverters = new HashMap<>();
    }

    public void initialize() {
        // TODO This is similar to what we do in the AbstractCgmesModel
        // with terminal data
        // Consider moving this part to AbstractCgmesModel
        // Initialize terminal data, for each terminal store the DC topological node
        context.cgmes().dcTerminals().forEach(t -> {
            DcTerminal td = new DcTerminal(
                    t.getId(CgmesNames.DC_TERMINAL),
                    t.getId("DCConductingEquipment"),
                    t.getLocal("dcConductingEquipmentType"),
                    t.asBoolean("connected", false));
            terminals.put(td.id(), td);
        });
        context.cgmes().dcTerminalsTP().forEach(t -> {
            DcTerminal td = terminals.get(t.getId("DCTerminal"));
            assert td != null;
            td.tp(t.getId("DCTopologicalNode"), t.getId(CgmesNames.SUBSTATION), context);
        });
    }

    public void map(PropertyBag ccgmes, HvdcConverterStation ciidm) {
        // Store we have found this converted HVDC Converter station at
        // the corresponding DCTopologicalNode for the given CGMES DCTerminal
        String terminalId = ccgmes.getId(CgmesNames.DC_TERMINAL);
        String dcTopologicalNode = terminals.get(terminalId).topologicalNode();
        addConverterAt(dcTopologicalNode, ciidm, ccgmes);
    }

    public HvdcConverterStation converterAt(String terminalId) {
        String dcTopologicalNode = terminals.get(terminalId).topologicalNode();
        return converters.get(dcTopologicalNode);
    }

    public PropertyBag cgmesConverterFor(HvdcConverterStation converter) {
        return cgmesConverters.get(converter);
    }

    public void addConverterAt(
            String dcTopologicalNode,
            HvdcConverterStation c,
            PropertyBag cc) {
        // Check there are only one converter at that node
        if (converters.containsKey(dcTopologicalNode)) {
            String reason = String.format(
                    "Unsupported modeling. More than one Converter at DCTopologicalNode %s: %s, %s",
                    dcTopologicalNode,
                    c.getId(),
                    converters.get(dcTopologicalNode).getId());
            context.invalid("HvdcConverterStation", reason);
            // TODO what to do? overwrite, preserve first, throw an exception ?
        }
        converters.put(dcTopologicalNode, c);
        // We also need to take note of some parameters of the converter
        // for building the HvdcLine (operatingMode, ratedUdc. ...).
        // We keep a reference to the original property bag for the CGMES converter.
        cgmesConverters.put(c, cc);
    }

    public static class DcTerminal {
        private final String id;
        private final String conductingEquipment;
        private final String conductingEquipmentType;
        private final boolean connected;
        private String topologicalNode;
        private String substation;

        public DcTerminal(
                String id,
                String conductingEquipment,
                String conductingEquipmentType,
                boolean connected) {
            this.id = id;
            this.conductingEquipment = conductingEquipment;
            this.conductingEquipmentType = conductingEquipmentType;
            this.connected = connected;
        }

        public void tp(String topologicalNode, String substation, Conversion.Context context) {
            checkAssign(topologicalNode, substation, context);
        }

        public String id() {
            return id;
        }

        public String conductingEquipment() {
            return conductingEquipment;
        }

        public String conductingEquipmentType() {
            return conductingEquipmentType;
        }

        public String topologicalNode() {
            return topologicalNode;
        }

        public String substation() {
            return substation;
        }

        public boolean connected() {
            return connected;
        }

        private void checkAssign(String topologicalNode, String substation,
                Conversion.Context context) {
            checkAssignAttr("topologicalNode", this.topologicalNode, topologicalNode, context);
            this.topologicalNode = topologicalNode;
            checkAssignAttr("substation", this.substation, substation, context);
            this.substation = substation;
        }

        private boolean checkAssignAttr(String attribute, String value0, String value1,
                Conversion.Context context) {
            if (value0 == null || value0.equals(value1)) {
                return true;
            } else {
                String reason = String.format("Reassign different values: %s, %s", value0, value1);
                context.invalid(attribute, reason);
                return false;
            }
        }
    }

    private final Conversion.Context context;
    private final Map<String, DcTerminal> terminals;
    private final Map<String, HvdcConverterStation> converters;
    private final Map<HvdcConverterStation, PropertyBag> cgmesConverters;
}
