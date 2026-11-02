package com.example.cloneapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cloneapp.R;
import com.example.cloneapp.databinding.ActivityAboutBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * About screen displaying application version, developer credits, licenses, and legal terms.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
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

        // Privacy Policy -> WebView
        binding.rowPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_TITLE, getString(R.string.link_privacy));
                intent.putExtra(WebViewActivity.EXTRA_CONTENT_TYPE, WebViewActivity.TYPE_PRIVACY);
                startActivity(intent);
            }
        });

        // Terms of Service -> WebView
        binding.rowTermsService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_TITLE, getString(R.string.link_terms));
                intent.putExtra(WebViewActivity.EXTRA_CONTENT_TYPE, WebViewActivity.TYPE_TERMS);
                startActivity(intent);
            }
        });

        // Open Source Licenses
        binding.rowLicenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(AboutActivity.this)
                        .setTitle(R.string.link_licenses)
                        .setMessage("Clone App Pro utilizes the following open-source software libraries:\n\n" +
                                "• AndroidX AppCompat (Apache 2.0)\n" +
                                "• Material Components for Android (Apache 2.0)\n" +
                                "• AndroidX Security Crypto (Apache 2.0)\n" +
                                "• Google Gson (Apache 2.0)\n" +
                                "• Bumptech Glide (BSD / MIT / Apache 2.0)")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });

        // Check for updates
        binding.rowCheckUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AboutActivity.this, R.string.msg_latest_version, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
