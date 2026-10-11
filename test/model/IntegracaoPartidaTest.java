package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
		assertEquals("Cavalos restantes no turno", 3, c.partida.getTurno().getCavalos());
		assertEquals("Mãos restantes no turno", 3, c.partida.getTurno().getMaos());
		assertEquals("Espadas virtuais restantes no turno", 1, c.partida.getTurno().getRecursoVirtual(TipoTributo.ESPADA));
		assertEquals("Votos de Azul", 1, c.azul().getVotos());
		assertEquals("Chamadas ao hook de cartas", 1, componentes.avaliacoes);
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
		assertEquals("Jogador da vez", c.jogadores.get(1), c.partida.getJogadorDaVez());
		assertEquals("Moedas virtuais restantes no turno", 0, c.partida.getTurno().getRecursoVirtual(TipoTributo.MOEDA));
		assertEquals("Moedas físicas de Azul", 1, c.azul().contarTributo(TipoTributo.MOEDA));
		assertEquals("Moedas físicas de Vermelho", 1, c.jogadores.get(1).contarTributo(TipoTributo.MOEDA));
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
		boolean resultadoDeGastarTributo = c.partida.gastarTributo(TipoTributo.MOEDA, 1);
		assertTrue("O pagamento deve ser aceito com recursos suficientes", resultadoDeGastarTributo);
		assertEquals("Moedas virtuais restantes no turno", 0, c.partida.getTurno().getRecursoVirtual(TipoTributo.MOEDA));
		assertEquals("Moedas físicas de Azul", 1, c.azul().contarTributo(TipoTributo.MOEDA));
		assertEquals("Estoque físico de moedas no banco", 11, c.partida.getEstoque(TipoTributo.MOEDA));
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
		boolean resultadoDeGastarTributo = c.partida.gastarTributo(TipoTributo.MOEDA, 3);
		assertFalse("O pagamento deve ser recusado sem consumir recursos", resultadoDeGastarTributo);
		assertEquals("Moedas virtuais restantes no turno", 1, c.partida.getTurno().getRecursoVirtual(TipoTributo.MOEDA));
		assertEquals("Moedas físicas de Azul", 1, c.azul().contarTributo(TipoTributo.MOEDA));
		assertEquals("Estoque físico de moedas no banco", 11, c.partida.getEstoque(TipoTributo.MOEDA));
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
		boolean resultadoDeTerminarTurno = c.partida.terminarTurno();
		assertFalse("A escolha obrigatória da parte 2 deve impedir encerrar o turno", resultadoDeTerminarTurno);
		assertEquals("Jogador da vez", c.azul(), c.partida.getJogadorDaVez());
		assertEquals("Número do turno corrente", 1, c.partida.getTurno().getNumero());
		assertEquals("Fase da partida", Fase.EM_ANDAMENTO, c.partida.getFase());
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
		Fase faseEncerrada = c.partida.getFase();
		int rodadaEncerrada = c.partida.getRodada();
		int pontuacoesAoEncerrar = componentes.pontuacoes;
		boolean resultadoDeTerminarTurno = c.partida.terminarTurno();
		boolean resultadoDeAtivarColuna = c.partida.ativarColuna(Coluna.BRANCA);
		assertEquals("Fase antes do último jogador da rodada final", Fase.EM_ANDAMENTO, antesDoUltimo);
		assertEquals("Fase da partida", Fase.ENCERRADA, faseEncerrada);
		assertEquals("Rodada corrente", 2, rodadaEncerrada);
		assertEquals("Chamadas à pontuação final", 1, pontuacoesAoEncerrar);
		assertFalse("Uma partida encerrada não deve aceitar outra tentativa de terminar o turno", resultadoDeTerminarTurno);
		assertFalse("Uma partida encerrada não deve aceitar ativação da coluna branca", resultadoDeAtivarColuna);
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
		assertEquals("Rodada final agendada", 2, c.partida.getRodadaFinal());
		assertEquals("Fase antes do último jogador da rodada final", Fase.EM_ANDAMENTO, antesDoUltimo);
		assertEquals("Fase da partida", Fase.ENCERRADA, c.partida.getFase());
		assertEquals("Chamadas à pontuação final", 1, componentes.pontuacoes);
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
		assertEquals("Fase da partida", Fase.EM_ANDAMENTO, c.partida.getFase());
		assertEquals("Rodada final agendada", -1, c.partida.getRodadaFinal());
		assertEquals("Rodada corrente", 2, c.partida.getRodada());
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
		boolean resultadoDeMoverKhanParaMelhorias = c.partida.moverKhanParaMelhorias();
		assertFalse("A recusa do componente de melhorias deve impedir mover o Khan", resultadoDeMoverKhanParaMelhorias);
		assertEquals("Posição do Khan", null, c.partida.getPosicaoKhan());
		assertTrue("A obrigação do Khan deve continuar pendente", c.partida.getTurno().isKhanPendente());
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
		assertTrue("O segundo ataque com espadas deve ser aceito", segundoAtaque);
		assertEquals("Espadas físicas de Azul", 0, c.azul().contarTributo(TipoTributo.ESPADA));
		assertEquals("Moedas físicas de Azul", 0, c.azul().contarTributo(TipoTributo.MOEDA));
		assertEquals("Tesouros restantes na cidade atacada", 1, cidade.getTesouros());
		assertEquals("Tesouros recebidos por Azul", 3, c.partida.getTesourosRecebidos(c.azul()));
		assertEquals("Tesouros transferidos ao componente da parte 2", 3, componentes.tesouros);
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
		assertEquals("Posição coletiva da ficha neutra no Kurultai", 10, c.partida.getPosicaoKurultai());
		assertEquals("Rodada final agendada", 2, c.partida.getRodadaFinal());
		assertEquals("Votos de Azul", 5, c.azul().getVotos());
		assertEquals("Votos de Vermelho", 5, c.jogadores.get(1).getVotos());
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
		boolean resultadoDeEscolherPeao = c.partida.escolherPeao(1);
		assertFalse("A ação complementar deve impedir trocar o peão de Altani", resultadoDeEscolherPeao);
		assertEquals("Chamadas ao hook de cartas", 1, componentes.avaliacoes);
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
		assertEquals("Rodada final antes da conquista da última cidade", -1, antesDaUltima);
		assertTrue("A última cidade deve ser conquistada", conquistou);
		assertEquals("Rodada final agendada", 2, c.partida.getRodadaFinal());
		assertEquals("Posição coletiva da ficha neutra no Kurultai", 0, c.partida.getPosicaoKurultai());
		assertEquals("Cidades disponíveis para ataque", 0, c.reveladas().size());
		assertEquals("Yurts disponíveis de Azul", 8, c.azul().getYurtsDisponiveis());
		assertEquals("Fase da partida", Fase.EM_ANDAMENTO, c.partida.getFase());
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
		assertTrue("O Khan deve ser aceito na área de melhorias pelo componente da parte 2", moveu);
		assertEquals("Posição do Khan", Partida.AREA_MELHORIAS, c.partida.getPosicaoKhan());
		assertFalse("A obrigação do Khan deve estar resolvida", c.partida.getTurno().isKhanPendente());
		assertEquals("Moedas físicas de Azul", 2, c.azul().contarTributo(TipoTributo.MOEDA));
		assertEquals("Espadas físicas de Azul", 1, c.azul().contarTributo(TipoTributo.ESPADA));
		assertEquals("Estoque físico de moedas no banco", 10, c.partida.getEstoque(TipoTributo.MOEDA));
		assertEquals("Estoque físico de espadas no banco", 8, c.partida.getEstoque(TipoTributo.ESPADA));
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
		assertTrue("O turno sem escolhas pendentes deve ser encerrado", terminou);
		assertEquals("Chamadas ao hook de cartas", 1, avaliacoes[0]);
		assertFalse("O encerramento não deve abrir uma escolha pendente", pendente[0]);
		assertEquals("Jogador da vez", c.jogadores.get(1), c.partida.getJogadorDaVez());
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
