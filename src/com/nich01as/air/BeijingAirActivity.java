package com.nich01as.air;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class BeijingAirActivity extends Activity {
    
    private static final String[] AQI_DES = new String[]{"优", "良", "轻微污染", "轻度污染", "中度污染", "重污染"};
    
    private static final int[] BG = new int[]{R.drawable.bg0, R.drawable.bg51, R.drawable.bg101, R.drawable.bg151, R.drawable.bg201, R.drawable.bg301};
    
    private static final String API_URL = "http://ruguo.se/api/air";
    
    private TextView mStatusText = null;
    
    private TextView mUpdateTime = null;
    
    private AirStatus mAirStatus = new AirStatus();
    
    private UpdateChecker mChecker = null;
    private AdView mAdView = null;
    
    private LinearLayout mAdLayout = null;
    
    private View mContainer = null;
    
    private TextView mDesText = null;
    
    private AirApp mApp;
    
    private static final int DIALOG_REFRESH = 1;
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mStatusText = (TextView) findViewById(R.id.air_quality);
        mUpdateTime = (TextView) findViewById(R.id.update_time);
        //mRefresh = findViewById(R.id.refresh);
        //mShare = findViewById(R.id.share);
        //mRefresh.setOnClickListener(this);
        //mShare.setOnClickListener(this);
        mContainer = findViewById(R.id.container);
        mAdView = new AdView(this, AdSize.BANNER, "a14e6c6cd30e5a2");
        mAdLayout = (LinearLayout) findViewById(R.id.ad);
        mAdLayout.addView(mAdView);
        mApp = (AirApp) getApplication();
        mDesText = (TextView) findViewById(R.id.air_des);
   }
    
    private class UpdateChecker extends AsyncTask<Void, Void, AirStatus> {

        @Override
        protected AirStatus doInBackground(Void... params) {
            
            AndroidHttpClient client = AndroidHttpClient.newInstance("ruguose");
            HttpUriRequest reuqest = new HttpGet(API_URL);
            try {
                HttpResponse response = client.execute(reuqest);
                
                InputStreamReader reader = new InputStreamReader(response.getEntity().getContent());
                char[] buffer = new char[1024];
                StringBuilder sb = new StringBuilder();
                int len = 0;
                while ((len = reader.read(buffer)) >= 0) {
                    sb.append(buffer, 0, len);
                }
                String[] results = sb.toString().split(";");
                mAirStatus.updateDate = results[0];
                mAirStatus.updateTime = results[1];
                mAirStatus.airStatus = Integer.parseInt(results[4].trim());
                return mAirStatus;
            } catch (Exception e) {
                Log.e("BeijingAir", "error", e);
                return null;
            }
        }
        
        @Override
        public void onPostExecute(AirStatus airStatus) {
            if (airStatus != null) {
                mApp.updateCheckTime();
                mApp.setAQIValue(airStatus.airStatus);
                updatePage();
            }
            dismissDialog(DIALOG_REFRESH);
        }
    }
    
    private void updatePage() {
        SimpleDateFormat formator = new SimpleDateFormat("更新于 yyyy 年 MM 月 dd 日 HH:mm");
        mUpdateTime.setText(formator.format(new Date(mApp.getLastUpdateTime())));
        mDesText.setText(AQI_DES[mApp.getAQILevel()]);
        mContainer.setBackgroundResource(BG[mApp.getAQILevel()]);
        mStatusText.setText(Integer.toString(mApp.getAQLValue()));
    }
    
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_REFRESH:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage("正在更新……");
                return dialog;
        }
        return null;
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.xml.main_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                updateData();
                break;
            case R.id.share:
                mContainer.buildDrawingCache();
                Bitmap bitmap = mContainer.getDrawingCache();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, os);
                File cacheDir = Environment.getExternalStorageDirectory();
                File file = new File(cacheDir, "share.jpg");
                FileOutputStream fout = null;
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    fout = new FileOutputStream(file);
                    os.writeTo(fout);
                    os.flush();
                    fout.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                       if (fout != null){
                           fout.close();
                       }
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                    
                share(file.getAbsolutePath());
                break;
        }
        
        return true;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mApp.getAQLValue() == 0) {
            updateData();
        } else {
            updatePage();
        }
        AdRequest adRequest = new AdRequest();
        mAdView.loadAd(adRequest);
    }

    /**
     * 
     */
    private void share(String url) {
        Log.d("Main", "url is " + url);
        Uri emailTOUri = Uri.parse("mailto:");// 联系人地址
        Intent intent = new Intent(Intent.ACTION_SEND, emailTOUri);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_SUBJECT, "北京的空气质量");
        StringBuilder sb = new StringBuilder();
        sb.append("#BeijingAir# 目前北京的空气质量为: " + AQI_DES[mApp.getAQILevel()] + "。 PM2.5为" + mApp.getAQLValue() + "。");
        if (mApp.getAQLValue() > 300) {
            sb.append("尼玛该带防毒面具了！");
        }
        sb.append("  https://market.android.com/details?id=com.nich01as.air");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));
        startActivity(intent);
    }

    /**
     * 
     */
    private void updateData() {
        if (mChecker != null) {
            mChecker.cancel(true);
            mChecker = null;
        }
        mChecker = new UpdateChecker();
        mChecker.execute(new Void[]{});
        showDialog(DIALOG_REFRESH);
    }
    
    private class AirStatus {
        
        public String updateDate = null;
        public String updateTime = null;
        public int airStatus = 0;
    }
}