package model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.junit.Test;

/** Exercita os pontos de integração da parte 2, recursos e encerramento. */
public class IntegracaoPartidaTest {
	private static final int TEMPO_LIMITE = 1000;

	/**
	 * Objetivo: aplicar melhorias e avaliar cartas imediatamente na ativação.
	 * Entrada: três espadas físicas; melhoria com uma espada virtual, um cavalo
	 * e duas mãos; hook concede um voto quando as quatro espadas aparecem.
	 * Saída esperada: ações somadas e voto recebido antes de outra jogada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void ativacaoSomaMelhoriasEAvaliaCartaComRecursoVirtual() {
		PartidaTest.Cenario c = PartidaTest.padrao();
		c.azul().adicionarTributo(TipoTributo.ESPADA, 3);
		ComponentesTeste componentes = new ComponentesTeste();
		Map<TipoTributo, Integer> virtuais = new EnumMap<>(TipoTributo.class);
		virtuais.put(TipoTributo.ESPADA, 1);
		componentes.efeitos = new EfeitosColuna(1, 2, virtuais);
		componentes.cartaQuatroEspadas = true;
		c.iniciar(componentes);
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.AMARELA), "ativar melhoria antiga");
		assertEquals("A ativação deve chamar a parte 2 depois de disponibilizar os recursos virtuais",
				Arrays.asList(3, 3, 1, 1, 1), Arrays.asList(c.partida.getTurno().getCavalos(), c.partida.getTurno().getMaos(),
						c.partida.getTurno().getRecursoVirtual(TipoTributo.ESPADA), c.azul().getVotos(), componentes.avaliacoes));
	}

	/**
	 * Objetivo: expirar os recursos virtuais ao trocar de jogador.
	 * Entrada: Azul ativa verde com moeda virtual e encerra sem gastá-la.
	 * Saída esperada: novo turno sem moeda virtual; moeda física de Azul preservada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void recursosVirtuaisExpiramSemVirarPecasFisicas() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.VERDE), "moeda virtual de Chagatai");
		PartidaTest.exigir(c.partida.getTurno().getRecursoVirtual(TipoTributo.MOEDA) == 1, "virtual disponível no turno azul");
		PartidaTest.exigir(c.partida.terminarTurno(), "trocar de jogador");
		assertEquals("A moeda virtual expira e não aumenta o saldo físico de ninguém",
				Arrays.asList(c.jogadores.get(1), 0, 1, 1), Arrays.asList(c.partida.getJogadorDaVez(),
						c.partida.getTurno().getRecursoVirtual(TipoTributo.MOEDA), c.azul().contarTributo(TipoTributo.MOEDA),
						c.jogadores.get(1).contarTributo(TipoTributo.MOEDA)));
	}

	/**
	 * Objetivo: pagar recursos virtuais antes das peças físicas.
	 * Entrada: uma moeda física e uma virtual da verde; pagar uma moeda.
	 * Saída esperada: virtual zero, física um e banco físico inalterado.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void pagamentoUsaVirtualPrimeiroSemCriarPecaNoBanco() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.VERDE), "moeda virtual");
		assertEquals("Usar moeda virtual não devolve uma moeda física ao banco",
				Arrays.asList(true, 0, 1, 11), Arrays.asList(c.partida.gastarTributo(TipoTributo.MOEDA, 1),
						c.partida.getTurno().getRecursoVirtual(TipoTributo.MOEDA), c.azul().contarTributo(TipoTributo.MOEDA),
						c.partida.getEstoque(TipoTributo.MOEDA)));
	}

	/**
	 * Objetivo: não consumir o virtual parcialmente em pagamento insuficiente.
	 * Entrada: uma moeda física mais uma virtual; tentativa de pagar três.
	 * Saída esperada: recusa e ambos os saldos e banco preservados.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void pagamentoInsuficientePreservaRecursosFisicosEVirtuais() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.VERDE), "moeda virtual");
		assertEquals("A validação deve preceder o consumo de qualquer recurso",
				Arrays.asList(false, 1, 1, 11), Arrays.asList(c.partida.gastarTributo(TipoTributo.MOEDA, 3),
						c.partida.getTurno().getRecursoVirtual(TipoTributo.MOEDA), c.azul().contarTributo(TipoTributo.MOEDA),
						c.partida.getEstoque(TipoTributo.MOEDA)));
	}

	/**
	 * Objetivo: respeitar escolhas obrigatórias mantidas pela parte 2.
	 * Entrada: coluna amarela ativa e hook informa uma escolha de bônus pendente.
	 * Saída esperada: não encerrar; jogador, turno e fase preservados.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void escolhaPendenteDaParteDoisImpedeTrocaDeTurno() {
		PartidaTest.Cenario c = PartidaTest.padrao();
		ComponentesTeste componentes = new ComponentesTeste();
		componentes.escolhaPendente = true;
		c.iniciar(componentes);
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.AMARELA), "coluna sem Khan");
		assertEquals("Uma escolha obrigatória impede passar a vez",
				Arrays.asList(false, c.azul(), 1, Fase.EM_ANDAMENTO), Arrays.asList(c.partida.terminarTurno(),
						c.partida.getJogadorDaVez(), c.partida.getTurno().getNumero(), c.partida.getFase()));
	}

	/**
	 * Objetivo: completar a rodada do disparo e depois uma rodada inteira.
	 * Entrada: hook concede dez votos a Azul na primeira ativação da rodada 1.
	 * Saída esperada: fim somente depois de Azul/Vermelho da rodada 2; pontuação
	 * final uma vez e ações após o encerramento recusadas.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void disparoNoPrimeiroJogadorCompletaRodadaAtualEMaisUma() {
		PartidaTest.Cenario c = PartidaTest.padrao();
		ComponentesTeste componentes = new ComponentesTeste();
		componentes.premio = 10;
		componentes.corPremiada = Cor.AZUL;
		c.iniciar(componentes);
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.AMARELA), "disparar fim com carta aberta");
		PartidaTest.exigir(c.partida.getRodadaFinal() == 2, "agendar rodada final 2");
		PartidaTest.turnoVazio(c.partida);
		PartidaTest.turnoVazio(c.partida);
		PartidaTest.turnoVazio(c.partida);
		Fase antesDoUltimo = c.partida.getFase();
		PartidaTest.turnoVazio(c.partida);
		assertEquals("O jogo deve aguardar o último jogador da rodada final e pontuar uma única vez",
				Arrays.asList(Fase.EM_ANDAMENTO, Fase.ENCERRADA, 2, 1, false, false),
				Arrays.asList(antesDoUltimo, c.partida.getFase(), c.partida.getRodada(), componentes.pontuacoes,
						c.partida.terminarTurno(), c.partida.ativarColuna(Coluna.BRANCA)));
	}

	/**
	 * Objetivo: manter a duração correta quando o último jogador dispara o fim.
	 * Entrada: Azul termina; Vermelho recebe dez votos na rodada 1.
	 * Saída esperada: ainda seguem os dois jogadores da rodada 2 antes do fim.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void disparoNoUltimoJogadorAindaPermiteRodadaFinalCompleta() {
		PartidaTest.Cenario c = PartidaTest.padrao();
		ComponentesTeste componentes = new ComponentesTeste();
		componentes.premio = 10;
		componentes.corPremiada = Cor.VERMELHO;
		c.iniciar(componentes);
		PartidaTest.turnoVazio(c.partida);
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.VERDE), "disparo por Vermelho");
		PartidaTest.turnoVazio(c.partida);
		PartidaTest.turnoVazio(c.partida);
		Fase antesDoUltimo = c.partida.getFase();
		PartidaTest.turnoVazio(c.partida);
		assertEquals("O disparo no fim da rodada não elimina a rodada completa seguinte",
				Arrays.asList(2, Fase.EM_ANDAMENTO, Fase.ENCERRADA, 1),
				Arrays.asList(c.partida.getRodadaFinal(), antesDoUltimo, c.partida.getFase(), componentes.pontuacoes));
	}

	/**
	 * Objetivo: não tratar um mapa sem cidades como todas as cidades conquistadas.
	 * Entrada: mapa mínimo sem cidades, com dois jogadores e nenhum voto.
	 * Saída esperada: dois turnos normais, fase em andamento e nenhuma rodada final.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void mapaSemCidadesNaoDisparaFimAutomaticamente() {
		PartidaTest.Cenario c = new PartidaTest.Cenario(false, Herdeiro.CHAGATAI, Herdeiro.OGEDEI).iniciar();
		PartidaTest.turnoVazio(c.partida);
		PartidaTest.turnoVazio(c.partida);
		assertEquals("O conjunto vazio de cidades não deve encerrar a partida de teste",
				Arrays.asList(Fase.EM_ANDAMENTO, -1, 2), Arrays.asList(c.partida.getFase(), c.partida.getRodadaFinal(), c.partida.getRodada()));
	}

	/**
	 * Objetivo: respeitar a recusa do componente que administra melhorias.
	 * Entrada: coluna com Khan; hook de melhorias retorna false.
	 * Saída esperada: posição do Khan não muda e sua obrigação continua pendente.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void recusaDoHookDeMelhoriasPreservaKhanPendente() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar(new ComponentesTeste());
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.VERMELHA), "coluna com Khan");
		assertEquals("A parte 3 só resolve o Khan depois que a parte 2 aceita a ação",
				Arrays.asList(false, null, true), Arrays.asList(c.partida.moverKhanParaMelhorias(),
						c.partida.getPosicaoKhan(), c.partida.getTurno().isKhanPendente()));
	}

	/**
	 * Objetivo: a compra de Ogedei não aumenta o custo do primeiro ataque com espada.
	 * Entrada: moeda inicial e três espadas; comprar e atacar duas vezes a mesma cidade.
	 * Saída esperada: a compra custa uma moeda e os ataques 1 + 2 espadas;
	 * três tesouros transferidos pelo hook e uma peça restante na cidade.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void ogedeiCompraComMoedaEDepoisAtacaPorUmaEDuasEspadas() {
		PartidaTest.Cenario c = new PartidaTest.Cenario(Herdeiro.OGEDEI, Herdeiro.CHAGATAI);
		c.azul().adicionarTributo(TipoTributo.ESPADA, 3);
		ComponentesTeste componentes = new ComponentesTeste();
		c.iniciar(componentes);
		Cidade cidade = c.revelada();
		PartidaTest.exigir(c.partida.pegarTesouroComMoeda(cidade.getNome()), "compra de Ogedei");
		PartidaTest.exigir(c.partida.atacarCidade(cidade.getNome()), "primeiro ataque deve custar uma espada");
		boolean segundoAtaque = c.partida.atacarCidade(cidade.getNome());
		assertEquals("A moeda de Ogedei não é um ataque com espada e o hook recebe cada peça tomada",
				Arrays.asList(true, 0, 0, 1, 3, 3), Arrays.asList(segundoAtaque,
						c.azul().contarTributo(TipoTributo.ESPADA), c.azul().contarTributo(TipoTributo.MOEDA),
						cidade.getTesouros(), c.partida.getTesourosRecebidos(c.azul()), componentes.tesouros));
	}

	/**
	 * Objetivo: a ficha neutra soma votos de todos os jogadores.
	 * Entrada: em partida de dois jogadores, Azul e Vermelho recebem cinco votos cada.
	 * Saída esperada: dez na ficha neutra e rodada final 2, apesar de ninguém ter dez.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void kurultaiSomaCincoVotosDeCadaJogadorParaDispararFim() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		c.azul().adicionarVotos(5);
		c.jogadores.get(1).adicionarVotos(5);
		PartidaTest.exigir(c.partida.concluirAcaoComplementar(false), "registrar votos recebidos da parte 2");
		assertEquals("O limite de dez é coletivo, não o máximo individual",
				Arrays.asList(10, 2, 5, 5), Arrays.asList(c.partida.getPosicaoKurultai(), c.partida.getRodadaFinal(),
						c.azul().getVotos(), c.jogadores.get(1).getVotos()));
	}

	/**
	 * Objetivo: a entrega implementada pela parte 2 também fixa o peão de Altani.
	 * Entrada: componente conclui uma ação complementar que usou o peão.
	 * Saída esperada: hook avaliado uma vez e troca para outro peão recusada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void acaoComplementarComPeaoBloqueiaTrocaDeAltani() {
		PartidaTest.Cenario c = new PartidaTest.Cenario(Herdeiro.ALTANI, Herdeiro.OGEDEI);
		ComponentesTeste componentes = new ComponentesTeste();
		c.iniciar(componentes);
		PartidaTest.exigir(c.partida.concluirAcaoComplementar(true), "entrega válida implementada pela parte 2");
		assertEquals("A integração de uma entrega deve manter um único peão ativo no turno",
				Arrays.asList(false, 1), Arrays.asList(c.partida.escolherPeao(1), componentes.avaliacoes));
	}

	/**
	 * Objetivo: a conquista de todas as cidades também dispara a rodada final.
	 * Entrada: quatro cidades; Azul tem quatro espadas e conquista cada uma
	 * tomando sua última peça, inclusive a cidade revelada após a primeira conquista.
	 * Saída esperada: antes da quarta não há rodada final; após ela, rodada final 2,
	 * zero votos na ficha neutra e quatro yurts de conquista gastos.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void conquistarTodasAsCidadesDisparaFimMesmoSemVotos() {
		PartidaTest.Cenario c = PartidaTest.padrao();
		c.azul().adicionarTributo(TipoTributo.ESPADA, 4);
		c.iniciar();
		for (int i = 0; i < 3; i++) {
			Cidade cidade = c.revelada();
			PartidaTest.deixarUltimoTesouro(cidade, c.azul());
			PartidaTest.exigir(c.partida.atacarCidade(cidade.getNome()), "conquistar uma das três primeiras cidades");
		}
		int antesDaUltima = c.partida.getRodadaFinal();
		Cidade ultima = c.revelada();
		PartidaTest.deixarUltimoTesouro(ultima, c.azul());
		boolean conquistou = c.partida.atacarCidade(ultima.getNome());
		assertEquals("Só a conquista da última cidade deve disparar o fim, independentemente do Kurultai",
				Arrays.asList(-1, true, 2, 0, 0, 8, Fase.EM_ANDAMENTO),
				Arrays.asList(antesDaUltima, conquistou, c.partida.getRodadaFinal(), c.partida.getPosicaoKurultai(),
						c.reveladas().size(), c.azul().getYurtsDisponiveis(), c.partida.getFase()));
	}

	/**
	 * Objetivo: integrar o Khan na área de melhorias com o banco físico da partida.
	 * Entrada: coluna vermelha; componente aceita o destino e entrega uma moeda
	 * e uma espada usando receberTributoDoEstoque.
	 * Saída esperada: posição na área de melhorias, obrigação resolvida e cada
	 * peça recebida descontada do banco.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void khanNaAreaDeMelhoriasRecebePecasFisicasEEncerraObrigacao() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar(new ComponentesPartida() {
			@Override public boolean moverKhanParaMelhorias(Partida partida) {
				PartidaTest.exigir(partida.receberTributoDoEstoque(TipoTributo.MOEDA, 1) == 1, "moeda do ícone de melhorias");
				PartidaTest.exigir(partida.receberTributoDoEstoque(TipoTributo.ESPADA, 1) == 1, "espada do ícone de melhorias");
				return true;
			}
		});
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.VERMELHA), "coluna com Khan");
		boolean moveu = c.partida.moverKhanParaMelhorias();
		assertEquals("A aceitação do destino deve resolver o Khan e conservar as peças físicas",
				Arrays.asList(true, Partida.AREA_MELHORIAS, false, 2, 1, 10, 8),
				Arrays.asList(moveu, c.partida.getPosicaoKhan(), c.partida.getTurno().isKhanPendente(),
						c.azul().contarTributo(TipoTributo.MOEDA), c.azul().contarTributo(TipoTributo.ESPADA),
						c.partida.getEstoque(TipoTributo.MOEDA), c.partida.getEstoque(TipoTributo.ESPADA)));
	}

	/**
	 * Objetivo: encerrar o turno sem avaliar as cartas novamente ou abrir escolhas.
	 * Entrada: a ativação chama o hook uma vez; uma segunda chamada indevida
	 * abriria uma escolha obrigatória de bônus na parte 2.
	 * Saída esperada: turno encerrado, uma única avaliação, nenhuma escolha nova
	 * e Vermelho como jogador da vez.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void encerrarTurnoNaoRepeteHookDeCartasNemAbreEscolha() {
		final int[] avaliacoes = { 0 };
		final boolean[] pendente = { false };
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar(new ComponentesPartida() {
			@Override public void aposAcao(Partida partida) {
				avaliacoes[0]++;
				if (avaliacoes[0] > 1) pendente[0] = true;
			}
			@Override public boolean temEscolhaPendente(Jogador jogador) { return pendente[0]; }
		});
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.AMARELA), "primeira avaliação de cartas após ativar");
		boolean terminou = c.partida.terminarTurno();
		assertEquals("O encerramento não deve reabrir o fluxo de ações e escolhas da parte 2",
				Arrays.asList(true, 1, false, c.jogadores.get(1)),
				Arrays.asList(terminou, avaliacoes[0], pendente[0], c.partida.getJogadorDaVez()));
	}

	/** Implementação controlada do contrato; nenhuma regra de carta real é duplicada. */
	private static final class ComponentesTeste implements ComponentesPartida {
		EfeitosColuna efeitos = EfeitosColuna.semEfeitos();
		boolean escolhaPendente;
		boolean cartaQuatroEspadas;
		boolean premiou;
		int premio;
		Cor corPremiada;
		int avaliacoes;
		int pontuacoes;
		int tesouros;
		@Override public EfeitosColuna efeitosDasMelhorias(Jogador jogador, Coluna coluna, int numeroTurno) { return efeitos; }
		@Override public boolean temEscolhaPendente(Jogador jogador) { return escolhaPendente; }
		@Override public void receberTesouro(Jogador jogador, Cidade cidade) { tesouros++; }
		@Override public void pontuarFimDeJogo(Partida partida) { pontuacoes++; }
		@Override public void aposAcao(Partida partida) {
			avaliacoes++;
			Jogador jogador = partida.getJogadorDaVez();
			if (premiou) return;
			if (cartaQuatroEspadas && jogador.contarTributo(TipoTributo.ESPADA)
					+ partida.getTurno().getRecursoVirtual(TipoTributo.ESPADA) >= 4) {
				jogador.adicionarVotos(1); premiou = true;
			} else if (premio > 0 && jogador.getCor() == corPremiada && partida.getTurno().getColuna() != null) {
				jogador.adicionarVotos(premio); premiou = true;
			}
		}
	}
}
