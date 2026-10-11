package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/** Escolha por passo entre visitar e pular um yurt próprio intermediário. */
public class MovimentoOpcionalTest {
	private Tabuleiro tabuleiro;
	private Jogador azul;
	private Jogador vermelho;
	private Peao peao;
	private Parada a;
	private Parada b;
	private Parada c;

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
		tabuleiro.colocarYurt(b, azul);
	}

	/** Objetivo: visitar yurt próprio. Entrada: A-B-C sem saltos. Saída: custo 2, visitas B/C. */
	@Test(timeout = 1000)
	public void visitarYurtProprioCustaCavaloEAutorizaSuaVisita() {
		List<String> rota = List.of("B", "C");
		List<Boolean> saltos = List.of(false, false);
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, rota, saltos, 1));
		Movimento.Resultado movimento = Movimento.validarRota(tabuleiro, peao, azul, rota, saltos, 2);
		assertNotNull(movimento);
		assertEquals(2, movimento.getCusto());
		assertEquals(Set.of(b, c), movimento.getVisitadas());
		assertEquals(c, movimento.getDestino());
		assertEquals(a, tabuleiro.posicaoDe(peao));
		assertEquals(11, azul.getYurtsDisponiveis());
	}

	/** Objetivo: pular por escolha. Entrada: salto em B, visita C. Saída: custo 1 e só C visitada. */
	@Test(timeout = 1000)
	public void saltarYurtProprioEconomizaCavaloEExcluiSuaVisita() {
		Movimento.Resultado movimento = Movimento.validarRota(tabuleiro, peao, azul,
				List.of("B", "C"), List.of(true, false), 1);
		assertNotNull(movimento);
		assertEquals(1, movimento.getCusto());
		assertEquals(Set.of(c), movimento.getVisitadas());
		assertEquals(a, tabuleiro.posicaoDe(peao));
	}

	/** Objetivo: recusar instruções inválidas. Entrada: listas/nulos/saltos ilegais. Saída: null sem mutação. */
	@Test(timeout = 1000)
	public void instrucoesInvalidasNaoAlteramPosicaoOuYurts() {
		List<String> rota = List.of("B", "C");
		tabuleiro.colocarYurt(c, azul);
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, rota, List.of(true), 2));
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, rota, List.of(true, false, false), 2));
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, rota, null, 2));
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, rota, Arrays.asList(null, false), 2));
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, rota, List.of(false, true), 2));
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, List.of("B", "inexistente"), List.of(true, false), 2));
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, List.of("C"), List.of(false), 2));
		tabuleiro.removerYurt(b, azul);
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, rota, List.of(true, false), 2));
		tabuleiro.colocarYurt(b, vermelho);
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, rota, List.of(true, false), 2));
		assertEquals(a, tabuleiro.posicaoDe(peao));
		assertEquals(11, azul.getYurtsDisponiveis());
		assertEquals(List.of(Cor.VERMELHO), b.getYurts());
		assertEquals(List.of(Cor.AZUL), c.getYurts());
	}

	/** Objetivo: manter passagem por ocupantes. Entrada: oponente em B/depois C. Saída: passa B, recusa C. */
	@Test(timeout = 1000)
	public void ocupacaoIntermediariaPermiteVisitaOuSaltoMasDestinoCheioRecusa() {
		tabuleiro.posicionarPeao(vermelho.getPeao(0), b);
		Movimento.Resultado visita = Movimento.validarRota(tabuleiro, peao, azul,
				List.of("B", "C"), List.of(false, false), 2);
		assertNotNull(visita);
		assertEquals(Set.of(b, c), visita.getVisitadas());
		assertNotNull(Movimento.validarRota(tabuleiro, peao, azul, List.of("B", "C"), List.of(true, false), 1));
		tabuleiro.posicionarPeao(vermelho.getPeao(0), c);
		assertNull(Movimento.validarRota(tabuleiro, peao, azul, List.of("B", "C"), List.of(true, false), 2));
		assertEquals(a, tabuleiro.posicaoDe(peao));
	}

	/** Objetivo: preservar chamadas existentes. Entrada: rota padrão e cálculo mínimo. Saída: ambos pulam B. */
	@Test(timeout = 1000)
	public void rotaPadraoECalculoMinimoMantemSaltosAutomaticos() {
		Movimento.Resultado padrao = Movimento.validarRota(tabuleiro, peao, azul, List.of("B", "C"), 1);
		Movimento.Resultado minimo = Movimento.calcular(tabuleiro, peao, azul, "C", 1);
		assertNotNull(padrao);
		assertNotNull(minimo);
		assertEquals(1, padrao.getCusto());
		assertEquals(padrao.getCusto(), minimo.getCusto());
		assertEquals(Set.of(c), padrao.getVisitadas());
		assertEquals(padrao.getVisitadas(), minimo.getVisitadas());
		Movimento.Resultado destinoYurt = Movimento.validarRota(tabuleiro, peao, azul,
				List.of("B"), List.of(false), 1);
		assertEquals(1, destinoYurt.getCusto());
		assertEquals(Set.of(b), destinoYurt.getVisitadas());
	}

	/** Objetivo: opções por ocorrência. Entrada: visita B e depois pula B. Saída: custo 3, B continua visitada. */
	@Test(timeout = 1000)
	public void saltoPosteriorNaoApagaVisitaAnteriorAoMesmoYurt() {
		Movimento.Resultado movimento = Movimento.validarRota(tabuleiro, peao, azul,
				List.of("B", "A", "B", "C"), List.of(false, false, true, false), 3);
		assertNotNull(movimento);
		assertEquals(3, movimento.getCusto());
		assertEquals(Set.of(a, b, c), movimento.getVisitadas());
	}
}
