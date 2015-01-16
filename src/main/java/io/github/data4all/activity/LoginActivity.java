package io.github.data4all.activity;

import java.util.Timer;
import java.util.TimerTask;

import io.github.data4all.R;
import io.github.data4all.logger.Log;
import io.github.data4all.service.OrientationListener;
import io.github.data4all.util.Optimizer;
import io.github.data4all.util.PointToCoordsTransformUtil;
import oauth.signpost.OAuth;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

    final String TAG = getClass().getSimpleName();

    OrientationListener ol = new OrientationListener();
    PointToCoordsTransformUtil ptt = new PointToCoordsTransformUtil();
    TextView editText1;
    Optimizer optimizer = new Optimizer();
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG,
                "SharedPreferences:"
                        + PreferenceManager.getDefaultSharedPreferences(
                                getBaseContext()).getAll());

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                SharedPreferences sharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                // Stay logged in?
                CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox1);

                // Already got token?
                if (!sharedPrefs.contains(OAuth.OAUTH_TOKEN)
                        && !sharedPrefs.contains(OAuth.OAUTH_TOKEN_SECRET)) {

                    setTemporaryField(!checkBox.isChecked());
                    startActivity(new Intent().setClass(v.getContext(),
                            PrepareRequestTokenActivity.class));
                } else {

                    Toast.makeText(getApplicationContext(),
                            R.string.alreadyLoggedIn, Toast.LENGTH_SHORT)
                            .show();

                }
            }
        });

        // for debugging
        Button deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                deleteTokenFromSharedPreferences();

            }
        });
        
        
        double[] p = ptt.calculate2dPoint(optimizer.currentPos());
        editText1 = (TextView) findViewById(R.id.editText1);
        editText1.setText("X-Koord: " + p[0] + " Y-Koord: " + p[1]);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Delete OAuthToken when onDestroy() is called
        if (isTokenTemporary()) {
            deleteTokenFromSharedPreferences();
        }

    }

    private void deleteTokenFromSharedPreferences() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        if (sharedPrefs.contains(OAuth.OAUTH_TOKEN)
                && sharedPrefs.contains(OAuth.OAUTH_TOKEN_SECRET)) {

            Editor ed = sharedPrefs.edit();
            ed.remove(OAuth.OAUTH_TOKEN);
            ed.remove(OAuth.OAUTH_TOKEN_SECRET);
            ed.remove("IS_TEMPORARY");
            ed.commit();
        }
        Log.i(TAG, "SharedPreferences:" + sharedPrefs.getAll());
    }

    private boolean isTokenTemporary() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        return sharedPrefs.getBoolean("IS_TEMPORARY", false);
    }

    private void setTemporaryField(boolean b) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        Editor ed = sharedPrefs.edit();
        ed.putBoolean("IS_TEMPORARY", b);
        ed.commit();
        Log.i(TAG, "SharedPreferences:" + sharedPrefs.getAll());

    }

}
