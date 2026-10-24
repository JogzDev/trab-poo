# 2ª iteração: tela gráfica com movimentação do peão

Objetivo da entrega: uma janela Swing em que os jogadores ativam uma coluna da
cartela e movem o peão pelo mapa, respeitando as regras de movimento do manual.

A arquitetura segue o MVC e os padrões do cap. 17 (Facade, Singleton e
Observer). As classes do Model continuam com visibilidade de pacote. A View e o
Controller falam com o Model **somente** pela fachada pública `model.ModelAPI`.

## Divisão do trabalho

Cada parte cria arquivos próprios, então os commits não entram em conflito.

| Parte | Responsável | Arquivos | Depende de |
|---|---|---|---|
| 1. Fachada, Observer e painel do turno | @JogzDev | `model/ModelAPI`, `model/ObservadorIF`, `model/ObservadoIF`, esqueleto de `controller/Controlador`, `view/PainelTurno`, `view/CoresJogador`, `test/model/ModelAPITest` | nada (já entregue) |
| 2. Tabuleiro gráfico | a definir | `view/PainelTabuleiro`, `view/CoordenadasMapa` | só do contrato abaixo |
| 3. Janelas e fluxo do jogo | a definir | `controller/Main`, `view/JanelaInicial`, `view/JanelaJogo`, corpo dos métodos de `Controlador`, README da 2ª iteração | só do contrato abaixo |

A Parte 1 já está pronta e testada (157 testes, zero falhas). Depois que ela
for commitada, a Parte 2 e a Parte 3 podem trabalhar ao mesmo tempo, porque uma
não usa nada da outra além de um construtor (`new PainelTabuleiro()`).

### Parte 1: o que já está pronto

- `ModelAPI`, `ObservadorIF` e `ObservadoIF`: descritos no contrato abaixo.
- `Controlador`: Singleton com as assinaturas combinadas, repassando para a fachada.
- `view.PainelTurno extends JPanel implements ObservadorIF` (largura `PainelTurno.LARGURA`):
  jogador da vez, herdeiro e rodada; um botão por coluna com as ações impressas na
  cartela, habilitado só se a ficha estiver livre e nenhuma coluna tiver sido
  ativada; cavalos restantes; escolha de peão da Altani enquanto `podeEscolherPeao()`;
  lista e botão para mover o Khan quando `isKhanPendente()`; botão "Terminar turno"
  habilitado com `podeTerminarTurno()`; dica do próximo passo e aviso quando o
  Controlador recusa uma ação.
- `view.CoresJogador`: converte os nomes de cor de jogador e de coluna em `java.awt.Color`.
  A Parte 2 deve usar essa classe para pintar os peões, assim as cores batem nos dois painéis.

### Parte 2: tabuleiro gráfico (`view.PainelTabuleiro`)

- `public class PainelTabuleiro extends JPanel implements ObservadorIF`, com
  construtor público sem parâmetros que chama `Controlador.getInstance().registra(this)`.
- Sobrescrever `getPreferredSize()` (sugestão: 1000 x 700) e `paintComponent(Graphics g)` com `Graphics2D`.
- `CoordenadasMapa`: um `Map<String, Point>` com a posição de cada uma das 39
  paradas (nomes em `ModelAPI.getParadas()`). Sugestão de disposição: Rússia ao
  norte, Pérsia a sudoeste, China a leste e Karakorum no centro. Cada cidade fica
  no meio do seu anel de paradas (`getParadasDaCidade`).
- O repositório já tem `docs/mapa-manual.jpg` (1200 x 1130). Uma opção é usar a
  imagem como fundo (`ImageIO.read`) e marcar as coordenadas sobre ela; a outra é
  desenhar o mapa de forma esquemática, só com rotas e paradas.
- Desenhar: rotas (`getVizinhas`), paradas com formato diferente por tipo
  (`getTipoParada`: SIMPLES, DUPLA, KARAKORUM), nome das cidades, peões na cor
  do jogador (`getCorJogador`, `getPosicaoPeao`). Se dois peões estiverem na
  mesma parada, desenhar lado a lado.
- Destacar os destinos possíveis (`getDestinosPossiveis`) e o peão ativo (`getPeaoAtivo`).
- `MouseListener` (ou `MouseAdapter`): achar a parada mais próxima do clique,
  dentro de um raio, e chamar `Controlador.getInstance().paradaClicada(nome)`.
- `notify(ObservadoIF o)`: só chama `repaint()`. O `paintComponent` lê o estado
  atual da `ModelAPI`.

Para testar sozinho, sem a Parte 3, usar um `main` temporário. Com o
`PainelTurno` ao lado, já dá para jogar turnos completos:

```java
Controlador.getInstance().novaPartida(List.of("Ana", "Bia"), List.of("ALTANI", "OGEDEI"));
JFrame f = new JFrame("Teste");
f.add(new PainelTabuleiro(), BorderLayout.CENTER);
f.add(new PainelTurno(), BorderLayout.LINE_END);
f.pack();
f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
f.setVisible(true);
```

### Parte 3: janelas e fluxo do jogo

- `controller.Main`: abre a `JanelaInicial`.
- `view.JanelaInicial extends JFrame`: escolher de 2 a 5 jogadores, nome de cada um
  e herdeiro sem repetição (`ModelAPI.getHerdeiros()`). Ao confirmar, chama
  `Controlador.getInstance().novaPartida(nomes, herdeiros)`. Se devolver `false`,
  mostrar a mensagem e não fechar; se devolver `true`, abrir a `JanelaJogo`.
- `view.JanelaJogo extends JFrame`: `BorderLayout`, com `new PainelTabuleiro()` no
  `CENTER` e `new PainelTurno()` no `LINE_END`; título com o nome do jogo e
  encerramento do programa ao fechar.
- `Controlador`: manter as assinaturas e completar o fluxo. Avisar quando um clique
  no mapa for recusado (`paradaClicada` devolveu `false`), anunciar a troca de
  turno e tratar o fim da partida (`isPartidaEncerrada()`).
- Atualizar o README para a 2ª iteração: como rodar o jogo pelo `controller.Main`
  e o que a tela permite fazer.

## Contrato (não mudar sem avisar o grupo)

### Observer (`model`)

```java
public interface ObservadorIF { void notify(ObservadoIF o); }
public interface ObservadoIF  { void add(ObservadorIF o); void remove(ObservadorIF o); }
```

A `ModelAPI` notifica todos os observadores depois de cada ação bem-sucedida
(nova partida, ativar coluna, escolher peão, mover, mover Khan, terminar turno).
O observador recebe a própria fachada e consulta o que precisa.

### Controlador (`controller`, Singleton)

| Método | Quem chama |
|---|---|
| `getInstance()` | todos |
| `registra(ObservadorIF)` / `desregistra(ObservadorIF)` | construtor dos painéis |
| `boolean novaPartida(List<String> nomes, List<String> herdeiros)` | JanelaInicial |
| `boolean paradaClicada(String parada)` | PainelTabuleiro |
| `boolean ativarColuna(String coluna)` | PainelTurno |
| `boolean escolherPeao(int peao)` | PainelTurno |
| `boolean moverKhan(String provincia)` | PainelTurno |
| `boolean terminarTurno()` | PainelTurno |

### Leitura do estado (`model.ModelAPI.getInstance()`)

Convenções: paradas e cidades pelo nome do mapa; jogadores e peões por índice;
herdeiros, colunas, cores, tipos e regiões pelo nome da constante em maiúsculas.
Ações recusadas devolvem `false` e não alteram nada.

| Assunto | Métodos |
|---|---|
| Partida | `getHerdeiros()`, `temPartida()`, `isPartidaEncerrada()`, `getRodada()` |
| Mapa | `getParadas()`, `getTipoParada(p)`, `getRegiaoParada(p)`, `getVizinhas(p)`, `getCidades()`, `getParadasDaCidade(c)`, `isCidadeRevelada(c)` |
| Jogadores | `getNumJogadores()`, `getJogadorDaVez()`, `getNomeJogador(i)`, `getCorJogador(i)`, `getHerdeiroJogador(i)`, `getNumPeoes(i)`, `getPosicaoPeao(i, peao)` |
| Turno | `getColunasDisponiveis()`, `getDescricaoColuna(c)`, `getColunaAtivada()`, `getCavalosRestantes()`, `getPeaoAtivo()`, `podeEscolherPeao()` |
| Movimento | `getDestinosPossiveis()`, `podeMoverPara(p)` |
| Khan | `isKhanPendente()`, `getPosicaoKhan()`, `getDestinosDoKhan()` |
| Fim do turno | `podeTerminarTurno()` |

Cores: AZUL, VERDE, VERMELHO, AMARELO e PRETO, atribuídas na ordem dos jogadores.

## Regras de movimento já garantidas pelo Model

1. No turno, o jogador ativa **uma** coluna da cartela; a ficha daquela coluna só
   volta quando ele ativar a coluna branca, que recupera todas para o turno seguinte.
2. Os cavalos da coluna são os movimentos do turno. Cada parada percorrida custa
   um cavalo, e o peão pode parar antes de gastar todos.
3. O peão pode passar por paradas ocupadas, mas só termina numa parada se houver
   espaço: na simples, só se não houver peão de outro jogador; na dupla, cabem 2
   peões; em Karakorum, cabem todos.
4. Yurts próprios no caminho são pulados sem gastar cavalo.
5. Altani tem 2 peões, mas só um age por turno. A troca é permitida até a primeira ação.
6. Se a coluna tiver o ícone do Khan, ele precisa ir para uma das 4 províncias do
   Khan, diferente da atual, antes de terminar o turno.
7. `moverPara` escolhe o caminho mais barato até a parada clicada. Por isso a
   View só precisa enviar o destino, não o caminho.

## Ordem de integração

1. Commit da Parte 1 (este).
2. Partes 2 e 3 em paralelo, cada uma nos seus arquivos.
3. Quem terminar por último junta tudo, roda `TodosOsTestes` e abre o jogo pelo `controller.Main`.
4. Gerar o ZIP da entrega com o README atualizado pela Parte 3.
