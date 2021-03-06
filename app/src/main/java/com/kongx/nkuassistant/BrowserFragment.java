package com.kongx.nkuassistant;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.List;


public class BrowserFragment extends Fragment {
    private static final String URL = "URL";
    private static List<BrowserFragment> list = new ArrayList<>();
    private String mUrl;
    public WebView webView = null;
    public BrowserFragment() { }
    public static BrowserFragment newInstance(String url) {
        BrowserFragment fragment = new BrowserFragment();
        Bundle args = new Bundle();
        args.putString(URL, url);
        fragment.setArguments(args);
        return fragment;
    }
    public static boolean hasInstance(){
        if(list.size() > 0 && list.get(list.size()-1).webView.canGoBack()){
            list.get(list.size()-1).webView.goBack();
            return true;
        }
        return false;
    }
    public static void clearInstance(){
        list.clear();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrl = getArguments().getString(URL);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_browser, container, false);
        webView = (WebView) v.findViewById(R.id.ic_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(mUrl);
        list.add(this);
        return v;
    }

}
