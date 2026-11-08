package model;

import static org.junit.Assert.*;
import java.util.Map;
import org.junit.Test;

public class ObjetivosTest {
	/** Objetivo: usar tributos virtuais apenas da vez. Entrada: três espadas físicas e uma virtual. Saída esperada: objetivo sem gasto. */
	@Test public void espadasVirtuaisContamSomenteNoTurnoDoDono() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		c.azul().adicionarTributo(TipoTributo.ESPADA, 3);
		Inventario i = new Inventario(); Objetivo o = aberta("Quatro espadas");
		assertFalse(o.objetivoCumprido(c.azul(), c.partida, i));
		c.partida.getTurno().adicionarEfeitos(new EfeitosColuna(0, 0, Map.of(TipoTributo.ESPADA, 1)));
		assertTrue(o.objetivoCumprido(c.azul(), c.partida, i));
		assertEquals(3, c.azul().contarTributo(TipoTributo.ESPADA));
		assertEquals(1, c.partida.getTurno().getRecursoVirtual(TipoTributo.ESPADA));
		PartidaTest.turnoVazio(c.partida);
		assertFalse(o.objetivoCumprido(c.azul(), c.partida, i));
	}
	/** Objetivo: diferenciar total de variedade. Entrada: quatro ferros e quatro outros tipos virtuais. Saída esperada: cinco tipos. */
	@Test public void tesourosContamTiposSemConfundirDuplicatas() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar(); Inventario i = new Inventario();
		for (int n = 0; n < 4; n++) i.receberTesouro(TipoTesouro.FERRO);
		assertTrue(aberta("Quatro tesouros").objetivoCumprido(c.azul(), c.partida, i));
		assertFalse(aberta("Cinco tipos de tesouro").objetivoCumprido(c.azul(), c.partida, i));
		for (TipoTesouro tipo : TipoTesouro.values()) if (tipo != TipoTesouro.FERRO) c.partida.getTurno().adicionarTesouroVirtual(tipo);
		assertTrue(aberta("Cinco tipos de tesouro").objetivoCumprido(c.azul(), c.partida, i));
		assertEquals(4, i.totalTesouros());
	}
	/** Objetivo: incluir Tolui e conquistas no total. Entrada: yurt em K, dois na dupla e uma conquista. Saída esperada: quatro. */
	@Test public void totalDeYurtsIncluiCapitalDuplaEConquista() {
		PartidaTest.Cenario c = new PartidaTest.Cenario(Herdeiro.TOLUI, Herdeiro.OGEDEI).iniciar();
		assertTrue(c.tabuleiro.colocarYurt(c.tabuleiro.getParada("D"), c.azul()));
		assertTrue(c.tabuleiro.colocarYurt(c.tabuleiro.getParada("D"), c.azul()));
		Cidade cidade = c.revelada(); while (cidade.podeSerAtacada()) cidade.retirarTesouro(c.azul().getCor());
		Objetivo o = new ObjetivoMapa(ObjetivoMapa.Criterio.YURTS, 4);
		assertTrue(o.objetivoCumprido(c.azul(), c.partida, new Inventario()));
	}
	/** Objetivo: exigir todas as regiões. Entrada: yurts em duas e depois três regiões. Saída esperada: falso/verdadeiro. */
	@Test public void yurtPorRegiaoExigeAsTres() {
		PartidaTest.Cenario c = PartidaTest.padrao();
		Parada persia = c.tabuleiro.adicionarParada("P", Regiao.PERSIA, TipoParada.SIMPLES); c.iniciar();
		c.tabuleiro.colocarYurt(c.tabuleiro.getParada("B"), c.azul());
		c.tabuleiro.colocarYurt(c.tabuleiro.getParada("E"), c.azul());
		Inventario i = new Inventario(); Objetivo o = aberta("Um yurt por região");
		assertFalse(o.objetivoCumprido(c.azul(), c.partida, i));
		c.tabuleiro.colocarYurt(persia, c.azul());
		assertTrue(o.objetivoCumprido(c.azul(), c.partida, i));
	}
	/** Objetivo: rede conectada. Entrada: peças em B, C, duas em D e uma isolada E. Saída esperada: rede de quatro. */
	@Test public void redeContaPecasConectadasSemSomarGrupoIsolado() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		for (String p : new String[] { "B", "C", "D", "D", "E" }) c.tabuleiro.colocarYurt(c.tabuleiro.getParada(p), c.azul());
		Objetivo o = new ObjetivoMapa(ObjetivoMapa.Criterio.REDE, 4, 5, 6);
		assertEquals(1, o.votos(c.azul(), c.partida, new Inventario()));
	}
	/** Objetivo: adjacência não é conquista nem yurt de Karakorum. Entrada: conquista sem yurts ao redor; yurt em C; yurt em K. Saída esperada: Cidadão 0/1/1 voto; Prefeito sempre 0. */
	@Test public void objetivosDeAdjacenciaExcluemCentroDaCidadeEKarakorum() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		Cidade cidade = c.revelada(); while (cidade.podeSerAtacada()) cidade.retirarTesouro(c.azul().getCor());
		Objetivo cidadao = secreta("Cidadão"), prefeito = secreta("Prefeito"); Inventario i = new Inventario();
		assertEquals(0, cidadao.votos(c.azul(), c.partida, i));
		c.tabuleiro.colocarYurt(c.tabuleiro.getParada("C"), c.azul());
		assertEquals(1, cidadao.votos(c.azul(), c.partida, i));
		c.tabuleiro.getParada("K").colocarYurt(c.azul().getCor(), true);
		assertEquals(1, cidadao.votos(c.azul(), c.partida, i));
		assertEquals(0, prefeito.votos(c.azul(), c.partida, i));
	}
	/** Objetivo: manual, p. 20, o yurt de Tolui em Karakorum não forma rede. Entrada: Tolui com yurts em B e E, ligadas só por K. Saída esperada: Viajante 0 voto; a carta aberta de 6 yurts conta K. */
	@Test public void viajanteNaoUsaYurtDeToluiEmKarakorum() {
		PartidaTest.Cenario c = new PartidaTest.Cenario(Herdeiro.TOLUI, Herdeiro.OGEDEI).iniciar();
		assertTrue(c.tabuleiro.getParada("K").temYurtDe(c.azul().getCor()));
		assertTrue(c.tabuleiro.colocarYurt(c.tabuleiro.getParada("B"), c.azul()));
		assertTrue(c.tabuleiro.colocarYurt(c.tabuleiro.getParada("E"), c.azul()));
		assertEquals(0, secreta("Viajante").votos(c.azul(), c.partida, new Inventario()));
		assertEquals(3, new ObjetivoMapa(ObjetivoMapa.Criterio.YURTS, 3).progresso(c.azul(), c.partida, new Inventario()));
	}
	/** Objetivo: patamares secretos. Entrada: 0 a 10 melhorias. Saída esperada: 0/1/2/3 nos limites 4/7/10. */
	@Test public void soldadoPontuaNosTresLimites() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar(); Inventario i = new Inventario();
		Objetivo o = secreta("Soldado");
		for (int n = 0; n <= 10; n++) {
			assertEquals("Quantidade " + n, n < 4 ? 0 : n < 7 ? 1 : n < 10 ? 2 : 3, o.votos(c.azul(), c.partida, i));
			if (n < 10) { Coluna cor = new Coluna[] { Coluna.VERMELHA, Coluna.VERDE, Coluna.AMARELA }[n / 4]; adicionar(i, cor, Melhoria.Simbolo.MOEDA); }
		}
	}
	/** Objetivo: pares disjuntos e tesouros distintos. Entrada: 3 moedas, 1 ferro e 1 pele. Saída esperada: um par e três símbolos. */
	@Test public void paresNaoReutilizamMelhoriaNemMisturamTesouros() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar(); Inventario i = new Inventario();
		for (int n = 0; n < 3; n++) adicionar(i, Coluna.VERDE, Melhoria.Simbolo.MOEDA);
		adicionar(i, Coluna.VERMELHA, Melhoria.Simbolo.FERRO); adicionar(i, Coluna.AMARELA, Melhoria.Simbolo.PELES);
		assertEquals(1, secreta("Sacerdote").votos(c.azul(), c.partida, i));
		assertEquals(0, secreta("Colecionador").votos(c.azul(), c.partida, i));
		adicionar(i, Coluna.VERDE, Melhoria.Simbolo.MOEDA);
		assertEquals(2, secreta("Sacerdote").votos(c.azul(), c.partida, i));
	}
	/** Objetivo: branca conta como melhoria, não como linha regional. Entrada: branca com quatro. Saída esperada: coluna completa sem linha. */
	@Test public void brancaContaParaColunaCompletaMasNaoParaLinha() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar(); Inventario i = new Inventario();
		for (int n = 0; n < 4; n++) assertTrue(i.adicionarMelhoria(new Melhoria(Coluna.VERDE, Melhoria.Simbolo.MAO), Coluna.BRANCA, 0));
		assertTrue(aberta("Uma coluna completa").objetivoCumprido(c.azul(), c.partida, i));
		assertFalse(aberta("Uma linha").objetivoCumprido(c.azul(), c.partida, i));
	}
	/** Objetivo: bônus usados continuam contando. Entrada: duas peças viradas. Saída esperada: demanda cumprida. */
	@Test public void bonusUsadosContinuamContando() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar(); Inventario i = new Inventario();
		i.receberBonus(TipoBonus.MOEDAS); i.receberBonus(TipoBonus.ESPADAS);
		i.usarBonus(TipoBonus.MOEDAS); i.usarBonus(TipoBonus.ESPADAS);
		assertTrue(aberta("Dois bônus").objetivoCumprido(c.azul(), c.partida, i));
	}
	static Objetivo aberta(String nome) { return CartasOficiais.abertas().stream().filter(c -> c.getNome().equals(nome)).findFirst().orElseThrow().getObjetivo(); }
	static Objetivo secreta(String nome) { return CartasOficiais.secretas().stream().filter(c -> c.getNome().equals(nome)).findFirst().orElseThrow().getObjetivo(); }
	static void adicionar(Inventario i, Coluna coluna, Melhoria.Simbolo simbolo) {
		assertTrue("Adicionar melhoria ao cenário", i.adicionarMelhoria(new Melhoria(coluna, simbolo), coluna, 0));
	}
}
