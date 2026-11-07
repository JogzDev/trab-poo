package model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Test;

/** Integração da preparação real com a fachada. O cadastro abaixo é sintético. */
public class PreparacaoCompletaTest {
	private final ModelAPI api = ModelAPI.getInstance();
	private final Contador observador = new Contador();

	private static class Contador implements ObservadorIF {
		int chamadas;
		@Override public void notify(ObservadoIF observado) { chamadas++; }
	}

	@After public void limpar() { api.remove(observador); }

	/** Peças para testes de integração; não representa a distribuição oficial de cores. */
	public static List<String> cadastro() {
		List<String> linhas = new ArrayList<>(List.of(
			"# Cadastro sintético de teste",
			"MELHORIA;VERMELHA;MOEDA;8", "MELHORIA;VERDE;ESPADA;8",
			"MELHORIA;AMARELA;YURT;8", "MELHORIA;VERMELHA;MAO;6",
			"MELHORIA;VERDE;CAVALOS;9", "MELHORIA;AMARELA;FERRO;1",
			"MELHORIA;VERMELHA;PELES;1", "MELHORIA;VERDE;GADO;1",
			"MELHORIA;AMARELA;TECIDO;1", "MELHORIA;VERMELHA;ESPECIARIAS;1"));
		List<Parada> paradas = new ArrayList<>(MapaOficial.criar().paradasDeConselheiro());
		paradas.sort(Comparator.comparing(Parada::getNome));
		int tipo = 0;
		for (Parada parada : paradas) {
			String pedido = parada.isKarakorum() ? "FERRO:1,PELES:1,GADO:1"
					: tipo < 5 ? TipoTesouro.values()[tipo++].name() + ":2" : "QUAISQUER";
			linhas.add("CONSELHEIRO;" + parada.getNome() + ";" + pedido);
		}
		return linhas;
	}

	/** Preparação impede ações; última escolha inicia o jogo e notifica uma só vez. */
	@Test public void escolhasConcluemPreparacaoDeDoisACincoJogadores() {
		for (int quantidade = 2; quantidade <= 5; quantidade++) {
			List<String> nomes = new ArrayList<>(), herdeiros = new ArrayList<>();
			for (int i = 0; i < quantidade; i++) {
				nomes.add("Jogador " + i);
				herdeiros.add(Herdeiro.values()[i].name());
			}
			assertTrue(api.novaPartidaCompleta(nomes, herdeiros, cadastro()));
			assertTrue(api.isPreparacao());
			assertEquals(4, api.getCartasAbertas().size());
			assertTrue(api.getColunasDisponiveis().isEmpty());
			assertFalse(api.ativarColuna("VERMELHA"));
			assertFalse(api.moverPara("Sarai Sudoeste"));
			assertFalse(api.escolherPeao(1));
			assertFalse(api.terminarTurno());
			api.add(observador);
			observador.chamadas = 0;
			for (int i = 0; i < quantidade; i++) {
				assertEquals(i, api.getJogadorPreparando());
				assertEquals(2, api.getOpcoesCartaSecreta().size());
				assertTrue(api.getOpcoesCartaSecreta().get(0).contains("1/2/3 votos"));
				assertFalse(api.escolherCartaSecreta(-1));
				assertFalse(api.escolherCartaSecreta(2));
				assertEquals(i, observador.chamadas);
				assertTrue(api.escolherCartaSecreta(i % 2));
				assertEquals(i + 1, observador.chamadas);
				assertEquals(i + 1 < quantidade, api.isPreparacao());
			}
			assertEquals(-1, api.getJogadorPreparando());
			assertTrue(api.getOpcoesCartaSecreta().isEmpty());
			assertFalse(api.escolherCartaSecreta(0));
			assertEquals(0, api.getJogadorDaVez());
			assertEquals(4, api.getColunasDisponiveis().size());
			assertTrue(api.ativarColuna("VERMELHA"));
			api.remove(observador);
		}
	}

	/** Configuração recusada preserva inclusive a preparação e as opções anteriores. */
	@Test public void cadastroInvalidoPreservaPartidaENaoNotifica() {
		assertTrue(api.novaPartidaCompleta(List.of("Ana", "Bia"), List.of("JOCHI", "ALTANI"), cadastro()));
		List<String> cartas = api.getOpcoesCartaSecreta();
		api.add(observador);
		assertFalse(api.novaPartidaCompleta(List.of("X", "Y"), List.of("TOLUI", "OGEDEI"), List.of()));
		assertFalse(api.getErroConfiguracao().isBlank());
		assertEquals("Ana", api.getNomeJogador(0));
		assertEquals(cartas, api.getOpcoesCartaSecreta());
		assertEquals(0, api.getJogadorPreparando());
		assertEquals(0, observador.chamadas);
	}

	/** O parser recusa peças e pedidos inválidos em vez de completar dados ausentes. */
	@Test public void validarQuantidadesCoresEPedidos() {
		for (String invalida : List.of("MELHORIA;BRANCA;MOEDA;8", "MELHORIA;VERMELHA;MOEDA;0",
				"MELHORIA;VERMELHA;MOEDA;9", "MELHORIA;VERMELHA;INVALIDO;8",
				"MELHORIA;VERMELHA;MOEDA;999999999999", "OUTRO;A;B;1")) {
			List<String> linhas = cadastro();
			linhas.set(1, invalida);
			assertThrows(IllegalArgumentException.class, () -> ConfiguracaoPecas.criar(linhas, MapaOficial.criar(), new Random(1)));
		}
		List<String> repetido = cadastro();
		repetido.add(repetido.get(repetido.size() - 1));
		assertThrows(IllegalArgumentException.class, () -> ConfiguracaoPecas.criar(repetido, MapaOficial.criar(), new Random(1)));
		List<String> ausente = cadastro();
		ausente.remove(ausente.size() - 1);
		assertThrows(IllegalArgumentException.class, () -> ConfiguracaoPecas.criar(ausente, MapaOficial.criar(), new Random(1)));
		List<String> capital = cadastro();
		capital.replaceAll(l -> l.startsWith("CONSELHEIRO;Karakorum;") ? "CONSELHEIRO;Karakorum;QUAISQUER" : l);
		assertThrows(IllegalArgumentException.class, () -> ConfiguracaoPecas.criar(capital, MapaOficial.criar(), new Random(1)));
	}

	/** Jochi e os tesouros usam os componentes reais já durante a preparação. */
	@Test public void preparaMercadoInventariosECidades() {
		Tabuleiro tabuleiro = MapaOficial.criar();
		ComponentesKhan componentes = ConfiguracaoPecas.criar(cadastro(), tabuleiro, new Random(1));
		Jogador jochi = new Jogador("Ana", Cor.AZUL, Herdeiro.JOCHI);
		Jogador altani = new Jogador("Bia", Cor.VERDE, Herdeiro.ALTANI);
		Partida partida = new Partida(tabuleiro, List.of(jochi, altani), componentes, new Random(1));
		assertFalse(partida.iniciar());
		assertEquals(5, componentes.getMercado().size());
		assertEquals(38, componentes.melhoriasNaReserva());
		assertEquals(1, componentes.inventarioDe(jochi).contarMelhorias(Coluna.BRANCA));
		assertEquals(0, componentes.inventarioDe(altani).contarMelhorias(Coluna.BRANCA));
		int tesouros = 0;
		for (Cidade cidade : tabuleiro.getCidades()) {
			assertEquals(cidade.isRevelada() ? 4 : 0, componentes.tesourosEm(cidade).size());
			tesouros += componentes.tesourosEm(cidade).size();
		}
		assertEquals(12, tesouros);
		assertEquals(28, componentes.tesourosNaReserva());
		for (Jogador jogador : List.of(jochi, altani)) {
			Carta escolhida = componentes.cartasDe(jogador).get(1);
			assertTrue(componentes.escolherCartaSecreta(jogador, escolhida));
			assertEquals(List.of(escolhida), componentes.cartasDe(jogador));
			assertFalse(componentes.escolherCartaSecreta(jogador, escolhida));
		}
		assertEquals(2, componentes.getDescarteCartas().size());
		assertTrue(partida.iniciar());
	}

	/** Alternar para a demonstração limpa os dados da preparação anterior. */
	@Test public void demonstracaoNaoHerdaComponentesDaPreparacao() {
		assertTrue(api.novaPartidaCompleta(List.of("Ana", "Bia"), List.of("JOCHI", "ALTANI"), cadastro()));
		assertTrue(api.novaPartida(List.of("X", "Y"), List.of("TOLUI", "OGEDEI")));
		assertFalse(api.isPreparacao());
		assertEquals(-1, api.getJogadorPreparando());
		assertTrue(api.getCartasAbertas().isEmpty());
		assertTrue(api.getOpcoesCartaSecreta().isEmpty());
	}
}
