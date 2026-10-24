package model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/** Suíte JUnit 4 do Model (1ª iteração) e da fachada pública (2ª iteração). */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	JogadorTest.class,
	CidadeProvinciaTest.class,
	TabuleiroTest.class,
	MapaOficialTest.class,
	MovimentoTest.class,
	MovimentoOpcionalTest.class,
	PartidaTest.class,
	MovimentoPartidaTest.class,
	IntegracaoPartidaTest.class,
	ConsultasPartidaTest.class,
	BaralhoTest.class,
	ObjetivosTest.class,
	InventarioTest.class,
	ComponentesKhanTest.class,
	PontuacaoTest.class,
	ModelAPITest.class
})
public class TodosOsTestes {
}
