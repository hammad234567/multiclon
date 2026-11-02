package com.example.cloneapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cloneapp.R;
import com.example.cloneapp.adapters.CloneListAdapter;
import com.example.cloneapp.databinding.ActivityMainBinding;
import com.example.cloneapp.databinding.ItemDiscoverAppBinding;
import com.example.cloneapp.models.CloneModel;
import com.example.cloneapp.utils.AppUtils;
import com.example.cloneapp.utils.CloneManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Main application dashboard containing 3 tabs:
 * 1. Home (Grid of active clones + FAB)
 * 2. Discover (Featured cloneable applications)
 * 3. Me (User profile, Privacy Space entry, and Settings)
 */
public class MainActivity extends AppCompatActivity implements CloneListAdapter.OnCloneActionListener {

    private ActivityMainBinding binding;
    private CloneManager cloneManager;
    private CloneListAdapter cloneAdapter;

    // Featured apps model for Discover tab
    public static class FeaturedApp {
        String name;
        String packageName;
        String description;
        int fallbackIconRes;

        public FeaturedApp(String name, String packageName, String description, int fallbackIconRes) {
            this.name = name;
            this.packageName = packageName;
            this.description = description;
            this.fallbackIconRes = fallbackIconRes;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cloneManager = CloneManager.getInstance(this);

        initToolbar();
        initNavigation();
        initClonesRecyclerView();
        initDiscoverRecyclerView();
        initMeTab();

        // FAB -> Open App Selection
        binding.fabAddClone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AppSelectActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshClonesList();
    }

    private void initToolbar() {
        binding.topToolbar.inflateMenu(R.menu.menu_main);
        binding.topToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_vip) {
                    startActivity(new Intent(MainActivity.this, VipActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void initNavigation() {
        binding.bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    showTab(0);
                    return true;
                } else if (itemId == R.id.nav_discover) {
                    showTab(1);
                    return true;
                } else if (itemId == R.id.nav_me) {
                    showTab(2);
                    return true;
                }
                return false;
            }
        });
    }

    private void showTab(int index) {
        binding.containerHome.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        binding.containerDiscover.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        binding.containerMe.setVisibility(index == 2 ? View.VISIBLE : View.GONE);

        // Show FAB only on Home tab
        binding.fabAddClone.setVisibility(index == 0 ? View.VISIBLE : View.GONE);

        if (index == 0) {
            binding.topToolbar.setTitle(R.string.app_name);
            refreshClonesList();
        } else if (index == 1) {
            binding.topToolbar.setTitle(R.string.nav_discover);
        } else {
            binding.topToolbar.setTitle(R.string.nav_me);
        }
    }

    private void initClonesRecyclerView() {
        cloneAdapter = new CloneListAdapter(this, new ArrayList<CloneModel>(), this);
        binding.rvClones.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvClones.setAdapter(cloneAdapter);
    }

    private void refreshClonesList() {
        List<CloneModel> visibleClones = cloneManager.getVisibleClones();
        cloneAdapter.updateData(visibleClones);

        if (visibleClones.isEmpty()) {
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.rvClones.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.rvClones.setVisibility(View.VISIBLE);
        }
    }

    private void initDiscoverRecyclerView() {
        final List<FeaturedApp> featuredApps = new ArrayList<>();
        featuredApps.add(new FeaturedApp("WhatsApp Messenger", "com.whatsapp", "Dual WhatsApp accounts on one phone", R.drawable.ic_launcher_background));
        featuredApps.add(new FeaturedApp("Instagram", "com.instagram.android", "Run personal and business profiles", R.drawable.ic_launcher_background));
        featuredApps.add(new FeaturedApp("Facebook", "com.facebook.katana", "Log in to two Facebook accounts simultaneously", R.drawable.ic_launcher_background));
        featuredApps.add(new FeaturedApp("Telegram", "org.telegram.messenger", "Separate work & personal Telegram channels", R.drawable.ic_launcher_background));
        featuredApps.add(new FeaturedApp("Messenger", "com.facebook.orca", "Chat with multiple Facebook identities", R.drawable.ic_launcher_background));
        featuredApps.add(new FeaturedApp("TikTok", "com.zhiliaoapp.musically", "Manage multiple creator accounts", R.drawable.ic_launcher_background));
        featuredApps.add(new FeaturedApp("X (Twitter)", "com.twitter.android", "Switch seamlessly between Twitter handles", R.drawable.ic_launcher_background));
        featuredApps.add(new FeaturedApp("Discord", "com.discord", "Connect to separate gaming and work servers", R.drawable.ic_launcher_background));

        binding.rvDiscoverApps.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDiscoverApps.setAdapter(new RecyclerView.Adapter<DiscoverViewHolder>() {
            @NonNull
            @Override
            public DiscoverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ItemDiscoverAppBinding b = ItemDiscoverAppBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                );
                return new DiscoverViewHolder(b);
            }

            @Override
            public void onBindViewHolder(@NonNull DiscoverViewHolder holder, int position) {
                final FeaturedApp app = featuredApps.get(position);
                holder.binding.tvDiscoverName.setText(app.name);
                holder.binding.tvDiscoverDesc.setText(app.description);

                PackageManager pm = getPackageManager();
                try {
                    Drawable icon = pm.getApplicationIcon(app.packageName);
                    holder.binding.ivDiscoverIcon.setImageDrawable(icon);
                } catch (PackageManager.NameNotFoundException e) {
                    holder.binding.ivDiscoverIcon.setImageResource(app.fallbackIconRes);
                }

                holder.binding.btnDiscoverClone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleDiscoverClone(app);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return featuredApps.size();
            }
        });
    }

    private void handleDiscoverClone(FeaturedApp app) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(app.packageName, 0);
            // App is installed! Show clone prompt
            promptCloneApp(app.packageName, app.name);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, app.name + " is not installed on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void promptCloneApp(final String packageName, final String defaultName) {
        final View dialogView = getLayoutInflater().inflate(R.layout.activity_clone_detail, null);
        final TextInputEditText input = new TextInputEditText(this);
        input.setText(defaultName + " Clone");
        input.setSelectAllOnFocus(true);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_clone_title)
                .setView(input)
                .setPositiveButton(R.string.btn_clone, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText() != null ? input.getText().toString().trim() : defaultName;
                        if (name.isEmpty()) name = defaultName;

                        String iconBase64 = "";
                        try {
                            Drawable icon = getPackageManager().getApplicationIcon(packageName);
                            iconBase64 = AppUtils.drawableToBase64(icon);
                        } catch (Exception ignored) {
                        }

                        CloneModel newClone = new CloneModel(
                                UUID.randomUUID().toString(),
                                packageName,
                                name,
                                iconBase64,
                                false,
                                System.currentTimeMillis()
                        );
                        cloneManager.addClone(newClone);
                        Toast.makeText(MainActivity.this, R.string.clone_success, Toast.LENGTH_SHORT).show();
                        binding.bottomNav.setSelectedItemId(R.id.nav_home);
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void initMeTab() {
        // VIP Banner -> VipActivity
        binding.cardVipBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VipActivity.class));
            }
        });
        binding.btnMeUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VipActivity.class));
            }
        });

        // Privacy Space -> PrivacySpaceAuthActivity
        binding.rowPrivacySpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PrivacySpaceAuthActivity.class));
            }
        });

        // VIP Plans Row -> VipActivity
        binding.rowVipPlans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VipActivity.class));
            }
        });

        // Settings -> SettingsActivity
        binding.rowSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        // About -> AboutActivity
        binding.rowAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
        });
    }

    // ----------------------------------------------------
    // CloneListAdapter.OnCloneActionListener Implementations
    // ----------------------------------------------------

    @Override
    public void onLaunch(CloneModel clone) {
        cloneManager.launchClone(this, clone);
    }

    @Override
    public void onDetails(CloneModel clone) {
        Intent intent = new Intent(this, CloneDetailActivity.class);
        intent.putExtra("EXTRA_CLONE_ID", clone.getId());
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
                            refreshClonesList();
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
        refreshClonesList();
        String msg = clone.isHidden() ? "Moved to Privacy Space" : "Restored to Home Screen";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
                        refreshClonesList();
                        Toast.makeText(MainActivity.this, "Clone deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    static class DiscoverViewHolder extends RecyclerView.ViewHolder {
        ItemDiscoverAppBinding binding;

        public DiscoverViewHolder(ItemDiscoverAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
