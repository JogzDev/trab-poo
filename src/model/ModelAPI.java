package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Fachada pública do Model (padrões Facade e Singleton, INF1636 cap. 17).
 *
 * É a única classe do pacote model visível para a View e o Controller. Todas
 * as outras continuam com visibilidade de pacote. A fachada só recebe e
 * devolve tipos simples (String, int, boolean e listas de String), para que
 * nenhuma classe interna do Model vaze para fora do pacote.
 *
 * Convenções do contrato:
 * - paradas, cidades e províncias são identificadas pelo nome usado no mapa;
 * - jogadores e peões são identificados por índices (ordem de jogo; 0 ou 1);
 * - herdeiros, colunas, cores, tipos e regiões são o nome da constante em
 *   maiúsculas (por exemplo "ALTANI", "VERMELHA", "AZUL", "DUPLA", "RUSSIA");
 * - ações devolvem false quando a regra não permite, sem alterar nada;
 * - toda ação bem-sucedida notifica os observadores registrados.
 */
public final class ModelAPI implements ObservadoIF {

	/** Local do Khan fora das províncias: a área de melhorias (manual, p. 9). */
	public static final String AREA_MELHORIAS = Partida.AREA_MELHORIAS;

	private static ModelAPI instancia = null;

	private final List<ObservadorIF> observadores = new ArrayList<>();
	private Partida partida;

	private ModelAPI() {
	}

	/**
	 * Componentes usados pela tela da 2ª iteração. As cartas e melhorias ainda
	 * não aparecem na interface; mover o Khan para a área de melhorias é uma
	 * jogada válida e, sem peças de melhoria em jogo, não há o que repor.
	 * Quando a parte das melhorias entrar na tela, a fachada passa a usar
	 * ComponentesKhan com as peças configuradas.
	 */
	private static final class ComponentesDaTela implements ComponentesPartida {
		@Override
		public boolean moverKhanParaMelhorias(Partida partida) {
			return true;
		}
	}

	public static ModelAPI getInstance() {
		if (instancia == null) {
			instancia = new ModelAPI();
		}
		return instancia;
	}

	// ============================================================ Observer

	@Override
	public void add(ObservadorIF o) {
		if (o != null && !observadores.contains(o)) {
			observadores.add(o);
		}
	}

	@Override
	public void remove(ObservadorIF o) {
		observadores.remove(o);
	}

	private void notificar() {
		for (ObservadorIF o : new ArrayList<>(observadores)) {
			o.notify(this);
		}
	}

	// ============================================================ partida

	/** Nomes aceitos em novaPartida, na ordem do manual. */
	public List<String> getHerdeiros() {
		List<String> nomes = new ArrayList<>();
		for (Herdeiro h : Herdeiro.values()) {
			nomes.add(h.name());
		}
		return nomes;
	}

	/**
	 * Cria e inicia uma partida no mapa oficial. As cores são atribuídas na
	 * ordem dos jogadores (AZUL, VERDE, VERMELHO, AMARELO, PRETO).
	 *
	 * @param nomes     nomes dos jogadores, na ordem de jogo (2 a 5)
	 * @param herdeiros herdeiro de cada jogador, sem repetição ("ALTANI" ou "Altani")
	 * @return false se a configuração for inválida; a partida anterior é mantida
	 */
	public boolean novaPartida(List<String> nomes, List<String> herdeiros) {
		if (nomes == null || herdeiros == null || nomes.size() != herdeiros.size()
				|| nomes.size() < 2 || nomes.size() > Cor.values().length) {
			return false;
		}
		try {
			List<Jogador> jogadores = new ArrayList<>();
			for (int i = 0; i < nomes.size(); i++) {
				Herdeiro herdeiro = herdeiro(herdeiros.get(i));
				if (herdeiro == null) {
					return false;
				}
				jogadores.add(new Jogador(nomes.get(i), Cor.values()[i], herdeiro));
			}
			Partida nova = new Partida(MapaOficial.criar(), jogadores, new ComponentesDaTela(), new Random());
			if (!nova.iniciar()) {
				return false;
			}
			partida = nova;
		} catch (IllegalArgumentException e) {
			return false; // nome vazio ou herdeiro repetido
		}
		notificar();
		return true;
	}

	public boolean temPartida() {
		return partida != null;
	}

	public boolean isPartidaEncerrada() {
		return partida != null && partida.getFase() == Fase.ENCERRADA;
	}

	public int getRodada() {
		return partida == null ? 0 : partida.getRodada();
	}

	// ============================================================ mapa

	/** Nomes de todas as paradas, em ordem alfabética. */
	public List<String> getParadas() {
		List<String> nomes = new ArrayList<>();
		if (partida != null) {
			for (Parada p : partida.getTabuleiro().getParadas()) {
				nomes.add(p.getNome());
			}
		}
		Collections.sort(nomes);
		return nomes;
	}

	/** "SIMPLES", "DUPLA" ou "KARAKORUM"; null se a parada não existir. */
	public String getTipoParada(String parada) {
		Parada p = parada(parada);
		return p == null ? null : p.getTipo().name();
	}

	/** "RUSSIA", "CHINA" ou "PERSIA"; null para Karakorum ou parada inexistente. */
	public String getRegiaoParada(String parada) {
		Parada p = parada(parada);
		return p == null || p.getRegiao() == null ? null : p.getRegiao().name();
	}

	/** Paradas ligadas por rota à parada informada, em ordem alfabética. */
	public List<String> getVizinhas(String parada) {
		List<String> nomes = new ArrayList<>();
		Parada p = parada(parada);
		if (p != null) {
			for (Parada v : partida.getTabuleiro().vizinhas(p)) {
				nomes.add(v.getNome());
			}
		}
		Collections.sort(nomes);
		return nomes;
	}

	public List<String> getCidades() {
		List<String> nomes = new ArrayList<>();
		if (partida != null) {
			for (Cidade c : partida.getTabuleiro().getCidades()) {
				nomes.add(c.getNome());
			}
		}
		Collections.sort(nomes);
		return nomes;
	}

	/** Paradas do anel ao redor da cidade, em ordem alfabética. */
	public List<String> getParadasDaCidade(String cidade) {
		List<String> nomes = new ArrayList<>();
		Cidade c = cidade(cidade);
		if (c != null) {
			for (Parada p : c.getParadasAdjacentes()) {
				nomes.add(p.getNome());
			}
		}
		Collections.sort(nomes);
		return nomes;
	}

	public boolean isCidadeRevelada(String cidade) {
		Cidade c = cidade(cidade);
		return c != null && c.isRevelada();
	}

	// ============================================================ jogadores

	public int getNumJogadores() {
		return partida == null ? 0 : partida.getJogadores().size();
	}

	public int getJogadorDaVez() {
		return partida == null ? -1 : partida.getJogadores().indexOf(partida.getJogadorDaVez());
	}

	public String getNomeJogador(int jogador) {
		return jogador(jogador).getNome();
	}

	/** Nome da constante de Cor: "AZUL", "VERDE", "VERMELHO", "AMARELO" ou "PRETO". */
	public String getCorJogador(int jogador) {
		return jogador(jogador).getCor().name();
	}

	public String getHerdeiroJogador(int jogador) {
		return jogador(jogador).getHerdeiro().name();
	}

	/** 2 para Altani, 1 para os demais. */
	public int getNumPeoes(int jogador) {
		return jogador(jogador).getPeoes().size();
	}

	/** Nome da parada onde está o peão (índice 0, ou 1 no caso de Altani). */
	public String getPosicaoPeao(int jogador, int peao) {
		Jogador j = jogador(jogador);
		if (peao < 0 || peao >= j.getPeoes().size()) {
			throw new IllegalArgumentException("Peão inexistente: " + peao);
		}
		return partida.getTabuleiro().posicaoDe(j.getPeao(peao)).getNome();
	}

	// ============================================================ turno

	/** Colunas cuja ficha de ativação o jogador da vez ainda pode usar. */
	public List<String> getColunasDisponiveis() {
		List<String> nomes = new ArrayList<>();
		if (emAndamento()) {
			Jogador j = partida.getJogadorDaVez();
			for (Coluna c : Coluna.values()) {
				if (j.temFichaDisponivel(c)) {
					nomes.add(c.name());
				}
			}
		}
		return nomes;
	}

	/** Ações impressas na coluna da cartela do jogador da vez (para exibição). */
	public String getDescricaoColuna(String coluna) {
		Coluna c = coluna(coluna);
		if (!emAndamento() || c == null) {
			return "";
		}
		return partida.getJogadorDaVez().getHerdeiro().getAcoes(c).toString();
	}

	/** Coluna ativada neste turno; null enquanto nenhuma foi ativada. */
	public String getColunaAtivada() {
		if (!emAndamento() || partida.getTurno().getColuna() == null) {
			return null;
		}
		return partida.getTurno().getColuna().name();
	}

	/** Coloca a ficha na coluna e libera as ações dela. Uma por turno. */
	public boolean ativarColuna(String coluna) {
		Coluna c = coluna(coluna);
		if (!emAndamento() || c == null || !partida.ativarColuna(c)) {
			return false;
		}
		notificar();
		return true;
	}

	public int getCavalosRestantes() {
		return emAndamento() ? partida.getTurno().getCavalos() : 0;
	}

	/** Índice do peão que age neste turno (sempre 0, exceto para Altani). */
	public int getPeaoAtivo() {
		if (!emAndamento()) {
			return -1;
		}
		return partida.getJogadorDaVez().getPeoes().indexOf(partida.getTurno().getPeaoAtivo());
	}

	/** Altani pode trocar de peão até a primeira ação que depende de posição. */
	public boolean podeEscolherPeao() {
		return emAndamento() && partida.getJogadorDaVez().getPeoes().size() > 1
				&& !partida.getTurno().peaoJaAgiu();
	}

	public boolean escolherPeao(int peao) {
		if (!podeEscolherPeao() || !partida.escolherPeao(peao)) {
			return false;
		}
		notificar();
		return true;
	}

	/** Paradas onde o peão ativo pode terminar o movimento com os cavalos restantes. */
	public List<String> getDestinosPossiveis() {
		List<String> nomes = new ArrayList<>();
		if (emAndamento() && getCavalosRestantes() > 0) {
			for (String nome : getParadas()) {
				if (partida.podeMoverPara(nome)) {
					nomes.add(nome);
				}
			}
		}
		return nomes;
	}

	public boolean podeMoverPara(String parada) {
		return emAndamento() && parada != null && partida.podeMoverPara(parada);
	}

	/**
	 * Move o peão ativo pelo caminho mais barato até a parada. Cada parada
	 * custa um cavalo; yurts próprios intermediários são pulados sem custo.
	 */
	public boolean moverPara(String parada) {
		if (!podeMoverPara(parada) || !partida.moverPara(parada)) {
			return false;
		}
		notificar();
		return true;
	}

	/** true quando a coluna ativada obriga a mover o Khan antes de terminar o turno. */
	public boolean isKhanPendente() {
		return emAndamento() && partida.getTurno().isKhanPendente();
	}

	/** Local onde o Khan está (província ou AREA_MELHORIAS); null antes do primeiro movimento. */
	public String getPosicaoKhan() {
		return partida == null ? null : partida.getPosicaoKhan();
	}

	/**
	 * Todos os locais que o Khan pode ocupar: as províncias do Khan, em ordem
	 * alfabética, e AREA_MELHORIAS. São os locais clicáveis do Khan no mapa.
	 */
	public List<String> getLocaisDoKhan() {
		List<String> nomes = new ArrayList<>();
		if (partida != null) {
			for (Provincia p : partida.getTabuleiro().provinciasDoKhan()) {
				nomes.add(p.getNome());
			}
			Collections.sort(nomes);
			nomes.add(AREA_MELHORIAS);
		}
		return nomes;
	}

	/** Locais para onde o Khan pode ir agora: qualquer um de getLocaisDoKhan(), menos o atual. */
	public List<String> getDestinosDoKhan() {
		List<String> nomes = new ArrayList<>();
		if (isKhanPendente()) {
			for (String local : getLocaisDoKhan()) {
				if (!local.equals(partida.getPosicaoKhan())) {
					nomes.add(local);
				}
			}
		}
		return nomes;
	}

	/** Move o Khan para uma província do Khan ou para AREA_MELHORIAS. */
	public boolean moverKhan(String local) {
		if (!isKhanPendente() || local == null) {
			return false;
		}
		boolean moveu = AREA_MELHORIAS.equals(local) ? partida.moverKhanParaMelhorias()
				: partida.moverKhanParaProvincia(local);
		if (!moveu) {
			return false;
		}
		notificar();
		return true;
	}

	/** Exige coluna ativada, Khan resolvido e nenhuma escolha obrigatória pendente. */
	public boolean podeTerminarTurno() {
		return partida != null && partida.podeTerminarTurno();
	}

	public boolean terminarTurno() {
		if (!emAndamento() || !partida.terminarTurno()) {
			return false;
		}
		notificar();
		return true;
	}

	// ============================================================ auxiliares

	private boolean emAndamento() {
		return partida != null && partida.getFase() == Fase.EM_ANDAMENTO;
	}

	private Jogador jogador(int indice) {
		if (partida == null || indice < 0 || indice >= partida.getJogadores().size()) {
			throw new IllegalArgumentException("Jogador inexistente: " + indice);
		}
		return partida.getJogadores().get(indice);
	}

	private Parada parada(String nome) {
		if (partida == null || nome == null || !partida.getTabuleiro().existeParada(nome)) {
			return null;
		}
		return partida.getTabuleiro().getParada(nome);
	}

	private Cidade cidade(String nome) {
		if (partida == null || nome == null) {
			return null;
		}
		for (Cidade c : partida.getTabuleiro().getCidades()) {
			if (c.getNome().equals(nome)) {
				return c;
			}
		}
		return null;
	}

	private static Herdeiro herdeiro(String nome) {
		for (Herdeiro h : Herdeiro.values()) {
			if (h.name().equalsIgnoreCase(nome) || h.getNome().equalsIgnoreCase(nome)) {
				return h;
			}
		}
		return null;
	}

	private static Coluna coluna(String nome) {
		for (Coluna c : Coluna.values()) {
			if (c.name().equalsIgnoreCase(nome)) {
				return c;
			}
		}
		return null;
	}
}
