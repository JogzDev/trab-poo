package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;

import controller.Controlador;
import model.ModelAPI;
import model.ObservadoIF;
import model.ObservadorIF;

/**
 * Alterna entre preparação e partida conforme as notificações do Model.
 * Tabuleiro e painel de turno limitados a 1400 x 1050.
 */
public class JanelaJogo extends JFrame implements ObservadorIF {
	private final CardLayout telas = new CardLayout();
	private final JPanel conteudo = new JPanel(telas);
	private final JTabbedPane lateral = new JTabbedPane();

	public static final int LARGURA_MAX = 1400;
	public static final int ALTURA_MAX = 1050;

	public JanelaJogo() {
		super("Herdeiros de Khan");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		JPanel jogo = new JPanel(new BorderLayout());
		PainelTabuleiro mapa = new PainelTabuleiro();
		JPanel areaMapa = new JPanel(new BorderLayout());
		areaMapa.add(new PainelRota(mapa), BorderLayout.NORTH);
		areaMapa.add(mapa, BorderLayout.CENTER);
		jogo.add(areaMapa, BorderLayout.CENTER);
		lateral.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		lateral.setPreferredSize(new Dimension(PainelTurno.LARGURA + 12, PainelTabuleiro.ALTURA));
		lateral.addTab("Turno", new JScrollPane(new PainelTurno()));
		lateral.addTab("Ações", new JScrollPane(new PainelAcoes()));
		lateral.addTab("Melhorias", new JScrollPane(new PainelMelhorias()));
		lateral.addTab("Entregas", new JScrollPane(new PainelEntregas()));
		lateral.addTab("Objetivos", new JScrollPane(new PainelObjetivos()));
		lateral.addTab("Resultado", new JScrollPane(new PainelResultado()));
		jogo.add(lateral, BorderLayout.LINE_END);
		conteudo.add(new PainelPreparacao(), "PREPARACAO");
		conteudo.add(jogo, "JOGO");
		add(conteudo, BorderLayout.CENTER);
		Controlador.getInstance().registra(this);
		notify(ModelAPI.getInstance());
		pack();
		setSize(Math.min(getWidth(), LARGURA_MAX), Math.min(getHeight(), ALTURA_MAX));
		// setMaximumSize não impede o usuário de maximizar um JFrame; sem
		// redimensionar, a janela nunca passa de 1400 x 1050.
		setResizable(false);
		setLocationRelativeTo(null);
	}

	@Override public void notify(ObservadoIF observado) {
		String etapa = Controlador.getInstance().getEtapaPartida();
		telas.show(conteudo, etapa.equals("PREPARACAO") ? "PREPARACAO" : "JOGO");
		if (etapa.equals("RESULTADO")) lateral.setSelectedIndex(5);
		else if (etapa.equals("ENTREGAS")) lateral.setSelectedIndex(3);
		else if (etapa.equals("MELHORIAS")) lateral.setSelectedIndex(2);
	}
}
