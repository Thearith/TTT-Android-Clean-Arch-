package thearith.com.tictactoe.data.local;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import thearith.com.tictactoe.cross.model.GamePosition;
import thearith.com.tictactoe.cross.model.Player;
import thearith.com.tictactoe.cross.model.WinningState;
import thearith.com.tictactoe.cross.utils.ArrayUtils;
import thearith.com.tictactoe.cross.utils.GameStateUtils;
import thearith.com.tictactoe.data.repository.datasource.GameManager;
import thearith.com.tictactoe.cross.model.GameState;
import thearith.com.tictactoe.cross.model.PlayerType;
import thearith.com.tictactoe.presentation.internal.di.ApplicationScope;

/**
 * Created by Thearith on 7/25/17.
 */

@ApplicationScope
public class LocalGameManager implements GameManager {

    @Inject
    LocalGameManager() {

    }


    /**
     * Initialize Game States
     * */
    @Override
    public Observable<GameState> initializeGame(List<Player> players, int size) {
        List<List<PlayerType>> initialScores = initializeScores(size);
        WinningState initialWinnerState = initializeWinnerState();
        GameState initialGameState = new GameState(initialScores, players, initialWinnerState);
        return Observable.just(initialGameState);
    }

    private List<List<PlayerType>> initializeScores(int size) {
        return ArrayUtils.init2DArray(PlayerType.UNKNOWN, size);
    }

    private WinningState initializeWinnerState() {
        return WinningState.NOT_OVER;
    }



    /**
     * Update Games by:
     *  1. Check TicTacToe's Grid's position
     *  2. Update Winner's status if there is a winner
     *  3. Alternate turns between users
     * */
    @Override
    public Observable<GameState> check(@NonNull GameState gameState, @NonNull GamePosition gamePosition) {
        PlayerType value = gamePosition.getValue();
        int row = gamePosition.getRow();
        int col = gamePosition.getCol();

        return Observable.just(gameState)
                .map(state -> this.markGrid(state, value, row, col))
                .map(this::checkWinner)
                .map(this::changePlayerTurn);
    }


    /**
     * Check TicTacToe's Grid's position
     * */
    private GameState markGrid(final GameState state, final PlayerType value, final int row, final int col) {
        List<List<PlayerType>> scores = ArrayUtils.copy2DArray(state.getScores());
        if(scores == null || scores.size() <= row || scores.get(row).size() <= col) {
            return state;
        }

        scores.get(row).set(col, value);

        return new GameState(scores, state.getPlayers(), state.getWinningState());
    }


    /**
     * Update Winner's status if there is a winner
     * */
    private GameState checkWinner(final GameState state) {
        List<List<PlayerType>> scores = state.getScores();
        WinningState winner = checkGrid(scores);

        return new GameState(scores, state.getPlayers(), winner);
    }

    private WinningState checkGrid(List<List<PlayerType>> scores) {
        if(scores != null) {
            WinningState winnerRow = checkRows(scores);
            if(winnerRow.isWinning()) {
                return winnerRow;
            }

            WinningState winnerColumn = checkColumns(scores);
            if(winnerRow.isWinning()) {
                return winnerColumn;
            }

            WinningState winnerDiagonal = checkDiagonals(scores);
            if(winnerRow.isWinning()) {
                return winnerDiagonal;
            }

            WinningState drawState = checkDrawState(scores);
            if(drawState.isDraw()) {
                return drawState;
            }
        }

        return WinningState.NOT_OVER;
    }

    private WinningState checkRows(@NonNull List<List<PlayerType>> scores) {
        int size = scores.size();

        for(int i=0; i<size; i++) {
            boolean isSameType = true;
            for(int j=0; j<size-1; j++) {
                PlayerType value = scores.get(i).get(j);
                PlayerType adjacentValue = scores.get(i).get(j+1);
                if(!value.equal(adjacentValue)) {
                    isSameType = false;
                    break;
                }
            }

            if(isSameType) {
                return GameStateUtils.getWinner(scores.get(i).get(0));
            }
        }

        return WinningState.NOT_OVER;
    }

    private WinningState checkColumns(@NonNull List<List<PlayerType>> scores) {
        int size = scores.size();

        for(int i=0; i<size; i++) {
            boolean isSameType = true;
            for(int j=0; j<size-1; j++) {
                PlayerType value = scores.get(j).get(i);
                PlayerType adjacentValue = scores.get(j+1).get(i);
                if(!value.equal(adjacentValue)) {
                    isSameType = false;
                    break;
                }
            }

            if(isSameType) {
                return GameStateUtils.getWinner(scores.get(0).get(i));
            }
        }

        return WinningState.NOT_OVER;
    }

    private WinningState checkDiagonals(@NonNull List<List<PlayerType>> scores) {
        WinningState winnerLeft = checkLeftDiagonal(scores);
        if(winnerLeft.isWinning()) {
            return winnerLeft;
        }

        WinningState winnerRight = checkRightDiagonal(scores);
        if(winnerLeft.isWinning()) {
            return winnerRight;
        }

        return WinningState.NOT_OVER;
    }

    private WinningState checkLeftDiagonal(@NonNull List<List<PlayerType>> scores) {
        int size = scores.size();
        boolean isSameType = true;

        for(int i=0; i<size-1; i++) {
            PlayerType value = scores.get(i).get(i);
            PlayerType adjacentValue = scores.get(i+1).get(i+1);
            if(!value.equal(adjacentValue)) {
                isSameType = false;
                break;
            }
        }

        WinningState winner = GameStateUtils.getWinner(scores.get(0).get(0));
        return isSameType ? winner : WinningState.NOT_OVER;
    }

    private WinningState checkRightDiagonal(List<List<PlayerType>> scores) {
        int size = scores.size();
        boolean isSameType = true;

        for(int i=0; i<size-1; i++) {
            PlayerType value = scores.get(i).get(size-1-i);
            PlayerType adjacentValue = scores.get(i+1).get(size-2-i);
            if(!value.equal(adjacentValue)) {
                isSameType = false;
                break;
            }
        }

        WinningState winner = GameStateUtils.getWinner(scores.get(0).get(size-1));
        return isSameType ? winner : WinningState.NOT_OVER;
    }

    private WinningState checkDrawState(List<List<PlayerType>> scores) {
        for(List<PlayerType> rows : scores) {
            for(PlayerType col : rows) {
                if(col.isUnknown()) {
                    return WinningState.NOT_OVER;
                }
            }
        }

        return WinningState.DRAW;
    }


    /**
     *  Alternate turns between users
     * */
    private GameState changePlayerTurn(GameState gameState) {
        List<Player> players = ArrayUtils.copyArray(gameState.getPlayers());
        int newTurn = 0;
        for(int i=0; i<players.size(); i++) {
            Player player = players.get(i);
            if(player.isTurn()) {
                newTurn = (i + 1) % players.size();
                break;
            }
        }

        for(int i=0; i<players.size(); i++) {
            boolean isTurn = i == newTurn;
            players.get(i).setTurn(isTurn);
        }

        return new GameState(gameState.getScores(), players, gameState.getWinningState());
    }

}
