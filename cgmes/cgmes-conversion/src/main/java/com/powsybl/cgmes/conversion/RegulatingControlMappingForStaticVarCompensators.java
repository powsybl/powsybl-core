package com.powsybl.cgmes.conversion;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public class RegulatingControlMappingForStaticVarCompensators {

    public RegulatingControlMappingForStaticVarCompensators(RegulatingControlMapping parent) {
        this.parent = parent;
        this.context = parent.context();
        mapping = new HashMap<>();
    }

    public void add(String staticVarCompensatorId, PropertyBag sm) {
        String cgmesRegulatingControlId = getRegulatingControlId(sm);
        boolean controlEnabledProperty = sm.asBoolean("controlEnabled", false);
        double defaultTargetVoltage = sm.asDouble("voltageSetPoint");
        double defaultTargetReactivePower = sm.asDouble("q");
        String defaultRegulationMode = sm.getId("controlMode");

        if (mapping.containsKey(staticVarCompensatorId)) {
            throw new CgmesModelException(
                "StaticVarCompensator already added, IIDM StaticVarCompensator Id : " + staticVarCompensatorId);
        }

        CgmesRegulatingControlForStaticVarCompensator rc = new CgmesRegulatingControlForStaticVarCompensator();
        rc.regulatingControlId = cgmesRegulatingControlId;
        rc.controlEnabledProperty = controlEnabledProperty;
        rc.defaultTargetVoltage = defaultTargetVoltage;
        rc.defaultTargetReactivePower = defaultTargetReactivePower;
        rc.defaultRegulationMode = defaultRegulationMode;

        mapping.put(staticVarCompensatorId, rc);
    }

    void applyRegulatingControls(Network network) {
        network.getStaticVarCompensatorStream().forEach(this::apply);
    }

    private void apply(StaticVarCompensator svc) {
        CgmesRegulatingControlForStaticVarCompensator rd = mapping.get(svc.getId());
        apply(svc, rd);
    }

    private void apply(StaticVarCompensator svc, CgmesRegulatingControlForStaticVarCompensator rc) {
        if (rc == null) {
            return;
        }
        if (!rc.controlEnabledProperty) {
            return;
        }

        String controlId = rc.regulatingControlId;
        if (controlId == null) {
            context.missing("Regulating control Id not defined");
            RegulatingControlSvcAttributes rcSvc = getDefaultRegulatingControl(rc);
            apply(rcSvc, svc);
            return;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            RegulatingControlSvcAttributes rcSvc = getDefaultRegulatingControl(rc);
            apply(rcSvc, svc);
            return;
        }
        if (!control.enabled) {
            return;
        }

        RegulatingControlSvcAttributes rcSvc = getRegulatingControlSvc(control, rc, svc.getId());
        apply(rcSvc, svc);
        if (rcSvc != null) {
            control.hasCorrectlySet();
        }
    }

    private RegulatingControlSvcAttributes getRegulatingControlSvc(RegulatingControl control,
        CgmesRegulatingControlForStaticVarCompensator rc, String svcId) {
        if (context.terminalMapping().areAssociated(control.cgmesTerminal, control.topologicalNode)) {
            return getRegulatingControl(control);
        } else {
            context.pending(
                String.format(
                    "Remote control for static var compensator %s replaced by voltage local control at nominal voltage",
                    svcId),
                "IIDM model does not support remote control for static var compensators");

            return getDefaultRegulatingControl(rc);
        }
    }

    private RegulatingControlSvcAttributes getRegulatingControl(RegulatingControl control) {

        double targetVoltage = Double.NaN;
        double targetReactivePower = Double.NaN;
        StaticVarCompensator.RegulationMode regulationMode = StaticVarCompensator.RegulationMode.OFF;

        if (RegulatingControlMapping.isControlModeVoltage(control.mode.toLowerCase())) {
            regulationMode = StaticVarCompensator.RegulationMode.VOLTAGE;
            targetVoltage = control.targetValue;
        } else if (control.mode.toLowerCase().endsWith("reactivepower")) {
            regulationMode = StaticVarCompensator.RegulationMode.REACTIVE_POWER;
            targetReactivePower = control.targetValue;
        }

        RegulatingControlSvcAttributes rcSvc = new RegulatingControlSvcAttributes();
        rcSvc.targetVoltage = targetVoltage;
        rcSvc.targetReactivePower = targetReactivePower;
        rcSvc.regulationMode = regulationMode;

        return rcSvc;
    }

    private RegulatingControlSvcAttributes getDefaultRegulatingControl(CgmesRegulatingControlForStaticVarCompensator rc) {

        double targetVoltage = Double.NaN;
        double targetReactivePower = Double.NaN;
        StaticVarCompensator.RegulationMode regulationMode = StaticVarCompensator.RegulationMode.OFF;

        if (RegulatingControlMapping.isControlModeVoltage(rc.defaultRegulationMode.toLowerCase())) {
            regulationMode = StaticVarCompensator.RegulationMode.VOLTAGE;
            targetVoltage = rc.defaultTargetVoltage;
        } else if (rc.defaultRegulationMode.toLowerCase().endsWith("reactivepower")) {
            regulationMode = StaticVarCompensator.RegulationMode.REACTIVE_POWER;
            targetReactivePower = rc.defaultTargetReactivePower;
        }

        RegulatingControlSvcAttributes rcSvc = new RegulatingControlSvcAttributes();
        rcSvc.targetVoltage = targetVoltage;
        rcSvc.targetReactivePower = targetReactivePower;
        rcSvc.regulationMode = regulationMode;

        return rcSvc;
    }

    private void apply(RegulatingControlSvcAttributes rcSvc, StaticVarCompensator svc) {
        if (rcSvc == null) {
            return;
        }
        svc.setVoltageSetPoint(rcSvc.targetVoltage);
        svc.setReactivePowerSetPoint(rcSvc.targetReactivePower);
        svc.setRegulationMode(rcSvc.regulationMode);
    }

    private String getRegulatingControlId(PropertyBag p) {
        String regulatingControlId = null;

        if (p.containsKey(RegulatingControlMapping.REGULATING_CONTROL)) {
            String controlId = p.getId(RegulatingControlMapping.REGULATING_CONTROL);
            RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
            if (control != null) {
                regulatingControlId = controlId;
            }
        }

        return regulatingControlId;
    }

    private static class CgmesRegulatingControlForStaticVarCompensator {
        String regulatingControlId;
        boolean controlEnabledProperty;
        double defaultTargetVoltage;
        double defaultTargetReactivePower;
        String defaultRegulationMode;
    }

    private static class RegulatingControlSvcAttributes {
        double targetVoltage;
        double targetReactivePower;
        StaticVarCompensator.RegulationMode regulationMode;
    }

    private final RegulatingControlMapping parent;
    private final Map<String, CgmesRegulatingControlForStaticVarCompensator> mapping;
    private final Context context;
}
