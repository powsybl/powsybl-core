/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag.network.io;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RecordWriter {

    public static final String NEWLINE = System.getProperty("line.separator");

    public static final Locale LOCALE = new Locale("en", "US");

    public enum Justification {
        Right,
        Left
    }

    private final Writer writer;

    private int mCurrentLinePos = 1;

    public RecordWriter(Writer writer) {
        this.writer = writer;
    }

    private String format(float aValue, int digit) {
        String val = Float.isNaN(aValue) ? ""   :
                     //...null value will be replaced by "0."
                     aValue == 0.f       ? "0." :
                     //...23.0000 will be replaced by "23."
                     aValue % 1 == 0.f   ? String.format(LOCALE, "%d.", (int)aValue)
                     //...format float on n digit (right justification)
                     : String.format(LOCALE, "%-" + digit + "f",aValue);

        //...truncate the string if the length is greater than digit+1
        if ( val.length() > digit + 1 ) {
            val = val.substring(0, digit + 1);
        }

        if ( val.contains(".")) {
            while( val.endsWith("0")) {
                val = val.substring(0, val.length()-1);
            }
        }

        return val;
    }

    public void addValue( float aValue, int aColStart, int aColEnd) throws IOException
    {
        String key = format(aValue, aColEnd - aColStart);
        this.addValue( key, aColStart, aColEnd, Justification.Right);
    }

    public void addValue( int aValue, int aColStart, int aColEnd) throws IOException
    {
        String key = Integer.toString(aValue);
        this.addValue( key, aColStart, aColEnd, Justification.Right);
    }

    public void addValue( String aKey, int aColStart, int aColEnd) throws IOException
    {
        this.addValue(aKey, aColStart, aColEnd, Justification.Left);
    }

    public void addValue( char aKey, int aColStart) throws IOException
    {
        this.addValue(Character.toString(aKey), aColStart, aColStart, Justification.Left);
    }

    public void addValue( char aKey, int aColStart, int aColEnd) throws IOException
    {
        this.addValue(Character.toString(aKey), aColStart, aColEnd, Justification.Left);
    }

    public void addValue( String aKey, int aColStart) throws IOException
    {
        this.addValue(aKey, aColStart, aColStart + aKey.length()-1);
    }

    public void addValue( String aKey, int aColStart, int aColEnd, Justification aJust) throws IOException
    {
        if ( aColEnd < aColStart ) {
            throw new RuntimeException("Bad record encoding for " + aKey);
        }
        int size = 1 + aColEnd - aColStart;

        //...add blank before the next value
        if (aColStart > mCurrentLinePos )
        {
            int blanknumber = aColStart - mCurrentLinePos;
            writer.append(String.format(LOCALE, "%" + blanknumber + "s", ""));
            mCurrentLinePos = aColStart;
        }
        mCurrentLinePos += size;

        if ( aJust == Justification.Left )
        {
            writer.append(String.format(LOCALE, "%-" + size + "s" , aKey));
        }
        else
        {
            writer.append(String.format(LOCALE, "%" + size + "s" , aKey));
        }
    }

    /**
     * Add anew line at the end of the current record line
     */
    public void newLine() throws IOException
    {
        mCurrentLinePos = 1;
        writer.append(NEWLINE);
    }
}
