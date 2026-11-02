package com.example.cloneapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.cloneapp.R;
import com.example.cloneapp.adapters.CloneListAdapter;
import com.example.cloneapp.databinding.ActivityPrivacySpaceBinding;
import com.example.cloneapp.models.CloneModel;
import com.example.cloneapp.utils.CloneManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen presenting hidden clones locked inside the Privacy Space vault.
 */
public class PrivacySpaceActivity extends AppCompatActivity implements CloneListAdapter.OnCloneActionListener {

    private ActivityPrivacySpaceBinding binding;
    private CloneManager cloneManager;
    private CloneListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacySpaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cloneManager = CloneManager.getInstance(this);

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

        initRecyclerView();

        binding.fabAddPrivacyClone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open AppSelectActivity to create a new clone
                Intent intent = new Intent(PrivacySpaceActivity.this, AppSelectActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHiddenClones();
    }

    private void initRecyclerView() {
        adapter = new CloneListAdapter(this, new ArrayList<CloneModel>(), this);
        binding.rvPrivacyClones.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvPrivacyClones.setAdapter(adapter);
    }

    private void refreshHiddenClones() {
        List<CloneModel> hiddenClones = cloneManager.getHiddenClones();
        adapter.updateData(hiddenClones);

        if (hiddenClones.isEmpty()) {
            binding.layoutPrivacyEmpty.setVisibility(View.VISIBLE);
            binding.rvPrivacyClones.setVisibility(View.GONE);
        } else {
            binding.layoutPrivacyEmpty.setVisibility(View.GONE);
            binding.rvPrivacyClones.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLaunch(CloneModel clone) {
        cloneManager.launchClone(this, clone);
    }

    @Override
    public void onDetails(CloneModel clone) {
        Intent intent = new Intent(this, CloneDetailActivity.class);
        intent.putExtra(CloneDetailActivity.EXTRA_CLONE_ID, clone.getId());
        startActivity(intent);
    }

    @Override
    public void onRename(final CloneModel clone) {
        final TextInputEditText input = new TextInputEditText(this);
        input.setText(clone.getCustomName());
        input.setSelectAllOnFocus(true);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.menu_rename)
                .setView(input)
                .setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText() != null ? input.getText().toString().trim() : "";
                        if (!newName.isEmpty()) {
                            clone.setCustomName(newName);
                            cloneManager.updateClone(clone);
                            refreshHiddenClones();
                        }
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    public void onTogglePrivacy(CloneModel clone) {
        clone.setHidden(!clone.isHidden());
        cloneManager.updateClone(clone);
        refreshHiddenClones();
        Toast.makeText(this, "Restored to Home Screen", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete(final CloneModel clone) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.dialog_delete_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cloneManager.removeClone(clone.getId());
                        refreshHiddenClones();
                        Toast.makeText(PrivacySpaceActivity.this, "Clone deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
