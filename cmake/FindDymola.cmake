# - Find Dymola
#
# Dymola_FOUND             True if Dymola exists, false otherwise
#
# =============================================================================
# Copyright (c) 2016, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
# =============================================================================

if (NOT DYMOLA_HOME AND NOT $ENV{DYMOLA_HOME} STREQUAL "")
	set(DYMOLA_HOME $ENV{DYMOLA_HOME})
endif()

if (NOT DYMOLA_HOME AND NOT $ENV{DYMOLA_ROOT} STREQUAL "")
	set(DYMOLA_HOME $ENV{DYMOLA_ROOT})
endif()

if (NOT DYMOLA_HOME OR NOT IS_DIRECTORY "${DYMOLA_HOME}")
    message(FATAL_ERROR "Dymola not found. The variable DYMOLA_HOME is NOT set or is NOT a valid directory")
endif()

set(Dymola_FOUND TRUE)
