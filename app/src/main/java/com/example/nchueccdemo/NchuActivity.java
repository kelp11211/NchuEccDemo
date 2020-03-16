package com.example.nchueccdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.nchueccdemo.chain.Contracts;
import com.example.nchueccdemo.chain.NCHUToken;
import com.example.nchueccdemo.http.NettHttpRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

public class NchuActivity extends AppCompatActivity {
    private final String TAG = "=== NchuActivity ===";
    private final String USER = "user1", CONTRACT = "contract", STORE = "store1", NCHU = "nchu";
    private TextView text_nchu_address, text_nchu_tokens;
    private Button btn_nchu_notify;
    private EditText input_phone;
    private ImageView image;
    private LinearLayout layout;
    private boolean isClicked = false;

    private Contracts thisContracts;
    private NCHUToken nchuToken;
    private String nchuAddr;
    private NotificationManager notificationManager;
    private NettHttpRequest nett;
    private int current = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nchu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        initializeContract();
        initializeViews();
        // 每次進來畫面都更新一次
        updateBalance();
    }

    private void initializeContract() {
        thisContracts = new Contracts();

        nchuAddr = thisContracts.getNchuAddr();

        nchuToken = thisContracts.getNchuToken();

    }

    private void initializeViews() {
        image = findViewById(R.id.image_nchu);
        layout = findViewById(R.id.layout_nchu_address);
        layout.setVisibility(View.GONE);

        image.setOnClickListener(view -> {
            layout.setVisibility((isClicked) ? View.GONE : View.VISIBLE);
            isClicked = !isClicked;
        });

        text_nchu_address  = findViewById(R.id.text_nchu_address);
        text_nchu_tokens = findViewById(R.id.text_nchu_token);

        text_nchu_address.setText(nchuAddr);

        input_phone = findViewById(R.id.input_phone);

        btn_nchu_notify = findViewById(R.id.button_nchu_notify);
        btn_nchu_notify.setOnClickListener(view -> {
            String phone = input_phone.getText().toString();
            if (phone != null && phone.length() == 10) {
                sendSMS(phone);
            } else {
                showNotification();
            }
        });
    }

    // 中興幣合約餘額查詢
    public void updateBalance() {
        new Thread(() -> {
            try {
                BigInteger token_balance = nchuToken.balanceOf(nchuAddr).send();
//                Log.d(TAG, "餘額是 " + token_balance + " NETT");
                runOnUiThread(() -> {
                    text_nchu_tokens.setText(token_balance + " NETT");
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {

            }
        }).start();
    }

    private void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private void showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channelLove = new NotificationChannel(
                    "NCHU",
                    "Channel Nchu",
                    NotificationManager.IMPORTANCE_HIGH);
            channelLove.setDescription("Channel Nchu");
            channelLove.enableLights(true);
            channelLove.enableVibration(true);

            notificationManager.createNotificationChannel(channelLove);

            Notification.Builder builder = new Notification.Builder(this, "1")
                    .setSmallIcon(R.drawable.wallet)
                    .setChannelId("NCHU")
                    .setContentTitle("中興大學匯款通知")
                    .setContentText("中興大學已確認您要清算的碳幣數量，已將現金匯款至您的帳戶。");


            notificationManager.notify(1, builder.build());
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                    .setSmallIcon(R.drawable.wallet)
                    .setContentTitle("中興大學匯款通知")
                    .setContentText("中興大學已確認您要清算的碳幣數量，已將現金匯款至您的帳戶。")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("中興大學已確認您要清算的碳幣數量，已將現金匯款至您的帳戶。"))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            notificationManager.notify(1, builder.build());
        }

    }

    public void updateBalanceByDB() {
        new Thread(() -> {
            try {
                nett.doGetUserTokens(NCHU);
                nett.sendRequest();
                Thread.sleep(1000);
                JSONObject obj = new JSONObject(nett.getResponse().trim());
                int tokens = obj.getInt("tokens");
                current = tokens;
                runOnUiThread(() -> {
                    text_nchu_tokens.setText(tokens + " NETT");

                });
            } catch (JSONException e) {
                Log.d(TAG, "Error : " + e.getMessage());
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {

            }
        }).start();
    }

    private void sendSMS(String phone) {

        SmsManager sm = SmsManager.getDefault();
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(), 0);

        sm.sendTextMessage(phone,  // 要送出簡訊的電話
                null,    // 電話號碼位址
                "中興大學已確認您要清算的碳幣數量，已將現金匯款至您的帳戶。", // 簡訊內容
                sentPI,    // 發送簡訊结果（是否成功發送），需使用PendingIntent
                null);   // 對方接收結果（是否已成功接收），需使用PendingIntent

        input_phone.setText("");
        input_phone.clearFocus();

/*
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
        //intent.setType("vnd.android-dir/mms-sms");
        intent.putExtra("sms_body", "中興大學已確認您要清算的碳幣數量，已將現金匯款至您的帳戶。");
        startActivity(intent);
*/
/*
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + Uri.encode(phone)));
        intent.setType("vnd.android-dir/mms-sms");
        intent.putExtra("sms_body","中興大學已確認您要清算的碳幣數量，已將現金匯款至您的帳戶。");
        startActivity(intent);
*/
    }
}
