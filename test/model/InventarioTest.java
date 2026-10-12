package model;

import static org.junit.Assert.*;
import java.util.Map;
import org.junit.Test;

public class InventarioTest {
	/** Objetivo: gasto atômico. Entrada: ferro suficiente, peles insuficientes. Saída esperada: ferro preservado. */
	@Test public void gastoInsuficienteNaoConsomeOutroTipo() {
		Inventario i = new Inventario(); i.receberTesouro(TipoTesouro.FERRO);
		assertFalse(i.gastarTesouros(Map.of(TipoTesouro.FERRO, 1, TipoTesouro.PELES, 1)));
		assertEquals(1, i.contarTesouro(TipoTesouro.FERRO));
		assertFalse(i.gastarTesouros(Map.of(TipoTesouro.FERRO, -1)));
		assertTrue(i.gastarTesouros(Map.of(TipoTesouro.FERRO, 1))); assertEquals(0, i.totalTesouros());
	}
	/** Objetivo: respeitar cor, capacidade e unicidade física. Entrada: peça em coluna errada, repetida, quinta peça. Saída esperada: recusas. */
	@Test public void melhoriaRespeitaCorCapacidadeEIdentidade() {
		Inventario i = new Inventario(); Melhoria m = new Melhoria(Coluna.VERDE, Melhoria.Simbolo.MAO);
		assertFalse(i.adicionarMelhoria(m, Coluna.AMARELA, 0));
		assertTrue(i.adicionarMelhoria(m, Coluna.BRANCA, 0));
		assertFalse(i.adicionarMelhoria(m, Coluna.VERDE, 0));
		for (int n = 0; n < 4; n++) ObjetivosTest.adicionar(i, Coluna.VERDE, Melhoria.Simbolo.MAO);
		assertFalse(i.adicionarMelhoria(new Melhoria(Coluna.VERDE, Melhoria.Simbolo.MAO), Coluna.VERDE, 0));
		assertThrows(UnsupportedOperationException.class, () -> i.getMelhorias(Coluna.VERDE).clear());
	}
	/** Objetivo: validar pedidos. Entrada: pares específicos, três quaisquer e três diferentes em K. Saída esperada: só ofertas corretas. */
	@Test public void pedidosDosConselheirosExigemCombinacaoCorreta() {
		PartidaTest.Cenario c = PartidaTest.padrao();
		Conselheiro par = new Conselheiro(c.tabuleiro.getParada("D"), Map.of(TipoTesouro.PELES, 2), TipoBonus.MOEDAS);
		assertTrue(par.aceita(Map.of(TipoTesouro.PELES, 2)));
		assertFalse(par.aceita(Map.of(TipoTesouro.FERRO, 2)));
		assertFalse(par.aceita(Map.of(TipoTesouro.PELES, 3)));
		Conselheiro qualquer = new Conselheiro(c.tabuleiro.getParada("D"), Map.of(), TipoBonus.TESOURO);
		assertTrue(qualquer.aceita(Map.of(TipoTesouro.FERRO, 3)));
		assertTrue(qualquer.aceita(Map.of(TipoTesouro.FERRO, 1, TipoTesouro.PELES, 2)));
		assertFalse(qualquer.aceita(Map.of(TipoTesouro.FERRO, 2)));
		assertThrows(IllegalArgumentException.class, () -> new Conselheiro(c.tabuleiro.getKarakorum(), Map.of(), TipoBonus.VOTO));
	}
	/** Objetivo: efeitos impressos. Entrada: todos os símbolos. Saída esperada: dois cavalos, uma mão ou uma unidade virtual. */
	@Test public void efeitosDasMelhoriasCorrespondemAosSimbolos() {
		assertEquals(2, new Melhoria(Coluna.VERDE, Melhoria.Simbolo.CAVALOS).getEfeitos().getCavalos());
		assertEquals(1, new Melhoria(Coluna.VERDE, Melhoria.Simbolo.MAO).getEfeitos().getMaos());
		for (TipoTributo tipo : TipoTributo.values()) {
			Melhoria m = new Melhoria(Coluna.VERDE, Melhoria.Simbolo.valueOf(tipo.name()));
			assertEquals(Integer.valueOf(1), m.getEfeitos().getRecursosVirtuais().get(tipo)); assertNull(m.getTesouroVirtual());
		}
		for (TipoTesouro tipo : TipoTesouro.values()) {
			Melhoria m = new Melhoria(Coluna.VERDE, Melhoria.Simbolo.valueOf(tipo.name()));
			assertEquals(tipo, m.getTesouroVirtual()); assertTrue(m.getEfeitos().getRecursosVirtuais().isEmpty());
		}
	}
}
