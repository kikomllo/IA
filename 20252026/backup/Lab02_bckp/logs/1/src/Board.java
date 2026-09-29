import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board implements Ilayout, Cloneable {
    private static final int DIM=3;
    private int board[][];
    
    public Board(){ board = new int[DIM][DIM];}
    
    public Board(String str) throws IllegalStateException {
        if (str.length() != DIM*DIM) throw new
            IllegalStateException("Invalid arg in Board constructor");
        
        board = new int[DIM][DIM];
        int si=0;

        for(int i=0; i<DIM; i++)
            for(int j=0; j<DIM; j++)
                board[i][j] = Character.getNumericValue(str.charAt(si++));
    }

    private Board(int[][] other) {
        board = new int[DIM][DIM];
        for (int i = 0; i < DIM; i++) {
            board[i] = Arrays.copyOf(other[i], DIM);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for(int i=0; i<DIM; i++){
            for(int j=0; j<DIM; j++){
                str.append((board[i][j] == 0) ? " " : board[i][j]);
            }
            str.append("\n");
        }

        return str.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Board)) return false;

        Board other = (Board) o;
        for (int i = 0; i < DIM; i++) {
            if (!Arrays.equals(this.board[i], other.board[i])) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public List<Ilayout> children(){
        List<Ilayout> list = new ArrayList<>();
        int pos_x = -1, pos_y = -1;

        for (pos_x = 0 ; pos_x < 3; pos_x++){
            for (pos_y = 0 ; pos_y < 3; pos_y++){
                if (board[pos_x][pos_y] == 0 || pos_y == 2) break;
            }
            if (board[pos_x][pos_y] == 0 || pos_x == 2) break;
        }

        int[][] moves = {{-1,0},{1,0},{0,-1},{0,1}};
        
        for (int[] move : moves) {
            int newpos_x = pos_x + move[0], ny = pos_y + move[1];
            if (newpos_x >= 0 && newpos_x < DIM && ny >= 0 && ny < DIM) {
                int[][] newBoard = copyBoard();
                newBoard[pos_x][pos_y] = newBoard[newpos_x][ny];
                newBoard[newpos_x][ny] = 0;
                list.add(new Board(newBoard));
            }
        }

        return list;
    }

    private int[][] copyBoard() {
        int[][] copy = new int[DIM][DIM];
        for (int i = 0; i < DIM; i++) {
            copy[i] = Arrays.copyOf(board[i], DIM);
        }
        return copy;
    }

    @Override
    public boolean isGoal(Ilayout l){
        return (this.toString().equals(l.toString()));
    }
    
    @Override
    public double getK(){
        return 1.0f;
    } 
}