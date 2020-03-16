package com.example.nchueccdemo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.nchueccdemo.chain.Contracts;
import com.example.nchueccdemo.chain.NCHUToken;
import com.example.nchueccdemo.http.NettHttpRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;



public class UserActivity extends AppCompatActivity {
    private final String TAG = "=== UserActivity ===";
    private final String USER = "user1", CONTRACT = "contract", STORE = "store1", NCHU = "nchu";
    private TextView text_user_address, text_user_tokens, text_to_address;
    private Button btn_user_case, btn_user_scan, btn_user_transfer;
    private EditText input_token;
    private ImageView image;
    private LinearLayout layout;
    private boolean isClicked = false;

    private ProgressDialog progressDialog;
    private NCHUToken userToken, ownerToken, nchuToken;
    private String userAddr, storeAddr, nchuAddr, ownerAddr;
    private Contracts thisContracts;
    private NettHttpRequest nett;
    private int current = 0;

    private IntentIntegrator scanIntegrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> { finish(); });
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        initializeContract();
        initializeViews();
        // 每次進來畫面都更新一次
        updateBalance();
        // updateBalanceByDB();
    }

    private void initializeContract() {
        thisContracts = new Contracts();

        userAddr = thisContracts.getUserAddr();
        storeAddr = thisContracts.getStoreAddr();
        nchuAddr = thisContracts.getNchuAddr();
        ownerAddr = thisContracts.getOwnerAddr();

        userToken = thisContracts.getUserToken();
        ownerToken = thisContracts.getOwnerToken();
        nchuToken = thisContracts.getNchuToken();

//        Log.d(TAG, "ownerAddr " + ownerAddr);
//        Log.d(TAG, "userAddr " + userAddr);
//        Log.d(TAG, "storeAddr " + storeAddr);
//        Log.d(TAG, "nchuAddr " + nchuAddr);
    }

    private void initializeViews() {
        image = findViewById(R.id.image_user);
        layout = findViewById(R.id.layout_user_address);
        layout.setVisibility(View.GONE);

        image.setOnClickListener(view -> {
            layout.setVisibility((isClicked) ? View.GONE : View.VISIBLE);
            isClicked = !isClicked;
        });

        text_user_address = findViewById(R.id.text_user_address);
        text_user_tokens = findViewById(R.id.text_user_token);
        input_token = findViewById(R.id.input_user_money);
        text_to_address = findViewById(R.id.text_to_address);

        text_user_address.setText(thisContracts.getUserAddr());


        btn_user_case = findViewById(R.id.button_user_case);
        btn_user_scan = findViewById(R.id.button_user_scan);
        btn_user_transfer = findViewById(R.id.button_user_transfer);

        btn_user_case.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this, AlertDialog.THEME_HOLO_DARK);

            builder.setTitle(R.string.user_dialog_title);
            builder.setItems(R.array.list_case,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Long rewards = 0L;
                    switch (i) {
                        case 0:
                            rewards = 320L; break;
                        case 1:
                            rewards = 400L; break;
                        case 2:
                            rewards = 480L; break;
                    }
                    Snackbar.make(view, "您的回饋碳幣有" + rewards + "枚.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    giveRewards(ownerToken, userAddr, rewards, view);
                    //giveRewardsByDB(rewards.intValue(), CONTRACT, USER, view);
                }
            });

            AlertDialog case_dialog = builder.create();
            case_dialog.show();
        });

        btn_user_scan.setOnClickListener(view -> {
            scanIntegrator = new IntentIntegrator(UserActivity.this);
            scanIntegrator.setPrompt("請掃描");
            scanIntegrator.setTimeout(100000);
            scanIntegrator.setOrientationLocked(false);
            scanIntegrator.initiateScan();
        });

        btn_user_transfer.setOnClickListener(view -> {
            String input_str = input_token.getText().toString();
            String toAddr = text_to_address.getText().toString();

            if (toAddr == null || toAddr.equals("")) {
                showSnackbar(view, "請先掃描商家條碼");
            } else {
                if (input_str == null || input_str.equals("")) {
                    showSnackbar(view, "請輸入要轉帳的興大碳幣數量");
                } else if (current < Integer.parseInt(input_str)) {
                    showSnackbar(view, "您沒有這麼多興大碳幣。");
                } else {
                    giveRewards(userToken, toAddr, Long.parseLong(input_str), view);
                    //giveRewardsByDB(Integer.parseInt(input_str), USER, STORE, view);
                }
            }

        });

    }

    // 中興幣合約餘額查詢
    public void updateBalance() {
        new Thread(() -> {
            try {
                BigInteger token_balance = userToken.balanceOf(userAddr).send();
                current = token_balance.intValue();

//                Log.d(TAG, "餘額是 " + token_balance + " NETT");

                runOnUiThread(() -> {
                    text_user_tokens.setText(token_balance + " NETT");

                    input_token.setText("");
                    input_token.clearFocus();
                    text_to_address.setText("");
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {

            }
        }).start();
    }

    public void updateBalanceByDB() {
        new Thread(() -> {
            try {
                nett.doGetUserTokens(USER);
                nett.sendRequest();
                Thread.sleep(1000);
                JSONObject obj = new JSONObject(nett.getResponse().trim());
                //Log.d(TAG,"fucking error " + obj);
                int tokens = new JSONObject(nett.getResponse()).getInt("tokens");
                current = tokens;
                runOnUiThread(() -> {
                    text_user_tokens.setText(tokens + " NETT");
                    input_token.setText("");
                    input_token.clearFocus();
                    text_to_address.setText("");
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
    // 發ECC給user
    public void giveRewards(final NCHUToken token, final String toAddr, final Long amount, View view) {
        progressDialog = ProgressDialog.show(UserActivity.this,"交易中", "請等待...",true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    token.transfer(toAddr, BigInteger.valueOf(amount)).send();
                    showSnackbar(view, "轉帳完成");
//                    Log.d(TAG, "轉帳完成");
                    updateBalance();
                } catch (Exception e) {
//                    Log.d(TAG, "轉帳失敗");
                    Log.d(TAG,e.getMessage());
                    showSnackbar(view, "轉帳失敗...");
                }
                progressDialog.dismiss();

            }

        }).start();
    }

    public void giveRewardsByDB(int amount, String from, String to, View view) {
        progressDialog = ProgressDialog.show(UserActivity.this,"交易中", "請等待...",true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    nett.doAddTokens(amount, from, to);
                    nett.sendRequest();
                    Thread.sleep(1000);
                    showSnackbar(view, "轉帳完成");

                    updateBalanceByDB();
                } catch (Exception e) {
                    Log.d(TAG,e.getMessage());
                    showSnackbar(view, "轉帳失敗...");
                }
                progressDialog.dismiss();

            }

        }).start();

    }
    // QRcode掃描結果
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            if (scanningResult.getContents() != null) {
                String scanContent = scanningResult.getContents();
                if (!scanContent.equals("")) {
                    // Toast.makeText(getApplicationContext(), "掃描內容: " + scanContent.toString(), Toast.LENGTH_LONG).show();
                    // edAccount.setText(scanContent.toString());
                    text_to_address.setText(scanContent);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
            Toast.makeText(getApplicationContext(), "發生錯誤", Toast.LENGTH_LONG).show();
            text_to_address.setText("");
        }
    }

    private void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
}
