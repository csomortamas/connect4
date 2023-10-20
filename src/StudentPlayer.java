import java.util.*;
import java.util.logging.Logger;

public class StudentPlayer extends Player{
    private final int HUMAN_PLAYER = 1;
    private final int AI_PLAYER = 2;

    public StudentPlayer(int playerIndex, int[] boardSize, int nToConnect) {
        super(playerIndex, boardSize, nToConnect);
    }

    @Override
    public int step(Board board) {
        return minimax(board, 7, true, Integer.MIN_VALUE, Integer.MAX_VALUE)[1];
    }

    Comparator<Integer> closeComparator = new Comparator<Integer>() {
        @Override
        public int compare(Integer i1, Integer i2) {
            int i1Dist = Math.abs(i1 - 3);
            int i2Dist = Math.abs(i2 - 3);
            return Integer.compare(i1Dist, i2Dist);
        }
    };

    private int[] minimax(Board board, int depth, boolean isMaximizingPlayer, int alpha, int beta) {
        if (depth == 0 || board.gameEnded()) return new int[] {evaluate(board), -1};

        ArrayList<Integer> validSteps = board.getValidSteps();
        validSteps.sort(closeComparator);

        if (isMaximizingPlayer) {
            int maxScore = Integer.MIN_VALUE;
            int bestMaxMove = board.getValidSteps().get(0);

            for(int col : validSteps) {
                Board boardCopy = new Board(board);
                boardCopy.step(AI_PLAYER, col);
                int score = minimax(boardCopy, depth - 1, false, alpha, beta)[0];

                if (score >= maxScore) {
                    maxScore = score;
                    bestMaxMove = col;
                }
                alpha = Math.max(alpha, maxScore);
                if (alpha > beta) break;
            }
            return new int[] {maxScore, bestMaxMove};
        } else {
            int minScore = Integer.MAX_VALUE;
            int bestMinMove = board.getValidSteps().get(0);
            for(int col : validSteps) {
                Board boardCopy = new Board(board);
                boardCopy.step(HUMAN_PLAYER, col);
                int score = minimax(boardCopy, depth - 1, true, alpha, beta)[0];

                if(score <= minScore) {
                    minScore = score;
                    bestMinMove = col;
                }
                beta = Math.min(beta, minScore);
                if (alpha > beta) break;
            }
            return new int[] {minScore, bestMinMove};
        }
    }

    private int evaluate(Board board) {
        int score = 0;
        int windowSize = 4;
        int pieces = 0;

        int POINT_FOR_THREAT_OF_THREE = 100;
        int POINT_FOR_THREAT_OF_TWO = 20;
        int POINT_FOR_THREAT_OF_ONE = 3;

        int[][] state = board.getState();

        for (int[] row : state) {
            pieces += Arrays.stream(row).filter(num -> num != 0).count();
        }

        if(board.gameEnded()) {
            if (board.getWinner() == HUMAN_PLAYER) return -1_000_000 + pieces;
            else if (board.getWinner() == AI_PLAYER) return 1_000_000 - pieces;
            return 0;
        }

        for(int[] row : state) {
            for(int i = 0; i < row.length - windowSize; i++) {
                int[] window = Arrays.copyOfRange(row, i, i + windowSize);

                score += windowScore(window, POINT_FOR_THREAT_OF_THREE, POINT_FOR_THREAT_OF_TWO, POINT_FOR_THREAT_OF_ONE);
            }
        }

        // vertical
        for(int coll_ix = 0; coll_ix < 7; coll_ix++) {
            // get column array
            int[] column = new int[6];
            for(int row_ix = 0; row_ix < 6; row_ix++) {
                column[row_ix] = state[row_ix][coll_ix];
            }

            // assign points
            for(int i = 0; i < column.length - windowSize; i++) {
                int[] window = Arrays.copyOfRange(column, i, i + windowSize);

                score += windowScore(window, POINT_FOR_THREAT_OF_THREE, POINT_FOR_THREAT_OF_TWO, POINT_FOR_THREAT_OF_ONE);
            }
        }

        // diagonal positive slope
        for(int row_ix = 0; row_ix < 3; row_ix++) {
            for(int coll_ix = 0; coll_ix < 4; coll_ix++) {
                int[] window = new int[4];
                for(int i = 0; i < 4; i++) {
                    window[i] = state[row_ix + i][coll_ix + i];
                }

                score += windowScore(window, POINT_FOR_THREAT_OF_THREE, POINT_FOR_THREAT_OF_TWO, POINT_FOR_THREAT_OF_ONE);
            }
        }

        // diagonal negative slope
        for(int row_ix = 0; row_ix < 3; row_ix++) {
            for(int coll_ix = 3; coll_ix < 7; coll_ix++) {
                int[] window = new int[4];
                for(int i = 0; i < 4; i++) {
                    window[i] = state[row_ix + i][coll_ix - i];
                }
                score += windowScore(window, POINT_FOR_THREAT_OF_THREE, POINT_FOR_THREAT_OF_TWO, POINT_FOR_THREAT_OF_ONE);
            }
        }
        score += centerBonusVariant(board, 0, AI_PLAYER, 5);
        score += centerBonusVariant(board, 0, HUMAN_PLAYER, -5);

        score += centerBonusVariant(board, 1, AI_PLAYER, 4);
        score += centerBonusVariant(board, 1, HUMAN_PLAYER, -4);

        score += centerBonusVariant(board, 2, AI_PLAYER, 2);
        score += centerBonusVariant(board, 2, HUMAN_PLAYER, -2);

        return score;
    }

    private int centerBonusVariant(Board board, int centerOffset, int player, int bonusPerPiece) {
        if(centerOffset == 0) {
            int[] column = new int[6];
            for(int row_ix = 0; row_ix < 6; row_ix++) {
                column[row_ix] = board.getState()[row_ix][3];
            }
            return (int)Arrays.stream(column).filter(num -> num == player).count() * bonusPerPiece;
        } else {
            int sum = 0;
            int[] centerLeft = new int[6];
            int[] centerRight = new int[6];
            for(int row_ix = 0; row_ix < 6; row_ix++) {
                centerLeft[row_ix] = board.getState()[row_ix][3 - centerOffset];
            }
            for(int row_ix = 0; row_ix < 6; row_ix++) {
                centerRight[row_ix] = board.getState()[row_ix][3 + centerOffset];
            }
            sum += (int)Arrays.stream(centerLeft).filter(num -> num == player).count() * bonusPerPiece;
            sum += (int)Arrays.stream(centerRight).filter(num -> num == player).count() * bonusPerPiece;

            return sum;
        }
    }

    private int windowScore(int[] window, int pointForThreatOfThree, int pointForThreatOfTwo, int pointForThreatOfOne) {
        int score = 0;

        if(Arrays.stream(window).filter(num -> num == AI_PLAYER).count() == 3 && Arrays.stream(window).filter(num -> num == 0).count() == 1) score += pointForThreatOfThree;
        else if (Arrays.stream(window).filter(num -> num == AI_PLAYER).count() == 2 && Arrays.stream(window).filter(num -> num == 0).count() == 2) score += pointForThreatOfTwo;
        else if (Arrays.stream(window).filter(num -> num == AI_PLAYER).count() == 1 && Arrays.stream(window).filter(num -> num == 0).count() == 3) score += pointForThreatOfOne;

        if(Arrays.stream(window).filter(num -> num == HUMAN_PLAYER).count() == 3 && Arrays.stream(window).filter(num -> num == 0).count() == 1) score -= pointForThreatOfThree;
        else if (Arrays.stream(window).filter(num -> num == HUMAN_PLAYER).count() == 2 && Arrays.stream(window).filter(num -> num == 0).count() == 2) score -= pointForThreatOfTwo;
        else if (Arrays.stream(window).filter(num -> num == HUMAN_PLAYER).count() == 1 && Arrays.stream(window).filter(num -> num == 0).count() == 3) score -= pointForThreatOfOne;

        return score;
    }
}