package com.example.offlinepay.mesh;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.*;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class MeshManager {
    private static final String TAG = "MeshManager";
    private static final String SERVICE_ID = "com.example.offlinepay.MESH";
    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;

    private final Context context;
    private final ConnectionsClient connectionsClient;
    private final Set<String> connectedEndpoints = new HashSet<>();
    private Listener listener;

    public interface Listener {
        void onPeerCountChanged(int count);
    }

    public MeshManager(Context context) {
        this.context = context;
        this.connectionsClient = Nearby.getConnectionsClient(context);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public int getPeerCount() {
        return connectedEndpoints.size();
    }

    public void start() {
        startAdvertising();
        startDiscovery();
    }

    public void stop() {
        connectionsClient.stopAdvertising();
        connectionsClient.stopDiscovery();
        connectionsClient.stopAllEndpoints();
    }

    private void startAdvertising() {
        AdvertisingOptions options = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        connectionsClient.startAdvertising("SwiftNode", SERVICE_ID, connectionLifecycleCallback, options)
            .addOnSuccessListener(a -> Log.d(TAG, "Advertising..."))
            .addOnFailureListener(e -> Log.e(TAG, "Ad fail", e));
    }

    private void startDiscovery() {
        DiscoveryOptions options = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        connectionsClient.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
            .addOnSuccessListener(a -> Log.d(TAG, "Discovering..."))
            .addOnFailureListener(e -> Log.e(TAG, "Disc fail", e));
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
            Log.d(TAG, "Found peer: " + endpointId);
            connectionsClient.requestConnection("SwiftNode", endpointId, connectionLifecycleCallback);
        }
        @Override public void onEndpointLost(String endpointId) {}
    };

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo info) {
            connectionsClient.acceptConnection(endpointId, payloadCallback);
        }
        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            if (result.getStatus().isSuccess()) {
                connectedEndpoints.add(endpointId);
                if (listener != null) listener.onPeerCountChanged(connectedEndpoints.size());
                syncPackets(endpointId);
            }
        }
        @Override
        public void onDisconnected(String endpointId) {
            connectedEndpoints.remove(endpointId);
            if (listener != null) listener.onPeerCountChanged(connectedEndpoints.size());
        }
    };

    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            try {
                String jsonStr = new String(payload.asBytes(), StandardCharsets.UTF_8);
                JSONObject packet = new JSONObject(jsonStr);
                
                // Gossip: Save and Forward
                PacketStore.savePacket(context, packet);
                broadcast(packet);
            } catch (Exception e) { Log.e(TAG, "Payload error", e); }
        }
        @Override public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {}
    };

    public void broadcast(JSONObject packet) {
        byte[] bytes = packet.toString().getBytes(StandardCharsets.UTF_8);
        Payload payload = Payload.fromBytes(bytes);
        for (String endpoint : connectedEndpoints) {
            connectionsClient.sendPayload(endpoint, payload);
        }
    }

    private void syncPackets(String endpointId) {
        for (JSONObject packet : PacketStore.getAllPackets(context)) {
            connectionsClient.sendPayload(endpointId, Payload.fromBytes(packet.toString().getBytes(StandardCharsets.UTF_8)));
        }
    }
}
