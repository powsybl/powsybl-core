package com.powsybl.cgmes.conversion;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public class RegulatingControlMappingForGenerators {

    private static final String QPERCENT = "qPercent";

    public RegulatingControlMappingForGenerators(RegulatingControlMapping parent) {
        this.parent = parent;
        this.context = parent.context();
        mapping = new HashMap<>();
    }

    public static void initialize(GeneratorAdder adder) {
        adder.setRegulatingTerminal(null);
        adder.setTargetV(Double.NaN);
        adder.setVoltageRegulatorOn(false);
    }

    public void add(String iidmId, PropertyBag sm) {
        String rcId = getRegulatingControlId(sm);
        double qPercent = sm.asDouble(QPERCENT);
        add(iidmId, rcId, qPercent);
    }

    void applyRegulatingControls(Network network) {
        network.getGeneratorStream().forEach(this::apply);
    }

    private void apply(Generator gen) {
        RegulatingControlForGenerator rd = mapping.get(gen.getId());
        apply(gen, rd);
    }

    private void apply(Generator gen, RegulatingControlForGenerator rc) {
        if (rc == null) {
            return;
        }

        String controlId = rc.regulatingControlId;
        if (controlId == null) {
            context.missing("Regulating control Id not defined");
            return;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            return;
        }

        if (RegulatingControlMapping.isControlModeVoltage(control.mode)) {
            RegulatingControlVoltage gcv = getRegulatingControlVoltage(controlId, control, rc.qPercent, gen);
            apply(gcv, gen);
            control.hasCorrectlySetEq(gen.getId());
        } else {
            context.ignored(control.mode, String.format("Unsupported regulation mode for generator %s", gen.getId()));
        }
    }

    private RegulatingControlVoltage getRegulatingControlVoltage(String controlId,
        RegulatingControl control, double qPercent, Generator gen) {

        // Take default terminal if it has not been defined
        Terminal terminal = getRegulatingTerminal(gen, control.cgmesTerminal, control.topologicalNode);
        if (terminal == null) {
            context.missing(String.format(RegulatingControlMapping.MISSING_IIDM_TERMINAL, control.topologicalNode));
            return null;
        }

        double targetV;
        if (control.targetValue <= 0.0 || Double.isNaN(control.targetValue)) {
            targetV = terminal.getVoltageLevel().getNominalV();
            context.fixed(controlId, "Invalid value for regulating target value", control.targetValue, targetV);
        } else {
            targetV = control.targetValue;
        }

        boolean voltageRegulatorOn = false;
        if (control.enabled) {
            voltageRegulatorOn = true;
        }

        RegulatingControlVoltage gcv = new RegulatingControlVoltage();
        gcv.terminal = terminal;
        gcv.targetV = targetV;
        gcv.voltageRegulatorOn = voltageRegulatorOn;
        gcv.qPercent = qPercent;

        return gcv;
    }

    private static void apply(RegulatingControlVoltage rcv, Generator gen) {
        if (rcv == null) {
            return;
        }
        gen.setRegulatingTerminal(rcv.terminal);
        gen.setTargetV(rcv.targetV);
        gen.setVoltageRegulatorOn(rcv.voltageRegulatorOn);

        // add qPercent as an extension
        if (!Double.isNaN(rcv.qPercent)) {
            CoordinatedReactiveControl coordinatedReactiveControl = new CoordinatedReactiveControl(gen, rcv.qPercent);
            gen.addExtension(CoordinatedReactiveControl.class, coordinatedReactiveControl);
        }
    }

    private Terminal getRegulatingTerminal(Generator gen, String cgmesTerminal, String topologicalNode) {
        // Will take default terminal ONLY if it has not been explicitly defined in
        // CGMES
        Terminal terminal = getDefaultTerminal(gen);
        if (cgmesTerminal != null || topologicalNode != null) {
            terminal = parent.findRegulatingTerminal(cgmesTerminal, topologicalNode);
            // If terminal is null here it means that no IIDM terminal has been found
            // from the initial CGMES terminal or topological node,
            // we will consider the regulating control invalid,
            // in this case we will not use the default terminal
            // (no localization of regulating controls)
        }
        return terminal;
    }

    private static Terminal getDefaultTerminal(Generator gen) {
        return gen.getTerminal();
    }

    private void add(String iidmGeneratorId, String cgmesRegulatingControlId, double qPercent) {
        if (mapping.containsKey(iidmGeneratorId)) {
            throw new CgmesModelException("Generator already added, IIDM Generator Id : " + iidmGeneratorId);
        }

        RegulatingControlForGenerator rd = new RegulatingControlForGenerator();
        rd.regulatingControlId = cgmesRegulatingControlId;
        rd.qPercent = qPercent;
        mapping.put(iidmGeneratorId, rd);
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

    private static class RegulatingControlForGenerator {
        String regulatingControlId;
        double qPercent;
    }

    private static class RegulatingControlVoltage {
        Terminal terminal;
        double targetV;
        boolean voltageRegulatorOn;
        double qPercent;
    }

    private final RegulatingControlMapping parent;
    private final Map<String, RegulatingControlForGenerator> mapping;
    private final Context context;
}
