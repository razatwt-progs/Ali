package com.example.meraassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private LinearLayout content;
    private TextView status;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        int padding = dp(20);
        content.setPadding(padding, padding, padding, padding);
        scroll.addView(content);
        setContentView(scroll);

        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(
                insets.getSystemWindowInsetLeft(),
                insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),
                insets.getSystemWindowInsetBottom()
            );
            return insets;
        });
        scroll.requestApplyInsets();

        TextView title = new TextView(this);
        title.setText("Mera Assistant");
        title.setTextSize(26);
        content.addView(title);

        TextView info = new TextView(this);
        info.setText(
            "\nYeh basic button-control version hai; "
            + "AI chat aur voice abhi shamil nahi hain.\n\n"
            + "Home, Back, Recent aur Scroll commands "
            + "5 seconds baad chalengi. Is dauran target "
            + "app par khud switch karo.\n"
        );
        info.setTextSize(16);
        content.addView(info);

        status = new TextView(this);
        status.setTextSize(16);
        content.addView(status);

        addButton("Accessibility permission", v -> showPermissionInfo());

        addButton("YouTube kholo", v ->
            openApp("com.google.android.youtube"));

        addButton("WhatsApp kholo", v ->
            openApp("com.whatsapp"));

        addButton("Home — 5 seconds baad", v -> runAction("home"));
        addButton("Back — 5 seconds baad", v -> runAction("back"));
        addButton("Recent apps — 5 seconds baad", v -> runAction("recent"));
        addButton("Scroll aage — 5 seconds baad", v -> runAction("scroll"));

        addButton("STOP — pending command cancel", v -> {
            ControlService service = ControlService.getInstance();

            if (service != null) {
                service.cancelPending();
            }

            toast("Pending command cancel kar di.");
        });

        addButton("Control service band karo", v -> {
            ControlService service = ControlService.getInstance();

            if (service != null) {
                service.cancelPending();
                service.disableSelf();
                status.setText("\nControl service band kar di.\n");
            } else {
                toast("Service pehle se connected nahi hai.");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (status != null) {
            status.setText(
                ControlService.getInstance() == null
                    ? "\nAccessibility: connected nahi\n"
                    : "\nAccessibility: connected\n"
            );
        }
    }

    private void showPermissionInfo() {
        new AlertDialog.Builder(this)
            .setTitle("Accessibility ki ijazat")
            .setMessage(
                "Yeh permission app ko active screen ke UI "
                + "elements tak access aur navigation actions "
                + "karne ki capability deti hai.\n\n"
                + "Is version ka code screen content save ya "
                + "internet par send nahi karta. Scroll ke waqt "
                + "screen ke elements check karta hai.\n\n"
                + "Ijazat optional hai. YouTube aur WhatsApp "
                + "kholne ke liye iski zaroorat nahi.\n\n"
                + "Settings mein Mera Assistant Control ko "
                + "khud enable karo. Wahin se kabhi bhi "
                + "disable kar sakte ho."
            )
            .setNegativeButton("Abhi nahi", null)
            .setPositiveButton("Settings kholo", (dialog, which) -> {
                try {
                    startActivity(
                        new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    );
                } catch (Exception e) {
                    toast("Phone ki Settings manually kholo.");
                }
            })
            .show();
    }

    private void runAction(String action) {
        ControlService service = ControlService.getInstance();

        if (service == null) {
            toast("Pehle Accessibility permission enable karo.");
            return;
        }

        service.scheduleAction(action);
        toast("5 seconds mein action chalega. Target app par jao.");
    }

    private void openApp(String packageName) {
        Intent intent =
            getPackageManager().getLaunchIntentForPackage(packageName);

        if (intent == null) {
            toast("App installed nahi hai ya available nahi.");
            return;
        }

        try {
            startActivity(intent);
        } catch (Exception e) {
            toast("App khol nahi sake.");
        }
    }

    private void addButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setOnClickListener(listener);

        content.addView(
            button,
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        );
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
