// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file EurostagTimeSeries.h
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#ifndef ITESLA_EUROSTAGTIMESERIES_H
#define ITESLA_EUROSTAGTIMESERIES_H

#include <bits/stringfwd.h>
#include "TimeSeries.h"

namespace itesla {

    class EurostagTimeSeries : public TimeSeries {

    public:
        EurostagTimeSeries(const std::string& fileName);
        ~EurostagTimeSeries();

        std::vector<float> read(const std::string& id, const TimeSerieType& type) const;

    };

}

#endif //ITESLA_EUROSTAGTIMESERIES_H
