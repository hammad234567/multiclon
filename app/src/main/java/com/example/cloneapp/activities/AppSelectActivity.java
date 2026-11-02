package com.example.cloneapp.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cloneapp.R;
import com.example.cloneapp.adapters.AppSelectAdapter;
import com.example.cloneapp.databinding.ActivityAppSelectBinding;
import com.example.cloneapp.models.CloneModel;
import com.example.cloneapp.utils.AppUtils;
import com.example.cloneapp.utils.CloneManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity presenting user with searchable list of installed applications to clone.
 */
public class AppSelectActivity extends AppCompatActivity implements AppSelectAdapter.OnAppSelectListener {

    private ActivityAppSelectBinding binding;
    private AppSelectAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppSelectBinding.inflate(getLayoutInflater());
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

        initViews();
        loadApps(false);
    }

    private void initViews() {
        adapter = new AppSelectAdapter(new ArrayList<AppUtils.AppInfo>(), this);
        binding.rvSelectApps.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSelectApps.setAdapter(adapter);

        // Search text watcher
        binding.etSearchApp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // System apps toggle
        binding.switchSystemApps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loadApps(isChecked);
            }
        });
    }

    private void loadApps(final boolean includeSystem) {
        binding.progressLoadingApps.setVisibility(View.VISIBLE);
        binding.rvSelectApps.setVisibility(View.GONE);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<AppUtils.AppInfo> apps = AppUtils.getInstalledApps(AppSelectActivity.this, includeSystem);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            binding.progressLoadingApps.setVisibility(View.GONE);
                            binding.rvSelectApps.setVisibility(View.VISIBLE);
                            adapter.updateData(apps);
                            binding.tvAppCount.setText(apps.size() + " apps found");
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onAppSelected(final AppUtils.AppInfo appInfo) {
        final TextInputEditText input = new TextInputEditText(this);
        input.setText(appInfo.getAppName() + " Clone");
        input.setSelectAllOnFocus(true);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_clone_title)
                .setMessage("Create an isolated clone instance for " + appInfo.getAppName() + "?")
                .setView(input)
                .setPositiveButton(R.string.btn_clone, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String cloneName = input.getText() != null ? input.getText().toString().trim() : "";
                        if (cloneName.isEmpty()) {
                            cloneName = appInfo.getAppName() + " Clone";
                        }

                        // Encode app icon to base64
                        String iconBase64 = AppUtils.drawableToBase64(appInfo.getIcon());

                        CloneModel newClone = new CloneModel(
                                UUID.randomUUID().toString(),
                                appInfo.getPackageName(),
                                cloneName,
                                iconBase64,
                                false,
                                System.currentTimeMillis()
                        );

                        CloneManager.getInstance(AppSelectActivity.this).addClone(newClone);
                        Toast.makeText(AppSelectActivity.this, R.string.clone_success, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
