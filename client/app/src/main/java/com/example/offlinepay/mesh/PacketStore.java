package com.example.offlinepay.mesh;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class PacketStore {
    private static final String PREFS_NAME = "MeshPackets";
    private static final String KEY_PACKETS = "pending_packets";

    public static synchronized void savePacket(Context context, JSONObject packet) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            JSONArray array = new JSONArray(prefs.getString(KEY_PACKETS, "[]"));
            
            // Deduplicate by packetId
            String newId = packet.getString("packetId");
            for (int i = 0; i < array.length(); i++) {
                if (array.getJSONObject(i).getString("packetId").equals(newId)) return;
            }

            array.put(packet);
            prefs.edit().putString(KEY_PACKETS, array.toString()).apply();
            Log.d("PacketStore", "Saved mesh packet: " + newId);
        } catch (Exception e) {
            Log.e("PacketStore", "Failed to save packet", e);
        }
    }

    public static synchronized List<JSONObject> getAllPackets(Context context) {
        List<JSONObject> list = new ArrayList<>();
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            JSONArray array = new JSONArray(prefs.getString(KEY_PACKETS, "[]"));
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getJSONObject(i));
            }
        } catch (Exception e) {
            Log.e("PacketStore", "Failed to load packets", e);
        }
        return list;
    }

    public static synchronized void clear(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
