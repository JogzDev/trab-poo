package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/** Integra o cálculo de rotas com cavalos, visitas e ações da Partida. */
public class MovimentoPartidaTest {
	private static final int TEMPO_LIMITE = 1000;

	/**
	 * Objetivo: não mover antes de receber ações de uma coluna.
	 * Entrada: partida iniciada, sem ativação; tentar chegar a B.
	 * Saída esperada: consulta e movimento recusados; peão continua em K.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void semColunaAtivadaNaoHaMovimento() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		boolean podeMover = c.partida.podeMoverPara("B");
		boolean resultadoDeMoverPara = c.partida.moverPara("B");
		assertFalse("Sem ações de coluna não deve haver destino alcançável", podeMover);
		assertFalse("Sem coluna ativada, o peão não deve sair de Karakorum", resultadoDeMoverPara);
		assertEquals("Posição do peão de Azul", "K", c.tabuleiro.posicaoDe(c.azul().getPeao(0)).getNome());
		assertEquals("Cavalos restantes no turno", 0, c.partida.getTurno().getCavalos());
	}

	/**
	 * Objetivo: recusar caminhos que custam mais cavalos que os disponíveis.
	 * Entrada: dois cavalos da amarela; D exige três passos de K.
	 * Saída esperada: movimento recusado e os dois cavalos preservados.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void limiteDeCavalosRecusaMovimentoSemGastarParcialmente() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.AMARELA), "dois cavalos");
		boolean resultadoDeMoverPara = c.partida.moverPara("D");
		assertFalse("D exige três cavalos e a coluna amarela oferece apenas dois", resultadoDeMoverPara);
		assertEquals("Posição do peão de Azul", "K", c.tabuleiro.posicaoDe(c.azul().getPeao(0)).getNome());
		assertEquals("Cavalos restantes no turno", 2, c.partida.getTurno().getCavalos());
	}

	/**
	 * Objetivo: não terminar numa parada simples de outro jogador.
	 * Entrada: Vermelho ocupa B; Azul tem dois cavalos.
	 * Saída esperada: movimento recusado sem mover o peão ou gastar cavalos.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void destinoSimplesOcupadoERecusado() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		c.tabuleiro.posicionarPeao(c.jogadores.get(1).getPeao(0), c.tabuleiro.getParada("B"));
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.AMARELA), "coluna de movimento");
		boolean resultadoDeMoverPara = c.partida.moverPara("B");
		assertFalse("B ocupada por Vermelho não deve aceitar o peão de Azul como destino", resultadoDeMoverPara);
		assertEquals("Posição do peão de Azul", "K", c.tabuleiro.posicaoDe(c.azul().getPeao(0)).getNome());
		assertEquals("Cavalos restantes no turno", 2, c.partida.getTurno().getCavalos());
	}

	/**
	 * Objetivo: atravessar uma parada ocupada e registrar sua visita.
	 * Entrada: Vermelho em B; Azul percorre B-C e coleta a espada de X em B.
	 * Saída esperada: ambos os movimentos/ação permitidos; destino C e zero cavalos.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void atravessaParadaOcupadaEPermiteColetarTributoAdjacente() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		c.tabuleiro.posicionarPeao(c.jogadores.get(1).getPeao(0), c.tabuleiro.getParada("B"));
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.AMARELA), "dois cavalos");
		PartidaTest.exigir(c.partida.moverPor(Arrays.asList("B", "C")), "atravessar B ocupada");
		boolean resultadoDePegarTributo = c.partida.pegarTributo("X");
		assertTrue("A coleta deve ser aceita na parada visitada", resultadoDePegarTributo);
		assertEquals("Posição do peão de Azul", "C", c.tabuleiro.posicaoDe(c.azul().getPeao(0)).getNome());
		assertEquals("Cavalos restantes no turno", 0, c.partida.getTurno().getCavalos());
		assertEquals("Espadas físicas de Azul", 1, c.azul().contarTributo(TipoTributo.ESPADA));
	}

	/**
	 * Objetivo: pular yurt próprio sem gastar o cavalo intermediário.
	 * Entrada: yurt azul em B; um cavalo da verde; destino C por K-B-C.
	 * Saída esperada: movimento aceito; custo um e B não consta das visitas.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void saltoSobreYurtProprioEconomizaCavaloEExcluiVisita() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		PartidaTest.exigir(c.tabuleiro.colocarYurt(c.tabuleiro.getParada("B"), c.azul()), "yurt do cenário");
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.VERDE), "um cavalo");
		boolean moveu = c.partida.moverPara("C");
		assertTrue("O movimento válido deve ser aceito", moveu);
		assertEquals("Posição do peão de Azul", "C", c.tabuleiro.posicaoDe(c.azul().getPeao(0)).getNome());
		assertEquals("Cavalos restantes no turno", 0, c.partida.getTurno().getCavalos());
		assertFalse("A parada B pulada não deve constar nas visitas", c.partida.getTurno().getParadasVisitadas().contains(c.tabuleiro.getParada("B")));
	}

	/**
	 * Objetivo: impedir coleta a partir de uma parada pulada.
	 * Entrada: Azul pula seu yurt em B e termina em C; X só é adjacente a B.
	 * Saída esperada: coleta recusada; espada de X e mão do jogador preservadas.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void paradaPuladaNaoAutorizaColetaDeProvincia() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		PartidaTest.exigir(c.tabuleiro.colocarYurt(c.tabuleiro.getParada("B"), c.azul()), "yurt do cenário");
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.VERDE), "um cavalo e uma mão");
		PartidaTest.exigir(c.partida.moverPara("C"), "salto de B");
		boolean resultadoDePegarTributo = c.partida.pegarTributo("X");
		assertFalse("A coleta deve ser recusada na parada pulada", resultadoDePegarTributo);
		assertEquals("Peças na província X", 1, c.tabuleiro.getProvincia("X").getPecas());
		assertEquals("Mãos restantes no turno", 1, c.partida.getTurno().getMaos());
		assertEquals("Espadas físicas de Azul", 0, c.azul().contarTributo(TipoTributo.ESPADA));
	}

	/**
	 * Objetivo: validar toda a rota explícita antes de aplicar o primeiro passo.
	 * Entrada: rota B-E; K-B existe, mas B-E não é uma ligação.
	 * Saída esperada: recusa; posição K, dois cavalos e somente K visitada.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void rotaComSegundoPassoInvalidoNaoAplicaPrimeiroPasso() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.AMARELA), "dois cavalos");
		boolean resultadoDeMoverPor = c.partida.moverPor(Arrays.asList("B", "E"));
		assertFalse("A rota inválida deve ser recusada", resultadoDeMoverPor);
		assertEquals("Posição do peão de Azul", "K", c.tabuleiro.posicaoDe(c.azul().getPeao(0)).getNome());
		assertEquals("Cavalos restantes no turno", 2, c.partida.getTurno().getCavalos());
		assertEquals("Paradas visitadas após a recusa da rota", Collections.singleton(c.tabuleiro.getParada("K")), c.partida.getTurno().getParadasVisitadas());
	}

	/**
	 * Objetivo: aceitar um segundo jogador numa parada dupla.
	 * Entrada: Azul inicia em C; Vermelho em D dupla; um cavalo disponível.
	 * Saída esperada: Azul chega a D e a parada passa a ter dois peões.
	 */
	@Test(timeout = TEMPO_LIMITE)
	public void paradaDuplaAceitaSegundoOcupante() {
		PartidaTest.Cenario c = PartidaTest.padrao().iniciar();
		c.tabuleiro.posicionarPeao(c.azul().getPeao(0), c.tabuleiro.getParada("C"));
		c.tabuleiro.posicionarPeao(c.jogadores.get(1).getPeao(0), c.tabuleiro.getParada("D"));
		PartidaTest.exigir(c.partida.escolherPeao(0), "posição inicial em C");
		PartidaTest.exigir(c.partida.ativarColuna(Coluna.VERDE), "um cavalo");
		boolean resultadoDeMoverPara = c.partida.moverPara("D");
		assertTrue("O movimento deve ser aceito", resultadoDeMoverPara);
		assertEquals("Peões na parada dupla D", 2, c.tabuleiro.peoesEm(c.tabuleiro.getParada("D")).size());
		assertEquals("Cavalos restantes no turno", 0, c.partida.getTurno().getCavalos());
	}
}
