// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file mpiutil.hpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#ifndef MPIUTIL_HPP
#define MPIUTIL_HPP

namespace powsybl {

namespace mpi {

std::string processorName();
std::string version();
void initThreadFunneled();

}

}

#endif // MPIUTIL_HPP
