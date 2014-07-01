package com.olyware.game3072.views;

public interface JoystickListener {
	public void onGameOver(int score);

	public void onUndoSelected(int undos);

	public void onRestartSelected();

	public void onAdSelected();

	public void onLeaderboardSelected();
}
