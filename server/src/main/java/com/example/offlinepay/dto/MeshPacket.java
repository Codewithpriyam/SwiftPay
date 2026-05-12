package com.example.offlinepay.dto;

import lombok.Data;

@Data
public class MeshPacket {
    private String packetId;
    private int ttl;
    private long createdAt;
    private String ciphertext;
}
