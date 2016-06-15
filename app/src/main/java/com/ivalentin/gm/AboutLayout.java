package com.ivalentin.gm;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Layout that show information about the app and the developer. 
 * It's pretty much static.
 * 
 * @see Fragment
 * 
 * @author Iñigo Valentin
 *
 */
public class AboutLayout extends Fragment{

	/**
	 * Run when the fragment is inflated.
	 * Assigns views, gets the date and does the first call to the {@link populate function}.
	 * 
	 * @param inflater A LayoutInflater to manage views
	 * @param container The container View
	 * @param savedInstanceState Bundle containing the state
	 * 
	 * @return the fragment view
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@SuppressLint("InflateParams") //Throws unknown error when done properly.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		//Load the layout.
		View view = inflater.inflate(R.layout.fragment_layout_about, null);
		
		//Set the title
		((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_about));
		
		//Set some tricky webView to show some text	.    
	    WebView wvCode = (WebView) view.findViewById(R.id.wv_about_code);
	    wvCode.loadData(view.getContext().getString(R.string.about_code), "text/html", "UTF-8");
	    
	    //Return the view itself.
		return view;
	}
}
