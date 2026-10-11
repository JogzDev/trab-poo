package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

/** Consultas usadas pelo futuro Controller e escolha de visitas na rota. */
public class ConsultasPartidaTest {
	private static final int TEMPO_LIMITE = 1000;

	/**
	 * Objetivo: consultar um ataque sem executá-lo ou avaliar cartas.
	 * Entrada: cidade revelada adjacente e duas espadas; consultar duas vezes.
	 * Saída esperada: true, com tesouros, tributos, banco e callbacks preservados.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void consultaAtaqueNaoConsomeRecursosNemTesouros() {
		PartidaTest.Cenario c = PartidaTest.padrao();
		c.azul().adicionarTributo(TipoTributo.ESPADA, 2);
		Contador componentes = new Contador();
		c.iniciar(componentes);
		Cidade cidade = c.revelada();
		int banco = c.partida.getEstoque(TipoTributo.ESPADA);
		assertTrue("Primeiro ataque está disponível", c.partida.podeAtacarCidade(cidade.getNome()));
		assertTrue("Consultar novamente mantém o mesmo resultado", c.partida.podeAtacarCidade(cidade.getNome()));
		assertEquals("Nenhum tesouro foi retirado", 4, cidade.getTesouros());
		assertEquals("As espadas continuam com o jogador", 2, c.azul().contarTributo(TipoTributo.ESPADA));
		assertEquals("O banco não recebeu peças", banco, c.partida.getEstoque(TipoTributo.ESPADA));
		assertEquals("Não houve transferência de tesouro", 0, c.partida.getTesourosRecebidos(c.azul()));
		assertEquals("Consultar não avalia cartas", 0, componentes.avaliacoes);
		assertFalse("Consultar não fixa o peão", c.partida.getTurno().peaoJaAgiu());
	}

	/**
	 * Objetivo: refletir o custo crescente na consulta e na execução.
	 * Entrada: duas espadas; atacar uma vez e consultar o segundo ataque.
	 * Saída esperada: primeiro permitido, segundo recusado sem cobrar a espada restante.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void consultaAtaqueAcompanhaCustoDaExecucao() {
		PartidaTest.Cenario c = PartidaTest.padrao();
		c.azul().adicionarTributo(TipoTributo.ESPADA, 2);
		c.iniciar();
		Cidade cidade = c.revelada();
		assertTrue("Consulta permite gastar a primeira espada", c.partida.podeAtacarCidade(cidade.getNome()));
		assertTrue("Execução aceita o primeiro ataque", c.partida.atacarCidade(cidade.getNome()));
		assertFalse("O segundo ataque exige duas espadas", c.partida.podeAtacarCidade(cidade.getNome()));
		assertFalse("Execução segue a mesma restrição", c.partida.atacarCidade(cidade.getNome()));
		assertEquals("A recusa preserva a espada restante", 1, c.azul().contarTributo(TipoTributo.ESPADA));
		assertEquals("A recusa preserva os três tesouros", 3, cidade.getTesouros());
	}

	/**
	 * Objetivo: recusar a conquista sem peça de yurt na consulta.
	 * Entrada: uma espada, último tesouro e estoque de yurts vazio.
	 * Saída esperada: consulta e execução recusadas, preservando espada e cidade.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void consultaConquistaExigeYurtDisponivel() {
		PartidaTest.Cenario c = PartidaTest.padrao();
		c.azul().adicionarTributo(TipoTributo.ESPADA, 1);
		while (c.azul().temYurtDisponivel()) c.azul().retirarYurtDoEstoque();
		c.iniciar();
		Cidade cidade = c.revelada();
		PartidaTest.deixarUltimoTesouro(cidade, c.azul());
		assertFalse("Sem yurt não se pode conquistar", c.partida.podeAtacarCidade(cidade.getNome()));
		assertFalse("A execução também recusa a conquista", c.partida.atacarCidade(cidade.getNome()));
		assertEquals("A espada não foi cobrada", 1, c.azul().contarTributo(TipoTributo.ESPADA));
		assertEquals("O último tesouro continua na cidade", 1, cidade.getTesouros());
		assertFalse("A cidade continua sem conquista", cidade.isConquistada());
	}

	/**
	 * Objetivo: consultar a coleta sem gastar a mão ou retirar a peça.
	 * Entrada: coluna vermelha com duas mãos e província KA adjacente.
	 * Saída esperada: consulta true preserva estado; após coletar a última peça, false.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void consultaColetaPreservaMaosEPecas() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.VERMELHA), "ativar mãos");
		assertTrue("Província adjacente tem uma peça", c.partida.podePegarTributo("KA"));
		assertEquals("A consulta não gasta mãos", 2, c.partida.getTurno().getMaos());
		assertEquals("A consulta não retira a peça", 1, c.tabuleiro.getProvincia("KA").getPecas());
		assertEquals("A consulta não entrega moedas", 1, c.azul().contarTributo(TipoTributo.MOEDA));
		assertTrue("A execução aceita a coleta consultada", c.partida.pegarTributo("KA"));
		assertFalse("Província vazia não permite nova coleta", c.partida.podePegarTributo("KA"));
		assertEquals("Só a execução gastou uma mão", 1, c.partida.getTurno().getMaos());
	}

	/**
	 * Objetivo: consultar construção sem gastar tributo ou peça de yurt.
	 * Entrada: jogador visita B com um tributo de yurt disponível.
	 * Saída esperada: consulta true sem mudança; construção preenche a parada e nova consulta false.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void consultaConstrucaoPreservaTributoEEstoque() {
		PartidaTest.Cenario c = PartidaTest.padrao();
		c.azul().adicionarTributo(TipoTributo.YURT, 1);
		c.iniciar();
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.VERMELHA), "ativar movimento");
		PartidaTest.exigir(c.partida.moverPara("B"), "visitar parada B");
		assertTrue("Parada visitada aceita o yurt", c.partida.podeConstruirYurt("B"));
		assertEquals("Consultar preserva o tributo", 1, c.azul().contarTributo(TipoTributo.YURT));
		assertEquals("Consultar preserva o estoque do jogador", 12, c.azul().getYurtsDisponiveis());
		assertEquals("Consultar não coloca yurt no mapa", 0, c.tabuleiro.getParada("B").contarYurts());
		assertTrue("Construção segue a consulta", c.partida.construirYurt("B"));
		assertFalse("Parada cheia recusa outra construção", c.partida.podeConstruirYurt("B"));
		assertEquals("Somente uma peça foi colocada", 1, c.tabuleiro.getParada("B").contarYurts());
	}

	/**
	 * Objetivo: preservar a habilidade de construção adjacente de Tolui.
	 * Entrada: yurt inicial em Karakorum, tributo disponível e peão oponente em B.
	 * Saída esperada: consulta true sem colocar yurt nem gastar recursos.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void consultaToluiPermiteAdjacenteComPeaoOponente() {
		PartidaTest.Cenario c = new PartidaTest.Cenario(Herdeiro.TOLUI, Herdeiro.OGEDEI);
		c.azul().adicionarTributo(TipoTributo.YURT, 1);
		c.iniciar();
		c.tabuleiro.posicionarPeao(c.jogadores.get(1).getPeao(0), c.tabuleiro.getParada("B"));
		assertTrue("Tolui pode construir na adjacente ocupada por peão", c.partida.podeConstruirYurt("B"));
		assertEquals("Consulta não gasta o tributo", 1, c.azul().contarTributo(TipoTributo.YURT));
		assertEquals("Somente o yurt inicial saiu do estoque", 11, c.azul().getYurtsDisponiveis());
		assertEquals("A parada B continua sem yurt", 0, c.tabuleiro.getParada("B").contarYurts());
	}

	/**
	 * Objetivo: recusar consultas fora da partida e para nomes inexistentes.
	 * Entrada: preparação ainda não iniciada; depois iniciar e consultar nomes desconhecidos.
	 * Saída esperada: todas false, sem gastar recursos ou ativar coluna.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void consultasRecusamPreparacaoENomesInexistentes() {
		PartidaTest.Cenario c = PartidaTest.padrao();
		c.criar(new ComponentesPartida() { });
		assertFalse("Durante preparação não se ataca", c.partida.podeAtacarCidade("Cidade1"));
		assertFalse("Durante preparação não se coleta", c.partida.podePegarTributo("KA"));
		assertFalse("Durante preparação não se constrói", c.partida.podeConstruirYurt("B"));
		PartidaTest.exigir(c.partida.iniciar(), "iniciar preparação base");
		assertFalse("Cidade desconhecida não permite ataque", c.partida.podeAtacarCidade("inexistente"));
		assertFalse("Província desconhecida não permite coleta", c.partida.podePegarTributo("inexistente"));
		assertFalse("Parada desconhecida não permite construção", c.partida.podeConstruirYurt("inexistente"));
		assertEquals("Nenhuma consulta iniciou a coluna", null, c.partida.getTurno().getColuna());
	}

	/**
	 * Objetivo: consultar ações sem fixar o peão de Altani.
	 * Entrada: primeiro peão ativo, carta e tributo alcançáveis; consultas repetidas.
	 * Saída esperada: consultas true e seleção do segundo peão ainda permitida.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void consultasNaoFixamPeaoDeAltani() {
		PartidaTest.Cenario c = new PartidaTest.Cenario(Herdeiro.ALTANI, Herdeiro.OGEDEI);
		c.azul().adicionarTributo(TipoTributo.ESPADA, 1);
		c.iniciar();
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.VERMELHA), "ativar mãos de Altani");
		assertTrue("Altani pode atacar sem executar", c.partida.podeAtacarCidade(c.revelada().getNome()));
		assertTrue("Altani pode coletar sem executar", c.partida.podePegarTributo("KA"));
		assertTrue("As consultas não impediram escolher o segundo peão", c.partida.escolherPeao(1));
		assertEquals("O segundo peão ficou ativo", c.azul().getPeao(1), c.partida.getTurno().getPeaoAtivo());
	}

	/**
	 * Objetivo: visitar intencionalmente um yurt próprio e habilitar a coleta.
	 * Entrada: rota K-B-C, yurt próprio em B e flags false/false.
	 * Saída esperada: dois cavalos gastos, B visitada e província X disponível.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void visitaOpcionalAoYurtHabilitaColeta() {
		PartidaTest.Cenario c = comYurtEmB();
		assertTrue("A rota aceita visitar o próprio yurt", c.partida.moverPor(Arrays.asList("B", "C"), Arrays.asList(false, false)));
		assertEquals("A visita usou os dois cavalos", 0, c.partida.getTurno().getCavalos());
		assertTrue("B conta como visitada", c.partida.getTurno().getParadasVisitadas().contains(c.tabuleiro.getParada("B")));
		assertTrue("A visita permite coletar X", c.partida.podePegarTributo("X"));
		assertTrue("A coleta é executável", c.partida.pegarTributo("X"));
	}

	/**
	 * Objetivo: manter a alternativa de saltar o yurt sem habilitar coleta.
	 * Entrada: mesma rota K-B-C, mas flags true/false.
	 * Saída esperada: apenas um cavalo gasto e X indisponível porque B foi pulada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void saltoOpcionalAoYurtNaoHabilitaColeta() {
		PartidaTest.Cenario c = comYurtEmB();
		assertTrue("A rota aceita pular o próprio yurt", c.partida.moverPor(Arrays.asList("B", "C"), Arrays.asList(true, false)));
		assertEquals("O salto preservou um cavalo", 1, c.partida.getTurno().getCavalos());
		assertFalse("B não conta como visitada", c.partida.getTurno().getParadasVisitadas().contains(c.tabuleiro.getParada("B")));
		assertFalse("X não fica alcançável pelo salto", c.partida.podePegarTributo("X"));
	}

	/**
	 * Objetivo: não aplicar uma rota com escolhas de salto incompletas.
	 * Entrada: dois passos e apenas uma flag de salto.
	 * Saída esperada: false, posição inicial, cavalos e escolha de peão preservados.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void rotaComFlagsInvalidasPreservaPartida() {
		PartidaTest.Cenario c = comYurtEmB();
		assertFalse("Quantidade de flags precisa corresponder aos passos", c.partida.moverPor(Arrays.asList("B", "C"), Arrays.asList(true)));
		assertEquals("A posição continua em Karakorum", c.tabuleiro.getKarakorum(), c.tabuleiro.posicaoDe(c.azul().getPeao(0)));
		assertEquals("Nenhum cavalo foi gasto", 2, c.partida.getTurno().getCavalos());
		assertFalse("A recusa não fixa o peão", c.partida.getTurno().peaoJaAgiu());
	}

	private static PartidaTest.Cenario comYurtEmB() {
		PartidaTest.Cenario c = PartidaTest.padrao();
		PartidaTest.exigir(c.tabuleiro.colocarYurt(c.tabuleiro.getParada("B"), c.azul()), "preparar yurt próprio");
		c.iniciar();
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.AMARELA), "ativar dois cavalos");
		return c;
	}

	private static final class Contador implements ComponentesPartida {
		private int avaliacoes;
		@Override public void aposAcao(Partida partida) { avaliacoes++; }
	}
}
