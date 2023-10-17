import java.util.Arrays;

public class StudentPlayer extends Player{
    public StudentPlayer(int playerIndex, int[] boardSize, int nToConnect) {
        super(playerIndex, boardSize, nToConnect);
    }

    @Override
    public int step(Board board) {
        //System.out.println(Arrays.deepToString(board.getState()));

        return minimax(board, 6, true, Integer.MIN_VALUE, Integer.MAX_VALUE)[1];
        //return 6;
    }

    // minimax
    private int[] minimax(Board board, int depth, boolean isMaximizingPlayer, int alpha, int beta) {
        int[] best = new int[2];

        if(depth == 0 || board.gameEnded()) {
            best[0] = evaluate(board);
            best[1] = -1;

            //System.out.println("best score: " + best[0]);
            //for(int i = 0; i < 6; i++) {
            //    System.out.println((Arrays.toString(board.getState()[i])));
            //}
            //System.out.println("\n\n");

            return best;
        }

        if(isMaximizingPlayer) {
            int maxScore = Integer.MIN_VALUE;
            for(int col : board.getValidSteps()) {
                Board boardCopy = new Board(board);
                boardCopy.step(2, col);
                int[] score = minimax(boardCopy, depth - 1, false, alpha, beta);
                if(score[0] >= maxScore) {
                    maxScore = score[0];
                    best[0] = maxScore;
                    best[1] = col;
                }
                alpha = Math.max(alpha, maxScore);
                if (maxScore >= beta) break;


            }
        } else {
            int minScore = Integer.MAX_VALUE;
            for(int col : board.getValidSteps()) {
                Board boardCopy = new Board(board);
                boardCopy.step(1, col);
                int[] score = minimax(boardCopy, depth - 1, true, alpha, beta);
                if(score[0] <= minScore) {
                    minScore = score[0];
                    best[0] = minScore;
                    best[1] = col;
                }

                beta = Math.min(beta, minScore);
                if (minScore <= alpha) break;
            }
        }

        return best;

    }


    // heuristics
    private int evaluate(Board board) {
        int score = 0;

        // evaluate for win/loss
        if (board.gameEnded()) return evaluateGameEnded(board);

        // evaluate for a given board state


        for(int[] row : board.getState()) {
            score += rowPoints2(row);
        }

        return score;
    }

    // evaluate for win/loss
    private int evaluateGameEnded(Board board) {
        if (board.getWinner() == 1) {
            return Integer.MIN_VALUE;
        } else if (board.getWinner() == 2) {
            return Integer.MAX_VALUE;
        } else {
            return 0;
        }
    }

    // row point calculator
    static private int rowPoints(int[] row) {
        int score = 0;
        int X = 1; // player 1
        int O = 2; // player 2

        int currentStreakX = 0, currentStreakO = 0; // current streaks for each player
        int maxStreakX = 0, maxStreakO = 0; // max streaks for each player
        int isCurrentlyStreaking = -1; // -1 = no one is streaking, 1 = player 1 is streaking, 2 = player 2 is streaking

        for(int tile : row) {
            if(tile == X) {
                if (isCurrentlyStreaking == O) {
                    maxStreakO = Math.max(maxStreakO, currentStreakO);
                    currentStreakO = 0;
                }
                currentStreakX++;
                isCurrentlyStreaking = 1;
            } else if(tile == O) {
                if (isCurrentlyStreaking == X) {
                    maxStreakX = Math.max(maxStreakX, currentStreakX);
                    currentStreakX = 0;
                }
                currentStreakO++;
                isCurrentlyStreaking = 2;
            }
        }

        maxStreakO = Math.max(maxStreakO, currentStreakO);
        maxStreakX = Math.max(maxStreakX, currentStreakX);

        score = (maxStreakO * maxStreakO) - (maxStreakX * maxStreakX);

        return score;
    }

    // second
    static private int rowPoints2(int[] row) {
        int scoreX = 0, scoreO = 0;
        int e = 0, X = 1, O = 2;

        for (int i = 0; i < row.length; i++) {
            if (row[i] == X) {
                if (i == 0) {
                    if (row[i + 1] == X) {
                        scoreX += 1;
                    }
                    scoreX += 1;
                } else if (i == row.length - 1) {
                    if (row[i - 1] == X) {
                        scoreX += 1;
                    }
                    scoreX += 1;
                } else {
                    if (row[i - 1] == X) {
                        scoreX += 1;
                    }
                    if (row[i + 1] == X) {
                        scoreX += 1;
                    }
                    scoreX += 1;
                }
            } else if (row[i] == O) {
                if (i == 0) {
                    if (row[i + 1] == O) {
                        scoreO += 1;
                    }
                    scoreO += 1;
                } else if (i == row.length - 1) {
                    if (row[i - 1] == O) {
                        scoreO += 1;
                    }
                    scoreO += 1;
                } else {
                    if (row[i - 1] == O) {
                        scoreO += 1;
                    }
                    if (row[i + 1] == O) {
                        scoreO += 1;
                    }
                    scoreO += 1;
                }
            }
        }
        return scoreO - scoreX;
    }

    // evaluate v3
    private int evaluate3(Board board) {
        int score = 0;

        return score;
    }
}
