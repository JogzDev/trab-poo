package model;

/**
 * Monta o Tabuleiro com o mapa do jogo físico.
 *
 * ATENÇÃO: dados a validar com o tabuleiro físico. Este mapa foi
 * transcrito do manual escaneado (Regas.pdf, pp. 4 e 5), onde a rede de rotas
 * aparece pequena e borrada. O que está confirmado pelo manual:
 *
 * - 3 regiões (Rússia, China, Pérsia) e Karakorum no centro, fora delas;
 * - 9 cidades: Kiev, Moscou, Sarai (Rússia); Samarcanda, Bagdá, Cabul
 * (Pérsia); Pequim, Kaesong, Cantão (China);
 * - 7 paradas de conselheiro: 6 paradas duplas na borda do mapa (com as
 * peças de bônus trono, espadas, cavalos, yurts, moedas e baú) e
 * Karakorum;
 * - 4 províncias do Khan, cada uma com setas para 2 vizinhas;
 * - cada cidade fica dentro de um anel de rotas com paradas ao redor.
 *
 * O que é aproximação e deve ser conferido olhando o tabuleiro de verdade:
 * o número exato de paradas em cada anel, quais rotas ligam os anéis, e a
 * localização/tipo de tributo de cada província. Para corrigir, basta editar as
 * listas abaixo: nenhuma outra classe depende desses nomes.
 *
 * Convenção de nomes das paradas sem nome no jogo: "<Cidade> Norte/
 * Sul/Leste/Oeste" para as paradas do anel da cidade; "Conselheiro <posição>"
 * para as paradas duplas.
 */
final class MapaOficial {

	// Nomes públicos ao pacote, para evitar strings repetidas nos testes/controller.
	static final String KARAKORUM = "Karakorum";

	static final String CONSELHEIRO_NORTE = "Conselheiro Norte";       // bônus: trono (1 voto)
	static final String CONSELHEIRO_NORDESTE = "Conselheiro Nordeste"; // bônus: 2 espadas
	static final String CONSELHEIRO_OESTE = "Conselheiro Oeste";       // bônus: 3 cavalos + 1 mão
	static final String CONSELHEIRO_LESTE = "Conselheiro Leste";       // bônus: 2 yurts
	static final String CONSELHEIRO_SUDOESTE = "Conselheiro Sudoeste"; // bônus: 2 moedas
	static final String CONSELHEIRO_SUL = "Conselheiro Sul";           // bônus: baú (3 tesouros)

	private MapaOficial() {
	}

	/** Cria um tabuleiro novo, vazio de peças, com a geografia do jogo. */
	static Tabuleiro criar() {
		Tabuleiro t = new Tabuleiro();
		criarParadas(t);
		criarRotas(t);
		criarCidades(t);
		criarProvincias(t);
		return t;
	}

	private static void criarParadas(Tabuleiro t) {
		t.adicionarParada(KARAKORUM, null, TipoParada.KARAKORUM);

		// ---- Rússia (norte)
		anel(t, Regiao.RUSSIA, "Kiev", "Norte", "Oeste", "Sul");
		anel(t, Regiao.RUSSIA, "Moscou", "Norte", "Oeste", "Sul", "Leste");
		anel(t, Regiao.RUSSIA, "Sarai", "Norte", "Oeste", "Sul", "Leste");
		t.adicionarParada(CONSELHEIRO_NORTE, Regiao.RUSSIA, TipoParada.DUPLA);
		t.adicionarParada(CONSELHEIRO_NORDESTE, Regiao.RUSSIA, TipoParada.DUPLA);
		t.adicionarParada(CONSELHEIRO_OESTE, Regiao.RUSSIA, TipoParada.DUPLA);

		// ---- Pérsia (sudoeste)
		anel(t, Regiao.PERSIA, "Samarcanda", "Norte", "Oeste", "Sul", "Leste");
		anel(t, Regiao.PERSIA, "Bagdá", "Norte", "Oeste", "Sul");
		anel(t, Regiao.PERSIA, "Cabul", "Norte", "Oeste", "Sul", "Leste");
		t.adicionarParada(CONSELHEIRO_SUDOESTE, Regiao.PERSIA, TipoParada.DUPLA);

		// ---- China (leste)
		anel(t, Regiao.CHINA, "Pequim", "Norte", "Oeste", "Sul", "Leste");
		anel(t, Regiao.CHINA, "Kaesong", "Norte", "Oeste", "Sul");
		anel(t, Regiao.CHINA, "Cantão", "Norte", "Oeste", "Sul");
		t.adicionarParada(CONSELHEIRO_LESTE, Regiao.CHINA, TipoParada.DUPLA);
		t.adicionarParada(CONSELHEIRO_SUL, Regiao.CHINA, TipoParada.DUPLA);
	}

	/** Cria as paradas "<cidade> <lado>" e as liga em ciclo (o anel da cidade). */
	private static void anel(Tabuleiro t, Regiao r, String cidade, String... lados) {
		for (String lado : lados) {
			t.adicionarParada(cidade + " " + lado, r, TipoParada.SIMPLES);
		}
		for (int i = 0; i < lados.length; i++) {
			String a = cidade + " " + lados[i];
			String b = cidade + " " + lados[(i + 1) % lados.length];
			t.ligar(a, b);
		}
	}

	private static void criarRotas(Tabuleiro t) {
		// Rússia
		t.ligar("Kiev Norte", "Moscou Norte");
		t.ligar("Kiev Sul", CONSELHEIRO_OESTE);
		t.ligar("Kiev Sul", "Moscou Oeste");
		t.ligar("Moscou Norte", CONSELHEIRO_NORTE);
		t.ligar(CONSELHEIRO_NORTE, "Sarai Norte");
		t.ligar("Moscou Leste", "Sarai Oeste");
		t.ligar("Sarai Leste", CONSELHEIRO_NORDESTE);
		// Rússia ↔ China
		t.ligar(CONSELHEIRO_NORDESTE, "Kaesong Norte");
		t.ligar("Sarai Sul", "Kaesong Oeste");
		// Rússia ↔ Pérsia
		t.ligar(CONSELHEIRO_OESTE, "Samarcanda Oeste");
		t.ligar("Moscou Sul", "Samarcanda Norte");
		// Pérsia
		t.ligar("Samarcanda Oeste", "Bagdá Norte");
		t.ligar("Samarcanda Sul", "Bagdá Norte");
		t.ligar("Samarcanda Sul", "Cabul Oeste");
		t.ligar("Bagdá Sul", CONSELHEIRO_SUDOESTE);
		t.ligar(CONSELHEIRO_SUDOESTE, "Cabul Oeste");
		// Pérsia ↔ China
		t.ligar("Cabul Leste", CONSELHEIRO_SUL);
		t.ligar(CONSELHEIRO_SUL, "Cantão Oeste");
		t.ligar("Cabul Norte", "Pequim Oeste");
		// China
		t.ligar("Kaesong Oeste", "Pequim Norte");
		t.ligar("Kaesong Sul", CONSELHEIRO_LESTE);
		t.ligar(CONSELHEIRO_LESTE, "Pequim Leste");
		t.ligar("Pequim Sul", "Cantão Norte");
		t.ligar("Cantão Norte", "Kaesong Sul");
		// Karakorum (centro): rotas para as quatro direções
		t.ligar(KARAKORUM, "Moscou Sul");
		t.ligar(KARAKORUM, "Sarai Sul");
		t.ligar(KARAKORUM, "Samarcanda Leste");
		t.ligar(KARAKORUM, "Cabul Norte");
		t.ligar(KARAKORUM, "Pequim Oeste");
	}

	private static void criarCidades(Tabuleiro t) {
		cidade(t, Regiao.RUSSIA, "Kiev", "Norte", "Oeste", "Sul");
		cidade(t, Regiao.RUSSIA, "Moscou", "Norte", "Oeste", "Sul", "Leste");
		cidade(t, Regiao.RUSSIA, "Sarai", "Norte", "Oeste", "Sul", "Leste");
		cidade(t, Regiao.PERSIA, "Samarcanda", "Norte", "Oeste", "Sul", "Leste");
		cidade(t, Regiao.PERSIA, "Bagdá", "Norte", "Oeste", "Sul");
		cidade(t, Regiao.PERSIA, "Cabul", "Norte", "Oeste", "Sul", "Leste");
		cidade(t, Regiao.CHINA, "Pequim", "Norte", "Oeste", "Sul", "Leste");
		cidade(t, Regiao.CHINA, "Kaesong", "Norte", "Oeste", "Sul");
		cidade(t, Regiao.CHINA, "Cantão", "Norte", "Oeste", "Sul");
	}

	private static void cidade(Tabuleiro t, Regiao r, String nome, String... lados) {
		String[] paradas = new String[lados.length];
		for (int i = 0; i < lados.length; i++) {
			paradas[i] = nome + " " + lados[i];
		}
		t.adicionarCidade(nome, r, paradas);
	}

	private static void criarProvincias(Tabuleiro t) {
		// Rússia
		t.adicionarProvincia("Estepe de Kiev", Regiao.RUSSIA, TipoTributo.YURT, false,
				"Kiev Norte", "Moscou Norte", "Moscou Oeste");
		Provincia khanNO = t.adicionarProvincia("Khan do Noroeste", Regiao.RUSSIA, TipoTributo.MOEDA, true,
				"Kiev Sul", CONSELHEIRO_OESTE, "Moscou Oeste");
		t.adicionarProvincia("Planície de Moscou", Regiao.RUSSIA, TipoTributo.MOEDA, false,
				"Moscou Sul", "Samarcanda Norte", "Moscou Oeste");
		t.adicionarProvincia("Estepe Central", Regiao.RUSSIA, TipoTributo.YURT, false,
				"Moscou Sul", "Moscou Leste", KARAKORUM);
		Provincia khanNE = t.adicionarProvincia("Khan do Nordeste", Regiao.RUSSIA, TipoTributo.YURT, true,
				"Sarai Sul", "Sarai Oeste", KARAKORUM);
		t.adicionarProvincia("Costa de Sarai", Regiao.RUSSIA, TipoTributo.MOEDA, false,
				"Sarai Leste", "Sarai Sul", CONSELHEIRO_NORDESTE);
		// China
		t.adicionarProvincia("Vale de Kaesong", Regiao.CHINA, TipoTributo.ESPADA, false,
				"Kaesong Oeste", "Sarai Sul", KARAKORUM);
		t.adicionarProvincia("Colinas de Pequim", Regiao.CHINA, TipoTributo.MOEDA, false,
				"Pequim Leste", "Kaesong Sul", CONSELHEIRO_LESTE);
		t.adicionarProvincia("Campos de Pequim", Regiao.CHINA, TipoTributo.YURT, false,
				"Pequim Sul", "Pequim Leste", "Cantão Norte");
		t.adicionarProvincia("Planície Chinesa", Regiao.CHINA, TipoTributo.YURT, false,
				"Pequim Oeste", "Pequim Sul", "Cabul Leste");
		Provincia khanSE = t.adicionarProvincia("Khan do Sudeste", Regiao.CHINA, TipoTributo.ESPADA, true,
				"Cantão Oeste", "Cantão Norte", CONSELHEIRO_SUL);
		// Pérsia
		t.adicionarProvincia("Deserto de Samarcanda", Regiao.PERSIA, TipoTributo.ESPADA, false,
				"Samarcanda Oeste", "Samarcanda Norte", CONSELHEIRO_OESTE);
		t.adicionarProvincia("Oásis Central", Regiao.PERSIA, TipoTributo.YURT, false,
				"Samarcanda Leste", "Cabul Norte", KARAKORUM);
		t.adicionarProvincia("Vale de Bagdá", Regiao.PERSIA, TipoTributo.MOEDA, false,
				"Bagdá Norte", "Samarcanda Sul", "Bagdá Oeste");
		Provincia khanSO = t.adicionarProvincia("Khan do Sudoeste", Regiao.PERSIA, TipoTributo.YURT, true,
				"Bagdá Sul", CONSELHEIRO_SUDOESTE, "Cabul Oeste");
		t.adicionarProvincia("Montanhas de Cabul", Regiao.PERSIA, TipoTributo.ESPADA, false,
				"Cabul Sul", "Cabul Leste", CONSELHEIRO_SUL);

		// Setas das províncias do Khan: cada uma aponta para 2 vizinhas.
		khanNO.adicionarVizinhaDoKhan(t.getProvincia("Estepe de Kiev"));
		khanNO.adicionarVizinhaDoKhan(t.getProvincia("Deserto de Samarcanda"));
		khanNE.adicionarVizinhaDoKhan(t.getProvincia("Costa de Sarai"));
		khanNE.adicionarVizinhaDoKhan(t.getProvincia("Vale de Kaesong"));
		khanSE.adicionarVizinhaDoKhan(t.getProvincia("Campos de Pequim"));
		khanSE.adicionarVizinhaDoKhan(t.getProvincia("Montanhas de Cabul"));
		khanSO.adicionarVizinhaDoKhan(t.getProvincia("Vale de Bagdá"));
		khanSO.adicionarVizinhaDoKhan(t.getProvincia("Oásis Central"));

		// Preparação (manual, p. 4, item 7): 1 peça de tributo em cada província.
		for (Provincia p : t.getProvincias()) {
			p.colocarPeca();
		}
	}
}
