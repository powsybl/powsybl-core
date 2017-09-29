// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file mpitags.hpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#ifndef MPITAGS_HPP
#define MPITAGS_HPP

#include <string>

namespace powsybl {

const int MAX_CORES_PER_RANK = 1000;
const int JOB_BUFFER_TAG = 0;
const int JOB_LENGTH_TAG = JOB_BUFFER_TAG + MAX_CORES_PER_RANK;
const int JOB_RESULT_BUFFER_TAG = JOB_LENGTH_TAG + MAX_CORES_PER_RANK;
const int JOB_RESULT_LENGTH_TAG = JOB_RESULT_BUFFER_TAG + MAX_CORES_PER_RANK;

enum class Step {
    COMMON_FILES_BCAST,
    TASKS_EXECUTION,
    SHUTDOWN
};

int step2specialLength(Step step);
Step specialLength2step(int length);
bool isSpecialLength(int length);

std::string str(const Step& step);

}

#endif // MPITAGS_HPP
