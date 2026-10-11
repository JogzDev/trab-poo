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

/** Regras de rotas em um mapa pequeno e controlado. */
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
		assertNull("Um cavalo não deve alcançar C a dois passos de A", Movimento.calcular(tabuleiro, peao, azul, "C", 1));
		Movimento.Resultado movimento = Movimento.calcular(tabuleiro, peao, azul, "C", 2);
		assertNotNull("Dois cavalos devem permitir alcançar C", movimento);
		assertEquals("O destino calculado deve ser C", c, movimento.getDestino());
		assertEquals("O custo de A-B-C deve ser dois cavalos", 2, movimento.getCusto());
		assertEquals("As visitas devem conter B e C", Set.of(b, c), movimento.getVisitadas());
	}

	/** Objetivo: saltar yurt próprio. Entrada: yurt em B. Saída: custo 1 e só C visitada. */
	@Test
	public void intermediarioComYurtProprioNaoCustaNemContaComoVisita() {
		tabuleiro.colocarYurt(b, azul);
		Movimento.Resultado calculado = Movimento.calcular(tabuleiro, peao, azul, "C", 1);
		Movimento.Resultado escolhido = Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "C"), 1);
		assertEquals("Pular o yurt de B deve reduzir o custo para um cavalo", 1, calculado.getCusto());
		assertEquals("A parada B pulada não deve contar como visita", Set.of(c), calculado.getVisitadas());
		assertEquals("A rota explícita deve registrar as mesmas visitas da busca", calculado.getVisitadas(), escolhido.getVisitadas());
		assertEquals("A rota explícita deve ter o mesmo custo da busca", calculado.getCusto(), escolhido.getCusto());
	}

	/** Objetivo: cobrar a parada final. Entrada: destino B com yurt azul. Saída: custo 1, visita B. */
	@Test
	public void destinoComYurtProprioCustaUmEContaComoVisita() {
		tabuleiro.colocarYurt(b, azul);
		Movimento.Resultado movimento = Movimento.calcular(tabuleiro, peao, azul, "B", 1);
		assertEquals("Parar no destino com yurt próprio ainda deve custar um cavalo", 1, movimento.getCusto());
		assertEquals("O destino B deve contar como visita", Set.of(b), movimento.getVisitadas());
	}

	/** Objetivo: atravessar ocupantes. Entrada: peão vermelho em B. Saída: chega em C, visita B e C. */
	@Test
	public void ocupacaoIntermediariaNaoBloqueiaOCaminho() {
		tabuleiro.posicionarPeao(vermelho.getPeao(0), b);
		Movimento.Resultado movimento = Movimento.calcular(tabuleiro, peao, azul, "C", 2);
		assertEquals("Atravessar B ocupada deve custar os dois passos da rota", 2, movimento.getCusto());
		assertEquals("B ocupada e C devem contar como visitas", Set.of(b, c), movimento.getVisitadas());
		assertNotNull("A rota explícita deve permitir atravessar B ocupada", Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "C"), 2));
	}

	/** Objetivo: impedir destino ocupado. Entrada: peão vermelho em C. Saída: null nas duas APIs. */
	@Test
	public void ocupacaoDoDestinoRecusaOMovimento() {
		tabuleiro.posicionarPeao(vermelho.getPeao(0), c);
		assertNull("A busca deve recusar o destino C ocupado por outro jogador", Movimento.calcular(tabuleiro, peao, azul, "C", 2));
		assertNull("A rota explícita deve recusar o destino C ocupado", Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "C"), 2));
	}

	/** Objetivo: aceitar retorno pago. Entrada: A-B-A-B-C. Saída: custo 4; custo 3 é insuficiente. */
	@Test
	public void rotaExplicitaPodeRepetirParadasEVoltarAOrigem() {
		Movimento.Resultado volta = Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "A"), 2);
		assertEquals("A rota B-A deve terminar de volta em A", a, volta.getDestino());
		assertEquals("Voltar à origem deve custar dois cavalos", 2, volta.getCusto());
		assertEquals("O retorno deve registrar as visitas a A e B", Set.of(a, b), volta.getVisitadas());
		assertNull("A busca de menor custo não deve sugerir ficar na origem", Movimento.calcular(tabuleiro, peao, azul, "A", 2));
		assertNull("Três cavalos não devem pagar a rota de quatro passos", Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "A", "B", "C"), 3));
		assertEquals("Quatro cavalos devem pagar exatamente os quatro passos", 4, Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "A", "B", "C"), 4).getCusto());
	}

	/** Objetivo: distinguir última ocorrência. Entrada: B-C-B, com yurt em B. Saída: custo 2, visitas C e B. */
	@Test
	public void destinoVisitadoAntesSoCustaNaUltimaOcorrenciaSeTemYurtProprio() {
		tabuleiro.colocarYurt(b, azul);
		Movimento.Resultado movimento = Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "C", "B"), 2);
		assertEquals("A rota B-C-B deve terminar em B", b, movimento.getDestino());
		assertEquals("Pular a primeira entrada em B deve deixar o custo em dois cavalos", 2, movimento.getCusto());
		assertEquals("A última entrada em B e a passagem por C devem contar como visitas", Set.of(b, c), movimento.getVisitadas());
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
		assertEquals("A rota com dois yurts intermediários deve custar um cavalo", 1, movimento.getCusto());
		assertEquals("Os dois yurts pulados não devem constar nas visitas", Set.of(c), movimento.getVisitadas());
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
		assertEquals("O empate de custo deve escolher a rota curta por B", Set.of(b, c), Movimento.calcular(tabuleiro, peao, azul, "C", 2).getVisitadas());
	}

	/** Objetivo: desempatar por nomes. Entrada: A-B-C e A-D-C. Saída: visita B, independentemente do HashSet. */
	@Test
	public void empateDeCustoEPassosUsaOrdemDosNomes() {
		Parada d = tabuleiro.adicionarParada("D", Regiao.RUSSIA, TipoParada.SIMPLES);
		tabuleiro.ligar(a, d);
		tabuleiro.ligar(d, c);
		assertEquals("O empate de custo e passos deve escolher B antes de D", Set.of(b, c), Movimento.calcular(tabuleiro, peao, azul, "C", 2).getVisitadas());
	}

	/** Objetivo: validar entradas e rotas. Entrada: nomes/rotas/orçamento inválidos. Saída: null. */
	@Test
	public void argumentosOuRotasInvalidosNaoGeramResultado() {
		assertNull("A busca deve recusar nome de destino inexistente", Movimento.calcular(tabuleiro, peao, azul, "inexistente", 2));
		assertNull("A busca deve recusar destino nulo", Movimento.calcular(tabuleiro, peao, azul, null, 2));
		assertNull("A busca deve recusar orçamento sem cavalos", Movimento.calcular(tabuleiro, peao, azul, "B", 0));
		assertNull("A busca deve recusar tabuleiro nulo", Movimento.calcular(null, peao, azul, "B", 1));
		assertNull("A validação deve recusar rota nula", Movimento.validarRota(tabuleiro, peao, azul, null, 2));
		assertNull("A validação deve recusar rota vazia", Movimento.validarRota(tabuleiro, peao, azul, Collections.emptyList(), 2));
		assertNull("A rota não deve saltar uma ligação inexistente entre A e C", Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("C"), 2));
		assertNull("A rota deve recusar uma parada inexistente no segundo passo", Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "inexistente"), 2));
		assertNull("Um passo da rota não deve permanecer na mesma parada", Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("A"), 1));
	}

	/** Objetivo: exigir peão do jogador e posicionado. Entrada: outro peão/falso/sem posição. Saída: null. */
	@Test
	public void peaoDevePertencerAoJogadorEEstarPosicionado() {
		assertNull("O peão azul não deve ser movido como peão do jogador vermelho", Movimento.calcular(tabuleiro, peao, vermelho, "B", 1));
		Peao falso = new Peao(Cor.AZUL, 0);
		tabuleiro.posicionarPeao(falso, a);
		assertNull("Um objeto de peão com mesma cor e índice não deve substituir o peão registrado", Movimento.calcular(tabuleiro, falso, azul, "B", 1));
		assertNull("Um peão sem posição inicial não deve gerar movimento", Movimento.calcular(tabuleiro, vermelho.getPeao(0), vermelho, "B", 1));
	}

	/** Objetivo: calcular sem mutações. Entrada: duas consultas. Saída: posição e estoques preservados. */
	@Test
	public void calculoEValidacaoNaoAlteramOEstado() {
		azul.adicionarTributo(TipoTributo.MOEDA, 3);
		tabuleiro.colocarYurt(b, azul);
		int yurts = azul.getYurtsDisponiveis();
		Movimento.Resultado resultado = Movimento.calcular(tabuleiro, peao, azul, "C", 1);
		assertNotNull("A consulta da rota válida deve produzir resultado", Movimento.validarRota(tabuleiro, peao, azul, Arrays.asList("B", "C"), 1));
		assertEquals("A consulta deve preservar a posição do peão em A", a, tabuleiro.posicaoDe(peao));
		assertEquals("A consulta deve preservar os yurts disponíveis do jogador", yurts, azul.getYurtsDisponiveis());
		assertEquals("A consulta deve preservar as três moedas físicas", 3, azul.contarTributo(TipoTributo.MOEDA));
		assertTrue("A consulta deve preservar o yurt azul em B", b.temYurtDe(Cor.AZUL));
		tabuleiro.removerYurt(b, azul);
		assertFalse("O resultado deve preservar suas visitas após remover o yurt do tabuleiro", resultado.getVisitadas().contains(b));
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
		assertNull("A busca deve recusar a parada isolada mesmo com cem cavalos", Movimento.calcular(tabuleiro, peao, azul, "Isolada", 100));
	}
}
