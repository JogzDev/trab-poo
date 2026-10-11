package model;

/**
 * Os três tipos de peça de tributo que existem nas províncias do mapa.
 *
 * - ESPADA: permite atacar cidades (tomar tesouros).
 * - YURT: permite construir um yurt do jogador no tabuleiro.
 * - MOEDA: duas moedas compram uma peça de melhoria.
 *
 */
enum TipoTributo {
	ESPADA("Espada"),
	YURT("Yurt"),
	MOEDA("Moeda");

	private final String nome;

	TipoTributo(String nome) {
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
