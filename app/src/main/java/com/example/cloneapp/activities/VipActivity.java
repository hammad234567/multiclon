package com.example.cloneapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cloneapp.R;
import com.example.cloneapp.databinding.ActivityVipBinding;
import com.example.cloneapp.utils.PrefsHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * VIP Membership subscription screen offering Monthly, Yearly, and Lifetime plans
 * with feature breakdown and simulated checkout.
 */
public class VipActivity extends AppCompatActivity {

    private ActivityVipBinding binding;
    private int selectedPlan = 1; // 0: Monthly, 1: Yearly (default), 2: Lifetime

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupPlanSelection();
        updatePlanSelectionUI();

        binding.btnSubscribeVip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String planName = selectedPlan == 0 ? "Monthly ($4.99)" : (selectedPlan == 1 ? "Yearly ($29.99)" : "Lifetime ($59.99)");
                new MaterialAlertDialogBuilder(VipActivity.this)
                        .setTitle("Upgrade to " + planName)
                        .setMessage(R.string.billing_notice)
                        .setPositiveButton("Simulate VIP Unlock", (dialog, which) -> {
                            PrefsHelper.getInstance(VipActivity.this).setVip(true);
                            Toast.makeText(VipActivity.this, "VIP features unlocked successfully!", Toast.LENGTH_LONG).show();
                            finish();
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();
            }
        });
    }

    private void setupPlanSelection() {
        binding.cardPlanMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPlan = 0;
                updatePlanSelectionUI();
            }
        });

        binding.cardPlanYearly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPlan = 1;
                updatePlanSelectionUI();
            }
        });

        binding.cardPlanLifetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPlan = 2;
                updatePlanSelectionUI();
            }
        });
    }

    private void updatePlanSelectionUI() {
        binding.radioMonthly.setChecked(selectedPlan == 0);
        binding.radioYearly.setChecked(selectedPlan == 1);
        binding.radioLifetime.setChecked(selectedPlan == 2);

        int primaryColor = ContextCompat.getColor(this, R.color.primary);
        int outlineColor = ContextCompat.getColor(this, R.color.outline_variant);

        // Highlight selected card border
        binding.cardPlanMonthly.setStrokeColor(selectedPlan == 0 ? primaryColor : outlineColor);
        binding.cardPlanMonthly.setStrokeWidth(selectedPlan == 0 ? 4 : 2);

        binding.cardPlanYearly.setStrokeColor(selectedPlan == 1 ? primaryColor : outlineColor);
        binding.cardPlanYearly.setStrokeWidth(selectedPlan == 1 ? 5 : 2);

        binding.cardPlanLifetime.setStrokeColor(selectedPlan == 2 ? primaryColor : outlineColor);
        binding.cardPlanLifetime.setStrokeWidth(selectedPlan == 2 ? 4 : 2);
    }
}
