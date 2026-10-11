# Integração da parte 3

A parte 3 controla a partida no Model: preparação, turnos, ativação, movimento, coleta, ataques, yurts, Khan, tributos e sequência de encerramento. A parte 2 conecta cartas, tesouros, melhorias, bônus e entregas por `ComponentesPartida`. Não deve criar um segundo controle de turno nem outro estoque de tributos.

## Contratos dos componentes

| Operação | Responsabilidade da parte 2 |
|---|---|
| `preparacaoConcluida()` | Confirmar que todos resolveram as escolhas iniciais exigidas, incluindo conselheiro secreto e melhoria inicial de Jochi. |
| `efeitosDasMelhorias(Jogador, Coluna, int)` | Retornar `EfeitosColuna` com cavalos, mãos e tributos virtuais da coluna, respeitando o turno de aquisição. Uma melhoria comprada neste turno não pode produzir ações nele. |
| `receberTesouro(Jogador, Cidade)` | Registrar o tesouro no inventário tipado do jogador. O callback ocorre após a retirada do contador da cidade e a eventual revelação da próxima; a parte 2 conserva a sequência dos tipos para identificar a peça retirada. |
| `aposAcao(Partida)` | Avaliar as cartas abertas imediatamente após a ativação e após ações bem-sucedidas. Uma condição satisfeita por recursos virtuais concede o voto antes que eles sejam gastos. |
| `moverKhanParaMelhorias(Partida)` | Repor os espaços de melhorias e conceder tributos dos ícones expostos ao jogador da vez, usando os auxiliares de banco da partida recebida. Se retornar `false`, preservar todos os estados e recursos. |
| `temEscolhaPendente(Jogador)` | Informar escolhas obrigatórias que impedem o encerramento do turno, como uma recompensa gratuita ou o tesouro de um bônus. |
| `pontuarFimDeJogo(Partida)` | Aplicar uma única vez os votos de influência segundo as melhorias e os votos do conselheiro secreto. |

`EfeitosColuna` transporta as quantidades adicionais de cavalos e mãos e um `Map<TipoTributo, Integer>` de tributos virtuais. Tesouros virtuais exigem representação própria na parte 2: não pertencem ao enum de tributos, que contém apenas moeda, espada e yurt.

Os componentes base permitem iniciar uma partida para testar o subconjunto disponível. Essa preparação base não comprova a presença de cartas ou melhorias. Sem a parte 2, estão disponíveis apenas os votos já obtidos, sem influência ou conselheiro secreto. A identificação final do vencedor e os desempates ainda dependem dessa integração. `getTesourosRecebidos(Jogador)` é um contador acumulado de recebimentos; não representa o saldo de tesouros não entregues para desempate.

## Recursos e votos

O banco é limitado a 10 espadas, 10 tributos de yurt e 15 moedas, descontando as peças nas províncias e com os jogadores. A parte 2 deve solicitar gastos por `Partida.gastarTributo(TipoTributo, int)` e recebimentos por `Partida.receberTributoDoEstoque(TipoTributo, int)`, que atuam sobre o jogador da vez, sem chamar diretamente `Jogador.gastarTributo(...)` ou criar peças físicas. O gasto prioriza os recursos virtuais e devolve as peças físicas ao banco; recursos virtuais expiram com o turno. O recebimento devolve a quantidade efetivamente retirada do banco.

No callback `aposAcao`, a parte 2 concede votos por `Jogador.adicionarVotos(int)`. Ao retornar, `Partida` sincroniza os aumentos de votos de todos os jogadores com a ficha neutra e verifica o gatilho de encerramento. A pontuação adicional concedida por `pontuarFimDeJogo` não avança a neutra nem abre novos turnos. Cada carta aberta sai de jogo após ser cumprida, e apenas o primeiro jogador recebe seu voto.

Depois de uma ação própria bem-sucedida, a parte 2 chama `Partida.concluirAcaoComplementar(boolean usaPeao)`. Informar `true` em entregas ou outras ações de posição fixa o peão de Altani; compras e bônus independentes da posição usam `false`. Esse método avalia as cartas e sincroniza os votos. Os auxiliares de banco não fazem essa conclusão automaticamente. Não chamar `concluirAcaoComplementar` de dentro de `aposAcao`, para evitar recursão. Cavalos, mãos e tributos virtuais de bônus podem ser acrescentados por `partida.getTurno().adicionarEfeitos(EfeitosColuna)`.

`terminarTurno()` verifica Khan e escolhas pendentes antes de concluir. Ele recupera as fichas quando a coluna é branca e sincroniza votos e gatilhos, mas **não chama `aposAcao` novamente**: as cartas já foram avaliadas durante as ações. Assim, encerrar não abre uma nova escolha obrigatória nem repete uma recompensa.

Uma recompensa impossível por esgotamento completo das melhorias precisa deixar de bloquear o turno. A parte 2 deve conservar a política explicitamente documentada de estoque finito, sem fabricar peças. Chamadas recusadas preservam o estado: validar antes de gastar ou retirar qualquer peça.

## Regras que a integração deve preservar

- **Ataque (p. 10):** o primeiro tesouro de cada cidade no turno custa uma espada; cada tesouro seguinte da mesma cidade custa duas. O contador é por cidade, não global.
- **Ogedei (p. 20):** a compra por moeda não aumenta esse contador de ataques com espada; estando adjacente, pode tomar o próximo tesouro da mesma cidade com uma espada.
- **Visitas (pp. 8, 10 e 11):** coleta, ataque e construção podem usar as paradas visitadas durante o turno. Paradas puladas com yurts próprios não contam como visitadas.
- **Altani (p. 20):** somente um peão faz as ações no turno. A primeira ação que depende de posição fixa o peão, mesmo se ele não se mover.
- **Tolui (p. 20):** começa com um yurt em Karakorum, sem influência regional, e pode construir nas paradas adjacentes. Um peão oponente nessa parada não impede a construção.
- **Branca (p. 9):** recupera todas as fichas de ativação, inclusive a usada na branca; elas ficam disponíveis no próximo turno.
- **Khan (p. 9):** a ação indicada pela coluna é obrigatória; o Khan muda de lugar e repõe sua província e as duas apontadas, respeitando o limite de três peças e o banco.
- **Encerramento (p. 14):** alcançar 10/14/16/17 votos neutros em partidas de 2/3/4/5 jogadores, ou conquistar todas as cidades, dispara o fim. Termina-se a rodada atual e joga-se mais uma rodada completa antes da pontuação final.

## Reaproveitamento da versão individual

| Aproveitar | Adaptação necessária |
|---|---|
| Condições das cartas abertas e secretas | Trocar `Mapa`/`TabuleiroJogador` e o estado de `Jogo` pelos objetos do grupo e pelos componentes. |
| Melhorias, bônus e inventários de tesouro | Integrar a ativação, o turno de aquisição, as escolhas pendentes e o banco de `Partida`. |
| Testes e cenários de regressão | Reescrever a preparação e as chamadas para a API do grupo; manter as expectativas de regras. |
| Regras de influência e desempate | Aplicar na pontuação final com as melhorias reais e os tesouros não entregues da parte 2. |

Os módulos são referências de lógica, não substituições diretas de arquivos. `Jogo`, `Turno`, `Mapa` e seus enums da versão individual usam contratos diferentes. Neste repositório, `Partida` e `Tabuleiro` permanecem os responsáveis pelo estado e pelas decisões do Model.
