package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes do Tabuleiro com um mapa pequeno de fixture (independente do
 * mapa oficial), para que cada caso teste exatamente uma regra.
 *
 * Mapa de fixture:
 *
 * A ─ B ─ C          A, B: Rússia (simples)   C: Rússia (dupla)
 * │       │          D: Pérsia (simples)      K: Karakorum
 * D ─ K ──┘          Cidade "Sarai" (Rússia) adjacente a B e C
 *
 */
public class TabuleiroTest {

	private Tabuleiro tab;
	private Parada a, b, c, d, k;
	private Jogador azul, verde, vermelho;

	@Before
	public void setUp() {
		tab = new Tabuleiro();
		a = tab.adicionarParada("A", Regiao.RUSSIA, TipoParada.SIMPLES);
		b = tab.adicionarParada("B", Regiao.RUSSIA, TipoParada.SIMPLES);
		c = tab.adicionarParada("C", Regiao.RUSSIA, TipoParada.DUPLA);
		d = tab.adicionarParada("D", Regiao.PERSIA, TipoParada.SIMPLES);
		k = tab.adicionarParada("K", null, TipoParada.KARAKORUM);
		tab.ligar("A", "B");
		tab.ligar("B", "C");
		tab.ligar("A", "D");
		tab.ligar("D", "K");
		tab.ligar("K", "C");
		tab.adicionarCidade("Sarai", Regiao.RUSSIA, "B", "C");

		azul = new Jogador("Ana", Cor.AZUL, Herdeiro.JOCHI);
		verde = new Jogador("Bia", Cor.VERDE, Herdeiro.TOLUI);
		vermelho = new Jogador("Caio", Cor.VERMELHO, Herdeiro.ALTANI);
	}

	// ------------------------------------------------------------ vizinhança

	/**
	 * Objetivo: verificar que uma rota criada com ligar(a, b) é simétrica.
	 * Entrada: rota A–B.
	 * Saída esperada: saoVizinhas(A,B) e saoVizinhas(B,A) são verdadeiros.
	 */
	@Test
	public void rotaEhSimetrica() {
		assertTrue(tab.saoVizinhas(a, b));
		assertTrue(tab.saoVizinhas(b, a));
		assertTrue(tab.saoVizinhas("A", "B"));
	}

	/**
	 * Objetivo: verificar que paradas sem rota direta não são vizinhas, mesmo
	 * que estejam a dois passos.
	 * Entrada: A e C (ligadas apenas via B).
	 * Saída esperada: saoVizinhas(A,C) é falso; vizinhas(A) = {B, D}.
	 */
	@Test
	public void paradasSemRotaDiretaNaoSaoVizinhas() {
		assertFalse(tab.saoVizinhas(a, c));
		Set<Parada> viz = tab.vizinhas(a);
		assertEquals(2, viz.size());
		assertTrue(viz.contains(b));
		assertTrue(viz.contains(d));
	}

	/**
	 * Objetivo: garantir que consultar uma parada inexistente falha de forma
	 * explícita, em vez de devolver null e causar NullPointerException depois.
	 * Entrada: nome "Xanadu".
	 * Saída esperada: IllegalArgumentException.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void paradaInexistenteLancaExcecao() {
		tab.getParada("Xanadu");
	}

	/**
	 * Objetivo: as paradas são agrupadas corretamente por região e Karakorum
	 * não pertence a nenhuma.
	 * Entrada: fixture.
	 * Saída esperada: Rússia = {A,B,C}, Pérsia = {D}, China = {}, K sem região.
	 */
	@Test
	public void paradasAgrupadasPorRegiao() {
		assertEquals(3, tab.paradasDaRegiao(Regiao.RUSSIA).size());
		assertEquals(1, tab.paradasDaRegiao(Regiao.PERSIA).size());
		assertEquals(0, tab.paradasDaRegiao(Regiao.CHINA).size());
		assertNull(k.getRegiao());
		assertEquals(k, tab.getKarakorum());
	}

	// ----------------------------------------------------------------- yurts

	/**
	 * Objetivo: construir um yurt retira do estoque do jogador e passa a
	 * "pertencer" à parada (o equivalente ao dono mudar em War).
	 * Entrada: Azul constrói em A.
	 * Saída esperada: A tem 1 yurt azul, Azul fica com 11 no estoque,
	 * temYurtDe(A, Azul) verdadeiro e temYurtDe(A, Verde) falso.
	 */
	@Test
	public void colocarYurtRetiraDoEstoqueEMarcaParada() {
		assertTrue(tab.colocarYurt(a, azul));
		assertEquals(1, tab.yurtsEm(a));
		assertEquals(Jogador.YURTS_INICIAIS - 1, azul.getYurtsDisponiveis());
		assertTrue(tab.temYurtDe(a, azul));
		assertFalse(tab.temYurtDe(a, verde));
		assertTrue(tab.paradasComYurtDe(azul).contains(a));
	}

	/**
	 * Objetivo: parada simples comporta no máximo 1 yurt (manual, p. 11).
	 * Entrada: Azul constrói em A; depois Verde tenta construir em A.
	 * Saída esperada: segunda construção recusada; A continua com 1 yurt e o
	 * estoque de Verde não muda.
	 */
	@Test
	public void paradaSimplesRecusaSegundoYurt() {
		assertTrue(tab.colocarYurt(a, azul));
		assertFalse(tab.colocarYurt(a, verde));
		assertEquals(1, tab.yurtsEm(a));
		assertEquals(Jogador.YURTS_INICIAIS, verde.getYurtsDisponiveis());
	}

	/**
	 * Objetivo: parada dupla comporta 2 yurts, inclusive do mesmo jogador, e
	 * recusa o terceiro (manual, p. 11).
	 * Entrada: Azul constrói duas vezes em C e tenta uma terceira.
	 * Saída esperada: duas aceitas, terceira recusada; C tem 2 yurts azuis.
	 */
	@Test
	public void paradaDuplaAceitaDoisYurtsDoMesmoJogador() {
		assertTrue(tab.colocarYurt(c, azul));
		assertTrue(tab.colocarYurt(c, azul));
		assertFalse(tab.colocarYurt(c, azul));
		assertEquals(2, c.contarYurts(Cor.AZUL));
	}

	/**
	 * Objetivo: Karakorum não recebe yurts, exceto pela regra especial do
	 * herdeiro Tolui (colocarYurt com ignorarCapacidade).
	 * Entrada: Azul tenta construir em K; depois Tolui usa a exceção.
	 * Saída esperada: construção normal recusada; a exceção coloca o yurt, e
	 * ele não conta para nenhuma região.
	 */
	@Test
	public void karakorumSoAceitaYurtPelaRegraDoTolui() {
		assertFalse(tab.colocarYurt(k, azul));
		assertTrue(k.colocarYurt(Cor.VERDE, true));
		assertEquals(1, tab.yurtsEm(k));
		for (Regiao r : Regiao.values()) {
			assertEquals(0, tab.contarYurts(verde, r));
		}
	}

	/**
	 * Objetivo: remover um yurt devolve a peça ao estoque do jogador e só
	 * remove yurt da cor certa.
	 * Entrada: Azul constrói em A; Verde tenta remover; Azul remove.
	 * Saída esperada: remoção por Verde falha; por Azul funciona e o estoque
	 * volta a 12.
	 */
	@Test
	public void removerYurtDevolveAoEstoque() {
		tab.colocarYurt(a, azul);
		assertFalse(tab.removerYurt(a, verde));
		assertTrue(tab.removerYurt(a, azul));
		assertEquals(0, tab.yurtsEm(a));
		assertEquals(Jogador.YURTS_INICIAIS, azul.getYurtsDisponiveis());
	}

	// ------------------------------------------------------- maioria (influência)

	/**
	 * Objetivo: a maioria de yurts numa região (equivalente a "dominar o
	 * continente") só é obtida com mais yurts que todos os outros; empate
	 * coloca os dois em 1º e ninguém em 2º (manual, p. 15).
	 * Entrada: Azul em A e B; Verde em C (dupla) duas vezes.
	 * Saída esperada: líderes = {Azul, Verde}; segundos = {}; depois que Azul
	 * constrói mais um em C, Azul é o único líder e Verde fica em 2º.
	 */
	@Test
	public void maioriaDeYurtsComEmpateNoPrimeiro() {
		tab.colocarYurt(a, azul);
		tab.colocarYurt(b, azul);
		tab.colocarYurt(c, verde);
		tab.colocarYurt(c, verde); // C é dupla: 2 yurts verdes
		assertEquals(2, tab.contarYurts(azul, Regiao.RUSSIA));
		assertEquals(2, tab.contarYurts(verde, Regiao.RUSSIA));
		Set<Cor> lideres = tab.lideresEmYurts(Regiao.RUSSIA);
		assertEquals(2, lideres.size());
		assertTrue(tab.temMaioriaDeYurts(azul, Regiao.RUSSIA));
		assertTrue(tab.temMaioriaDeYurts(verde, Regiao.RUSSIA));
		assertTrue(tab.segundosEmYurts(Regiao.RUSSIA).isEmpty());

		// Desempate: Verde perde um yurt
		tab.removerYurt(c, verde);
		assertEquals(Set.of(Cor.AZUL), tab.lideresEmYurts(Regiao.RUSSIA));
		assertEquals(Set.of(Cor.VERDE), tab.segundosEmYurts(Regiao.RUSSIA));
		assertFalse(tab.temMaioriaDeYurts(verde, Regiao.RUSSIA));
	}

	/**
	 * Objetivo: o yurt de conquista de uma cidade conta para a maioria da
	 * região da cidade (manual, p. 10).
	 * Entrada: Verde tem 1 yurt em A; Azul conquista Sarai (Rússia) tomando
	 * os 4 tesouros.
	 * Saída esperada: antes, Verde lidera; depois da conquista, Azul tem 1
	 * yurt na Rússia (o da cidade) e empata com Verde.
	 */
	@Test
	public void yurtDeConquistaContaParaRegiao() {
		tab.colocarYurt(a, verde);
		assertEquals(Set.of(Cor.VERDE), tab.lideresEmYurts(Regiao.RUSSIA));

		Cidade sarai = tab.getCidade("Sarai");
		sarai.revelar();
		for (int i = 0; i < Cidade.TESOUROS_AO_REVELAR; i++) {
			assertTrue(sarai.retirarTesouro(Cor.AZUL));
		}
		assertTrue(sarai.isConquistada());
		assertEquals(Cor.AZUL, sarai.getConquistadaPor());
		assertEquals(1, tab.contarYurts(azul, Regiao.RUSSIA));
		assertEquals(Set.of(Cor.AZUL, Cor.VERDE), tab.lideresEmYurts(Regiao.RUSSIA));
	}

	// ----------------------------------------------------------------- peões

	/**
	 * Objetivo: um peão só se move para parada vizinha (um cavalo = um passo).
	 * Entrada: peão azul em A; tenta ir para C (não vizinha) e para B.
	 * Saída esperada: movimento para C recusado e posição continua A;
	 * movimento para B aceito.
	 */
	@Test
	public void peaoSoMoveParaVizinha() {
		Peao p = azul.getPeao(0);
		tab.posicionarPeao(p, a);
		assertFalse(tab.moverPeao(p, c));
		assertEquals(a, tab.posicaoDe(p));
		assertTrue(tab.moverPeao(p, b));
		assertEquals(b, tab.posicaoDe(p));
		assertTrue(tab.peoesEm(b).contains(p));
	}

	/**
	 * Objetivo: não se pode terminar o movimento numa parada simples ocupada
	 * por outro jogador, mas pode numa dupla (2 peões) ou em Karakorum
	 * (todos) — manual, p. 7.
	 * Entrada: peão verde em B, em C e em K; peão azul tenta terminar em cada.
	 * Saída esperada: B recusa; C aceita; K aceita mesmo com 2 ocupantes.
	 */
	@Test
	public void capacidadeDePeoesPorTipoDeParada() {
		Peao pAzul = azul.getPeao(0);
		Peao pVerde = verde.getPeao(0);
		Peao pVermelho = vermelho.getPeao(0);

		tab.posicionarPeao(pVerde, b);
		assertFalse(tab.podeTerminarMovimentoEm(pAzul, b));

		tab.posicionarPeao(pVerde, c);
		assertTrue(tab.podeTerminarMovimentoEm(pAzul, c));
		tab.posicionarPeao(pAzul, c);
		assertFalse(tab.podeTerminarMovimentoEm(pVermelho, c)); // dupla cheia

		tab.posicionarPeao(pVerde, k);
		tab.posicionarPeao(pAzul, k);
		assertTrue(tab.podeTerminarMovimentoEm(pVermelho, k));
	}

	/**
	 * Objetivo: os dois peões de Altani não bloqueiam um ao outro (a regra de
	 * ocupação vale só para peões de OUTRO jogador).
	 * Entrada: peão 1 de Altani (vermelho) em A; peão 2 quer terminar em A.
	 * Saída esperada: permitido.
	 */
	@Test
	public void peoesDoMesmoJogadorNaoSeBloqueiam() {
		assertEquals(2, vermelho.getPeoes().size());
		tab.posicionarPeao(vermelho.getPeao(0), a);
		assertTrue(tab.podeTerminarMovimentoEm(vermelho.getPeao(1), a));
	}

	// --------------------------------------------------------- cidades/províncias

	/**
	 * Objetivo: as consultas de adjacência cidade↔parada e província↔parada
	 * respondem a partir de uma parada.
	 * Entrada: cidade Sarai adjacente a B e C; província "Estepe" (yurt)
	 * adjacente a A e D.
	 * Saída esperada: cidadesAdjacentes(B) = {Sarai}; cidadesAdjacentes(A) = {};
	 * provinciasAdjacentes(D) = {Estepe}.
	 */
	@Test
	public void adjacenciaDeCidadesEProvincias() {
		Provincia estepe = tab.adicionarProvincia("Estepe", Regiao.RUSSIA, TipoTributo.YURT, false, "A", "D");
		assertEquals(Set.of(tab.getCidade("Sarai")), tab.cidadesAdjacentes(b));
		assertTrue(tab.cidadesAdjacentes(a).isEmpty());
		assertEquals(Set.of(estepe), tab.provinciasAdjacentes(d));
		assertTrue(tab.provinciasAdjacentes(c).isEmpty());
	}
}
