package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/** Regras de rotas em um mapa pequeno, sem depender da topologia provisória. */
public class MovimentoTest {
	private Tabuleiro tabuleiro;
	private Jogador azul;
	private Jogador vermelho;
	private Peao peao;
	private Parada a, b, c;

	@Before
	public void preparar() {
		tabuleiro = new Tabuleiro();
		a = tabuleiro.adicionarParada("A", Regiao.RUSSIA, TipoParada.SIMPLES);
		b = tabuleiro.adicionarParada("B", Regiao.RUSSIA, TipoParada.SIMPLES);
		c = tabuleiro.adicionarParada("C", Regiao.RUSSIA, TipoParada.SIMPLES);
		tabuleiro.ligar(a, b);
		tabuleiro.ligar(b, c);
		azul = new Jogador("Azul", Cor.AZUL, Herdeiro.CHAGATAI);
		vermelho = new Jogador("Vermelho", Cor.VERMELHO, Herdeiro.OGEDEI);
		peao = azul.getPeao(0);
		tabuleiro.posicionarPeao(peao, a);
	}

	/** Objetivo: respeitar cavalos. Entrada: A-B-C, 1 ou 2. Saída: null ou custo 2. */
	@Test
	public void calculaCustoERecusaOrcamentoInsuficiente() {
		assertNull(Movimento.calcular(tabuleiro, peao, azul, "C", 1));
		Movimento.Resultado movimento = Movimento.calcular(tabuleiro, peao, azul, "C", 2);
		assertNotNull(movimento);
		assertEquals(c, movimento.getDestino());
		assertEquals(2, movimento.getCusto());
		assertEquals(Set.of(b, c), movimento.getVisitadas());
	}

	/** Objetivo: saltar yurt próprio. Entrada: yurt em B. Saída: custo 1 e só C visitada. */
	@Test
	public void intermediarioComYurtProprioNaoCustaNemContaComoVisita() {
		tabuleiro.colocarYurt(b, azul);
		Movimento.Resultado calculado = Movimento.calcular(tabuleiro, peao, azul, "C", 1);
		Movimento.Resultado escolhido = Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "C"), 1);
		assertEquals(1, calculado.getCusto());
		assertEquals(Set.of(c), calculado.getVisitadas());
		assertEquals(calculado.getVisitadas(), escolhido.getVisitadas());
		assertEquals(calculado.getCusto(), escolhido.getCusto());
	}

	/** Objetivo: cobrar a parada final. Entrada: destino B com yurt azul. Saída: custo 1, visita B. */
	@Test
	public void destinoComYurtProprioCustaUmEContaComoVisita() {
		tabuleiro.colocarYurt(b, azul);
		Movimento.Resultado movimento = Movimento.calcular(tabuleiro, peao, azul, "B", 1);
		assertEquals(1, movimento.getCusto());
		assertEquals(Set.of(b), movimento.getVisitadas());
	}

	/** Objetivo: atravessar ocupantes. Entrada: peão vermelho em B. Saída: chega em C, visita B e C. */
	@Test
	public void ocupacaoIntermediariaNaoBloqueiaOCaminho() {
		tabuleiro.posicionarPeao(vermelho.getPeao(0), b);
		Movimento.Resultado movimento = Movimento.calcular(tabuleiro, peao, azul, "C", 2);
		assertEquals(2, movimento.getCusto());
		assertEquals(Set.of(b, c), movimento.getVisitadas());
		assertNotNull(Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "C"), 2));
	}

	/** Objetivo: impedir destino ocupado. Entrada: peão vermelho em C. Saída: null nas duas APIs. */
	@Test
	public void ocupacaoDoDestinoRecusaOMovimento() {
		tabuleiro.posicionarPeao(vermelho.getPeao(0), c);
		assertNull(Movimento.calcular(tabuleiro, peao, azul, "C", 2));
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "C"), 2));
	}

	/** Objetivo: aceitar retorno pago. Entrada: A-B-A-B-C. Saída: custo 4; custo 3 é insuficiente. */
	@Test
	public void rotaExplicitaPodeRepetirParadasEVoltarAOrigem() {
		Movimento.Resultado volta = Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "A"), 2);
		assertEquals(a, volta.getDestino());
		assertEquals(2, volta.getCusto());
		assertEquals(Set.of(a, b), volta.getVisitadas());
		assertNull(Movimento.calcular(tabuleiro, peao, azul, "A", 2));
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "A", "B", "C"), 3));
		assertEquals(4, Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "A", "B", "C"), 4).getCusto());
	}

	/** Objetivo: distinguir última ocorrência. Entrada: B-C-B, com yurt em B. Saída: custo 2, visitas C e B. */
	@Test
	public void destinoVisitadoAntesSoCustaNaUltimaOcorrenciaSeTemYurtProprio() {
		tabuleiro.colocarYurt(b, azul);
		Movimento.Resultado movimento = Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "C", "B"), 2);
		assertEquals(b, movimento.getDestino());
		assertEquals(2, movimento.getCusto());
		assertEquals(Set.of(b, c), movimento.getVisitadas());
	}

	/** Objetivo: priorizar cavalos, não distância. Entrada: rota mais longa com yurts. Saída: custo 1. */
	@Test
	public void caminhoMaisLongoPodeSerMaisBarato() {
		Parada x = tabuleiro.adicionarParada("X", Regiao.RUSSIA, TipoParada.SIMPLES);
		Parada z = tabuleiro.adicionarParada("Z", Regiao.RUSSIA, TipoParada.SIMPLES);
		tabuleiro.ligar(a, z);
		tabuleiro.ligar(z, x);
		tabuleiro.ligar(x, c);
		tabuleiro.colocarYurt(z, azul);
		tabuleiro.colocarYurt(x, azul);
		Movimento.Resultado movimento = Movimento.calcular(tabuleiro, peao, azul, "C", 1);
		assertEquals(1, movimento.getCusto());
		assertEquals(Set.of(c), movimento.getVisitadas());
	}

	/** Objetivo: desempatar por passos. Entrada: duas rotas de custo 2. Saída: escolhe A-B-C. */
	@Test
	public void empateDeCustoPrefereMenosPassos() {
		Parada x = tabuleiro.adicionarParada("X", Regiao.RUSSIA, TipoParada.SIMPLES);
		Parada z = tabuleiro.adicionarParada("Z", Regiao.RUSSIA, TipoParada.SIMPLES);
		tabuleiro.ligar(a, z);
		tabuleiro.ligar(z, x);
		tabuleiro.ligar(x, c);
		tabuleiro.colocarYurt(z, azul);
		assertEquals(Set.of(b, c), Movimento.calcular(tabuleiro, peao, azul, "C", 2).getVisitadas());
	}

	/** Objetivo: desempatar por nomes. Entrada: A-B-C e A-D-C. Saída: visita B, independentemente do HashSet. */
	@Test
	public void empateDeCustoEPassosUsaOrdemDosNomes() {
		Parada d = tabuleiro.adicionarParada("D", Regiao.RUSSIA, TipoParada.SIMPLES);
		tabuleiro.ligar(a, d);
		tabuleiro.ligar(d, c);
		assertEquals(Set.of(b, c), Movimento.calcular(tabuleiro, peao, azul, "C", 2).getVisitadas());
	}

	/** Objetivo: validar entradas e rotas. Entrada: nomes/rotas/orçamento inválidos. Saída: null. */
	@Test
	public void argumentosOuRotasInvalidosNaoGeramResultado() {
		assertNull(Movimento.calcular(tabuleiro, peao, azul, "inexistente", 2));
		assertNull(Movimento.calcular(tabuleiro, peao, azul, null, 2));
		assertNull(Movimento.calcular(tabuleiro, peao, azul, "B", 0));
		assertNull(Movimento.calcular(null, peao, azul, "B", 1));
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, null, 2));
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, Collections.emptyList(), 2));
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("C"), 2));
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "inexistente"), 2));
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("A"), 1));
	}

	/** Objetivo: exigir peão do jogador e posicionado. Entrada: outro peão/falso/sem posição. Saída: null. */
	@Test
	public void peaoDevePertencerAoJogadorEEstarPosicionado() {
		assertNull(Movimento.calcular(tabuleiro, peao, vermelho, "B", 1));
		Peao falso = new Peao(Cor.AZUL, 0);
		tabuleiro.posicionarPeao(falso, a);
		assertNull(Movimento.calcular(tabuleiro, falso, azul, "B", 1));
		assertNull(Movimento.calcular(tabuleiro, vermelho.getPeao(0), vermelho, "B", 1));
	}

	/** Objetivo: calcular sem mutações. Entrada: duas consultas. Saída: posição e estoques preservados. */
	@Test
	public void calculoEValidacaoNaoAlteramOEstado() {
		azul.adicionarTributo(TipoTributo.MOEDA, 3);
		tabuleiro.colocarYurt(b, azul);
		int yurts = azul.getYurtsDisponiveis();
		Movimento.Resultado resultado = Movimento.calcular(tabuleiro, peao, azul, "C", 1);
		assertNotNull(Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "C"), 1));
		assertEquals(a, tabuleiro.posicaoDe(peao));
		assertEquals(yurts, azul.getYurtsDisponiveis());
		assertEquals(3, azul.contarTributo(TipoTributo.MOEDA));
		assertTrue(b.temYurtDe(Cor.AZUL));
		tabuleiro.removerYurt(b, azul);
		assertFalse(resultado.getVisitadas().contains(b));
	}

	/** Objetivo: proteger o resultado. Entrada: tentar alterar visitas. Saída: UnsupportedOperationException. */
	@Test(expected = UnsupportedOperationException.class)
	public void conjuntoDeVisitadasEhImutavel() {
		Movimento.calcular(tabuleiro, peao, azul, "C", 2).getVisitadas().clear();
	}

	/** Objetivo: finalizar busca sem rota. Entrada: parada isolada. Saída: null. */
	@Test(timeout = 1000)
	public void destinoSemCaminhoEhRecusado() {
		tabuleiro.adicionarParada("Isolada", Regiao.RUSSIA, TipoParada.SIMPLES);
		assertNull(Movimento.calcular(tabuleiro, peao, azul, "Isolada", 100));
	}
}
