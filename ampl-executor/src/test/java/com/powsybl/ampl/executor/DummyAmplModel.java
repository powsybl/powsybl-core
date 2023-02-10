package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.AbstractNetworkApplierFactory;
import com.powsybl.ampl.converter.AmplReadableElement;
import com.powsybl.ampl.converter.NetworkApplier;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
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
    public AbstractNetworkApplierFactory getNetworkApplierFactory() {
        return new AbstractNetworkApplierFactory() {
            protected NetworkApplier of() {
                return new DummyNetworkApplier();
            }
        };
    }

    @Override
    public String getNetworkDataPrefix() {
        return "network";
    }

    @Override
    public Collection<AmplReadableElement> getAmplReadableElement() {
        return Collections.singleton(AmplReadableElement.GENERATOR);
    }

}
