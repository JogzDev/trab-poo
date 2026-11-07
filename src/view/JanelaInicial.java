package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.Controlador;
import model.ModelAPI;

/**
 * Janela de configuração da partida (2ª iteração, Parte 3): número de
 * jogadores (2 a 5), nome e herdeiro de cada um, sem herdeiro repetido.
 * Recusas aparecem num JLabel da própria janela, sem JOptionPane.
 */
public class JanelaInicial extends JFrame {

	private static final int MIN_JOGADORES = 2;
	private static final int MAX_JOGADORES = 5;

	private final List<String> herdeiros = ModelAPI.getInstance().getHerdeiros();
	private final JComboBox<Integer> cbQuantidade = new JComboBox<Integer>();
	private final JTextField[] tfNomes = new JTextField[MAX_JOGADORES];
	private final JComboBox<String>[] cbHerdeiros = novosCombos();
	private final JPanel[] paineisLinha = new JPanel[MAX_JOGADORES];
	private final JCheckBox cbCompleta = new JCheckBox("Preparar com cartas, tesouros e melhorias");
	private final JLabel lbPecas = new JLabel("Modo reduzido: sem cartas e melhorias. Cadastro oficial pendente.");
	private List<String> pecas;
	private final JLabel lbAviso = new JLabel(" ");

	/** Mostra só as linhas dos jogadores escolhidos. */
	private class AcaoQuantidade implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			mostrarLinhas();
		}
	}

	/** Valida o formulário e pede ao Controlador para criar a partida. */
	private class AcaoIniciar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			iniciar();
		}
	}

	private class AcaoCarregarPecas implements ActionListener {
		@Override public void actionPerformed(ActionEvent e) {
			JFileChooser seletor = new JFileChooser();
			seletor.setDialogTitle("Selecionar cadastro das peças");
			if (seletor.showOpenDialog(JanelaInicial.this) != JFileChooser.APPROVE_OPTION) return;
			try {
				List<String> novas = Files.readAllLines(seletor.getSelectedFile().toPath(), StandardCharsets.UTF_8);
				pecas = novas;
				cbCompleta.setSelected(true);
				lbPecas.setText("<html>Cadastro: " + PainelPreparacao.escapar(seletor.getSelectedFile().getName()) + "</html>");
				lbAviso.setText(" ");
			} catch (IOException ex) {
				lbAviso.setText("Não foi possível ler o cadastro das peças.");
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static JComboBox<String>[] novosCombos() {
		return new JComboBox[MAX_JOGADORES];
	}

	public JanelaInicial() {
		super("Herdeiros de Khan");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout(8, 8));

		JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
		topo.add(new JLabel("Número de jogadores:"));
		for (int n = MIN_JOGADORES; n <= MAX_JOGADORES; n++) {
			cbQuantidade.addItem(n);
		}
		cbQuantidade.addActionListener(new AcaoQuantidade());
		topo.add(cbQuantidade);
		add(topo, BorderLayout.NORTH);

		JPanel linhas = new JPanel(new GridLayout(MAX_JOGADORES, 1, 4, 4));
		linhas.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
		for (int i = 0; i < MAX_JOGADORES; i++) {
			JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lbJogador = new JLabel("Jogador " + (i + 1) + ":");
			tfNomes[i] = new JTextField("Jogador " + (i + 1), 12);
			cbHerdeiros[i] = new JComboBox<String>(herdeiros.toArray(new String[0]));
			cbHerdeiros[i].setSelectedIndex(i % herdeiros.size());
			linha.add(lbJogador);
			linha.add(tfNomes[i]);
			linha.add(cbHerdeiros[i]);
			linhas.add(linha);
			paineisLinha[i] = linha;
		}
		JPanel centro = new JPanel(new BorderLayout(8, 8));
		centro.add(linhas, BorderLayout.CENTER);
		JPanel configuracao = new JPanel(new GridLayout(3, 1, 4, 4));
		configuracao.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
		configuracao.add(cbCompleta);
		JButton carregar = new JButton("Selecionar cadastro das peças…");
		carregar.addActionListener(new AcaoCarregarPecas());
		configuracao.add(carregar);
		configuracao.add(lbPecas);
		centro.add(configuracao, BorderLayout.SOUTH);
		add(centro, BorderLayout.CENTER);

		JPanel base = new JPanel(new BorderLayout(4, 4));
		base.setBorder(BorderFactory.createEmptyBorder(4, 12, 12, 12));
		lbAviso.setForeground(new Color(180, 0, 0));
		JButton btIniciar = new JButton("Iniciar partida");
		btIniciar.addActionListener(new AcaoIniciar());
		base.add(lbAviso, BorderLayout.CENTER);
		base.add(btIniciar, BorderLayout.EAST);
		add(base, BorderLayout.SOUTH);

		mostrarLinhas();
		pack();
		// Sem redimensionar, a janela nunca passa de 1400 x 1050 (enunciado).
		setResizable(false);
		setLocationRelativeTo(null);
	}

	private int quantidade() {
		return (Integer) cbQuantidade.getSelectedItem();
	}

	private void mostrarLinhas() {
		int n = quantidade();
		for (int i = 0; i < MAX_JOGADORES; i++) {
			paineisLinha[i].setVisible(i < n);
		}
		lbAviso.setText(" ");
	}

	private void iniciar() {
		List<String> nomes = new ArrayList<String>();
		List<String> escolhidos = new ArrayList<String>();
		Set<String> usados = new HashSet<String>();
		for (int i = 0; i < quantidade(); i++) {
			String nome = tfNomes[i].getText().trim();
			String herdeiro = (String) cbHerdeiros[i].getSelectedItem();
			if (nome.isEmpty()) {
				lbAviso.setText("Dê um nome ao jogador " + (i + 1) + ".");
				return;
			}
			if (!usados.add(herdeiro)) {
				lbAviso.setText("Cada jogador precisa de um herdeiro diferente.");
				return;
			}
			nomes.add(nome);
			escolhidos.add(herdeiro);
		}
		if (cbCompleta.isSelected() && pecas == null) {
			lbAviso.setText("Selecione o cadastro das peças antes de preparar a partida.");
			return;
		}
		boolean criada = cbCompleta.isSelected()
				? Controlador.getInstance().novaPartidaCompleta(nomes, escolhidos, pecas)
				: Controlador.getInstance().novaPartida(nomes, escolhidos);
		if (criada) {
			dispose();
		} else {
			String erro = cbCompleta.isSelected() ? ModelAPI.getInstance().getErroConfiguracao() : "Configuração inválida.";
			lbAviso.setText("<html><div style='width:360px'>" + PainelPreparacao.escapar(erro) + "</div></html>");
		}
	}
}
