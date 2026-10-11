package model;

/**
 * Proposta provisória de integração da parte 2 com a partida. O contrato precisa
 * ser alinhado com a Pessoa 2 antes da integração na main; não impõe uma
 * arquitetura já aprovada para cartas, melhorias ou tesouros tipados.
 * Os métodos padrão permitem testar somente as regras das partes 1 e 3.
 * Na integração completa, as escolhas iniciais ficam pendentes até todos
 * escolherem a carta secreta e Jochi escolher sua melhoria.
 */
interface ComponentesPartida {
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
