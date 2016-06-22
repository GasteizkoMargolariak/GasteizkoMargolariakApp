package com.ivalentin.gm;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment openen for La Blanca sections while the festivals are not close.
 *
 * @see Fragment
 *
 * @author Iñigo Valentin
 *
 */
public class LablancaNoFestivalsLayout extends Fragment {

	/**
	 * Run when the fragment is inflated.
	 *
	 * @param inflater           A LayoutInflater to manage views
	 * @param container          The container View
	 * @param savedInstanceState Bundle containing the state
	 * @return The fragment view
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	@SuppressLint("InflateParams")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//Load the layout
		View view = inflater.inflate(R.layout.fragment_layout_lablanca_nofestivals, null);

		//Set the title
		((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_lablanca));

		return view;
	}
}