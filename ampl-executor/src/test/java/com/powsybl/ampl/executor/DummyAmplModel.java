package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.NetworkApplier;
import com.powsybl.iidm.network.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class DummyAmplModel extends AbstractAmplModel {

    @Override
    public List<Pair<String, InputStream>> getModelAsStream() {
        return new LinkedList<>();
    }

    @Override
    public List<String> getAmplRunFiles() {
        return new LinkedList<>();
    }

    @Override
    public String getOutputFilePrefix() {
        return "output";
    }

    @Override
    public NetworkApplier getNetworkApplier() {
        return new NetworkApplier() {
            @Override
            public void applyGenerators(Generator g, int busNum, boolean vregul, double targetV, double targetP, double targetQ, double p, double q) {
                // do nothing with the results
            }

            @Override
            public void applyBattery(Battery b, double targetP, double targetQ, double p, double q) {
                // do nothing with the results
            }

            @Override
            public void applyShunt(ShuntCompensator sc, double q, int sections) {
                // do nothing with the results
            }

            @Override
            public void applySvc(StaticVarCompensator svc, boolean vregul, double targetV, double q) {
                // do nothing with the results
            }

            @Override
            public void applyVsc(VscConverterStation vsc, boolean vregul, double targetV, double targetQ, double p, double q) {
                // do nothing with the results
            }
        };
    }

    @Override
    public String getNetworkDataPrefix() {
        return "network";
    }

}
