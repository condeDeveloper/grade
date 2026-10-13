package br.com.conde.grade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FaseAmplaTest {

    // ───────────────────── o juiz contra todos os metodos ─────────────────────

    @Test
    @DisplayName("todo metodo acha o mesmo conjunto de pares, em todo mundo e toda celula")
    void todosBatemComOJuiz() {
        List<String> falhas = new ArrayList<>();
        int conferidas = 0;

        int recusadas = 0;

        for (int quantos : new int[] {0, 1, 2, 50, 300})
            for (int raio : new int[] {1, 400, 20_000})
                for (var mundo : Mundos.todos(quantos, raio, 19).entrySet()) {
                    long[] certo = Juiz.esperado(mundo.getValue());

                    for (int celula : new int[] {1, 137, 800, 50_000})
                        for (var metodo : Mundos.osMetodos(celula).entrySet()) {
                            Juiz.Veredito veredito;

                            try {
                                veredito = Juiz.julgar(metodo.getValue().get(),
                                        mundo.getValue(), certo);
                            } catch (IllegalArgumentException recusa) {
                                /*
                                 * A GRADE RECUSA quando um corpo cobriria celulas demais, e
                                 * isso nao e falha: e o limite dela, dito em voz alta. Corpo
                                 * de raio vinte mil com celula um cobre um bilhao e meio.
                                 */
                                assertThat(recusa).hasMessageContaining("celulas de lado");
                                recusadas++;
                                continue;
                            }

                            conferidas++;

                            if (!veredito.passou())
                                falhas.add(metodo.getKey() + " em " + mundo.getKey() + ", "
                                        + quantos + " corpos de raio " + raio + ", celula "
                                        + celula + ": " + veredito.falha());
                        }
                }

        assertThat(conferidas).isGreaterThan(400);
        assertThat(recusadas).as("e algumas combinacoes sao absurdas de proposito")
                .isGreaterThan(0);
        assertThat(falhas).isEmpty();
    }

    /**
     * O CASO QUE QUEBRA UMA GRADE ESCRITA COM VIZINHOS EM VEZ DE CAIXA ENVOLVENTE.
     *
     * <p>Um corpo enorme e um corpo minusculo podem se tocar estando a dezenas de celulas de
     * distancia dos CENTROS. Uma grade que consulte so as oito celulas vizinhas do centro
     * perde esse par. Esta aqui, cada corpo e posto em todas as celulas que a caixa dele
     * cobre, e nenhuma vizinha precisa ser consultada.
     */
    @Test
    @DisplayName("um corpo enorme encosta num minusculo a muitas celulas de distancia")
    void oEnormeEOMinusculo() {
        Corpo[] corpos = {
                new Corpo(0, 0, 10_000),
                new Corpo(9_900, 0, 10),
                new Corpo(10_020, 0, 10)};

        for (int celula : new int[] {100, 250, 500}) {
            for (boolean regra : new boolean[] {true, false}) {
                long[] achou = new GradeUniforme(celula, regra)
                        .pares(corpos, new Contador()).distintos();

                assertThat(achou)
                        .as("celula " + celula + ", regra " + regra)
                        .hasSize(1);

                assertThat((int) (achou[0] >>> 32)).isZero();
                assertThat((int) achou[0]).isEqualTo(1);
            }
        }
    }

    @Test
    @DisplayName("dois corpos exatamente encostados contam como tocando")
    void exatamenteEncostados() {
        Corpo[] encostados = {new Corpo(0, 0, 50), new Corpo(100, 0, 50)};
        Corpo[] porUm = {new Corpo(0, 0, 50), new Corpo(101, 0, 50)};

        for (int celula : new int[] {1, 7, 1_000})
            for (var metodo : Mundos.osMetodos(celula).entrySet()) {
                assertThat(metodo.getValue().get().pares(encostados, new Contador()).distintos())
                        .as(metodo.getKey() + " com celula " + celula + ": encostados contam")
                        .hasSize(1);

                assertThat(metodo.getValue().get().pares(porUm, new Contador()).distintos())
                        .as(metodo.getKey() + " com celula " + celula + ": por um nao conta")
                        .isEmpty();
            }
    }

    @Test
    @DisplayName("coordenadas negativas funcionam, inclusive atravessando o zero")
    void coordenadasNegativas() {
        Corpo[] corpos = {
                new Corpo(-5_000, -5_000, 100),
                new Corpo(-5_050, -5_050, 100),
                new Corpo(5_000, 5_000, 100),
                new Corpo(-10, 10, 100)};

        for (int celula : new int[] {1, 64, 7_000})
            for (var metodo : Mundos.osMetodos(celula).entrySet())
                assertThat(Juiz.julgar(metodo.getValue().get(), corpos).divergencias())
                        .as(metodo.getKey() + " com celula " + celula)
                        .isZero();
    }

    @Test
    @DisplayName("todos os corpos no mesmo lugar dao todos os pares")
    void todosNoMesmoLugar() {
        Corpo[] corpos = new Corpo[40];

        java.util.Arrays.fill(corpos, new Corpo(7, 7, 3));

        for (var metodo : Mundos.osMetodos(600).entrySet())
            assertThat(metodo.getValue().get().pares(corpos, new Contador()).distintos())
                    .as(metodo.getKey())
                    .hasSize((int) ForcaBruta.todosOsPares(40));
    }

    @Test
    @DisplayName("celula menor que um nao e celula")
    void celulaInvalida() {
        assertThatThrownBy(() -> new GradeUniforme(0, true))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new GradeUniforme(-5, true))
                .isInstanceOf(IllegalArgumentException.class);

        /*
         * E a grade tem um teto que nao e do construtor e sim do dado: um corpo que cubra
         * celulas demais e recusado na hora de entrar, com o numero no texto do erro.
         */
        assertThatThrownBy(() -> new GradeUniforme(1, true)
                .pares(new Corpo[] {new Corpo(0, 0, 20_000)}, new Contador()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1600080001");

        assertThat(GradeUniforme.MAXIMO_DE_CELULAS_POR_CORPO).isEqualTo(65_536);
    }

    // ───────────────────── o que cada metodo promete sobre si ─────────────────────

    @Test
    @DisplayName("a forca bruta testa exatamente n vezes n menos um sobre dois, sempre")
    void aForcaBrutaNaoTemCasoBom() {
        for (var mundo : Mundos.todos(300, 400, 3).values()) {
            Contador contador = new Contador();

            new ForcaBruta().pares(mundo, contador);

            assertThat(contador.paresTestados()).isEqualTo(ForcaBruta.todosOsPares(300));
        }

        assertThat(ForcaBruta.todosOsPares(2_000)).isEqualTo(1_999_000);
    }

    /**
     * AS DUAS COLUNAS ANDAM EM DIRECOES OPOSTAS, e e por isso que o contador tem as duas.
     *
     * <p>Com uma coluna so, a conclusao seria que celula menor e sempre melhor.
     */
    @Test
    @DisplayName("celula menor derruba os pares e levanta as celulas, sem excecao")
    void asDuasColunasAndamAoContrario() {
        Corpo[] corpos = Mundos.uniforme(1_500, 300, 11);
        long paresAnterior = Long.MAX_VALUE;
        long celulasAnterior = 0;

        for (int celula : new int[] {150, 300, 600, 1_200, 2_400, 4_800}) {
            Contador contador = new Contador();

            new GradeUniforme(celula, true).pares(corpos, contador);

            assertThat(contador.paresTestados())
                    .as("celula " + celula + ": celula maior testa mais pares")
                    .isGreaterThan(paresAnterior == Long.MAX_VALUE ? -1 : paresAnterior);

            assertThat(contador.celulasTocadas()).as("celula " + celula).isGreaterThan(0L);

            if (celulasAnterior > 0)
                assertThat(contador.celulasTocadas())
                        .as("celula " + celula + ": celula maior toca menos celulas")
                        .isLessThan(celulasAnterior);

            paresAnterior = contador.paresTestados();
            celulasAnterior = contador.celulasTocadas();
        }
    }

    /**
     * A REGRA DO CANTO NAO MUDA A RESPOSTA, so o trabalho.
     *
     * <p>Essa e a propriedade que faz dela uma otimizacao e nao uma heuristica, e ela
     * precisa de um teste e nao de uma frase.
     */
    @Test
    @DisplayName("a regra do canto devolve exatamente o mesmo conjunto e testa menos")
    void aRegraDoCantoNaoMudaAResposta() {
        Corpo[] corpos = Mundos.uniforme(800, 500, 13);

        for (int celula : new int[] {50, 125, 500, 2_000}) {
            Contador comRegra = new Contador();
            Contador semRegra = new Contador();

            long[] com = new GradeUniforme(celula, true).pares(corpos, comRegra).distintos();
            long[] sem = new GradeUniforme(celula, false).pares(corpos, semRegra).distintos();

            assertThat(com).as("celula " + celula).isEqualTo(sem);

            assertThat(comRegra.paresTestados())
                    .as("celula " + celula + ": e testa menos")
                    .isLessThanOrEqualTo(semRegra.paresTestados());

            assertThat(semRegra.paresRepetidos())
                    .as("celula " + celula + ": o que ela evitou")
                    .isEqualTo(semRegra.paresTestados() - comRegra.paresTestados());
        }
    }

    @Test
    @DisplayName("com celula grande ninguem divide celula e a regra nao economiza nada")
    void comCelulaGrandeARegraNaoEconomiza() {
        Corpo[] corpos = Mundos.uniforme(500, 10, 17);

        Contador comRegra = new Contador();
        Contador semRegra = new Contador();

        new GradeUniforme(100_000, true).pares(corpos, comRegra);
        new GradeUniforme(100_000, false).pares(corpos, semRegra);

        assertThat(semRegra.paresRepetidos()).isZero();
        assertThat(comRegra.paresTestados()).isEqualTo(semRegra.paresTestados());
    }

    @Test
    @DisplayName("a varredura e poda nao tem parametro, e por isso nao tem celula errada")
    void aVarreduraNaoTemParametro() {
        Corpo[] misturado = Mundos.deTamanhosMisturados(1_000, 100, 20_000, 100, 5);

        Contador daVarredura = new Contador();
        Contador daGrade = new Contador();

        new VarreduraEPoda().pares(misturado, daVarredura);
        new GradeUniforme(200, true).pares(misturado, daGrade);

        assertThat(daVarredura.trabalho())
                .as("com tamanhos misturados ela ganha da grade com folga")
                .isLessThan(daGrade.trabalho() / 10);

        assertThat(daVarredura.celulasTocadas()).isZero();
    }

    @Test
    @DisplayName("o caso ruim da varredura e poda e a coluna, e ela nao foge dele")
    void oCasoRuimDaVarredura() {
        Corpo[] emColuna = Mundos.emColuna(1_000, 300, 10, 5);
        Corpo[] espalhado = Mundos.uniforme(1_000, 300, 5);

        Contador daColuna = new Contador();
        Contador doEspalhado = new Contador();

        new VarreduraEPoda().pares(emColuna, daColuna);
        new VarreduraEPoda().pares(espalhado, doEspalhado);

        assertThat(daColuna.paresTestados())
                .as("empilhados no mesmo x, a lista ativa guarda quase todo mundo")
                .isGreaterThan(ForcaBruta.todosOsPares(1_000) / 2);

        assertThat(doEspalhado.paresTestados()).isLessThan(daColuna.paresTestados() / 50);
    }

    // ───────────────────── o sorteio da casa ─────────────────────

    /**
     * O TESTE DO DESLOCAMENTO DE 32.
     *
     * <p>A celula de um corpo e a coordenada dividida pelo lado da celula, e com lado
     * potencia de dois isso e um deslocamento. Coordenadas com bits baixos ruins cairiam
     * numa rede regular e a grade mediria a rede.
     */
    @Test
    @DisplayName("o deslocamento de 32 e o que espalha os corpos pelas celulas")
    void oDeslocamentoDeTrintaEDois() {
        Mundos.Sorteio sorteio = new Mundos.Sorteio(1);
        int[] porCelula = new int[64];

        for (int i = 0; i < 64_000; i++) porCelula[sorteio.ate(Mundos.LADO) & 63]++;

        for (int celula = 0; celula < 64; celula++)
            assertThat(porCelula[celula]).as("celula " + celula).isBetween(850, 1_150);

        long estado = 1;
        int alternancias = 0;
        int anterior = -1;

        for (int i = 0; i < 1_000; i++) {
            estado = estado * 6364136223846793005L + 1442695040888963407L;

            int baixo = (int) (estado & 1);

            if (anterior >= 0 && baixo != anterior) alternancias++;

            anterior = baixo;
        }

        assertThat(alternancias)
                .as("o bit baixo do estado cru alterna em TODAS, que e uma rede regular")
                .isEqualTo(999);
    }

    @Test
    @DisplayName("os mundos sao o que dizem ser")
    void osMundosSaoOQueDizem() {
        Corpo[] coluna = Mundos.emColuna(500, 100, 50, 1);
        int menor = Integer.MAX_VALUE;
        int maior = Integer.MIN_VALUE;

        for (Corpo corpo : coluna) {
            menor = Math.min(menor, corpo.x());
            maior = Math.max(maior, corpo.x());
        }

        assertThat(maior - menor).as("em coluna cabe numa faixa estreita de x").isLessThan(50);

        Corpo[] misturado = Mundos.deTamanhosMisturados(1_000, 10, 500, 10, 1);
        int grandes = 0;

        for (Corpo corpo : misturado) if (corpo.raio() == 500) grandes++;

        assertThat(grandes).isEqualTo(10);

        assertThat(new Corpo(3, 4, 7).distanciaAoQuadrado(new Corpo(0, 0, 1))).isEqualTo(25);
        assertThat(new Corpo(0, 0, 3).toca(new Corpo(5, 0, 2))).isTrue();
        assertThat(new Corpo(0, 0, 3).toca(new Corpo(6, 0, 2))).isFalse();
    }
}
