// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file mpitags.cpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#include <stdexcept>
#include "mpitags.hpp"

namespace powsybl {

std::string str(const Step& step) {
    switch (step) {
        case Step::COMMON_FILES_BCAST:
            return "COMMON_FILES_BCAST";
        case Step::TASKS_EXECUTION:
            return "TASKS_EXECUTION";
        case Step::SHUTDOWN:
            return "SHUTDOWN";
        default:
            throw std::runtime_error("Unexpected step value");
    }
}

// special length message to switch slaves state
const int SPECIAL_LENGTH_SWITCH_TO_SHUTDOWN = -1;
const int SPECIAL_LENGTH_SWITCH_TO_TASKS_EXECUTION = -2;
const int SPECIAL_LENGTH_SWITCH_TO_COMMON_FILES_BCAST = -3;

int step2specialLength(Step step) {
	switch (step) {
        case Step::COMMON_FILES_BCAST:
            return SPECIAL_LENGTH_SWITCH_TO_COMMON_FILES_BCAST;
        case Step::TASKS_EXECUTION:
            return SPECIAL_LENGTH_SWITCH_TO_TASKS_EXECUTION;
        case Step::SHUTDOWN:
            return SPECIAL_LENGTH_SWITCH_TO_SHUTDOWN;
        default:
            throw std::runtime_error("Unexpected step value");
    }
}

Step specialLength2step(int length) {
	switch (length) {
		case SPECIAL_LENGTH_SWITCH_TO_SHUTDOWN:
			return Step::SHUTDOWN;
		case SPECIAL_LENGTH_SWITCH_TO_TASKS_EXECUTION:
			return Step::TASKS_EXECUTION;
		case SPECIAL_LENGTH_SWITCH_TO_COMMON_FILES_BCAST:
			return Step::COMMON_FILES_BCAST;
		default:
			throw std::runtime_error("Unexpected length code");
	}
}

bool isSpecialLength(int length) {
	return length == SPECIAL_LENGTH_SWITCH_TO_SHUTDOWN 
		|| length == SPECIAL_LENGTH_SWITCH_TO_TASKS_EXECUTION 
		|| length == SPECIAL_LENGTH_SWITCH_TO_COMMON_FILES_BCAST;
}

}
