/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StudyCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudyCase.class);

    private static final String ROOT_ID = "root_id";

    private final DataObject intCase;

    public StudyCase(DataObject intCase) {
        this.intCase = Objects.requireNonNull(intCase);
    }

    public String getName() {
        return intCase.getName();
    }

    public Instant getTime() {
        return intCase.findFirstChildByClass("SetTime")
                .map(setTime -> setTime.getInstantAttributeValue("datetime"))
                .orElseThrow(() -> new PowerFactoryException("SetTime class not found"));
    }

    public List<DataObject> getElmNets() {
        return intCase.findFirstChildByClass("ElmNet")
                .map(elmNet -> elmNet.getChildrenByClass("IntRef").stream()
                        .map(obj -> obj.getObjectAttributeValue("obj_id"))
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new PowerFactoryException("ElmNet class not found"));
    }

    public List<NetworkVariation> getNetworkVariations() {
        return intCase.findFirstChildByClass("IntAcscheme")
                .map(intAcscheme -> intAcscheme.getChildrenByClass("IntRef").stream()
                        .map(obj -> obj.getObjectAttributeValue("obj_id"))
                        .map(intScheme -> new NetworkVariation(intScheme, this))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private static boolean isNetworkObject(DataObject obj) {
        DataClass dataClass = obj.getDataClass();
        return dataClass.getName().startsWith("Elm") || dataClass.getName().startsWith("Sta");
    }

    private void findNetworkObjects(Map<Long, DataObject> networkObjectsById, int[] normalObjectCount, int[] hiddenObjectCount) {
        for (DataObject elmNet : getElmNets()) {
            elmNet.traverse(obj -> {
                if (isNetworkObject(obj)) {
                    SchemeStatus schemeStatus = obj.getSchemeStatus();
                    if (schemeStatus == SchemeStatus.NORMAL_OBJECT) {
                        networkObjectsById.put(obj.getId(), obj);
                        normalObjectCount[0]++;
                    } else if (schemeStatus == SchemeStatus.HIDDEN_OBJECT) {
                        hiddenObjectCount[0]++;
                    } else {
                        throw new PowerFactoryException("Unexpected scheme status: " + schemeStatus);
                    }
                }
            });
        }
    }

    private void applyNetworkVariations(Map<Long, DataObject> networkObjectsById, int[] addedObjectCount, int[] modifiedObjectCount, int[] deletedObjectCount) {
        for (NetworkVariation variation : getNetworkVariations()) {
            for (NetworkExpansionStage expansionStage : variation.getActiveExpansionStages()) {
                expansionStage.traverse(obj -> {
                    if (isNetworkObject(obj)) {
                        SchemeStatus schemeStatus = obj.getSchemeStatus();
                        if (schemeStatus == SchemeStatus.MODIFICATION_OBJECT) {
                            DataObject root = obj.getObjectAttributeValue(ROOT_ID);
                            root.copyAttributeValues(obj);
                            modifiedObjectCount[0]++;
                        } else if (schemeStatus == SchemeStatus.ADD_OBJECT) {
                            DataObject root = obj.getObjectAttributeValue(ROOT_ID);
                            root.copyAttributeValues(obj);
                            // add hidden object with new values from expansion nstage
                            networkObjectsById.put(root.getId(), root);
                            addedObjectCount[0]++;
                        } else {
                            throw new PowerFactoryException("Unexpected scheme status in a expansion stage: " + schemeStatus);
                        }
                    } else if (obj.getDataClassName().equals("IntSdel")) {
                        // delete root object
                        DataObject root = obj.getObjectAttributeValue(ROOT_ID);
                        networkObjectsById.remove(root.getId());
                        deletedObjectCount[0]++;
                    }
                });
            }
        }
    }

    public List<DataObject> applyNetworkExpansionStages() {
        Map<Long, DataObject> networkObjectsById = new HashMap<>();
        int[] normalObjectCount = new int[1];
        int[] hiddenObjectCount = new int[1];

        findNetworkObjects(networkObjectsById, normalObjectCount, hiddenObjectCount);

        int[] addedObjectCount = new int[1];
        int[] modifiedObjectCount = new int[1];
        int[] deletedObjectCount = new int[1];

        applyNetworkVariations(networkObjectsById, addedObjectCount, modifiedObjectCount, deletedObjectCount);

        LOGGER.info("Network objects summary: {} normal, {} hidden, {} added, {} modified, {} deleted",
                normalObjectCount[0], hiddenObjectCount[0], addedObjectCount[0], modifiedObjectCount[0], deletedObjectCount[0]);

        return new ArrayList<>(networkObjectsById.values());
    }
}
