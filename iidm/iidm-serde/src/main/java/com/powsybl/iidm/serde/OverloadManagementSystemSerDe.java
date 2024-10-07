/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class OverloadManagementSystemSerDe extends AbstractComplexIdentifiableSerDe<OverloadManagementSystem, OverloadManagementSystemAdder, Substation> {

    static final OverloadManagementSystemSerDe INSTANCE = new OverloadManagementSystemSerDe();

    static final String ROOT_ELEMENT_NAME = "overloadManagementSystem";
    static final String ARRAY_ELEMENT_NAME = "overloadManagementSystems";

    private static final String BRANCH_TRIPPING_TAG = "branchTripping";
    private static final String SWITCH_TRIPPING_TAG = "switchTripping";
    private static final String THREE_WINDINGS_TRANSFORMER_TRIPPING_TAG = "threeWindingsTransformerTripping";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(OverloadManagementSystem oms, Substation substation, NetworkSerializerContext context) {
        context.getWriter().writeBooleanAttribute("enabled", oms.isEnabled());
        context.getWriter().writeStringAttribute("monitoredElementId", context.getAnonymizer().anonymizeString(oms.getMonitoredElementId()));
        context.getWriter().writeEnumAttribute("side", oms.getMonitoredSide());
    }

    @Override
    protected void writeSubElements(OverloadManagementSystem oms, Substation substation, NetworkSerializerContext context) {
        oms.getTrippings().forEach(t -> writeTripping(t, context));
    }

    private void writeTripping(OverloadManagementSystem.Tripping tripping, NetworkSerializerContext context) {
        switch (tripping.getType()) {
            case BRANCH_TRIPPING -> {
                OverloadManagementSystem.BranchTripping branchTripping = (OverloadManagementSystem.BranchTripping) tripping;
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), BRANCH_TRIPPING_TAG);
                writeTrippingCommonAttributes(tripping, context);
                context.getWriter().writeStringAttribute("branchId",
                        context.getAnonymizer().anonymizeString(branchTripping.getBranchToOperateId()));
                context.getWriter().writeEnumAttribute("side", branchTripping.getSideToOperate());
                context.getWriter().writeEndNode();
            }
            case SWITCH_TRIPPING -> {
                OverloadManagementSystem.SwitchTripping switchTripping = (OverloadManagementSystem.SwitchTripping) tripping;
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), SWITCH_TRIPPING_TAG);
                writeTrippingCommonAttributes(tripping, context);
                context.getWriter().writeStringAttribute("switchId",
                        context.getAnonymizer().anonymizeString(switchTripping.getSwitchToOperateId()));
                context.getWriter().writeEndNode();
            }
            case THREE_WINDINGS_TRANSFORMER_TRIPPING -> {
                OverloadManagementSystem.ThreeWindingsTransformerTripping twtTripping =
                        (OverloadManagementSystem.ThreeWindingsTransformerTripping) tripping;
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), THREE_WINDINGS_TRANSFORMER_TRIPPING_TAG);
                writeTrippingCommonAttributes(tripping, context);
                context.getWriter().writeStringAttribute("threeWindingsTransformerId",
                        context.getAnonymizer().anonymizeString(twtTripping.getThreeWindingsTransformerToOperateId()));
                context.getWriter().writeEnumAttribute("side", twtTripping.getSideToOperate());
                context.getWriter().writeEndNode();
            }
            default -> throw new PowsyblException("Unexpected tripping type: " + tripping.getType());
        }
    }

    private void writeTrippingCommonAttributes(OverloadManagementSystem.Tripping tripping, NetworkSerializerContext context) {
        context.getWriter().writeStringAttribute("key", tripping.getKey());
        String nameOrKey = tripping.getNameOrKey();
        if (nameOrKey != null && !nameOrKey.equals(tripping.getKey())) {
            context.getWriter().writeStringAttribute("name", nameOrKey);
        } else {
            context.getWriter().writeStringAttribute("name", null);
        }
        context.getWriter().writeDoubleAttribute("currentLimit", tripping.getCurrentLimit());
        context.getWriter().writeBooleanAttribute("openAction", tripping.isOpenAction());
    }

    @Override
    protected OverloadManagementSystemAdder createAdder(Substation s) {
        return s.newOverloadManagementSystem();
    }

    @Override
    protected void readRootElementAttributes(OverloadManagementSystemAdder adder,
                                             Substation parent,
                                             List<Consumer<OverloadManagementSystem>> toApply,
                                             NetworkDeserializerContext context) {
        boolean enabled = context.getReader().readBooleanAttribute("enabled", true);
        String monitoredElementId = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("monitoredElementId"));
        ThreeSides monitoredSide = context.getReader().readEnumAttribute("side", ThreeSides.class, ThreeSides.ONE);
        if (adder != null) {
            adder.setEnabled(enabled)
                    .validateAfterCreation()
                    .setMonitoredElementId(monitoredElementId)
                    .setMonitoredElementSide(monitoredSide);

            toApply.addAll(adder.getValidationChecks());
        }
    }

    @Override
    protected void readSubElements(String id, OverloadManagementSystemAdder adder,
                                   List<Consumer<OverloadManagementSystem>> toApply,
                                   NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            String key = context.getReader().readStringAttribute("key");
            String name = context.getReader().readStringAttribute("name");
            double currentLimit = context.getReader().readDoubleAttribute("currentLimit");
            boolean openAction = context.getReader().readBooleanAttribute("openAction");

            switch (elementName) {
                case BRANCH_TRIPPING_TAG -> readBranchTripping(adder, context, key, name, currentLimit, openAction, toApply);
                case SWITCH_TRIPPING_TAG -> readSwitchTripping(adder, context, key, name, currentLimit, openAction, toApply);
                case THREE_WINDINGS_TRANSFORMER_TRIPPING_TAG -> readThreeWindingsTransformerTripping(adder, context, key, name, currentLimit, openAction, toApply);
                default -> readSubElement(elementName, id, toApply, context);
            }
        });
    }

    private static void readBranchTripping(OverloadManagementSystemAdder adder, NetworkDeserializerContext context,
                                           String key, String name, double currentLimit, boolean openAction, List<Consumer<OverloadManagementSystem>> toApply) {
        String branchId = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("branchId"));
        TwoSides sideToOperate = context.getReader().readEnumAttribute("side", TwoSides.class, TwoSides.ONE);
        context.getReader().readEndNode();
        if (adder != null) {
            OverloadManagementSystemAdder.BranchTrippingAdder branchTrippingAdder = adder.newBranchTripping()
                    .setKey(key)
                    .setName(name)
                    .setCurrentLimit(currentLimit)
                    .setOpenAction(openAction)
                    .setBranchToOperateId(branchId)
                    .setSideToOperate(sideToOperate);
            branchTrippingAdder.add();

            toApply.addAll(branchTrippingAdder.getValidationChecks());
        }
    }

    private static void readSwitchTripping(OverloadManagementSystemAdder adder, NetworkDeserializerContext context,
                                           String key, String name, double currentLimit, boolean openAction, List<Consumer<OverloadManagementSystem>> toApply) {
        String switchId = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("switchId"));
        context.getReader().readEndNode();
        if (adder != null) {
            OverloadManagementSystemAdder.SwitchTrippingAdder switchTrippingAdder = adder.newSwitchTripping()
                    .setKey(key)
                    .setName(name)
                    .setCurrentLimit(currentLimit)
                    .setOpenAction(openAction)
                    .setSwitchToOperateId(switchId);
            switchTrippingAdder.add();

            toApply.addAll(switchTrippingAdder.getValidationChecks());
        }
    }

    private static void readThreeWindingsTransformerTripping(OverloadManagementSystemAdder adder, NetworkDeserializerContext context,
                                                             String key, String name, double currentLimit, boolean openAction, List<Consumer<OverloadManagementSystem>> toApply) {
        String twtId = context.getAnonymizer().deanonymizeString(
                context.getReader().readStringAttribute("threeWindingsTransformerId"));
        ThreeSides sideToOperate = context.getReader().readEnumAttribute("side", ThreeSides.class, ThreeSides.ONE);
        context.getReader().readEndNode();
        if (adder != null) {
            OverloadManagementSystemAdder.ThreeWindingsTransformerTrippingAdder threeWindingsTransformerTrippingAdder = adder.newThreeWindingsTransformerTripping()
                    .setKey(key)
                    .setName(name)
                    .setCurrentLimit(currentLimit)
                    .setOpenAction(openAction)
                    .setThreeWindingsTransformerToOperateId(twtId)
                    .setSideToOperate(sideToOperate);
            threeWindingsTransformerTrippingAdder.add();

            toApply.addAll(threeWindingsTransformerTrippingAdder.getValidationChecks());
        }
    }

    @Override
    protected boolean postponeValidation() {
        // OverloadManagementSystems may reference other elements which are not in the same substation (for instance lines).
        // In that case, there's no guarantee that the other elements were previously read when deserializing the network.
        // This could lead to errors at the OverloadManagementSystem's creation.
        // To avoid this, validation of exernal dependencies of the created overload management system is postponed.
        return true;
    }

    public final void skip(Substation s, NetworkDeserializerContext context) {
        List<Consumer<OverloadManagementSystem>> toApply = new ArrayList<>();
        String id = readIdentifierAttributes(null, context);
        readRootElementAttributes(null, s, toApply, context);
        readSubElements(id, null, toApply, context);
    }
}
