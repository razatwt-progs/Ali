package com.example.meraassistant;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class ControlService extends AccessibilityService {

    private static ControlService instance;

    private final Handler handler =
        new Handler(Looper.getMainLooper());

    public static ControlService getInstance() {
        return instance;
    }

    @Override
    protected void onServiceConnected() {
        instance = this;
    }

    public void cancelPending() {
        handler.removeCallbacksAndMessages(null);
    }

    public void scheduleAction(String action) {
        cancelPending();

        handler.postDelayed(() -> {
            boolean success;

            switch (action) {
                case "home":
                    success = performGlobalAction(GLOBAL_ACTION_HOME);
                    break;

                case "back":
                    success = performGlobalAction(GLOBAL_ACTION_BACK);
                    break;

                case "recent":
                    success = performGlobalAction(GLOBAL_ACTION_RECENTS);
                    break;

                case "scroll":
                    AccessibilityNodeInfo root = getRootInActiveWindow();
                    success = false;

                    if (root != null) {
                        try {
                            success = scrollNode(root);
                        } finally {
                            root.recycle();
                        }
                    }
                    break;

                default:
                    success = false;
            }

            Toast.makeText(
                this,
                success
                    ? "Action bhej diya."
                    : "Action available nahi tha.",
                Toast.LENGTH_SHORT
            ).show();

        }, 5000);
    }

    private boolean scrollNode(AccessibilityNodeInfo node) {
        if (node.isScrollable()
                && node.performAction(
                    AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);

            if (child == null) {
                continue;
            }

            try {
                if (scrollNode(child)) {
                    return true;
                }
            } finally {
                child.recycle();
            }
        }

        return false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Screen events ko record ya transmit nahi karte.
    }

    @Override
    public void onInterrupt() {
        cancelPending();
    }

    @Override
    public void onDestroy() {
        cancelPending();

        if (instance == this) {
            instance = null;
        }

        super.onDestroy();
    }
}
