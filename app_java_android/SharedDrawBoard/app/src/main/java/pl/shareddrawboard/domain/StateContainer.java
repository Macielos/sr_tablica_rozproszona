package pl.shareddrawboard.domain;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.shareddrawboard.api.DrawboardClientPool;
import pl.shareddrawboard.api.DrawboardServer;
import pl.shareddrawboard.api.BoardServerConnector;

/**
 * Created by Arjan on 08.01.2017.
 *
 * Singleton do trzymania obiektów istniejących przez cały lifetime appki
 * Bez tego przy obracaniu telefonu, minimalizowaniu appki itd. będę tracił aktualnego boarda
 *
 */

public class StateContainer {
	private Board board;
	private DrawboardServer server;
	private DrawboardClientPool clientPool;
	private BoardServerConnector boardServerConnector;
	private Set<Board> boardsFromServer = new HashSet<>();

	public Board getBoard() {
		return board;
	}

	public DrawboardServer getServer() {
		return server;
	}

	public DrawboardClientPool getClientPool() {
		return clientPool;
	}

	public BoardServerConnector getBoardServerConnector() {
		return boardServerConnector;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public Set<Board> getBoardsFromServer() {
		return boardsFromServer;
	}

	public void setBoardsFromServer(Set<Board> boardsFromServer) {
		this.boardsFromServer = boardsFromServer;
	}

	public void setServer(DrawboardServer server) {
		this.server = server;
	}

	public static StateContainer instance = new StateContainer();

	private StateContainer() {
		//board = new Board(200, 150);
		try {
			server = new DrawboardServer(board);
			server.start();
		} catch (ConnectException e) {
			//TODO jebnij w apce oknem z komunikatem ze nie ma neta
			e.printStackTrace();
		}

		clientPool = new DrawboardClientPool(/*TODO name ustalany przy logowaniu*/null);

		boardServerConnector = new BoardServerConnector();
	}

}
