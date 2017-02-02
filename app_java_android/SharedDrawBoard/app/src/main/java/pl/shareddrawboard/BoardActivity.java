package pl.shareddrawboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.shareddrawboard.domain.StateContainer;

public class BoardActivity extends AppCompatActivity {

	private static final String TAG = BoardActivity.class.getSimpleName();

	@Bind(R.id.boardName)
	TextView boardName;
	@Bind(R.id.boardView)
	BoardView boardView;
	@Bind(R.id.quit)
	Button quit;
	@Bind(R.id.button_draw_move)
	Button buttonDrawMove;
	@Bind(R.id.scrollView)
	CustomScrollView scrollView;

	boolean drawMode = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_board);
		ButterKnife.bind(this);
		if(StateContainer.instance.getBoard() != null) {
			boardName.setText(StateContainer.instance.getBoard().getName());
		}
	}

	@OnClick(R.id.quit)
	public void onQuitClick() {
		startActivity(new Intent(getApplicationContext(), MenuActivity.class));
		finish();
	}

	@OnClick(R.id.button_draw_move)
	public void onDrawMoveClick() {
		drawMode = !drawMode;
		boardView.setDrawMode(drawMode);
		buttonDrawMove.setText(drawMode ? "Move" : "Draw");
		scrollView.setScrollEnabled(!drawMode);
	}
}
