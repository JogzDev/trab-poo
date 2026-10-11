# Herdeiros de Khan — INF1636 (POO)

Projeto Eclipse em Java 17 para a **1ª iteração**, dedicada ao componente Model. A entrega reúne a **parte 1** (tabuleiro e jogadores) e a **parte 3** (partida, turnos e movimento). A parte 2 (cartas, tipos de tesouro, melhorias, bônus e entregas) ainda precisa ser integrada; esta versão não é o jogo completo.

## Estrutura

| Caminho | Conteúdo |
|---|---|
| `src/model/` | Classes do Model, todas com visibilidade de pacote. |
| `test/model/` | Testes JUnit 4, no mesmo pacote do Model. |
| `lib/` | JUnit 4.13.2 e Hamcrest Core 1.3. |
| `.project`, `.classpath`, `.settings/` | Configuração Eclipse, JavaSE-17. |
| `scripts/run_tests.sh` | Compilação e execução dos testes pelo terminal. |
| `docs/` | Regras, mapa de referência e contratos de integração. |

`Tabuleiro`, `Parada`, `Cidade`, `Provincia`, `Peao`, `Jogador` e os enums representam o mapa e os jogadores. `Partida`, `Turno`, `Fase` e `Movimento` controlam a preparação, a ordem dos jogadores, a ativação de colunas, as ações, o caminho percorrido e o encerramento. `ComponentesPartida` e `EfeitosColuna` permitem conectar a parte 2 sem duplicar esse controle.

O **Model decide se cada ação é legal** e mantém os recursos e estados consistentes. O futuro Controller solicitará ações e consultará seus resultados; a View apresentará esses dados. A fachada pública e o Singleton ficam para a iteração correspondente do trabalho.

## Classes da base — parte 1

| Classe | Papel |
|---|---|
| `Tabuleiro` | Grafo de paradas e rotas em coleções; registra cidades, províncias e posições de peões. Consulta vizinhança, adjacência, capacidade, yurts e maioria por região. |
| `Jogador` | Nome, cor, herdeiro, peões, estoque pessoal de 12 yurts, quatro fichas de ativação, tributos e votos. |
| `Parada` | Nó do mapa, com região, tipo e yurts de uma ou mais cores; respeita a capacidade de construção. |
| `Cidade` | Revelação com quatro tesouros, retirada de tesouros e conquista pelo último; o yurt de conquista conta para influência, sem auxiliar movimento. |
| `Provincia` | Tipo de tributo, até três peças, paradas adjacentes e setas das províncias do Khan. |
| `Peao` | Identifica um peão e sua cor; Altani possui dois. A posição fica no tabuleiro. |
| `Herdeiro` | Os cinco herdeiros, suas habilidades e ações impressas nas quatro colunas, transcritas das cartelas. |
| `MapaOficial` | Monta a geografia em `Tabuleiro`; as aproximações da transcrição estão descritas abaixo. |
| `Cor`, `Regiao`, `Coluna`, `TipoTributo`, `TipoParada` | Enums para as cinco cores, três regiões, quatro colunas, moeda/espada/yurt e parada simples/dupla/Karakorum. |

Os arquivos dessa base foram preservados. A parte 3 acrescenta o controle das regras de turno sobre esses objetos; a parte 2 deverá usar os contratos de integração para cartas, peças e inventários adicionais.

## Abrir e testar

No Eclipse, use **File > Import > General > Existing Projects into Workspace > Select root directory** e selecione a pasta do repositório. Configure um JDK 17 ou superior; as dependências JUnit já estão em `lib/`. Execute `test/model/TodosOsTestes.java` com **Run As > JUnit Test**.

No terminal, com JDK 17 ou superior:

```sh
./scripts/run_tests.sh
```

A suíte completa passou no runner com Java 17: **83 testes, zero falhas** — 29 da base e 54 das novas classes de movimento, partida e integração. Os testes de integração usam componentes substituíveis; não representam a parte 2 completa. Veja os cenários e a organização em [docs/testes-parte3.md](docs/testes-parte3.md).

## Integrar a parte 2

Os contratos, a política de tributos e os pontos de verificação das cartas estão em [docs/parte3-integracao.md](docs/parte3-integracao.md). A implementação base dos componentes permite testar o subconjunto entregue. Ela não resolve as escolhas de cartas, não concede tesouros tipados e não calcula a pontuação final de influência e conselheiro secreto. A identificação final do vencedor e os desempates dependem dessa integração.

## Limitações e referências

`MapaOficial` foi transcrito da imagem pequena do manual (pp. 4–5). Estão confirmadas três regiões, nove cidades (três por região), sete conselheiros (seis paradas duplas e Karakorum) e quatro províncias do Khan. Ainda são aproximações **a quantidade de paradas em cada anel de cidade, as rotas que ligam os anéis e o tipo de tributo e a posição de cada província**. Esses dados precisam ser conferidos com o tabuleiro físico e ajustados nas listas de `MapaOficial.java`. Os testes verificam invariantes como simetria e conectividade; isso não comprova toda a topologia. Consulte [docs/mapa-manual.jpg](docs/mapa-manual.jpg) e [docs/regras-para-o-model.md](docs/regras-para-o-model.md).

A versão individual mais abrangente oferece regras e testes aproveitáveis para cartas e melhorias, mas suas classes e API diferem das do grupo. A integração exige adaptar esses módulos aos contratos deste repositório; copiar a classe `Jogo` inteira criaria dois controles de partida e estoques concorrentes.
