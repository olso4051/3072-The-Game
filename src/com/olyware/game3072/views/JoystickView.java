package com.olyware.game3072.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.olyware.game3072.R;

public class JoystickView extends View {

	final private int paddingDP = 10, textPaddingDP = 4, NumTextSizes = 4, NumTriangles = 16, NumBoxes = 3, minSlideDistanceDP = 50,
			scoreMinus = -1;
	final private int frameTimeSlide = 10, totalTimeSlide = 100, frameTimeNewNumbers = 10, totalTimeNewNumbers = 100;
	final private int[] textSizeSP = { 23, 18, 13, 10 };
	final private String[] textToSize = { "96", "768", "3072", "196608" };
	final private double angleDifferenceMax = Math.PI / 6;
	final private float sqrt3 = (float) Math.sqrt(3), percent = 0.3f, fontToPixSize = 0.75f, pixToFontSize = 1 / fontToPixSize,
			trianglePaddingPercent = 1 / 12f;

	private int[] textSizePix = new int[NumTextSizes];
	private TextPaint[] textPaint = new TextPaint[NumTextSizes];
	private Path[] labelBackground = new Path[NumBoxes], textBackground = new Path[NumBoxes];
	private float[] labelCenterX = new float[NumBoxes], labelCenterY = new float[NumBoxes];
	private float[] textCenterX = new float[NumBoxes], textCenterY = new float[NumBoxes];
	private String[] labels = new String[NumBoxes];
	private List<Triangle> triangles;
	private List<Bundle> states;

	private int Width, Height, PaddingLeft, PaddingTop, PaddingRight, PaddingBottom, minDistance, previousScore, currentScore, scorePlus,
			bestScore, undos, maxUndos;
	private long lastTimeSlide = 0, startTimeSlide = 0, lastTimeNewNumbers = 0, startTimeNewNumbers = 0;
	private float w, h, scoreboardHeight, leaderboardTop, adHeight, gameHeight, padding, textPadding, newNumberScale, labelTextSize,
			textTextSize, textTextSizeBig;
	private double startX, startY;
	private boolean slid, leaderboard = true;
	private String Restart, Undo, name1, score1, place1 = "1st", name2, score2, place2;
	private Drawable ad;
	private Path triangleBigPath, adPathOutside, adPathInside;
	private TextPaint labelTextPaint, textTextPaint, leftTextPaint, rightTextPaint, leftTextPaintBig, rightTextPaintBig;
	private Paint scoreboardPaintRounded, labelBackgroundPaint, triangleBigPaintRounded, triangleSmallPaintRounded, adPaintRounded;

	private Random rand = new Random();

	private JoystickListener listener;

	private Handler animateHandler;
	private Runnable slide, newNumbers;

	private Resources res;
	private Context ctx;

	private class Triangle {
		private int right, upRight, upLeft, left, downLeft, downRight;
		private int value, previousValue, slideTo, slideFrom, row, positionInMatrix;
		private boolean upOrDown, newNumber;
		private float X, Y, textY, xSlide, ySlide, yTextSlide, angle, textHeight, textWidth;
		private Path path, pathSlide;
		private Paint backgroundPaint, backgroundPaintPrevious;
		private TextPaint textPaint, textPaintPrevious;

		private Triangle(int position) {
			X = 0;
			Y = 0;
			textY = 0;
			xSlide = 0;
			ySlide = 0;
			yTextSlide = 0;
			angle = 0;
			newNumber = false;
			value = 0;
			previousValue = 0;
			slideTo = -1;
			slideFrom = -1;
			path = new Path();
			pathSlide = new Path();
			textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			textPaint.setTextAlign(Paint.Align.CENTER);
			textPaint.setColor(res.getColor(R.color.background196608));
			textPaint.setTextSize(textSizePix[0]);
			String v = value + "";
			Rect b = new Rect();
			textPaint.getTextBounds(v, 0, v.length(), b);
			textHeight = b.height();
			textWidth = b.width();

			textPaintPrevious = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			textPaintPrevious.setTextAlign(Paint.Align.CENTER);
			textPaintPrevious.setColor(res.getColor(R.color.background196608));
			textPaintPrevious.setTextSize(textSizePix[0]);
			backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
			backgroundPaint.setColor(res.getColor(R.color.background0));
			backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			backgroundPaint.setStrokeJoin(Paint.Join.ROUND);
			backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
			backgroundPaint.setPathEffect(new CornerPathEffect(padding));
			backgroundPaintPrevious = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
			backgroundPaintPrevious.setColor(res.getColor(R.color.background0));
			backgroundPaintPrevious.setStyle(Paint.Style.FILL_AND_STROKE);
			backgroundPaintPrevious.setStrokeJoin(Paint.Join.ROUND);
			backgroundPaintPrevious.setStrokeCap(Paint.Cap.ROUND);
			backgroundPaintPrevious.setPathEffect(new CornerPathEffect(padding));

			row = (int) Math.ceil(Math.sqrt(position + 1));
			int positionsBefore = (int) Math.pow(row - 1, 2);
			int positionsBefore2 = (int) Math.pow(row - 2, 2);
			if (row - 2 < 0)
				positionsBefore2 = 0;
			int positionsInRowBefore = 2 * row - 3;
			int positionsInRow = 2 * row - 1;
			int positionsInRowAfter = 2 * row + 1;
			int positionAtRowEnd = (int) Math.pow(row, 2) - 1;
			int positionAtRowStart = positionsBefore;
			int positionAtRowStartInRowBefore = positionsBefore2;
			int positionAtRowStartInRowAfter = positionAtRowEnd + 1;
			int positionInRow = position - positionsBefore;
			positionInMatrix = positionInRow + 8 - (7 + positionsInRow) / 2;
			if (isOdd(positionInRow))
				upOrDown = false;
			else
				upOrDown = true;
			right = position + 1;
			if (right > positionAtRowEnd) {
				right = -1;
			}
			left = position - 1;
			if (left < positionAtRowStart) {
				left = -1;
			}
			upRight = upOrDown ? position + 1 : position - positionsInRowBefore - 1;
			if (upRight >= (upOrDown ? positionAtRowStartInRowAfter : positionAtRowStart))
				upRight = -1;
			upLeft = upOrDown ? position - 1 : position - positionsInRowBefore - 1;
			if (upLeft < (upOrDown ? positionAtRowStart : positionAtRowStartInRowBefore))
				upLeft = -1;
			downRight = upOrDown ? position + positionsInRowAfter - 1 : position + 1;
			if (downRight > 16)
				downRight = -1;
			downLeft = upOrDown ? position + positionsInRow + 1 : position - 1;
			if (downLeft > 16)
				downLeft = -1;
		}

		public int getLeft() {
			return left;
		}

		public int getRight() {
			return right;
		}

		public int getUpRight() {
			return upRight;
		}

		public int getUpLeft() {
			return upLeft;
		}

		public int getDownLeft() {
			return downLeft;
		}

		public int getDownRight() {
			return downRight;
		}

		public int getDirection(Direction dir) {
			switch (dir) {
			case DOWN_LEFT:
				return getDownLeft();
			case DOWN_RIGHT:
				return getDownRight();
			case LEFT:
				return getLeft();
			case RIGHT:
				return getRight();
			case UP_LEFT:
				return getUpLeft();
			case UP_RIGHT:
				return getUpRight();
			default:
				return getRight();

			}
		}

		public int getValue() {
			return value;
		}

		public int getPreviousValue() {
			return previousValue;
		}

		public int getSlideTo() {
			return slideTo;
		}

		public int getSlideFrom() {
			return slideFrom;
		}

		public int getRow() {
			return row;
		}

		public int getPositionInMatrix() {
			return positionInMatrix;
		}

		public float getX() {
			return X;
		}

		public float getY() {
			return Y;
		}

		public float getTextY() {
			return textY;
		}

		public float getXSlide() {
			return xSlide;
		}

		public float getYSlide() {
			return ySlide;
		}

		public float getYTextSlide() {
			return yTextSlide;
		}

		public Path getPath() {
			return path;
		}

		public Path getPathSlide() {
			return pathSlide;
		}

		public float getAngle() {
			return angle;
		}

		public Paint getBackgroundPaint() {
			return backgroundPaint;
		}

		public Paint getBackgroundPaintPrevious() {
			return backgroundPaintPrevious;
		}

		public TextPaint getTextPaint() {
			return textPaint;
		}

		public TextPaint getTextPaintPrevious() {
			return textPaintPrevious;
		}

		public void resetTextPaint(TextPaint tp) {
			textPaint.set(tp);
			if (value < 100) {
				textHeight = textSizePix[0] * fontToPixSize;
				textPaint.setTextSize(textSizePix[0]);
				textPaint.setColor(res.getColor(R.color.background196608));
			} else if (value < 1000) {
				textHeight = textSizePix[1] * fontToPixSize;
				textPaint.setTextSize(textSizePix[1]);
				textPaint.setColor(res.getColor(R.color.background3));
			} else if (value < 10000) {
				textHeight = textSizePix[2] * fontToPixSize;
				textPaint.setTextSize(textSizePix[2]);
				textPaint.setColor(res.getColor(R.color.background3));
			} else {
				textHeight = textSizePix[3] * fontToPixSize;
				textPaint.setTextSize(textSizePix[3]);
				textPaint.setColor(res.getColor(R.color.background3));
			}
			textY = Y + (upOrDown ? textHeight * percent : textHeight * (1 - percent));
			textPaintPrevious.setTypeface(tp.getTypeface());
			textPaintPrevious.setTextSize(textPaint.getTextSize());
		}

		public void setValue(int value, boolean newNumber, SetPrevious setPrevious) {
			float previousTextSize = textPaint.getTextSize();
			int previousTextColor = textPaint.getColor();
			if (value < 100) {
				textHeight = textSizePix[0] * fontToPixSize;
				textPaint.setTextSize(textSizePix[0]);
				textPaint.setColor(res.getColor(R.color.background196608));
			} else if (value < 1000) {
				textHeight = textSizePix[1] * fontToPixSize;
				textPaint.setTextSize(textSizePix[1]);
				textPaint.setColor(res.getColor(R.color.background3));
			} else if (value < 10000) {
				textHeight = textSizePix[2] * fontToPixSize;
				textPaint.setTextSize(textSizePix[2]);
				textPaint.setColor(res.getColor(R.color.background3));
			} else {
				textHeight = textSizePix[3] * fontToPixSize;
				textPaint.setTextSize(textSizePix[3]);
				textPaint.setColor(res.getColor(R.color.background3));
			}
			textY = Y + (upOrDown ? textHeight * percent : textHeight * (1 - percent));
			this.newNumber = newNumber;

			switch (setPrevious) {
			case SET_TO_VALUE:
				previousValue = value;
				textPaintPrevious.setTextSize(textPaint.getTextSize());
				textPaintPrevious.setColor(textPaint.getColor());
				break;
			case SET_TO_PREVIOUS_VALUE:
				previousValue = this.value;
				textPaintPrevious.setTextSize(previousTextSize);
				textPaintPrevious.setColor(previousTextColor);
				break;
			case DONT_SET:
				break;
			}

			this.value = value;

			if (isInEditMode()) {
				backgroundPaintPrevious.setColor(res.getColor(R.color.background0));
				backgroundPaint.setColor(res.getColor(R.color.background3));
			} else {
				int ID = res.getIdentifier("background" + previousValue, "color", ctx.getPackageName());
				backgroundPaintPrevious.setColor(res.getColor(ID));
				ID = res.getIdentifier("background" + value, "color", ctx.getPackageName());
				backgroundPaint.setColor(res.getColor(ID));
			}
		}

		public void setSlideTo(int pos) {
			slideTo = pos;
		}

		public void setSlideFrom(int pos) {
			slideFrom = pos;
		}

		public void setXY(float x, float y) {
			X = x;
			Y = y + (upOrDown ? 0 : -padding / 4 - h / 3);
			int mult = upOrDown ? 1 : -1;
			path.reset();
			path.moveTo(X, Y + h / 3 * mult);
			path.lineTo(X + w / 2, Y + h / 3 * mult);
			path.lineTo(X, Y - h * 2 / 3 * mult);
			path.lineTo(X - w / 2, Y + h / 3 * mult);
			path.lineTo(X, Y + h / 3 * mult);
			pathSlide.reset();
			pathSlide.moveTo(0, h / 3 * mult);
			pathSlide.lineTo(w / 2, h / 3 * mult);
			pathSlide.lineTo(0, -h * 2 / 3 * mult);
			pathSlide.lineTo(-w / 2, h / 3 * mult);
			pathSlide.lineTo(0, h / 3 * mult);
			textHeight = textSizePix[0];
			textY = Y + (upOrDown ? textHeight * 3 / 16 : textHeight * 9 / 16);
		}

		public void setSlidePath(float percent) {
			if (slideTo >= 0 && slideTo <= 15) {
				Triangle triFinal = triangles.get(slideTo);
				float xFinal = triFinal.getX();
				float yFinal = triFinal.getY();
				float yTextFinal = triFinal.getTextY();
				boolean upOrDownFinal = triFinal.isUpOrDown();
				xSlide = (xFinal - X) * percent + X;
				ySlide = (yFinal - Y) * percent + Y;
				yTextSlide = (yTextFinal - textY) * percent + textY;
				if (upOrDownFinal != upOrDown) {
					angle = (float) (percent * 60);
				} else {
					angle = 0;
				}
			}
		}

		public boolean isUpOrDown() {
			return upOrDown;
		}

		public boolean isSliding() {
			return (slideTo >= 0 && slideTo <= 15);
		}

		public boolean isNewNumber() {
			return newNumber;
		}

		public void reset() {
			newNumber = false;
			slideTo = -1;
			slideFrom = -1;
			previousValue = value;
			textPaintPrevious.setTextSize(textPaint.getTextSize());
			textPaintPrevious.setColor(textPaint.getColor());
			backgroundPaintPrevious.setColor(backgroundPaint.getColor());
		}
	}

	private enum SetPrevious {
		DONT_SET, SET_TO_PREVIOUS_VALUE, SET_TO_VALUE;
	}

	private enum Direction {
		RIGHT, UP_RIGHT, UP_LEFT, LEFT, DOWN_LEFT, DOWN_RIGHT;

		public static Direction fromValue(int value) {
			switch (value) {
			case 0:
			case 6:
				return RIGHT;
			case 1:
				return UP_RIGHT;
			case 2:
				return UP_LEFT;
			case 3:
				return LEFT;
			case 4:
				return DOWN_LEFT;
			case 5:
				return DOWN_RIGHT;
			default:
				return RIGHT;
			}
		}

		public static Direction opposite(Direction dir) {
			switch (dir) {
			case RIGHT:
				return LEFT;
			case UP_RIGHT:
				return DOWN_LEFT;
			case UP_LEFT:
				return DOWN_RIGHT;
			case LEFT:
				return RIGHT;
			case DOWN_LEFT:
				return UP_RIGHT;
			case DOWN_RIGHT:
				return UP_LEFT;
			default:
				return RIGHT;
			}
		}
	}

	// =========================================
	// Constructors
	// =========================================

	public JoystickView(Context context) {
		super(context);
		initView(context);
	}

	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.JoystickView, 0, 0);

		try {
			labelTextSize = a.getDimension(R.styleable.JoystickView_labelTextSize, 0);
			textTextSize = a.getDimension(R.styleable.JoystickView_textTextSize, 0);
			textTextSizeBig = a.getDimension(R.styleable.JoystickView_textTextSizeBig, 0);
			Restart = a.getString(R.styleable.JoystickView_restart);
			if (Restart == null)
				Restart = context.getString(R.string.joystick_restart);
			Undo = a.getString(R.styleable.JoystickView_undo);
			if (Undo == null)
				Undo = context.getString(R.string.joystick_restart);
			place1 = a.getString(R.styleable.JoystickView_place1);
			if (place1 == null)
				place1 = context.getString(R.string.leaderboard_default_place1st);
			name1 = a.getString(R.styleable.JoystickView_name1);
			if (name1 == null)
				name1 = context.getString(R.string.leaderboard_default_name1);
			score1 = a.getString(R.styleable.JoystickView_score1);
			if (score1 == null)
				score1 = context.getString(R.string.leaderboard_default_score1);
			place2 = a.getString(R.styleable.JoystickView_place2);
			if (place2 == null)
				place2 = context.getString(R.string.leaderboard_default_place2);
			name2 = a.getString(R.styleable.JoystickView_name2);
			if (name2 == null)
				name2 = context.getString(R.string.leaderboard_default_name2);
			score2 = a.getString(R.styleable.JoystickView_score2);
			if (score2 == null)
				score2 = context.getString(R.string.leaderboard_default_score2);
		} finally {
			a.recycle();
		}
		initView(context);
	}

	public JoystickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	// =========================================
	// Initialization
	// =========================================

	private void initView(Context ctx) {
		this.ctx = ctx;
		res = getResources();
		setFocusable(true);
		animateHandler = new Handler();
		minDistance = dpToPx(minSlideDistanceDP);
		padding = dpToPx(paddingDP);
		textPadding = spToPx(textPaddingDP);
		newNumberScale = 1;
		slid = false;
		previousScore = 0;
		currentScore = 0;
		bestScore = 0;
		scorePlus = 0;
		undos = 0;

		triangleBigPath = new Path();
		adPathOutside = new Path();
		adPathInside = new Path();
		for (int i = 0; i < NumBoxes; i++) {
			labelBackground[i] = new Path();
			textBackground[i] = new Path();
		}
		labels[0] = res.getString(R.string.score_current);
		labels[1] = res.getString(R.string.score_best);
		labels[2] = res.getString(R.string.score_global);

		labelBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		labelBackgroundPaint.setColor(res.getColor(R.color.background384));                    // set the color
		labelBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);       // set to STOKE
		labelBackgroundPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
		labelBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
		labelBackgroundPaint.setPathEffect(new CornerPathEffect(padding));   // set the path effect when they join.

		triangleBigPaintRounded = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		triangleBigPaintRounded.set(labelBackgroundPaint);
		triangleBigPaintRounded.setColor(res.getColor(R.color.background));                    // set the color
		triangleBigPaintRounded.setPathEffect(new CornerPathEffect(padding * 2));   // set the path effect when they join

		scoreboardPaintRounded = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		scoreboardPaintRounded.set(triangleBigPaintRounded);
		scoreboardPaintRounded.setPathEffect(new CornerPathEffect(padding));   // set the path effect when they join.

		triangleSmallPaintRounded = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		triangleSmallPaintRounded.set(triangleBigPaintRounded);
		triangleSmallPaintRounded.setColor(res.getColor(R.color.background0));
		triangleSmallPaintRounded.setPathEffect(new CornerPathEffect(padding));   // set the path effect when they join.

		adPaintRounded = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		adPaintRounded.set(triangleBigPaintRounded);
		adPaintRounded.setColor(res.getColor(R.color.white));
		adPaintRounded.setPathEffect(new CornerPathEffect(padding));   // set the path effect when they join.

		labelTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		labelTextPaint.setTextAlign(Paint.Align.CENTER);
		labelTextPaint.setColor(res.getColor(R.color.background3));
		labelTextPaint.setTextSize(labelTextSize);

		textTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textTextPaint.setTextAlign(Paint.Align.CENTER);
		textTextPaint.setColor(res.getColor(R.color.background196608));
		textTextPaint.setTextSize(textTextSize);

		leftTextPaint = new TextPaint(textTextPaint);
		leftTextPaint.setTextAlign(Paint.Align.LEFT);

		leftTextPaintBig = new TextPaint(leftTextPaint);
		leftTextPaintBig.setTextSize(textTextSizeBig);

		rightTextPaint = new TextPaint(textTextPaint);
		rightTextPaint.setTextAlign(Paint.Align.RIGHT);

		rightTextPaintBig = new TextPaint(rightTextPaint);
		rightTextPaintBig.setTextSize(textTextSizeBig);

		for (int i = 0; i < NumTextSizes; i++) {
			textSizePix[i] = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP[i], res.getDisplayMetrics());
			textPaint[i] = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			textPaint[i].setTextAlign(Paint.Align.CENTER);
			textPaint[i].setColor(Color.BLACK);
			textPaint[i].setTextSize(textSizePix[i]);
		}

		ad = res.getDrawable(R.drawable.hiq_ad);
		ad.setBounds(-ad.getIntrinsicWidth() / 2, -ad.getIntrinsicHeight() / 2, ad.getIntrinsicWidth() / 2, ad.getIntrinsicHeight() / 2);

		listener = new JoystickListener() {
			@Override
			public void onGameOver(int score) {
			}

			@Override
			public void onUndoSelected(int undos) {
			}

			@Override
			public void onRestartSelected() {
			}

			@Override
			public void onAdSelected() {
			}

			@Override
			public void onLeaderboardSelected() {
			}
		};

		triangles = new ArrayList<Triangle>(NumTriangles);
		for (int i = 0; i < NumTriangles; i++) {
			triangles.add(new Triangle(i));
		}
		states = new ArrayList<Bundle>(undos);
		newRandom(true);
	}

	// =========================================
	// Public Methods
	// =========================================

	public void setOnJostickListener(JoystickListener listener) {
		this.listener = listener;
	}

	public void setTypeface(Typeface font) {
		for (int i = 0; i < NumTextSizes; i++) {
			textPaint[i].setTypeface(font);
		}
		labelTextPaint.setTypeface(font);
		textTextPaint.setTypeface(font);
		leftTextPaint.setTypeface(font);
		rightTextPaint.setTypeface(font);
		leftTextPaintBig.setTypeface(font);
		rightTextPaintBig.setTypeface(font);
	}

	public void removeCallbacks() {
		animateHandler.removeCallbacksAndMessages(null);
	}

	public void setBest(int best) {
		bestScore = best;
		invalidate();
	}

	public Bundle getState() {
		Bundle state = new Bundle();
		state.putInt("score", currentScore);
		state.putInt("undos", undos);
		int[] values = new int[NumTriangles];
		for (int i = 0; i < NumTriangles; i++) {
			values[i] = triangles.get(i).getValue();
		}
		state.putIntArray("values", values);
		return state;
	}

	public int[] getValues() {
		int[] values = new int[NumTriangles];
		for (int i = 0; i < NumTriangles; i++) {
			values[i] = triangles.get(i).getValue();
		}
		return values;
	}

	public int getCurrentScore() {
		return currentScore;
	}

	public int getUndos() {
		return undos;
	}

	public boolean setState(int score, int undos, int[] values) {
		currentScore = score;
		this.undos = undos;
		if (values.length != NumTriangles)
			return false;
		for (int i = 0; i < NumTriangles; i++) {
			triangles.get(i).setValue(values[i], false, SetPrevious.SET_TO_VALUE);
		}
		states.clear();
		states.add(getState());
		return true;
	}

	public void reset() {
		currentScore = 0;
		undos = maxUndos;
		for (int i = 0; i < NumTriangles; i++) {
			triangles.get(i).setValue(0, false, SetPrevious.SET_TO_VALUE);
		}
		newRandom(true);
		invalidate();
	}

	public void setMaxUndos(int max) {
		if (max > maxUndos)
			undos += max - maxUndos;
		maxUndos = max;
		invalidate();
	}

	public void setLeaderboard(String name1, String score1, String name2, String score2, String place2) {
		this.name1 = name1;
		this.score1 = score1;
		this.name2 = name2;
		this.score2 = score2;
		this.place2 = place2;
		invalidate();
	}

	public void setLeaderboardUserName(String name, boolean firstPlace) {
		if (firstPlace)
			name1 = name;
		else
			name2 = name;
		invalidate();
	}

	public int getGameBottom() {
		return (int) (scoreboardHeight + padding + gameHeight);
	}

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		PaddingLeft = getPaddingLeft();
		PaddingTop = getPaddingTop();
		PaddingRight = getPaddingRight();
		PaddingBottom = getPaddingBottom();
		Width = measure(widthMeasureSpec);
		Height = measure(heightMeasureSpec);
		scoreboardHeight = padding * 3 / 2 + textPadding * 10 + labelTextSize * 2 + textTextSize * 3;
		adHeight = padding * 2 + ad.getIntrinsicHeight();
		gameHeight = (Width - PaddingLeft - PaddingRight) / 2 * sqrt3;
		int minHeight = (int) (PaddingTop + scoreboardHeight + padding + gameHeight + padding + adHeight + PaddingBottom);
		if (minHeight > Height) {
			// if (leaderboard) {
			leaderboard = false;
			scoreboardHeight = padding + textPadding * 4 + labelTextSize + textTextSize;
			minHeight = (int) (PaddingTop + scoreboardHeight + padding + gameHeight + padding + adHeight + PaddingBottom);
			if (minHeight > Height) {
				gameHeight += (Height - minHeight);
			}
		} else
			leaderboard = true;
		// Height = (int) (PaddingTop + scoreBoardHeight + padding + gameHeight + padding + adHeight + PaddingBottom);

		/*if (Height > measure(heightMeasureSpec)) {
			Height = measure(heightMeasureSpec);
			Width = (int) ((Height - pt - pb) * 2 / sqrt3 + pl + pr);
		}*/
		h = (gameHeight - padding * 15 / 4) / 4;
		w = 2 * h / sqrt3;
		setTrianglePaths();

		setMeasuredDimension(Width, Height);

		initRunnables();
	}

	private int measure(int measureSpec) {
		int result = 0;
		// Decode the measurement specifications.
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.UNSPECIFIED) {
			// Return a default size of 480 if no bounds are specified.
			result = 480;
		} else {
			// As you want to fill the available space
			// always return the full available bounds.
			result = specSize;
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Drawing Restart, Undos(#)
		canvas.drawText(Restart, PaddingLeft, triangles.get(0).getTextY(), leftTextPaintBig);
		/*canvas.drawText(Undo + "(" + ((undos == 0 || states.size() <= 1) ? "?" : Math.min(undos, states.size() - 1)) + ")", Width
				- PaddingRight, triangles.get(0).getTextY(), rightTextPaintBig);*/
		canvas.drawText(Undo + "(" + ((undos == 0) ? "?" : undos) + ")", Width - PaddingRight, triangles.get(0).getTextY(),
				rightTextPaintBig);

		// Drawing Game Board
		canvas.drawPath(triangleBigPath, triangleBigPaintRounded);
		boolean sliding = false, newNumbers = false;
		for (int i = 0; i < NumTriangles; i++) {
			Triangle tri = triangles.get(i);
			canvas.drawPath(tri.getPath(), triangleSmallPaintRounded);
			if (tri.isSliding())
				sliding = true;
			else if (tri.isNewNumber())
				newNumbers = true;
		}
		if (sliding) {
			for (int i = 0; i < NumTriangles; i++) {
				Triangle tri = triangles.get(i);
				if (!tri.isSliding()) {
					int value = tri.getPreviousValue();
					if (value > 0) {
						canvas.drawPath(tri.getPath(), tri.getBackgroundPaintPrevious());
						canvas.drawText(value + "", tri.getX(), tri.getTextY(), tri.getTextPaintPrevious());
					}
				}
			}
			for (int i = 0; i < NumTriangles; i++) {
				Triangle tri = triangles.get(i);
				if (tri.isSliding()) {
					int value = tri.getPreviousValue();
					if (value == 0)
						value = tri.getValue();
					canvas.save();
					canvas.translate(tri.getXSlide(), tri.getYSlide());
					canvas.rotate(tri.getAngle());
					canvas.drawPath(tri.getPathSlide(), tri.getBackgroundPaintPrevious());
					canvas.restore();
					canvas.drawText(value + "", tri.getXSlide(), tri.getYTextSlide(), tri.getTextPaintPrevious());
				}
			}
		} else if (newNumbers) {
			for (int i = 0; i < NumTriangles; i++) {
				Triangle tri = triangles.get(i);
				if (tri.isNewNumber()) {
					canvas.save();
					canvas.translate(tri.getX(), tri.getY());
					canvas.scale(newNumberScale, newNumberScale);
					canvas.drawPath(tri.getPathSlide(), tri.getBackgroundPaint());
					canvas.drawText(tri.getValue() + "", 0, 0, tri.getTextPaint());
					canvas.restore();
				} else {
					int value = tri.getValue();
					if (value > 0) {
						canvas.drawPath(tri.getPath(), tri.getBackgroundPaint());
						canvas.drawText(value + "", tri.getX(), tri.getTextY(), tri.getTextPaint());
					}
				}
			}
		} else {
			for (int i = 0; i < NumTriangles; i++) {
				Triangle tri = triangles.get(i);
				int value = tri.getValue();
				if (value > 0) {
					canvas.drawPath(tri.getPath(), tri.getBackgroundPaint());
					canvas.drawText(value + "", tri.getX(), tri.getTextY(), tri.getTextPaint());
				}
			}
		}

		// Drawing ScoreBoard
		canvas.drawRect(PaddingLeft, PaddingTop, Width - PaddingRight, scoreboardHeight + PaddingTop, scoreboardPaintRounded);
		for (int i = 0; i < NumBoxes; i++) {
			if (i < 2 || leaderboard) {
				canvas.drawPath(labelBackground[i], labelBackgroundPaint);
				canvas.drawPath(textBackground[i], triangleSmallPaintRounded);
				canvas.drawText(labels[i], labelCenterX[i], labelCenterY[i], labelTextPaint);
				if (i == 0) {
					canvas.save();
					canvas.translate(textCenterX[i], textCenterY[i]);
					if (sliding) {
						canvas.drawText(previousScore + "", 0, 0, textTextPaint);
					} else if (newNumbers) {
						canvas.scale(newNumberScale, newNumberScale);
						canvas.drawText(previousScore + "", 0, 0, textTextPaint);
					} else {
						canvas.drawText(currentScore + "", 0, 0, textTextPaint);
					}
					canvas.restore();
				} else if (i == 1) {
					canvas.drawText(bestScore + "", textCenterX[i], textCenterY[i], textTextPaint);
				} else {
					canvas.drawText(place1, textCenterX[i] - Width / 6 - padding, textCenterY[i], rightTextPaint);
					canvas.drawText(name1, textCenterX[i] - Width / 6 + padding, textCenterY[i], leftTextPaint);
					canvas.drawText(score1, Width - PaddingRight - padding * 2, textCenterY[i], rightTextPaint);
					canvas.drawText(place2, textCenterX[i] - Width / 6 - padding, textCenterY[i] + textPadding * 2 + textTextSize,
							rightTextPaint);
					canvas.drawText(name2, textCenterX[i] - Width / 6 + padding, textCenterY[i] + textPadding * 2 + textTextSize,
							leftTextPaint);
					canvas.drawText(score2, Width - PaddingRight - padding * 2, textCenterY[i] + textPadding * 2 + textTextSize,
							rightTextPaint);
				}
			}
		}

		// Draw the Ad
		canvas.drawRect(PaddingLeft, (int) (Height - adHeight), Width - PaddingRight, Height, scoreboardPaintRounded);
		canvas.drawRect(PaddingLeft + padding / 2, (int) (Height - adHeight + padding / 2), Width - PaddingRight - padding / 2, Height
				- padding / 2, adPaintRounded);
		canvas.save();
		canvas.translate(Width / 2, Height - adHeight / 2);
		ad.draw(canvas);
		canvas.restore();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int actionType = event.getAction();
		if (actionType == MotionEvent.ACTION_DOWN) {
			startX = event.getX();
			startY = event.getY();
			if ((startY > scoreboardHeight + PaddingTop + padding) && (startY < PaddingTop + scoreboardHeight + padding * 3 + h)) {
				if (startX < (Width - w) / 2 - padding) {
					listener.onRestartSelected();
				} else if (startX > (Width + w) / 2 + padding) {
					if (undos == 0) {
						listener.onUndoSelected(0);
					} else if (undos > 0 && states.size() > 1) {
						undoSlide();
					}
				}
			} else if ((startY < scoreboardHeight + PaddingTop) && (startY > leaderboardTop) && leaderboard) {
				listener.onLeaderboardSelected();
			} else if ((startY > Height - adHeight) && (startY < Height)) {
				listener.onAdSelected();
			}
		} else if (actionType == MotionEvent.ACTION_MOVE && !slid) {
			double moveX = event.getX(), moveY = event.getY();
			double distance = Math.sqrt(Math.pow(moveX - startX, 2) + Math.pow(moveY - startY, 2));
			if (distance > minDistance) {
				slid = true;
				Direction direction = null;
				double angle = Math.atan2(startY - moveY, moveX - startX);
				if (angle < 0)// because atan2 returns from -pi to pi, we want 0 to 2pi
					angle += 2 * Math.PI;
				for (int i = 0; i <= 6; i++) {
					double angleDiff = Math.abs(i * Math.PI / 3 - angle);
					if (angleDiff < angleDifferenceMax) {
						direction = Direction.fromValue(i);
					}
				}
				if (direction != null) {
					slideAll(direction);
				}
			}
		} else if (actionType == MotionEvent.ACTION_UP) {
			slid = false;
		}
		return true;
	}

	private void slideAll(Direction dir) {
		boolean combined = false, slid = false;
		scorePlus = 0;
		int[] startingPositions = getStartingPositions(dir);
		for (int i = 0; i < 4; i++) {
			int score_single = combine(dir, startingPositions[i], false);
			scorePlus += score_single;
			combined = (score_single > 0) || combined;
			slid = slide(dir, startingPositions[i]) || slid;
		}
		if (combined || slid) {
			newRandom(false);
			previousScore = currentScore;
			currentScore += scorePlus + scoreMinus;
			startTimeSlide = System.currentTimeMillis();
			animateHandler.post(slide);
		} else if (isGameOver()) {
			listener.onGameOver(currentScore);
		}
	}

	private int combine(Direction dir, int startPosition, boolean test) {

		Direction oppDir = Direction.opposite(dir);
		int position = startPosition;
		int positionNext = triangles.get(position).getDirection(oppDir);
		int score = 0;

		while (positionNext >= 0 && positionNext <= 15) {
			int value = triangles.get(position).getValue();
			int valueNext = triangles.get(positionNext).getValue();
			if (value == valueNext && value != 0) {
				score += value * 2;
				if (!test) {
					triangles.get(position).setValue(value * 2, true, SetPrevious.SET_TO_PREVIOUS_VALUE);
					triangles.get(position).setSlideFrom(positionNext);
					triangles.get(positionNext).setValue(0, false, SetPrevious.SET_TO_PREVIOUS_VALUE);
					triangles.get(positionNext).setSlideTo(position);
				}
			}
			if (valueNext != 0)
				position = positionNext;
			positionNext = triangles.get(positionNext).getDirection(oppDir);
		}
		return score;
	}

	private boolean slide(Direction dir, int startPosition) {

		Direction oppDir = Direction.opposite(dir);
		int position = startPosition;
		int zeros = 0;
		boolean slid = false;

		while (position >= 0 && position <= 15) {
			int value = triangles.get(position).getValue();
			if (value > 0) {
				slid = move(dir, zeros, position);
			} else {
				zeros++;
			}
			position = triangles.get(position).getDirection(oppDir);
		}
		return slid;
	}

	private boolean move(Direction dir, int spaces, int position) {
		if (spaces > 0) {
			Triangle tri = triangles.get(position);
			int value = tri.getValue();
			int fromPosition = tri.getSlideFrom();
			boolean newNumber = tri.isNewNumber();
			int newPosition = position;

			for (int i = 0; i < spaces; i++) {
				newPosition = triangles.get(newPosition).getDirection(dir);
			}

			if (newPosition >= 0 && newPosition <= 15) {
				tri.setValue(0, false, SetPrevious.DONT_SET);
				tri.setSlideTo(newPosition);
				if (fromPosition >= 0 && fromPosition <= 15) {
					triangles.get(fromPosition).setSlideTo(newPosition);
				}
				triangles.get(newPosition).setValue(value, newNumber, SetPrevious.DONT_SET);
			}
			return true;
		} else {
			return false;
		}
	}

	private void newRandom(boolean first) {
		if (first) {
			int pos = rand.nextInt(16);
			triangles.get(pos).setValue(3, false, SetPrevious.SET_TO_VALUE);
			int upper = 15 - pos;
			if (upper > pos)
				pos = rand.nextInt(upper) + pos + 1;
			else
				pos = rand.nextInt(pos);
			triangles.get(pos).setValue(3, false, SetPrevious.SET_TO_VALUE);
			states.clear();
			states.add(getState());
		} else {
			List<Integer> openTriangles = new ArrayList<Integer>();
			for (int i = 0; i < NumTriangles; i++) {
				if (triangles.get(i).getValue() == 0)
					openTriangles.add(i);
			}
			if (openTriangles.size() > 0) {
				int value = rand.nextInt(10);
				if (value == 0)
					value = 6;
				else
					value = 3;
				triangles.get(openTriangles.get(rand.nextInt(openTriangles.size()))).setValue(value, !first, SetPrevious.DONT_SET);
				if (states.size() >= undos + 2 && states.size() >= 1) {
					states.remove(states.size() - 1);
					states.add(0, getState());
				} else {
					states.add(0, getState());
				}
			}
		}
	}

	private void undoSlide() {
		undos -= 1;
		states.remove(0);
		setState(states.get(0));
		invalidate();
	}

	private void setState(Bundle state) {
		currentScore = state.getInt("score");
		int[] values = state.getIntArray("values");
		for (int i = 0; i < NumTriangles; i++) {
			triangles.get(i).setValue(values[i], false, SetPrevious.SET_TO_VALUE);
		}
	}

	private void initRunnables() {
		slide = new Runnable() {
			@Override
			public void run() {
				lastTimeSlide = System.currentTimeMillis();
				float percent = (lastTimeSlide - startTimeSlide) / (float) totalTimeSlide;
				for (int i = 0; i < NumTriangles; i++) {
					triangles.get(i).setSlidePath(percent > 1 ? 1 : percent);
				}
				if (percent <= 1) {
					invalidate();
					animateHandler.postDelayed(slide, frameTimeSlide);
				} else {
					for (int i = 0; i < NumTriangles; i++) {
						triangles.get(i).setSlideTo(-1);
					}
					startTimeNewNumbers = lastTimeSlide;
					animateHandler.post(newNumbers);
				}
			}
		};
		newNumbers = new Runnable() {
			@Override
			public void run() {
				lastTimeNewNumbers = System.currentTimeMillis();
				float percent = (lastTimeNewNumbers - startTimeNewNumbers) / (float) totalTimeNewNumbers;
				if (percent <= 1) {
					newNumberScale = setNewNumberScale(percent);
					invalidate();
					animateHandler.postDelayed(newNumbers, frameTimeNewNumbers);
				} else {
					newNumberScale = 1;
					for (int i = 0; i < NumTriangles; i++) {
						triangles.get(i).reset();
					}
					invalidate();
					if (isGameOver())
						listener.onGameOver(currentScore);
				}
			}
		};
	}

	private void setTrianglePaths() {
		for (int i = 0; i < NumBoxes; i++) {
			int row = i / 2;
			int col = i % 2;
			float left, topLabel, right, bottomLabel, bottomText1, bottomText2;
			if (row == 0) {
				topLabel = PaddingTop + padding / 2;
				bottomLabel = topLabel + textPadding * 2 + labelTextSize;
				bottomText2 = bottomLabel + textPadding * 2 + textTextSize;
				textCenterY[i] = (bottomLabel + bottomText2) / 2 + textTextSize * 3 / 8;
			} else {
				topLabel = PaddingTop + padding + textPadding * 4 + labelTextSize + textTextSize;
				leaderboardTop = topLabel;
				bottomLabel = topLabel + textPadding * 2 + labelTextSize;
				bottomText1 = bottomLabel + textPadding * 2 + textTextSize;
				bottomText2 = bottomText1 + textPadding * 2 + textTextSize;
				textCenterY[i] = (bottomLabel + bottomText1) / 2 + textTextSize * 3 / 8;
			}
			labelCenterY[i] = (topLabel + bottomLabel) / 2 + labelTextSize * 3 / 8;

			if (col == 0 && row == 0) {
				left = PaddingLeft + padding / 2;
				right = Width / 2 - padding / 4;
			} else if (col == 1) {
				left = Width / 2 + padding / 4;
				right = Width - PaddingRight - padding / 2;
			} else {
				left = PaddingLeft + padding / 2;
				right = Width - PaddingRight - padding / 2;
			}
			labelCenterX[i] = (left + right) / 2;
			textCenterX[i] = labelCenterX[i];

			labelBackground[i].moveTo(left, bottomLabel);
			labelBackground[i].lineTo(left, topLabel);
			labelBackground[i].lineTo(right, topLabel);
			labelBackground[i].lineTo(right, bottomLabel);

			textBackground[i].moveTo(left, bottomLabel);
			textBackground[i].lineTo(left, bottomText2);
			textBackground[i].lineTo(right, bottomText2);
			textBackground[i].lineTo(right, bottomLabel);
		}

		int gameBottom = (int) (PaddingTop + scoreboardHeight + padding + gameHeight);
		int left = (int) (Width / 2 - gameHeight / sqrt3);
		int right = (int) (Width / 2 + gameHeight / sqrt3);
		triangleBigPath.reset();
		triangleBigPath.moveTo(Width / 2, gameBottom);
		triangleBigPath.lineTo(left, gameBottom);
		triangleBigPath.lineTo(Width / 2, scoreboardHeight + PaddingTop + padding);
		triangleBigPath.lineTo(right, gameBottom);
		triangleBigPath.lineTo(Width / 2, gameBottom);

		Rect bounds = new Rect();
		for (int i = 0; i < NumTextSizes; i++) {
			textTextPaint.getTextBounds(textToSize[i], 0, textToSize[i].length(), bounds);
			float y = bounds.height();
			float x = bounds.width();
			float ratio = x / y;
			textSizePix[i] = (int) ((((2 - 4 * trianglePaddingPercent) * h - w / sqrt3) / (2 + sqrt3 * ratio))
					/ (1 - 2 * percent / (2 + sqrt3 * ratio)) * pixToFontSize);
			textPaint[i].setTextSize(textSizePix[i]);
		}

		for (int i = 0; i < NumTriangles; i++) {
			Triangle tri = triangles.get(i);
			float X = Width / 2 + (w + padding * sqrt3 / 2) / 2 * (tri.getPositionInMatrix() - 4);
			float Y = PaddingTop + scoreboardHeight + padding * 2 + h * 2 / 3 + (h + padding * 3 / 4) * (tri.getRow() - 1);
			tri.setXY(X, Y);
			tri.resetTextPaint(textPaint[0]);
		}

		adPathOutside.reset();
		adPathOutside.moveTo(Width / 2, Height);
		adPathOutside.lineTo(PaddingLeft, Height);
		adPathOutside.lineTo(PaddingLeft, Height - adHeight);
		adPathOutside.lineTo(Width - PaddingRight, Height - adHeight);
		adPathOutside.lineTo(Width - PaddingRight, Height);
		adPathOutside.moveTo(Width / 2, Height);

		adPathInside.reset();
		adPathInside.moveTo(Width / 2, Height - padding);
		adPathInside.lineTo(PaddingLeft + padding, Height - padding);
		adPathInside.lineTo(PaddingLeft + padding, Height - adHeight + padding);
		adPathInside.lineTo(Width - PaddingRight - padding, Height - adHeight + padding);
		adPathInside.lineTo(Width - PaddingRight - padding, Height - padding);
		adPathInside.moveTo(Width / 2, Height - padding);
	}

	private boolean isOdd(int val) {
		return (val & 0x01) != 0;
	}

	private int dpToPx(int dp) {
		DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
		int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return px;
	}

	private int spToPx(int sp) {
		DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
		int px = Math.round(sp * (displayMetrics.scaledDensity));
		return px;
	}

	private float setNewNumberScale(float percent) {
		// this.scale = (float) (Math.sin(percent * Math.PI / 4 + Math.PI / 6) * 1.05);
		// this.scale = (float) (Math.sin(percent * 2*Math.PI / 3) * 1.1);
		// this.scale = percent / 2 + .6f;
		return (float) (.833 - Math.cos(percent * Math.PI) / 3);
	}

	public boolean isGameOver() {
		for (int i = 0; i < NumTriangles; i++) {
			if (triangles.get(i).getValue() == 0)
				return false;
		}
		Direction[] allDirs = { Direction.RIGHT, Direction.UP_RIGHT, Direction.UP_LEFT, Direction.LEFT, Direction.DOWN_LEFT,
				Direction.DOWN_RIGHT };
		int score = 0;
		for (Direction dir : allDirs) {
			int[] startingPositions = getStartingPositions(dir);
			for (int i = 0; i < 4; i++)
				score += combine(dir, startingPositions[i], true);
			if (score > 0)
				break;
		}
		return (score == 0);
	}

	private int[] getStartingPositions(Direction dir) {
		int[] startingPositions = new int[4];
		for (int i = 1; i <= 4; i++) {
			switch (dir) {
			case RIGHT:
			case UP_RIGHT:
				startingPositions[i - 1] = i * i - 1;
				break;
			case LEFT:
			case UP_LEFT:
				startingPositions[i - 1] = (i - 1) * (i - 1);
				break;
			case DOWN_LEFT:
			case DOWN_RIGHT:
				startingPositions[i - 1] = 2 * i + 7;
				break;
			default:
				startingPositions[i - 1] = i * i - 1;
				break;
			}
		}
		return startingPositions;
	}
}