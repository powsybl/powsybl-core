package com.powsybl.ampl.converter;

import java.io.IOException;

/**
 * This enum maps elements to their reading function in {@link AmplNetworkReader}
 *
 * @author Nicolas Pierre < nicolas.pierre@artelys.com >
 */
public enum AmplReadableElement {
    BATTERY(AmplNetworkReader::readBatteries),
    BUS(AmplNetworkReader::readBuses),
    BRANCH(AmplNetworkReader::readBranches),
    GENERATOR(AmplNetworkReader::readGenerators),
    HVDCLINE(AmplNetworkReader::readHvdcLines),
    LCC_CONVERTER_STATION(AmplNetworkReader::readLccConverterStations),
    LOAD(AmplNetworkReader::readLoads),
    PHASE_TAP_CHANGER(AmplNetworkReader::readPhaseTapChangers),
    RATIO_TAP_CHANGER(AmplNetworkReader::readRatioTapChangers),
    SHUNT(AmplNetworkReader::readShunts),
    VSC_CONVERTER_STATION(AmplNetworkReader::readVscConverterStations);
    private final ReadElementInterface readElementConsumer;

    AmplReadableElement(ReadElementInterface readElementConsumer) {
        this.readElementConsumer = readElementConsumer;
    }

    public void readElement(AmplNetworkReader reader) throws IOException {
        this.readElementConsumer.read(reader);
    }

    @FunctionalInterface
    private interface ReadElementInterface {
        void read(AmplNetworkReader reader) throws IOException;
    }
}
