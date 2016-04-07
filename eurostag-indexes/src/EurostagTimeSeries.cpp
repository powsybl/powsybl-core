// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file EurostagTimeSeries.cpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#include "EurostagTimeSeries.h"
#include "EurostagApiException.hpp"
#include <stdexcept>
#include <api_eurostag.h>
#include <iostream>

//if it was not present, it would not link against EUROSTAG libs !?!?! .....
extern "C" {
int gBatchMode = 1;
void ecrea1_() {
}
void ecmnou_() {
}
}

namespace itesla {

    EurostagTimeSeries::EurostagTimeSeries(const std::string& fileName) {
        int code = esg_init();
        if (code != 0)
            throw EurostagApiException("esg_init", "", TimeSerieType::UNKNOWN, code);

        code = esg_loadCase((char*) fileName.c_str());
        if (code != 0)
            throw EurostagApiException("esg_loadCase", "", TimeSerieType::UNKNOWN, code);
    }

    EurostagTimeSeries::~EurostagTimeSeries() {
        int code = esg_unloadCase();
        if (code != 0)
            throw EurostagApiException("esg_unloadCase", "", TimeSerieType::UNKNOWN, code);
    }

    std::vector<float> EurostagTimeSeries::read(const std::string& id, const TimeSerieType& type) const {

        int nbrTimes;
        int code = esg_getTimesNumber(&nbrTimes);
        if (code != 0)
            throw EurostagApiException("esg_getTimesNumber", id, TimeSerieType::UNKNOWN, code);

        std::vector<float> values;

        int typeVar;
        eustagUnit unit;
        char* zid1 = (char *) "        ";
        char* zid2 = (char *) "        ";
        char* zidn = (char *) "        ";
        char* zid3 = (char *) "        ";
        char* zid4 = (char *) "        ";

        switch (type) {
            case TimeSerieType::FREQUENCY:
                typeVar = 41;
                unit = ESG_SI;
                zid1 = (char *) id.c_str();
                break;

            case TimeSerieType::ANGLE:
                typeVar = 36;
                unit = ESG_SI;
                zid1 = (char *) id.c_str();
                break;

            case TimeSerieType::UNDER_VOLTAGE_AUTOMATON:
                typeVar = 30;
                unit = ESG_SI;
                zid1 = (char *) "A11     ";
                zid2 = (char *) id.c_str();
                zid4 = (char *) "ESOUST  ";
                break;

            case TimeSerieType::OVER_VOLTAGE_AUTOMATON:
                typeVar = 30;
                unit = ESG_SI;
                zid1 = (char *) "A11     ";
                zid2 = (char *) id.c_str();
                zid4 = (char *) "ESURT   ";
                break;

            case TimeSerieType::UNDER_SPEED_AUTOMATON:
                typeVar = 30;
                unit = ESG_SI;
                zid1 = (char *) "A12     ";
                zid2 = (char *) id.c_str();
                zid4 = (char *) "KACTSS  ";
                break;

            case TimeSerieType::OVER_SPEED_AUTOMATON:
                typeVar = 30;
                unit = ESG_SI;
                zid1 = (char *) "A12     ";
                zid2 = (char *) id.c_str();
                zid4 = (char *) "KACTSU   ";
                break;

            case TimeSerieType::INTEGRATION_STEP:
                typeVar = 15;
                unit = ESG_PU;
                break;

            case TimeSerieType::GENERATOR_ACTIVE_POWER:
                typeVar = 6;
                unit = ESG_SI;
                zid1 = (char *) id.c_str();
                break;

            default: throw std::runtime_error("Variable not supported");
        }

        float* tmp;
        code = esg_getValues(typeVar, zid1, zid2, zidn, zid3, zid4, unit, &tmp);
        if (code != 0)
            throw EurostagApiException("esg_getValues", id, type, code);

        for (int i = 0; i < nbrTimes; ++i) {
            values.push_back(tmp[i]);
        }
        return values;
    }


}