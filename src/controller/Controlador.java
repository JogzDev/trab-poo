package controller;

import java.util.List;

import model.ModelAPI;
import model.ObservadorIF;

/**
 * Controlador da aplicação (Singleton, INF1636 cap. 17).
 *
 * CONTRATO DA 2ª ITERAÇÃO: as assinaturas abaixo foram combinadas entre as
 * três partes e não devem mudar sem avisar o grupo. A View chama somente
 * estes métodos para agir; para ler o estado, os observadores consultam a
 * ModelAPI recebida em notify (callback do padrão Observer).
 *
 * Esta versão apenas repassa as chamadas para a fachada do Model, para que a
 * Parte 2 (tabuleiro) e a Parte 3 (janelas e turno) possam trabalhar em
 * paralelo desde o início. A Parte 3 completa o fluxo: abrir as janelas,
 * ignorar cliques recusados e manter a troca de turno e o fim da partida
 * visíveis nos painéis, sem caixas de diálogo.
 */
public final class Controlador {

	private static Controlador instancia = null;

	private final ModelAPI model = ModelAPI.getInstance();

	private Controlador() {
	}

	public static Controlador getInstance() {
		if (instancia == null) {
			instancia = new Controlador();
		}
		return instancia;
	}

	/** Os painéis chamam no construtor: Controlador.getInstance().registra(this). */
	public void registra(ObservadorIF o) {
		model.add(o);
	}

	public void desregistra(ObservadorIF o) {
		model.remove(o);
	}

	/** Chamado pela JanelaInicial (Parte 3). */
	public boolean novaPartida(List<String> nomes, List<String> herdeiros) {
		return model.novaPartida(nomes, herdeiros);
	}

	/**
	 * Chamados pelo PainelTabuleiro (Parte 2), porque todo deslocamento é feito
	 * clicando no mapa: numa parada (mover o peão), num local do Khan
	 * (província do Khan ou ModelAPI.AREA_MELHORIAS) ou no outro peão de Altani.
	 * Um clique recusado não faz nada: o painel não mostra mensagem.
	 */
	public boolean paradaClicada(String parada) {
		return model.moverPara(parada);
	}

	public boolean moverKhan(String local) {
		return model.moverKhan(local);
	}

	public boolean escolherPeao(int peao) {
		return model.escolherPeao(peao);
	}

	/** Chamados pelo PainelTurno (Parte 1). */
	public boolean ativarColuna(String coluna) {
		return model.ativarColuna(coluna);
	}

	public boolean terminarTurno() {
		return model.terminarTurno();
	}
}
