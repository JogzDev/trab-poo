package model;

/**
 * Objeto observado do padrão Observer (INF1636, cap. 17).
 *
 * Implementado pela fachada ModelAPI: mantém a lista de observadores e os
 * notifica depois de cada alteração bem-sucedida no estado da partida.
 */
public interface ObservadoIF {
	void add(ObservadorIF o);
	void remove(ObservadorIF o);
}
