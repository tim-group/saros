/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.util;

/**
 * Utility to convert byte to string and vice versa
 * <p/>
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.20
 * Time: 11.10
 */

public class ByteHexUtil
{
    /**
     * Method returns char[] representation of byte b
     */
    private static char[] byteToHex(byte b)
    {

        char hexDigit[] = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
                'e', 'f'
        };
        char[] array = {hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f]};

        return array;
    }

    /**
     * @param bytes
     * @return
     */
    public static String byteToHex(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
        {
            sb.append(byteToHex(b));
        }

        return sb.toString();
    }

    /**
     * @param sHex
     * @return
     */
    public static byte[] hexToByte(String sHex)
    {

        int length = sHex.length();
        if ((length % 2) != 0)
        {
            return null;
        }


        byte[] b;
        b = new byte[length / 2];
        for (int i = 0; i < length; i = i + 2)
        {
            String sByte = sHex.substring(i, i + 2);

            int iByte = Integer.parseInt(sByte, 16);

            if (iByte >= 128)
            {
                iByte = iByte - 256;
            }

            b[i / 2] = (byte) iByte;
        }

        return b;
    }

}
