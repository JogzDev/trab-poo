package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Testes do estado guardado por Jogador e dos dados de Herdeiro. */
public class JogadorTest {

	/**
	 * Objetivo: a preparação dá a cada jogador 12 yurts, 4 peças de ativação e
	 * o número de peões do herdeiro (manual, p. 3; cartela de Altani).
	 * Entrada: jogador Altani e jogador Jochi.
	 * Saída esperada: 12 yurts; 4 fichas; Altani com 2 peões, Jochi com 1.
	 */
	@Test
	public void preparacaoInicialDoJogador() {
		Jogador altani = new Jogador("Ana", Cor.AZUL, Herdeiro.ALTANI);
		Jogador jochi = new Jogador("Bia", Cor.VERDE, Herdeiro.JOCHI);
		assertEquals(Jogador.YURTS_INICIAIS, altani.getYurtsDisponiveis());
		assertEquals(4, altani.getFichasDisponiveis().size());
		assertEquals(2, altani.getPeoes().size());
		assertEquals(1, jochi.getPeoes().size());
		assertEquals(Cor.AZUL, altani.getPeao(1).getCor());
		assertEquals(0, altani.getVotos());
	}

	/**
	 * Objetivo: cada peça de ativação só pode ser usada uma vez até a coluna
	 * branca recuperá-las (manual, pp. 6 e 9).
	 * Entrada: usar Vermelha duas vezes; recuperarFichas(); usar Vermelha.
	 * Saída esperada: 1º uso ok, 2º recusado, após recuperar volta a funcionar.
	 */
	@Test
	public void fichaDeAtivacaoSoUsaUmaVezAteRecuperar() {
		Jogador j = new Jogador("Ana", Cor.AZUL, Herdeiro.OGEDEI);
		assertTrue(j.usarFicha(Coluna.VERMELHA));
		assertFalse(j.usarFicha(Coluna.VERMELHA));
		assertFalse(j.temFichaDisponivel(Coluna.VERMELHA));
		assertEquals(3, j.getFichasDisponiveis().size());
		j.recuperarFichas();
		assertTrue(j.usarFicha(Coluna.VERMELHA));
	}

	/**
	 * Objetivo: gastar tributo que o jogador não tem é recusado sem alterar o
	 * saldo (ex.: comprar melhoria custa 2 moedas).
	 * Entrada: 1 moeda; gastar 2; depois gastar 1.
	 * Saída esperada: gasto de 2 recusado e saldo continua 1; gasto de 1 ok e
	 * saldo vai a 0.
	 */
	@Test
	public void gastarTributoInsuficienteEhRecusado() {
		Jogador j = new Jogador("Ana", Cor.AZUL, Herdeiro.CHAGATAI);
		j.adicionarTributo(TipoTributo.MOEDA, 1);
		assertFalse(j.gastarTributo(TipoTributo.MOEDA, 2));
		assertEquals(1, j.contarTributo(TipoTributo.MOEDA));
		assertTrue(j.gastarTributo(TipoTributo.MOEDA, 1));
		assertEquals(0, j.contarTributo(TipoTributo.MOEDA));
		assertEquals(0, j.contarTributo(TipoTributo.ESPADA));
	}

	/**
	 * Objetivo: as colunas transcritas das cartelas estão coerentes: a coluna
	 * branca é igual para todos (2 cavalos, Khan, recuperar) e cada herdeiro
	 * tem exatamente uma coluna colorida com Khan e uma com moeda virtual.
	 * Entrada: todos os herdeiros.
	 * Saída esperada: conforme descrito.
	 */
	@Test
	public void colunasDosHerdeirosSaoCoerentes() {
		for (Herdeiro h : Herdeiro.values()) {
			Herdeiro.AcoesColuna branca = h.getAcoes(Coluna.BRANCA);
			assertEquals(2, branca.getCavalos());
			assertTrue(branca.temKhan());
			assertTrue(branca.recuperaFichas());

			int comKhan = 0, comMoeda = 0;
			for (Coluna c : new Coluna[] { Coluna.VERMELHA, Coluna.VERDE, Coluna.AMARELA }) {
				Herdeiro.AcoesColuna ac = h.getAcoes(c);
				assertFalse(ac.recuperaFichas());
				assertTrue(ac.getCavalos() >= 1 && ac.getMaos() >= 1);
				if (ac.temKhan()) comKhan++;
				if (ac.temMoedaVirtual()) comMoeda++;
			}
			assertEquals(h + ": colunas com Khan", 1, comKhan);
			assertEquals(h + ": colunas com moeda virtual", 1, comMoeda);
		}
		assertEquals(2, Herdeiro.ALTANI.getQuantidadePeoes());
	}
}
