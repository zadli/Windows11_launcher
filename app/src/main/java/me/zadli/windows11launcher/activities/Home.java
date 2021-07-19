package me.zadli.windows11launcher.activities;

import android.accessibilityservice.AccessibilityService;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

import me.zadli.windows11launcher.R;
import me.zadli.windows11launcher.adapters.AppList;
import me.zadli.windows11launcher.services.PowerMenuService;

public class Home extends AppCompatActivity {

    CardView appList;
    ImageView appListButton;
    TextView userName;
    ImageView powerButton;
    TextView allAppsButton;
    TextView pinnedAppsText;
    LinearLayout appWidgets;
    RecyclerView appListRecycler;
    TextView appsText;
    MaterialSearchBar appSearch;
    boolean appListOpened = false;
    boolean allAppsOpened = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        appList = findViewById(R.id.appList);
        appListButton = findViewById(R.id.appListButton);
        appListRecycler = findViewById(R.id.appListRecycler);
        userName = findViewById(R.id.userName);
        powerButton = findViewById(R.id.powerButton);
        appWidgets = findViewById(R.id.appWidgets);
        allAppsButton = findViewById(R.id.allAppsButton);
        pinnedAppsText = findViewById(R.id.pinnedAppsText);
        appsText = findViewById(R.id.appsText);
        appSearch = findViewById(R.id.appSearch);


        Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = getPackageManager().queryIntentActivities(i, 0);

        appSearch.setRoundedSearchBarEnabled(true);
        appSearch.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<ResolveInfo> query = new ArrayList<>();

                for (ResolveInfo info : apps) {
                    if (String.valueOf(getPackageManager()
                            .getApplicationLabel(info.activityInfo.applicationInfo)).toLowerCase()
                            .contains(String.valueOf(s).toLowerCase())){
                        query.add(info);
                    }
                }

                appListRecycler.setAdapter(null);
                pinnedAppsText.setVisibility(View.GONE);
                appsText.setText("Search");
                allAppsButton.setText("< Back");
                appListRecycler.setLayoutManager(new GridLayoutManager(Home.this, 4));
                appListRecycler.setAdapter(new AppList(query,
                        getPackageManager(),
                        Home.this));
                appListRecycler.setNestedScrollingEnabled(false);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setUpWidgets();



        if (allAppsOpened) {
            pinnedAppsText.setVisibility(View.GONE);
            appsText.setText("All apps");
            allAppsButton.setText("< Pinned apps");
            appListRecycler.setLayoutManager(new GridLayoutManager(this, 4));
            appListRecycler.setAdapter(new AppList(apps,
                    getPackageManager(),
                    this));
            appListRecycler.setNestedScrollingEnabled(false);
        } else {
            appsText.setText("Pinned apps");
            allAppsButton.setText("All apps >");
            appListRecycler.setAdapter(null);
            pinnedAppsText.setVisibility(View.VISIBLE);
        }

        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType("com.google");
        if (accounts.length != 0) {
            userName.setText(accounts[0].name);
        } else {
            userName.setText("Administrator");
        }

        appListButton.setOnTouchListener(this::onTouchAnimation);
        powerButton.setOnTouchListener(this::onTouchAnimation);
        appListButton.setOnClickListener(v -> {
            if (appListOpened) {
                Animation animation_out = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out);
                appList.startAnimation(animation_out);
                animation_out.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        appList.setVisibility(View.GONE);
                        appListOpened = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            } else {
                appList.setVisibility(View.VISIBLE);
                Animation animation_in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in);
                appList.startAnimation(animation_in);
                appListOpened = true;
            }
        });
        powerButton.setOnClickListener(v -> {
            if (isAccessServiceEnabled()) {
                ComponentName component = new ComponentName(getApplicationContext(),
                        PowerMenuService.class);
                getApplicationContext().getPackageManager().setComponentEnabledSetting(component,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);

                Intent intent = new Intent("me.zadli.windows11launcher.ACCESSIBILITY_ACTION");
                intent.putExtra("action", AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else {
                Toast.makeText(this, "Please enable launcher in accessibility settings " +
                        "for work power button", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });
        allAppsButton.setOnTouchListener(this::onTouchAnimation);
        allAppsButton.setOnClickListener(v -> {
            allAppsOpened = !allAppsOpened;
            if (allAppsOpened) {
                appsText.setText("All apps");
                allAppsButton.setText("< Pinned apps");
                pinnedAppsText.setVisibility(View.GONE);
                appListRecycler.setLayoutManager(new GridLayoutManager(Home.this,
                        4));
                appListRecycler.setAdapter(new AppList(apps,
                        getPackageManager(),
                        Home.this));
                appListRecycler.setNestedScrollingEnabled(false);
            } else {
                appsText.setText("Pinned apps");
                allAppsButton.setText("All apps >");
                appListRecycler.setAdapter(null);
                pinnedAppsText.setVisibility(View.VISIBLE);
            }
        });
    }

    public boolean isAccessServiceEnabled() {
        String prefString = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return prefString != null && prefString.contains(getPackageName() +
                "/" + PowerMenuService.class.getName());
    }

    public boolean onTouchAnimation(View v, MotionEvent event) {
        ObjectAnimator animationX;
        ObjectAnimator animationY;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            animationX = ObjectAnimator
                    .ofFloat(v, "ScaleX", 0.8f);
            animationY = ObjectAnimator
                    .ofFloat(v, "ScaleY", 0.8f);
        } else {
            animationX = ObjectAnimator
                    .ofFloat(v, "ScaleX", 1f);
            animationY = ObjectAnimator
                    .ofFloat(v, "ScaleY", 1f);
        }
        animationX.setDuration(150);
        animationY.setDuration(150);
        animationX.start();
        animationY.start();
        return false;
    }

    public void setUpWidgets() {
        View widgetClock = getLayoutInflater().inflate(R.layout.widget_clock, appWidgets, false);
        appWidgets.addView(widgetClock);
        TextClock clock = widgetClock.findViewById(R.id.widgetClockMain);
    }
}