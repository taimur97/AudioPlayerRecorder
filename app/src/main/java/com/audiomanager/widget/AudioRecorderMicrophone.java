package com.audiomanager.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import com.audiomanager.app.R;

public class AudioRecorderMicrophone extends ViewGroup {
	private static final int ANIMATION_INTERVAL = 100;

	private static final float MAX_RELATIVE_SCALE = 0.8f;
	private static final int MAX_AMPLITUDE = 20000; // Real maximum value for MediaRecorder.getMaxAmplitude() is Integer.MAX_VALUE.

	private ImageView mMicrophoneView;
	private View mBackgroundView;

	private MediaRecorder mMediaRecorder;

	private AnimationRunnable mAnimationRunnable = new AnimationRunnable();
	private float mAnimationScale = 1.0f; // In the beginning, the background view has the same size as the microphone view.

	public AudioRecorderMicrophone(Context context) {
		super(context);
		init();
	}

	public AudioRecorderMicrophone(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AudioRecorderMicrophone(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mMicrophoneView = new ImageView(getContext());
		mMicrophoneView.setImageDrawable(getResources().getDrawable(R.drawable.vs_micbtn_on_selector));
		// Center microphone, since the drawable is closer to the top.
		mMicrophoneView.setPadding(
				0,
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
				0,
				0
		);

		mBackgroundView = new View(getContext());
		final Drawable circleDrawable = new ShapeDrawable(new OvalShape());
		circleDrawable.setAlpha(85);
		circleDrawable.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
			mBackgroundView.setBackgroundDrawable(circleDrawable);
		else
			mBackgroundView.setBackground(circleDrawable);

		// Background below microphone.
		addView(mBackgroundView);
		addView(mMicrophoneView);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Measure microphone.
		measureChild(mMicrophoneView, widthMeasureSpec, heightMeasureSpec);
		final int microphoneMeasuredWidth = mMicrophoneView.getMeasuredWidth();
		final int microphoneMeasuredHeight = mMicrophoneView.getMeasuredHeight();

		// Measure background to be the same size as the microphone.
		measureChild(
				mBackgroundView,
				MeasureSpec.makeMeasureSpec(microphoneMeasuredWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(microphoneMeasuredHeight, MeasureSpec.EXACTLY));

		final int width, height;
		final LayoutParams lp = getLayoutParams();
		// Calculate width.
		if(lp != null && lp.width == LayoutParams.MATCH_PARENT)
			width = MeasureSpec.getSize(widthMeasureSpec);
		else
			width = (int)(microphoneMeasuredWidth * (1.0f + MAX_RELATIVE_SCALE) + .5f);

		// Calculate height.
		if(lp != null && lp.height == LayoutParams.MATCH_PARENT)
			height = MeasureSpec.getSize(heightMeasureSpec);
		else
			height = (int)(microphoneMeasuredHeight * (1.0f + MAX_RELATIVE_SCALE) + .5f);

		// Set dimensions; respect the contract.
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		layoutChildViewAtCenter(mMicrophoneView, l, t, r, b);
		layoutChildViewAtCenter(mBackgroundView, l, t, r, b);
	}

	private void layoutChildViewAtCenter(View view, int l, int t, int r, int b) {
		final int measuredWidth = view.getMeasuredWidth();
		final int measuredHeight = view.getMeasuredHeight();
		final int left = (int)(((r - l) - measuredWidth + .5f) / 2);
		final int top = (int)(((b - t) - measuredHeight + .5f) / 2);
		view.layout(left, top, left + measuredWidth, top + measuredHeight);
	}

	public void setMediaRecorder(MediaRecorder recorder) {
		mMediaRecorder = recorder;

		if(mMediaRecorder != null) {
			final Handler handler = getHandler();
			if(handler != null)
				handler.removeCallbacks(mAnimationRunnable);

			postDelayed(mAnimationRunnable, ANIMATION_INTERVAL);
		}
	}

	private class AnimationRunnable implements Runnable {
		@Override
		public void run() {
			final MediaRecorder recorder = mMediaRecorder;
			if(recorder != null) {
				final float oldScale = mAnimationScale;

				// Calculate new scale.
				final float relativeScale = Math.min(recorder.getMaxAmplitude(), MAX_AMPLITUDE) / (float)MAX_AMPLITUDE;
				mAnimationScale = 1.0f + (relativeScale * MAX_RELATIVE_SCALE);

				// Transition from old scale to new scale during the update interval.
				mBackgroundView.clearAnimation();
				final Animation animation = new ScaleAnimation(
						oldScale,
						mAnimationScale,
						oldScale,
						mAnimationScale,
						Animation.RELATIVE_TO_SELF,
						0.5f,
						Animation.RELATIVE_TO_SELF,
						0.5f);
				animation.setFillAfter(true);
				animation.setDuration(ANIMATION_INTERVAL);
				mBackgroundView.startAnimation(animation);

				// Post animation runnable to update the animation.
				postDelayed(mAnimationRunnable, ANIMATION_INTERVAL);
			}
		}
	}
}