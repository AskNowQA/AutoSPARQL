package org.aksw.autosparql.tbsl.gui.vaadin.util;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

/**
 *
 * @author Joe Mill (posted at http://stackoverflow.com/questions/607176/java-equivalent-to-javascripts-encodeuricomponent-that-produces-identical-outpu)
 *
 */
public final class EscapeUtils
{
    /** used for the encodeURIComponent function */
    private static final BitSet dontNeedEncoding;

    static
    {
        dontNeedEncoding = new BitSet(256);

        // a-z
        for (int i = 97; i <= 122; ++i)
        {
            dontNeedEncoding.set(i);
        }
        // A-Z
        for (int i = 65; i <= 90; ++i)
        {
            dontNeedEncoding.set(i);
        }
        // 0-9
        for (int i = 48; i <= 57; ++i)
        {
            dontNeedEncoding.set(i);
        }

        // '()*
        for (int i = 39; i <= 42; ++i)
        {
            dontNeedEncoding.set(i);
        }
        dontNeedEncoding.set(33); // !
        dontNeedEncoding.set(45); // -
        dontNeedEncoding.set(46); // .
        dontNeedEncoding.set(95); // _
        dontNeedEncoding.set(126); // ~
    }

    /**
     * A Utility class should not be instantiated.
     */
    private EscapeUtils()
    {

    }

    /**
     * Escapes all characters except the following: alphabetic, decimal digits, - _ . ! ~ * ' ( )
     *
     * @param input
     *            A component of a URI
     * @return the escaped URI component
     */
    public static String encodeURIComponent(String input)
    {
        if (input == null)
        {
            return input;
        }

        StringBuilder filtered = new StringBuilder(input.length());
        char c;
        for (int i = 0; i < input.length(); ++i)
        {
            c = input.charAt(i);
            if (dontNeedEncoding.get(c))
            {
                filtered.append(c);
            }
            else
            {
                final byte[] b = charToBytesUTF(c);

                for (int j = 0; j < b.length; ++j)
                {
                    filtered.append('%');
                    filtered.append("0123456789ABCDEF".charAt(b[j] >> 4 & 0xF));
                    filtered.append("0123456789ABCDEF".charAt(b[j] & 0xF));
                }
            }
        }
        return filtered.toString();
    }

    private static byte[] charToBytesUTF(char c)
    {
        try
        {
            return new String(new char[] { c }).getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return new byte[] { (byte) c };
        }
    }
}
