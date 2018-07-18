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

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    IAB mIAB = null;
    DevPurchaseView mDevPurchaseView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        DebugUtil.Log.d( TAG, "onCreate" );

        {
            // check billing permission
            {
                final PackageManager pm = this.getPackageManager();
                if ( null != pm )
                {
                    boolean haveBilling = false;
                    try
                    {
                        final PackageInfo info = pm.getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
                        if ( null != info )
                        {
                            if ( null != info.requestedPermissions )
                            {
                                for ( final String name : info.requestedPermissions )
                                {
                                    if ( 0 == "com.android.vending.BILLING".compareTo(name) )
                                    {
                                        haveBilling = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    catch ( PackageManager.NameNotFoundException e )
                    {
                        DebugUtil.Log.e( TAG, "Exception", e );
                    }
                    if ( !haveBilling )
                    {
                        DebugUtil.Log.e(TAG, "must add AndroidManifest.xml\n<uses-permission android:name=\"com.android.vending.BILLING\" />" );
                    }
                }
            }

            // check getaccount permission
            {
                final PackageManager pm = this.getPackageManager();
                if ( null != pm )
                {
                    boolean haveGetAccount = false;
                    try
                    {
                        final PackageInfo info = pm.getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
                        if ( null != info )
                        {
                            if ( null != info.requestedPermissions )
                            {
                                for ( final String name : info.requestedPermissions )
                                {
                                    if ( 0 == "android.permission.GET_ACCOUNTS".compareTo(name) )
                                    {
                                        haveGetAccount = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    catch ( PackageManager.NameNotFoundException e )
                    {
                        DebugUtil.Log.e( TAG, "Exception", e );
                    }
                    if ( !haveGetAccount )
                    {
                        //DebugUtil.Log.e(TAG, "must add AndroidManifest.xml\n<uses-permission android:name=\"android.permission.GET_ACCOUNTS\" />" );
                    }
                    else
                    {
                        final android.accounts.AccountManager am = android.accounts.AccountManager.get( this.getApplicationContext() );
                        if ( null != am )
                        {
                            final android.accounts.Account[] accounts = am.getAccounts();
                            if ( null != accounts )
                            {
                                for ( final android.accounts.Account account : accounts )
                                {
                                    final String name = account.name;
                                    final String type = account.type;
                                    final int hashCode = account.hashCode();
                                    DebugUtil.Log.d( TAG, " name=" + name );
                                    DebugUtil.Log.d( TAG, " type=" + type );
                                    DebugUtil.Log.d( TAG, " hash=" + hashCode );
                                }
                            }
                        }
                    }
                }
            }
        }

        mIAB = new IAB( this );

        mDevPurchaseView = new DevPurchaseView( this, mIAB );
    }

    @Override
    protected void onDestroy()
    {
        if ( null != mIAB )
        {
            mIAB.dispose();
        }
        if ( null != mDevPurchaseView )
        {
            mDevPurchaseView.dispose( this );
        }
        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if ( null != mIAB )
        {
            mIAB.queryPurchases();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if ( null != mIAB )
        {
            if ( mIAB.onActivityResult( requestCode, resultCode, data ) )
            {
                return;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
