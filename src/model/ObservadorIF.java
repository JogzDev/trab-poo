package model;

/**
 * Observador do padrão Observer (INF1636, cap. 17).
 *
 * Os painéis da View implementam esta interface e se registram na fachada
 * por meio do Controlador. Quando o estado da partida muda, a fachada chama
 * notify(this); o observador então consulta o que precisa redesenhar
 * (callback), por exemplo a posição dos peões ou os destinos possíveis.
 */
public interface ObservadorIF {
	void notify(ObservadoIF o);
}
