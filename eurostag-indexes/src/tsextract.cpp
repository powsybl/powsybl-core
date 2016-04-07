// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file tsextract.cpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#include <iostream>
#include <fstream>
#include "EurostagTimeSeries.h"

int main(int argc, char *argv[]) {
    if (argc != 5) {
        std::cerr << argv[0] << " <.res file> <equipment id> <variable type> <output file>" << std::endl;
        exit(-1);
    }
    std::string resFile = argv[1];
    std::string equipmentId = argv[2];
    itesla::TimeSerieType timeSerieType = itesla::toTimeSerieType(argv[3]);
    std::string outFile = argv[4];

    itesla::EurostagTimeSeries timeSeries(resFile);
    std::vector<float> values = timeSeries.read(equipmentId, timeSerieType);
    std::ofstream out(outFile);
    for (std::vector<float>::const_iterator it = values.begin(); it != values.end(); it++) {
        out << *it << std::endl;
    }

    return 0;
}
