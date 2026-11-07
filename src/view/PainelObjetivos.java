package view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import controller.Controlador;
import model.ModelAPI;
import model.ObservadoIF;
import model.ObservadorIF;

/** Cartas abertas e consulta privada da carta do jogador da vez via Observer. */
public class PainelObjetivos extends JPanel implements ObservadorIF {
	public PainelObjetivos() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
		Controlador.getInstance().registra(this);
		atualizar();
	}
	@Override public Dimension getPreferredSize() { return new Dimension(PainelTurno.LARGURA - 20, super.getPreferredSize().height); }
	@Override public void notify(ObservadoIF observado) { atualizar(); }
	private class Revelar implements ActionListener {
		@Override public void actionPerformed(ActionEvent evento) { Controlador.getInstance().mostrarCartaSecreta(); }
	}
	private class Ocultar implements ActionListener {
		@Override public void actionPerformed(ActionEvent evento) { Controlador.getInstance().ocultarCartaSecreta(); }
	}
	private void atualizar() {
		removeAll();
		ModelAPI m = ModelAPI.getInstance();
		if (m.temPartida()) {
			adicionar("<b>Kurultai: " + m.getPosicaoKurultai() + " / " + m.getLimiteKurultai() + "</b>"
					+ (m.getRodadaFinal() < 0 ? "" : "<br>Rodada final: " + m.getRodadaFinal()));
			for (int i = 0; i < m.getNumJogadores(); i++) adicionar(PainelPreparacao.escapar(m.getNomeJogador(i)) + ": " + m.getVotosJogador(i) + " voto(s)");
		}
		adicionar("<b>Objetivos abertos</b>");
		if (!m.isPartidaCompleta()) adicionar("As cartas exigem uma partida com cadastro das peças.");
		else if (m.isPreparacao()) adicionar("Conclua a preparação para consultar seus objetivos.");
		else if (m.isPartidaEncerrada()) adicionar("Consulte as cartas secretas e os votos na aba Resultado.");
		else {
			adicionar("Progresso de " + PainelPreparacao.escapar(m.getNomeJogador(m.getJogadorDaVez()))
					+ ". O primeiro a cumprir uma carta aberta recebe 1 voto; ela sai de jogo sem reposição.");
			for (String carta : m.getObjetivosAbertosComProgresso()) adicionar(PainelPreparacao.escapar(carta));
			if (m.getObjetivosAbertosComProgresso().isEmpty()) adicionar("Todas as cartas abertas foram cumpridas.");
			adicionar("<b>Sua carta secreta</b>");
			if (m.isCartaSecretaVisivel()) {
				adicionar(PainelPreparacao.escapar(m.getCartaSecretaAtual()));
				JButton ocultar = new JButton("Ocultar carta secreta");
				ocultar.addActionListener(new Ocultar());
				adicionar(ocultar);
			} else {
				adicionar("Só o jogador da vez deve consultar esta carta. Ela volta a ficar oculta na troca do turno.");
				JButton revelar = new JButton("Revelar minha carta");
				revelar.addActionListener(new Revelar());
				adicionar(revelar);
			}
		}
		revalidate(); repaint();
	}
	private void adicionar(String texto) { adicionar(new JLabel("<html><div style='width:145px'>" + texto + "</div></html>")); }
	private void adicionar(javax.swing.JComponent componente) {
		componente.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(componente); add(Box.createVerticalStrut(12));
	}
}
