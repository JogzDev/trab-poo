package model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Ações adicionais das melhorias/bônus; os recursos virtuais expiram no turno. */
final class EfeitosColuna {
	private final int cavalos;
	private final int maos;
	private final Map<TipoTributo, Integer> recursosVirtuais;

	EfeitosColuna(int cavalos, int maos, Map<TipoTributo, Integer> recursosVirtuais) {
		if (cavalos < 0 || maos < 0 || recursosVirtuais == null) {
			throw new IllegalArgumentException("Efeitos precisam de quantidades não negativas");
		}
		this.cavalos = cavalos;
		this.maos = maos;
		Map<TipoTributo, Integer> copia = new EnumMap<>(TipoTributo.class);
		for (Map.Entry<TipoTributo, Integer> item : recursosVirtuais.entrySet()) {
			if (item.getKey() == null || item.getValue() == null || item.getValue() < 0) {
				throw new IllegalArgumentException("Recurso virtual inválido");
			}
			copia.put(item.getKey(), item.getValue());
		}
		this.recursosVirtuais = Collections.unmodifiableMap(copia);
	}

	static EfeitosColuna semEfeitos() {
		return new EfeitosColuna(0, 0, Collections.emptyMap());
	}

	int getCavalos() { return cavalos; }
	int getMaos() { return maos; }
	Map<TipoTributo, Integer> getRecursosVirtuais() { return recursosVirtuais; }
}
