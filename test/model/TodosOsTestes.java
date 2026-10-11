package model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/** Suíte JUnit 4 dos cenários das partes 1 e 3 do Model. */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	JogadorTest.class,
	CidadeProvinciaTest.class,
	TabuleiroTest.class,
	MapaOficialTest.class,
	MovimentoTest.class,
	PartidaTest.class,
	MovimentoPartidaTest.class,
	IntegracaoPartidaTest.class
})
public class TodosOsTestes {
}
