package model;

import java.util.List;

/** Catálogo dos 14 conselheiros abertos e 10 secretos (manual, pp. 18-19). */
final class CartasOficiais {
	private CartasOficiais() { }
	static List<Carta> abertas() {
		return List.of(
			recurso("Quatro espadas", ObjetivoRecursos.Criterio.ESPADAS, 4),
			mapa("Seis yurts", ObjetivoMapa.Criterio.YURTS, 6),
			mapa("Dez yurts", ObjetivoMapa.Criterio.YURTS, 10),
			recurso("Quatro tesouros", ObjetivoRecursos.Criterio.TESOUROS, 4),
			recurso("Cinco tipos de tesouro", ObjetivoRecursos.Criterio.TESOUROS_DIFERENTES, 5),
			mapa("Um yurt por região", ObjetivoMapa.Criterio.YURTS_POR_REGIAO, 1),
			mapa("Dois yurts por região", ObjetivoMapa.Criterio.YURTS_POR_REGIAO, 2),
			recurso("Dois bônus", ObjetivoRecursos.Criterio.BONUS, 2),
			recurso("Três bônus", ObjetivoRecursos.Criterio.BONUS, 3),
			mapa("Duas conquistas", ObjetivoMapa.Criterio.CIDADES, 2),
			mapa("Três conquistas", ObjetivoMapa.Criterio.CIDADES, 3),
			recurso("Uma linha", ObjetivoRecursos.Criterio.LINHAS, 1),
			recurso("Duas colunas", ObjetivoRecursos.Criterio.DUAS_MELHORIAS_POR_COLUNA, 2),
			recurso("Uma coluna completa", ObjetivoRecursos.Criterio.MAIOR_COLUNA, 4));
	}
	static List<Carta> secretas() {
		return List.of(
			mapa("Viajante", ObjetivoMapa.Criterio.REDE, 3, 6, 8),
			recurso("Soldado", ObjetivoRecursos.Criterio.MELHORIAS, 4, 7, 10),
			recurso("Colecionador", ObjetivoRecursos.Criterio.SIMBOLOS, 4, 6, 7),
			recurso("Sacerdote", ObjetivoRecursos.Criterio.PARES, 1, 2, 4),
			mapa("Governador", ObjetivoMapa.Criterio.REGIOES_DESENVOLVIDAS, 1, 2, 3),
			mapa("Bajulador", ObjetivoMapa.Criterio.PROVINCIAS_KHAN, 2, 3, 4),
			mapa("Prefeito", ObjetivoMapa.Criterio.CIDADES_COM_DOIS_YURTS, 2, 3, 4),
			mapa("Cidadão", ObjetivoMapa.Criterio.CIDADES_COM_UM_YURT, 3, 6, 8),
			recurso("Cavaleiro", ObjetivoRecursos.Criterio.CAVALOS_OU_MAOS, 2, 4, 5),
			mapa("Mensageiro", ObjetivoMapa.Criterio.CONSELHEIROS, 2, 4, 6));
	}
	private static Carta mapa(String nome, ObjetivoMapa.Criterio criterio, int... patamares) {
		return new Carta(nome, new ObjetivoMapa(criterio, patamares));
	}
	private static Carta recurso(String nome, ObjetivoRecursos.Criterio criterio, int... patamares) {
		return new Carta(nome, new ObjetivoRecursos(criterio, patamares));
	}
}
