package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Calcula e valida movimentos sem alterar o tabuleiro ou os recursos.
 * A aplicação do resultado e o gasto dos cavalos pertencem à partida.
 */
final class Movimento {

	private Movimento() {
	}

	/**
	 * Encontra o caminho de menor custo até um destino diferente da origem.
	 * Empates usam menos passos e, em seguida, a ordem dos nomes percorridos.
	 * O cálculo escolhe pular yurts próprios intermediários para economizar
	 * cavalos; o destino sempre custa 1. A rota explícita permite não pular.
	 *
	 * @return null se os argumentos ou o movimento forem inválidos
	 */
	static Resultado calcular(Tabuleiro tabuleiro, Peao peao, Jogador jogador,
			String nomeDestino, int cavalos) {
		if (!entradaValida(tabuleiro, peao, jogador, cavalos)) {
			return null;
		}
		Parada origem = tabuleiro.posicaoDe(peao);
		Parada destino = parada(tabuleiro, nomeDestino);
		if (destino == null || destino.equals(origem)
				|| !tabuleiro.podeTerminarMovimentoEm(peao, destino)) {
			return null;
		}

		Comparator<Candidato> ordem = Movimento::comparar;
		PriorityQueue<Candidato> fila = new PriorityQueue<>(ordem);
		Map<Parada, Candidato> melhores = new HashMap<>();
		Candidato inicial = new Candidato(origem, 0, Collections.emptyList());
		melhores.put(origem, inicial);
		fila.add(inicial);

		while (!fila.isEmpty()) {
			Candidato atual = fila.remove();
			if (melhores.get(atual.parada) != atual) {
				continue;
			}
			if (atual.parada.equals(destino)) {
				return resultado(atual.percorridas, atual.custo, saltosAutomaticos(atual.percorridas, jogador));
			}
			List<Parada> vizinhas = new ArrayList<>(tabuleiro.vizinhas(atual.parada));
			vizinhas.sort(Comparator.comparing(Parada::getNome));
			for (Parada vizinha : vizinhas) {
				int custo = atual.custo + custoEntrada(vizinha, destino, jogador);
				if (custo > cavalos) {
					continue;
				}
				List<Parada> percorridas = new ArrayList<>(atual.percorridas);
				percorridas.add(vizinha);
				Candidato novo = new Candidato(vizinha, custo, percorridas);
				Candidato anterior = melhores.get(vizinha);
				if (anterior == null || ordem.compare(novo, anterior) < 0) {
					melhores.put(vizinha, novo);
					fila.add(novo);
				}
			}
		}
		return null;
	}

	/**
	 * Valida os nomes percorridos, excluindo a origem e incluindo o destino.
	 * A rota pode repetir paradas, inclusive voltar à origem, pagando o custo.
	 * Só o último ponto é submetido à regra de ocupação de destino.
	 * Esta chamada conserva a opção de pular yurts próprios intermediários.
	 */
	static Resultado validarRota(Tabuleiro tabuleiro, Peao peao, Jogador jogador,
			List<String> nomes, int cavalos) {
		if (!entradaValida(tabuleiro, peao, jogador, cavalos)
				|| nomes == null || nomes.isEmpty()) {
			return null;
		}
		List<Parada> percorridas = paradasDaRota(tabuleiro, nomes);
		if (percorridas == null) {
			return null;
		}
		return validarRota(tabuleiro, peao, jogador, nomes, saltosAutomaticos(percorridas, jogador), cavalos);
	}

	/**
	 * Cada opção corresponde à parada de chegada do mesmo passo da rota.
	 * true pula um yurt próprio intermediário, sem custo nem visita; false
	 * visita a parada e custa um cavalo, mesmo quando ela tem um yurt próprio.
	 * O destino sempre é visitado e não pode ter a opção true.
	 */
	static Resultado validarRota(Tabuleiro tabuleiro, Peao peao, Jogador jogador,
			List<String> nomes, List<Boolean> saltarYurts, int cavalos) {
		if (!entradaValida(tabuleiro, peao, jogador, cavalos)
				|| nomes == null || nomes.isEmpty() || saltarYurts == null
				|| saltarYurts.size() != nomes.size()) {
			return null;
		}
		List<Parada> percorridas = paradasDaRota(tabuleiro, nomes);
		if (percorridas == null) {
			return null;
		}
		Parada destino = percorridas.get(percorridas.size() - 1);
		if (!tabuleiro.podeTerminarMovimentoEm(peao, destino)) {
			return null;
		}
		Parada anterior = tabuleiro.posicaoDe(peao);
		int custo = 0;
		for (int i = 0; i < percorridas.size(); i++) {
			Parada atual = percorridas.get(i);
			if (!tabuleiro.saoVizinhas(anterior, atual)) {
				return null;
			}
			boolean ultima = i == percorridas.size() - 1;
			Boolean saltar = saltarYurts.get(i);
			if (saltar == null || (saltar && (ultima || !atual.temYurtDe(jogador.getCor())))) {
				return null;
			}
			custo += saltar ? 0 : 1;
			if (custo > cavalos) {
				return null;
			}
			anterior = atual;
		}
		return resultado(percorridas, custo, saltarYurts);
	}

	private static boolean entradaValida(Tabuleiro tabuleiro, Peao peao,
			Jogador jogador, int cavalos) {
		return tabuleiro != null && peao != null && jogador != null && cavalos > 0
				&& jogador.getPeoes().contains(peao) && tabuleiro.posicaoDe(peao) != null;
	}

	private static Parada parada(Tabuleiro tabuleiro, String nome) {
		try {
			return tabuleiro.getParada(nome);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static int custoEntrada(Parada parada, Parada destino, Jogador jogador) {
		return parada.equals(destino) || !parada.temYurtDe(jogador.getCor()) ? 1 : 0;
	}

	private static List<Parada> paradasDaRota(Tabuleiro tabuleiro, List<String> nomes) {
		List<Parada> percorridas = new ArrayList<>();
		for (String nome : nomes) {
			Parada p = parada(tabuleiro, nome);
			if (p == null) {
				return null;
			}
			percorridas.add(p);
		}
		return percorridas;
	}

	private static List<Boolean> saltosAutomaticos(List<Parada> percorridas, Jogador jogador) {
		List<Boolean> saltarYurts = new ArrayList<>();
		for (int i = 0; i < percorridas.size(); i++) {
			saltarYurts.add(i < percorridas.size() - 1 && percorridas.get(i).temYurtDe(jogador.getCor()));
		}
		return saltarYurts;
	}

	private static Resultado resultado(List<Parada> percorridas, int custo, List<Boolean> saltarYurts) {
		Set<Parada> visitadas = new LinkedHashSet<>();
		for (int i = 0; i < percorridas.size(); i++) {
			if (!saltarYurts.get(i)) {
				visitadas.add(percorridas.get(i));
			}
		}
		return new Resultado(percorridas.get(percorridas.size() - 1), custo, visitadas);
	}

	private static int comparar(Candidato a, Candidato b) {
		int comparacao = Integer.compare(a.custo, b.custo);
		if (comparacao != 0) {
			return comparacao;
		}
		comparacao = Integer.compare(a.percorridas.size(), b.percorridas.size());
		if (comparacao != 0) {
			return comparacao;
		}
		for (int i = 0; i < a.percorridas.size(); i++) {
			comparacao = a.percorridas.get(i).getNome().compareTo(b.percorridas.get(i).getNome());
			if (comparacao != 0) {
				return comparacao;
			}
		}
		return 0;
	}

	private static final class Candidato {
		private final Parada parada;
		private final int custo;
		private final List<Parada> percorridas;

		private Candidato(Parada parada, int custo, List<Parada> percorridas) {
			this.parada = parada;
			this.custo = custo;
			this.percorridas = percorridas;
		}
	}

	/** Resultado estável: o conjunto de visitas não muda com o tabuleiro. */
	static final class Resultado {
		private final Parada destino;
		private final int custo;
		private final Set<Parada> visitadas;

		private Resultado(Parada destino, int custo, Set<Parada> visitadas) {
			this.destino = destino;
			this.custo = custo;
			this.visitadas = Collections.unmodifiableSet(new LinkedHashSet<>(visitadas));
		}

		Parada getDestino() {
			return destino;
		}

		int getCusto() {
			return custo;
		}

		Set<Parada> getVisitadas() {
			return visitadas;
		}
	}
}
