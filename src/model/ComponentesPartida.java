package model;

/**
 * Contrato entre a partida e os componentes de cartas, melhorias e tesouros.
 * ComponentesKhan implementa a parte 2. Os métodos padrão permitem continuar
 * testando isoladamente as regras das partes 1 e 3.
 * Na integração, cada jogador escolhe sua carta secreta; Jochi recebe uma
 * melhoria aleatória inicial na coluna branca.
 */
interface ComponentesPartida {
	/** Valida a configuração antes de alterar os jogadores e as cidades. */
	default void validarPreparacao(Tabuleiro tabuleiro, java.util.List<Jogador> jogadores) { }
	/** Inicializa cartas e peças depois da preparação do tabuleiro. */
	default void preparar(Partida partida) { }
	default boolean preparacaoConcluida() { return true; }

	/** Só os acréscimos; as ações impressas no herdeiro são somadas pela Partida. */
	default EfeitosColuna efeitosDasMelhorias(Jogador jogador, Coluna coluna, int numeroTurno) {
		return EfeitosColuna.semEfeitos();
	}

	/**
	 * Após retirar UMA peça do contador da cidade, transfere seu tipo ao jogador.
	 * A parte 2 mantém a sequência dos tipos sincronizada com Cidade.getTesouros().
	 */
	default void receberTesouro(Jogador jogador, Cidade cidade) { }
	default boolean podeReceberTesouro(Cidade cidade, TipoTesouro tipo) { return tipo == null; }
	default void receberTesouro(Jogador jogador, Cidade cidade, TipoTesouro tipo) {
		receberTesouro(jogador, cidade);
	}

	/** Avalia cartas abertas, inclusive imediatamente após ativar uma coluna. */
	default void aposAcao(Partida partida) { }

	/**
	 * Repõe melhorias e entrega tributos pelos ícones livres. false significa
	 * nenhuma alteração. Usar o banco de Partida para receber as peças físicas.
	 */
	default boolean moverKhanParaMelhorias(Partida partida) { return false; }

	/** Por exemplo: melhoria gratuita ou escolha de tesouro de um bônus. */
	default boolean temEscolhaPendente(Jogador jogador) { return false; }

	/** Aplica influência por melhorias e cartas secretas; chamado uma única vez. */
	default void pontuarFimDeJogo(Partida partida) { }
}
