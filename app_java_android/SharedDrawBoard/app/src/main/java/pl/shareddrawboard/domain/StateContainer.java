package pl.shareddrawboard.domain;

import java.net.ConnectException;

import pl.shareddrawboard.api.Connector;

/**
 * Created by Arjan on 08.01.2017.
 *
 * Singleton do trzymania obiektów istniejących przez cały lifetime appki
 * Bez tego przy obracaniu telefonu, minimalizowaniu appki itd. będę tracił aktualnego boarda
 *
 */

public class StateContainer {
	private Board board;
	private Connector connector;

	public Board getBoard() {
		return board;
	}

	public Connector getConnector() {
		return connector;
	}

	public static StateContainer instance = new StateContainer();

	private StateContainer() {
		board = new Board(200, 150);
		try {
			connector = new Connector(/*TODO name ustalany przy logowaniu*/ null, board);
			connector.startListeners();
		} catch (ConnectException e) {
			//TODO jebnij w apce oknem z komunikatem ze nie ma neta
			e.printStackTrace();
		}
	}
}
