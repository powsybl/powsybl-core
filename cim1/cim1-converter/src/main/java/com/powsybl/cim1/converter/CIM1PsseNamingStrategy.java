/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cim1.converter;

import cim1.model.CIMModel;
import cim1.model.EnergyConsumer;
import cim1.model.IdentifiedObject;
import cim1.model.SynchronousMachine;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CIM1PsseNamingStrategy implements CIM1NamingStrategy {

    private static final Pattern POWER_TRANSFORMER_DESCRIPTION_PATTERN = Pattern.compile("(\\d*) (\\d*) (\\d*) '(\\d*) '");

    private final CIMModel model;

    private BiMap<String, String> idMapping = null;

    CIM1PsseNamingStrategy(CIMModel model) {
        this.model = Objects.requireNonNull(model);
    }

    private static void addName(Map<String, String> idMapping, Set<String> names, cim1.model.IdentifiedObject object) {
        StringBuilder name = new StringBuilder();
        if (object instanceof cim1.model.PowerTransformer) {
            // name field seems to always contains "1" while description seems to have a name constistent with
            // lines one. Just reshape it to look like lines
            Matcher matcher = POWER_TRANSFORMER_DESCRIPTION_PATTERN.matcher(object.getDescription());
            if (matcher.matches()) {
                name.append(matcher.group(1)).append("_").append(matcher.group(2)).append("_").append(matcher.group(3)).append("_").append(matcher.group(4));
            }
        } else {
            name.append(object.getName().trim());
            // generators and loads have the same naming and collides, just add to suffixe to distinguish generators
            // and loads
            if (object instanceof SynchronousMachine) {
                name.append("_GEN");
            } else if (object instanceof EnergyConsumer) {
                name.append("_LOAD");
            }
        }
        if (name.toString().isEmpty()) {
            throw new CIM1Exception(object.getId() + " is unnamed");
        }
        if (names.contains(name.toString())) {
            throw new CIM1Exception(object.getId() + " name is not unique: " + name);
        }
        names.add(name.toString());
        idMapping.put(object.getId(), name.toString());
    }

    private BiMap<String, String> getIdMapping() {
        if (idMapping == null) {
            idMapping = HashBiMap.create();
            // check names are uniques
            Set<String> names = new HashSet<>();
            for (cim1.model.SubGeographicalRegion r : model.getId_SubGeographicalRegion().values()) {
                addName(idMapping, names, r);
            }
            for (cim1.model.Substation s : model.getId_Substation().values()) {
                addName(idMapping, names, s);
            }
            for (cim1.model.VoltageLevel vl : model.getId_VoltageLevel().values()) {
                addName(idMapping, names, vl);
            }
            for (cim1.model.ACLineSegment l : model.getId_ACLineSegment().values()) {
                addName(idMapping, names, l);
            }
            for (cim1.model.PowerTransformer twt : model.getId_PowerTransformer().values()) {
                addName(idMapping, names, twt);
            }
            for (cim1.model.Switch s : model.getId_Switch().values()) {
                addName(idMapping, names, s);
            }
            for (cim1.model.SynchronousMachine sm : model.getId_SynchronousMachine().values()) {
                addName(idMapping, names, sm);
            }
            for (cim1.model.EnergyConsumer ec : model.getId_EnergyConsumer().values()) {
                addName(idMapping, names, ec);
            }
            for (cim1.model.ShuntCompensator sc : model.getId_ShuntCompensator().values()) {
                addName(idMapping, names, sc);
            }
            for (cim1.model.TopologicalNode tn : model.getId_TopologicalNode().values()) {
                addName(idMapping, names, tn);
            }
        }
        return idMapping;
    }

    @Override
    public String getId(IdentifiedObject object) {
        String id = getIdMapping().get(object.getId());
        if (id == null) {
            throw new CIM1Exception("Mapping not found for id " + object.getId());
        }
        return id;
    }

    @Override
    public String getName(IdentifiedObject object) {
        return object.getName();
    }

    @Override
    public String getCimId(String id) {
        String cimId = getIdMapping().inverse().get(id);
        if (cimId == null) {
            throw new CIM1Exception("Mapping not found for id " + id);
        }
        return cimId;
    }
}
