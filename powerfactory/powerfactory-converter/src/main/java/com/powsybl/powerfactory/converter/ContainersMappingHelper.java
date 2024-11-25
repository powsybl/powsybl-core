/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import com.google.common.primitives.Ints;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.DataObjectIndex;
import com.powsybl.powerfactory.model.DataObjectRef;
import com.powsybl.powerfactory.model.PowerFactoryException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
final class ContainersMappingHelper {

    private ContainersMappingHelper() {
    }

    private static final class Edge {

        private final DataObject obj1;

        private final DataObject obj2;

        private final boolean transformer;

        private final boolean zeroImpedance;

        private Edge(DataObject obj1, DataObject obj2, boolean transformer, boolean zeroImpedance) {
            this.obj1 = Objects.requireNonNull(obj1);
            this.obj2 = Objects.requireNonNull(obj2);
            this.transformer = transformer;
            this.zeroImpedance = zeroImpedance;
        }

        private DataObject getObj1() {
            return obj1;
        }

        private DataObject getObj2() {
            return obj2;
        }

        private boolean isTransformer() {
            return transformer;
        }

        private boolean isZeroImpedance() {
            return zeroImpedance;
        }
    }

    private static final class BusesToVoltageLevelId {

        private final DataObjectIndex index;

        private BusesToVoltageLevelId(DataObjectIndex index) {
            this.index = index;
        }

        private double getNominalVoltage(Integer id) {
            DataObject elmTerm = index.getDataObjectById(id).orElseThrow(() -> new PowerFactoryException("One ElemTerm was expected"));
            return NodeConverter.getNominalVoltage(elmTerm);
        }

        private String getVoltageLevelId(Set<Integer> ids) {
            String voltageLevelId = getPowerFactoryVoltageLevelId(ids);
            // automatic naming
            return Objects.requireNonNullElseGet(voltageLevelId, () -> "VL" + ids.stream().sorted().findFirst()
                .orElseThrow(() -> new PowerFactoryException("Unexpected empty ids set")));
        }

        private String getSubstationId(Set<Integer> ids) {
            String substationId = getPowerFactorySubstationId(ids);
            // automatic naming
            return Objects.requireNonNullElseGet(substationId, () -> "S" + ids.stream().sorted().findFirst()
                .orElseThrow(() -> new PowerFactoryException("Unexpected empty ids set")));
        }

        // Find an ElmSite with same ElmSubstats as defined by the ids argument
        private String getPowerFactorySubstationId(Set<Integer> ids) {

            Set<DataObject> elmTerms = elmTermsAssociatedTo(ids);
            Set<DataObject> elmSubstats = elmSubstatsAssociatedTo(elmTerms);
            Set<DataObject> elmSites = elmSitesAssociatedTo(elmSubstats);

            if (elmSites.size() != 1) {
                return null;
            }
            DataObject elmSite = elmSites.iterator().next();

            Set<DataObject> dataObjects = elmSite.getChildren().stream()
                .filter(dataObject -> dataObject.getDataClassName().equals(DataAttributeNames.ELMSUBSTAT))
                .collect(Collectors.toSet());

            return elmSubstats.equals(dataObjects) ? elmSite.getLocName() : null;
        }

        // Find an ElmSubstat with same ElmTerms (Nodes) as defined by the ids argument
        private String getPowerFactoryVoltageLevelId(Set<Integer> ids) {

            Set<DataObject> elmTerms = elmTermsAssociatedTo(ids);
            Set<DataObject> elmSubstats = elmSubstatsAssociatedTo(elmTerms);

            if (elmSubstats.size() != 1) {
                return null;
            }
            DataObject elmSubstat = elmSubstats.iterator().next();

            Set<DataObject> dataObjects = elmSubstat.getChildren().stream()
                .filter(dataObject -> dataObject.getDataClassName().equals(DataAttributeNames.ELMTERM))
                .collect(Collectors.toSet());

            return elmTerms.equals(dataObjects) ? elmSubstat.getLocName() : null;
        }

        private Set<DataObject> elmTermsAssociatedTo(Set<Integer> ids) {
            return ids.stream()
                .flatMap(id -> index.getDataObjectById(id).stream())
                .filter(dataObject -> dataObject.getDataClassName().equals(DataAttributeNames.ELMTERM))
                .collect(Collectors.toSet());
        }

        private Set<DataObject> elmSubstatsAssociatedTo(Set<DataObject> elmTerms) {
            return elmTerms.stream().map(DataObject::getParent)
                .filter(dataObject -> dataObject.getDataClassName().equals(DataAttributeNames.ELMSUBSTAT))
                .collect(Collectors.toSet());
        }

        private Set<DataObject> elmSitesAssociatedTo(Set<DataObject> elmSubstats) {
            return elmSubstats.stream().map(DataObject::getParent)
                .filter(dataObject -> dataObject.getDataClassName().equals(DataAttributeNames.ELMSITE))
                .collect(Collectors.toSet());
        }
    }

    private static boolean isConnectedElm(DataObject connectedObj) {
        if (connectedObj == null) {
            return false;
        }
        String dataClassName = connectedObj.getDataClassName();
        return dataClassName.equals("ElmTr2")
                || dataClassName.equals("ElmTr3")
                || dataClassName.equals("ElmLne")
                || dataClassName.equals("ElmCoup")
                || dataClassName.equals("ElmZpu")
                || dataClassName.equals("ElmVsc");
    }

    private static void createNodes(List<DataObject> elmTerms, List<DataObject> nodes,
        Map<DataObject, List<DataObject>> connectedElmByElmTermId) {
        for (DataObject elmTerm : elmTerms) {
            nodes.add(elmTerm);
            for (DataObject staCubic : elmTerm.getChildrenByClass("StaCubic")) {
                DataObject connectedObj = staCubic.findObjectAttributeValue(DataAttributeNames.OBJ_ID)
                        .flatMap(DataObjectRef::resolve)
                        .orElse(null);
                if (isConnectedElm(connectedObj)) {
                    connectedElmByElmTermId.computeIfAbsent(connectedObj, k -> new ArrayList<>()).add(elmTerm);
                }
            }
        }
    }

    // we do not have to consider the exact orientation of the element
    private static void createEdges(List<Edge> edges, Map<DataObject, List<DataObject>> connectedElmByElmTermId) {
        for (Map.Entry<DataObject, List<DataObject>> e : connectedElmByElmTermId.entrySet()) {
            DataObject connectedObj = e.getKey();
            List<DataObject> elmTerms = e.getValue();
            if (elmTerms.size() == 2) {
                switch (connectedObj.getDataClassName()) {
                    case "ElmTr2":
                        // All transformers are considered with impedance
                        edges.add(new Edge(elmTerms.get(0), elmTerms.get(1), true, false));
                        break;
                    case "ElmLne", "ElmZpu":
                        // All lines are considered with impedance, only zero impedance lines are necessary
                        break;
                    case "ElmCoup":
                        edges.add(new Edge(elmTerms.get(0), elmTerms.get(1), false, true));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected object class: " + connectedObj.getDataClassName());
                }
            } else if (elmTerms.size() == 3) {
                if (connectedObj.getDataClassName().equals("ElmTr3")) {
                    edges.add(new Edge(elmTerms.get(0), elmTerms.get(1), true, false));
                    edges.add(new Edge(elmTerms.get(0), elmTerms.get(2), true, false));
                } else if (connectedObj.getDataClassName().equals("ElmVsc")) {
                    edges.add(new Edge(elmTerms.get(0), elmTerms.get(1), false, true));
                    edges.add(new Edge(elmTerms.get(0), elmTerms.get(2), false, true));
                } else {
                    throw new IllegalStateException("Unexpected object class: " + connectedObj.getDataClassName());
                }
            }
        }
    }

    static ContainersMapping create(DataObjectIndex index, List<DataObject> elmTerms) {
        List<DataObject> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        Map<DataObject, List<DataObject>> connectedElmByElmTermId = new HashMap<>();

        createNodes(elmTerms, nodes, connectedElmByElmTermId);
        createEdges(edges, connectedElmByElmTermId);

        BusesToVoltageLevelId busesToVoltageLevelId = new BusesToVoltageLevelId(index);

        return ContainersMapping.create(nodes, edges,
            obj -> Ints.checkedCast(obj.getId()),
            edge -> Ints.checkedCast(edge.getObj1().getId()),
            edge -> Ints.checkedCast(edge.getObj2().getId()),
            Edge::isZeroImpedance,
            Edge::isTransformer,
            busesToVoltageLevelId::getNominalVoltage,
            busesToVoltageLevelId::getVoltageLevelId,
            busesToVoltageLevelId::getSubstationId);
    }
}
