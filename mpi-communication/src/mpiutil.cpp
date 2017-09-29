// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file mpiutil.cpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#include <mpi.h>
#include <string>
#include <stdexcept>
#include <boost/lexical_cast.hpp>
#include "mpiutil.hpp"

namespace powsybl {

namespace mpi {

std::string processorName() {
    char buffer[MPI_MAX_PROCESSOR_NAME];
    int length;
    if (MPI_Get_processor_name(buffer, &length) != MPI_SUCCESS) {
        throw new std::runtime_error("MPI_Get_processor_name error");
    }
    return std::string(buffer, length);
}

std::string version() {
    int version;
    int subversion;
    if (MPI_Get_version(&version, &subversion) != MPI_SUCCESS) {
        throw new std::runtime_error("MPI_Get_version error");
    }
    return boost::lexical_cast<std::string>(version) + "." + boost::lexical_cast<std::string>(subversion);
}

void initThreadFunneled() {
    int provided;
    if (MPI_Init_thread(NULL, NULL, MPI_THREAD_FUNNELED, &provided) != MPI_SUCCESS) {
        throw new std::runtime_error("MPI_Init_thread error");
    }
    if (provided != MPI_THREAD_FUNNELED) {
        throw std::runtime_error("MPI_THREAD_FUNNELED level not supported (provided="
                                 + boost::lexical_cast<std::string>(provided) + ")");
    }
}

}

}
