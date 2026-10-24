package view;

import java.awt.Color;

/**
 * Traduz os nomes de cor e de coluna devolvidos pela ModelAPI para cores do
 * Java2D. Fica na View porque a cor de tela é decisão de apresentação; o Model
 * só conhece os nomes ("AZUL", "VERMELHA" etc.).
 */
public final class CoresJogador {

	private CoresJogador() {
	}

	/** Cor de um jogador: AZUL, VERDE, VERMELHO, AMARELO ou PRETO. */
	public static Color doJogador(String cor) {
		if (cor == null) {
			return Color.GRAY;
		}
		switch (cor) {
		case "AZUL": return new Color(30, 90, 200);
		case "VERDE": return new Color(30, 140, 60);
		case "VERMELHO": return new Color(200, 40, 40);
		case "AMARELO": return new Color(230, 180, 0);
		case "PRETO": return new Color(30, 30, 30);
		default: return Color.GRAY;
		}
	}

	/** Cor de uma coluna da cartela: VERMELHA, VERDE, AMARELA ou BRANCA. */
	public static Color daColuna(String coluna) {
		if (coluna == null) {
			return Color.GRAY;
		}
		switch (coluna) {
		case "VERMELHA": return new Color(200, 40, 40);
		case "VERDE": return new Color(30, 140, 60);
		case "AMARELA": return new Color(230, 180, 0);
		case "BRANCA": return new Color(190, 190, 190);
		default: return Color.GRAY;
		}
	}
}
