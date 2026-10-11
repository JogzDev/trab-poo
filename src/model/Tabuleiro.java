package model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * O tabuleiro principal de Herdeiros de Khan: o mapa do Império Mongol.
 *
 * Estrutura de dados (toda baseada em HashMap/HashSet):
 *
 * - paradas: nome → Parada (os nós das rotas);
 * - rotas: Parada → conjunto de paradas vizinhas (as
 * arestas; sempre simétricas);
 * - paradasPorRegiao: Regiao → paradas que ela contém;
 * - cidades e provincias: nome → objeto; cada um sabe a
 * quais paradas é adjacente;
 * - posicaoDosPeoes: Peao → parada onde está.
 *
 * O tabuleiro só conhece a geografia e o estado das peças sobre ela. Quem
 * decide QUANDO um jogador pode mover, construir ou atacar é o Controller (que
 * usa as consultas daqui: saoVizinhas, podeTerminarMovimentoEm,
 * contarYurts(Jogador, Regiao) etc.).
 *
 * Correspondência com o esqueleto originalmente combinado pelo grupo (que
 * usava termos de War): Territorio → Parada, fronteiras → rotas,
 * Continente → Regiao, exércitos → yurts, donoDe → temYurtDe,
 * dominaContinente → temMaioriaDeYurts.
 */
class Tabuleiro {

	private final Map<String, Parada> paradas = new HashMap<>();
	private final Map<Parada, Set<Parada>> rotas = new HashMap<>();
	private final Map<Regiao, Set<Parada>> paradasPorRegiao = new HashMap<>();
	private final Map<String, Cidade> cidades = new HashMap<>();
	private final Map<String, Provincia> provincias = new HashMap<>();
	private final Map<Peao, Parada> posicaoDosPeoes = new HashMap<>();
	private Parada karakorum;

	Tabuleiro() {
		for (Regiao r : Regiao.values()) {
			paradasPorRegiao.put(r, new HashSet<>());
		}
	}

	// ========================================================== construção

	/**
	 * Cria e registra uma parada.
	 *
	 * @throws IllegalArgumentException se já existe parada com esse nome ou se
	 * já existe um Karakorum
	 */
	Parada adicionarParada(String nome, Regiao regiao, TipoParada tipo) {
		if (paradas.containsKey(nome)) {
			throw new IllegalArgumentException("Parada duplicada: " + nome);
		}
		Parada p = new Parada(nome, regiao, tipo);
		if (p.isKarakorum()) {
			if (karakorum != null) {
				throw new IllegalArgumentException("Só pode haver um Karakorum");
			}
			karakorum = p;
		}
		paradas.put(nome, p);
		rotas.put(p, new HashSet<>());
		if (regiao != null) {
			paradasPorRegiao.get(regiao).add(p);
		}
		return p;
	}

	/** Liga duas paradas por uma rota (nos dois sentidos). */
	void ligar(Parada a, Parada b) {
		exigirRegistrada(a);
		exigirRegistrada(b);
		if (a.equals(b)) {
			throw new IllegalArgumentException("Parada não pode ser vizinha de si mesma: " + a);
		}
		rotas.get(a).add(b);
		rotas.get(b).add(a);
	}

	/** Liga duas paradas pelos nomes. */
	void ligar(String a, String b) {
		ligar(getParada(a), getParada(b));
	}

	/** Cria uma cidade e informa quais paradas ficam ao redor dela. */
	Cidade adicionarCidade(String nome, Regiao regiao, String... paradasAdjacentes) {
		if (cidades.containsKey(nome)) {
			throw new IllegalArgumentException("Cidade duplicada: " + nome);
		}
		Cidade c = new Cidade(nome, regiao);
		for (String p : paradasAdjacentes) {
			c.adicionarParadaAdjacente(getParada(p));
		}
		cidades.put(nome, c);
		return c;
	}

	/** Cria uma província e informa quais paradas fazem fronteira com ela. */
	Provincia adicionarProvincia(String nome, Regiao regiao, TipoTributo tributo, boolean doKhan,
			String... paradasAdjacentes) {
		if (provincias.containsKey(nome)) {
			throw new IllegalArgumentException("Província duplicada: " + nome);
		}
		Provincia pr = new Provincia(nome, regiao, tributo, doKhan);
		for (String p : paradasAdjacentes) {
			pr.adicionarParadaAdjacente(getParada(p));
		}
		provincias.put(nome, pr);
		return pr;
	}

	private void exigirRegistrada(Parada p) {
		if (p == null || paradas.get(p.getNome()) != p) {
			throw new IllegalArgumentException("Parada não pertence a este tabuleiro: " + p);
		}
	}

	// ============================================================ consultas

	/**
	 * @throws IllegalArgumentException se não existe parada com esse nome
	 */
	Parada getParada(String nome) {
		Parada p = paradas.get(nome);
		if (p == null) {
			throw new IllegalArgumentException("Parada inexistente: " + nome);
		}
		return p;
	}

	boolean existeParada(String nome) {
		return paradas.containsKey(nome);
	}

	Collection<Parada> getParadas() {
		return Collections.unmodifiableCollection(paradas.values());
	}

	/** Karakorum, ou null se o mapa não tem uma. */
	Parada getKarakorum() {
		return karakorum;
	}

	/** Paradas vizinhas (ligadas por uma rota) à parada informada. */
	Set<Parada> vizinhas(Parada p) {
		exigirRegistrada(p);
		return Collections.unmodifiableSet(rotas.get(p));
	}

	boolean saoVizinhas(Parada a, Parada b) {
		exigirRegistrada(a);
		return rotas.get(a).contains(b);
	}

	boolean saoVizinhas(String a, String b) {
		return saoVizinhas(getParada(a), getParada(b));
	}

	Set<Parada> paradasDaRegiao(Regiao r) {
		return Collections.unmodifiableSet(paradasPorRegiao.get(r));
	}

	/** Paradas de conselheiro: as 6 paradas duplas + Karakorum. */
	Set<Parada> paradasDeConselheiro() {
		Set<Parada> s = new HashSet<>();
		for (Parada p : paradas.values()) {
			if (p.isConselheiro()) {
				s.add(p);
			}
		}
		return s;
	}

	Cidade getCidade(String nome) {
		Cidade c = cidades.get(nome);
		if (c == null) {
			throw new IllegalArgumentException("Cidade inexistente: " + nome);
		}
		return c;
	}

	Collection<Cidade> getCidades() {
		return Collections.unmodifiableCollection(cidades.values());
	}

	/** Cidades que podem ser atacadas a partir de uma parada. */
	Set<Cidade> cidadesAdjacentes(Parada p) {
		Set<Cidade> s = new HashSet<>();
		for (Cidade c : cidades.values()) {
			if (c.isAdjacente(p)) {
				s.add(c);
			}
		}
		return s;
	}

	Provincia getProvincia(String nome) {
		Provincia pr = provincias.get(nome);
		if (pr == null) {
			throw new IllegalArgumentException("Província inexistente: " + nome);
		}
		return pr;
	}

	Collection<Provincia> getProvincias() {
		return Collections.unmodifiableCollection(provincias.values());
	}

	/** Províncias de onde se pode pegar tributo estando numa parada. */
	Set<Provincia> provinciasAdjacentes(Parada p) {
		Set<Provincia> s = new HashSet<>();
		for (Provincia pr : provincias.values()) {
			if (pr.isAdjacente(p)) {
				s.add(pr);
			}
		}
		return s;
	}

	/** As províncias marcadas com o ícone do Khan. */
	Set<Provincia> provinciasDoKhan() {
		Set<Provincia> s = new HashSet<>();
		for (Provincia pr : provincias.values()) {
			if (pr.isProvinciaDoKhan()) {
				s.add(pr);
			}
		}
		return s;
	}

	// =============================================================== yurts

	/**
	 * Constrói um yurt do jogador na parada: retira do estoque do jogador e
	 * coloca na parada, respeitando a capacidade dela.
	 *
	 * @return false (sem alterar nada) se a parada está cheia ou o
	 * jogador não tem yurts
	 */
	boolean colocarYurt(Parada p, Jogador j) {
		exigirRegistrada(p);
		if (!p.podeReceberYurt() || !j.temYurtDisponivel()) {
			return false;
		}
		j.retirarYurtDoEstoque();
		p.colocarYurt(j.getCor());
		return true;
	}

	/** Remove um yurt do jogador da parada e devolve ao estoque dele. */
	boolean removerYurt(Parada p, Jogador j) {
		exigirRegistrada(p);
		if (!p.removerYurt(j.getCor())) {
			return false;
		}
		j.devolverYurtAoEstoque();
		return true;
	}

	int yurtsEm(Parada p) {
		exigirRegistrada(p);
		return p.contarYurts();
	}

	/** Se o jogador tem pelo menos um yurt nessa parada (o "dono" em War). */
	boolean temYurtDe(Parada p, Jogador j) {
		exigirRegistrada(p);
		return p.temYurtDe(j.getCor());
	}

	/** Paradas onde o jogador construiu yurts (não inclui cidades). */
	Set<Parada> paradasComYurtDe(Jogador j) {
		Set<Parada> s = new HashSet<>();
		for (Parada p : paradas.values()) {
			if (p.temYurtDe(j.getCor())) {
				s.add(p);
			}
		}
		return s;
	}

	/**
	 * Quantos yurts o jogador tem na região, contando os yurts das paradas e os
	 * yurts de conquista das cidades (manual, p. 10). Yurts em Karakorum não
	 * contam para região nenhuma.
	 */
	int contarYurts(Jogador j, Regiao r) {
		return contarYurts(j.getCor(), r);
	}

	int contarYurts(Cor cor, Regiao r) {
		int n = 0;
		for (Parada p : paradasPorRegiao.get(r)) {
			n += p.contarYurts(cor);
		}
		for (Cidade c : cidades.values()) {
			if (c.getRegiao() == r && c.getConquistadaPor() == cor) {
				n++;
			}
		}
		return n;
	}

	/** Cor → número de yurts na região, só para cores com pelo menos um yurt. */
	Map<Cor, Integer> yurtsPorCor(Regiao r) {
		Map<Cor, Integer> m = new HashMap<>();
		for (Cor c : Cor.values()) {
			int n = contarYurts(c, r);
			if (n > 0) {
				m.put(c, n);
			}
		}
		return m;
	}

	/**
	 * Cores com o maior número de yurts na região. Se houver empate, todos os
	 * empatados ficam em 1º lugar (manual, p. 15). Vazio se ninguém tem yurt.
	 */
	Set<Cor> lideresEmYurts(Regiao r) {
		Map<Cor, Integer> m = yurtsPorCor(r);
		int max = 0;
		for (int n : m.values()) {
			max = Math.max(max, n);
		}
		Set<Cor> s = new HashSet<>();
		if (max == 0) {
			return s;
		}
		for (Map.Entry<Cor, Integer> e : m.entrySet()) {
			if (e.getValue() == max) {
				s.add(e.getKey());
			}
		}
		return s;
	}

	/**
	 * Cores em 2º lugar em yurts na região. Se houver empate no 1º lugar,
	 * ninguém fica em 2º; se houver empate no 2º, todos os empatados contam
	 * como 2º (manual, p. 15).
	 */
	Set<Cor> segundosEmYurts(Regiao r) {
		Set<Cor> lideres = lideresEmYurts(r);
		Set<Cor> s = new HashSet<>();
		if (lideres.size() != 1) {
			return s;
		}
		Map<Cor, Integer> m = yurtsPorCor(r);
		int segundo = 0;
		for (Map.Entry<Cor, Integer> e : m.entrySet()) {
			if (!lideres.contains(e.getKey())) {
				segundo = Math.max(segundo, e.getValue());
			}
		}
		if (segundo == 0) {
			return s;
		}
		for (Map.Entry<Cor, Integer> e : m.entrySet()) {
			if (e.getValue() == segundo) {
				s.add(e.getKey());
			}
		}
		return s;
	}

	/** Se o jogador está entre os que têm mais yurts na região. */
	boolean temMaioriaDeYurts(Jogador j, Regiao r) {
		return lideresEmYurts(r).contains(j.getCor());
	}

	// =============================================================== peões

	/** Coloca (ou recoloca) um peão numa parada sem verificar rotas: uso na preparação. */
	void posicionarPeao(Peao peao, Parada p) {
		exigirRegistrada(p);
		posicaoDosPeoes.put(peao, p);
	}

	/** Parada onde o peão está; null se ainda não foi posicionado. */
	Parada posicaoDe(Peao peao) {
		return posicaoDosPeoes.get(peao);
	}

	/** Peões que estão na parada. */
	Set<Peao> peoesEm(Parada p) {
		exigirRegistrada(p);
		Set<Peao> s = new HashSet<>();
		for (Map.Entry<Peao, Parada> e : posicaoDosPeoes.entrySet()) {
			if (e.getValue().equals(p)) {
				s.add(e.getKey());
			}
		}
		return s;
	}

	/**
	 * Se o peão pode TERMINAR um movimento na parada (manual, p. 7): pode
	 * passar por parada ocupada, mas só termina nela se couber. Parada simples
	 * não aceita peão de outro jogador; dupla aceita 2 peões; Karakorum aceita
	 * todos. Peões do próprio jogador (Altani) nunca impedem.
	 */
	boolean podeTerminarMovimentoEm(Peao peao, Parada destino) {
		exigirRegistrada(destino);
		int ocupantesDeOutros = 0;
		for (Peao outro : peoesEm(destino)) {
			if (outro != peao && outro.getCor() != peao.getCor()) {
				ocupantesDeOutros++;
			}
		}
		return ocupantesDeOutros < destino.getCapacidadePeoes();
	}

	/**
	 * Move o peão para uma parada vizinha (um passo = um cavalo).
	 *
	 * @return false se o destino não é vizinho ou não aceita o peão
	 */
	boolean moverPeao(Peao peao, Parada destino) {
		Parada origem = posicaoDe(peao);
		if (origem == null || !saoVizinhas(origem, destino) || !podeTerminarMovimentoEm(peao, destino)) {
			return false;
		}
		posicaoDosPeoes.put(peao, destino);
		return true;
	}

	@Override
	public String toString() {
		return "Tabuleiro[" + paradas.size() + " paradas, " + cidades.size() + " cidades, "
				+ provincias.size() + " províncias]";
	}
}
