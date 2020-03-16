package com.example.nchueccdemo;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button userBtn, storeBtn, nchuBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userBtn = findViewById(R.id.button_user);
        userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });
        storeBtn = findViewById(R.id.button_store);
        storeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, StoreActivity.class);
                startActivity(intent);
            }
        });
        nchuBtn = findViewById(R.id.button_nchu);
        nchuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, NchuActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        switch (item.getItemId()) {
            case R.id.action_user:
                Snackbar.make(findViewById(R.id.content_main), "User", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
            case R.id.action_store:
                Snackbar.make(findViewById(R.id.content_main), "Store", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
            case R.id.action_nchu:
                Snackbar.make(findViewById(R.id.content_main), "Nchu", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
            case R.id.action_settings:
                Snackbar.make(findViewById(R.id.content_main), "Setting", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
