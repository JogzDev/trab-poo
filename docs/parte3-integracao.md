# Integração da parte 3

A parte 3 controla a partida no Model: preparação, turnos, ativação, movimento, coleta, ataques, yurts, Khan, tributos e sequência de encerramento.

`ComponentesPartida` é uma **proposta provisória**, usada nesta contribuição para demonstrar a ligação com cartas, tesouros, melhorias, bônus e entregas. O contrato precisa ser alinhado com a Pessoa 2 antes da integração na `main`; não impõe uma arquitetura já aprovada para a parte 2. O PR permanece draft até esse alinhamento. As assinaturas abaixo descrevem a proposta atual e podem ser ajustadas em conjunto.

## Contratos dos componentes

| Operação | Responsabilidade proposta |
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

O banco é limitado a 10 espadas, 10 tributos de yurt e 15 moedas, descontando as peças nas províncias e com os jogadores. Nesta proposta, gastos passam por `Partida.gastarTributo(TipoTributo, int)` e recebimentos por `Partida.receberTributoDoEstoque(TipoTributo, int)`, que atuam sobre o jogador da vez. Assim, o integrador evita gastar diretamente em `Jogador` e perder a devolução ao banco. O gasto prioriza os recursos virtuais e devolve as peças físicas ao banco; recursos virtuais expiram com o turno. O recebimento devolve a quantidade efetivamente retirada do banco.

No callback `aposAcao`, a parte 2 concede votos por `Jogador.adicionarVotos(int)`. Ao retornar, `Partida` sincroniza os aumentos de votos de todos os jogadores com a ficha neutra e verifica o gatilho de encerramento. A pontuação adicional concedida por `pontuarFimDeJogo` não avança a neutra nem abre novos turnos. Cada carta aberta sai de jogo após ser cumprida, e apenas o primeiro jogador recebe seu voto.

Depois de uma ação própria bem-sucedida, a parte 2 chama `Partida.concluirAcaoComplementar(boolean usaPeao)`. Informar `true` em entregas ou outras ações de posição fixa o peão de Altani; compras e bônus independentes da posição usam `false`. Esse método avalia as cartas e sincroniza os votos. Os auxiliares de banco não fazem essa conclusão automaticamente. Não chamar `concluirAcaoComplementar` de dentro de `aposAcao`, para evitar recursão. Cavalos, mãos e tributos virtuais de bônus podem ser acrescentados por `partida.getTurno().adicionarEfeitos(EfeitosColuna)`.

`terminarTurno()` verifica Khan e escolhas pendentes antes de concluir. Ele recupera as fichas quando a coluna é branca e sincroniza votos e gatilhos, mas **não chama `aposAcao` novamente**: as cartas já foram avaliadas durante as ações. Assim, encerrar não abre uma nova escolha obrigatória nem repete uma recompensa.

O tratamento de recompensas impossíveis por esgotamento completo das melhorias ainda precisa ser alinhado com a Pessoa 2. A proposta é não fabricar peças e impedir que uma recompensa sem peça disponível bloqueie indefinidamente o turno. Chamadas recusadas preservam o estado: a implementação valida antes de gastar ou retirar qualquer peça.

## Consultas de legalidade e movimento opcional

`Partida.podeAtacarCidade(String)`, `podePegarTributo(String)` e `podeConstruirYurt(String)` permitem consultar uma ação antes de executá-la. Elas usam as regras da partida e preservam o estado, incluindo recursos físicos e virtuais, posição, visitas e efeitos de cartas. Os métodos de execução também validam novamente a ação.

`moverPara(String)` calcula um caminho de menor custo, com saltos automáticos sobre yurts próprios intermediários. A parada de destino custa um cavalo e conta como visitada, mesmo que tenha yurt próprio.

A rota explícita `moverPor(List<String>, List<Boolean> saltarYurts)` permite optar por uma visita intermediária em vez de saltar. A lista de nomes contém as chegadas de cada passo, sem incluir a origem e incluindo o destino. A lista de flags tem o mesmo tamanho: `false` paga um cavalo e registra a parada; `true` salta um yurt próprio intermediário, sem custo e sem registrar visita. Esse controle permite escolher uma parada que habilite ações posteriores.

Uma flag `true` no destino ou em parada sem yurt próprio é inválida. Listas nulas, flags nulas e tamanhos divergentes também são recusados. Uma rota recusada não move parcialmente o peão nem gasta cavalos. O overload `moverPor(List<String>)` continua usando saltos automáticos sobre yurts próprios intermediários.

## Regras que a integração deve preservar

- **Ataque (p. 10):** o primeiro tesouro de cada cidade no turno custa uma espada; cada tesouro seguinte da mesma cidade custa duas. O contador é por cidade, não global.
- **Ogedei (p. 20):** a compra por moeda não aumenta esse contador de ataques com espada; estando adjacente, pode tomar o próximo tesouro da mesma cidade com uma espada.
- **Visitas (pp. 8, 10 e 11):** coleta, ataque e construção podem usar as paradas visitadas durante o turno. Paradas puladas com yurts próprios não contam como visitadas.
- **Altani (p. 20):** somente um peão faz as ações no turno. A primeira ação que depende de posição fixa o peão, mesmo se ele não se mover.
- **Tolui (p. 20):** começa com um yurt em Karakorum, sem influência regional, e pode construir nas paradas adjacentes. Um peão oponente nessa parada não impede a construção.
- **Branca (p. 9):** recupera todas as fichas de ativação, inclusive a usada na branca; elas ficam disponíveis no próximo turno.
- **Khan (p. 9):** a ação indicada pela coluna é obrigatória; o Khan muda de lugar e repõe sua província e as duas apontadas, respeitando o limite de três peças e o banco.
- **Encerramento (p. 14):** alcançar 10/14/16/17 votos neutros em partidas de 2/3/4/5 jogadores, ou conquistar todas as cidades, dispara o fim. Termina-se a rodada atual e joga-se mais uma rodada completa antes da pontuação final.
