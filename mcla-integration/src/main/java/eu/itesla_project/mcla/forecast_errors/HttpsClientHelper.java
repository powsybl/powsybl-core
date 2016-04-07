/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.forecast_errors;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class HttpsClientHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpsClientHelper.class);

	public static void remoteDataToFile(String https_url, String username,
			String password, String filePath) throws Exception  {
		remoteDataToFile(https_url, username, password, filePath, true);
	}

	public static void remoteDataToFile(String https_url, String username,
			String password, String filePath, boolean trustCerts) throws Exception
			{
		URL url;

		if (trustCerts == true) {
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {
				}
			} };
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
			// Create & install all-trusting host name verifier
			HttpsURLConnection
					.setDefaultHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String hostname,
								SSLSession session) {
							return true;
						}
					});
		}

		url = new URL(https_url);
		HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
		String userpass = username + ":" + password;
		String basicAuth = "Basic "
				+ javax.xml.bind.DatatypeConverter.printBase64Binary(userpass
						.getBytes());

		con.setRequestProperty("Authorization", basicAuth);
		File oFile = new File(filePath);
		PrintWriter fw = new PrintWriter(oFile);
		get_content(con, fw);

	}


	public static void remoteDataToFilePOST(String https_url, String parameters, String username,
			String password, String filePath) throws Exception  {
		remoteDataToFilePOST(https_url, parameters, username, password, filePath, true);
	}
	
	public static void remoteDataToFilePOST(String https_url, String parameters, String username,
			String password, String filePath, boolean trustCerts) throws Exception
			{
		URL url = new URL(https_url);
		// Create & install all-trusting host name verifier
		if ((trustCerts == true) && ("https".equalsIgnoreCase(url.getProtocol()))){
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {
				}
			} };
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection
					.setDefaultHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String hostname,
								SSLSession session) {
							return true;
						}
					});
		}

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		String userpass = username + ":" + password;
		String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
		con.setRequestProperty("Authorization", basicAuth);
		con.setRequestMethod("POST");
		//con.setRequestProperty("User-Agent", USER_AGENT);
		//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con.setDoOutput(true);

		//send POST request
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		try {
			wr.writeBytes(parameters);
		} finally {
			if (wr!=null) {
				try {
				wr.flush();
				wr.close();
				} catch (Throwable t) {
				}
			}
		}
		
		int responseCode = con.getResponseCode();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Sending 'POST' request to URL : {} " , url);
			LOGGER.trace("Post parameters : {}" , parameters);
			LOGGER.trace("Response Code : {}" , responseCode);
		}
		switch (responseCode) {
			case 200:
				break;
			default: throw new Exception("Unexpected HTTP code: " + responseCode);
		}

		File oFile = new File(filePath);
		PrintWriter fw = new PrintWriter(oFile);
		get_content(con, fw);
	}

	
	
	protected static void get_content(HttpURLConnection con, PrintWriter writer) throws Exception {
		if ((con != null) && (writer != null)) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(
						con.getInputStream()));

				String input;
				

				while ((input = br.readLine()) != null) {
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace(input);
					}
					writer.println(input);
				}
			} finally {
				try {
					writer.flush();
					writer.close();
				} catch (Throwable t) {
				}
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
					}
				}
			}

		}
	}
	
	public static void postCSVData(String https_url, String cvsData, String username,
			String password, boolean trustCerts) throws Exception
			{
		URL url = new URL(https_url);
		// Create & install all-trusting host name verifier
		if ((trustCerts == true) && ("https".equalsIgnoreCase(url.getProtocol()))){
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {
				}
			} };
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection
					.setDefaultHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String hostname,
								SSLSession session) {
							return true;
						}
					});
		}

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		String userpass = username + ":" + password;
		String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
		con.setRequestProperty("Authorization", basicAuth);
		// add csv content type, otherwise histodb does not know how to correctly handle the post
		con.setRequestProperty("Content-type","text/csv"); 
		con.setRequestMethod("POST");
		//con.setRequestProperty("User-Agent", USER_AGENT);
		//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con.setDoOutput(true);

		//send POST request
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		try {
			wr.writeBytes(cvsData);
		} finally {
			if (wr!=null) {
				try {
				wr.flush();
				wr.close();
				} catch (Throwable t) {
				}
			}
		}
		
		int responseCode = con.getResponseCode();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Sending 'POST' request to URL : {} " , url);
			LOGGER.trace("Response Code : {}" , responseCode);
		}
		switch (responseCode) {
			case 200:
				break;
			default: throw new Exception("Unexpected HTTP code: " + responseCode);
		}
	}

}