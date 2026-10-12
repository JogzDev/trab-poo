package model;

import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class PontuacaoTest {
	/** Objetivo: conferir tabela impressa. Entrada: 0 a 4 melhorias. Saída esperada: 1/1/2/2/3 e 0/1/1/2/2. */
	@Test public void tabelaDeInfluenciaRespeitaTodosOsLimites() {
		int[] primeiro = { 1, 1, 2, 2, 3 }, segundo = { 0, 1, 1, 2, 2 };
		for (int n = 0; n <= 4; n++) {
			assertEquals("Primeiro com " + n + " melhorias", primeiro[n], Pontuacao.votosPorMelhorias(n, true));
			assertEquals("Segundo com " + n + " melhorias", segundo[n], Pontuacao.votosPorMelhorias(n, false));
		}
		assertThrows(IllegalArgumentException.class, () -> Pontuacao.votosPorMelhorias(5, true));
	}
	/** Objetivo: desempate em primeiro suprime segundo. Entrada: dois líderes e um terceiro. Saída esperada: ambos líderes pontuam. */
	@Test public void empateNoPrimeiroNaoPremiaSegundo() {
		PartidaTest.Cenario c = new PartidaTest.Cenario(Herdeiro.CHAGATAI, Herdeiro.OGEDEI, Herdeiro.ALTANI).iniciar();
		Jogador a = c.azul(), b = c.jogadores.get(1), d = c.jogadores.get(2);
		c.tabuleiro.colocarYurt(c.tabuleiro.getParada("D"), a); c.tabuleiro.colocarYurt(c.tabuleiro.getParada("D"), a);
		c.tabuleiro.colocarYurt(c.tabuleiro.getParada("B"), b); c.tabuleiro.colocarYurt(c.tabuleiro.getParada("C"), b);
		Parada extra = c.tabuleiro.adicionarParada("R", Regiao.RUSSIA, TipoParada.SIMPLES); c.tabuleiro.colocarYurt(extra, d);
		Inventario i = new Inventario();
		ObjetivosTest.adicionar(i, Coluna.AMARELA, Melhoria.Simbolo.MAO); ObjetivosTest.adicionar(i, Coluna.AMARELA, Melhoria.Simbolo.MAO);
		assertEquals(2, Pontuacao.influencia(a, c.tabuleiro, i));
		assertEquals(2, Pontuacao.influencia(b, c.tabuleiro, i));
		assertEquals(0, Pontuacao.influencia(d, c.tabuleiro, i));
	}
	/** Objetivo: empate em segundo premia ambos. Entrada: 2/1/1 yurts. Saída esperada: um voto para cada segundo com duas melhorias. */
	@Test public void empateNoSegundoPremiaAmbos() {
		PartidaTest.Cenario c = new PartidaTest.Cenario(Herdeiro.CHAGATAI, Herdeiro.OGEDEI, Herdeiro.ALTANI).iniciar();
		c.tabuleiro.colocarYurt(c.tabuleiro.getParada("D"), c.azul()); c.tabuleiro.colocarYurt(c.tabuleiro.getParada("D"), c.azul());
		c.tabuleiro.colocarYurt(c.tabuleiro.getParada("B"), c.jogadores.get(1));
		c.tabuleiro.colocarYurt(c.tabuleiro.getParada("C"), c.jogadores.get(2));
		Inventario i = new Inventario();
		ObjetivosTest.adicionar(i, Coluna.AMARELA, Melhoria.Simbolo.MAO); ObjetivosTest.adicionar(i, Coluna.AMARELA, Melhoria.Simbolo.MAO);
		assertEquals(1, Pontuacao.influencia(c.jogadores.get(1), c.tabuleiro, i));
		assertEquals(1, Pontuacao.influencia(c.jogadores.get(2), c.tabuleiro, i));
	}
	/** Objetivo: exigir yurt e coluna regional. Entrada: quatro melhorias brancas e nenhum yurt, depois yurt chinês. Saída esperada: zero/um. */
	@Test public void brancaNaoAumentaInfluenciaENinguemPontuaSemYurt() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar(); Inventario i = new Inventario();
		for (int n = 0; n < 4; n++) assertTrue(i.adicionarMelhoria(new Melhoria(Coluna.VERMELHA, Melhoria.Simbolo.MAO), Coluna.BRANCA, 0));
		assertEquals(0, Pontuacao.influencia(c.azul(), c.tabuleiro, i));
		c.tabuleiro.colocarYurt(c.tabuleiro.getParada("E"), c.azul());
		assertEquals(1, Pontuacao.influencia(c.azul(), c.tabuleiro, i));
	}
	/** Objetivo: integrar contagem final uma vez. Entrada: fim disparado e dois yurts regionais. Saída esperada: influência adicionada sem mover neutra. */
	@Test public void fimAplicaInfluenciaECartaSecretaUmaUnicaVez() {
		ComponentesKhanTest.Cenario c = new ComponentesKhanTest.Cenario(Herdeiro.CHAGATAI, 20).iniciar();
		c.base.tabuleiro.colocarYurt(c.base.tabuleiro.getParada("B"), c.jogador());
		c.base.tabuleiro.colocarYurt(c.base.tabuleiro.getParada("E"), c.jogador());
		c.jogador().adicionarVotos(10); c.partida.concluirAcaoComplementar(false);
		int neutra = c.partida.getPosicaoKurultai();
		int esperado = c.jogador().getVotos() + Pontuacao.influencia(c.jogador(), c.base.tabuleiro, c.inventario())
				+ c.componentes.cartasDe(c.jogador()).get(0).getObjetivo().votos(c.jogador(), c.partida, c.inventario());
		for (int n = 0; n < 4; n++) PartidaTest.turnoVazio(c.partida);
		assertEquals(Fase.ENCERRADA, c.partida.getFase());
		assertEquals(esperado, c.jogador().getVotos());
		assertEquals(neutra, c.partida.getPosicaoKurultai());
		c.componentes.pontuarFimDeJogo(c.partida);
		assertEquals(esperado, c.jogador().getVotos());
		assertFalse(c.partida.terminarTurno()); assertFalse(c.componentes.comprarMelhoria(0));
		assertEquals(List.of(c.jogador()), c.componentes.getVencedores());
	}
	/** Objetivo: desempatar por saldo físico e distância. Entrada: votos iguais, tesouros e posições variados. Saída esperada: critérios em ordem. */
	@Test public void vencedorUsaTesourosRestantesDistanciaEVitoriaCompartilhada() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		Map<Jogador, Inventario> inventarios = new HashMap<>();
		for (Jogador jogador : c.jogadores) { inventarios.put(jogador, new Inventario()); jogador.adicionarVotos(5); }
		assertTrue(Pontuacao.vencedores(c.partida, inventarios).isEmpty());
		c.partida.concluirAcaoComplementar(false);
		for (int n = 0; n < 4; n++) PartidaTest.turnoVazio(c.partida);
		assertEquals(c.jogadores, Pontuacao.vencedores(c.partida, inventarios));
		Jogador outro = c.jogadores.get(1);
		c.tabuleiro.posicionarPeao(outro.getPeao(0), c.tabuleiro.getParada("D"));
		assertEquals(List.of(c.azul()), Pontuacao.vencedores(c.partida, inventarios));
		inventarios.get(outro).receberTesouro(TipoTesouro.FERRO);
		assertEquals(List.of(outro), Pontuacao.vencedores(c.partida, inventarios));
		assertTrue(inventarios.get(outro).gastarTesouros(Map.of(TipoTesouro.FERRO, 1)));
		assertEquals(List.of(c.azul()), Pontuacao.vencedores(c.partida, inventarios));
	}
}
