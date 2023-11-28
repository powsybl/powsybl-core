/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.model.CgmesNames;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class LoadGroups {
    Map<String, LoadGroup> uniqueGroupByClass = new HashMap<>();

    Collection<LoadGroup> found() {
        return uniqueGroupByClass.values();
    }

    String groupFor(String loadClassName, CgmesExportContext context) {
        if (loadClassName.equals(CgmesNames.ENERGY_CONSUMER)
                || loadClassName.equals(CgmesNames.STATION_SUPPLY)) {
            return null;
        }
        LoadGroup loadGroup = uniqueGroupByClass.get(loadClassName);
        if (loadGroup == null) {
            loadGroup = createGroupFor(loadClassName, context);
        }
        return loadGroup.id;
    }

    LoadGroup createGroupFor(String loadClassName, CgmesExportContext context) {
        String id = CgmesExportUtil.getUniqueId(loadClassName + "LoadGroup", context.getUuidNamespace()); //TODO what to put here
        String className = GROUP_CLASS_NAMES.get(loadClassName);
        String groupName = GROUP_NAMES.get(loadClassName);
        return new LoadGroup(className, id, groupName);
    }

    static final Map<String, String> GROUP_CLASS_NAMES = Map.of(
            CgmesNames.CONFORM_LOAD, CgmesNames.CONFORM_LOAD_GROUP,
            CgmesNames.NONCONFORM_LOAD, CgmesNames.NONCONFORM_LOAD_GROUP);
    static final Map<String, String> GROUP_NAMES = Map.of(
            CgmesNames.CONFORM_LOAD, "Conform loads",
            CgmesNames.NONCONFORM_LOAD, "NonConform loads");
}
