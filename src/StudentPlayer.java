import java.util.Arrays;
import java.util.Random;

public class StudentPlayer extends Player{
    private final int HUMAN_PLAYER = 1;
    private final int AI_PLAYER = 2;

    public StudentPlayer(int playerIndex, int[] boardSize, int nToConnect) {
        super(playerIndex, boardSize, nToConnect);
    }

    @Override
    public int step(Board board) {
        //return minimax(board, 3, true, Integer.MIN_VALUE, Integer.MAX_VALUE)[1];
        //return getBestMove(board, true);
        return minimax2(board, 4, true, Integer.MIN_VALUE, Integer.MAX_VALUE)[1];
    }

    // minimax
    private int[] minimax(Board board, int depth, boolean isMaximizingPlayer, int alpha, int beta) {
        int[] best = new int[2];

        if(depth == 0 || board.gameEnded()) {
            best[0] = evaluate3(board);
            best[1] = board.getValidSteps().get(0);

            return best;
        }

        if(isMaximizingPlayer) {
            int maxScore = Integer.MIN_VALUE;
            for(int col : board.getValidSteps()) {
                Board boardCopy = new Board(board);
                boardCopy.step(AI_PLAYER, col);
                int[] score = minimax(boardCopy, depth - 1, false, alpha, beta);
                if(score[0] > maxScore) {
                    maxScore = score[0];
                    best[0] = maxScore;
                    best[1] = col;
                }
                //alpha = Math.max(alpha, maxScore);
                //if (maxScore >= beta) break;
            }
        } else {
            int minScore = Integer.MAX_VALUE;
            for(int col : board.getValidSteps()) {
                Board boardCopy = new Board(board);
                boardCopy.step(HUMAN_PLAYER, col);
                int[] score = minimax(boardCopy, depth - 1, true, alpha, beta);
                if(score[0] < minScore) {
                    minScore = score[0];
                    best[0] = minScore;
                    best[1] = col;
                }

                //beta = Math.min(beta, minScore);
                //if (minScore <= alpha) break;
            }
        }

        return best;
    }

    private int[] minimax2(Board board, int depth, boolean isMaximizingPlayer, int alpha, int beta) {
        if (depth == 0 || board.gameEnded()) return new int[] {evaluate3(board), getBestMove(board, isMaximizingPlayer)};

        if (isMaximizingPlayer) {
            int maxScore = Integer.MIN_VALUE;
            int bestMaxMove = board.getValidSteps().get(0); // get the first valid move

            for(int col : board.getValidSteps()) {
                Board boardCopy = new Board(board);
                boardCopy.step(AI_PLAYER, col);
                int score = minimax2(boardCopy, depth - 1, false, alpha, beta)[0];
                if (score >= maxScore) {
                    maxScore = score;
                    bestMaxMove = col;
                }
            }
            return new int[] {maxScore, bestMaxMove};
        } else {
            int minScore = Integer.MAX_VALUE;
            int bestMinMove = board.getValidSteps().get(0); // get the first valid move
            for(int col : board.getValidSteps()) {
                Board boardCopy = new Board(board);
                boardCopy.step(HUMAN_PLAYER, col);
                int score = minimax2(boardCopy, depth - 1, true, alpha, beta)[0];
                if(score <= minScore) {
                    minScore = score;
                    bestMinMove = col;
                }
            }
            return new int[] {minScore, bestMinMove};
        }
    }

    private int getBestMove(Board board, boolean isMaximizingPlayer) {
        int bestMove = -1;

        if (isMaximizingPlayer) {
            int maxScore = Integer.MIN_VALUE;
            int currScore;
            for(int move : board.getValidSteps()) {
                Board boardCopy = new Board(board);
                boardCopy.step(AI_PLAYER, move);
                currScore = evaluate3(boardCopy);
                if(currScore > maxScore) {
                    maxScore = currScore;
                    bestMove = move;
                }
            }
        } else {
            int minScore = Integer.MAX_VALUE;
            int currScore;
            for(int move : board.getValidSteps()) {
                Board boardCopy = new Board(board);
                boardCopy.step(HUMAN_PLAYER, move);
                currScore = evaluate3(boardCopy);
                if(currScore < minScore) {
                    minScore = currScore;
                    bestMove = move;
                }
            }
        }

        return bestMove;
    }

    // evaluate v3
    private int evaluate3(Board board) {
        int score = 0;
        int windowSize = 4;

        // evaluate for win/loss
        if(board.gameEnded()) {
            if (board.getWinner() == HUMAN_PLAYER) return -1_000_000;
            else if (board.getWinner() == AI_PLAYER) return 1_000_000;
            return 0;
        }


        // horizontal
        for(int[] row : board.getState()) {
            for(int i = 0; i < row.length - windowSize; i++) {
                int[] window = Arrays.copyOfRange(row, i, i + windowSize);

                score += windowScore(window, 100, 20, 3);
            }
        }

        // vertical
        for(int coll_ix = 0; coll_ix < 7; coll_ix++) {
            // get column array
            int[] column = new int[6];
            for(int row_ix = 0; row_ix < 6; row_ix++) {
                column[row_ix] = board.getState()[row_ix][coll_ix];
            }

            // assign points
            for(int i = 0; i < column.length - windowSize; i++) {
                int[] window = Arrays.copyOfRange(column, i, i + windowSize);

                score += windowScore(window, 100, 20, 3);
            }
        }

        // diagonal positive slope
        for(int row_ix = 0; row_ix < 3; row_ix++) {
            for(int coll_ix = 0; coll_ix < 4; coll_ix++) {
                int[] window = new int[4];
                for(int i = 0; i < 4; i++) {
                    window[i] = board.getState()[row_ix + i][coll_ix + i];
                }

                score += windowScore(window, 100, 20, 3);
            }
        }

        // diagonal negative slope
        for(int row_ix = 0; row_ix < 3; row_ix++) {
            for(int coll_ix = 3; coll_ix < 7; coll_ix++) {
                int[] window = new int[4];
                for(int i = 0; i < 4; i++) {
                    window[i] = board.getState()[row_ix + i][coll_ix - i];
                }
                score += windowScore(window, 100, 20, 3);
            }
        }

        // center column bonus
        int[] centerArray = new int[6];
        for(int row_ix = 0; row_ix < 6; row_ix++) {
            centerArray[row_ix] = board.getState()[row_ix][3];
        }
        score += centerBonus(centerArray, AI_PLAYER, 3);
        score += centerBonus(centerArray, HUMAN_PLAYER, -3);

        return score;
    }

    private int centerBonus(int[] centerColumn, int player, int bonusPerPiece) {
        return (int)Arrays.stream(centerColumn).filter(num -> num == player).count() * bonusPerPiece;
    }

    private int windowScore(int[] window, int pointForThreatOfThree, int pointForThreatOfTwo, int pointForThreatOfOne) {
        int score = 0;

        //if(Arrays.stream(window).filter(num -> num == AI_PLAYER).count() == 4) return 100_000;
        //if(Arrays.stream(window).filter(num -> num == HUMAN_PLAYER).count() == 4) return -100_000;

        if(Arrays.stream(window).filter(num -> num == AI_PLAYER).count() == 3 && Arrays.stream(window).filter(num -> num == 0).count() == 1) score += pointForThreatOfThree;
        else if (Arrays.stream(window).filter(num -> num == AI_PLAYER).count() == 2 && Arrays.stream(window).filter(num -> num == 0).count() == 2) score += pointForThreatOfTwo;
        else if (Arrays.stream(window).filter(num -> num == AI_PLAYER).count() == 1 && Arrays.stream(window).filter(num -> num == 0).count() == 3) score += pointForThreatOfOne;

        if(Arrays.stream(window).filter(num -> num == HUMAN_PLAYER).count() == 3 && Arrays.stream(window).filter(num -> num == 0).count() == 1) score -= pointForThreatOfThree;
        else if (Arrays.stream(window).filter(num -> num == HUMAN_PLAYER).count() == 2 && Arrays.stream(window).filter(num -> num == 0).count() == 2) score -= pointForThreatOfTwo;
        else if (Arrays.stream(window).filter(num -> num == HUMAN_PLAYER).count() == 1 && Arrays.stream(window).filter(num -> num == 0).count() == 3) score -= pointForThreatOfOne;

        return score;
    }
}
