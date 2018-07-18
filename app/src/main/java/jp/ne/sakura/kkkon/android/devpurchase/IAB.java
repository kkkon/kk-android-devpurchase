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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kkkon on 2018/07/15.
 */

public class IAB
{
    private static final String TAG = "IAB";

    Context     mContextApp = null;
    String      mPackageName = null;

    ServiceConnection       mServiceConn = null;
    IInAppBillingService    mService = null;

    private static final int    RESULT_OK = 0;
    private static final int    RESULT_USER_CANCELED = 1;
    private static final int    RESULT_SERVICE_UNAVAILABLE = 2;
    private static final int    RESULT_BILLING_UNAVAILABLE = 3;
    private static final int    RESULT_ITEM_UNAVAILABLE = 4;
    private static final int    RESULT_DEVELOPER_ERROR = 5;
    private static final int    RESULT_ERROR = 6;
    private static final int    RESULT_ITEM_ALREADY_OWNED = 7;
    private static final int    RESULT_ITEM_NOT_OWNED = 8;




    @android.support.annotation.RequiresPermission("com.android.vending.BILLING")
    public IAB( final Context context )
    {
        mContextApp = context.getApplicationContext();
        mPackageName = context.getPackageName();

        mServiceConn = new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service)
            {
                DebugUtil.Log.d( TAG, "ServiceConnection#onServiceConnected" );
                mService = IInAppBillingService.Stub.asInterface( service );
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName)
            {
                DebugUtil.Log.d( TAG, "ServiceConnection#onServiceDisconnected" );
                mService = null;
            }
        };
        final Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        final boolean isBinded = mContextApp.bindService( serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE );
        if ( false == isBinded )
        {
            DebugUtil.Log.d( TAG, "bindService failed" );
        }
    }

    public void dispose()
    {
        if ( null != mService )
        {
            if ( null != mContextApp )
            {
                mContextApp.unbindService( mServiceConn );
                mServiceConn = null;
            }
        }

        mContextApp = null;
        mPackageName = null;
    }

    public void querySku(final ArrayList<String> queryList )
    {
        if ( null == mService )
        {
            return;
        }

        (new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final Bundle querySkus = new Bundle();
                querySkus.putStringArrayList( "ITEM_ID_LIST", queryList );

                try
                {
                    final Bundle skuDetails = mService.getSkuDetails(3, mPackageName, "inapp", querySkus);
                    if ( null != skuDetails )
                    {
                        final int response = skuDetails.getInt("RESPONSE_CODE");
                        DebugUtil.Log.d( TAG, "getSkuDetails response:" + response );
                        if ( RESULT_OK == response )
                        {
                            final ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                            for ( final String json : responseList )
                            {
                                try
                                {
                                    final JSONObject object = new JSONObject(json);
                                    DebugUtil.Log.e( TAG, "  sku:" + object );
                                }
                                catch ( JSONException e )
                                {
                                    DebugUtil.Log.e( TAG, "", e );
                                }
                            }
                        }
                    }
                }
                catch ( RemoteException e )
                {
                    DebugUtil.Log.e( TAG, "", e );
                }
                catch ( Exception e )
                {
                    DebugUtil.Log.e( TAG, "", e );
                }

            }
        })).start();
    }

    public void purchase( final Activity activity, final String sku )
    {
        if ( null == mService )
        {
            return;
        }

        try
        {
            final String developerPayload = "";
            final Bundle buyIntentBundle = mService.getBuyIntent(3, mPackageName, sku, "inapp", developerPayload);
            if ( null != buyIntentBundle )
            {
                final int response = buyIntentBundle.getInt("RESPONSE_CODE");
                DebugUtil.Log.d( TAG, "getBuyIntent response:" + response );
                if ( RESULT_OK == response )
                {
                    final PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    try
                    {
                        activity.startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
                    }
                    catch ( IntentSender.SendIntentException e )
                    {
                        DebugUtil.Log.e( TAG, "", e );
                    }
                }
            }

        }
        catch ( RemoteException e )
        {
            DebugUtil.Log.e( TAG, "", e );
        }
        catch ( Exception e )
        {
            DebugUtil.Log.e( TAG, "", e );
        }
    }

    public boolean onActivityResult( int requestCode, int resultCode, Intent data )
    {
        if ( 1001 != requestCode )
        {
            return false;
        }

        if ( null == data )
        {
            return true;
        }
        final int response = data.getIntExtra("RESPONSE_CODE", RESULT_ERROR );
        DebugUtil.Log.d( TAG, "onActivityResult resultCode=" + resultCode + ",response:" + response );
        if ( Activity.RESULT_OK == resultCode && RESULT_OK == response )
        {
            final String purchaseData = data.getStringExtra( "INAPP_PURCHASE_DATA" );
            final String signatureData = data.getStringExtra( "INAPP_DATA_SIGNATURE" );
            DebugUtil.Log.d( TAG, "onActivityResult purchaseData:" + purchaseData );
            DebugUtil.Log.d( TAG, "onActivityResult signatureData:" + signatureData );
            DebugUtil.Log.d( TAG, "onActivityResult extras:" + data.getExtras() );

        }

        return true;
    }

    public static class Purchase
    {
        public String   purchaseData;
        public String   signatureData;

        public String   purchaseToken;

        public Purchase(String purchaseData, String signatureData)
        {
            this.purchaseData = purchaseData;
            this.signatureData = signatureData;

            try
            {
                JSONObject object = new JSONObject(purchaseData);
                this.purchaseToken = object.optString( "token", object.optString("purchaseToken") );

            }
            catch ( JSONException e )
            {
                DebugUtil.Log.e( TAG, "", e );
            }
        }
    }

    List<Purchase>     mPurchases = new ArrayList<Purchase>();

    public Purchase getPurchase()
    {
        if ( 0 < mPurchases.size() )
        {
            return mPurchases.get(0);
        }
        return null;
    }


    public void queryPurchases()
    {
        if ( null == mService )
        {
            return;
        }

        (new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                mPurchases.clear();

                String continueToken = null;
                do
                {

                    try
                    {
                        final Bundle ownedItems = mService.getPurchases(3, mPackageName, "inapp", continueToken );
                        if ( null != ownedItems )
                        {
                            final int response = ownedItems.getInt("RESPONSE_CODE");
                            DebugUtil.Log.d( TAG, "getPurchases response:" + response );
                            if ( RESULT_OK == response )
                            {
                                final ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                                final ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                                final ArrayList<String> signatureDataList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                                continueToken = ownedItems.getString("CONTINUATION_TOKEN");
                                for ( int index = 0; index < purchaseDataList.size(); ++index )
                                {
                                    final String sku = ownedSkus.get( index );
                                    final String purchaseData = purchaseDataList.get( index );
                                    final String signatureData = signatureDataList.get( index );
                                    DebugUtil.Log.d( TAG, "getPurchases   sku=" + sku );
                                    DebugUtil.Log.d( TAG, "getPurchases   purchase=" + purchaseData );
                                    DebugUtil.Log.d( TAG, "getPurchases   signature=" + signatureData );
                                    {
                                        final Purchase purchase = new Purchase( purchaseData, signatureData );
                                        mPurchases.add( purchase );
                                    }
                                    {
                                        try
                                        {
                                            final JSONObject object = new JSONObject(purchaseData);
                                            DebugUtil.Log.e( TAG, "  purchase:" + object );
                                        }
                                        catch ( JSONException e )
                                        {
                                            DebugUtil.Log.e( TAG, "", e );
                                        }
                                    }
                                }
                            }
                        }
                    }
                    catch ( RemoteException e )
                    {
                        DebugUtil.Log.e( TAG, "", e );
                    }
                    catch ( Exception e )
                    {
                        DebugUtil.Log.e( TAG, "", e );
                    }

                } while ( null != continueToken );

            }
        })).start();
    }

    public void consume( final String purchaseData )
    {
        if ( null == mService )
        {
            return;
        }

        try
        {
            final int response = mService.consumePurchase(3, mPackageName, purchaseData );
            DebugUtil.Log.d( TAG, "consumePurchase response:" + response );
            if ( RESULT_OK == response )
            {
//                for ( final Purchase purchase : mPurchases )
//                {
//                    if ( purchase.purchaseToken.equals(purchaseData) )
//                    {
//                        mPurchases.remove( purchase );
//                        break;
//                    }
//                }
                //mPurchases.remove();
            }

        }
        catch ( RemoteException e )
        {
            DebugUtil.Log.e( TAG, "", e );
        }
        catch ( Exception e )
        {
            DebugUtil.Log.e( TAG, "", e );
        }
    }


}
