package model;

/**
 * As três regiões do mapa do Império Mongol: Rússia, China e Pérsia.
 *
 * Equivale ao "continente" de um jogo como War: agrupa paradas e cidades e
 * é a unidade usada na contagem de influência (maioria de yurts) ao final da
 * partida. Karakorum, a capital, fica no centro e não pertence a nenhuma região.
 *
 * Cada região está associada a uma coluna colorida do tabuleiro do jogador
 * (manual, p. 12: "Cada uma das 3 colunas coloridas é relacionada a uma das 3
 * regiões no tabuleiro, de acordo com sua cor"). A associação abaixo foi obtida
 * do exemplo de pontuação da p. 15 do manual: Pérsia ↔ coluna verde,
 * Rússia ↔ coluna amarela, China ↔ coluna vermelha.
 */
enum Regiao {
	RUSSIA("Rússia", Coluna.AMARELA),
	CHINA("China", Coluna.VERMELHA),
	PERSIA("Pérsia", Coluna.VERDE);

	private final String nome;
	private final Coluna coluna;

	Regiao(String nome, Coluna coluna) {
		this.nome = nome;
		this.coluna = coluna;
	}

	String getNome() {
		return nome;
	}

	/** Coluna do tabuleiro do jogador cujas melhorias pontuam nesta região. */
	Coluna getColuna() {
		return coluna;
	}

	@Override
	public String toString() {
		return nome;
	}
}
