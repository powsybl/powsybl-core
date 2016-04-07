/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.db;

import java.util.Map;

import eu.itesla_project.modules.online.StateProcessingStatus;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
class StateProcessingStatusImpl implements StateProcessingStatus {
	
	private final Map<String, String> processingStatus;
	private final String detail;
	
	StateProcessingStatusImpl(Map<String, String> processingStatus, String detail) {
		this.processingStatus = processingStatus;
		this.detail = detail;
	}

	@Override
	public Map<String, String> getStatus() {
		return processingStatus;
	}

	@Override
	public String getDetail() {
		return detail;
	}

}
