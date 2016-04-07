// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file TimeSeries.h
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#ifndef ITESLA_TIMESERIES_H
#define ITESLA_TIMESERIES_H

#include <vector>
#include <bits/stringfwd.h>

namespace itesla {

    enum class TimeSerieType {
        FREQUENCY,
        ANGLE,
        UNDER_VOLTAGE_AUTOMATON,
        OVER_VOLTAGE_AUTOMATON,
        UNDER_SPEED_AUTOMATON,
        OVER_SPEED_AUTOMATON,
        INTEGRATION_STEP,
        GENERATOR_ACTIVE_POWER,
        UNKNOWN
    };

    TimeSerieType toTimeSerieType(const std::string& txt);
    std::string toStr(TimeSerieType type);

    class TimeSeries {
    public:
        virtual ~TimeSeries() {}
        virtual std::vector<float> read(const std::string& id, const TimeSerieType& type) const = 0;

    };

}

#endif //ITESLA_TIMESERIES_H
