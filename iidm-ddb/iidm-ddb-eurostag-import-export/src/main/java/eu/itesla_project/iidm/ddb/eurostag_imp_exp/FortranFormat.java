package eu.itesla_project.iidm.ddb.eurostag_imp_exp;
//
//  FortranFormat Version 1.1, written by Kevin J. Theisen
//
//  Copyright (c) 2009 iChemLabs, LLC.  All rights reserved.
//
//  $Revision: 793 $
//  $Author: kevin $
//  $LastChangedDate: 2009-11-15 20:03:16 -0400 (Sun, 15 Nov 2009) $
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are met:
//
//  1. Redistributions of source code must retain the above copyright notice,
//     this list of conditions and the following disclaimer.
//  2. Redistributions in binary form must reproduce the above copyright notice,
//     this list of conditions and the following disclaimer in the
//     documentation and/or other materials provided with the distribution.
//  3. Neither the name of the iChemLabs nor the names of its contributors
//     may be used to endorse or promote products derived from this software
//     without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
//  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
//  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
//  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
//  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
//  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * The Class FortranFormat.
 */
public class FortranFormat {

	/** A hash of the descriptors for easy access. */
	private final static HashMap<String, EditDescriptor> DESCRIPTOR_HASH = new HashMap<String, EditDescriptor>(EditDescriptor.values().length);

	static {
		for (final EditDescriptor ed : EditDescriptor.values()) {
			DESCRIPTOR_HASH.put(ed.getTag(), ed);
		}
	}

	/**
	 * The Enum EditDescriptor.
	 */
	private enum EditDescriptor {

		/** The CHARACTER. */
		CHARACTER("A", false) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				String use = null;
				if (o != null) {
					use = o instanceof String ? (String) o : o.toString();
				}
				return format(o == null ? null : u.getLength() > 0 && use.length() > u.getLength() ? use.substring(0, u.getLength()) : use, use != null && u.getLength() == 0 ? use.length() : u.getLength(), !options.isLeftAlignCharacters());
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				return s.trim();
			}
		},
		/** The CHARACTER. */
		CHARACTER2("K", false) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				String use = null;
				if (o != null) {
					use = o instanceof String ? (String) o : o.toString();
				}
				return format(o == null ? null : u.getLength() > 0 && use.length() > u.getLength() ? use.substring(0, u.getLength()) : use, use != null && u.getLength() == 0 ? use.length() : u.getLength(), true);
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				return s.trim();
			}
		},

		/** The INTEGER. */
		INTEGER("I", false) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				String s = o == null ? null : Integer.toString((Integer) o);
				if (s != null && u.getDecimalLength() > 0) {
					final boolean neg = s.charAt(0) == '-';
					if (neg) {
						s = s.substring(1);
					}
					final int numzeros = u.getDecimalLength() - s.length();
					final StringBuilder sb2 = new StringBuilder();
					if (neg) {
						sb2.append('-');
					}
					for (int j = 0; j < numzeros; j++) {
						sb2.append('0');
					}
					sb2.append(s);
					s = sb2.toString();
				}
				return format(s, u.getLength(), true);
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				if(s.length()==0){
					if(options.isReturnZeroForBlanks()){
						return Integer.valueOf(0);
					}else{
						return null;
					}
				}else{
					return Integer.parseInt(s);
				}
			}
		},

		/** The LOGICAL. */
		LOGICAL("L", false) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				String s = o == null ? null : (Boolean) o ? "T" : "F";
				if (s != null) {
					final StringBuilder sb2 = new StringBuilder();
					for (int j = 0; j < u.getLength() - 1; j++) {
						sb2.append(' ');
					}
					sb2.append(s);
					s = sb2.toString();
				}
				return format(s, u.getLength(), false);
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				return s.length() == 0 ? null : s.charAt(0) == 'T' || s.charAt(0) == 't';
			}
		},

		/** The REAL_DECIMAL. */
		REAL_DECIMAL("F", false) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				String s = null;
				if (o != null) {
					//bic original loses precision Double d = o instanceof Double ? (Double) o : (Float) o;
					Double d = o instanceof Double ? (Double) o : new Double(o.toString());
					final boolean neg = d < 0;
					if (neg) {
						d *= -1;
					}
					final StringBuilder dfs = new StringBuilder();
					final int intLength = Integer.toString(d.intValue()).length();
					for (int j = 0; j < intLength; j++) {
						dfs.append('0');
					}
					dfs.append('.');
					for (int j = 0; j < u.getDecimalLength(); j++) {
						dfs.append('0');
					}
					//bic s = (neg ? '-' : "") + new DecimalFormat(dfs.toString()).format(d);
					s = (neg ? '-' : "") + newDF(dfs.toString()).format(d);

					//pro Eurostag, make it compact: drop the leading zero character, if the string starts with "0." or "-0."
					if (s.length()> u.getLength()) {
						if (s.startsWith("0.")) {
							s=s.substring(1,s.length());
						} else if (s.startsWith("-0.")) {
							s="-"+s.substring(2,s.length());
						}
						//then, if needed, 'truncate' to match the desired length
						//todo - round instead?
						if ((s.indexOf('.') != -1) && (s.substring(0,u.getLength()).indexOf('.') != -1)) {
							s=s.substring(0,u.getLength());
						}
					}
				}

				String retVal=format(s, u.getLength(), true);
				//bic add final . if Integer ... needed?  it seems it's not needed.
//				if ((retVal.trim().length()>0) && (!retVal.contains("."))) {
//					retVal=retVal+".";
//				}
				return retVal;
			}

			@Override
			public Object parse(final Unit u, String s, final Options options) throws IOException {
				Double returning = null;
				if (s.indexOf('E') == -1) {
					returning = s.length() == 0 ? null : Double.parseDouble(s) / (s.indexOf('.') == -1 ? Math.pow(10, u.getDecimalLength()) : 1);
				} else {
					String end = s.substring(s.indexOf("E") + 1);
					if (end.startsWith("+")) {
						end = end.substring(1);
					}
					s = s.substring(0, s.indexOf("E"));
					returning = s.length() == 0 ? null : Double.parseDouble(s) / (s.indexOf('.') == -1 ? Math.pow(10, u.getDecimalLength()) : 1) * Math.pow(10, Integer.parseInt(end));
				}
				if (returning == null && options.isReturnZeroForBlanks()) {
					returning = Double.valueOf(0);
				}
				if (returning == null) { return null; }
				return options.isReturnFloats() && s.length() != 0 ? new Float(returning) : returning;
			}
		},

		/** The REAL_DECIMAL_REDUNDANT. */
		REAL_DECIMAL_REDUNDANT("G", false) {
			@Override
			public String format(final Unit u, final Object o, final Options options) throws IOException {
				return REAL_DECIMAL.format(u, o, options);
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				return REAL_DECIMAL.parse(u, s, options);
			}
		},

		/** The REAL_DOUBLE. */
		REAL_DOUBLE("D", false) {
			@Override
			public String format(final Unit u, final Object o, final Options options) throws IOException {
				throw new java.io.IOException("Ouput for the D edit descriptor is not supported.");
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				throw new java.io.IOException("Input for the D edit descriptor is not supported.");
			}
		},

		/** The REAL_ENGINEERING. */
		REAL_ENGINEERING("EN", false) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				String s = null;
				if (o != null) {
					Double d = o instanceof Double ? (Double) o : (Float) o;
					int exp = 0;
					final boolean neg = d < 0;
					if (neg) {
						d *= -1;
					}
					while (d > 10) {
						d /= 10;
						exp += 1;
					}
					while (d < 1) {
						d *= 10;
						exp -= 1;
					}
					while (exp % 3 != 0) {
						d *= 10;
						exp -= 1;
					}
					final boolean expneg = exp < 0;
					if (expneg) {
						exp *= -1;
					}
					StringBuilder dfs = new StringBuilder();
					dfs.append("0.");
					for (int j = 0; j < u.getDecimalLength(); j++) {
						dfs.append('0');
					}
					//s = (neg ? "-" : "") + new DecimalFormat(dfs.toString()).format(d);
					s = (neg ? "-" : "") + newDF(dfs.toString()).format(d);
					dfs = new StringBuilder();
					for (int j = 0; j < u.getExponentLength(); j++) {
						dfs.append('0');
					}
					//s = s + "E" + (expneg ? "-" : "+") + new DecimalFormat(dfs.toString()).format(exp);
					s = s + "E" + (expneg ? "-" : "+") + newDF(dfs.toString()).format(exp);
				}
				return format(s, u.getLength(), true);
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				return REAL_DECIMAL.parse(u, s, options);
			}
		},

		/** The REAL_EXPONENT. */
		REAL_EXPONENT("E", false) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				String s = null;
				if (o != null) {
					Double d = o instanceof Double ? (Double) o : (Float) o;
					int exp = 0;
					final boolean neg = d < 0;
					if (neg) {
						d *= -1;
					}
					while (d > 1) {
						d /= 10;
						exp += 1;
					}
					while (d < .1) {
						d *= 10;
						exp -= 1;
					}
					final boolean expneg = exp < 0;
					if (expneg) {
						exp *= -1;
					}
					StringBuilder dfs = new StringBuilder();
					dfs.append("0.");
					for (int j = 0; j < u.getDecimalLength(); j++) {
						dfs.append('0');
					}
					//s = (neg ? '-' : "") + new DecimalFormat(dfs.toString()).format(d);
					s = (neg ? '-' : "") + newDF(dfs.toString()).format(d);
					dfs = new StringBuilder();
					for (int j = 0; j < u.getExponentLength(); j++) {
						dfs.append('0');
					}
					//s = s + 'E' + (expneg ? '-' : '+') + new DecimalFormat(dfs.toString()).format(exp);
					s = s + 'E' + (expneg ? '-' : '+') + newDF(dfs.toString()).format(exp);
				}
				return format(s, u.getLength(), true);
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				return REAL_DECIMAL.parse(u, s, options);
			}
		},

		/** The REAL_SCIENTIFIC. */
		REAL_SCIENTIFIC("ES", false) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				String s = null;
				if (o != null) {
					Double d = o instanceof Double ? (Double) o : (Float) o;
					int exp = 0;
					final boolean neg = d < 0;
					if (neg) {
						d *= -1;
					}
					while (d > 10) {
						d /= 10;
						exp += 1;
					}
					while (d < 1) {
						d *= 10;
						exp -= 1;
					}
					final boolean expneg = exp < 0;
					if (expneg) {
						exp *= -1;
					}
					StringBuilder dfs = new StringBuilder();
					dfs.append("0.");
					for (int j = 0; j < u.getDecimalLength(); j++) {
						dfs.append('0');
					}
					//s = (neg ? "-" : "") + new DecimalFormat(dfs.toString()).format(d);
					s = (neg ? "-" : "") + newDF(dfs.toString()).format(d);
					dfs = new StringBuilder();
					for (int j = 0; j < u.getExponentLength(); j++) {
						dfs.append('0');
					}
					//s = s + "E" + (expneg ? "-" : "+") + new DecimalFormat(dfs.toString()).format(exp);
					s = s + "E" + (expneg ? "-" : "+") + newDF(dfs.toString()).format(exp);
				}
				return format(s, u.getLength(), true);
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				return REAL_DECIMAL.parse(u, s, options);
			}
		},

		/** The BLANK_CONTROL_REMOVE. */
		BLANK_CONTROL_REMOVE("BN", true) {
			@Override
			public String format(final Unit u, final Object o, final Options options) throws IOException {
				throw new java.io.IOException("Ouput for the BN edit descriptor is not supported.");
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				// do nothing
				return null;
			}
		},

		/** The BLANK_CONTROL_ZEROS. */
		BLANK_CONTROL_ZEROS("BZ", true) {
			@Override
			public String format(final Unit u, final Object o, final Options options) throws IOException {
				throw new java.io.IOException("Ouput for the BZ edit descriptor is not supported.");
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				// do nothing
				return null;
			}
		},

		/** The FORMAT_SCANNING_CONTROL. */
		FORMAT_SCANNING_CONTROL(":", true) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				// Do nothing
				return "";
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				// never called
				return null;
			}
		},

		/** The POITIONING_HORIZONTAL. */
		POITIONING_HORIZONTAL("X", true) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				final StringBuilder sb = new StringBuilder();
				for (int j = 0; j < u.getLength(); j++) {
					sb.append(options.getPositioningChar());
				}
				return sb.toString();
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				// do nothing
				return null;
			}
		},

		/** The POSITIONIN g_ tab. */
		POSITIONING_TAB("T", true) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				// never called
				return null;
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				throw new java.io.IOException("Input for the T edit descriptor is not supported.");
			}
		},

		/** The POSITIONING_TAB_LEFT. */
		POSITIONING_TAB_LEFT("TL", true) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				// never called
				return null;
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				throw new java.io.IOException("Input for the TL edit descriptor is not supported.");
			}
		},

		/** The POSITIONING_TAB_RIGHT. */
		POSITIONING_TAB_RIGHT("TR", true) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				// never called
				return null;
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				throw new java.io.IOException("Input for the TR edit descriptor is not supported.");
			}
		},

		/** The POSITIONING_VERTICAL. */
		POSITIONING_VERTICAL("/", true) {
			@Override
			public String format(final Unit u, final Object o, final Options options) {
				return "\n";
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				// never called
				return null;
			}
		},

		/** The SIGN_CONTROL_COMPILER. */
		SIGN_CONTROL_COMPILER("S", true) {
			@Override
			public String format(final Unit u, final Object o, final Options options) throws IOException {
				throw new java.io.IOException("Ouput for the S edit descriptor is not supported.");
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				// do nothing
				return null;
			}
		},

		/** The SIGN_CONTROL_POSITIVE_ALWAYS. */
		SIGN_CONTROL_POSITIVE_ALWAYS("SP", true) {
			@Override
			public String format(final Unit u, final Object o, final Options options) throws IOException {
				throw new java.io.IOException("Ouput for the SP edit descriptor is not supported.");
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				// do nothing
				return null;
			}
		},

		/** The SIGN_CONTROL_POSITIVE_NEVER. */
		SIGN_CONTROL_POSITIVE_NEVER("SS", true) {
			@Override
			public String format(final Unit u, final Object o, final Options options) throws IOException {
				throw new java.io.IOException("Ouput for the SS edit descriptor is not supported.");
			}

			@Override
			public Object parse(final Unit u, final String s, final Options options) throws IOException {
				// do nothing
				return null;
			}
		};

		/** The tag. */
		private final String tag;

		/** If non-repeatable. */
		private final boolean nonRepeatable;

		/**
		 * Instantiates a new edits the descriptor.
		 * 
		 * @param tag
		 *            the edit descriptor tag
		 * @param nonRepeatable
		 *            whether the edit descriptor is non-repeatable or not
		 */
		private EditDescriptor(final String tag, final boolean nonRepeatable) {
			this.tag = tag;
			this.nonRepeatable = nonRepeatable;
		}

		/**
		 * Gets the tag.
		 * 
		 * @return the tag
		 */
		public String getTag() {
			return tag;
		}

		/**
		 * Formats the object.
		 * 
		 * @param u
		 *            the parent unit
		 * @param o
		 *            the object to be formatted
		 * @param options
		 *            the options
		 * 
		 * @return the formatted string
		 * 
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		public abstract String format(Unit u, Object o, Options options) throws IOException;

		/**
		 * Parses the object.
		 * 
		 * @param u
		 *            the parent unit
		 * @param s
		 *            the String to be parsed
		 * @param options
		 *            the options
		 * 
		 * @return the parsed object
		 * 
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		public abstract Object parse(Unit u, String s, Options options) throws IOException;

		/**
		 * Helper method to add spaces and right align content.
		 * 
		 * @param s
		 *            is the String to append
		 * @param length
		 *            is the desired length
		 * @param rightAligned
		 *            specifies if the content should be right-aligned
		 * 
		 * @return the formatted string
		 */
		protected String format(final String s, final int length, final boolean rightAligned) {
			final StringBuilder sb = new StringBuilder();
			if (s == null) {
				for (int i = 0; i < length; i++) {
					sb.append(' ');
				}
			} else if (length == -1) {
				sb.append(s);
			} else if (s.length() > length) {
				for (int i = 0; i < length; i++) {
					sb.append('*');
					//sb.append(s);
				}
			} else {
				final int dif = length - s.length();
				if (rightAligned) {
					for (int j = 0; j < dif; j++) {
						sb.append(' ');
					}
				}
				sb.append(s);
				if (!rightAligned) {
					for (int j = 0; j < dif; j++) {
						sb.append(' ');
					}
				}
			}
			return sb.toString();
		}

		/**
		 * Checks if is non-repeatable.
		 * 
		 * @return true, if is non-repeatable
		 */
		public boolean isNonRepeatable() {
			return nonRepeatable;
		}
	}

	/**
	 * The Class SpecificationStringInterpreter.
	 */
	protected static class SpecificationStringInterpreter {

		/** Cached strings along each step of the pre-processing. */
		private final String original, input, withoutParenthesis, multipliedOut, withCommas;

		/**
		 * Instantiates a new specification string interpreter.
		 * 
		 * @param s
		 *            the String to be pre-processed
		 * 
		 * @throws ParseException
		 *             the parse exception
		 */
		public SpecificationStringInterpreter(final String s) throws ParseException {
			if (s == null) { throw new NullPointerException("The format specification string may not be null."); }
			original = s;

			//check for malformatted root parenthesis
			final int open = s.indexOf('(');
			if (open == -1) { throw new ParseException(
					"Fortran format specification strings must begin with an open parenthesis '(' and end with a close parenthesis ')'. Blank spaces are tolerated before an open parenthesis and any characters are tolerated after a close parenthesis. No characters outside of the root parenthesis affect the format specification.",
					0); }
			final int close = findClosingParenthesis(s, open);
			final String before = s.substring(0, open);
			if (before.replaceAll(" ", "").length() != 0) { throw new ParseException("Only spaces may precede the root parenthesis.", 0); }

			input = s.substring(open + 1, close).replaceAll(" ", "");
			withCommas = checkCommas(input);
			multipliedOut = multiplyOut(withCommas);
			withoutParenthesis = removeParenthesis(multipliedOut);
		}

		/**
		 * Adds the commas to the correct places.
		 * 
		 * @param input
		 *            the input
		 * 
		 * @return the string
		 */
		protected final String checkCommas(final String input) {
			final StringBuilder sb = new StringBuilder();
			boolean hitE = false;
			boolean lastWasChar = true;
			boolean foundNotNum = false;
			for (int i = 0; i < input.length(); i++) {
				final char c = input.charAt(i);
				if (c == '(' || c == ')' || c == ',') {
					//skip over
					sb.append(c);
				} else if (c == EditDescriptor.POITIONING_HORIZONTAL.getTag().charAt(0)) {
					sb.append(c);
					if (i != input.length() - 1 && input.charAt(i + 1) != ')' && input.charAt(i + 1) != ',') {
						sb.append(',');
						lastWasChar = true;
					}
				} else if (c == '.' || Character.isDigit(c)) {
					sb.append(c);
					lastWasChar = false;
					if (i != 0 && input.charAt(i - 1) == ',') {
						foundNotNum = false;
					}
				} else {
					if (foundNotNum && !lastWasChar && i != 0 && sb.charAt(sb.length() - 1) != ',' && !(c == EditDescriptor.REAL_EXPONENT.getTag().charAt(0) && hitE)) {
						sb.append(',');
						hitE = false;
					}
					if (c == EditDescriptor.REAL_EXPONENT.getTag().charAt(0)) {
						hitE = true;
					}
					foundNotNum = true;
					lastWasChar = true;
					sb.append(c);
					if (c == '/') {
						sb.append(',');
					}
				}
			}
			return sb.toString();
		}

		/**
		 * Multiplies out compound descriptors.
		 * 
		 * @param input
		 *            the input
		 * 
		 * @return the string
		 * 
		 * @throws ParseException
		 *             the parse exception
		 */
		protected final String multiplyOut(final String input) throws ParseException {
			final StringBuilder sb = new StringBuilder();
			final StringBuilder current = new StringBuilder();
			final StringBuilder number = new StringBuilder();
			int multiplier = 1;
			for (int i = 0; i < input.length(); i++) {
				final char c = input.charAt(i);
				if (c == '(') {
					if (number.length() > 0) {
						multiplier = Integer.parseInt(number.toString());
					}
					if (current.length() > 0) {
						for (int j = 0; j < multiplier; j++) {
							sb.append(current.toString());
						}
						current.delete(0, current.length());
						number.delete(0, number.length());
					}
					final int closing = findClosingParenthesis(input, i);
					final String center = multiplyOut(input.substring(i + 1, closing));
					for (int j = 0; j < multiplier; j++) {
						sb.append('(');
						sb.append(center);
						sb.append(')');
					}
					i = closing;
					multiplier = 1;
					current.delete(0, current.length());
					number.delete(0, number.length());
				} else if (c == ',') {
					for (int j = 0; j < multiplier; j++) {
						sb.append(current.toString());
						sb.append(',');
					}
					multiplier = 1;
					current.delete(0, current.length());
				} else if (Character.isDigit(c) && current.length() == 0) {
					number.append(c);
				} else {
					if (c == EditDescriptor.POITIONING_HORIZONTAL.getTag().charAt(0)) {
						sb.append(number);
						number.delete(0, number.length());
						number.append('1');
					}
					if (number.length() > 0) {
						multiplier = Integer.parseInt(number.toString());
						number.delete(0, number.length());
					}
					current.append(c);
				}
			}
			if (current.length() > 0) {
				for (int j = 0; j < multiplier; j++) {
					sb.append(current.toString());
					if (j != multiplier - 1) {
						sb.append(',');
					}
				}
			}
			return sb.toString();
		}

		/**
		 * Removes the parenthesis from the specification string.
		 * 
		 * @param input
		 *            the input
		 * 
		 * @return the string
		 * 
		 * @throws ParseException
		 *             the parse exception
		 */
		protected final String removeParenthesis(final String input) throws ParseException {
			final StringBuilder sb = new StringBuilder();
			boolean hitParenthesis = false;
			for (int i = 0; i < input.length(); i++) {
				final char c = input.charAt(i);
				if (c == '(' || c == ')') {
					hitParenthesis = true;
				} else {
					if (hitParenthesis && sb.length() != 0 && sb.charAt(sb.length() - 1) != ',') {
						sb.append(',');
					}
					hitParenthesis = false;
					if (c != ',' || sb.charAt(sb.length() - 1) != ',') {
						sb.append(c);
					}
				}
			}
			return sb.toString();
		}

		/**
		 * Find the closing parenthesis to a given open parenthesis in a string.
		 * 
		 * @param withParen
		 *            is the String containing the open parenthesis in question.
		 * @param open
		 *            is the index of the open parenthesis
		 * 
		 * @return the index of the corresponding close parenthesis
		 * 
		 * @throws ParseException
		 *             the parse exception
		 */
		private final int findClosingParenthesis(final String withParen, final int open) throws ParseException {
			final Stack<Integer> s = new Stack<Integer>();
			for (int i = open + 1; i < withParen.length(); i++) {
				final char c = withParen.charAt(i);
				switch (c) {
				case ')':
					if (s.isEmpty()) {
						return i;
					} else {
						s.pop();
					}
					break;
				case '(':
					s.push(i);
					break;
				}
			}
			throw new ParseException("Missing a close parenthesis.", open);
		}

		/**
		 * Parses the format specification string after pre-processing.
		 * 
		 * @return the ArrayList of Units that correspond to the format
		 * 
		 * @throws ParseException
		 *             the parse exception
		 */
		public final ArrayList<Unit> getUnits() throws ParseException {
			final StringTokenizer st = new StringTokenizer(getCompletedInterpretation(), ",");
			final ArrayList<Unit> units = new ArrayList<Unit>(st.countTokens());
			while (st.hasMoreTokens()) {
				final String s = st.nextToken();
				boolean reachedType = false, hasDecimal = false, hasExponent = false;
				final StringBuilder before = new StringBuilder(), type = new StringBuilder(), decimal = new StringBuilder(), exponent = new StringBuilder();
				StringBuilder after = new StringBuilder();
				for (int i = 0; i < s.length(); i++) {
					if (s.charAt(i) == '.') {
						hasDecimal = true;
					} else if (reachedType && s.charAt(i) == 'E') {
						hasExponent = true;
					} else if (Character.isLetter(s.charAt(i)) || s.charAt(i) == '/') {
						type.append(s.charAt(i));
						reachedType = true;
					} else {
						if (hasExponent) {
							exponent.append(s.charAt(i));
						} else if (hasDecimal) {
							decimal.append(s.charAt(i));
						} else if (reachedType) {
							after.append(s.charAt(i));
						} else {
							before.append(s.charAt(i));
						}
					}
				}
				int repeats = before.length() == 0 ? 1 : Integer.parseInt(before.toString());
				if (type.toString().equals(EditDescriptor.POITIONING_HORIZONTAL.getTag())) {
					after = before;
					repeats = 1;
				}
				if (type.toString().equals(EditDescriptor.REAL_EXPONENT.getTag()) && exponent.length() == 0) {
					exponent.append('2');
				}
				for (int i = 0; i < repeats; i++) {
					if (!DESCRIPTOR_HASH.containsKey(type.toString())) { throw new ParseException("Unsupported Edit Descriptor: " + type.toString(), original.indexOf(type.toString())); }
					final Unit u = new Unit(DESCRIPTOR_HASH.get(type.toString()), after.length() == 0 ? 0 : Integer.parseInt(after.toString()));
					if (decimal.length() != 0) {
						u.decimalLength = Integer.parseInt(decimal.toString());
					}
					if (exponent.length() != 0) {
						u.exponentLength = Integer.parseInt(exponent.toString());
					}
					units.add(u);
				}
			}
			return units;
		}

		/**
		 * Gets the completed interpretation.
		 * 
		 * @return the completed interpretation
		 */
		public String getCompletedInterpretation() {
			return withoutParenthesis;
		}

	}

	/**
	 * The Class Unit. Holds a single Edit Descriptor.
	 */
	private static class Unit {

		/** The Edit Descriptor type. */
		private final EditDescriptor type;

		/** The length 'w'. */
		private final int length;

		/** The decimal length 'd'. */
		private int decimalLength;

		/** The exponent length 'e'. */
		private int exponentLength;

		/**
		 * Instantiates a new unit.
		 * 
		 * @param type
		 *            the type
		 * @param length
		 *            the length 'w'
		 */
		public Unit(final EditDescriptor type, final int length) {
			this.type = type;
			this.length = length;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return type.getTag() + length + (decimalLength > 0 ? "." + decimalLength : "") + (exponentLength > 0 ? "E" + exponentLength : "") + " ";
		}

		/**
		 * Gets the type.
		 * 
		 * @return the EditDescriptor type
		 */
		public EditDescriptor getType() {
			return type;
		}

		/**
		 * Gets the length.
		 * 
		 * @return the length
		 */
		public int getLength() {
			return length;
		}

		/**
		 * Gets the decimal length.
		 * 
		 * @return the decimal length
		 */
		public int getDecimalLength() {
			return decimalLength;
		}

		/**
		 * Gets the exponent length.
		 * 
		 * @return the exponent length
		 */
		public int getExponentLength() {
			return exponentLength;
		}

	}

	/**
	 * The Class Options.
	 */
	public static class Options {

		/** The char to use when skipping positions during write. */
		private char positioningChar = ' ';

		/**
		 * Use this to set whether or not to append a return line to the end of
		 * the generated string during write.
		 */
		private boolean addReturn = false;

		/**
		 * Use this to choose whether to return decimals as Float or Double
		 * objects.
		 */
		private boolean returnFloats = false;

		/**
		 * Use this to choose whether to return zero if a blank is read for
		 * numbers.
		 */
		private boolean returnZeroForBlanks = false;

		/** Use this to choose whether character strings are left aligned. */
		//bic private boolean leftAlignCharacters = false;
		private boolean leftAlignCharacters = true;

		/**
		 * Gets the positioning char. This is the character to use when skipping
		 * spaces during write.
		 * 
		 * @return the positioning char
		 */
		public char getPositioningChar() {
			return positioningChar;
		}

		/**
		 * Sets the positioning char. This is the character to use when skipping
		 * spaces during write.
		 * 
		 * @param positioningChar
		 *            the new positioning character
		 */
		public void setPositioningChar(final char positioningChar) {
			this.positioningChar = positioningChar;
		}

		/**
		 * Specifies whether or not to add a new line at the end of a line
		 * during write.
		 * 
		 * @param addReturn
		 *            the new return line behavior
		 */
		public void setAddReturn(final boolean addReturn) {
			this.addReturn = addReturn;
		}

		/**
		 * Checks if returns are added at the end of lines during write.
		 * 
		 * @return true, if is if new lines are added at the end of lines during
		 *         write
		 */
		public boolean isAddReturn() {
			return addReturn;
		}

		/**
		 * Checks if floats are returned instead of doubles.
		 * 
		 * @return true, if floats are returned
		 */
		public boolean isReturnFloats() {
			return returnFloats;
		}

		/**
		 * Set whether floats are returned instead of doubles.
		 * 
		 * @param returnFloats
		 *            the return floats
		 */
		public void setReturnFloats(final boolean returnFloats) {
			this.returnFloats = returnFloats;
		}

		/**
		 * Checks if zeros are returned for blanks.
		 * 
		 * @return true, if zeros are returned
		 */
		public boolean isReturnZeroForBlanks() {
			return returnZeroForBlanks;
		}

		/**
		 * Set whether zeros are returned for blanks.
		 * 
		 * @param returnZeroForBlanks
		 *            the return zero for blanks
		 */
		public void setReturnZeroForBlanks(final boolean returnZeroForBlanks) {
			this.returnZeroForBlanks = returnZeroForBlanks;
		}

		/**
		 * Checks if characters are left aligned.
		 * 
		 * @return true, if characters are left aligned
		 */
		public boolean isLeftAlignCharacters() {
			return leftAlignCharacters;
		}

		/**
		 * Set whether characters are left aligned.
		 * 
		 * @param leftAlignCharacters
		 *            the left align characters
		 */
		public void setLeftAlignCharacters(final boolean leftAlignCharacters) {
			this.leftAlignCharacters = leftAlignCharacters;
		}

	}

	/**
	 * Static read function similar to Fortran implementation.
	 * 
	 * @param data
	 *            is the data to be parsed
	 * @param format
	 *            is the format specification
	 * 
	 * @return a ArrayList<object> of all the parsed data as Java objects
	 * 
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static ArrayList<Object> read(final String data, final String format) throws ParseException, IOException {
		final FortranFormat ff = new FortranFormat(format);
		return ff.parse(data);
	}

	/**
	 * Static write function similar to the Fortran implementation.
	 * 
	 * @param objects
	 *            is the vector of objects to be formatted
	 * @param format
	 *            is the format specification
	 * 
	 * @return the formatted string
	 * 
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String write(final ArrayList<Object> objects, final String format) throws ParseException, IOException {
		final FortranFormat ff = new FortranFormat(format);
		return ff.format(objects);
	}

	/** The parsed Edit Descriptors. */
	private final ArrayList<Unit> units;

	/** The options. */
	private final Options options = new Options();

	/**
	 * Instantiates a new FortranFormat object.
	 * 
	 * @param specificationString
	 *            is the format specification string
	 * 
	 * @throws ParseException
	 *             the parse exception
	 */
	public FortranFormat(final String specificationString) throws ParseException {
		units = new SpecificationStringInterpreter(specificationString).getUnits();
	}

	/**
	 * Parses the input.
	 * 
	 * @param s
	 *            is the input string
	 * 
	 * @return a ArrayList<Object> of all the parsed data as Java Objects
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ArrayList<Object> parse(final String s) throws IOException {
		final StringTokenizer st = new StringTokenizer(s, "\n");
		final ArrayList<Object> returning = new ArrayList<Object>(units.size());
		StringReader sr = new StringReader(st.hasMoreTokens()?st.nextToken():"");
		for (final Unit u : units) {
			final char[] chars = new char[u.length];
			sr.read(chars, 0, u.length);
			final StringBuilder sb = new StringBuilder(chars.length);
			for (final char c : chars) {
				if ((u.type == EditDescriptor.CHARACTER || c != ' ') && c != 0) {
					sb.append(c);
				}
			}
			final String complete = sb.toString();
			if (u.type == EditDescriptor.FORMAT_SCANNING_CONTROL) {
				break;
			} else if (u.type == EditDescriptor.POSITIONING_VERTICAL) {
				sr = new StringReader(st.hasMoreTokens()?st.nextToken():"");
			} else {
				if (!u.type.isNonRepeatable()) {
					returning.add(u.type.parse(u, complete, options));
				}
			}
		}
		return returning;
	}

	/**
	 * Formats the given object.
	 * 
	 * @param object
	 *            is the Java Object to be formatted
	 * 
	 * @return the formatted string
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String format(final Object object) throws IOException {
		final ArrayList<Object> input = new ArrayList<Object>(1);
		input.add(object);
		return format(input);
	}

	/**
	 * Formats the given objects.
	 * 
	 * @param objects
	 *            are the Java Objects to be formatted
	 * 
	 * @return the formatted string
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String format(final ArrayList<Object> objects) throws IOException {
		int minus = 0;
		StringBuilder sb = new StringBuilder();
		int place = -1;
		StringBuilder save = null;
		for (int i = 0; i < objects.size() + minus; i++) {
			final Unit u = units.get(i);
			final Object o = objects.get(i - minus);
			if (u.type == EditDescriptor.POSITIONING_TAB || u.type == EditDescriptor.POSITIONING_TAB_LEFT || u.type == EditDescriptor.POSITIONING_TAB_RIGHT) {
				if (save == null) {
					save = sb;
				} else {
					while (place - 1 + sb.length() > save.length()) {
						save.append(' ');
					}
					save.replace(place - 1, place - 1 + sb.length(), sb.toString());
				}
				switch (u.type) {
				case POSITIONING_TAB:
					place = u.length;
					break;
				case POSITIONING_TAB_LEFT:
					place -= u.length - sb.length();
					break;
				case POSITIONING_TAB_RIGHT:
					place += u.length + sb.length();
					break;
				}
				sb = new StringBuilder();
			} else {
				sb.append(u.type.format(u, o, options));
			}
			if (u.type.isNonRepeatable()) {
				minus++;
			}
		}
		if (save != null) {
			while (place - 1 + sb.length() > save.length()) {
				save.append(' ');
			}
			save.replace(place - 1, place - 1 + sb.length(), sb.toString());
			sb = save;
			save = null;
			place = -1;
		}
		if (options.isAddReturn()) {
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Gets the Options object.
	 * 
	 * @return the options object
	 */
	public Options getOptions() {
		return options;
	}
	
	
	public static DecimalFormat newDF_(String formatString){
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.UK);
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(','); 
		DecimalFormat df = new DecimalFormat(formatString, otherSymbols);
		return df;
	}

	
	public static DecimalFormat newDF___(String formatString){
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(' '); 
		DecimalFormat df = new DecimalFormat(formatString, otherSymbols);
		return df;
	}

	public static DecimalFormat newDF(String formatString){
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(' '); 
		//DecimalFormat df = new DecimalFormat("####.####",otherSymbols);
		DecimalFormat df = new DecimalFormat("########.########",otherSymbols);
		return df;
	}


}
