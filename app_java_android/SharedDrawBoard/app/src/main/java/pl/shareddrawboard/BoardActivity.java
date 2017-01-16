package pl.shareddrawboard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BoardActivity extends AppCompatActivity {

	private static final String TAG = BoardActivity.class.getSimpleName();

	@Bind(R.id.boardName)
	EditText boardName;
	@Bind(R.id.boardView)
	BoardView boardView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_board);
		ButterKnife.bind(this);

	}
}
