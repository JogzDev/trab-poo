# Herdeiros de Khan: Trabalho de INF1636 (POO)

Projeto Eclipse em Java do jogo **Herdeiros de Khan**, organizado em MVC.
Esta é a **1ª iteração** (28/09 → 11/10/2026): só o componente **Model**, sem
View nem Controller, com testes unitários em JUnit 4.

> Importante: o jogo do trabalho é **Herdeiros de Khan** (manual em
> `Regas.pdf`), não War. O documento da 1ª iteração usa War apenas como
> exemplo de como o Controller consulta o Model. A divisão de tarefas inicial
> do grupo foi escrita com termos de War; a tabela de correspondência está
> mais abaixo.

## Estrutura do projeto

```
.project / .classpath / .settings   Projeto Eclipse (JavaSE-17, JUnit 4 em lib/)
src/model/                          Pacote Model (todas as classes são package-private)
test/model/                         Testes JUnit 4 (mesmo pacote, pasta de fontes separada)
lib/                                junit-4.13.2.jar e hamcrest-core-1.3.jar
scripts/run_tests.sh                Roda os testes pela linha de comando (sem Eclipse)
docs/                               Resumo das regras usadas pelo Model e imagem do mapa
```

Conforme o enunciado, as classes do Model **não são públicas**: só o pacote
`model` as enxerga. A API pública (Façade + Singleton) será criada na 3ª
iteração. Todas as coleções usam o framework de coleções (`HashMap`,
`HashSet`, `ArrayList`).

## Classes do Model (parte 1: tabuleiro e jogadores)

| Classe | Papel |
|---|---|
| `Cor` (enum) | As 5 cores de jogador. |
| `Regiao` (enum) | Rússia, China e Pérsia; cada uma ligada a uma coluna colorida da cartela. |
| `Coluna` (enum) | Vermelha, Verde, Amarela e Branca (colunas de ações da cartela). |
| `TipoTributo` (enum) | Espada, Yurt e Moeda. |
| `TipoParada` (enum) | Simples (1 peão / 1 yurt), Dupla (2 / 2, tem conselheiro) e Karakorum (todos / 0). |
| `Parada` | Nó das rotas. Guarda os yurts construídos nela, respeitando a capacidade. |
| `Cidade` | Uma das 9 cidades: revelada → 4 tesouros → último tesouro conquista (yurt de conquista). |
| `Provincia` | Área entre rotas com um tipo de tributo (máx. 3 peças); 4 delas são "do Khan" e apontam para 2 vizinhas. |
| `Peao` | Peão de um jogador (Altani tem 2). |
| `Herdeiro` (enum) | Os 5 herdeiros, habilidade e as 4 colunas de ações transcritas das cartelas. |
| `Jogador` | Cor, herdeiro, peões, estoque de 12 yurts, 4 peças de ativação, tributos e votos. |
| `Tabuleiro` | `HashMap<String, Parada>` + `HashMap<Parada, Set<Parada>>` (rotas) + cidades, províncias, posição dos peões; consultas de vizinhança, yurts, maioria por região e movimento. |
| `MapaOficial` | Monta o `Tabuleiro` com a geografia do jogo (**rotas a validar**, ver abaixo). |

### Correspondência com a divisão original (termos de War → Herdeiros de Khan)

| Combinado no grupo | Neste projeto | Observação |
|---|---|---|
| `Territorio` | `Parada` | Nó do mapa; não tem um único dono, guarda yurts de várias cores. |
| `HashMap<String, Territorio>` | `Tabuleiro.paradas` | Igual. |
| `HashMap<Territorio, Set<Territorio>>` fronteiras | `Tabuleiro.rotas` | Igual (rotas sempre simétricas). |
| `Continente` + bônus de exércitos | `Regiao` | O "bônus" do jogo são os votos de influência por maioria de yurts. |
| `getTerritorio(nome)` | `getParada(nome)` | Lança `IllegalArgumentException` se não existe. |
| `saoVizinhos(a, b)` | `saoVizinhas(a, b)` | |
| `donoDe(territorio)` | `temYurtDe(parada, jogador)` / `Parada.getYurts()` | |
| `exercitosEm(territorio)` | `yurtsEm(parada)` | |
| `adicionarExercitos` / `removerExercitos` | `colocarYurt(parada, jogador)` / `removerYurt(...)` | Mexem no estoque do jogador. |
| `territoriosDe(jogador)` | `paradasComYurtDe(jogador)` | |
| `dominaContinente(jogador, continente)` | `temMaioriaDeYurts(jogador, regiao)` + `lideresEmYurts` / `segundosEmYurts` | Regra de empate do manual (p. 15). |

O que o Model **não** faz de propósito: decidir *quando* uma ação é permitida
(sequência do turno, gasto de cavalos, uso do Khan). Isso é do Controller, que
usa as consultas do `Tabuleiro` (`saoVizinhas`, `podeTerminarMovimentoEm`,
`cidadesAdjacentes`, `provinciasAdjacentes`...). Cartas, tesouros e melhorias
são das outras partes do trabalho e se encaixam em `Jogador`/`Cidade` sem
alterar o que existe.

## ⚠️ Mapa: o que precisa ser conferido com o tabuleiro físico

`MapaOficial` foi transcrito do manual escaneado (pp. 4 a 5), onde a rede de rotas
aparece pequena. Está **confirmado**: 3 regiões; 9 cidades (3 por região);
7 conselheiros (6 paradas duplas + Karakorum); 4 províncias do Khan.
É **aproximação**: quantas paradas há em cada anel de cidade, quais rotas ligam
os anéis, e o tributo/posição de cada província. Para corrigir, edite apenas
as listas em `MapaOficial.java` (uma linha por parada/rota/província); os
testes de `MapaOficialTest` continuam valendo. Veja `docs/mapa-manual.jpg`.

## Como rodar os testes sem Eclipse

```bash
./scripts/run_tests.sh
```

Compila `src/` e `test/` com `javac` e roda `org.junit.runner.JUnitCore`.
Resultado atual: **146 testes, todos passando** (29 da parte 1, 46 da parte 2 e 71 da parte 3).
O script respeita `JAVA_HOME`; use um JDK 17 ou superior, como o incluído no Eclipse.

## Testes (JUnit 4)

Cada caso de teste documenta **Objetivo / Entrada / Saída esperada** no
Javadoc. Os principais (os três pedidos na divisão do grupo estão em negrito):

- `TabuleiroTest`: **vizinhança correta** (rota simétrica, não-vizinhas a dois
  passos, parada inexistente), **"dono muda"** (construir yurt retira do estoque
  e marca a parada; capacidade 1/2/0 por tipo de parada; remoção devolve ao
  estoque), **maioria na região** (empate no 1º lugar, 2º lugar, yurt de
  conquista de cidade contando para a região), movimento de peões (só vizinha,
  capacidade de peões, peões de Altani não se bloqueiam), adjacência de cidades e
  províncias.
- `CidadeProvinciaTest`: revelar/atacar/conquistar cidade; máximo de 3 peças
  por província; setas do Khan só em províncias do Khan.
- `JogadorTest`: preparação inicial; peças de ativação; gasto de tributo
  insuficiente; coerência das colunas transcritas das cartelas.
- `MapaOficialTest`: invariantes do mapa: 9 cidades (3/região), 7
  conselheiros, rotas simétricas, mapa conexo a partir de Karakorum, 4
  províncias do Khan com 2 vizinhas e 1 tributo inicial em cada província.

## Parte 3: partida, turnos e movimento

Esta contribuição acrescenta `Partida`, `Turno`, `Fase`, `Movimento` e os testes da parte 3, preservando as classes e a documentação original da parte 1 acima. `Partida` decide as regras de preparação, turno, gasto de recursos, ações e encerramento no Model. O futuro Controller poderá consultar a legalidade e solicitar a execução das ações.

- `podeAtacarCidade`, `podePegarTributo` e `podeConstruirYurt` consultam a legalidade sem gastar recursos nem alterar o estado.
- `moverPara` escolhe um caminho de menor custo e salta automaticamente yurts próprios intermediários. O destino sempre custa um cavalo e conta como visitado.
- `moverPor(List<String>, List<Boolean>)` permite escolher em cada passagem intermediária se o peão salta o yurt próprio ou visita a parada pagando um cavalo. Assim, passar por um yurt pode habilitar coleta, ataque ou construção quando o jogador optar pela visita.
- A branca recupera todas as fichas para o próximo turno. O fim termina a rodada atual e permite mais uma rodada completa.

`ComponentesPartida` e `EfeitosColuna` ligam a partida à implementação da parte 2, `ComponentesKhan`. Os métodos padrão continuam disponíveis para testar isoladamente as partes 1 e 3. A integração acrescenta preparação dos componentes e seleção do tipo de tesouro nos ataques, preservando as assinaturas anteriores.

O construtor simples de `Partida` mantém o subconjunto das partes 1 e 3. Ao fornecer `ComponentesKhan`, a partida também usa cartas, tesouros, melhorias, entregas, bônus, influência regional e desempates finais. As limitações do mapa descritas acima continuam válidas; a configuração física das melhorias por cor e dos conselheiros deve ser fornecida explicitamente.

Consulte [docs/parte3-integracao.md](docs/parte3-integracao.md) para os contratos, consultas e escolhas de movimento, e [docs/testes-parte3.md](docs/testes-parte3.md) para os 71 testes dessa parte.

## Parte 2: cartas, tesouros, melhorias e votos

- `Carta`, `Baralho`, `CartasOficiais`, `Objetivo`, `ObjetivoMapa` e `ObjetivoRecursos`: os 14 conselheiros abertos e 10 secretos; escolha inicial, distribuição por jogador e descarte sem reposição.
- `Inventario`, `Melhoria`, `TipoTesouro`, `TipoBonus` e `Conselheiro`: peças físicas, efeitos virtuais, entregas e bônus de uso único.
- `ComponentesKhan`: integra a preparação, as ações e as escolhas pendentes à `Partida`.
- `Pontuacao`: influência por maioria regional, cartas secretas e desempates por tesouros não entregues e distância da capital.

O catálogo de cores das 44 melhorias não foi inventado a partir do PDF monocromático: a lista de peças e os pedidos/bônus dos conselheiros entram como configuração. Veja os dados pendentes, as interpretações de casos ambíguos e exemplos de uso em [docs/parte2-integracao.md](docs/parte2-integracao.md). Os testes novos estão documentados em [docs/testes-parte2.md](docs/testes-parte2.md).

No Eclipse, atualize o projeto com **F5** e execute `test/model/TodosOsTestes.java` em **Run As > JUnit Test**. No terminal, use `JAVA_HOME="/caminho/do/jdk" ./scripts/run_tests.sh`. Validação desta revisão: **146 testes, zero falhas**, compilados com `--release 17` usando o JDK 25 do Eclipse.
