# Testes da parte 3

Os testes usam JUnit 4 e ficam no pacote `model`, com acesso às classes de visibilidade de pacote. Cada cenário documenta no Javadoc **objetivo, entrada e saída esperada**, prepara estado independente e verifica a regra junto com seus efeitos. As asserções têm mensagens específicas; chamadas que alteram estado são executadas uma única vez por cenário. Uma ação recusada deve preservar recursos, posição e demais estados envolvidos.

Os componentes substitutos usados pelos testes isolam o contrato da parte 3. A implementação real `ComponentesKhan` possui testes próprios, documentados em [testes-parte2.md](testes-parte2.md).

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
| `MovimentoTest` | 15 | Cálculo de caminhos e validação de rotas explícitas com saltos automáticos. |
| `PartidaTest` | 16 | Preparação e ordem dos jogadores; ativação; recursos e bancos finitos; Khan; ataques; yurts; branca; gatilhos e duração do encerramento. |
| `MovimentoPartidaTest` | 8 | Caminhos e custo em cavalos; passagem por ocupantes; capacidade de destino; saltos sobre yurts; visitas válidas para ações posteriores; escolha de Altani. |
| `IntegracaoPartidaTest` | 15 | Componentes substituíveis para preparação, melhorias, cartas, tesouros, Khan na área de melhorias, escolhas pendentes e pontuação final. |
| `MovimentoOpcionalTest` | 6 | Escolha entre saltar um yurt próprio e visitar a parada pagando um cavalo; validação das flags e preservação do estado em rotas inválidas. |
| `ConsultasPartidaTest` | 11 | Legalidade de ataque, coleta e construção sem alteração de recursos, posição ou estado do turno. |

As quatro classes da base e as seis classes da parte 3 somam **29 + 71 = 100 testes**. `TodosOsTestes` agora também inclui os 46 testes da parte 2: **146 no total**.

Os testes de integração usam implementações pequenas de `ComponentesPartida` para provar que a partida chama os contratos no momento correto. Eles complementam os testes das cartas e melhorias reais da parte 2.

## Verificações prioritárias

- O primeiro ataque custa uma espada **por cidade e por turno**; ataques seguintes à mesma cidade custam duas.
- A compra de Ogedei por moeda preserva o custo de uma espada para o próximo ataque à mesma cidade.
- Recursos físicos usados voltam ao banco; recursos virtuais não fabricam peças e expiram no turno seguinte.
- Uma carta cumprida na ativação é avaliada imediatamente, antes de gastar seus recursos virtuais.
- A primeira ação de posição de Altani fixa seu peão, mesmo antes de mover.
- Um caminho recusado não altera a posição nem gasta cavalos. Paradas puladas por yurt não habilitam coleta, ataque ou construção.
- Visitar um yurt próprio intermediário por escolha explícita custa um cavalo e permite ações que dependem daquela parada.
- Consultar a legalidade de ataque, coleta ou construção preserva os recursos e demais estados, inclusive nos casos inválidos.
- A branca recupera todas as fichas para o próximo turno; Khan e escolhas obrigatórias impedem finalizar antes da resolução.
- Encerrar o turno não reavalia cartas nem cria nova escolha obrigatória: os callbacks já ocorreram nas ações bem-sucedidas.
- O fim respeita os jogadores restantes da rodada atual e uma rodada completa adicional; a pontuação final executa uma única vez.

## Execução

Com JDK 17 ou superior:

```sh
./scripts/run_tests.sh
```

No Eclipse, executar `test/model/TodosOsTestes.java` com **Run As > JUnit Test**. O script compila fontes e testes, descobre as classes terminadas em `Test` e executa o JUnit com as bibliotecas de `lib/`.

Resultado histórico da entrega isolada das partes 1 e 3, com JDK 17: **`OK (100 tests)`**. Resultado após integrar a parte 2, com o JDK do Eclipse e `--release 17`: **`OK (146 tests)`**. A cobertura e os limites dos testes reais dos componentes estão em [testes-parte2.md](testes-parte2.md).
