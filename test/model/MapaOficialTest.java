package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes estruturais do mapa montado por MapaOficial. Eles verificam
 * o que o manual garante (contagens, conectividade, simetria), e continuarão
 * valendo quando o grupo corrigir as rotas a partir do tabuleiro físico.
 */
public class MapaOficialTest {

	private Tabuleiro tab;

	@Before
	public void setUp() {
		tab = MapaOficial.criar();
	}

	/**
	 * Objetivo: o mapa tem 9 cidades, 3 por região, cada uma adjacente a pelo
	 * menos uma parada da sua própria região.
	 */
	@Test
	public void noveCidadesTresPorRegiao() {
		assertEquals(9, tab.getCidades().size());
		for (Regiao r : Regiao.values()) {
			int n = 0;
			for (Cidade c : tab.getCidades()) {
				if (c.getRegiao() == r) {
					n++;
					boolean temParadaDaRegiao = false;
					for (Parada p : c.getParadasAdjacentes()) {
						if (p.getRegiao() == r) temParadaDaRegiao = true;
					}
					assertTrue(c + " sem parada na sua região", temParadaDaRegiao);
				}
			}
			assertEquals(r + " deveria ter 3 cidades", 3, n);
		}
	}

	/**
	 * Objetivo: existem exatamente 7 paradas de conselheiro (6 duplas +
	 * Karakorum) e Karakorum não pertence a região nenhuma.
	 */
	@Test
	public void seteConselheirosIncluindoKarakorum() {
		Set<Parada> cons = tab.paradasDeConselheiro();
		assertEquals(7, cons.size());
		assertNotNull(tab.getKarakorum());
		assertTrue(cons.contains(tab.getKarakorum()));
		assertEquals(null, tab.getKarakorum().getRegiao());
		int duplas = 0;
		for (Parada p : cons) {
			if (p.isDupla()) duplas++;
		}
		assertEquals(6, duplas);
	}

	/**
	 * Objetivo: todas as rotas são simétricas e nenhuma parada é isolada.
	 */
	@Test
	public void rotasSimetricasESemParadaIsolada() {
		for (Parada p : tab.getParadas()) {
			Set<Parada> viz = tab.vizinhas(p);
			assertTrue(p + " está isolada", viz.size() >= 1);
			for (Parada q : viz) {
				assertTrue("Rota assimétrica " + p + " -> " + q, tab.saoVizinhas(q, p));
			}
		}
	}

	/**
	 * Objetivo: o mapa é conexo: partindo de Karakorum (onde todos os peões
	 * começam) é possível chegar a qualquer parada por rotas.
	 */
	@Test
	public void todasAsParadasAlcancaveisDeKarakorum() {
		Set<Parada> visitadas = new HashSet<>();
		Deque<Parada> fila = new ArrayDeque<>();
		fila.add(tab.getKarakorum());
		visitadas.add(tab.getKarakorum());
		while (!fila.isEmpty()) {
			Parada p = fila.poll();
			for (Parada q : tab.vizinhas(p)) {
				if (visitadas.add(q)) fila.add(q);
			}
		}
		assertEquals(tab.getParadas().size(), visitadas.size());
	}

	/**
	 * Objetivo: há 4 províncias do Khan, cada uma apontando para exatamente 2
	 * vizinhas; e, na preparação, toda província tem 1 peça de tributo.
	 */
	@Test
	public void quatroProvinciasDoKhanETributoInicial() {
		Set<Provincia> khan = tab.provinciasDoKhan();
		assertEquals(4, khan.size());
		for (Provincia p : khan) {
			assertEquals(p + " deve apontar para 2 vizinhas", 2, p.getVizinhasDoKhan().size());
		}
		for (Provincia p : tab.getProvincias()) {
			assertEquals(p + " deveria começar com 1 peça", 1, p.getPecas());
			assertTrue(p + " sem parada adjacente", p.getParadasAdjacentes().size() >= 1);
		}
	}
}
