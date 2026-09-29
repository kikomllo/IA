package interfaces;

import java.util.List;

/**
 * Interface com as operacoes que um state tem de ter para ser explorado
 * por um algoritmo de search.
 *
 * <p>E a parte "strategy" do design pattern <i>Strategy</i>: o algoritmo so
 * conhece esta interface, por isso para resolver outro problema basta criar
 * uma classe nova que a implemente (como o {@link classes.Board} faz para o 8-puzzle).
 *
 * @see classes.BestFirst
 * @see classes.Board
 */
public interface Ilayout {

	/**
	 * Gera todos os children deste layout, a partir deste layout com um unico move.
	 *
	 * @return lista de layouts.
	 */
	List<Ilayout> children();

	/**
	 * Verifica se este layout e o goal.
	 *
	 * @param l layout goal
	 * @return {@code true} se o recetor for igual a {@code l}, {@code false} caso contrario
	 */
	boolean isGoal(Ilayout l);

	/**
	 * Devolve o custo de ir do father para este layout.
	 *
	 * @return custo de um move
	 */
	double getK();

}
