package com.olyware.game3072.views;

import android.content.Context;

import com.olyware.game3072.R;

public enum OverlayType {
	GameOver, GameOverChallengeWon, GameOverChallengeLost, GameOverChallengeTie, ExitGame, Restart, Undo, Challenge;
	public static String getTitle(Context ctx, OverlayType o) {
		switch (o) {
		case GameOver:
			return ctx.getString(R.string.fragment_overlay_text_title_gameover);
		case GameOverChallengeWon:
			return ctx.getString(R.string.fragment_overlay_text_title_gameover_challenge_won);
		case GameOverChallengeLost:
			return ctx.getString(R.string.fragment_overlay_text_title_gameover_challenge_lost);
		case GameOverChallengeTie:
			return ctx.getString(R.string.fragment_overlay_text_title_gameover_challenge_tie);
		case ExitGame:
			return ctx.getString(R.string.fragment_overlay_text_title_exit);
		case Restart:
			return ctx.getString(R.string.fragment_overlay_text_title_restart);
		case Undo:
			return ctx.getString(R.string.fragment_overlay_text_title_undo);
		case Challenge:
			return ctx.getString(R.string.fragment_overlay_text_title_challenge);
		default:
			return "";
		}
	}

	public static String getSubtitle(Context ctx, OverlayType o) {
		switch (o) {
		case GameOver:
		case GameOverChallengeTie:
			return ctx.getString(R.string.fragment_overlay_text_subtitle_gameover);
		case GameOverChallengeWon:
			return ctx.getString(R.string.fragment_overlay_text_subtitle_gameover_challenge_won);
		case GameOverChallengeLost:
			return ctx.getString(R.string.fragment_overlay_text_subtitle_gameover_challenge_lost);
		case ExitGame:
			return ctx.getString(R.string.fragment_overlay_text_subtitle_exit);
		case Restart:
			return ctx.getString(R.string.fragment_overlay_text_subtitle_restart);
		case Undo:
			return ctx.getString(R.string.fragment_overlay_text_subtitle_undo);
		case Challenge:
			return ctx.getString(R.string.fragment_overlay_text_subtitle_challenge);
		default:
			return "";
		}
	}

	public static String getSubtitleHighlight(Context ctx, OverlayType o) {
		switch (o) {
		case GameOver:
		case GameOverChallengeWon:
		case GameOverChallengeLost:
		case GameOverChallengeTie:
		case Challenge:
			return ctx.getString(R.string.fragment_overlay_text_subtitle_highlight_gameover);
		case ExitGame:
			return ctx.getString(R.string.fragment_overlay_text_subtitle_highlight_exit);
		case Restart:
			return ctx.getString(R.string.fragment_overlay_text_subtitle_highlight_restart);
		case Undo:
			return ctx.getString(R.string.fragment_overlay_text_subtitle_highlight_undo);
		default:
			return "";
		}
	}

	public static String getButton1(Context ctx, OverlayType o) {
		switch (o) {
		case GameOver:
		case GameOverChallengeWon:
		case GameOverChallengeLost:
		case GameOverChallengeTie:
			return ctx.getString(R.string.fragment_overlay_button1_gameover);
		case ExitGame:
			return ctx.getString(R.string.fragment_overlay_button1_exit);
		case Restart:
			return ctx.getString(R.string.fragment_overlay_button1_restart);
		case Undo:
			return ctx.getString(R.string.fragment_overlay_button1_undo);
		case Challenge:
			return "";
		default:
			return "";
		}
	}

	public static String getButton2(Context ctx, OverlayType o) {
		switch (o) {
		case GameOver:
		case GameOverChallengeWon:
		case GameOverChallengeLost:
		case GameOverChallengeTie:
			return ctx.getString(R.string.fragment_overlay_button2_gameover);
		case ExitGame:
			return ctx.getString(R.string.fragment_overlay_button2_exit);
		case Restart:
			return ctx.getString(R.string.fragment_overlay_button2_restart);
		case Undo:
			return ctx.getString(R.string.fragment_overlay_button2_undo);
		case Challenge:
			return "";
		default:
			return "";
		}
	}

	public static String getButton3(Context ctx, OverlayType o) {
		switch (o) {
		case GameOver:
			return ctx.getString(R.string.fragment_overlay_button3_gameover);
		case GameOverChallengeWon:
			return ctx.getString(R.string.fragment_overlay_button3_gameover_challenge_won);
		case GameOverChallengeLost:
			return ctx.getString(R.string.fragment_overlay_button3_gameover_challenge_lost);
		case GameOverChallengeTie:
			return ctx.getString(R.string.fragment_overlay_button3_gameover_challenge_won);
		case ExitGame:
			return ctx.getString(R.string.fragment_overlay_button3_exit);
		case Restart:
			return ctx.getString(R.string.fragment_overlay_button3_restart);
		case Undo:
			return ctx.getString(R.string.fragment_overlay_button3_undo);
		case Challenge:
			return ctx.getString(R.string.fragment_overlay_button3_challenge);
		default:
			return "";
		}
	}

	public static String getAd(Context ctx, OverlayType o) {
		return ctx.getString(R.string.fragment_overlay_text_ad);
	}
}
