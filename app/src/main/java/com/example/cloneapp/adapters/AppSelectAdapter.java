package com.example.cloneapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cloneapp.databinding.ItemAppSelectBinding;
import com.example.cloneapp.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying searchable list of installed applications available for cloning.
 */
public class AppSelectAdapter extends RecyclerView.Adapter<AppSelectAdapter.AppViewHolder> implements Filterable {

    public interface OnAppSelectListener {
        void onAppSelected(AppUtils.AppInfo appInfo);
    }

    private final List<AppUtils.AppInfo> originalList;
    private List<AppUtils.AppInfo> filteredList;
    private final OnAppSelectListener listener;

    public AppSelectAdapter(List<AppUtils.AppInfo> appList, OnAppSelectListener listener) {
        this.originalList = appList != null ? new ArrayList<>(appList) : new ArrayList<>();
        this.filteredList = appList != null ? new ArrayList<>(appList) : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<AppUtils.AppInfo> newList) {
        this.originalList.clear();
        if (newList != null) {
            this.originalList.addAll(newList);
        }
        this.filteredList = new ArrayList<>(this.originalList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppSelectBinding binding = ItemAppSelectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new AppViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppUtils.AppInfo appInfo = filteredList.get(position);
        holder.bind(appInfo);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint == null ? "" : constraint.toString().trim().toLowerCase();
                List<AppUtils.AppInfo> resultList = new ArrayList<>();

                if (query.isEmpty()) {
                    resultList.addAll(originalList);
                } else {
                    for (AppUtils.AppInfo app : originalList) {
                        if (app.getAppName().toLowerCase().contains(query)
                                || app.getPackageName().toLowerCase().contains(query)) {
                            resultList.add(app);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = resultList;
                results.count = resultList.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (List<AppUtils.AppInfo>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    class AppViewHolder extends RecyclerView.ViewHolder {
        private final ItemAppSelectBinding binding;

        public AppViewHolder(ItemAppSelectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final AppUtils.AppInfo appInfo) {
            binding.tvAppName.setText(appInfo.getAppName());
            binding.tvPackageName.setText(appInfo.getPackageName());
            binding.ivAppIcon.setImageDrawable(appInfo.getIcon());

            if (appInfo.isSystemApp()) {
                binding.tvSystemTag.setVisibility(View.VISIBLE);
            } else {
                binding.tvSystemTag.setVisibility(View.GONE);
            }

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onAppSelected(appInfo);
                    }
                }
            };

            binding.cardAppItem.setOnClickListener(clickListener);
            binding.btnCloneApp.setOnClickListener(clickListener);
        }
    }
}
