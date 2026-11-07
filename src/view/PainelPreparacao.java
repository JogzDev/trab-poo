package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import controller.Controlador;
import model.ModelAPI;
import model.ObservadoIF;
import model.ObservadorIF;

/** Preparação local com passagem da tela antes de revelar as duas cartas. */
public class PainelPreparacao extends JPanel implements ObservadorIF {
	private final JLabel jogador = new JLabel();
	private final JButton revelar = new JButton("Estou pronto para ver minhas cartas");
	private final JPanel opcoes = new JPanel(new GridLayout(0, 1, 8, 8));
	private final JButton confirmar = new JButton("Confirmar carta secreta");
	private final ButtonGroup grupo = new ButtonGroup();
	private int exibido = -1;
	private boolean reveladas;

	public PainelPreparacao() {
		setLayout(new GridBagLayout());
		JPanel cartela = new JPanel(new BorderLayout(16, 16));
		cartela.setPreferredSize(new Dimension(800, 420));
		cartela.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(200, 200, 200)),
				BorderFactory.createEmptyBorder(24, 24, 24, 24)));
		add(cartela);
		setPreferredSize(new Dimension(850, 600));
		JPanel topo = new JPanel(new GridLayout(0, 1, 8, 8));
		topo.add(new JLabel("Preparação — escolha uma das duas cartas e descarte a outra."));
		jogador.setFont(jogador.getFont().deriveFont(Font.BOLD, 16f));
		topo.add(jogador);
		revelar.addActionListener(new AcaoRevelar());
		topo.add(revelar);
		cartela.add(topo, BorderLayout.NORTH);
		cartela.add(opcoes, BorderLayout.CENTER);
		confirmar.addActionListener(new AcaoConfirmar());
		cartela.add(confirmar, BorderLayout.SOUTH);
		Controlador.getInstance().registra(this);
		atualizar();
	}

	private class AcaoRevelar implements ActionListener {
		@Override public void actionPerformed(ActionEvent e) {
			if (!ModelAPI.getInstance().isPreparacao()) return;
			reveladas = true;
			atualizar();
		}
	}

	private class AcaoSelecionar implements ActionListener {
		@Override public void actionPerformed(ActionEvent e) { confirmar.setEnabled(true); }
	}

	private class AcaoConfirmar implements ActionListener {
		@Override public void actionPerformed(ActionEvent e) {
			if (reveladas && grupo.getSelection() != null) {
				Controlador.getInstance().escolherCartaSecreta(Integer.parseInt(grupo.getSelection().getActionCommand()));
			}
		}
	}

	@Override public void notify(ObservadoIF observado) { atualizar(); }

	private void atualizar() {
		ModelAPI m = ModelAPI.getInstance();
		if (!m.isPreparacao()) {
			opcoes.removeAll();
			confirmar.setEnabled(false);
			return;
		}
		int atual = m.getJogadorPreparando();
		if (exibido != atual) {
			exibido = atual;
			reveladas = false;
		}
		jogador.setText("<html>" + (reveladas ? "Carta secreta de " : "Passe a tela para ")
				+ "<b>" + escapar(m.getNomeJogador(atual)) + "</b>.</html>");
		revelar.setVisible(!reveladas);
		opcoes.removeAll();
		while (grupo.getButtonCount() > 0) grupo.remove(grupo.getElements().nextElement());
		confirmar.setVisible(reveladas);
		confirmar.setEnabled(false);
		if (reveladas) {
			List<String> cartas = m.getOpcoesCartaSecreta();
			for (int i = 0; i < cartas.size(); i++) {
				JRadioButton opcao = new JRadioButton("<html><div style='width:480px'>" + escapar(cartas.get(i)) + "</div></html>");
				opcao.setActionCommand(Integer.toString(i));
				opcao.addActionListener(new AcaoSelecionar());
				grupo.add(opcao);
				opcoes.add(opcao);
			}
		}
		revalidate();
		repaint();
	}

	static String escapar(String texto) {
		return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
