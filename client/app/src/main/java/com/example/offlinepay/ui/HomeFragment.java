package com.example.offlinepay.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.offlinepay.MainActivity;
import com.example.offlinepay.R;

public class HomeFragment extends Fragment {

    private EditText etRecipientVpa, etAmount;
    private Button btnPay;
    private TextView tvStatus;
    private MeshRadarView meshRadar;
    private View btnScanPay, chipUpi, chipPhone, chipContacts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        etRecipientVpa = view.findViewById(R.id.etRecipientVpa);
        etAmount = view.findViewById(R.id.etAmount);
        btnPay = view.findViewById(R.id.btnPay);
        tvStatus = view.findViewById(R.id.tvStatus);
        meshRadar = view.findViewById(R.id.meshRadar);
        btnScanPay = view.findViewById(R.id.btnScanPayWidget);
        chipUpi = view.findViewById(R.id.chipUpi);
        chipPhone = view.findViewById(R.id.chipPhone);
        chipContacts = view.findViewById(R.id.chipContacts);

        btnPay.setOnClickListener(v -> {
            String vpa = etRecipientVpa.getText().toString();
            String amount = etAmount.getText().toString();
            if (vpa.isEmpty() || amount.isEmpty()) {
                showStatus("❌ Please enter details", false);
                return;
            }
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).initiatePayment(amount, vpa);
            }
        });

        // Placeholder for chips
        View.OnClickListener chipListener = v -> showStatus("Feature coming soon", true);
        chipUpi.setOnClickListener(chipListener);
        chipPhone.setOnClickListener(chipListener);
        chipContacts.setOnClickListener(chipListener);
        btnScanPay.setOnClickListener(chipListener);

        return view;
    }

    private void showStatus(String msg, boolean success) {
        if (tvStatus != null) {
            tvStatus.setText(msg);
            tvStatus.setTextColor(success ? 0xCCFFFFFF : 0xFFFF6B6B);
        }
    }
}
