package Enunciado;

import java.util.ArrayList;
import java.util.List;

public class Board implements Ilayout, Cloneable {
    private static final int dim=3;
    private int board[][];
    
    public Board(){ board = new int[dim][dim];}
    
    public Board(String str) throws IllegalStateException {
        if (str.length() != dim*dim) throw new
            IllegalStateException("Invalid arg in Board constructor");
        
        board = new int[dim][dim];
        int si=0;

        for(int i=0; i<dim; i++)
            for(int j=0; j<dim; j++)
                board[i][j] = Character.getNumericValue(str.charAt(si++));
    }

    public String toString() {
        String str = "";

        for(int i=0; i<dim; i++){
            for(int j=0; j<dim; j++){
                if (board[i][j] == 0) str += " ";
                else str += board[i][j];
            }
            str += "\n";
        }

        return str;
    }

    public boolean equals(Object o) { return o.toString().equals(toString());}

    private String toStringInput(){
        String str = "";

        for(int i=0; i<dim; i++){
            for(int j=0; j<dim; j++){
                str += board[i][j];
            }
        }

        return str;
    }

    private String swapNumbers(String board, int number){
        String new_board = "";

        for (int i = 0; i < 9; i++)
            if (("" + board.charAt(i)).equals("" + number)) new_board += 0;
            else if (("" + board.charAt(i)).equals("" + 0)) new_board += number;
            else new_board += board.charAt(i);

        return new_board;
    }

    @Override
    public List<Ilayout> children(){
        List<Ilayout> list = new ArrayList<>();
        int pos_x = 0, pos_y = 0;

        for (pos_x = 0 ; pos_x < 3; pos_x++){
            for (pos_y = 0 ; pos_y < 3; pos_y++){
                if (board[pos_x][pos_y] == 0) break;
                if (pos_y == 2) break;
            }
            if (board[pos_x][pos_y] == 0) break;
            if (pos_x == 2) break;
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

    //TODO ...TO BE COMPLETED
}