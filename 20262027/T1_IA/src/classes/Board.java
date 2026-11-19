package classes;

import java.util.ArrayList;
import java.util.List;

import interfaces.Ilayout;

/**
 * Board 3x3 do 8-puzzle.
 *
 * <p>Cada posicao guarda o numero da peca ({@code 1} a {@code 8}) e o {@code 0}
 * e o espaco vazio. Implementa {@link Ilayout} para poder ser resolvido
 * pelo {@link BestFirst}.
 *
 * @see Ilayout
 * @see BestFirst
 */
public class Board implements Ilayout, Cloneable {

	private static final int dim = 3; // define a dimensao do tabuleiro: 3 linhas x 3 colunas
	private int board[][]; // matriz que guarda a configuracao atual do puzzle

	/** Cria um board vazio, com tudo a {@code 0}. */
	public Board() { // cria a matriz 3x3 que representa o tabuleiro
		board = new int[dim][dim];

	}

	/**
	 * Faz uma copia independente deste board (deep copy da matriz).
	 *
	 * @return novo {@code Board} igual a este, que pode ser alterado sem mexer no original
	 */
	public Board clone() {

		Board copy = new Board();

		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				copy.board[i][j] = board[i][j];
			}
		}

		return copy;

	}

	/**
	 * Cria um board a partir de uma string, linha a linha.
	 *
	 * <p>Por exemplo, {@code new Board("023145678")} da este board:
	 * <pre>
	 *  23
	 * 145
	 * 678
	 * </pre>
	 *
	 * @param str string com 9 digitos, em que o {@code 0} e o espaco vazio
	 * @throws IllegalStateException se {@code str} nao tiver exatamente 9 caracteres
	 */
	public Board(String str) {
		if (str.length() != dim * dim) { // verifica se a configuracao recebida tem exatamente 9 posicoes
			throw new IllegalStateException("Invalid arg in Board constructor");
			// da erro caso a configuracao tenha um tamanho invalido
		}
		board = new int[dim][dim]; // cria a matriz 3x3 que representa o tabuleiro

		int si = 0; // indice usado para percorrer os caracteres da string
		int i, j;

		for (i = 0; i < dim; i++) {
			for (j = 0; j < dim; j++) {
				board[i][j] = Character.getNumericValue(str.charAt(si++));
				/* converte cada caracter da string num numero e coloca-o na posicao 
				correspondente da matriz si++ avanca para o caracter seguinte */
			}
		}
	}

	/**
	 * Converte o board em texto: 3 linhas, com um espaco no lugar do {@code 0}.
	 *
	 * @return o board em formato de texto, com {@code '\n'} no fim de cada linha
	 */
	@Override
	public String toString() {

		String result = ""; // string onde e construida a representacao do tabuleiro
		int i, j;

		for (i = 0; i < dim; i++) {
			for (j = 0; j < dim; j++) {

				if (board[i][j] == 0) { // representa o 0, que corresponde ao espaco vazio do puzzle, por um espaco
					result += ' ';
				} else {
					result += board[i][j]; // adiciona a String o valor da posicao atual
				}
			}

			result += "\n";
		}

		return result; 
	}

	/**
	 * Dois boards sao iguais se tiverem a mesma peca em todas as posicoes.
	 *
	 * @param o objeto a comparar
	 * @return {@code true} se {@code o} for um {@code Board} com o mesmo layout
	 */
	@Override
	public boolean equals(Object o) {

		if (o instanceof Board) { // verifica se o objeto que estamos a comparar e um Board
			Board b = (Board) o; // converte o objeto para Board, permitindo aceder ao seu tabuleiro

			for (int i = 0; i < dim; i++) {
				for (int j = 0; j < dim; j++) {
					if (b.board[i][j] != board[i][j]) { // compara cada posicao dos dois tabuleiros
						return false; // se alguma for diferente, os estados nao sao iguais
					}
				}
			}

			return true; // se todas as posicoes forem iguais, os dois Board representam o mesmo estado

		}

		return false;

	}

	/**
	 * Calcula o hash lendo as 9 posicoes como se fossem um numero
	 * (por exemplo, {@code "023145678"} da {@code 23145678}). Assim cada
	 * layout tem um hash diferente, sem colisoes.
	 *
	 * @return hash do board
	 */
	@Override
	public int hashCode() {

		int hash = 0;

		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				hash = hash * 10 + board[i][j];
			}
		}

		return hash;
	}

	/**
	 * Gera os children movendo o espaco vazio para cima, baixo, esquerda
	 * e direita, sempre que o move for possivel.
	 *
	 * @return lista com 2 a 4 children
	 * @see #clone()
	 */
	@Override
	public List<Ilayout> children() {

		List<Ilayout> result = new ArrayList<>();
		int i, j;

		for (i = 0; i < dim; i++) {
			for (j = 0; j < dim; j++) {
				if (board[i][j] == 0) {
					if (i > 0) {
						Board child = this.clone();

						int temp = child.board[i - 1][j];
						child.board[i - 1][j] = child.board[i][j];
						child.board[i][j] = temp;

						result.add(child);
					}

					if (i < dim - 1) {
						Board child = this.clone();

						int temp = child.board[i + 1][j];
						child.board[i + 1][j] = child.board[i][j];
						child.board[i][j] = temp;

						result.add(child);
					}

					if (j > 0) {
						Board child = this.clone();

						int temp = child.board[i][j - 1];
						child.board[i][j - 1] = child.board[i][j];
						child.board[i][j] = temp;

						result.add(child);
					}

					if (j < dim - 1) {
						Board child = this.clone();

						int temp = child.board[i][j + 1];
						child.board[i][j + 1] = child.board[i][j];
						child.board[i][j] = temp;

						result.add(child);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Verifica se este board e o goal.
	 *
	 * @param l layout final (o goal)
	 * @return {@code true} se {@code l} for um {@code Board} igual a este
	 * @see #equals(Object)
	 */
	@Override
	public boolean isGoal(Ilayout l) {

		if (l instanceof Board b) {
			return this.equals(b);
		}

		return false;
	}

	/**
	 * Devolve o custo de um move.
	 *
	 * @return {@code 1}, porque no 8-puzzle todos os moves custam o mesmo
	 */
	@Override
	public double getK() {
		return 1;
	}
}
