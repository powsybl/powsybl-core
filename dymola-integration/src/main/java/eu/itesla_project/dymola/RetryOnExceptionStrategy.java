/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class RetryOnExceptionStrategy {
	public static final int DEFAULT_RETRIES = 3;
		public static final long DEFAULT_WAIT_TIME_IN_MILLI = 2000;
 
		private int numberOfRetries;
		private int numberOfTriesLeft;
		private long timeToWait;
 
		public RetryOnExceptionStrategy() {
			this(DEFAULT_RETRIES, DEFAULT_WAIT_TIME_IN_MILLI);
		}
 
		public RetryOnExceptionStrategy(int numberOfRetries,
										long timeToWait) {
			this.numberOfRetries = numberOfRetries;
			numberOfTriesLeft = numberOfRetries;
			this.timeToWait = timeToWait;
		}
 
		public boolean shouldRetry() {
			return numberOfTriesLeft > 0;
		}
 
		public void errorOccured(Exception e) throws Exception {
			numberOfTriesLeft--;
			if (!shouldRetry()) {
				String excMsg=(e!=null)? e.getMessage() : "";
				throw new Exception("Retry Failed: Total " + numberOfRetries
						+ " attempts made at interval " + getTimeToWait()
						+ "ms - Exception message: " + excMsg);
			}
			waitUntilNextTry();
		}
 
		public long getTimeToWait() {
			return timeToWait;
		}
 
		private void waitUntilNextTry() {
			try {
				Thread.sleep(getTimeToWait());
			} catch (InterruptedException ignored) {
			}
		}
	}