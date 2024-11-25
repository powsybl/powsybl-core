/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class OperationalLimitConversion extends AbstractIdentifiedObjectConversion {

    private static final String LIMIT_TYPE = "limitType";
    private static final String OPERATIONAL_LIMIT = "Operational limit";
    private static final String OPERATIONAL_LIMIT_TYPE_NAME = "operationalLimitTypeName";
    private static final String OPERATIONAL_LIMIT_SUBCLASS = "OperationalLimitSubclass";
    private static final String OPERATIONAL_LIMIT_SET_ID = "OperationalLimitSet";
    private static final String OPERATIONAL_LIMIT_SET_NAME = "OperationalLimitSetName";
    private static final String PROPERTY_PREFIX = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.OPERATIONAL_LIMIT_SET;
    private static final String PERMANENT_LIMIT = "Permanent Limit";
    private static final String TEMPORARY_LIMIT = "Temporary Limit";

    public OperationalLimitConversion(PropertyBag l, Context context) {
        super(CgmesNames.OPERATIONAL_LIMIT, l, context);
        String limitSubclass = p.getLocal(OPERATIONAL_LIMIT_SUBCLASS);
        String limitSetId = p.getId(OPERATIONAL_LIMIT_SET_ID);
        String limitSetName = p.getLocal(OPERATIONAL_LIMIT_SET_NAME);
        limitSetName = limitSetName != null ? limitSetName : limitSetId;
        // Limit can associated to a Terminal or to an Equipment
        terminalId = l.getId("Terminal");
        equipmentId = l.getId("Equipment");
        Terminal terminal = null;
        if (limitSubclass == null || limitSubclass.equals(CgmesNames.ACTIVE_POWER_LIMIT) || limitSubclass.equals(CgmesNames.APPARENT_POWER_LIMIT) || limitSubclass.equals(CgmesNames.CURRENT_LIMIT)) {
            if (limitSubclass == null) {
                // Support for CIM14, all limits are assumed to be current
                limitSubclass = CgmesNames.CURRENT_LIMIT;
            }
            if (terminalId != null) {
                terminal = context.terminalMapping().findForFlowLimits(terminalId);
            }
            if (terminal != null) {
                checkAndCreateLimitsAdder(context.terminalMapping().number(terminalId), limitSubclass, limitSetId, limitSetName, terminal.getConnectable());
            } else if (equipmentId != null) {
                // The equipment may be a Branch, a Dangling line, a Switch ...
                Identifiable<?> identifiable = context.network().getIdentifiable(equipmentId);
                checkAndCreateLimitsAdder(-1, limitSubclass, limitSetId, limitSetName, identifiable);
            }
        } else if (limitSubclass.equals("VoltageLimit")) {
            if (terminalId != null) {
                terminal = context.terminalMapping().findForVoltageLimits(terminalId);
            }
            setVoltageLevelForVoltageLimit(terminal);
        } else {
            notAssigned();
        }
    }

    private void setVoltageLevelForVoltageLimit(Terminal terminal) {
        if (terminal != null) {
            vl = terminal.getVoltageLevel();
        } else if (equipmentId != null) {
            Identifiable<?> i = context.network().getIdentifiable(equipmentId);
            if (i == null) {
                // Happens in BusBranch when the voltage limit is linked to a busbarSection
                vl = context.network().getVoltageLevel(p.getId("EquipmentContainer"));
            } else if (i instanceof Injection<?> injection) {
                vl = injection.getTerminal().getVoltageLevel();
            }
        }
    }

    /**
     * Store the CGMES OperationalLimitSet id/name pair in a property of the identifiable.
     * If the property already exists, meaning it has been created for another limit set of that identifiable,
     * then append the id/name pair to the property value (which actually represents a serialized json).
     * @param identifiable The Branch, DanglingLine, ThreeWindingsTransformer where the limit set id/name are stored.
     * @param limitSetId The OperationalLimitSet id to store.
     * @param limitSetName The OperationalLimitSet name to store.
     */
    private void storeCgmesId(Identifiable<?> identifiable, String limitSetId, String limitSetName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node;
            if (identifiable.hasProperty(PROPERTY_PREFIX)) {
                node = mapper.readTree(identifiable.getProperty(PROPERTY_PREFIX));
            } else {
                node = mapper.createObjectNode();
            }
            ((ObjectNode) node).put(limitSetId, limitSetName);
            identifiable.setProperty(PROPERTY_PREFIX, mapper.writeValueAsString(node));
        } catch (JsonProcessingException e) {
            throw new PowsyblException(e.getMessage(), e);
        }
    }

    /**
     * Create the LoadingLimitsAdder for the given branch + side and the given limit set + subclass.
     * @param terminalNumber The side of the branch to which the OperationalLimit applies.
     * @param limitSubClass The subclass of the OperationalLimit.
     * @param limitSetId The id of the set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param b The branch to which the OperationalLimit applies.
     */
    private void createLimitsAdder(int terminalNumber, String limitSubClass, String limitSetId, String limitSetName, Branch<?> b) {
        if (terminalNumber == 1) {
            OperationalLimitsGroup limitsGroup = b.getOperationalLimitsGroup1(limitSetId).orElseGet(() -> {
                storeCgmesId(b, limitSetId, limitSetName);
                return b.newOperationalLimitsGroup1(limitSetId); });
            loadingLimitsAdder1 = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubClass);
        } else if (terminalNumber == 2) {
            OperationalLimitsGroup limitsGroup = b.getOperationalLimitsGroup2(limitSetId).orElseGet(() -> {
                storeCgmesId(b, limitSetId, limitSetName);
                return b.newOperationalLimitsGroup2(limitSetId); });
            loadingLimitsAdder2 = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubClass);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Create the LoadingLimitsAdder for the given dangling line and the given limit set + subclass.
     * @param limitSubClass The subclass of the OperationalLimit.
     * @param limitSetId The set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param dl The branch to which the OperationalLimit applies.
     */
    private void createLimitsAdder(String limitSubClass, String limitSetId, String limitSetName, DanglingLine dl) {
        OperationalLimitsGroup limitsGroup = dl.getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
            storeCgmesId(dl, limitSetId, limitSetName);
            return dl.newOperationalLimitsGroup(limitSetId); });
        loadingLimitsAdder = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubClass);
    }

    /**
     * Create the LoadingLimitsAdder for the given 3w-transformer + side and the given limit set + subclass.
     * @param terminalNumber The side of the transformer to which the OperationalLimit applies.
     * @param limitSubClass The subclass of the OperationalLimit.
     * @param limitSetId The set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param twt The 3w-transformer to which the OperationalLimit applies.
     */
    private void createLimitsAdder(int terminalNumber, String limitSubClass, String limitSetId, String limitSetName, ThreeWindingsTransformer twt) {
        if (terminalNumber == 1) {
            OperationalLimitsGroup limitsGroup = twt.getLeg1().getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
                storeCgmesId(twt, limitSetId, limitSetName);
                return twt.getLeg1().newOperationalLimitsGroup(limitSetId); });
            loadingLimitsAdder = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubClass);
        } else if (terminalNumber == 2) {
            OperationalLimitsGroup limitsGroup = twt.getLeg2().getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
                storeCgmesId(twt, limitSetId, limitSetName);
                return twt.getLeg2().newOperationalLimitsGroup(limitSetId); });
            loadingLimitsAdder = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubClass);
        } else if (terminalNumber == 3) {
            OperationalLimitsGroup limitsGroup = twt.getLeg3().getOperationalLimitsGroup(limitSetId).orElseGet(() -> {
                storeCgmesId(twt, limitSetId, limitSetName);
                return twt.getLeg3().newOperationalLimitsGroup(limitSetId); });
            loadingLimitsAdder = context.loadingLimitsMapping().getLoadingLimitsAdder(limitsGroup, limitSubClass);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Create LoadingLimitsAdder(s) as per the given inputs.
     * If the inputs are inconsistent, no limit adder is created.
     * @param terminalNumber The side of the equipment to which the OperationalLimit applies.
     * @param limitSubClass The subclass of the OperationalLimit.
     * @param limitSetId The set containing the OperationalLimit.
     * @param limitSetName The name of the set containing the OperationalLimit.
     * @param identifiable The equipment to which the OperationalLimit applies.
     */
    private void checkAndCreateLimitsAdder(int terminalNumber, String limitSubClass, String limitSetId, String limitSetName, Identifiable<?> identifiable) {
        if (identifiable instanceof Line) {
            Branch<?> b = (Branch<?>) identifiable;
            if (terminalNumber == -1) {
                // Limits appliy to the whole equipment == to both sides
                createLimitsAdder(1, limitSubClass, limitSetId, limitSetName, b);
                createLimitsAdder(2, limitSubClass, limitSetId, limitSetName, b);
            } else if (terminalNumber == 1 || terminalNumber == 2) {
                createLimitsAdder(terminalNumber, limitSubClass, limitSetId, limitSetName, b);
            } else {
                notAssigned(b);
            }
        } else if (identifiable instanceof TwoWindingsTransformer) {
            Branch<?> b = (Branch<?>) identifiable;
            if (terminalNumber == 1 || terminalNumber == 2) {
                createLimitsAdder(terminalNumber, limitSubClass, limitSetId, limitSetName, b);
            } else {
                if (terminalNumber == -1) {
                    context.ignored(limitSubClass, "Defined for Equipment TwoWindingsTransformer. Should be defined for one Terminal of Two");
                }
                notAssigned(b);
            }
        } else if (identifiable instanceof DanglingLine dl) {
            createLimitsAdder(limitSubClass, limitSetId, limitSetName, dl);
        } else if (identifiable instanceof ThreeWindingsTransformer twt) {
            if (terminalNumber == 1 || terminalNumber == 2 || terminalNumber == 3) {
                createLimitsAdder(terminalNumber, limitSubClass, limitSetId, limitSetName, twt);
            } else {
                if (terminalNumber == -1) {
                    context.ignored(limitSubClass, "Defined for Equipment ThreeWindingsTransformer. Should be defined for one Terminal of Three");
                }
                notAssigned(twt);
            }
        } else if (identifiable instanceof Switch) {
            Switch aswitch = context.network().getSwitch(equipmentId);
            notAssigned(aswitch);
        } else {
            notAssigned(identifiable);
        }
    }

    @Override
    public boolean valid() {
        if (vl == null && loadingLimitsAdder == null && loadingLimitsAdder1 == null && loadingLimitsAdder2 == null) {
            missing(String.format("Terminal %s or Equipment %s", terminalId, equipmentId));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        double normalValue = p.asDouble("normalValue");
        double value = p.asDouble("value", normalValue);
        if (value <= 0) {
            context.ignored(OPERATIONAL_LIMIT, "value is <= 0");
            return;
        }
        if (vl != null) {
            convertVoltageLimit(value);
        } else {
            if (isPatl()) { // Permanent Admissible Transmission Loading
                convertPatl(value);
            } else if (isTatl()) { // Temporary Admissible Transmission Loading
                convertTatl(value);
            }
        }
    }

    private void convertVoltageLimit(double value) {
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        if (limitTypeName.equalsIgnoreCase("highvoltage") || "LimitTypeKind.highVoltage".equals(limitType)) {
            if (value < vl.getLowVoltageLimit()) {
                context.ignored("HighVoltageLimit", "Inconsistent with low voltage limit (" + vl.getLowVoltageLimit() + "kV)");
            } else if (value < vl.getHighVoltageLimit() || Double.isNaN(vl.getHighVoltageLimit())) {
                vl.setHighVoltageLimit(value);
            }
        } else if (limitTypeName.equalsIgnoreCase("lowvoltage") || "LimitTypeKind.lowVoltage".equals(limitType)) {
            if (value > vl.getHighVoltageLimit()) {
                context.ignored("LowVoltageLimit", "Inconsistent with high voltage limit (" + vl.getHighVoltageLimit() + "kV)");
            } else if (value > vl.getLowVoltageLimit() || Double.isNaN(vl.getLowVoltageLimit())) {
                vl.setLowVoltageLimit(value);
            }
        } else {
            notAssigned(vl);
        }
    }

    private boolean isPatl() {
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        return limitTypeName.equals("PATL") || "LimitTypeKind.patl".equals(limitType) || "LimitKind.patl".equals(limitType);
    }

    private void addPatl(double value, LoadingLimitsAdder<?, ?> adder) {
        if (Double.isNaN(adder.getPermanentLimit())) {
            adder.setPermanentLimit(value);
        } else {
            if (terminalId != null) {
                context.fixed(PERMANENT_LIMIT, () -> String.format("Several permanent limits defined for Terminal %s. Only the lowest is kept.", terminalId));
            } else {
                context.fixed(PERMANENT_LIMIT, () -> String.format("Several permanent limits defined for Equipment %s. Only the lowest is kept.", equipmentId));
            }
            if (adder.getPermanentLimit() > value) {
                adder.setPermanentLimit(value);
            }
        }
    }

    private void convertPatl(double value) {
        if (loadingLimitsAdder != null) {
            addPatl(value, loadingLimitsAdder);
        } else {
            if (loadingLimitsAdder1 != null) {
                addPatl(value, loadingLimitsAdder1);
            }
            if (loadingLimitsAdder2 != null) {
                addPatl(value, loadingLimitsAdder2);
            }
        }
    }

    private boolean isTatl() {
        String limitTypeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String limitType = p.getLocal(LIMIT_TYPE);
        return limitTypeName.equals("TATL") || "LimitTypeKind.tatl".equals(limitType) || "LimitKind.tatl".equals(limitType);
    }

    private void addTatl(String name, double value, int acceptableDuration, LoadingLimitsAdder<?, ?> adder) {
        if (Double.isNaN(value)) {
            context.ignored(TEMPORARY_LIMIT, "Temporary limit value is undefined");
            return;
        }

        if (Double.isNaN(adder.getTemporaryLimitValue(acceptableDuration))) {
            adder.beginTemporaryLimit()
                    .setAcceptableDuration(acceptableDuration)
                    .setName(name)
                    .setValue(value)
                    .ensureNameUnicity()
                    .endTemporaryLimit();
        } else {
            if (terminalId != null) {
                context.fixed(TEMPORARY_LIMIT, () -> String.format("Several temporary limits defined for same acceptable duration (%d s) for Terminal %s. Only the lowest is kept.", acceptableDuration, terminalId));
            } else {
                context.fixed(TEMPORARY_LIMIT, () -> String.format("Several temporary limits defined for same acceptable duration (%d s) for Equipment %s. Only the lowest is kept.", acceptableDuration, equipmentId));
            }
            if (adder.getTemporaryLimitValue(acceptableDuration) > value) {
                adder.beginTemporaryLimit()
                        .setAcceptableDuration(acceptableDuration)
                        .setName(name)
                        .setValue(value)
                        .ensureNameUnicity()
                        .endTemporaryLimit();
            }
        }
    }

    private void convertTatl(double value) {
        int acceptableDuration = p.containsKey("acceptableDuration") ? (int) p.asDouble("acceptableDuration") : Integer.MAX_VALUE;
        // We only accept high or absoluteValue (considered as high when
        // current from the conducting equipment to the terminal) limits
        String direction = p.getId("direction");

        // if there is no direction, the limit is considered as absoluteValue (cf. CGMES specification)
        if (direction == null || direction.endsWith("high") || direction.endsWith("absoluteValue")) {
            String name = Optional.ofNullable(p.getId("shortName")).orElse(p.getId("name"));
            if (loadingLimitsAdder != null) {
                addTatl(name, value, acceptableDuration, loadingLimitsAdder);
            } else {
                if (loadingLimitsAdder1 != null) {
                    addTatl(name, value, acceptableDuration, loadingLimitsAdder1);
                }
                if (loadingLimitsAdder2 != null) {
                    addTatl(name, value, acceptableDuration, loadingLimitsAdder2);
                }
            }
        } else if (direction.endsWith("low")) {
            context.invalid(TEMPORARY_LIMIT, () -> String.format("TATL %s is a low limit", id));
        } else {
            context.invalid(TEMPORARY_LIMIT, () -> String.format("TATL %s does not have a valid direction", id));
        }
    }

    private void notAssigned() {
        notAssigned(null);
    }

    private void notAssigned(Identifiable<?> eq) {
        String type = p.getLocal(LIMIT_TYPE);
        String typeName = p.getLocal(OPERATIONAL_LIMIT_TYPE_NAME);
        String subclass = p.getLocal(OPERATIONAL_LIMIT_SUBCLASS);
        Supplier<String> reason = () -> String.format(
                "Not assigned for %s %s. Limit id, type, typeName, subClass, terminal : %s, %s, %s, %s, %s",
                eq != null ? className(eq) : "",
                eq != null ? eq.getId() : "",
                id,
                type,
                typeName,
                subclass,
                terminalId);
        context.pending(OPERATIONAL_LIMIT, reason);
    }

    private static String className(Identifiable<?> o) {
        String s = o.getClass().getName();
        int dot = s.lastIndexOf('.');
        if (dot >= 0) {
            s = s.substring(dot + 1);
        }
        s = s.replace("Impl", "");
        return s;
    }

    private final String terminalId;
    private final String equipmentId;

    private LoadingLimitsAdder<?, ?> loadingLimitsAdder;
    private LoadingLimitsAdder<?, ?> loadingLimitsAdder1;
    private LoadingLimitsAdder<?, ?> loadingLimitsAdder2;

    private VoltageLevel vl;
}
