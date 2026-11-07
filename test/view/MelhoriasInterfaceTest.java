package view;

import static org.junit.Assert.*;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import model.ModelAPI;
import model.MelhoriasModelAPITest;
import model.PreparacaoCompletaTest;
import org.junit.Test;

/** Confirma compras e escolhas obrigatórias pelo Controller, na EDT. */
public class MelhoriasInterfaceTest {
	private static <T> List<T> componentes(Container raiz, Class<T> tipo) {
		List<T> encontrados = new ArrayList<>();
		for (Component c : raiz.getComponents()) {
			if (tipo.isInstance(c)) encontrados.add(tipo.cast(c));
			if (c instanceof Container) encontrados.addAll(componentes((Container) c, tipo));
		}
		return encontrados;
	}
	private static JButton botao(Container raiz, String prefixo) {
		for (JButton botao : componentes(raiz, JButton.class)) if (botao.getText().startsWith(prefixo)) return botao;
		throw new AssertionError("Botão ausente: " + prefixo);
	}
	@Test public void compraAtualizaMercadoECartelaSemReaproveitarSelecao() throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override public void run() {
				ModelAPI api = ModelAPI.getInstance();
				assertTrue(api.novaPartidaCompleta(List.of("Ana", "Bia"), List.of("OGEDEI", "ALTANI"), PreparacaoCompletaTest.cadastro()));
				assertTrue(api.escolherCartaSecreta(0));
				assertTrue(api.escolherCartaSecreta(0));
				PainelMelhorias painel = new PainelMelhorias();
				try {
					assertFalse(botao(painel, "Comprar").isEnabled());
					assertTrue(api.ativarColuna("VERDE"));
					List<JRadioButton> opcoes = componentes(painel, JRadioButton.class);
					assertEquals(6, opcoes.size()); // cinco abertas e a pilha fechada.
					assertTrue(opcoes.get(5).getText().contains("às cegas"));
					String coluna = api.getMercadoMelhorias().get(0).split(":")[0];
					opcoes.get(0).doClick();
					assertTrue(botao(painel, "Comprar").isEnabled());
					botao(painel, "Comprar").doClick();
					assertEquals(4, api.getMercadoMelhorias().size());
					assertEquals(1, api.getMelhoriasJogador(0, coluna).size());
					assertFalse(botao(painel, "Comprar").isEnabled());
					boolean descricao = false;
					for (JLabel label : componentes(painel, JLabel.class)) if (label.getText().contains("próximo turno")) descricao = true;
					assertTrue(descricao);
				} finally { api.remove(painel); }
			}
		});
	}
	@Test public void escolhaGratuitaDesbloqueiaTurnoPeloBotao() throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override public void run() {
				ModelAPI api = ModelAPI.getInstance();
				MelhoriasModelAPITest.prepararRecompensaPendente();
				PainelMelhorias painel = new PainelMelhorias();
				try {
					assertTrue(botao(painel, "Escolher grátis").isVisible());
					assertFalse(botao(painel, "Escolher grátis").isEnabled());
					componentes(painel, JRadioButton.class).get(0).doClick();
					assertFalse(botao(painel, "Comprar").isEnabled());
					assertTrue(botao(painel, "Escolher grátis").isEnabled());
					int moedas = api.getTributosJogador(0, "MOEDA");
					botao(painel, "Escolher grátis").doClick();
					assertFalse(api.isMelhoriaGratuitaPendente());
					assertFalse(botao(painel, "Escolher grátis").isVisible());
					assertEquals(1, api.getMelhoriasJogador(0, "BRANCA").size());
					assertEquals(moedas, api.getTributosJogador(0, "MOEDA"));
					if (api.isKhanPendente()) assertTrue(api.moverKhan(api.getDestinosDoKhan().get(0)));
					assertTrue(api.podeTerminarTurno());
				} finally { api.remove(painel); }
			}
		});
	}
}
