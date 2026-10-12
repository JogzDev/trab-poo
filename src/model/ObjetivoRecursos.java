package model;

import java.util.EnumMap;
import java.util.Map;

/** Objetivos de recursos, bônus e melhorias. Consultas nunca gastam peças. */
final class ObjetivoRecursos extends Objetivo {
	enum Criterio { ESPADAS, TESOUROS, TESOUROS_DIFERENTES, BONUS, LINHAS,
		DUAS_MELHORIAS_POR_COLUNA, MAIOR_COLUNA, MELHORIAS, SIMBOLOS, PARES, CAVALOS_OU_MAOS }
	private final Criterio criterio;
	ObjetivoRecursos(Criterio criterio, int... patamares) {
		super(patamares);
		if (criterio == null) throw new IllegalArgumentException("Critério obrigatório");
		this.criterio = criterio;
	}
	@Override int progresso(Jogador jogador, Partida partida, Inventario inventario) {
		Turno turno = partida.getTurno();
		boolean ativo = partida.getFase() == Fase.EM_ANDAMENTO && partida.getJogadorDaVez() == jogador;
		if (criterio == Criterio.ESPADAS) return jogador.contarTributo(TipoTributo.ESPADA)
				+ (ativo ? turno.getRecursoVirtual(TipoTributo.ESPADA) : 0);
		if (criterio == Criterio.TESOUROS || criterio == Criterio.TESOUROS_DIFERENTES) {
			int n = 0;
			for (TipoTesouro tipo : TipoTesouro.values()) {
				int saldo = inventario.contarTesouro(tipo) + (ativo ? turno.getTesouroVirtual(tipo) : 0);
				n += criterio == Criterio.TESOUROS ? saldo : (saldo > 0 ? 1 : 0);
			}
			return n;
		}
		if (criterio == Criterio.BONUS) return inventario.getBonus().size();
		if (criterio == Criterio.LINHAS) return inventario.linhasCompletas();
		Map<Melhoria.Simbolo, Integer> simbolos = new EnumMap<>(Melhoria.Simbolo.class);
		int total = 0;
		int colunasComDuas = 0;
		int maior = 0;
		for (Coluna coluna : Coluna.values()) {
			int n = inventario.contarMelhorias(coluna);
			total += n;
			maior = Math.max(maior, n);
			if (n >= 2) colunasComDuas++;
			for (Melhoria melhoria : inventario.getMelhorias(coluna)) simbolos.merge(melhoria.getSimbolo(), 1, Integer::sum);
		}
		switch (criterio) {
		case MELHORIAS: return total;
		case DUAS_MELHORIAS_POR_COLUNA: return colunasComDuas;
		case MAIOR_COLUNA: return maior;
		case SIMBOLOS: return simbolos.size();
		case PARES: return simbolos.values().stream().mapToInt(n -> n / 2).sum();
		case CAVALOS_OU_MAOS: return simbolos.getOrDefault(Melhoria.Simbolo.CAVALOS, 0) + simbolos.getOrDefault(Melhoria.Simbolo.MAO, 0);
		default: throw new IllegalStateException("Critério desconhecido");
		}
	}
}
