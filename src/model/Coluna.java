package model;

/**
 * As quatro colunas de ações do tabuleiro de jogador (cartela do herdeiro).
 *
 * <p>No seu turno o jogador coloca uma peça de ativação em uma coluna e executa
 * as ações impressas nela. As três colunas coloridas estão associadas a uma
 * {@link Regiao} do mapa (ver {@link Regiao#getColuna()}); a coluna BRANCA é a
 * que permite recuperar todas as peças de ativação usadas.
 */
enum Coluna {
	VERMELHA("Vermelha"),
	VERDE("Verde"),
	AMARELA("Amarela"),
	BRANCA("Branca");

	private final String nome;

	Coluna(String nome) {
		this.nome = nome;
	}

	String getNome() {
		return nome;
	}

	@Override
	public String toString() {
		return nome;
	}
}
