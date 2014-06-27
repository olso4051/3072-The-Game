package com.olyware.game3072;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.olyware.game3072.utils.EZ;
import com.olyware.game3072.utils.PostHighScore;
import com.olyware.game3072.utils.ShareHelper;
import com.olyware.game3072.utils.Typefaces;
import com.olyware.game3072.views.ButtonListener;
import com.olyware.game3072.views.JoystickListener;
import com.olyware.game3072.views.JoystickView;
import com.olyware.game3072.views.OverlayType;

public class MainActivity extends FragmentActivity {
	final public static int defaultMaxUndos = 0, WorstScore = -15;
	private RelativeLayout layout;
	private JoystickView joystick;
	private UiLifecycleHelper uiHelper;
	private ProgressDialog progressDialog;
	private Typefaces typefaces;
	private Context ctx;
	private boolean dialogOn = false;
	private int scoreToBeat = WorstScore;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

		uiHelper = new UiLifecycleHelper(this, null);
		uiHelper.onCreate(savedInstanceState);
		ctx = this;

		if (savedInstanceState != null)
			scoreToBeat = savedInstanceState.getInt("score_to_beat");
		scoreToBeat = ShareHelper.getDeepLinkData(getIntent().getData());

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		int best = sharedPrefs.getInt("score_best", 0);
		layout = (RelativeLayout) findViewById(R.id.layout_game);
		joystick = (JoystickView) findViewById(R.id.joystick);
		joystick.setOnJostickListener(new JoystickListener() {
			@Override
			public void onGameOver(final int score) {
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				if (score > sharedPrefs.getInt("score_best", 0)) {
					joystick.setBest(score);
					SharedPreferences.Editor edit = sharedPrefs.edit();
					edit.putInt("score_best", score).putBoolean("score_uploaded", false);
					edit.commit();
					new PostHighScore(MainActivity.this) {
						@Override
						protected void onPostExecute(Integer result) {
							SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
							SharedPreferences.Editor editor = sharedPrefs.edit();
							if (result == 0) {
								editor.putString("global_score", getGlobalHighScore()).putString("global_name", getName())
										.putBoolean("score_uploaded", true);
								if (getPlace().equals("1st")) {
									editor.putString("global_score2", getSecondPlaceScore())
											.putString("global_name2", getSecondPlaceName()).putString("global_place2", "2nd")
											.putBoolean("first_place", true);
									joystick.setLeaderboard(getName(), getGlobalHighScore(), getSecondPlaceName(), getSecondPlaceScore(),
											"2nd");
								} else {
									editor.putString("global_score2", String.valueOf(sharedPrefs.getInt("score_best", 0)))
											.putString("global_name2",
													sharedPrefs.getString("user_name", MainActivity.this.getString(R.string.default_name)))
											.putString("global_place2", getPlace()).putBoolean("first_place", false);
									joystick.setLeaderboard(getName(), getGlobalHighScore(),
											sharedPrefs.getString("user_name", MainActivity.this.getString(R.string.default_name)),
											String.valueOf(sharedPrefs.getInt("score_best", 0)), getPlace());
								}
							}
							editor.commit();
						}
					}.execute(String.valueOf(score), "new",
							sharedPrefs.getString("user_name", MainActivity.this.getString(R.string.default_name)));
				}
				if (!dialogOn) {
					dialogOn = true;
					String subtitleHighlight = "";
					OverlayType type = OverlayType.GameOver;
					if (scoreToBeat <= WorstScore) {
						subtitleHighlight = String.valueOf(score);
						type = OverlayType.GameOver;
					} else if (scoreToBeat > score) {
						type = OverlayType.GameOverChallengeLost;
						subtitleHighlight = scoreToBeat + "\n" + score;
					} else if (score > scoreToBeat) {
						type = OverlayType.GameOverChallengeWon;
						subtitleHighlight = score + "\n" + scoreToBeat;
					} else {
						type = OverlayType.GameOverChallengeTie;
						subtitleHighlight = String.valueOf(score);
					}
					final Bitmap cropped = cropScreen(takeScreenShot());
					final OverlayFragment mOverlay = OverlayFragment.newInstance(ctx, type, subtitleHighlight);
					mOverlay.setCancelable(false);
					mOverlay.setBackground(ctx, takeScreenShot());
					mOverlay.show(getSupportFragmentManager(), "fragment_overlay");
					mOverlay.setButtonListener(new ButtonListener() {
						@Override
						public void onButton1Clicked() {
							// Reset game
							mOverlay.dismiss();
							joystick.reset();
							dialogOn = false;
							scoreToBeat = WorstScore;
						}

						@Override
						public void onButton2Clicked() {
							// Return to game
							mOverlay.dismiss();
							dialogOn = false;
						}

						@Override
						public void onButton3Clicked() {
							// Share screenshot
							mOverlay.dismiss();
							progressDialog = ProgressDialog.show(MainActivity.this, "", "Starting Facebook", true);
							ShareHelper.shareFacebook(MainActivity.this, uiHelper, progressDialog, cropped, score,
									ShareHelper.getDeepLinkToShare(score));
							dialogOn = false;
						}

						@Override
						public void onAdClicked() {
							// Start Hiq Lockscreen
							mOverlay.dismiss();
							startPlayStore(getString(R.string.play_store_link_hiq));
							dialogOn = false;
						}
					});
				}
			}

			@Override
			public void onUndoSelected(int undos) {
				if (!dialogOn) {
					dialogOn = true;
					final OverlayFragment mOverlay = OverlayFragment.newInstance(ctx, OverlayType.Undo);
					mOverlay.setCancelable(false);
					mOverlay.setBackground(ctx, takeScreenShot());
					mOverlay.show(getSupportFragmentManager(), "fragment_overlay");
					mOverlay.setButtonListener(new ButtonListener() {
						@Override
						public void onButton1Clicked() {
							// Share
							mOverlay.dismiss();
							progressDialog = ProgressDialog.show(MainActivity.this, "", "Starting Facebook", true);
							ShareHelper.shareFacebook(MainActivity.this, uiHelper, progressDialog);
							dialogOn = false;
						}

						@Override
						public void onButton2Clicked() {
							// Not Now
							mOverlay.dismiss();
							dialogOn = false;
						}

						@Override
						public void onButton3Clicked() {
							// Rate
							mOverlay.dismiss();
							startPlayStore(getString(R.string.play_store_link_3072));
							dialogOn = false;
						}

						@Override
						public void onAdClicked() {
							// Start Hiq Lockscreen
							mOverlay.dismiss();
							startPlayStore(getString(R.string.play_store_link_hiq));
							dialogOn = false;
						}
					});
				}
			}

			@Override
			public void onRestartSelected() {
				if (!dialogOn) {
					dialogOn = true;
					final OverlayFragment mOverlay = OverlayFragment.newInstance(ctx, OverlayType.Restart, joystick.getCurrentScore());
					mOverlay.setCancelable(false);
					mOverlay.setBackground(ctx, takeScreenShot());
					mOverlay.show(getSupportFragmentManager(), "fragment_overlay");
					mOverlay.setButtonListener(new ButtonListener() {
						@Override
						public void onButton1Clicked() {
							// Reset Game
							mOverlay.dismiss();
							joystick.reset();
							dialogOn = false;
							scoreToBeat = WorstScore;
						}

						@Override
						public void onButton2Clicked() {
							// Return to Game
							mOverlay.dismiss();
							dialogOn = false;
						}

						@Override
						public void onButton3Clicked() {
							// nothing
						}

						@Override
						public void onAdClicked() {
							// Start Hiq Lockscreen
							mOverlay.dismiss();
							startPlayStore(getString(R.string.play_store_link_hiq));
							dialogOn = false;
						}
					});
				}
			}

			@Override
			public void onAdSelected() {
				startPlayStore(getString(R.string.play_store_link_hiq));
			}

			@Override
			public void onLeaderboardSelected() {
				if (!dialogOn) {
					dialogOn = true;
					SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
					final EditTextFragment mOverlay = EditTextFragment.newInstance(ctx,
							sharedPrefs.getString("user_name", MainActivity.this.getString(R.string.default_name)));
					mOverlay.setCancelable(false);
					mOverlay.setBackground(ctx, takeScreenShot());
					mOverlay.show(getSupportFragmentManager(), "fragment_overlay");
					mOverlay.setButtonListener(new ButtonListener() {
						@Override
						public void onButton1Clicked() {
							// Change Name
							SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
							sharedPrefs.edit().putString("user_name", mOverlay.getUserName()).commit();
							joystick.setLeaderboardUserName(mOverlay.getUserName(), sharedPrefs.getBoolean("first_place", false));
							mOverlay.dismiss();
							dialogOn = false;
						}

						@Override
						public void onButton2Clicked() {
							// Return
							mOverlay.dismiss();
							dialogOn = false;
						}

						@Override
						public void onButton3Clicked() {
							// nothing
						}

						@Override
						public void onAdClicked() {
							// Start Hiq Lockscreen
							mOverlay.dismiss();
							startPlayStore(getString(R.string.play_store_link_hiq));
							dialogOn = false;
						}
					});
				}
			}
		});
		joystick.setBest(best);
		joystick.setMaxUndos(sharedPrefs.getInt("max_undos", defaultMaxUndos));
		joystick.setLeaderboard(sharedPrefs.getString("global_name", getString(R.string.leaderboard_default_name1)),
				sharedPrefs.getString("global_score", getString(R.string.leaderboard_default_score1)),
				sharedPrefs.getString("global_name2", getString(R.string.leaderboard_default_name2)),
				sharedPrefs.getString("global_score2", getString(R.string.leaderboard_default_score2)),
				sharedPrefs.getString("global_place2", getString(R.string.leaderboard_default_place2)));
		int[] values = joystick.getValues();
		int sum = 0;
		for (int i = 0; i < values.length; i++) {
			values[i] = sharedPrefs.getInt("value" + i, 0);
			sum += values[i];
		}
		if (sum > 0)
			joystick.setState(sharedPrefs.getInt("score_current", 0), sharedPrefs.getInt("undos", 0), values);

		typefaces = Typefaces.getInstance(this);
		EZ.setFont(joystick, typefaces.robotoCondensed);

		PostHighScore putHighScore = new PostHighScore(this) {
			@Override
			protected void onPostExecute(Integer result) {
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
				SharedPreferences.Editor editor = sharedPrefs.edit();
				if (result == 0) {
					editor.putString("global_score", getGlobalHighScore()).putString("global_name", getName())
							.putBoolean("score_uploaded", true);
					if (getPlace().equals("1st")) {
						editor.putString("global_score2", getSecondPlaceScore()).putString("global_name2", getSecondPlaceName())
								.putString("global_place2", "2nd").putBoolean("first_place", true);
						joystick.setLeaderboard(getName(), getGlobalHighScore(), getSecondPlaceName(), getSecondPlaceScore(), "2nd");
					} else {
						editor.putString("global_score2", String.valueOf(sharedPrefs.getInt("score_best", 0)))
								.putString("global_name2",
										sharedPrefs.getString("user_name", MainActivity.this.getString(R.string.default_name)))
								.putString("global_place2", getPlace()).putBoolean("first_place", false);
						joystick.setLeaderboard(getName(), getGlobalHighScore(),
								sharedPrefs.getString("user_name", MainActivity.this.getString(R.string.default_name)),
								String.valueOf(sharedPrefs.getInt("score_best", 0)), getPlace());
					}
				}
				editor.commit();
			}
		};
		if (!sharedPrefs.getBoolean("score_uploaded", false))
			putHighScore.execute(String.valueOf(best), "new",
					sharedPrefs.getString("user_name", MainActivity.this.getString(R.string.default_name)));
		else
			putHighScore.execute(String.valueOf(best), "existing");

		if (scoreToBeat > WorstScore) {
			if (!dialogOn) {
				dialogOn = true;
				final OverlayFragment mOverlay = OverlayFragment.newInstance(ctx, OverlayType.Challenge, scoreToBeat);
				mOverlay.setCancelable(false);
				mOverlay.show(getSupportFragmentManager(), "fragment_overlay");
				mOverlay.setButtonListener(new ButtonListener() {
					@Override
					public void onButton1Clicked() {
						// Reset Game
						mOverlay.dismiss();
						joystick.reset();
						dialogOn = false;
						scoreToBeat = WorstScore;
					}

					@Override
					public void onButton2Clicked() {
						// Return to Game
						mOverlay.dismiss();
						dialogOn = false;
					}

					@Override
					public void onButton3Clicked() {
						// nothing
					}

					@Override
					public void onAdClicked() {
						// Start Hiq Lockscreen
						mOverlay.dismiss();
						startPlayStore(getString(R.string.play_store_link_hiq));
						dialogOn = false;
					}
				});
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
			@Override
			public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
				Log.d("test", String.format("Error: %s", error.toString()));
			}

			@Override
			public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
				boolean didFinishNormal = FacebookDialog.getNativeDialogDidComplete(data);
				String completionGesture = FacebookDialog.getNativeDialogCompletionGesture(data);
				String postID = FacebookDialog.getNativeDialogPostId(data);
				if (didFinishNormal && completionGesture.equals("post")) {
					if (postID != null)
						Log.d("test", "postID = " + postID);
					SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
					sharedPrefs.edit().putInt("max_undos", sharedPrefs.getInt("max_undos", defaultMaxUndos) + 1).commit();
				}
			}
		});
	}

	@Override
	protected void onPause() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor edit = sharedPrefs.edit();
		int[] values = joystick.getValues();
		edit.putInt("score_current", joystick.getCurrentScore());
		edit.putInt("undos", joystick.getUndos());
		for (int i = 0; i < values.length; i++) {
			edit.putInt("value" + i, values[i]);
		}
		edit.commit();
		uiHelper.onPause();
		super.onPause();
	}

	@Override
	protected void onStop() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		uiHelper.onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("score_to_beat", scoreToBeat);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		if (!dialogOn) {
			dialogOn = true;
			final OverlayFragment mOverlay = OverlayFragment.newInstance(ctx, OverlayType.ExitGame);
			mOverlay.setBackground(this, takeScreenShot());
			mOverlay.setCancelable(false);
			mOverlay.show(getSupportFragmentManager(), "fragment_overlay");
			mOverlay.setButtonListener(new ButtonListener() {
				@Override
				public void onButton1Clicked() {
					// Exit
					finish();
					dialogOn = false;
				}

				@Override
				public void onButton2Clicked() {
					// Return
					mOverlay.dismiss();
					dialogOn = false;
				}

				@Override
				public void onButton3Clicked() {
					// nothing
				}

				@Override
				public void onAdClicked() {
					// Start Hiq Lockscreen
					mOverlay.dismiss();
					startPlayStore(getString(R.string.play_store_link_hiq));
					dialogOn = false;
				}
			});
		}
	}

	@SuppressLint("NewApi")
	private Bitmap takeScreenShot() {
		View view = this.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();
		Bitmap b = Bitmap.createBitmap(b1, 0, 0, b1.getWidth(), b1.getHeight());
		view.destroyDrawingCache();
		return b;
	}

	private void startPlayStore(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	private Bitmap cropScreen(Bitmap b1) {
		int statusBarHeight = 0;
		int height = b1.getHeight();
		if (layout != null && b1 != null) {
			if (layout.getHeight() > 0 && layout.getHeight() < b1.getHeight()) {
				statusBarHeight += b1.getHeight() - layout.getHeight();
			}
			height = joystick.getGameBottom() + layout.getPaddingTop() * 2;
			int minHeight = (int) (b1.getWidth() / ShareHelper.FACEBOOK_LINK_RATIO);
			int maxHeight = b1.getHeight() - statusBarHeight;
			height = (height < minHeight) ? minHeight : height;
			height = (height > maxHeight) ? maxHeight : height;
		}

		Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, b1.getWidth(), height);
		return b;
	}
}
