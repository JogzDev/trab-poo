# 3ª iteração: integração da interface com o jogo completo

**Implementação funcional concluída.** Validação: 193 testes, incluindo uma
partida completa por botões e cliques. O cadastro oficial permanece pendente;
`config/pecas.demonstracao.txt` permite validar os fluxos com dados sintéticos.
Roteiro de execução e entrega em `docs/entrega-iteracao3.md`.

O enunciado pede acesso às regras existentes pela interface, Singleton, Façade
e Observer, com interfaces de observado/observador, e Controller coordenando
o fluxo. Salvamento e recuperação pertencem à última iteração.

## Primeiro marco implementado: preparação

- A demonstração da 2ª iteração continua disponível pela tela inicial.
- A opção **Preparar com cartas, tesouros e melhorias** usa `ComponentesKhan`,
  com cadastro explícito das peças, em vez de `ComponentesDaTela`.
- A tela inicial permite selecionar um arquivo UTF-8 de cadastro. A validação
  acontece ao solicitar a criação; dados inválidos mostram o motivo na janela
  e preservam a partida anterior. Não há cadastro oficial embutido ainda.
- Cada jogador recebe duas cartas secretas e confirma uma. Antes de revelar
  as opções, a tela pede que seja passada ao jogador correspondente. A outra
  carta é descartada pelo Model. As cartas mostram condições e patamares.
- Até a última escolha, nenhuma coluna, movimentação ou término de turno é
  permitido. A última confirmação inicia a partida automaticamente.
- O mercado, as cartas abertas, os tesouros e o inventário inicial de Jochi são
  preparados pelos componentes reais. Sua exposição durante os turnos está
  integrada nos painéis descritos nos marcos seguintes.

## Segundo marco implementado: ações no mapa

A janela da partida possui abas laterais **Turno** e **Ações**. A nova aba
mostra tributos físicos e virtuais, mãos restantes, yurts no estoque, votos e
tesouros recebidos. Suas escolhas e botões são atualizados pelo Observer:

- **Construir yurt**: lista somente as paradas em que o Model permite
  construir, incluindo o alcance especial de Tolui. Consome o tributo e a
  peça de yurt; o tabuleiro desenha as tendas nas cores dos jogadores.
- **Pegar tributo**: lista as províncias de coleta permitida, informa o tipo
  e a quantidade restante, e retira uma peça e uma mão por ação.
- **Atacar**: permite escolher a cidade e o tipo de tesouro disponível. O
  botão mostra o custo atual: uma espada no primeiro ataque à cidade no
  turno e duas nos seguintes. Cidade fechada, sem alcance, sem recursos ou
  sem yurt para conquistar não permite o ataque.
- **Ogedei**: permite pagar uma moeda por um tesouro de qualquer cidade
  revelada, uma vez por turno, sem retirar seu último tesouro.
- **Chagatai**: permite trocar um cavalo por uma espada uma vez por turno,
  respeitando o estoque do banco.

Movimento de peões, troca do peão de Altani e deslocamento do Khan continuam
nos cliques no mapa. As novas ações são coordenadas por
`Controlador → ModelAPI → Partida`; a View não calcula custos nem alcance.
A conquista da cidade também aparece como um yurt na região central do seu
anel no mapa. A demonstração continua sem tipos físicos de tesouro e usa a
opção automática; uma partida com cadastro válido permite a escolha do tipo.

Novas consultas de legalidade para Ogedei, Chagatai e ataques com tipo de
tesouro foram extraídas da execução existente em `Partida`. Consultar não
consome recursos e a execução usa as mesmas condições. Cada comando aceito
na fachada notifica os observadores uma vez; comandos recusados não notificam.

## Terceiro marco implementado: mercado e cartela

A aba **Melhorias** apresenta as peças abertas do mercado, identificadas pela
cor e pelo efeito. A posição 1 corresponde à direita do mercado. Há também
a opção de comprar às cegas do topo da pilha fechada; apenas a quantidade
restante é mostrada, sem expor a peça.

- O jogador seleciona uma oferta e compra por duas moedas, físicas ou virtuais.
  O Model consome as virtuais primeiro e verifica o limite de quatro peças
  por coluna. Uma compra não repõe automaticamente o mercado.
- A cartela pode ser consultada para qualquer jogador, mostrando as melhorias
  das quatro colunas e seus efeitos. A peça adquirida no turno atual fica
  marcada como disponível a partir do próximo turno.
- Completar uma linha das três cores pode gerar a escolha obrigatória de uma
  melhoria para a branca, respeitando a melhoria inicial de Jochi. A janela
  abre automaticamente a aba **Melhorias** e o painel do turno explica o
  bloqueio. O botão de recompensa não cobra moedas.
- Depois de comprar ou escolher uma recompensa, a seleção da oferta é limpa
  para evitar que um segundo clique compre a peça que acabou de ocupar a
  mesma posição. Mercado e cartela são atualizados pelo Observer.
- Mover o Khan para a área de melhorias no mapa repõe as peças pelas regras
  existentes. A interface reflete o novo mercado e a reserva.
- Na demonstração sem cadastro, a aba explica a indisponibilidade das melhorias.

A fachada acrescenta `getMercadoMelhorias()`, `getMelhoriasNaReserva()`,
`getMelhoriasJogador(jogador, coluna)`, `isMelhoriaGratuitaPendente()`,
`podeComprarMelhoria(indice)`, `comprarMelhoria(indice)`,
`podeEscolherMelhoriaGratuita(indice)` e `escolherMelhoriaGratuita(indice)`.
Índice `-1` representa a pilha fechada; os demais correspondem à lista do
mercado. As consultas de legalidade são sem efeitos colaterais e compartilham
as condições usadas na execução em `ComponentesKhan`.

## Quarto marco implementado: entregas e bônus

A aba **Entregas** mostra os sete conselheiros, o pedido de cada um, os votos
pela entrega, a recompensa da primeira entrega e se a parada foi visitada
pelo peão ativo neste turno. O jogador escolhe a quantidade de cada tipo de
tesouro em campos de 0 a 3, acompanhados dos saldos físicos e virtuais.

- O Model verifica pedido, visita e saldo antes de aceitar a entrega. Os
  tesouros virtuais são consumidos primeiro; os físicos são descartados.
- A primeira entrega retira o bônus do conselheiro. Entregas posteriores
  continuam pontuando, mas não recebem a mesma peça novamente.
- O painel mostra os bônus obtidos pelo jogador da vez, seus efeitos e se
  já foram usados. O bônus de voto é aplicado automaticamente. Os demais
  podem ser ativados uma única vez, pelas regras existentes.
- O bônus de tesouro retira até três peças da reserva. A interface abre
  automaticamente **Entregas** e apresenta as opções; confirmar uma guarda
  essa peça e descarta as restantes. O turno fica bloqueado até a escolha.
- Se também existir uma melhoria gratuita pendente, a escolha do tesouro é
  apresentada primeiro. Depois, a janela abre **Melhorias** para resolver a
  outra recompensa. O painel do turno explica a escolha ainda necessária.
- Depois de alterações no Model, o Observer atualiza os saldos e limpa a
  oferta, evitando uma segunda entrega acidental. Movimento continua no mapa.

Novos comandos da fachada e do Controller: `entregarTesouros(parada, oferta)`,
`usarBonus(tipo)` e `escolherTesouroDoBonus(tipo)`. A oferta é um
`Map<String, Integer>`: zeros são omitidos; tipos desconhecidos, repetidos
após normalização ou quantidades inválidas são recusados sem alterações.
A fachada também expõe pedidos, bônus, votos de entrega, visita, tesouros
virtuais, opções pendentes e consultas de legalidade. Nenhuma classe interna
do Model é exposta à View.

## Objetivos, Kurultai, rotas e resultado

- **Objetivos** mostra as cartas abertas restantes e o progresso do jogador
  da vez. A pontuação continua automática no Model, sem comandos para ganhar votos.
- Revelar/ocultar a carta secreta passa pelo Controller e pela fachada e gera
  notificação Observer. A troca de turno a oculta automaticamente. Não é
  possível consultar a carta de outro jogador antes do encerramento.
- A seleção da cartela pública também passa pelo Controller e Observer.
- **Turno** e **Objetivos** mostram Kurultai, votos e a rodada final, assim
  que o Model dispara o encerramento. A ficha neutra não inclui os bônus finais.
- **Resultado** abre automaticamente ao encerrar. Mostra vencedor(es), votos
  obtidos durante o jogo, influência regional, carta secreta e seus votos,
  tesouros físicos restantes e distância de Karakorum. O Model decide os
  desempates e preserva a vitória compartilhada quando aplicável.
- A pontuação de influência e cartas é registrada uma única vez. Comandos de
  jogo são recusados após o encerramento.
- No modo **Traçar rota no mapa**, o jogador clica em paradas consecutivas e
  executa ao clicar de novo na última. A opção de saltar yurt vale para cada
  parada ao ser acrescentada; o destino sempre é visitado. Isso permite
  passar por uma parada ocupada e escolher visitar ou saltar yurts próprios.
  A validação da rota é do Model; limpar o planejamento não move o peão.

`Controlador.getEtapaPartida()` organiza as prioridades: preparação,
resultado, escolha de tesouro, escolha de melhoria e jogo normal. A janela
consulta essa etapa em seu callback Observer. A validação das regras e a
pontuação permanecem em `Partida` e `ComponentesKhan`.

## Contrato de integração

| Camada | Responsabilidade |
|---|---|
| Model / `Partida` / `ComponentesKhan` | Regras, recursos, legalidade, cartas, peças e pontuação. |
| `ModelAPI` | Fachada Singleton; consultas e comandos com tipos públicos simples; notificação após ações aceitas. |
| `Controlador` | Receber intenções da View, encaminhar comandos e abrir a janela da partida. |
| Views observadoras | Refletir o estado e habilitar as interações apropriadas. |

Novos métodos da fachada:

- `novaPartidaCompleta(nomes, herdeiros, linhasDoCadastro)`: prepara, sem iniciar os turnos.
- `getErroConfiguracao()`: motivo da última recusa ao criar a preparação completa.
- `isPreparacao()` e `getJogadorPreparando()`: fase e jogador que deve escolher (`-1` fora da preparação).
- `getOpcoesCartaSecreta()`: somente as duas opções do jogador da preparação atual, com descrições.
- `escolherCartaSecreta(indice)`: aceita 0 ou 1; avança a preparação; a última escolha inicia os turnos.
- `getCartasAbertas()`: nomes e descrições das cartas abertas disponíveis.

`JanelaJogo` observa a fachada e usa `CardLayout` para trocar da preparação
para o tabuleiro. `PainelPreparacao` também é observador: cada confirmação
limpa as opções e oculta as próximas cartas até o próximo jogador revelá-las.
Apenas revelar/selecionar uma opção é estado local da View; confirmar altera
o Model e gera uma notificação. Os listeners são classes, sem lambdas.

## Cadastro pendente

Preencher uma cópia de `config/pecas.modelo.txt` usando o material oficial.
O modelo contém somente instruções; ele é propositalmente recusado até que
as peças e os pedidos sejam cadastrados. Não usar o cadastro dos testes como
material oficial: suas cores são sintéticas.

Formato de cada linha, separado por ponto e vírgula:

```text
MELHORIA;COR;SIMBOLO;QUANTIDADE
CONSELHEIRO;NOME DA PARADA;PEDIDO
```

Cores aceitas: `VERMELHA`, `VERDE`, `AMARELA`. Símbolos: `MOEDA`, `ESPADA`,
`YURT`, `MAO`, `CAVALOS`, `FERRO`, `PELES`, `GADO`, `TECIDO`, `ESPECIARIAS`.
Pode haver várias linhas da mesma combinação; as quantidades são somadas.
O total deve ser 44 peças: 8 moedas, 8 espadas, 8 yurts, 6 mãos, 9 cavalos
e uma de cada um dos cinco tesouros. A distribuição entre cores deve ser
conferida no material oficial; a validação dos totais não comprova essa distribuição.

Para uma parada dupla, o pedido é `TIPO:2` ou `QUAISQUER`, conforme a peça
oficial. Para Karakorum, são três tipos distintos com quantidade 1, separados
por vírgula, conforme a face escolhida. Os sete nomes de paradas estão no
modelo e devem aparecer exatamente uma vez. Bônus não entram no arquivo:
as sete peças são embaralhadas automaticamente, uma para cada conselheiro.
Linhas vazias e linhas iniciadas por `#` são ignoradas.

## Situação para a entrega

Implementados: preparação, ações básicas, habilidades, mercado, cartelas,
entregas, bônus, escolhas obrigatórias, objetivos, movimentos com rota explícita,
Kurultai e resultado final. Singleton, Façade e Observer estão preservados;
o Controller coordena a sequência de telas e encaminha os comandos.

**Pendência de dados:** conferir e cadastrar as cores das 44 melhorias e os
pedidos dos sete conselheiros a partir do material oficial. O cadastro sintético
incluído é para demonstração e não comprova fidelidade à distribuição física.
Não há salvamento/recuperação: o enunciado reserva esse recurso para a última iteração.

O empacotamento do projeto Eclipse é reproduzível com
`python3 scripts/package_delivery.py`. O ZIP inclui fontes, testes, bibliotecas,
imagem, configurações do Eclipse, documentação e os cadastros, sem `.git`,
classes compiladas ou arquivos temporários.

## Validação

`./scripts/run_tests.sh` usa JDK 17 ou superior. A suíte tem **193 testes**, incluindo os testes da
2ª iteração, preparação completa, ações da fachada, melhorias, entregas, bônus, objetivos,
rotas e fluxos Swing na EDT.
Os cenários verificam privacidade das cartas, coleta, construção, escolha de
tesouros, custo dos ataques, limites das habilidades especiais, compras,
reposição do mercado, recompensas de linhas completas, entregas, exclusividade
dos bônus, seus efeitos, resolução da escolha de tesouro, ocultação de cartas
na troca de turno e encerramento de uma partida completa pelos controles. Também pode ser executada no Eclipse por
`test/model/TodosOsTestes.java`.
