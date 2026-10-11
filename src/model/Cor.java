package model;

/**
 * Cores disponíveis para os jogadores de Herdeiros de Khan.
 *
 * <p>O jogo suporta de 2 a 5 jogadores; cada um escolhe uma cor e recebe o
 * tabuleiro de jogador (herdeiro), 4 peças de ativação e 12 yurts dessa cor.
 *
 * <p>Os nomes das cores foram escolhidos a partir da contagem de componentes
 * (5 fichas de jogador / 60 yurts = 12 por cor). Se as cores físicas do jogo
 * forem outras, basta renomear as constantes aqui: nenhuma outra classe depende
 * dos nomes específicos.
 */
enum Cor {
	AZUL("Azul"),
	VERDE("Verde"),
	VERMELHO("Vermelho"),
	AMARELO("Amarelo"),
	PRETO("Preto");

	private final String nome;

	Cor(String nome) {
		this.nome = nome;
	}

	/** Nome legível da cor (para exibição pela View). */
	String getNome() {
		return nome;
	}

	@Override
	public String toString() {
		return nome;
	}
}
