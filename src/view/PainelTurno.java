package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import controller.Controlador;
import model.ModelAPI;
import model.ObservadoIF;
import model.ObservadorIF;

/**
 * Painel lateral do turno (2ª iteração, Parte 1).
 *
 * Mostra de quem é a vez e oferece as ações do turno que não dependem de
 * clicar no mapa: ativar uma coluna da cartela, escolher o peão (Altani),
 * mover o Khan quando a coluna exige e terminar o turno. O movimento do peão
 * é feito clicando no PainelTabuleiro.
 *
 * Segue o padrão Observer do cap. 17: registra-se pelo Controlador e, a cada
 * notificação, consulta a ModelAPI para habilitar só as ações permitidas.
 * As ações passam sempre pelo Controlador; o painel não altera o Model.
 */
public class PainelTurno extends JPanel implements ObservadorIF {

	public static final int LARGURA = 260;

	/** As colunas na ordem da cartela, com os nomes do contrato da ModelAPI. */
	private static final String[] COLUNAS = { "VERMELHA", "VERDE", "AMARELA", "BRANCA" };

	private final JLabel lbJogador = new JLabel(" ");
	private final JLabel lbHerdeiro = new JLabel(" ");
	private final JLabel lbRodada = new JLabel(" ");
	private final Map<String, JButton> btColunas = new LinkedHashMap<>();
	private final JLabel lbCavalos = new JLabel(" ");
	private final JPanel pnPeoes = new JPanel(new GridLayout(1, 2, 6, 0));
	private final JRadioButton[] rbPeoes = { new JRadioButton("Peão 1"), new JRadioButton("Peão 2") };
	private final JPanel pnKhan = new JPanel(new BorderLayout(6, 6));
	private final DefaultComboBoxModel<String> destinosKhan = new DefaultComboBoxModel<>();
	private final JComboBox<String> cbKhan = new JComboBox<>(destinosKhan);
	private final JButton btMoverKhan = new JButton("Mover Khan");
	private final JButton btTerminar = new JButton("Terminar turno");
	private final JLabel lbMensagem = new JLabel(" ");

	public PainelTurno() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		setPreferredSize(new Dimension(LARGURA, 600));

		lbJogador.setFont(lbJogador.getFont().deriveFont(Font.BOLD, 18f));
		adicionar(lbJogador);
		adicionar(lbHerdeiro);
		adicionar(lbRodada);
		add(Box.createVerticalStrut(12));

		adicionar(titulo("Ativar coluna"));
		for (String coluna : COLUNAS) {
			JButton bt = new JButton(nomeLegivel(coluna));
			bt.setHorizontalAlignment(JButton.LEFT);
			bt.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(0, 8, 0, 0, CoresJogador.daColuna(coluna)),
					BorderFactory.createEmptyBorder(6, 8, 6, 8)));
			bt.addActionListener(e -> agir(Controlador.getInstance().ativarColuna(coluna)));
			btColunas.put(coluna, bt);
			adicionar(bt);
			add(Box.createVerticalStrut(4));
		}
		add(Box.createVerticalStrut(8));
		adicionar(lbCavalos);
		add(Box.createVerticalStrut(12));

		ButtonGroup grupo = new ButtonGroup();
		for (int i = 0; i < rbPeoes.length; i++) {
			int peao = i;
			grupo.add(rbPeoes[i]);
			pnPeoes.add(rbPeoes[i]);
			rbPeoes[i].addActionListener(e -> agir(Controlador.getInstance().escolherPeao(peao)));
		}
		pnPeoes.setBorder(BorderFactory.createTitledBorder("Peão que age"));
		adicionar(pnPeoes);
		add(Box.createVerticalStrut(8));

		pnKhan.setBorder(BorderFactory.createTitledBorder("Mover o Khan"));
		pnKhan.add(cbKhan, BorderLayout.CENTER);
		pnKhan.add(btMoverKhan, BorderLayout.PAGE_END);
		btMoverKhan.addActionListener(e -> agir(Controlador.getInstance().moverKhan((String) cbKhan.getSelectedItem())));
		adicionar(pnKhan);
		add(Box.createVerticalStrut(12));

		btTerminar.addActionListener(e -> agir(Controlador.getInstance().terminarTurno()));
		adicionar(btTerminar);
		add(Box.createVerticalStrut(8));
		adicionar(lbMensagem);
		add(Box.createVerticalGlue());

		Controlador.getInstance().registra(this);
		atualizar(ModelAPI.getInstance());
	}

	@Override
	public void notify(ObservadoIF o) {
		atualizar((ModelAPI) o);
	}

	/** Lê o estado atual do Model e habilita somente as ações permitidas. */
	private void atualizar(ModelAPI m) {
		if (!m.temPartida()) {
			lbJogador.setText("Nenhuma partida");
			habilitarTudo(false);
			return;
		}
		int vez = m.getJogadorDaVez();
		lbJogador.setText(m.getNomeJogador(vez));
		lbJogador.setForeground(CoresJogador.doJogador(m.getCorJogador(vez)));
		lbHerdeiro.setText("Herdeiro: " + nomeLegivel(m.getHerdeiroJogador(vez)));
		lbRodada.setText("Rodada " + m.getRodada());

		if (m.isPartidaEncerrada()) {
			lbJogador.setText("Partida encerrada");
			lbJogador.setForeground(Color.DARK_GRAY);
			habilitarTudo(false);
			lbMensagem.setText(" ");
			return;
		}

		List<String> livres = m.getColunasDisponiveis();
		String ativada = m.getColunaAtivada();
		for (Map.Entry<String, JButton> item : btColunas.entrySet()) {
			String nome = item.getKey();
			JButton bt = item.getValue();
			bt.setText(html("<b>" + nomeLegivel(nome) + "</b><br><small>" + m.getDescricaoColuna(nome) + "</small>", 150));
			bt.setEnabled(ativada == null && livres.contains(nome));
		}
		lbCavalos.setText(html(ativada == null ? "Nenhuma coluna ativada"
				: "Coluna " + nomeLegivel(ativada) + ": " + m.getCavalosRestantes() + " cavalo(s) restante(s)", 200));

		boolean altani = m.getNumPeoes(vez) > 1;
		pnPeoes.setVisible(altani);
		if (altani) {
			int ativo = m.getPeaoAtivo();
			for (int i = 0; i < rbPeoes.length; i++) {
				rbPeoes[i].setSelected(i == ativo);
				rbPeoes[i].setEnabled(m.podeEscolherPeao());
			}
		}

		boolean khan = m.isKhanPendente();
		pnKhan.setVisible(khan);
		destinosKhan.removeAllElements();
		for (String provincia : m.getDestinosDoKhan()) {
			destinosKhan.addElement(provincia);
		}
		btMoverKhan.setEnabled(khan && destinosKhan.getSize() > 0);

		btTerminar.setEnabled(m.podeTerminarTurno());
		lbMensagem.setText(dica(m, ativada, khan));
		lbMensagem.setForeground(Color.DARK_GRAY);

		revalidate();
		repaint();
	}

	/** Mostra um aviso quando o Controlador recusa a ação. */
	private void agir(boolean aceita) {
		if (!aceita) {
			lbMensagem.setText(html("Jogada não permitida pelas regras.", 200));
			lbMensagem.setForeground(new Color(180, 30, 30));
		}
	}

	private static String dica(ModelAPI m, String ativada, boolean khan) {
		if (ativada == null) {
			return html("Escolha uma coluna para ativar.", 200);
		}
		if (m.getCavalosRestantes() > 0 && !m.getDestinosPossiveis().isEmpty()) {
			return html("Clique numa parada destacada no mapa para mover o peão.", 200);
		}
		if (khan) {
			return html("Mova o Khan antes de terminar o turno.", 200);
		}
		return html("Termine o turno quando quiser.", 200);
	}

	private void habilitarTudo(boolean valor) {
		for (JButton bt : btColunas.values()) {
			bt.setEnabled(valor);
		}
		for (JRadioButton rb : rbPeoes) {
			rb.setEnabled(valor);
		}
		btMoverKhan.setEnabled(valor);
		btTerminar.setEnabled(valor);
		pnKhan.setVisible(valor);
	}

	private void adicionar(Component c) {
		if (c instanceof javax.swing.JComponent) {
			((javax.swing.JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
		}
		if (c instanceof JButton || c instanceof JPanel) {
			c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + 40));
		}
		add(c);
	}

	/** Texto HTML com largura fixa, para quebrar linha dentro do painel. */
	private static String html(String texto, int largura) {
		return "<html><div style='width:" + largura + "px'>" + texto + "</div></html>";
	}

	private static JLabel titulo(String texto) {
		JLabel lb = new JLabel(texto);
		lb.setFont(lb.getFont().deriveFont(Font.BOLD));
		return lb;
	}

	/** "VERMELHA" vira "Vermelha"; "ALTANI" vira "Altani". */
	private static String nomeLegivel(String constante) {
		if (constante == null || constante.isEmpty()) {
			return "";
		}
		return constante.charAt(0) + constante.substring(1).toLowerCase();
	}
}
