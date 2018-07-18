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
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by kkkon on 2018/07/15.
 */

public class DevPurchaseView
    implements View.OnClickListener
    , ListView.OnItemClickListener
{
    private static final String TAG = "DevPurchaseView";

    IAB         mIAB = null;

    WeakReference<Activity> mActivityRef = null;
    Context     mContextApp = null;
    LinearLayout mLayout = null;
    Button      mButton_QuerySku = null;
    ListView            mListView = null;
    ArrayList<String>   mListSku = new ArrayList<String>();
    Button      mButton_Purchases = null;
    Button      mButton_Consume = null;

    public DevPurchaseView( final Activity activity, final IAB iab )
    {
        mIAB = iab;

        mActivityRef = new WeakReference<Activity>(activity);
        mContextApp = activity.getApplicationContext();

        LinearLayout layout = new LinearLayout(mContextApp);
        layout.setOrientation( LinearLayout.VERTICAL );

        mButton_QuerySku = new Button(mContextApp);
        mButton_QuerySku.setText( "call QuerySku" );
        mButton_QuerySku.setOnClickListener( this );
        layout.addView( mButton_QuerySku );

        mButton_Purchases = new Button(mContextApp);
        mButton_Purchases.setText( "call QueryPurchases" );
        mButton_Purchases.setOnClickListener( this );
        layout.addView( mButton_Purchases );

        mButton_Consume = new Button(mContextApp);
        mButton_Consume.setText( "call Consume" );
        mButton_Consume.setOnClickListener( this );
        layout.addView( mButton_Consume );

        mListView = new ListView(mContextApp);
        mListView.setOnItemClickListener( this );
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>( mContextApp, android.R.layout.simple_list_item_1, mListSku );
        mListView.setAdapter( arrayAdapter );
        layout.addView( mListView );

        mLayout = layout;
        activity.setContentView( mLayout );
    }

    public void dispose( final Activity activity )
    {
        mButton_QuerySku.setOnClickListener( null );
        mButton_Purchases.setOnClickListener( null );
        mButton_Consume.setOnClickListener( null );
        mListView.setOnItemClickListener( null );

        //activity.setContentView( null );
        ((ViewGroup)mLayout.getParent()).removeAllViews();

        mButton_QuerySku = null;
        mListView = null;
        mButton_Purchases = null;
        mButton_Consume = null;
    }

    @Override
    public void onClick(final View view)
    {
        if ( view == mButton_QuerySku )
        {
            final ArrayList<String> skuList = new ArrayList<String>();
            mListSku.clear();
            skuList.add("android.test.purchased");
            skuList.add("android.test.canceled");
            skuList.add("android.test.refunded");
            skuList.add("android.test.item_unavailable");
            skuList.add("test.item001");
            mListSku.addAll( skuList );
            mListView.invalidateViews();
            mIAB.querySku( skuList );
        }
        else
        if ( view == mButton_Purchases )
        {
            mIAB.queryPurchases();
        }
        else
        if ( view == mButton_Consume )
        {
            final IAB.Purchase purchase = mIAB.getPurchase();
            if ( null != purchase )
            {
                mIAB.consume( purchase.purchaseToken );
            }
        }
    }

    @Override
    public void onItemClick( final AdapterView<?> adapterView, View view, int i, long l)
    {
        if ( adapterView == mListView )
        {
            final String id = mListSku.get(i);
            mIAB.purchase( mActivityRef.get(), id );
        }
    }
}
