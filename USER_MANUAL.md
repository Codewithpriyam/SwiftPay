# SwiftPay: User Manual & Feature Guide

Welcome to **SwiftPay**, your premium, offline-first UPI payment companion. This guide explains how to use the app and details the current working features.

---

## 1. Getting Started

### Initial Setup
1.  **Grant Permissions**: On first launch, the app will request **Bluetooth & Location Permissions**. This is required to find and communicate with nearby phones in the mesh network.
2.  **Visual Confirmation**: Look for the pulsing **Mesh Radar** at the top. This shows that your phone is actively scanning for peers to carry your payments.

---

## 2. Core Features

### A. Scan & Pay (Quickest Way)
1.  **Tap "Scan & Pay"**: The large pill-shaped button at the top opens the camera.
2.  **Scan a QR Code**: Point your camera at any standard UPI QR code.
3.  **Auto-Fill**: The app extracts the recipient's details.
4.  **Authorize**: Enter your PIN using the glass keypad and tap **Confirm**.

### B. Manual Transfer (Send Money)
1.  **Enter Details**: UPI ID / Phone and Amount.
2.  **Security Check**: Use the circular glass keypad to enter your **Secure PIN**.
3.  **Dispatch**: Tap **Confirm Payment**. The app will broadcast the payment into the mesh.

---

## 3. How the Mesh Network Works
SwiftPay is designed for areas with zero internet.
1.  **Hybrid Encryption**: Your payment is locked using **RSA + AES-256-GCM** (Bank Grade).
2.  **Gossip Broadcast**: The payment is broadcasted to nearby phones via Bluetooth.
3.  **The "Bridge"**: When any phone in the mesh (yours or a stranger's) gets internet, it silently uploads the payment to the cloud.
4.  **Idempotency**: The server ensures you are only charged once, even if multiple phones carry your payment.

---

## 4. Working Features Status

| Feature | Status | Description |
| :--- | :--- | :--- |
| **Mesh Radar** | ✅ WORKING | Live visual feedback of nearby peers and discovery status. |
| **Bluetooth Gossip** | ✅ WORKING | P2P packet propagation using Google Nearby Connections. |
| **Hybrid Crypto** | ✅ WORKING | RSA-OAEP + AES-GCM authenticated encryption for every packet. |
| **QR Scanner** | ✅ WORKING | Integrated camera scanner for UPI QR codes. |
| **Liquid Glass UI** | ✅ WORKING | Premium 60fps animated background and seamless glass cards. |
| **Auto-Bridge** | ✅ WORKING | Automatic background upload when internet connectivity is restored. |

---

## 5. Security & Safety

*   **Zero-Knowledge**: Intermediary phones *cannot* read your PIN or amount. They only see encrypted "packets."
*   **Unique Fingerprints**: Every packet has a SHA-256 hash to prevent tampering and duplication.
*   **Tactile Feedback**: Every PIN entry provides visual scaling feedback to ensure accuracy.

---

## 6. Troubleshooting

*   **Radar Not Pulsing**: Ensure Bluetooth is turned on and you have granted Location permissions.
*   **Payment Stuck**: If you stay offline for a long time, the payment stays in the mesh. It will settle as soon as *anyone* in the mesh gets internet.
*   **Camera Not Opening**: Check app settings to ensure Camera permission is granted.

---
*SwiftPay — Secured by Hybrid RSA • Driven by Mesh Networking*
