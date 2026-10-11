package model;

/**
 * Um peão de jogador sobre o mapa.
 *
 * Cada jogador tem 1 peão, exceto o herdeiro Altani, que tem 2 (ver
 * Herdeiro#getQuantidadePeoes()). A posição do peão é mantida pelo
 * Tabuleiro, que conhece as rotas e as capacidades das paradas; esta
 * classe guarda apenas a quem o peão pertence.
 */
class Peao {

	private final Cor cor;
	private final int indice; // 0 para o 1º peão do jogador, 1 para o 2º (Altani)

	Peao(Cor cor, int indice) {
		if (cor == null) {
			throw new IllegalArgumentException("Peão precisa de uma cor");
		}
		this.cor = cor;
		this.indice = indice;
	}

	Cor getCor() {
		return cor;
	}

	int getIndice() {
		return indice;
	}

	@Override
	public String toString() {
		return "Peão " + (indice + 1) + " " + cor;
	}
}
