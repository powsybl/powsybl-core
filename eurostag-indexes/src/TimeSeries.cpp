// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file TimeSeries.cpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#include <stdexcept>
#include "TimeSeries.h"

namespace itesla {

    TimeSerieType toTimeSerieType(const std::string& txt) {
        if (txt == "FREQUENCY") {
            return TimeSerieType::FREQUENCY;
        } else if (txt == "ANGLE") {
            return TimeSerieType::ANGLE;
        } else if (txt == "UNDER_VOLTAGE_AUTOMATON") {
            return TimeSerieType::UNDER_VOLTAGE_AUTOMATON;
        } else if (txt == "OVER_VOLTAGE_AUTOMATON") {
            return TimeSerieType::OVER_VOLTAGE_AUTOMATON;
        } else if (txt == "UNDER_SPEED_AUTOMATON") {
            return TimeSerieType::UNDER_SPEED_AUTOMATON;
        } else if (txt == "OVER_SPEED_AUTOMATON") {
            return TimeSerieType::OVER_SPEED_AUTOMATON;
        }  else if (txt == "INTEGRATION_STEP") {
            return TimeSerieType::INTEGRATION_STEP;
        }  else if (txt == "GENERATOR_ACTIVE_POWER") {
            return TimeSerieType::GENERATOR_ACTIVE_POWER;
        } else {
            throw std::runtime_error("Invalid time serie type " + txt);
        }
    }

    std::string toStr(TimeSerieType type) {
        switch (type) {
            case TimeSerieType::FREQUENCY:
                return "FREQUENCY";

            case TimeSerieType::ANGLE:
                return "ANGLE";

            case TimeSerieType::UNDER_VOLTAGE_AUTOMATON:
                return "UNDER_VOLTAGE_AUTOMATON";

            case TimeSerieType::OVER_VOLTAGE_AUTOMATON:
                return "OVER_VOLTAGE_AUTOMATON";

            case TimeSerieType::UNDER_SPEED_AUTOMATON:
                return "UNDER_SPEED_AUTOMATON";

            case TimeSerieType::OVER_SPEED_AUTOMATON:
                return "OVER_SPEED_AUTOMATON";

            case TimeSerieType::INTEGRATION_STEP:
                return "INTEGRATION_STEP";

            case TimeSerieType::GENERATOR_ACTIVE_POWER:
                return "GENERATOR_ACTIVE_POWER";

            case TimeSerieType::UNKNOWN:
                return "UNKNOWN";

            default: throw std::runtime_error("TODO");
        }
    }
}