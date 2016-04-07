/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.sampling;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class MatlabException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MatlabException() {
		super();
	}

	public MatlabException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MatlabException(String message, Throwable cause) {
		super(message, cause);
	}

	public MatlabException(String message) {
		super(message);
	}

	public MatlabException(Throwable cause) {
		super(cause);
	}
	

}
