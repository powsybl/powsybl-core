package com.powsybl.ampl.converter;

import java.io.IOException;

@FunctionalInterface
public interface AmplElementReader {
    void read(AmplNetworkReader reader) throws IOException;

}
