package com.example.offlinepay;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Context;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.offlinepay.ui.HomeFragment;
import com.example.offlinepay.ui.HistoryFragment;
import com.example.offlinepay.ui.ProfileFragment;
import com.example.offlinepay.mesh.MeshManager;
import com.example.offlinepay.ui.AnimatedMeshGradientDrawable;

public class MainActivity extends AppCompatActivity {

    private static final int PIN_MAX_LENGTH      = 6;

    private static final String SERVER_URL = "http://192.168.1.10:8080"; // Replace with your server IP
    private static final String SERVER_PUBLIC_KEY_B64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4T4Tr9e6BRtuasU/zfDkqdEQ1HcQP22KNZqlchZt5/ppd7vcpJDsG1E1ZIluodO7VxLt6ktlxcT2aULhb3EYC1jQOSl8Cu95zb5/F4FE8SljY6LKxrGwqCOiRPjTN7XEdQQ11ZWO9RZ8ZWpdP3ttMGtVPOLqlp68eUWb1/YfqUHEfL9lV+TY4YA55+YP8tQcdhChZObqqtDnC/coT+fX91fh7ZseyLHMgR9hqRnJ9Inoz5YrShJIr1lfH4Xe8yBq9P8wRRAJh9x5KbKojLOtSQqX5cpD/XjaH10ffKIFA1+ujH1U/HaIaaGLruOyQk+XMjfZ0s90eosAGGcnPUltbwIDAQAB";

    // ---------- Components ----------
    private MeshManager  meshManager;
    private final okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient();

    // ---------- Views ----------
    private ImageView iconHome, iconHistory, iconProfile;
    private TextView textHome, textHistory, textProfile;
    private View navHome, navHistory, navProfile;

    // ---------- State ----------
    private StringBuilder pinBuffer = new StringBuilder();
    private AnimatedMeshGradientDrawable meshGradient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Mesh
        meshManager = new MeshManager(this);
        meshManager.setListener(count -> runOnUiThread(() -> updateRadar(count)));

        // Initial Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
        }

        setupNavigation();
        checkAndRequestPermissions();
    }

    private void updateRadar(int count) {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof HomeFragment && f.getView() != null) {
            com.example.offlinepay.ui.MeshRadarView radar = f.getView().findViewById(R.id.meshRadar);
            if (radar != null) {
                radar.setPeerCount(count);
            }
        }
    }

    private void setupNavigation() {
        View navHome = findViewById(R.id.navHome);
        View navHistory = findViewById(R.id.navHistory);
        View navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> updateNavState(0));
        navHistory.setOnClickListener(v -> updateNavState(1));
        navProfile.setOnClickListener(v -> updateNavState(2));
    }

    private void updateNavState(int index) {
        int activeColor = getResources().getColor(R.color.neon_cyan);
        int inactiveColor = 0x66FFFFFF;

        ImageView[] icons = { findViewById(R.id.iconHome), findViewById(R.id.iconHistory), findViewById(R.id.iconProfile) };
        TextView[] texts = { findViewById(R.id.textHome), findViewById(R.id.textHistory), findViewById(R.id.textProfile) };
        View[] navs = { findViewById(R.id.navHome), findViewById(R.id.navHistory), findViewById(R.id.navProfile) };

        Fragment selectedFragment = null;
        switch (index) {
            case 0: selectedFragment = new HomeFragment(); break;
            case 1: selectedFragment = new HistoryFragment(); break;
            case 2: selectedFragment = new ProfileFragment(); break;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, selectedFragment)
                .commit();
        }

        for (int i = 0; i < 3; i++) {
            boolean active = (i == index);
            icons[i].setColorFilter(active ? activeColor : inactiveColor);
            texts[i].setTextColor(active ? activeColor : inactiveColor);
            texts[i].setTypeface(null, active ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            
            if (active) {
                final View targetNav = navs[i];
                targetNav.animate().scaleX(1.1f).scaleY(1.1f).alpha(1.0f).setDuration(150).withEndAction(() -> 
                    targetNav.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150)
                ).start();
            }
        }
    }

    public void initiatePayment(String amount, String recipient) {
        showPinDialog(amount, recipient);
    }

    private void checkAndRequestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            requestPermissions(new String[]{
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_ADVERTISE,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.NEARBY_WIFI_DEVICES,
                android.Manifest.permission.SEND_SMS
            }, 101);
        } else {
            requestPermissions(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.SEND_SMS
            }, 101);
        }
    }


    private void executeMeshPayment(String recipient, String amount) {
        showStatus("📡 Broadcasting to Mesh...", true);
        
        android.os.AsyncTask.execute(() -> {
            try {
                // 1. Create Instruction
                org.json.JSONObject instruction = new org.json.JSONObject();
                instruction.put("REC", recipient);
                instruction.put("AMT", amount);
                instruction.put("NONCE", java.util.UUID.randomUUID().toString());
                instruction.put("TS", System.currentTimeMillis());

                // 2. Encrypt
                String ciphertext = com.example.offlinepay.security.HybridCryptoEngine.encrypt(
                    instruction.toString(), SERVER_PUBLIC_KEY_B64
                );

                // 3. Wrap in MeshPacket
                org.json.JSONObject packet = new org.json.JSONObject();
                packet.put("packetId", java.util.UUID.randomUUID().toString());
                packet.put("ttl", 5);
                packet.put("createdAt", System.currentTimeMillis());
                packet.put("ciphertext", ciphertext);

                // 4. Save and Gossip
                com.example.offlinepay.mesh.PacketStore.savePacket(this, packet);
                
                int peerCount = meshManager.getPeerCount();
                if (peerCount > 0) {
                    meshManager.broadcast(packet);
                    runOnUiThread(() -> {
                        showStatus("✅ Payment Broadcasted via Mesh", true);
                        showSuccessDialog(amount, recipient);
                    });
                } else {
                    // Fallback to Direct Device SMS
                    runOnUiThread(() -> showStatus("📡 No Mesh Peers. Using Direct SMS...", true));
                    sendDirectSMS(ciphertext);
                    runOnUiThread(() -> {
                        showStatus("✅ Payment Sent via SMS", true);
                        showSuccessDialog(amount, recipient);
                    });
                }
            } catch (Exception e) {
                Log.e("SwiftPay", "Payment fail", e);
                runOnUiThread(() -> showStatus("❌ Error: " + e.getMessage(), false));
            }
        });
    }

    private void sendDirectSMS(String data) {
        try {
            // Replace with your real backend/gateway number
            String destination = "+919000000000"; 
            android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
            
            // Large payloads need multipart SMS
            java.util.ArrayList<String> parts = smsManager.divideMessage("SP:" + data);
            smsManager.sendMultipartTextMessage(destination, null, parts, null, null);
            Log.d("SwiftPay", "Direct SMS Sent to " + destination);
        } catch (Exception e) {
            Log.e("SwiftPay", "SMS fail", e);
            runOnUiThread(() -> Toast.makeText(this, "SMS Permission Required", Toast.LENGTH_LONG).show());
        }
    }

    private void attemptBridgeUpload() {
        if (!isNetworkAvailable()) return;

        java.util.List<org.json.JSONObject> packets = com.example.offlinepay.mesh.PacketStore.getAllPackets(this);
        if (packets.isEmpty()) return;

        showStatus("📡 Bridging to Cloud...", true);
        
        for (org.json.JSONObject packet : packets) {
            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                packet.toString(), okhttp3.MediaType.get("application/json")
            );
            okhttp3.Request request = new okhttp3.Request.Builder()
                .url(SERVER_URL + "/api/bridge/ingest")
                .header("X-Bridge-Node-Id", "node-" + android.os.Build.MODEL)
                .post(body)
                .build();

            httpClient.newCall(request).enqueue(new okhttp3.Callback() {
                @Override public void onFailure(okhttp3.Call call, java.io.IOException e) { Log.e("Bridge", "Upload fail", e); }
                @Override public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                    if (response.isSuccessful()) {
                        Log.d("Bridge", "Packet uploaded successfully");
                        // Ideally remove from store here, but for gossip demo we can keep it
                    }
                }
            });
        }
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo net = cm.getActiveNetworkInfo();
        return net != null && net.isConnected();
    }


    private void showPinDialog(String amount, String recipient) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pin_entry);
        
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.7f); // Deep dim for focus
            window.setWindowAnimations(android.R.style.Animation_InputMethod); // Smooth slide up
        }

        pinBuffer.setLength(0);
        View[] dots = {
            dialog.findViewById(R.id.dot1), dialog.findViewById(R.id.dot2),
            dialog.findViewById(R.id.dot3), dialog.findViewById(R.id.dot4),
            dialog.findViewById(R.id.dot5), dialog.findViewById(R.id.dot6)
        };

        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        btnConfirm.setEnabled(false);
        btnConfirm.setAlpha(0.5f);

        int[] ids = {R.id.key0, R.id.key1, R.id.key2, R.id.key3, R.id.key4, R.id.key5, R.id.key6, R.id.key7, R.id.key8, R.id.key9};
        for (int id : ids) {
            Button key = dialog.findViewById(id);
            setupKeypadAnimation(key);
            key.setOnClickListener(v -> {
                if (pinBuffer.length() < 6) {
                    pinBuffer.append(key.getText());
                    updateDialogDots(dots);
                    if (pinBuffer.length() == 6) {
                        btnConfirm.setEnabled(true);
                        btnConfirm.setAlpha(1.0f);
                        btnConfirm.animate().scaleX(1.05f).scaleY(1.05f).setDuration(200).withEndAction(() -> 
                            btnConfirm.animate().scaleX(1f).scaleY(1f).setDuration(200)
                        ).start();
                    }
                }
            });
        }

        dialog.findViewById(R.id.keyBack).setOnClickListener(v -> {
            if (pinBuffer.length() > 0) {
                pinBuffer.deleteCharAt(pinBuffer.length()-1);
                updateDialogDots(dots);
                btnConfirm.setEnabled(false);
                btnConfirm.setAlpha(0.5f);
            }
        });
        
        dialog.findViewById(R.id.keyClear).setOnClickListener(v -> {
            pinBuffer.setLength(0);
            updateDialogDots(dots);
            btnConfirm.setEnabled(false);
            btnConfirm.setAlpha(0.5f);
        });

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            executeMeshPayment(recipient, amount);
        });

        dialog.show();
    }

    private void setupKeypadAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                if (event.getAction() == android.view.MotionEvent.ACTION_UP) v.performClick();
            }
            return true;
        });
    }

    private void updateDialogDots(View[] dots) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(i < pinBuffer.length() ? R.drawable.bg_pin_dot_filled : R.drawable.bg_pin_dot_empty);
        }
    }

    private void animateButton(View btn, Runnable onEnd) {
        ObjectAnimator sx = ObjectAnimator.ofFloat(btn, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(btn, "scaleY", 1f, 1.05f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(sx, sy);
        set.setDuration(300);
        set.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) { onEnd.run(); }
        });
        set.start();
    }

    private void showSuccessDialog(String amount, String recipient) {
        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_payment_success);
        ((TextView)dialog.findViewById(R.id.dialogAmount)).setText("₹" + amount);
        ((TextView)dialog.findViewById(R.id.dialogRecipient)).setText(recipient);
        dialog.findViewById(R.id.btnDone).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showStatus(String msg, boolean success) {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof HomeFragment && f.getView() != null) {
            TextView tvStatus = f.getView().findViewById(R.id.tvStatus);
            if (tvStatus != null) {
                tvStatus.setText(msg);
                tvStatus.setTextColor(success ? 0xCCFFFFFF : 0xFFFF6B6B);
            }
        }
    }

    @Override protected void onResume() { super.onResume(); meshManager.start(); attemptBridgeUpload(); }
    @Override protected void onPause() { super.onPause(); meshManager.stop(); }
}
