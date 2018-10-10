package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class EquivalentInjectionConversion extends AbstractConductingEquipmentConversion {

    public EquivalentInjectionConversion(PropertyBag sm, Conversion.Context context) {
        super("EquivalentInjection", sm, context);
    }

    @Override
    public void convert() {
        double minP = p.asDouble("minP", 0);
        double maxP = p.asDouble("maxP", 0);
        EnergySource energySource = EnergySource.OTHER;
        PowerFlow f = powerFlow();

        double targetP = 0;
        double targetQ = 0;
        if (f.defined()) {
            targetP = -f.p();
            targetQ = -f.q();
        }
        boolean regulationCapability = p.asBoolean("regulationCapability", false);
        boolean regulationStatus = p.asBoolean("regulationStatus", regulationCapability);
        regulationStatus = regulationStatus && terminalConnected();
        double targetV = Double.NaN;
        if (terminalConnected() && regulationStatus) {
            targetV = p.asDouble("regulationTarget");
            if (targetV == 0) {
                fixed("regulationTarget", "Target voltage value can not be zero", targetV,
                        voltageLevel().getNominalV());
                targetV = voltageLevel().getNominalV();
            }
        }

        Generator g = voltageLevel().newGenerator()
                .setId(iidmId())
                .setName(iidmName())
                .setEnsureIdUnicity(false)
                .setBus(terminalConnected() ? busId() : null)
                .setConnectableBus(busId())
                .setMinP(minP)
                .setMaxP(maxP)
                .setVoltageRegulatorOn(regulationStatus)
                // .setRegulatingTerminal(regulatingTerminal)
                .setTargetP(targetP)
                .setTargetQ(targetQ)
                .setTargetV(targetV)
                .setEnergySource(energySource)
                // .setRatedS(ratedS)
                .add();

        convertedTerminals(g.getTerminal());
        ReactiveLimitsConversion.convert(p, g);
    }
}
