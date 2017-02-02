package pl.shareddrawboard;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import pl.shareddrawboard.api.UserEndpoint;
import pl.shareddrawboard.domain.Board;
import pl.shareddrawboard.domain.StateContainer;

/**
 * Created by Arjan on 01.02.2017.
 */
public class BoardListAdapter extends BaseAdapter {

	private final Activity activity;

	public BoardListAdapter(Activity activity) {
		this.activity = activity;
	}

	private final List<Board> boards = new ArrayList<>(StateContainer.instance.getBoardsFromServer());

	@Override
	public int getCount() {
		return boards.size();
	}

	@Override
	public Object getItem(int position) {
		return boards.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.item_board, null);
		ViewHolder viewHolder = new ViewHolder(view);

		viewHolder.boardItemName.setText(boards.get(position).getName());
		viewHolder.boardItemName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Board board = boards.get(position);
					StateContainer.instance.setBoard(board);
					StateContainer.instance.getClientPool().joinUser(new UserEndpoint(new URI(board.getIp()), board.getName()));
					activity.startActivity(new Intent(activity.getApplicationContext(), BoardActivity.class));
					activity.finish();
				} catch (URISyntaxException e) {
					e.printStackTrace();
					Dialog dialog = new Dialog(activity.getApplicationContext());
					dialog.setTitle("failed to connect");
					dialog.show();
				}
			}
		});
		return view;
	}

	static class ViewHolder {
		@Bind(R.id.board_item_name)
		TextView boardItemName;

		ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}
