package model;

/**
 * Tipos de parada (nó das rotas do mapa) e suas capacidades.
 *
 * Regras do manual (pp. 7 e 11):
 *
 * - Parada simples: só pode terminar o movimento nela se não houver peão de
 * outro jogador; comporta no máximo 1 yurt.
 * - Parada dupla (parada de conselheiro): comporta 2 peões e até 2 yurts
 * (podem ser 2 do mesmo jogador).
 * - Karakorum: comporta todos os peões e não pode receber yurts
 * (exceção: a habilidade inicial do herdeiro Tolui, tratada por
 * Parada#colocarYurt(Cor, boolean)).
 *
 */
enum TipoParada {
	SIMPLES(1, 1, false),
	DUPLA(2, 2, true),
	KARAKORUM(Integer.MAX_VALUE, 0, true);

	private final int capacidadePeoes;
	private final int capacidadeYurts;
	private final boolean conselheiro;

	TipoParada(int capacidadePeoes, int capacidadeYurts, boolean conselheiro) {
		this.capacidadePeoes = capacidadePeoes;
		this.capacidadeYurts = capacidadeYurts;
		this.conselheiro = conselheiro;
	}

	/** Quantos peões podem terminar o movimento nesta parada ao mesmo tempo. */
	int getCapacidadePeoes() {
		return capacidadePeoes;
	}

	/** Quantos yurts a parada comporta. */
	int getCapacidadeYurts() {
		return capacidadeYurts;
	}

	/** Se a parada tem um conselheiro (onde se entregam tesouros por votos). */
	boolean isConselheiro() {
		return conselheiro;
	}
}
