package pl.shareddrawboard;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import pl.shareddrawboard.api.DrawboardClientPool;
import pl.shareddrawboard.api.DrawboardServer;
import pl.shareddrawboard.domain.Board;
import pl.shareddrawboard.domain.BoardUpdate;
import pl.shareddrawboard.domain.Point;
import pl.shareddrawboard.domain.StateContainer;

public class BoardView extends View implements View.OnTouchListener {

	private static final String TAG = BoardView.class.getSimpleName();

	private TextPaint mTextPaint;
	private Paint paint;

	private Board board;
	private BoardUpdate boardUpdate;
	private DrawboardServer server;
	private DrawboardClientPool drawboardClientPool;

	int fieldSize = 5;
	int currentColor = Color.BLACK;
	int currentBrushSize = 5;

	private Point previousPoint;
	private boolean drawMode;

	public BoardView(Context context) {
		super(context);
		init(null, 0);
	}

	public BoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public BoardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		// Set up a default TextPaint object
		mTextPaint = new TextPaint();
		mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Paint.Align.LEFT);
		mTextPaint.setTextSize(60);

		paint = new Paint();
		setBackgroundColor(Color.LTGRAY);

		server = StateContainer.instance.getServer();
		board = StateContainer.instance.getBoard();
		drawboardClientPool = StateContainer.instance.getClientPool();

		if(server != null) {
			server.setView(this);
		}

		setOnTouchListener(this);

	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//Log.i(TAG, "draaawing");
		super.onDraw(canvas);

		paint.setColor(Color.WHITE);
		canvas.drawRect(0, 0, fieldSize*board.getWidth(), fieldSize*board.getHeight(), paint);

		paint.setColor(currentColor);
		for (int j = 0; j < board.getHeight(); ++j) {
			for (int i = 0; i < board.getWidth(); ++i) {
				int field = board.getField(i, j);
				if(field != Color.WHITE) {
					//Log.i(TAG, "draaawing at "+i+","+j);
					canvas.drawRect(fieldSize*i, fieldSize*j, fieldSize*(i + 1), fieldSize*(j + 1), paint);
				}
			}
		}

	}

	/**
	 * Called when a touch event is dispatched to a view. This allows listeners to
	 * get a chance to respond before the target view.
	 *
	 * @param v     The view the touch event has been dispatched to.
	 * @param event The MotionEvent object containing full information about
	 *              the event.
	 * @return True if the listener has consumed the event, false otherwise.
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN: case MotionEvent.ACTION_POINTER_DOWN:
				//Log.i(TAG, "DOWN: "+event.toString());
				if (boardUpdate == null) {
					Log.i(TAG, "new board");
					boardUpdate = new BoardUpdate(currentColor, currentBrushSize);
				}
				addPoint(new Point(event.getX()/fieldSize, event.getY()/fieldSize));
				break;
			case MotionEvent.ACTION_MOVE:
				//Log.i(TAG, "MOVE: "+event.toString());
				addPoint(new Point(event.getX()/fieldSize, event.getY()/fieldSize));

				break;
			case MotionEvent.ACTION_UP: case MotionEvent.ACTION_POINTER_UP:
				//Log.i(TAG, "UP: "+event.toString());
				BoardUpdate toSend;
				synchronized (this) {
					toSend = boardUpdate;
					boardUpdate = null;
				}
				//drawboardClientPool.sendBoardUpdate(toSend);
				break;
		}
		return true;
	}

	private void addPoint(Point point) {
		if (!boardUpdate.getPointsDrawn().isEmpty()) {
			Point previous = boardUpdate.getPointsDrawn().get(boardUpdate.getPointsDrawn().size() - 1);
			if (point.equals(previous)) {
				return;
			}
		}

		board.update(point, boardUpdate.getBrushSize(), boardUpdate.getBrushColor());

		drawboardClientPool.send(point, previousPoint == null ? point : previousPoint);
		previousPoint = point;

		//boardUpdate.addPointDrawn(point);
		invalidate();
		Log.i(TAG, "adding point " + point + "of color "+boardUpdate.getBrushColor());

	}

	public void setDrawMode(boolean drawMode) {
		this.drawMode = drawMode;
		setOnTouchListener(drawMode ? this : null);
	}
}
