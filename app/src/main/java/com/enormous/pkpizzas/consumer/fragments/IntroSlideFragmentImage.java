package com.enormous.pkpizzas.consumer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.common.ImageLoader;

public class IntroSlideFragmentImage extends Fragment {

    private ImageView imageView;
    private TextView textView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_intro_slide_image, container, false);
		findViews(view);

		//set image and text according to position
		int position = getArguments().getInt("position");
//        imageView.setImageBitmap(Utils.decodeImageResource(getResources(),
//                getResources().getIdentifier("intro"+position, "drawable", getActivity().getPackageName()),
//                500,
//                500));
        textView.setText(getResources().getText(getResources().getIdentifier("introText" + position, "string", getActivity().getPackageName())));
        ImageLoader.getInstance().displayImage(getActivity(), getResources().getIdentifier("intro"+position, "drawable", getActivity().getPackageName())+"", imageView, true, 500, 500, R.drawable.placeholder_image);

		return view;
	}

	public static Fragment newInstance(int position) {
		Fragment introSlideFragment = new IntroSlideFragmentImage();
		Bundle bundle = new Bundle();
		bundle.putInt("position", position);
		introSlideFragment.setArguments(bundle);
		return introSlideFragment;
	}

    private void findViews(View view) {
		imageView = (ImageView) view.findViewById(R.id.introImageView);
        textView = (TextView) view.findViewById(R.id.introTextView);
	}

}
