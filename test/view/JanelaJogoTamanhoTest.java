package view;

import static org.junit.Assert.*;
import java.awt.Dimension;
import java.awt.Rectangle;
import org.junit.Test;

/** Tamanho da janela do jogo diante de telas pequenas e grandes. */
public class JanelaJogoTamanhoTest {
	/**
	 * Objetivo: a janela cabe na área útil de um notebook.
	 * Entrada: janela preferida de 1212 x 1032; área útil de 1512 x 944 (tela de 1512 x 982 menos a barra de menus).
	 * Saída esperada: 1124 x 944, perdendo na largura o mesmo que perdeu na altura.
	 */
	@Test public void cabeNaTelaDoNotebook() {
		assertEquals(new Dimension(1124, 944), JanelaJogo.tamanhoQueCabe(new Dimension(1212, 1032), new Rectangle(0, 38, 1512, 944)));
	}
	/**
	 * Objetivo: em tela grande, nada muda.
	 * Entrada: janela preferida de 1212 x 1032; área útil de 3440 x 1415.
	 * Saída esperada: 1212 x 1032.
	 */
	@Test public void telaGrandeMantemTamanhoPreferido() {
		assertEquals(new Dimension(1212, 1032), JanelaJogo.tamanhoQueCabe(new Dimension(1212, 1032), new Rectangle(0, 25, 3440, 1415)));
	}
	/**
	 * Objetivo: o limite do enunciado continua valendo.
	 * Entrada: janela preferida de 1600 x 1200; área útil de 3440 x 1415.
	 * Saída esperada: no máximo 1400 x 1050.
	 */
	@Test public void respeitaLimiteDoEnunciado() {
		Dimension d = JanelaJogo.tamanhoQueCabe(new Dimension(1600, 1200), new Rectangle(0, 0, 3440, 1415));
		assertTrue(d.width <= JanelaJogo.LARGURA_MAX && d.height <= JanelaJogo.ALTURA_MAX);
	}
}
