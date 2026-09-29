import java.util.ArrayList;
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

    @Override
    public String toString() {
        String str = "";

        for(int i=0; i<DIM; i++){
            for(int j=0; j<DIM; j++){
                if (board[i][j] == 0) str += " ";
                else str += board[i][j];
            }
            str += "\n";
        }

        return str;
    }

    @Override
    public boolean equals(Object o) { return o.toString().equals(toString());}

    private String toStringInput(){
        String str = "";

        for(int i=0; i<DIM; i++){
            for(int j=0; j<DIM; j++){
                str += board[i][j];
            }
        }

        return str;
    }

    private String swapNumbers(String board, int number){
        StringBuilder new_board = new StringBuilder();

        for (int i = 0; i < 9; i++)
            if ((Character.toString(board.charAt(i))).equals(Integer.toString(number))) 
                new_board.append(0);

            else if ((Character.toString(board.charAt(i))).equals(Integer.toString(0))) 
                new_board.append(number);

            else new_board.append(board.charAt(i));

        return new_board.toString();
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

        int sub = 1;
        for (int i = 0; i < 4; i++){
            sub*=-1;
            if (i < 2) {
                if (pos_x + sub < 0 || pos_x + sub > 2) continue;
                list.add(new Board(swapNumbers(this.toStringInput(), board[pos_x+sub][pos_y])));
            } else {
                if (pos_y + sub < 0 || pos_y + sub > 2) continue;
                list.add(new Board(swapNumbers(this.toStringInput(), board[pos_x][pos_y+sub])));
            }
        }

        return list;
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