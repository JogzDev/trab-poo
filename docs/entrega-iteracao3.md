# Entrega da 3ª iteração

A implementação dos fluxos está concluída e foi validada com **193 testes**.
O cadastro oficial das peças permanece pendente por decisão registrada durante
a implementação. O pacote contém um cadastro sintético identificado para
experimentar todos os fluxos; ele não deve ser apresentado como oficial.

## Executar

Requer JDK 17 ou superior. No Eclipse, importe a pasta `HerdeirosDeKhan` como
projeto existente e execute `controller.Main` como aplicação Java.

Pelo terminal, na pasta do projeto:

```bash
./scripts/run_game.sh
```

Caso o Java não esteja no ambiente, configure `JAVA_HOME` para um JDK 17+.
Para iniciar com os componentes completos, clique **Selecionar cadastro das
peças…** e abra `config/pecas.demonstracao.txt`. Configure os jogadores e
confirme. Esse cadastro respeita os totais físicos, mas suas cores e pedidos
são sintéticos. Para usar dados oficiais, preencha uma cópia de
`config/pecas.modelo.txt` e selecione o arquivo preenchido.

## Conferência dos requisitos

| Requisito | Implementação e evidência |
|---|---|
| Integração View/Model | Preparação, movimento, construção, coleta, ataques, habilidades, melhorias, entregas, bônus, objetivos e resultado acessíveis pelas telas. |
| Singleton | `ModelAPI.getInstance()` e `Controlador.getInstance()`. |
| Façade | Views e Controller usam `ModelAPI`; as classes de domínio mantêm visibilidade de pacote. |
| Observer com interfaces | `ObservadoIF`, `ObservadorIF` e seus implementadores; comandos aceitos notificam os painéis. Cartas e seleção de cartela também passam pela notificação. |
| Controller organiza a sequência | Abre a partida, recebe os comandos e define prioridade da preparação, escolhas pendentes, jogo e resultado com `getEtapaPartida()`. |
| Restrições herdadas da 2ª iteração | Listeners nomeados, sem lambdas na View; deslocamentos por clique no mapa; imagem com `drawImage`; janela limitada a 1400 × 1050; recusas sem diálogo. |
| Salvamento e recuperação | Reservados à última iteração; não incluídos. |

## Roteiro de revisão gráfica

1. Configure de 2 a 5 jogadores. Passe a tela para cada um revelar e escolher
   sua carta secreta. Os turnos só são liberados após a última escolha.
2. Na aba **Turno**, ative uma coluna. Mova o peão e o Khan no mapa. Com
   Altani, escolha o peão antes de sua primeira ação de posição.
3. Na aba **Ações**, colete tributos, construa yurts e ataque cidades. Confira
   custos, tesouros, estoque, desenhos dos yurts e habilidades especiais.
4. Na aba **Melhorias**, escolha uma peça do mercado ou compre às cegas da
   pilha. Consulte as cartelas e resolva a recompensa para a branca ao
   completar uma linha. Reponha o mercado movendo o Khan no mapa.
5. Na aba **Entregas**, confira o pedido e a visita, escolha quantidades e
   entregue. Ative um bônus obtido. Se ele sortear tesouros, confirme uma das
   opções antes de terminar o turno.
6. Na aba **Objetivos**, confira as cartas abertas e seu progresso. Revele
   sua carta secreta e confira que ela fica oculta ao terminar o turno.
7. Acima do mapa, marque **Traçar rota no mapa**, clique em paradas consecutivas
   e clique novamente na última para executar. A opção de salto é registrada
   para cada parada acrescentada; o destino sempre é visitado.
8. Acompanhe Kurultai e a rodada final. No encerramento, a aba **Resultado**
   abre automaticamente com os vencedores, votos e critérios de desempate.

## Validação automatizada

```bash
./scripts/run_tests.sh
```

A suíte tem 193 testes. Além dos cenários das iterações anteriores, verifica
os contratos da fachada, custos e recusas sem alterações, recompensas,
privacidade das cartas e cliques reais em componentes Swing na EDT.

`PartidaCompletaInterfaceTest` percorre uma partida da preparação até o
encerramento pelas conquistas, usando botões e eventos de mouse no mapa.
Não injeta votos, recursos ou posições no domínio. Os testes anteriores
cobrem o gatilho do Kurultai, rodada final, influência e desempates. O caso
de rota passa por uma parada ocupada e executa pelo clique no destino.

As prévias gráficas foram renderizadas e inspecionadas, incluindo objetivos,
carta secreta, escolhas obrigatórias, rota e resultado. A janela de jogo
foi conferida dentro do limite de 1400 × 1050.

## Gerar o ZIP

```bash
python3 scripts/package_delivery.py
```

Saída: `output/entrega/HerdeirosDeKhan-iteracao3.zip`, contendo uma pasta raiz
`HerdeirosDeKhan` com `.project`, `.classpath`, `.settings`, `src`, `test`,
`lib`, `imagens`, `config`, `docs`, `scripts`, `README.md` e o enunciado.
A integridade do ZIP é conferida pelo script. O pacote não inclui `.git`,
builds nem dados temporários.

Antes de apresentar o pacote como versão fiel ao material físico, substituir
ou complementar o cadastro sintético pelo cadastro oficial conferido. A
configuração oficial foi deixada pendente; ela não é gerada pelo programa.
