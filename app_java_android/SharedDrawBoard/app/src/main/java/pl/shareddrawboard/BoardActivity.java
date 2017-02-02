package pl.shareddrawboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BoardActivity extends AppCompatActivity {

	private static final String TAG = BoardActivity.class.getSimpleName();

	@Bind(R.id.boardName)
	EditText boardName;
	@Bind(R.id.boardView)
	BoardView boardView;
	@Bind(R.id.quit)
	Button quit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_board);
		ButterKnife.bind(this);

	}

	@OnClick(R.id.quit)
	public void onClick() {
		startActivity(new Intent(getApplicationContext(), MenuActivity.class));
		finish();
	}
}
