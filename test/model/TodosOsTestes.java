package model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/** Suíte JUnit 4 dos cenários das três partes do Model. */
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
	PontuacaoTest.class
})
public class TodosOsTestes {
}
