package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.junit.Test;

/** Testes das regras de preparação, ativação e ações da parte 3. */
public class PartidaTest {
	static final int TEMPO_LIMITE = 1000;

	/**
	 * Objetivo: preparar peças e banco físico sem criar tributos adicionais.
	 * Entrada: Tolui, Chagatai e Ogedei; quatro províncias com uma peça cada.
	 * Saída esperada: moedas 1/1/2, Tolui com yurt inicial e banco 9/9/9.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void preparacaoDistribuiMoedasPeoesToluiEBancoFisico() {
		Cenario c = new Cenario(Herdeiro.TOLUI, Herdeiro.CHAGATAI, Herdeiro.OGEDEI);
		c.criar(new ComponentesPartida() { });
		assertEquals("Moedas físicas de Azul", 1, c.azul().contarTributo(TipoTributo.MOEDA));
		assertEquals("Moedas físicas de Vermelho", 1, c.jogadores.get(1).contarTributo(TipoTributo.MOEDA));
		assertEquals("Moedas físicas do terceiro jogador", 2, c.jogadores.get(2).contarTributo(TipoTributo.MOEDA));
		assertEquals("Yurts disponíveis de Azul", 11, c.azul().getYurtsDisponiveis());
		assertTrue("Tolui deve ter seu yurt inicial em Karakorum", c.tabuleiro.getKarakorum().temYurtDe(Cor.AZUL));
		assertEquals("Posição do peão de Azul", "K", c.tabuleiro.posicaoDe(c.azul().getPeao(0)).getNome());
		assertEquals("Estoque físico de espadas no banco", 9, c.partida.getEstoque(TipoTributo.ESPADA));
		assertEquals("Estoque físico de tributos de yurt no banco", 9, c.partida.getEstoque(TipoTributo.YURT));
		assertEquals("Estoque físico de moedas no banco", 9, c.partida.getEstoque(TipoTributo.MOEDA));
		assertEquals("Fase da partida", Fase.PREPARACAO, c.partida.getFase());
	}

	/**
	 * Objetivo: não iniciar com escolhas da parte 2 pendentes.
	 * Entrada: componente informa preparação incompleta; tentar iniciar e ativar.
	 * Saída esperada: ambas as ações recusadas e fase de preparação preservada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void preparacaoPendenteImpedeInicioEAtivacao() {
		Cenario c = padrao();
		c.criar(new ComponentesPartida() {
			@Override public boolean preparacaoConcluida() { return false; }
		});
		boolean resultadoDeIniciar = c.partida.iniciar();
		boolean resultadoDeAtivarColuna = c.partida.ativarColuna(Coluna.AMARELA);
		assertFalse("O início com preparação incompleta deve ser recusado", resultadoDeIniciar);
		assertFalse("Uma coluna não pode ser ativada durante a preparação pendente", resultadoDeAtivarColuna);
		assertEquals("Fase da partida", Fase.PREPARACAO, c.partida.getFase());
		assertEquals("Fichas de ativação disponíveis de Azul", 4, c.azul().getFichasDisponiveis().size());
	}

	/**
	 * Objetivo: ativar apenas uma coluna por turno sem consumir outra ficha.
	 * Entrada: Chagatai ativa amarela e tenta verde no mesmo turno.
	 * Saída esperada: segunda ativação recusada; dois cavalos e ficha verde intacta.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void segundaColunaNoMesmoTurnoERecusadaSemAlteracoes() {
		Cenario c = padrao().iniciar();
		exigir(c.partida.ativarColuna(Coluna.AMARELA), "ativação inicial");
		boolean resultadoDeAtivarColuna = c.partida.ativarColuna(Coluna.VERDE);
		assertFalse("A coluna verde deve ser recusada depois de ativar a amarela no mesmo turno", resultadoDeAtivarColuna);
		assertEquals("Cavalos restantes no turno", 2, c.partida.getTurno().getCavalos());
		assertEquals("Coluna ativada no turno", Coluna.AMARELA, c.partida.getTurno().getColuna());
		assertTrue("A ficha verde deve continuar disponível", c.azul().temFichaDisponivel(Coluna.VERDE));
	}

	/**
	 * Objetivo: resolver a obrigação do Khan antes de trocar de jogador.
	 * Entrada: Chagatai ativa vermelha e tenta encerrar sem mover Khan.
	 * Saída esperada: encerramento recusado; jogador e número do turno intactos.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void khanPendenteImpedeEncerrarTurno() {
		Cenario c = padrao().iniciar();
		exigir(c.partida.ativarColuna(Coluna.VERMELHA), "coluna com Khan");
		boolean resultadoDeTerminarTurno = c.partida.terminarTurno();
		assertFalse("O turno não deve terminar enquanto o Khan obrigatório estiver pendente", resultadoDeTerminarTurno);
		assertEquals("Jogador da vez", c.azul(), c.partida.getJogadorDaVez());
		assertEquals("Número do turno corrente", 1, c.partida.getTurno().getNumero());
		assertTrue("A obrigação do Khan deve continuar pendente", c.partida.getTurno().isKhanPendente());
	}

	/**
	 * Objetivo: abastecer destino e setas sem exceder três peças ou inventar recursos.
	 * Entrada: Khan vai a KA; X já tem três espadas, Y e KA têm uma peça.
	 * Saída esperada: KA/Y passam a dois; X fica com três; banco só paga moeda/yurt.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void khanAbasteceSetasRespeitandoCapacidadeEBanco() {
		Cenario c = padrao();
		c.tabuleiro.getProvincia("X").colocarPeca();
		c.tabuleiro.getProvincia("X").colocarPeca();
		c.iniciar();
		exigir(c.partida.ativarColuna(Coluna.VERMELHA), "coluna com Khan");
		exigir(c.partida.moverKhanParaProvincia("KA"), "movimento válido do Khan");
		assertEquals("Peças na província KA", 2, c.tabuleiro.getProvincia("KA").getPecas());
		assertEquals("Peças na província X", 3, c.tabuleiro.getProvincia("X").getPecas());
		assertEquals("Peças na província Y", 2, c.tabuleiro.getProvincia("Y").getPecas());
		assertEquals("Estoque físico de espadas no banco", 7, c.partida.getEstoque(TipoTributo.ESPADA));
		assertEquals("Estoque físico de tributos de yurt no banco", 8, c.partida.getEstoque(TipoTributo.YURT));
		assertEquals("Estoque físico de moedas no banco", 10, c.partida.getEstoque(TipoTributo.MOEDA));
		assertFalse("A obrigação do Khan deve estar resolvida", c.partida.getTurno().isKhanPendente());
	}

	/**
	 * Objetivo: o Khan não pode permanecer no destino usado anteriormente.
	 * Entrada: Azul move para KA; Vermelho ativa amarela e tenta repetir KA.
	 * Saída esperada: recusa sem abastecimento nem resolução da obrigação.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void khanNaoPermaneceNoMesmoLugarEmOutroTurno() {
		Cenario c = padrao().iniciar();
		exigir(c.partida.ativarColuna(Coluna.VERMELHA), "ativação azul");
		exigir(c.partida.moverKhanParaProvincia("KA"), "primeiro destino");
		exigir(c.partida.terminarTurno(), "troca de jogador");
		exigir(c.partida.ativarColuna(Coluna.AMARELA), "ativação de Ogedei");
		boolean resultadoDeMoverKhanParaProvincia = c.partida.moverKhanParaProvincia("KA");
		assertFalse("O Khan deve sair de KA em vez de permanecer no mesmo lugar", resultadoDeMoverKhanParaProvincia);
		assertEquals("Posição do Khan", "KA", c.partida.getPosicaoKhan());
		assertEquals("Peças na província KA", 2, c.tabuleiro.getProvincia("KA").getPecas());
		assertTrue("A obrigação do Khan deve continuar pendente", c.partida.getTurno().isKhanPendente());
	}

	/**
	 * Objetivo: rejeitar província sem ícone do Khan de forma atômica.
	 * Entrada: coluna vermelha ativa; destino X é uma província comum.
	 * Saída esperada: posição nula, peça e estoque de espada inalterados.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void destinoInvalidoDoKhanPreservaEstado() {
		Cenario c = padrao().iniciar();
		exigir(c.partida.ativarColuna(Coluna.VERMELHA), "coluna com Khan");
		boolean resultadoDeMoverKhanParaProvincia = c.partida.moverKhanParaProvincia("X");
		assertFalse("A província comum X não deve receber o Khan", resultadoDeMoverKhanParaProvincia);
		assertEquals("Posição do Khan", null, c.partida.getPosicaoKhan());
		assertEquals("Peças na província X", 1, c.tabuleiro.getProvincia("X").getPecas());
		assertEquals("Estoque físico de espadas no banco", 9, c.partida.getEstoque(TipoTributo.ESPADA));
		assertTrue("A obrigação do Khan deve continuar pendente", c.partida.getTurno().isKhanPendente());
	}

	/**
	 * Objetivo: a coluna branca recupera as fichas usadas ao encerrar o turno.
	 * Entrada: Azul usa amarela, depois branca e resolve o Khan.
	 * Saída esperada: ao terminar, as quatro fichas de Azul estão disponíveis.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void brancaRecuperaTodasAsFichasAposObrigacoes() {
		Cenario c = padrao().iniciar();
		exigir(c.partida.ativarColuna(Coluna.AMARELA), "uso da amarela");
		exigir(c.partida.terminarTurno(), "fim do turno azul");
		turnoVazio(c.partida);
		exigir(c.partida.ativarColuna(Coluna.BRANCA), "uso da branca");
		exigir(c.partida.moverKhanParaProvincia("KA"), "obrigação da branca");
		exigir(c.partida.terminarTurno(), "encerramento da branca");
		assertEquals("A branca deve recuperar inclusive sua própria ficha", 4, c.azul().getFichasDisponiveis().size());
	}

	/**
	 * Objetivo: cobrar uma espada no primeiro ataque e duas no seguinte à mesma cidade.
	 * Entrada: Azul tem três espadas e ataca a mesma cidade duas vezes.
	 * Saída esperada: duas peças transferidas, dois tesouros restantes e zero espadas.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void ataquesNaMesmaCidadeCustamUmaEDuasEspadas() {
		Cenario c = padrao();
		c.azul().adicionarTributo(TipoTributo.ESPADA, 3);
		c.iniciar();
		Cidade cidade = c.revelada();
		exigir(c.partida.atacarCidade(cidade.getNome()), "primeiro ataque");
		exigir(c.partida.atacarCidade(cidade.getNome()), "segundo ataque");
		assertEquals("Espadas físicas de Azul", 0, c.azul().contarTributo(TipoTributo.ESPADA));
		assertEquals("Tesouros restantes na cidade atacada", 2, cidade.getTesouros());
		assertEquals("Tesouros recebidos por Azul", 2, c.partida.getTesourosRecebidos(c.azul()));
		assertEquals("Estoque físico de espadas no banco", 9, c.partida.getEstoque(TipoTributo.ESPADA));
	}

	/**
	 * Objetivo: reiniciar o custo para outra cidade atacada no mesmo turno.
	 * Entrada: duas espadas; um ataque a cada uma de duas cidades diferentes.
	 * Saída esperada: ambos os ataques aceitos e zero espadas.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void primeiroAtaqueDeOutraCidadeCustaUmaEspada() {
		Cenario c = padrao();
		c.azul().adicionarTributo(TipoTributo.ESPADA, 2);
		c.iniciar();
		List<Cidade> cidades = c.reveladas();
		exigir(c.partida.atacarCidade(cidades.get(0).getNome()), "primeira cidade");
		boolean segundo = c.partida.atacarCidade(cidades.get(1).getNome());
		assertTrue("O primeiro ataque à segunda cidade deve ser aceito", segundo);
		assertEquals("Espadas físicas de Azul", 0, c.azul().contarTributo(TipoTributo.ESPADA));
		assertEquals("Tesouros recebidos por Azul", 2, c.partida.getTesourosRecebidos(c.azul()));
	}

	/**
	 * Objetivo: exigir um yurt de conquista antes de retirar o último tesouro.
	 * Entrada: cidade com uma peça; Azul com espada, mas sem yurts disponíveis.
	 * Saída esperada: recusa; espada, tesouro e cidade intactos.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void conquistaSemYurtDisponivelERecusadaSemCobrarEspada() {
		Cenario c = padrao();
		c.azul().adicionarTributo(TipoTributo.ESPADA, 1);
		while (c.azul().retirarYurtDoEstoque()) { }
		c.iniciar();
		Cidade cidade = c.revelada();
		deixarUltimoTesouro(cidade, c.azul());
		boolean resultadoDeAtacarCidade = c.partida.atacarCidade(cidade.getNome());
		assertFalse("O ataque sem yurt de conquista deve ser recusado", resultadoDeAtacarCidade);
		assertEquals("Espadas físicas de Azul", 1, c.azul().contarTributo(TipoTributo.ESPADA));
		assertEquals("Tesouros restantes na cidade atacada", 1, cidade.getTesouros());
		assertFalse("A cidade deve continuar sem conquista", cidade.isConquistada());
		assertEquals("Tesouros recebidos por Azul", 0, c.partida.getTesourosRecebidos(c.azul()));
	}

	/**
	 * Objetivo: conquistar com yurt e revelar a próxima cidade fechada.
	 * Entrada: quatro cidades no mapa; três reveladas; última peça de uma atacada.
	 * Saída esperada: um yurt gasto, cidade conquistada e três cidades atacáveis.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void conquistaGastaYurtERevelaProximaCidade() {
		Cenario c = padrao();
		c.azul().adicionarTributo(TipoTributo.ESPADA, 1);
		c.iniciar();
		Cidade cidade = c.revelada();
		deixarUltimoTesouro(cidade, c.azul());
		exigir(c.partida.atacarCidade(cidade.getNome()), "ataque de conquista");
		assertEquals("Yurts disponíveis de Azul", 11, c.azul().getYurtsDisponiveis());
		assertEquals("Cor do yurt de conquista", Cor.AZUL, cidade.getConquistadaPor());
		assertEquals("Cidades disponíveis para ataque", 3, c.reveladas().size());
		assertEquals("Tesouros na próxima cidade revelada", 4, c.reveladas().get(0).getTesouros());
	}

	/**
	 * Objetivo: Altani não troca peão após coletar sem mover.
	 * Entrada: peão 0 em B, coleta a espada de X usando a verde.
	 * Saída esperada: troca para o peão 1 recusada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void altaniFixaPeaoDepoisDeColetarTributo() {
		Cenario c = new Cenario(Herdeiro.ALTANI, Herdeiro.OGEDEI).iniciar();
		c.tabuleiro.posicionarPeao(c.azul().getPeao(0), c.tabuleiro.getParada("B"));
		exigir(c.partida.escolherPeao(0), "posição inicial do peão 0");
		exigir(c.partida.ativarColuna(Coluna.VERDE), "coluna com mão");
		exigir(c.partida.pegarTributo("X"), "coleta sem movimento");
		assertFalse("Coletar já usa o peão escolhido neste turno", c.partida.escolherPeao(1));
	}

	/**
	 * Objetivo: Altani não troca peão após construir sem mover.
	 * Entrada: peão 0 em B e tributo de yurt; construção aceita nessa parada.
	 * Saída esperada: troca para o peão 1 recusada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void altaniFixaPeaoDepoisDeConstruirYurt() {
		Cenario c = new Cenario(Herdeiro.ALTANI, Herdeiro.OGEDEI);
		c.azul().adicionarTributo(TipoTributo.YURT, 1);
		c.iniciar();
		c.tabuleiro.posicionarPeao(c.azul().getPeao(0), c.tabuleiro.getParada("B"));
		exigir(c.partida.escolherPeao(0), "posição inicial do peão 0");
		exigir(c.partida.construirYurt("B"), "construção sem movimento");
		assertFalse("Construir já usa o peão escolhido neste turno", c.partida.escolherPeao(1));
	}

	/**
	 * Objetivo: Altani não troca peão após atacar sem mover.
	 * Entrada: peão 0 em Karakorum com uma espada; cidade adjacente revelada.
	 * Saída esperada: troca para o peão 1 recusada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void altaniFixaPeaoDepoisDeAtacarCidade() {
		Cenario c = new Cenario(Herdeiro.ALTANI, Herdeiro.OGEDEI);
		c.azul().adicionarTributo(TipoTributo.ESPADA, 1);
		c.iniciar();
		exigir(c.partida.atacarCidade(c.revelada().getNome()), "ataque sem movimento");
		assertFalse("Atacar já usa o peão escolhido neste turno", c.partida.escolherPeao(1));
	}

	/**
	 * Objetivo: Chagatai troca apenas um cavalo por uma espada física por turno.
	 * Entrada: dois cavalos da amarela; executar duas tentativas de troca.
	 * Saída esperada: primeira aceita, segunda recusada; um cavalo restante,
	 * uma espada recebida e apenas uma espada retirada do banco.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void chagataiTrocaCavaloUmaVezPorTurnoRetirandoEspadaDoBanco() {
		Cenario c = padrao().iniciar();
		exigir(c.partida.ativarColuna(Coluna.AMARELA), "dois cavalos de Chagatai");
		boolean primeira = c.partida.trocarCavaloPorEspada();
		boolean segunda = c.partida.trocarCavaloPorEspada();
		assertTrue("A primeira troca de Chagatai deve ser aceita", primeira);
		assertFalse("A segunda troca de Chagatai no turno deve ser recusada", segunda);
		assertEquals("Cavalos restantes no turno", 1, c.partida.getTurno().getCavalos());
		assertEquals("Espadas físicas de Azul", 1, c.azul().contarTributo(TipoTributo.ESPADA));
		assertEquals("Estoque físico de espadas no banco", 8, c.partida.getEstoque(TipoTributo.ESPADA));
	}

	static Cenario padrao() { return new Cenario(Herdeiro.CHAGATAI, Herdeiro.OGEDEI); }

	static void exigir(boolean sucesso, String passo) {
		if (!sucesso) throw new IllegalStateException("Falha na preparação do cenário: " + passo);
	}

	static void deixarUltimoTesouro(Cidade cidade, Jogador jogador) {
		while (cidade.getTesouros() > 1) exigir(cidade.retirarTesouro(jogador.getCor()), "reduzir tesouros da cidade");
	}

	/** Avança um turno legal, resolvendo Khan quando exigido pela cartela. */
	static void turnoVazio(Partida partida) {
		if (partida.getTurno().getColuna() == null) {
			boolean ativou = false;
			for (Coluna coluna : new Coluna[] { Coluna.VERDE, Coluna.AMARELA, Coluna.VERMELHA, Coluna.BRANCA }) {
				if (partida.ativarColuna(coluna)) { ativou = true; break; }
			}
			exigir(ativou, "ativar coluna disponível no turno vazio");
		}
		if (partida.getTurno().isKhanPendente()) {
			String destino = "KA".equals(partida.getPosicaoKhan()) ? "KB" : "KA";
			exigir(partida.moverKhanParaProvincia(destino), "resolver Khan no turno vazio");
		}
		exigir(partida.terminarTurno(), "encerrar turno vazio");
	}

	/** Mapa mínimo determinístico; províncias recebem suas peças explicitamente. */
	static final class Cenario {
		final Tabuleiro tabuleiro = new Tabuleiro();
		final List<Jogador> jogadores = new ArrayList<>();
		Partida partida;
		Cenario(Herdeiro... herdeiros) { this(true, herdeiros); }
		Cenario(boolean comCidades, Herdeiro... herdeiros) {
			tabuleiro.adicionarParada("K", null, TipoParada.KARAKORUM);
			tabuleiro.adicionarParada("B", Regiao.RUSSIA, TipoParada.SIMPLES);
			tabuleiro.adicionarParada("C", Regiao.RUSSIA, TipoParada.SIMPLES);
			tabuleiro.adicionarParada("D", Regiao.RUSSIA, TipoParada.DUPLA);
			tabuleiro.adicionarParada("E", Regiao.CHINA, TipoParada.SIMPLES);
			tabuleiro.ligar("K", "B"); tabuleiro.ligar("B", "C");
			tabuleiro.ligar("C", "D"); tabuleiro.ligar("K", "E");
			Provincia x = tabuleiro.adicionarProvincia("X", Regiao.RUSSIA, TipoTributo.ESPADA, false, "B");
			Provincia y = tabuleiro.adicionarProvincia("Y", Regiao.RUSSIA, TipoTributo.YURT, false, "C");
			Provincia ka = tabuleiro.adicionarProvincia("KA", Regiao.RUSSIA, TipoTributo.MOEDA, true, "K");
			Provincia kb = tabuleiro.adicionarProvincia("KB", Regiao.RUSSIA, TipoTributo.MOEDA, true, "C");
			for (Provincia provincia : Arrays.asList(x, y, ka, kb)) provincia.colocarPeca();
			ka.adicionarVizinhaDoKhan(x); ka.adicionarVizinhaDoKhan(y);
			kb.adicionarVizinhaDoKhan(x); kb.adicionarVizinhaDoKhan(y);
			if (comCidades) for (int i = 1; i <= 4; i++) tabuleiro.adicionarCidade("Cidade" + i, Regiao.RUSSIA, "K", "C");
			Cor[] cores = { Cor.AZUL, Cor.VERMELHO, Cor.VERDE, Cor.AMARELO, Cor.PRETO };
			for (int i = 0; i < herdeiros.length; i++) jogadores.add(new Jogador("Jogador " + (i + 1), cores[i], herdeiros[i]));
		}
		Jogador azul() { return jogadores.get(0); }
		void criar(ComponentesPartida componentes) { partida = new Partida(tabuleiro, jogadores, componentes, new Random(42L)); }
		Cenario iniciar() { return iniciar(new ComponentesPartida() { }); }
		Cenario iniciar(ComponentesPartida componentes) {
			criar(componentes); exigir(partida.iniciar(), "iniciar partida com preparação concluída"); return this;
		}
		List<Cidade> reveladas() {
			List<Cidade> lista = new ArrayList<>();
			for (Cidade cidade : tabuleiro.getCidades()) if (cidade.podeSerAtacada()) lista.add(cidade);
			lista.sort(Comparator.comparing(Cidade::getNome)); return lista;
		}
		Cidade revelada() { return reveladas().get(0); }
	}
}
