# Testes da parte 3

Os testes usam JUnit 4 e ficam no pacote `model`, com acesso às classes de visibilidade de pacote. Cada cenário deve documentar no Javadoc **objetivo, entrada e saída esperada**, preparar estado independente e verificar a regra junto com seus efeitos. Uma ação recusada deve preservar recursos, posição e demais estados envolvidos.

## Base existente

A parte 1 tem 29 testes distribuídos em quatro classes:

| Classe | Casos | Cobertura principal |
|---|---:|---|
| `TabuleiroTest` | 15 | Rotas, adjacências, yurts, influência regional e capacidade de peões. |
| `CidadeProvinciaTest` | 5 | Revelação e conquista de cidades, limites de tributo e setas do Khan. |
| `JogadorTest` | 4 | Preparação, fichas de ativação, gasto de tributo e colunas dos herdeiros. |
| `MapaOficialTest` | 5 | Cidades e regiões, conselheiros, simetria, conectividade e províncias do Khan. |

Esses testes continuam fazendo parte da suíte. As invariantes do mapa não provam que todas as rotas correspondem ao tabuleiro físico.

## Casos da parte 3

| Classe | Casos | Responsabilidade |
|---|---:|---|
| `MovimentoTest` | 15 | Cálculo de caminhos e validação de rotas explícitas, sem depender do ciclo completo da partida. |
| `PartidaTest` | 16 | Preparação e ordem dos jogadores; ativação; recursos e bancos finitos; Khan; ataques; yurts; branca; gatilhos e duração do encerramento. |
| `MovimentoPartidaTest` | 8 | Caminhos e custo em cavalos; passagem por ocupantes; capacidade de destino; saltos sobre yurts; visitas válidas para ações posteriores; escolha de Altani. |
| `IntegracaoPartidaTest` | 15 | Componentes substituíveis para preparação, melhorias, cartas, tesouros, Khan na área de melhorias, escolhas pendentes e pontuação final. |

`TodosOsTestes` reúne as quatro classes da base e as quatro classes novas para execução no Eclipse: **29 + 54 = 83 testes**.

Os testes de integração usam implementações pequenas de `ComponentesPartida` para provar que a partida chama os contratos no momento correto. Eles não substituem os testes das cartas e melhorias reais, que ainda pertencem à parte 2.

## Verificações prioritárias

- O primeiro ataque custa uma espada **por cidade e por turno**; ataques seguintes à mesma cidade custam duas.
- A compra de Ogedei por moeda preserva o custo de uma espada para o próximo ataque à mesma cidade.
- Recursos físicos usados voltam ao banco; recursos virtuais não fabricam peças e expiram no turno seguinte.
- Uma carta cumprida na ativação é avaliada imediatamente, antes de gastar seus recursos virtuais.
- A primeira ação de posição de Altani fixa seu peão, mesmo antes de mover.
- Um caminho recusado não altera a posição nem gasta cavalos. Paradas puladas por yurt não habilitam coleta, ataque ou construção.
- A branca recupera todas as fichas para o próximo turno; Khan e escolhas obrigatórias impedem finalizar antes da resolução.
- Encerrar o turno não reavalia cartas nem cria nova escolha obrigatória: os callbacks já ocorreram nas ações bem-sucedidas.
- O fim respeita os jogadores restantes da rodada atual e uma rodada completa adicional; a pontuação final executa uma única vez.

## Execução

Com JDK 17 ou superior:

```sh
./scripts/run_tests.sh
```

No Eclipse, executar `test/model/TodosOsTestes.java` com **Run As > JUnit Test**. O script compila fontes e testes, descobre as classes terminadas em `Test` e executa o JUnit com as bibliotecas de `lib/`.

Resultado da execução da suíte completa no runner com Java 17: **`OK (83 tests)`**, zero falhas. As contagens por classe foram conferidas nas anotações `@Test`. Essa validação cobre o subconjunto entregue e seus contratos; não atesta cartas, melhorias e demais componentes reais da parte 2 ainda pendente.
