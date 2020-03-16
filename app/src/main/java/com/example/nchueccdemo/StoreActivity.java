package com.example.nchueccdemo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.example.nchueccdemo.chain.Contracts;
import com.example.nchueccdemo.chain.NCHUToken;
import com.example.nchueccdemo.http.NettHttpRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigInteger;
import java.util.EnumMap;
import java.util.Map;


public class StoreActivity extends AppCompatActivity {
    private final String TAG = "=== StoreActivity ===";
    private final String USER = "user1", CONTRACT = "contract", STORE = "store1", NCHU = "nchu";
    private TextView text_store_address, text_store_tokens;
    private Button btn_store_qrcode, btn_store_transfer;
    private ImageView qrcode_dialog_img;
    private ImageView image;
    private LinearLayout layout;
    private boolean isClicked = false;

    private ProgressDialog progressDialog;
    private NCHUToken storeToken;
    private String storeAddr, nchuAddr;
    private Contracts thisContracts;

    private long current;
    private Dialog mydialog;
    private Admin admin;
    private NettHttpRequest nett;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
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

        initializeContract();
        initializeViews();
        // 每次進來畫面都更新一次
        updateBalance();

    }

    private void initializeContract() {
        thisContracts = new Contracts();

        storeAddr = thisContracts.getStoreAddr();
        nchuAddr = thisContracts.getNchuAddr();

        storeToken = thisContracts.getStoreToken();

        admin = thisContracts.getAdmin();

    }

    private void initializeViews() {
        image = findViewById(R.id.image_store);
        layout = findViewById(R.id.layout_store_address);
        layout.setVisibility(View.GONE);

        image.setOnClickListener(view -> {
            layout.setVisibility((isClicked) ? View.GONE : View.VISIBLE);
            isClicked = !isClicked;
        });

        text_store_address = findViewById(R.id.text_store_address);
        text_store_tokens = findViewById(R.id.text_store_token);

        text_store_address.setText(storeAddr);

        btn_store_qrcode = findViewById(R.id.button_store_code);
        btn_store_transfer = findViewById(R.id.button_store_transfer);

        btn_store_qrcode.setOnClickListener(view -> {
            create_qrcode();
        });

        btn_store_transfer.setOnClickListener(view -> {

            if (current > 0)
                giveRewards(storeToken, nchuAddr, current, view);
            else
                showSnackbar(view, "目前沒有碳幣可以進行清算。");
        });

    }

    // 中興幣合約餘額查詢
    public void updateBalance() {
        new Thread(() -> {
            try {
                BigInteger token_balance = storeToken.balanceOf(storeAddr).send();
                current = token_balance.longValue();

//                Log.d(TAG, "餘額是 " + token_balance + " NETT");

                runOnUiThread(() -> {
                    text_store_tokens.setText(token_balance + " NETT");

                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {

            }
        }).start();
    }

    //產生錢包地址QRcode
    public void create_qrcode() {
        BarcodeEncoder encoder = new BarcodeEncoder();
        try {
            Map hints = new EnumMap(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            Bitmap bit = encoder.encodeBitmap(storeAddr, BarcodeFormat.QR_CODE, 500, 500,hints);
            qrcode_image(bit);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    // 產生qrcode_dialog
    private void qrcode_image(Bitmap bit) {
        mydialog = new Dialog(StoreActivity.this);
        mydialog.setContentView(R.layout.qrcode_dialog_image);
        qrcode_dialog_img = mydialog.findViewById(R.id.qrcode_image);
        qrcode_dialog_img.setImageBitmap(bit);
        mydialog.show();
    }

    public void giveRewards(NCHUToken token, String toAddr, long amount, View view) {
        progressDialog = ProgressDialog.show(StoreActivity.this,"交易中", "請等待...",true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    token.transfer(toAddr, BigInteger.valueOf(amount)).send();
                    showSnackbar(view, "轉帳完成");
                    updateBalance();
                } catch (Exception e) {
                    Log.d(TAG,e.getMessage());
                    showSnackbar(view, "轉帳失敗...");
                }
                progressDialog.dismiss();

            }

        }).start();
    }

    //以太幣餘額查詢
    private void getBlanceOf() throws IOException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (admin == null) return;
                //String address = "0x9cdc6de0952107e3973e4dcb56cc1e0c8d2a850f";
                //第二个参数：区块的参数，建议选最新区块
                EthGetBalance balance = null;
                try {
                    balance = admin.ethGetBalance(storeAddr, DefaultBlockParameter.valueOf("latest")).send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //格式转化 wei-ether
                //BigDecimal 轉 int
                int blanceETH = Convert.fromWei(balance.getBalance().toString(), Convert.Unit.ETHER).intValue();
                Log.d(TAG, "ETH " + blanceETH );

            }

        }).start();

    }

    private void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void updateBalanceByDB() {

        new Thread(() -> {
            try {
                nett.doGetUserTokens(STORE);
                nett.sendRequest();
                Thread.sleep(1000);
                JSONObject obj = new JSONObject(nett.getResponse().trim());
                int tokens = obj.getInt("tokens");
                current = tokens;
                runOnUiThread(() -> {
                    text_store_tokens.setText(tokens + " NETT");

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

    public void giveRewardsByDB(int amount, String from, String to, View view) {
        progressDialog = ProgressDialog.show(StoreActivity.this,"交易中", "請等待...",true);
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
}
