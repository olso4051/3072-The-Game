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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.olyware.game3072.views.ButtonListener;

public class EditTextFragment extends DialogFragment implements View.OnClickListener {

	private ButtonListener listener;
	private EditText username;

	public void setButtonListener(ButtonListener listener) {
		this.listener = listener;
	}

	public static EditTextFragment newInstance(Context ctx, String userName) {
		EditTextFragment f = new EditTextFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putString("username", userName);

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
					if (win != null && blurred != null) {
						// convert bitmap to BitmapDrawable so we can set it as the background of a view
						BitmapDrawable bDrawable = new BitmapDrawable(getResources(), blurred);

						Drawable wallpaper = new ColorDrawable(Color.argb(OverlayFragment.alpha, 0, 0, 0));
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
		View v = inflater.inflate(R.layout.fragment_edittext, container, false);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(OverlayFragment.alpha, 0, 0, 0)));

		Bundle arg = getArguments();

		username = (EditText) v.findViewById(R.id.fragment_edittext_edittext);
		username.setText(arg.getString("username"));

		TextView button1 = (TextView) v.findViewById(R.id.fragment_edittext_button1);
		button1.setOnClickListener(this);

		TextView button2 = (TextView) v.findViewById(R.id.fragment_edittext_button2);
		button2.setOnClickListener(this);

		TextView buttonAd = (TextView) v.findViewById(R.id.fragment_edittext_ad);
		buttonAd.setOnClickListener(this);

		return v;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.fragment_edittext_button1) {
			listener.onButton1Clicked();
		} else if (view.getId() == R.id.fragment_edittext_button2) {
			listener.onButton2Clicked();
		} else if (view.getId() == R.id.fragment_edittext_ad) {
			listener.onAdClicked();
		}
	}

	public String getUserName() {
		String usernameString;
		if (username != null)
			usernameString = username.getText().toString();
		else
			usernameString = getString(R.string.default_name);
		if (usernameString.equals(""))
			usernameString = getString(R.string.default_name);
		return usernameString;
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