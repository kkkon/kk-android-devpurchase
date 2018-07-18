/*
 * The MIT License
 *
 * Copyright 2018 kkkon.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jp.ne.sakura.kkkon.android.devpurchase;

/**
 * Created by kkkon on 2018/07/14.
 */

public class DebugUtil
{
    public static final boolean IS_DEBUG = true;//BuildConfig.DEBUG;

    public static class Log
    {
        public static void d( String tag, String message )
        {
            if ( IS_DEBUG )
            {
                android.util.Log.d( tag, message );
            }
        }
        public static void d( String tag, String message, Throwable th )
        {
            if ( IS_DEBUG )
            {
                android.util.Log.d( tag, message, th );
            }
        }

        public static void e( String tag, String message )
        {
            if ( IS_DEBUG )
            {
                android.util.Log.e( tag, message );
            }
        }
        public static void e( String tag, String message, Throwable tr )
        {
            if ( IS_DEBUG )
            {
                android.util.Log.e( tag, message, tr );
            }
        }
    }

}
