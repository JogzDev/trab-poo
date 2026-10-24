# 2ª iteração: tela gráfica com movimentação do peão

Objetivo da entrega: uma janela Swing em que os jogadores ativam uma coluna da
cartela e movem o peão pelo mapa, respeitando as regras de movimento do manual.

A arquitetura segue o MVC e os padrões do cap. 17 (Facade, Singleton e
Observer). As classes do Model continuam com visibilidade de pacote. A View e o
Controller falam com o Model **somente** pela fachada pública `model.ModelAPI`.

## Regras do enunciado que valem para as três partes

- **Nada de expressão lambda no tratamento de eventos** de componentes gráficos
  (perde 2,0 na nota). Usar classes que implementam `ActionListener`,
  `MouseListener` ou estendem `MouseAdapter`, como no cap. 12. Exemplo no
  `PainelTurno`: a classe interna `AcaoColuna` recebe o nome da coluna no construtor.
- **Todo deslocamento é feito clicando no mapa**, sem botões auxiliares: mover o
  peão, mover o Khan e escolher o peão da Altani.
- **Clique recusado não faz nada**: não mostra mensagem e não troca o jogador da vez.
- **Evitar caixas de diálogo** (`JOptionPane`): troca de turno e fim da partida
  aparecem no `PainelTurno`.
- **Tabuleiro e peões com `drawImage()`** do `Graphics2D`, a partir das imagens do EAD.
- **Janela de no máximo 1400 x 1050.**

## Divisão do trabalho

Cada parte cria arquivos próprios, então os commits não entram em conflito.

| Parte | Responsável | Arquivos | Depende de |
|---|---|---|---|
| 1. Fachada, Observer e painel do turno | @JogzDev | `model/ModelAPI`, `model/ObservadorIF`, `model/ObservadoIF`, esqueleto de `controller/Controlador`, `view/PainelTurno`, `view/CoresJogador`, `test/model/ModelAPITest` | nada (já entregue) |
| 2. Tabuleiro gráfico | @phmucelin | `view/PainelTabuleiro`, `view/CoordenadasMapa` | só do contrato abaixo |
| 3. Janelas e fluxo do jogo | a definir | `controller/Main`, `view/JanelaInicial`, `view/JanelaJogo`, corpo dos métodos de `Controlador`, README da 2ª iteração | só do contrato abaixo |

A Parte 1 já está pronta e testada (159 testes, zero falhas). Depois que ela
for commitada, a Parte 2 e a Parte 3 podem trabalhar ao mesmo tempo, porque uma
não usa nada da outra além de um construtor (`new PainelTabuleiro()`).

### Parte 1: o que já está pronto

- `ModelAPI`, `ObservadorIF` e `ObservadoIF`: descritos no contrato abaixo.
- `Controlador`: Singleton com as assinaturas combinadas, repassando para a fachada.
- `view.PainelTurno extends JPanel implements ObservadorIF` (largura `PainelTurno.LARGURA`):
  jogador da vez, herdeiro e rodada; um botão por coluna com as ações impressas na
  cartela, habilitado só se a ficha estiver livre e nenhuma coluna tiver sido
  ativada; cavalos restantes; peão ativo da Altani; onde está o Khan e, quando ele
  está pendente, a dica "clique num local do Khan no mapa"; botão "Terminar turno"
  habilitado com `podeTerminarTurno()`; dica do próximo passo e "Partida encerrada"
  no fim. Os botões usam classes internas (`AcaoColuna`, `AcaoTerminarTurno`).
- `view.CoresJogador`: converte os nomes de cor de jogador e de coluna em `java.awt.Color`.
  A Parte 2 deve usar essa classe para pintar os peões, assim as cores batem nos dois painéis.

### Parte 2: tabuleiro gráfico (`view.PainelTabuleiro`)

- `public class PainelTabuleiro extends JPanel implements ObservadorIF`, com
  construtor público sem parâmetros que chama `Controlador.getInstance().registra(this)`.
- Sobrescrever `getPreferredSize()` e `paintComponent(Graphics g)`. Com o
  `PainelTurno` ao lado (260 px), o tabuleiro tem no máximo 1140 x 1050.
- Desenhar o tabuleiro e os peões com `drawImage()`, a partir das imagens do EAD
  (o `tabuleiro.jpg` do zip). `CoresJogador` serve para os destaques: peão ativo,
  destinos possíveis e locais do Khan.
- `CoordenadasMapa`: um `Map<String, Point>` com a posição, sobre a imagem, de
  cada parada (`ModelAPI.getParadas()`) e de cada local do Khan
  (`getLocaisDoKhan()`: as províncias do Khan e `ModelAPI.AREA_MELHORIAS`).
- O `MapaOficial` atual não bate com o `tabuleiro.jpg`; ele é corrigido nesta parte,
  junto com os testes do `ModelAPITest` que dependem de nomes do mapa.
- Destacar os destinos possíveis (`getDestinosPossiveis`), o peão ativo
  (`getPeaoAtivo`) e, quando `isKhanPendente()`, os destinos do Khan (`getDestinosDoKhan`).
- Um `MouseListener` (classe nomeada, sem lambda) trata o clique:
  - num local do Khan, com o Khan pendente: `Controlador.getInstance().moverKhan(local)`;
  - no outro peão da Altani, enquanto `podeEscolherPeao()`: `Controlador.getInstance().escolherPeao(i)`;
  - numa parada: `Controlador.getInstance().paradaClicada(nome)`;
  - se a ação for recusada, não fazer nada.
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
  mostrar o motivo num `JLabel` da própria janela (sem `JOptionPane`) e não fechar;
  se devolver `true`, abrir a `JanelaJogo`.
- `view.JanelaJogo extends JFrame`: `BorderLayout`, com `new PainelTabuleiro()` no
  `CENTER` e `new PainelTurno()` no `LINE_END`, no máximo 1400 x 1050; título com o
  nome do jogo e encerramento do programa ao fechar.
- `Controlador`: manter as assinaturas e completar o fluxo. Clique recusado é
  ignorado sem mensagem; troca de turno e fim da partida já aparecem no
  `PainelTurno`, então nada de caixas de diálogo.
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
| `boolean paradaClicada(String parada)` | PainelTabuleiro (clique numa parada) |
| `boolean moverKhan(String local)` | PainelTabuleiro (clique num local do Khan) |
| `boolean escolherPeao(int peao)` | PainelTabuleiro (clique no outro peão da Altani) |
| `boolean ativarColuna(String coluna)` | PainelTurno |
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
| Khan | `isKhanPendente()`, `getPosicaoKhan()`, `getLocaisDoKhan()`, `getDestinosDoKhan()`, constante `ModelAPI.AREA_MELHORIAS` |
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
   Khan ou para a área de melhorias (manual, p. 9), sem ficar onde está, antes de
   terminar o turno. Nesta iteração as melhorias ainda não aparecem na tela, então
   ir para a área de melhorias não repõe peças.
7. `moverPara` escolhe o caminho mais barato até a parada clicada. Por isso a
   View só precisa enviar o destino, não o caminho.

## Ordem de integração

1. Commit da Parte 1 (este).
2. Partes 2 e 3 em paralelo, cada uma nos seus arquivos.
3. Quem terminar por último junta tudo, roda `TodosOsTestes` e abre o jogo pelo `controller.Main`.
4. Gerar o ZIP da entrega com o README atualizado pela Parte 3.
