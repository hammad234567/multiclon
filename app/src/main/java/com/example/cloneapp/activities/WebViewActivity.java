package com.example.cloneapp.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cloneapp.databinding.ActivityWebViewBinding;

/**
 * In-app web viewer rendering embedded legal and privacy documentation.
 */
public class WebViewActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_CONTENT_TYPE = "EXTRA_CONTENT_TYPE";

    public static final int TYPE_PRIVACY = 1;
    public static final int TYPE_TERMS = 2;

    private ActivityWebViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        int type = getIntent().getIntExtra(EXTRA_CONTENT_TYPE, TYPE_PRIVACY);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (title != null) {
                getSupportActionBar().setTitle(title);
            }
        }
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupWebView();
        loadHtmlContent(type);
    }

    private void setupWebView() {
        WebSettings settings = binding.webView.getSettings();
        settings.setJavaScriptEnabled(false);
        settings.setDefaultTextEncodingName("utf-8");

        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                binding.progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    binding.progressBar.setVisibility(View.GONE);
                } else {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.progressBar.setProgress(newProgress);
                }
            }
        });
    }

    private void loadHtmlContent(int type) {
        String html;
        if (type == TYPE_TERMS) {
            html = "<html><head><style>" +
                    "body{font-family:sans-serif;padding:16px;color:#333;line-height:1.6;background-color:#FAFAFA;}" +
                    "h2{color:#6750A4;}h3{color:#311B92;}" +
                    "</style></head><body>" +
                    "<h2>Terms of Service</h2>" +
                    "<p>Welcome to Clone App Pro. By utilizing this dual-space management platform, you agree to comply with and be bound by the following conditions.</p>" +
                    "<h3>1. Parallel Application Cloning</h3>" +
                    "<p>Clone App Pro provides task-level multi-account isolation. Users are solely responsible for ensuring compliance with third-party app terms of service when operating secondary accounts.</p>" +
                    "<h3>2. Security &amp; Encryption</h3>" +
                    "<p>The application employs Android Keystore backed EncryptedSharedPreferences for securing user authentication PINs and local configurations.</p>" +
                    "<h3>3. Virtualized Identifiers</h3>" +
                    "<p>Virtual device identifiers and simulated GPS parameters are provided solely for test and simulation environments.</p>" +
                    "</body></html>";
        } else {
            html = "<html><head><style>" +
                    "body{font-family:sans-serif;padding:16px;color:#333;line-height:1.6;background-color:#FAFAFA;}" +
                    "h2{color:#6750A4;}h3{color:#311B92;}" +
                    "</style></head><body>" +
                    "<h2>Privacy Policy</h2>" +
                    "<p>Your privacy is paramount. Clone App Pro operates strictly on an on-device local architecture.</p>" +
                    "<h3>1. Data Collection</h3>" +
                    "<p>Clone App Pro does NOT collect, upload, or transmit personal information, target app credentials, or telemetry data to any external server.</p>" +
                    "<h3>2. Local Storage</h3>" +
                    "<p>All clone preferences, encrypted credentials, virtual device IDs, and simulated coordinates remain strictly stored within the app's local sandbox.</p>" +
                    "<h3>3. App Queries</h3>" +
                    "<p>QUERY_ALL_PACKAGES is utilized strictly on-device to present you with a list of apps you can clone.</p>" +
                    "</body></html>";
        }

        binding.webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }
}
