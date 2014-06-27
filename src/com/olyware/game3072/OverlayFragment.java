package com.olyware.game3072;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.olyware.game3072.views.ButtonListener;
import com.olyware.game3072.views.OverlayType;

public class OverlayFragment extends DialogFragment implements View.OnClickListener {

	final public static int alpha = 175;

	private ButtonListener listener;

	public void setButtonListener(ButtonListener listener) {
		this.listener = listener;
	}

	public static OverlayFragment newInstance(Context ctx, OverlayType o) {
		return newInstance(ctx, o, "");
	}

	public static OverlayFragment newInstance(Context ctx, OverlayType o, int subtitleHighlight) {
		return newInstance(ctx, o, String.valueOf(subtitleHighlight));
	}

	public static OverlayFragment newInstance(Context ctx, OverlayType o, String subtitleHighlight) {
		OverlayFragment f = new OverlayFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putString("title", OverlayType.getTitle(ctx, o));
		args.putString("subtitle", OverlayType.getSubtitle(ctx, o));
		if (subtitleHighlight.equals(""))
			args.putString("subtitle_highlight", OverlayType.getSubtitleHighlight(ctx, o));
		else
			args.putString("subtitle_highlight", subtitleHighlight);
		args.putString("text_button1", OverlayType.getButton1(ctx, o));
		args.putString("text_button2", OverlayType.getButton2(ctx, o));
		args.putString("text_button3", OverlayType.getButton3(ctx, o));
		args.putString("text_button_ad", OverlayType.getAd(ctx, o));

		f.setArguments(args);

		return f;
	}

	public void setSubtitleHighlight(String text) {

	}

	public void setBackground(Context ctx, Bitmap back) {
		new BlurBackground(ctx) {
			@Override
			protected void onPostExecute(Bitmap blurred) {
				Dialog dialog = getDialog();
				if (dialog != null) {
					Window win = dialog.getWindow();
					if (win != null) {
						// convert bitmap to BitmapDrawable so we can set it as the background of a view
						BitmapDrawable bDrawable = new BitmapDrawable(getResources(), blurred);

						Drawable wallpaper = new ColorDrawable(Color.argb(alpha, 0, 0, 0));
						TransitionDrawable background = new TransitionDrawable(new Drawable[] { wallpaper, bDrawable });
						win.setBackgroundDrawable(background);
						background.startTransition(1000);
					}
				}
			}
		}.execute(back);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int style = DialogFragment.STYLE_NO_FRAME;
		int theme = R.style.IntroTheme;
		setStyle(style, theme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_overlay, container, false);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(alpha, 0, 0, 0)));

		Bundle arg = getArguments();

		TextView title = (TextView) v.findViewById(R.id.fragment_overlay_title);
		title.setText(arg.getString("title"));

		TextView subtitle = (TextView) v.findViewById(R.id.fragment_overlay_subtitle);
		subtitle.setText(arg.getString("subtitle"));

		TextView subtitleHighlight = (TextView) v.findViewById(R.id.fragment_overlay_subtitle_highlight);
		subtitleHighlight.setText(arg.getString("subtitle_highlight"));

		TextView button1 = (TextView) v.findViewById(R.id.fragment_overlay_button1);
		button1.setOnClickListener(this);
		button1.setText(arg.getString("text_button1"));

		TextView button2 = (TextView) v.findViewById(R.id.fragment_overlay_button2);
		button2.setOnClickListener(this);
		button2.setText(arg.getString("text_button2"));

		TextView button3 = (TextView) v.findViewById(R.id.fragment_overlay_button3);
		button3.setOnClickListener(this);
		String bText = arg.getString("text_button3");
		if (bText.contains("\n")) {
			int start = bText.indexOf("\n");
			Spannable span = new SpannableString(bText);
			span.setSpan(new ForegroundColorSpan(getActivity().getResources().getColor(R.color.light_red)), start, bText.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			button3.setText(span);
		} else {
			button3.setText(arg.getString("text_button3"));
		}

		TextView buttonAd = (TextView) v.findViewById(R.id.fragment_overlay_ad);
		buttonAd.setOnClickListener(this);
		buttonAd.setText(arg.getString("text_button_ad"));

		return v;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.fragment_overlay_button1) {
			listener.onButton1Clicked();
		} else if (view.getId() == R.id.fragment_overlay_button2) {
			listener.onButton2Clicked();
		} else if (view.getId() == R.id.fragment_overlay_button3) {
			listener.onButton3Clicked();
		} else if (view.getId() == R.id.fragment_overlay_ad) {
			listener.onAdClicked();
		}
	}

	private class BlurBackground extends AsyncTask<Bitmap, Void, Bitmap> {
		private Context ctx;

		BlurBackground(Context ctx) {
			this.ctx = ctx;
		}

		@Override
		protected Bitmap doInBackground(Bitmap... bmps) {
			final RenderScript rs = RenderScript.create(ctx);
			try {
				// blur bitmap
				final Allocation input = Allocation.createFromBitmap(rs, bmps[0], Allocation.MipmapControl.MIPMAP_NONE,
						Allocation.USAGE_SCRIPT);
				final Allocation output = Allocation.createTyped(rs, input.getType());
				final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
				script.setRadius(25f);
				script.setInput(input);
				script.forEach(output);
				output.copyTo(bmps[0]);

				// dim the bitmap renderscript
				final Allocation alloc1 = Allocation.createFromBitmap(rs, bmps[0], Allocation.MipmapControl.MIPMAP_NONE,
						Allocation.USAGE_SCRIPT);
				final Allocation alloc2 = Allocation.createTyped(rs, alloc1.getType());
				final ScriptC_dim scriptDim = new ScriptC_dim(rs);
				scriptDim.set_dimmingValue((256f - OverlayFragment.alpha) / 256f);
				scriptDim.forEach_dim(alloc1, alloc2);
				alloc2.copyTo(bmps[0]);
			} catch (Exception e) {
				bmps[0] = null;
			} finally {
				rs.destroy();
			}
			if (bmps[0] == null)
				Log.d("test", "bitmap is null");
			return bmps[0];
		}
	}
}