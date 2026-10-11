package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Testes das regras locais de {@link Cidade} e {@link Provincia}. */
public class CidadeProvinciaTest {

	/**
	 * Objetivo: cidade fechada não pode ser atacada; ao ser revelada recebe 4
	 * tesouros (manual, p. 4).
	 * Entrada: cidade nova; revelar().
	 * Saída esperada: antes, podeSerAtacada falso e retirarTesouro falha;
	 * depois, 4 tesouros e podeSerAtacada verdadeiro.
	 */
	@Test
	public void cidadeSoPodeSerAtacadaDepoisDeRevelada() {
		Cidade c = new Cidade("Pequim", Regiao.CHINA);
		assertFalse(c.podeSerAtacada());
		assertFalse(c.retirarTesouro(Cor.AZUL));
		c.revelar();
		assertEquals(Cidade.TESOUROS_AO_REVELAR, c.getTesouros());
		assertTrue(c.podeSerAtacada());
	}

	/**
	 * Objetivo: quem retira o último tesouro conquista a cidade; depois disso
	 * ninguém mais ataca (manual, p. 10).
	 * Entrada: cidade com 2 tesouros; Azul tira 1, Verde tira o último.
	 * Saída esperada: conquistada por Verde (não por Azul); novo ataque falha.
	 */
	@Test
	public void ultimoTesouroConquistaACidade() {
		Cidade c = new Cidade("Cabul", Regiao.PERSIA);
		c.revelar(2);
		assertTrue(c.retirarTesouro(Cor.AZUL));
		assertNull(c.getConquistadaPor());
		assertTrue(c.retirarTesouro(Cor.VERDE));
		assertEquals(Cor.VERDE, c.getConquistadaPor());
		assertTrue(c.isConquistada());
		assertFalse(c.podeSerAtacada());
		assertFalse(c.retirarTesouro(Cor.AZUL));
	}

	/**
	 * Objetivo: revelar duas vezes a mesma cidade é erro de programação.
	 * Entrada: revelar() duas vezes.
	 * Saída esperada: IllegalStateException.
	 */
	@Test(expected = IllegalStateException.class)
	public void revelarDuasVezesFalha() {
		Cidade c = new Cidade("Kiev", Regiao.RUSSIA);
		c.revelar();
		c.revelar();
	}

	/**
	 * Objetivo: província guarda no máximo 3 peças de tributo e não fica
	 * negativa (manual, p. 9).
	 * Entrada: colocar 4 peças; retirar 4.
	 * Saída esperada: 4ª colocação recusada (fica 3); 4ª retirada recusada
	 * (fica 0).
	 */
	@Test
	public void provinciaRespeitaMaximoDeTresPecas() {
		Provincia p = new Provincia("Estepe", Regiao.RUSSIA, TipoTributo.YURT, false);
		assertTrue(p.colocarPeca());
		assertTrue(p.colocarPeca());
		assertTrue(p.colocarPeca());
		assertFalse(p.colocarPeca());
		assertEquals(Provincia.MAXIMO_PECAS, p.getPecas());
		assertTrue(p.retirarPeca());
		assertTrue(p.retirarPeca());
		assertTrue(p.retirarPeca());
		assertFalse(p.retirarPeca());
		assertEquals(0, p.getPecas());
	}

	/**
	 * Objetivo: só província do Khan pode ter "vizinhas do Khan" (as setas).
	 * Entrada: província comum tenta registrar vizinha.
	 * Saída esperada: IllegalStateException.
	 */
	@Test(expected = IllegalStateException.class)
	public void provinciaComumNaoTemVizinhasDoKhan() {
		Provincia comum = new Provincia("Vale", Regiao.CHINA, TipoTributo.MOEDA, false);
		Provincia outra = new Provincia("Outra", Regiao.CHINA, TipoTributo.MOEDA, false);
		comum.adicionarVizinhaDoKhan(outra);
	}
}
