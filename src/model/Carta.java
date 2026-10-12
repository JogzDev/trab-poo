package model;

/** Conselheiro aberto ou secreto; o objetivo define a forma de pontuar. */
final class Carta {
	private final String nome;
	private final Objetivo objetivo;
	Carta(String nome, Objetivo objetivo) {
		if (nome == null || nome.isBlank() || objetivo == null) throw new IllegalArgumentException("Nome e objetivo obrigatórios");
		this.nome = nome;
		this.objetivo = objetivo;
	}
	String getNome() { return nome; }
	Objetivo getObjetivo() { return objetivo; }
	boolean isSecreta() { return objetivo.isSecreto(); }
	@Override public String toString() { return nome; }
}
