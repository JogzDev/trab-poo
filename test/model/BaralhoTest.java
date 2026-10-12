package model;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class BaralhoTest {
	/** Objetivo: conferir catálogo. Entrada: fábricas. Saída esperada: 14 abertas e 10 secretas únicas. */
	@Test public void catalogoTemTodasAsCartas() {
		assertEquals(14, CartasOficiais.abertas().size());
		assertEquals(10, CartasOficiais.secretas().size());
		HashSet<String> nomes = new HashSet<>();
		for (Carta carta : CartasOficiais.abertas()) { assertFalse(carta.isSecreta()); assertTrue(nomes.add(carta.getNome())); }
		for (Carta carta : CartasOficiais.secretas()) { assertTrue(carta.isSecreta()); assertTrue(nomes.add(carta.getNome())); }
	}
	/** Objetivo: embaralhar sem perder cartas. Entrada: semente fixa. Saída esperada: ordem repetível e sem repetição. */
	@Test public void compraEReproduzivelESemReposicao() {
		List<Carta> cartas = CartasOficiais.abertas();
		Baralho a = new Baralho(cartas, new Random(42)), b = new Baralho(cartas, new Random(42));
		HashSet<Carta> compradas = new HashSet<>();
		while (a.restantes() > 0) {
			Carta carta = a.comprarCarta();
			assertSame(carta, b.comprarCarta());
			assertTrue("Nenhuma carta repetida", compradas.add(carta));
		}
		assertEquals(new HashSet<>(cartas), compradas);
		assertNull(a.comprarCarta());
	}
	/** Objetivo: distribuição atômica. Entrada: pedido maior que reserva. Saída esperada: mão e reserva intactas. */
	@Test public void distribuicaoInsuficienteNaoCompraParcialmente() {
		Baralho baralho = new Baralho(CartasOficiais.secretas(), new Random(1));
		Jogador jogador = PartidaTest.padrao().azul();
		assertFalse(baralho.distribuir(jogador, 11));
		assertEquals(10, baralho.restantes());
		assertTrue(baralho.cartasDe(jogador).isEmpty());
		assertTrue(baralho.distribuir(jogador, 2));
		assertEquals(2, baralho.cartasDe(jogador).size());
	}
	/** Objetivo: descarte sem duplicação. Entrada: descartar carta da mão duas vezes. Saída esperada: só a primeira aceita. */
	@Test public void descarteRetiraDaMaoENaoReembaralha() {
		Baralho baralho = new Baralho(CartasOficiais.secretas(), new Random(1));
		Jogador jogador = PartidaTest.padrao().azul();
		assertTrue(baralho.distribuir(jogador, 2));
		Carta carta = baralho.cartasDe(jogador).get(0);
		assertTrue(baralho.descartar(carta));
		assertFalse(baralho.descartar(carta));
		assertEquals(1, baralho.cartasDe(jogador).size());
		assertEquals(List.of(carta), baralho.getDescarte());
		assertEquals(8, baralho.restantes());
	}
	/** Objetivo: proteger coleções. Entrada: alterar origem e retorno. Saída esperada: reserva intacta e retorno imutável. */
	@Test public void colecoesNaoPermitemAlteracaoExterna() {
		List<Carta> origem = new ArrayList<>(CartasOficiais.secretas());
		Baralho baralho = new Baralho(origem, new Random(1)); origem.clear();
		Jogador jogador = PartidaTest.padrao().azul();
		assertTrue(baralho.distribuir(jogador, 2));
		assertEquals(8, baralho.restantes());
		assertThrows(UnsupportedOperationException.class, () -> baralho.cartasDe(jogador).clear());
		assertThrows(UnsupportedOperationException.class, () -> baralho.getDescarte().clear());
	}
	/** Objetivo: validar peças. Entrada: carta nula/repetida. Saída esperada: exceção antes da compra. */
	@Test public void rejeitaPecasInvalidas() {
		Carta carta = CartasOficiais.abertas().get(0);
		assertThrows(IllegalArgumentException.class, () -> new Baralho(List.of(carta, carta), new Random()));
		assertThrows(IllegalArgumentException.class, () -> new Baralho(java.util.Arrays.asList(carta, null), new Random()));
	}
}
