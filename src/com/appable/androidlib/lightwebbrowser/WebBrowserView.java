package com.appable.androidlib.lightwebbrowser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import org.itri.html5webview.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;
import android.widget.FrameLayout;

public class WebBrowserView extends FrameLayout {

    private final String LOGTAG =  getClass().getName();
	private static String PLAY_MEDIA= 
			"<html>" +
				"<head>"+
			  		"<title>Internal Media Player: Appable</title>"+
		  		"</head>"+
				"<body>" +
					"<div style=\"text-align: center;\" >" +
						"<video controls autoplay width=\"640\" height=\"480\" >" +
							"<source src=\"%s\" type=\"%s\">" +
						"</video>" +
					"</div>" +
					"<script>"+
					    "var video = document.getElementsByTagName(\'video\')[0];"+
						"function stopVideo(){ "+
							"video.pause();} "+
						"window.onunload = stopVideo; "+
					  "</script>" +
				"</body>" +
			"</html>";
	private static final String TEXT_CONTENT_TYPE = "text/html";
	
	private OnLoadedURL mLoadedURLltn;
	
	public void setOnLoadedURL(OnLoadedURL mltn){
		mLoadedURLltn = mltn;
	}
	
	private Context 							mContext;
	private MyWebChromeClient					mWebChromeClient;
	private MyWebViewClient						mWebViewClient;
	private View								mCustomView;
	private FrameLayout							mCustomViewContainer;
	private WebChromeClient.CustomViewCallback 	mCustomViewCallback;
	private WebView								mWebView;
	
	public WebView getWebView(){
		return mWebView;
	}

	public boolean canGoBack(){
		return (mWebView!= null && mCustomView==null &&mWebView.canGoBack());
	}
	
	public void goBack(){
		mWebView.goBack();
	}
	
	public void loadData(String data){
		mWebView.loadData(data, "text/html", "utf-8");
	}
	
	public void loadUrl(String url){
		String trueUrl="";
		if(URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)){
			trueUrl = url;
		}else if(!URLUtil.isFileUrl(url) || 
				!URLUtil.isAssetUrl(url)){
//			trueUrl = "http://"+url;
			trueUrl = url;
		}else{
			trueUrl = url;
		}
//		mWebView.loadUrl(trueUrl);
		
		if(!mWebView.canGoBack()){
			mWebViewClient.shouldOverrideUrlLoading(mWebView, trueUrl);
			Log.i(LOGTAG,"can not go back");
		}else{
			mWebView.loadUrl(trueUrl);
			Log.i(LOGTAG,"can go back");
		}
		
	}
	
	public void restoreState(Bundle savedInstanceState){
		mWebView.restoreState(savedInstanceState);
	}
	
	public void saveState(Bundle outState){
		mWebView.saveState(outState);
	}
	
	public void stopLoading(){
		mWebView.stopLoading();
	}
	
	private void init(Context context) {
		mContext = context;		
		Activity a = (Activity) mContext;
		
		initObject();
		initGUI();
		initEvent();
	}
	
	private void initObject(){
		
		FrameLayout browserContent = (FrameLayout)LayoutInflater.from(mContext).inflate(R.layout.custom_screen, this);
//		init
		mWebView = (WebView) browserContent.findViewById(R.id.mwebview);
		mCustomViewContainer = (FrameLayout)findViewById(R.id.fullscreen_custom_content);
	}
	
	private void initGUI(){
		
		if(mWebChromeClient == null)
			mWebChromeClient = new MyWebChromeClient();
		
		if(mWebViewClient == null)
			mWebViewClient = new MyWebViewClient();
		
		mWebView.setWebChromeClient(mWebChromeClient);
		mWebView.setWebViewClient(mWebViewClient);
		// Configure the webview
	    WebSettings s = mWebView.getSettings();
		s.setPluginState(PluginState.ON);
		s.setAllowFileAccess(true);
		s.setSupportZoom(true);
        s.setLoadsImagesAutomatically(true);
        s.setUserAgentString(getDefaultUserAgent());
        

	    Log.i("","user agent:"+ s.getUserAgentString());
	    s.setBuiltInZoomControls(true);
	    s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
	    s.setUseWideViewPort(true);
	    s.setLoadWithOverviewMode(true);
	    s.setSavePassword(true);
	    s.setSaveFormData(true);
	    s.setJavaScriptEnabled(true);
        
        this.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        this.setScrollbarFadingEnabled(true);
	    
	    // enable navigator.geolocation 
	    s.setGeolocationEnabled(true);
	    
	    // enable Web Storage: localStorage, sessionStorage
	    s.setDomStorageEnabled(true);
	}
	
	private void initEvent(){
	}

	public String getDefaultUserAgent() {
	    StringBuilder result = new StringBuilder(64);
	    result.append("Mozilla/5.0");
//	    result.append(System.getProperty("java.vm.version")); // such as 1.1.0
	    
	    result.append(" (Linux; U; Android ");

	    String version = Build.VERSION.RELEASE; // "1.0" or "3.4b5"
	    result.append(version.length() > 0 ? version : "1.0");

	    Locale curLocale = mContext.getResources().getConfiguration().locale;
	    
	    result.append("; "+curLocale.toString());
	    
	    // add the model for the release build
	    if ("REL".equals(Build.VERSION.CODENAME)) {
	        String model = Build.MODEL;
	        if (model.length() > 0) {
	            result.append("; ");
	            result.append(model);
	        }
	    }
	    String id = Build.ID; // "MASTER" or "M4-rc20"
	    if (id.length() > 0) {
	        result.append(" Build/");
	        result.append(id);
	    }
	    result.append(")");
	    
	    String subfix = " AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0%sSafari/534.30";
	    if(isTablet()){
	    	result.append(String.format(subfix, " "));
	    }else{
	    	result.append(String.format(subfix, " Mobile "));
	    }
	    
	    return result.toString();
	}   

	private boolean isTablet(){
	    boolean istablet = ((mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE);
	    return istablet;
	}
	
	public WebBrowserView(Context context) {
		super(context);
		if(!isInEditMode()){
			init(context);
		}
	}

	
	public WebBrowserView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(!isInEditMode()){
			init(context);
		}
	}

	public WebBrowserView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if(!isInEditMode()){
			init(context);
		}
	}
	
    public boolean inCustomView() {
		return (mCustomView != null);
	}
    
    public void hideCustomView() {
		mWebChromeClient.onHideCustomView();
	}
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		if ((mCustomView == null) && mWebView.canGoBack()){
    			Log.i(LOGTAG,"goback");
    			mWebView.goBack();
    			return true;
    		}
    	}
    	return false;
    }

    private class MyWebChromeClient extends WebChromeClient {
		private Bitmap 		mDefaultVideoPoster;
		private View 		mVideoProgressView;
    	
    	@Override
		public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback)
		{
			Log.i(LOGTAG, "here in on ShowCustomView");
	        mWebView.setVisibility(View.GONE);
	        
	        // if a view already exists then immediately terminate the new one
	        if (mCustomView != null) {
	            callback.onCustomViewHidden();
	            return;
	        }
	        
	        mCustomViewContainer.addView(view);
	        mCustomView = view;
	        mCustomViewCallback = callback;
	        mCustomViewContainer.setVisibility(View.VISIBLE);
		}
		
		@Override
		public void onHideCustomView() {
			
			if (mCustomView == null)
				return;	       
			
			// Hide the custom view.
			mCustomView.setVisibility(View.GONE);
			
			// Remove the custom view from its container.
			mCustomViewContainer.removeView(mCustomView);
			mCustomView = null;
			mCustomViewContainer.setVisibility(View.GONE);
			mCustomViewCallback.onCustomViewHidden();
			
			mWebView.setVisibility(View.VISIBLE);
			
	        //Log.i(LOGTAG, "set it to webVew");
		}
		
		@Override
		public Bitmap getDefaultVideoPoster() {
			//Log.i(LOGTAG, "here in on getDefaultVideoPoster");	
			if (mDefaultVideoPoster == null) {
				mDefaultVideoPoster = BitmapFactory.decodeResource(
						getResources(), R.drawable.icon);
		    }
			return mDefaultVideoPoster;
		}
		
		@Override
		public View getVideoLoadingProgressView() {
			//Log.i(LOGTAG, "here in on getVideoLoadingPregressView");
			
	        if (mVideoProgressView == null) {
	            LayoutInflater inflater = LayoutInflater.from(mContext);
	            mVideoProgressView = inflater.inflate(R.layout.video_loading_progress, null);
	        }
	        return mVideoProgressView; 
		}
    	
    	 @Override
         public void onReceivedTitle(WebView view, String title) {
    		 
//            ((Activity) mContext).setTitle(title);
    		 if(mLoadedURLltn!=null){
    			 mLoadedURLltn.receivedTitle(view, title);
    		 }
         }

         @Override
         public void onProgressChanged(WebView view, int newProgress) {
        	 ((Activity) mContext).getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress*100);
         }
         
         @Override
         public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
             callback.invoke(origin, true, false);
         }
    }
	
	private class MyWebViewClient extends WebViewClient {
		@SuppressLint("NewApi")
		@Override
        public boolean  shouldOverrideUrlLoading  (WebView  view, String  url){
        	
        	String mimeType = null;
        	String location = null;
        	// this is to handle call from main thread
            StrictMode.ThreadPolicy prviousThreadPolicy = StrictMode.getThreadPolicy();

            // temporary allow network access main thread
            // in order to get mime type from content-type
            StrictMode.ThreadPolicy permitAllPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(permitAllPolicy);
            URLConnection connection = null;
            String orgUrl = url;
            while(true){
            	try{
            		URL mURL = new URL(orgUrl);
	            	connection = mURL.openConnection();
	                connection.setConnectTimeout(0);
	                connection.setReadTimeout(0);
	                
	                mimeType = connection.getContentType();
	                Log.i("WebBrowserView","mimeType:"+ mimeType);
	                location = connection.getHeaderField("Location");
	                Log.i("WebBrowserView","location:"+ location);
	                if(mimeType == null){
	                	mimeType = URLConnection.guessContentTypeFromName(orgUrl);
	                }
	                Log.i("", "mimeType from content-type "+ mimeType);
	            } catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            finally{
	                // restore main thread's default network access policy
	                StrictMode.setThreadPolicy(prviousThreadPolicy);
	                connection = null;
	            }
            	if(location!=null){
                	orgUrl = location;
                	Log.i(LOGTAG,"location: "+ location);
                }else{
                	Log.i(LOGTAG,"location: null");
                	break;
                }
            }
        	
        	if(mLoadedURLltn!=null){
            	mLoadedURLltn.loadedURL(view, orgUrl);
            }
        	
            if(isMediaContent(mimeType)){
            	view.loadDataWithBaseURL(orgUrl, String.format(PLAY_MEDIA, orgUrl, mimeType), TEXT_CONTENT_TYPE, null, orgUrl);
        		return false;
        	}else{
        		view.loadUrl(orgUrl);
                return false;
        	}
        }
        
		private void getHeader(URLConnection connection){
			int count = connection.getHeaderFields().size();
			for(int i = 0;i< count;i++){
				String field = connection.getHeaderField(i);
				String value;
				if(field!=null){
					value = connection.getHeaderField(field);
					Log.i(LOGTAG,"header["+i+"]: "+ field + ": " + value);
				}
			}
		}
		
        private boolean isMediaContent(String mimeType){
        	if(mimeType==null){
        		return false;
        	}
        	
        	if(mimeType.contains("audio") || mimeType.contains("video")){
        		return true;
        	}
        	return false;
        }
	}
	
	public interface onReceivedTitle{
		
	}
	
	public interface OnLoadedURL{
		void loadedURL(WebView view, String url);
		void receivedTitle(WebView view, String title);
		
	}
}