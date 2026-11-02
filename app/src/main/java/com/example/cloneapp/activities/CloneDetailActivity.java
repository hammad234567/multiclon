package com.example.cloneapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cloneapp.R;
import com.example.cloneapp.databinding.ActivityCloneDetailBinding;
import com.example.cloneapp.models.CloneModel;
import com.example.cloneapp.utils.AppUtils;
import com.example.cloneapp.utils.CloneManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DateFormat;
import java.util.Date;

/**
 * Detailed management screen for an individual clone. Allows modifying clone metadata,
 * launching the clone, navigating to privacy/GPS configurations, or deleting the clone.
 */
public class CloneDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CLONE_ID = "EXTRA_CLONE_ID";

    private ActivityCloneDetailBinding binding;
    private CloneManager cloneManager;
    private CloneModel clone;
    private String cloneId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCloneDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cloneManager = CloneManager.getInstance(this);
        cloneId = getIntent().getStringExtra(EXTRA_CLONE_ID);

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

        loadCloneData();
        setupActionListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCloneData();
    }

    private void loadCloneData() {
        if (cloneId == null) {
            Toast.makeText(this, "Clone not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        clone = cloneManager.getCloneById(cloneId);
        if (clone == null) {
            Toast.makeText(this, "Clone not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Title & Name
        binding.toolbar.setTitle(clone.getCustomName());
        binding.etDetailName.setText(clone.getCustomName());

        // Icon
        Drawable icon = null;
        if (clone.getCustomIconBase64() != null && !clone.getCustomIconBase64().isEmpty()) {
            icon = AppUtils.base64ToDrawable(this, clone.getCustomIconBase64());
        }
        if (icon == null) {
            try {
                icon = getPackageManager().getApplicationIcon(clone.getTargetPackage());
            } catch (PackageManager.NameNotFoundException ignored) {}
        }
        if (icon != null) {
            binding.ivDetailIcon.setImageDrawable(icon);
        }

        // Summary details
        binding.tvDetailPackage.setText(clone.getTargetPackage());
        binding.tvDetailId.setText(clone.getId());
        binding.tvDetailAffinity.setText("com.example.cloneapp.clone." + clone.getId().substring(0, Math.min(8, clone.getId().length())));

        String dateStr = DateFormat.getDateTimeInstance().format(new Date(clone.getCreatedAt()));
        binding.tvDetailCreated.setText(dateStr);

        // Privacy space status text
        if (clone.isHidden()) {
            binding.tvPrivacyToggleText.setText(R.string.btn_unhide_app);
            binding.ivPrivacyToggleIcon.setImageResource(R.drawable.ic_lock);
        } else {
            binding.tvPrivacyToggleText.setText(R.string.btn_hide_app);
            binding.ivPrivacyToggleIcon.setImageResource(R.drawable.ic_lock);
        }
    }

    private void setupActionListeners() {
        // Save Name
        binding.btnSaveName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = binding.etDetailName.getText() != null ? binding.etDetailName.getText().toString().trim() : "";
                if (!newName.isEmpty() && clone != null) {
                    clone.setCustomName(newName);
                    cloneManager.updateClone(clone);
                    binding.toolbar.setTitle(newName);
                    Toast.makeText(CloneDetailActivity.this, "Clone name saved", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Launch
        binding.rowLaunchClone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clone != null) {
                    cloneManager.launchClone(CloneDetailActivity.this, clone);
                }
            }
        });

        // Device Privacy
        binding.rowDevicePrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CloneDetailActivity.this, DevicePrivacyActivity.class);
                intent.putExtra(DevicePrivacyActivity.EXTRA_CLONE_ID, cloneId);
                startActivity(intent);
            }
        });

        // Fake GPS
        binding.rowFakeGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CloneDetailActivity.this, GpsSettingActivity.class);
                intent.putExtra(GpsSettingActivity.EXTRA_CLONE_ID, cloneId);
                startActivity(intent);
            }
        });

        // Hide / Unhide in Privacy Space
        binding.rowTogglePrivacySpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clone != null) {
                    clone.setHidden(!clone.isHidden());
                    cloneManager.updateClone(clone);
                    loadCloneData();
                    String msg = clone.isHidden() ? "Moved to Privacy Space" : "Restored to Home Screen";
                    Toast.makeText(CloneDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Delete Clone
        binding.rowDeleteClone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(CloneDetailActivity.this)
                        .setTitle(R.string.dialog_delete_title)
                        .setMessage(R.string.dialog_delete_message)
                        .setPositiveButton(R.string.dialog_delete_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cloneManager.removeClone(cloneId);
                                Toast.makeText(CloneDetailActivity.this, "Clone deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();
            }
        });
    }
}
