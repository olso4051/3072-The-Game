package com.olyware.game3072.utils;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.olyware.game3072.MainActivity;
import com.olyware.game3072.R;

public class ShareHelper {
	final public static float FACEBOOK_LINK_RATIO = 1.9178082191780821917808219178082f;
	final public static List<String> PERMISSIONS = Arrays.asList("public_profile");
	private static ProgressDialog staticProgressDialog;
	private static String staticLink;
	private static Context staticContext;
	private static Activity staticActivity;
	private static UiLifecycleHelper staticUiHelper;

	public static void shareFacebook(final Context context, final UiLifecycleHelper uiHelper, final ProgressDialog pDialog, Bitmap image,
			int score, String deepLink) {
		final String link = context.getString(R.string.play_store_link_3072_web);
		final String DeelDatApiKey = context.getString(R.string.deeldat_api_key);
		final String title = "Can you beat my score of " + score + " in 3072?";
		final String description = context.getString(R.string.share_description);
		String siteName = context.getString(R.string.app_name);
		String appName = context.getString(R.string.app_name);
		String appPackage = context.getApplicationContext().getPackageName();
		String appClass = "MainActivity";
		new UploadImage(context, image) {
			@Override
			protected void onPostExecute(Integer result) {
				Log.d("test", "success = " + getSuccess());
				Log.d("test", "error = " + getError());
				Log.d("test", "url = " + getURL());
				Log.d("test", "hash = " + getHash());
				if (result == 0 || getSuccess().equals("true")) {
					shareFacebook(context, uiHelper, pDialog, getURL());
				} else {
					shareFacebook(context, uiHelper, pDialog, link);
				}
			}
		}.execute(DeelDatApiKey, title, description, siteName, link, appName, appPackage, appClass, deepLink);

	}

	private static Session.StatusCallback loginCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private static void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state.isOpened()) {
			shareFacebook();
			// showFeedDialog(staticContext, staticLink);
		}
	}

	public static void shareFacebook(final Context context, final UiLifecycleHelper uiHelper, ProgressDialog pDialog) {
		shareFacebook(context, uiHelper, pDialog, context.getString(R.string.play_store_link_3072_web));
	}

	public static void shareFacebook(final Context context, final UiLifecycleHelper uiHelper, ProgressDialog pDialog, String link) {
		/*Activity act = (Activity) context;
		if (FacebookDialog.canPresentShareDialog(context.getApplicationContext(), FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
			FacebookDialog.ShareDialogBuilder shareDialogBuilder1 = new FacebookDialog.ShareDialogBuilder(act).setLink(link);
			FacebookDialog shareDialog1 = shareDialogBuilder1.build();
			uiHelper.trackPendingDialogCall(shareDialog1.present());
		} else {*/
		staticLink = link;
		staticContext = context;
		staticProgressDialog = pDialog;
		staticActivity = (Activity) context;
		staticUiHelper = uiHelper;
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(staticActivity).setPermissions(PERMISSIONS).setCallback(loginCallback));
		} else if (session.isOpened()) {
			shareFacebook();
			// showFeedDialog(context, link);
		} else {
			Session.openActiveSession(staticActivity, true, PERMISSIONS, loginCallback);
		}
		// }
	}

	private static void shareFacebook() {
		if (FacebookDialog.canPresentShareDialog(staticContext.getApplicationContext(), FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
			FacebookDialog.ShareDialogBuilder shareDialogBuilder1 = new FacebookDialog.ShareDialogBuilder(staticActivity)
					.setLink(staticLink);
			FacebookDialog shareDialog1 = shareDialogBuilder1.build();
			staticUiHelper.trackPendingDialogCall(shareDialog1.present());
		} else {
			showFeedDialog(staticContext, staticLink);
		}
	}

	private static void showFeedDialog(final Context context, String link) {
		Bundle params = new Bundle();
		params.putString("link", link);
		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(context, Session.getActiveSession(), params)).setOnCompleteListener(
				new OnCompleteListener() {
					@Override
					public void onComplete(Bundle values, FacebookException error) {
						if (error == null) {
							// When the story is posted, echo the success
							// and the post Id.
							final String postId = values.getString("post_id");
							if (postId != null) {
								Log.d("test", "Posted story, id: " + postId);
								SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
								sharedPrefs.edit().putInt("max_undos", sharedPrefs.getInt("max_undos", MainActivity.defaultMaxUndos) + 1)
										.commit();
							} else {
								// User clicked the Cancel button
								Log.d("test", "Publish cancelled");
							}
						} else if (error instanceof FacebookOperationCanceledException) {
							// User clicked the "x" button
							Log.d("test", "Publish cancelled");
						} else {
							// Generic, ex: network error
							Log.d("test", "Error posting story");
						}
						if (staticProgressDialog != null) {
							staticProgressDialog.dismiss();
							staticProgressDialog = null;
						}
					}
				}).build();
		feedDialog.show();
	}

	public static String getDeepLinkToShare(int score) {
		return String.valueOf(score);
	}

	public static int getDeepLinkData(Uri data) {
		if (data != null) {
			String target = "http://deeldat.com/f/";
			String url = data.toString();
			int start = url.indexOf(target);
			if (start >= 0) {
				start = url.indexOf('/', target.length()) + 1;
				if (start > target.length() && start < url.length()) {
					try {
						int score = Integer.parseInt(url.substring(start));
						return score;
					} catch (NumberFormatException e) {
						return MainActivity.WorstScore;
					}
				} else {
					return MainActivity.WorstScore;
				}
			} else {
				return MainActivity.WorstScore;
			}
		}
		return MainActivity.WorstScore;
	}
}
