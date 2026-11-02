package com.example.cloneapp.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cloneapp.R;
import com.example.cloneapp.databinding.ItemCloneCardBinding;
import com.example.cloneapp.models.CloneModel;
import com.example.cloneapp.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter presenting cloned applications in a grid format with launch and management actions.
 */
public class CloneListAdapter extends RecyclerView.Adapter<CloneListAdapter.CloneViewHolder> {

    public interface OnCloneActionListener {
        void onLaunch(CloneModel clone);
        void onDetails(CloneModel clone);
        void onRename(CloneModel clone);
        void onTogglePrivacy(CloneModel clone);
        void onDelete(CloneModel clone);
    }

    private final Context context;
    private List<CloneModel> cloneList;
    private final OnCloneActionListener listener;

    public CloneListAdapter(Context context, List<CloneModel> cloneList, OnCloneActionListener listener) {
        this.context = context;
        this.cloneList = cloneList != null ? cloneList : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<CloneModel> newList) {
        this.cloneList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CloneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCloneCardBinding binding = ItemCloneCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new CloneViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CloneViewHolder holder, int position) {
        CloneModel clone = cloneList.get(position);
        holder.bind(clone);
    }

    @Override
    public int getItemCount() {
        return cloneList.size();
    }

    class CloneViewHolder extends RecyclerView.ViewHolder {
        private final ItemCloneCardBinding binding;

        public CloneViewHolder(ItemCloneCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final CloneModel clone) {
            binding.tvCloneName.setText(clone.getCustomName());
            binding.tvClonePackage.setText(clone.getTargetPackage());

            // Load Icon: try Base64 first, fallback to PackageManager
            Drawable iconDrawable = null;
            if (clone.getCustomIconBase64() != null && !clone.getCustomIconBase64().isEmpty()) {
                iconDrawable = AppUtils.base64ToDrawable(context, clone.getCustomIconBase64());
            }

            if (iconDrawable != null) {
                binding.ivCloneIcon.setImageDrawable(iconDrawable);
            } else {
                try {
                    PackageManager pm = context.getPackageManager();
                    Drawable appIcon = pm.getApplicationIcon(clone.getTargetPackage());
                    binding.ivCloneIcon.setImageDrawable(appIcon);
                } catch (PackageManager.NameNotFoundException e) {
                    binding.ivCloneIcon.setImageResource(R.drawable.ic_launcher_background);
                }
            }

            // Launch Button
            binding.btnCloneLaunch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onLaunch(clone);
                    }
                }
            });

            // Card Click -> Details
            binding.cardCloneRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDetails(clone);
                    }
                }
            });

            // Card Long Press -> show Popup Menu
            binding.cardCloneRoot.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showPopupMenu(binding.ivCloneMenu, clone);
                    return true;
                }
            });

            // Menu button click
            binding.ivCloneMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu(v, clone);
                }
            });
        }

        private void showPopupMenu(View anchor, final CloneModel clone) {
            PopupMenu popup = new PopupMenu(context, anchor);
            popup.getMenu().add(0, 1, 0, R.string.btn_launch);
            popup.getMenu().add(0, 2, 1, R.string.menu_rename);
            popup.getMenu().add(0, 3, 2, R.string.menu_details);
            popup.getMenu().add(0, 4, 3, clone.isHidden() ? R.string.menu_unhide : R.string.menu_move_to_privacy);
            popup.getMenu().add(0, 5, 4, R.string.menu_delete);

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (listener == null) return false;
                    switch (item.getItemId()) {
                        case 1:
                            listener.onLaunch(clone);
                            return true;
                        case 2:
                            listener.onRename(clone);
                            return true;
                        case 3:
                            listener.onDetails(clone);
                            return true;
                        case 4:
                            listener.onTogglePrivacy(clone);
                            return true;
                        case 5:
                            listener.onDelete(clone);
                            return true;
                    }
                    return false;
                }
            });
            popup.show();
        }
    }
}
