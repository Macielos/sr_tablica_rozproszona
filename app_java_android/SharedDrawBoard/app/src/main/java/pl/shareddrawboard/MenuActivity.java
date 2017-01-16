package pl.shareddrawboard;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import pl.shareddrawboard.domain.Board;
import pl.shareddrawboard.domain.StateContainer;

public class MenuActivity extends AppCompatActivity {

	private static final String TAG = MenuActivity.class.getSimpleName();

	@Bind(R.id.create)
	Button create;
	@Bind(R.id.create_name)
	EditText createName;
	@Bind(R.id.create_button)
	Button createButton;
	@Bind(R.id.create_details)
	LinearLayout createDetails;
	@Bind(R.id.open)
	Button open;
	@Bind(R.id.open_details)
	LinearLayout openDetails;
	@Bind(R.id.quit)
	Button quit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		ButterKnife.bind(this);

		createDetails.setVisibility(View.GONE);
	}

	@OnClick({R.id.create, R.id.create_button, R.id.open, R.id.quit})
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.create:
				createDetails.setVisibility(createDetails.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
				break;
			case R.id.create_button:
				createButton.setEnabled(false);
				createName.setEnabled(false);
				createAndOpenBoard();
				break;
			case R.id.open:
				openDetails.setVisibility(openDetails.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
				break;
			case R.id.quit:
				finish();
				break;
		}
	}

	private void createAndOpenBoard() {
		final String boardName = createName.getText().toString();
		try {
			String ip = NetworkUtils.getIP();
			StateContainer.instance.getBoardRetriever().createBoard(boardName, ip, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					try {
						String response = new String(responseBody);
						JSONObject responseJson = new JSONObject(response);
						JSONObject boardJson = responseJson.getJSONObject("board");
						Board board = new Board(boardJson.getString("name"), responseJson.getString("ipAddress"), boardJson.getString("id"));
						Log.i(TAG, "Created board " + board);
						StateContainer.instance.setBoard(board);
						startActivity(new Intent(getApplicationContext(), BoardActivity.class));
						finish();
					} catch (JSONException e) {
						e.printStackTrace();
						onCreateBoardFailed();
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					Log.e(TAG, "error form server, code: " + statusCode + ", headers: " + Arrays.toString(headers) + ", body: " + new String(responseBody), error);
					onCreateBoardFailed();
				}
			});
		} catch (JSONException | ConnectException e) {
			e.printStackTrace();
			onCreateBoardFailed();
		}
	}

	private void onCreateBoardFailed() {
		Dialog dialog = new Dialog(MenuActivity.this);
		dialog.setTitle("Cannot connect to server. Try again later");
		dialog.show();
		createButton.setEnabled(true);
		createName.setEnabled(true);
	}

}
