package com.enormous.discover.consumer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.enormous.discover.consumer.R;


public class InviteFriendsFragmentByText extends Fragment {

    private ListView listView;
    private ProgressBar progressBar;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_invite_friends_listivew, container, false);
		findViews(view);
		return view;
	}

    private void findViews(View view) {
		listView = (ListView) view.findViewById(R.id.listView1);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
	}
	
	public void setAdapter(BaseAdapter adapter) {
		listView.setAdapter(adapter);
		progressBar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
	}
}
