import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import classes.BestFirst;
import classes.Board;
import interfaces.Ilayout;

/**
 * Unit tests do {@link Board} e do {@link BestFirst}.
 *
 * <p>Os test cases usam os exemplos do enunciado do Lab Tutorial 1.
 *
 * @see Board
 * @see BestFirst
 */
class PuzzleUnitTests {

	@Test
	//verifica se o Board e criado corretamente e se o toString() produz a representacao esperada
	public void testConstructor() {
		 Board b = new Board("023145678");
		 StringWriter writer = new StringWriter();
		 PrintWriter pw = new PrintWriter ( writer ) ;
		 pw.println(" 23");
		 pw.println("145");
		 pw.println("678");
		 assertEquals(b.toString(), writer.toString());
		 pw.close();
	}
	
	@Test
	//testa outra configuracao do tabuleiro
	public void testConstructor2() {
		Board b = new Board("123485670");
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter (writer) ;
		pw.println("123");
		pw.println("485");
		pw.println("67 ");
		assertEquals(b.toString(), writer.toString());
		pw.close();
	}
	
	@Test
	//verifica se dois Board com a mesma configuracao sao considerados iguais
	public void testEquals() {
	    Board b1 = new Board("023145678");
	    Board b2 = new Board("023145678");

	    assertEquals(b1, b2);
	}
	
	@Test
	//verifica se dois Board com configuracoes diferentes sao considerados diferentes
	public void testEqualsDifferent() {
	    Board b1 = new Board("023145678");
	    Board b2 = new Board("123045678");

	    assertFalse(b1.equals(b2));
	}
	
	@Test
	//verifica se dois Board iguais tem o mesmo hashCode
	public void testHashCode() {
	    Board b1 = new Board("023145678");
	    Board b2 = new Board("023145678");

	    assertEquals(b1.hashCode(), b2.hashCode());
	}
	
	@Test
	//verifica se o clone de um Board e igual ao original
	public void testClone() {
	    Board b1 = new Board("123405678");
	    Board b2 = b1.clone();

	    assertEquals(b1, b2);
	}
	
	@Test
	//verifica se o espaco vazio no centro gera 4 filhos
	public void testChildrenUp() {
	    Board b = new Board("123405678");

	    List<Ilayout> children = b.children();

	    assertEquals(4, children.size());
	}
	
	@Test
	//exemplo 1 do enunciado: o caminho tem 3 estados e custo 2
	public void testSolveSample1() {
	    BestFirst s = new BestFirst();
	    Iterator<BestFirst.State> it = s.solve(new Board("023145678"), new Board("123405678"));

	    List<BestFirst.State> path = new ArrayList<>();
	    while (it.hasNext()) {
	        path.add(it.next());
	    }

	    assertEquals(3, path.size());
	    assertEquals(2, (int) path.get(path.size() - 1).getG());
	}
	
	@Test
	//exemplo 1: verifica cada configuracao do caminho, pela ordem certa
	public void testSolveSample1Path() {
	    BestFirst s = new BestFirst();
	    Iterator<BestFirst.State> it = s.solve(new Board("023145678"), new Board("123405678"));

	    assertEquals(new Board("023145678").toString(), it.next().toString());
	    assertEquals(new Board("123045678").toString(), it.next().toString());
	    assertEquals(new Board("123405678").toString(), it.next().toString());
	    assertFalse(it.hasNext());
	}
	
	@Test
	//exemplo 2 do enunciado: o caminho tem 13 estados e custo 12
	public void testSolveSample2() {
	    BestFirst s = new BestFirst();
	    Iterator<BestFirst.State> it = s.solve(new Board("123456780"), new Board("436718520"));

	    List<BestFirst.State> path = new ArrayList<>();
	    while (it.hasNext()) {
	        path.add(it.next());
	    }

	    assertEquals(13, path.size());
	    assertEquals(12, (int) path.get(path.size() - 1).getG());
	}
	
	@Test
	//exemplo 2: verifica cada configuracao do caminho, pela ordem certa
	public void testSolveSample2Path() {
	    BestFirst s = new BestFirst();
	    Iterator<BestFirst.State> it = s.solve(new Board("123456780"), new Board("436718520"));
	    String[] expected = { "123456780", "123456708", "123406758", "103426758", "013426758",
	            "413026758", "413726058", "413726508", "413706528", "403716528", "430716528",
	            "436710528", "436718520" };

	    for (int i = 0; i < expected.length; i++) {
	        assertEquals(new Board(expected[i]).toString(), it.next().toString());
	    }
	    assertFalse(it.hasNext());
	}
	
	@Test
	//verifica se o custo comeca em 0 e aumenta 1 em cada jogada
	public void testSolveCost() {
	    BestFirst s = new BestFirst();
	    Iterator<BestFirst.State> it = s.solve(new Board("123456780"), new Board("436718520"));

	    int i = 0;
	    while (it.hasNext()) {
	        assertEquals(i, it.next().getG(), 0.0001);
	        i++;
	    }
	}
	
	@Test
	//se a configuracao inicial ja for o objetivo, o caminho so tem esse estado
	public void testSolveInitialIsGoal() {
	    BestFirst s = new BestFirst();
	    Iterator<BestFirst.State> it = s.solve(new Board("123405678"), new Board("123405678"));

	    BestFirst.State first = it.next();

	    assertEquals(new Board("123405678").toString(), first.toString());
	    assertEquals(0, (int) first.getG());
	    assertFalse(it.hasNext());
	}
	
	@Test
	//verifica se a saida impressa como no Main e igual a do exemplo 1
	public void testSolveOutput() {
	    BestFirst s = new BestFirst();
	    Iterator<BestFirst.State> it = s.solve(new Board("023145678"), new Board("123405678"));
	    StringWriter writer = new StringWriter();
	    PrintWriter pw = new PrintWriter(writer);
		
	    pw.println(" 23");
	    pw.println("145");
	    pw.println("678");
	    pw.println();
	    pw.println("123");
	    pw.println(" 45");
	    pw.println("678");
	    pw.println();
	    pw.println("123");
	    pw.println("4 5");
	    pw.println("678");
	    pw.println();
	    pw.println("2");

	    StringWriter output = new StringWriter();
	    PrintWriter out = new PrintWriter(output);
	    while (it.hasNext()) {
	        BestFirst.State i = it.next();
	        out.println(i);
	        if (!it.hasNext()) out.println((int) i.getG());
	    }

	    assertEquals(writer.toString(), output.toString());
	    pw.close();
	    out.close();
	}
	
	@Test
	//verifica se o mesmo BestFirst pode resolver dois problemas seguidos
	public void testSolveTwice() {
	    BestFirst s = new BestFirst();
	    s.solve(new Board("023145678"), new Board("123405678"));
	    Iterator<BestFirst.State> it = s.solve(new Board("123456780"), new Board("436718520"));

	    int count = 0;
	    while (it.hasNext()) {
	        it.next();
	        count++;
	    }

	    assertEquals(13, count);
	}
	
	@Test
	//verifica se o exemplo 2 e resolvido em menos de 1 segundo
	public void testEficientSolve() {
	    assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
	        BestFirst s = new BestFirst();
	        Iterator<BestFirst.State> it = s.solve(new Board("123456780"), new Board("436718520"));
	        assertNotNull(it);
	    });
	}
	
	@Test
	//verifica se o construtor rejeita configuracoes com tamanho errado
	public void testConstructorInvalidLength() {
	    assertThrows(IllegalStateException.class, () -> {
	        new Board("12345");
	    });
	}

}
