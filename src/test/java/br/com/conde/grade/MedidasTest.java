package br.com.conde.grade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Os numeros do README, afirmados.
 *
 * <p>Um README e texto: ele nao quebra quando deixa de ser verdade. Cada afirmacao das
 * tabelas esta repetida aqui para quebrar.
 */
class MedidasTest {

    /* As tabelas sao calculadas uma vez so: cada uma roda a forca bruta em milhares de corpos. */
    private static final List<Medidas.LinhaCelula> CELULA = Medidas.aCelula();
    private static final List<Medidas.LinhaDensidade> DENSIDADE = Medidas.aDensidade();
    private static final List<Medidas.LinhaTamanhos> TAMANHOS = Medidas.osTamanhos();
    private static final List<Medidas.LinhaRepetidos> REPETIDOS = Medidas.osRepetidos();

    // ───────────────────── 1. a correcao ─────────────────────

    @Test
    @DisplayName("nenhuma combinacao diverge da forca bruta")
    void aCorrecaoNaoDiverge() {
        List<Medidas.LinhaCorrecao> linhas = Medidas.aCorrecao();

        assertThat(linhas).hasSize(16);
        assertThat(linhas).allSatisfy(l ->
                assertThat(l.divergencias()).as(l.metodo() + " em " + l.mundo()).isZero());

        assertThat(pares(linhas, "uniforme")).isEqualTo(42);
        assertThat(pares(linhas, "em grupos")).isEqualTo(9_911);
        assertThat(pares(linhas, "em coluna")).isEqualTo(2_420);
        assertThat(pares(linhas, "misturado")).isEqualTo(493);
    }

    private static int pares(List<Medidas.LinhaCorrecao> linhas, String mundo) {
        List<Integer> todos = linhas.stream()
                .filter(l -> l.mundo().equals(mundo))
                .map(Medidas.LinhaCorrecao::pares)
                .distinct()
                .toList();

        assertThat(todos).as("os quatro metodos concordam").hasSize(1);

        return todos.get(0);
    }

    // ───────────────────── 2. o tamanho da celula ─────────────────────

    /**
     * POR QUE O CONTADOR TEM DUAS COLUNAS.
     *
     * <p>Os pares testados caem sem parar conforme a celula diminui, e as celulas tocadas
     * sobem sem parar. Com uma coluna so, a conclusao seria que celula menor e sempre
     * melhor.
     */
    @Test
    @DisplayName("as duas colunas andam em direcoes opostas, sem excecao")
    void asDuasColunasAndamAoContrario() {
        for (int i = 1; i < CELULA.size(); i++) {
            assertThat(CELULA.get(i).paresTestados())
                    .as("celula " + CELULA.get(i).celula() + " testa mais pares")
                    .isGreaterThan(CELULA.get(i - 1).paresTestados());

            assertThat(CELULA.get(i).celulasTocadas())
                    .as("celula " + CELULA.get(i).celula() + " toca menos celulas")
                    .isLessThan(CELULA.get(i - 1).celulasTocadas());
        }

        assertThat(celula(75).paresTestados()).isEqualTo(295);
        assertThat(celula(19_200).paresTestados()).isEqualTo(78_361);
        assertThat(celula(75).celulasTocadas()).isEqualTo(317_533);
        assertThat(celula(19_200).celulasTocadas()).isEqualTo(2_188);
    }

    /**
     * A CORRECAO: o fundo nao esta onde a regra de bolso diz.
     *
     * <p>Eu escrevi que o fundo seria a celula do tamanho do corpo. Ele esta em QUATRO
     * diametros, e a celula de um diametro custa quase o dobro.
     */
    @Test
    @DisplayName("o fundo da curva esta em quatro diametros, e nao em um")
    void oFundoEstaEmQuatroDiametros() {
        assertThat(melhorCelula()).isEqualTo(2_400);
        assertThat(celula(2_400).emDiametros()).isEqualTo(4.0);
        assertThat(celula(2_400).trabalho()).isEqualTo(12_975);

        assertThat(celula(600).trabalho())
                .as("a celula de um diametro custa quase o dobro do fundo")
                .isEqualTo(24_661);

        assertThat(celula(600).trabalho() / (double) celula(2_400).trabalho())
                .isCloseTo(1.90, within(0.01));

        assertThat(celula(75).trabalho())
                .as("e as duas pontas sao muito piores que as duas vizinhas do fundo")
                .isGreaterThan(celula(2_400).trabalho() * 35);

        assertThat(celula(19_200).trabalho()).isGreaterThan(celula(2_400).trabalho() * 12);
    }

    @Test
    @DisplayName("mesmo a pior celula testa menos de 4% dos pares da forca bruta")
    void ateAPiorCelulaGanhaDaForcaBruta() {
        assertThat(CELULA).allSatisfy(l ->
                assertThat(l.fracaoDaForcaBruta()).as("celula " + l.celula()).isLessThan(0.04));

        assertThat(celula(2_400).fracaoDaForcaBruta()).isCloseTo(0.00124, within(0.00001));
    }

    private static int melhorCelula() {
        return CELULA.stream()
                .min((a, b) -> Long.compare(a.trabalho(), b.trabalho()))
                .orElseThrow()
                .celula();
    }

    private static Medidas.LinhaCelula celula(int celula) {
        return CELULA.stream()
                .filter(l -> l.celula() == celula)
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 3. a densidade ─────────────────────

    /**
     * A GRADE NAO FOGE DO QUADRATICO: ela divide a constante.
     *
     * <p>A fracao da forca bruta fica em 0,0003 de quinhentos a oito mil corpos. Fracao
     * constante quer dizer que os pares testados crescem com o quadrado, igual a forca
     * bruta, so que com uma constante tres mil vezes menor.
     */
    @Test
    @DisplayName("a fracao da forca bruta fica constante: a grade e quadratica com constante menor")
    void aGradeNaoFogeDoQuadratico() {
        for (int corpos : new int[] {500, 1_000, 2_000, 4_000, 8_000})
            assertThat(densidade(corpos, "grade").fracaoDaForcaBruta())
                    .as(corpos + " corpos")
                    .isCloseTo(0.00032, within(0.00005));

        assertThat(densidade(8_000, "grade").paresTestados()
                / (double) densidade(4_000, "grade").paresTestados())
                .as("dobrar os corpos quadruplica os pares testados")
                .isCloseTo(4.0, within(0.3));

        assertThat(densidade(8_000, "forca bruta").paresTestados()).isEqualTo(31_996_000);
        assertThat(densidade(8_000, "grade").paresTestados()).isEqualTo(10_296);
    }

    @Test
    @DisplayName("a varredura e poda tambem fica constante, em 1,2%")
    void aVarreduraTambemEQuadratica() {
        for (int corpos : new int[] {1_000, 2_000, 4_000, 8_000})
            assertThat(densidade(corpos, "varredura e poda").fracaoDaForcaBruta())
                    .as(corpos + " corpos")
                    .isCloseTo(0.0120, within(0.0005));

        assertThat(densidade(8_000, "varredura e poda").paresTestados())
                .as("trinta e sete vezes mais pares que a grade")
                .isGreaterThan(densidade(8_000, "grade").paresTestados() * 35);
    }

    @Test
    @DisplayName("os tres metodos acham o mesmo numero de pares em toda densidade")
    void todosAchamOMesmo() {
        for (int corpos : new int[] {500, 1_000, 2_000, 4_000, 8_000}) {
            List<Integer> achados = DENSIDADE.stream()
                    .filter(l -> l.corpos() == corpos)
                    .map(Medidas.LinhaDensidade::paresAchados)
                    .distinct()
                    .toList();

            assertThat(achados).as(corpos + " corpos").hasSize(1);
        }

        assertThat(densidade(8_000, "grade").paresAchados()).isEqualTo(3_648);
    }

    private static Medidas.LinhaDensidade densidade(int corpos, String metodo) {
        return DENSIDADE.stream()
                .filter(l -> l.corpos() == corpos && l.metodo().equals(metodo))
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 4. os tamanhos misturados ─────────────────────

    /**
     * A INVERSAO: com corpos de tamanhos diferentes, a grade perde ate para a forca bruta.
     *
     * <p>A grade tem uma celula so, e o tamanho dela so pode estar certo para um tamanho de
     * corpo.
     */
    @Test
    @DisplayName("com 10% de corpos grandes, a grade custa 2,7 vezes a forca bruta")
    void aGradePerdeComTamanhosMisturados() {
        assertThat(tamanhos("todos iguais", "grade").vezesAForcaBruta())
                .as("com todos iguais ela ganha de 83 vezes")
                .isCloseTo(0.012, within(0.001));

        assertThat(tamanhos("1% grandes", "grade").vezesAForcaBruta())
                .isCloseTo(0.103, within(0.001));

        assertThat(tamanhos("10% grandes", "grade").vezesAForcaBruta())
                .as("e com 10% grandes ela PERDE para a forca bruta")
                .isCloseTo(2.702, within(0.001));

        assertThat(tamanhos("10% grandes", "grade").celulasTocadas())
                .as("cada corpo grande entra em milhares de celulas")
                .isEqualTo(571_385);
    }

    @Test
    @DisplayName("a varredura e poda nao tem celula, e por isso nao tem celula errada")
    void aVarreduraAguentaOsTamanhos() {
        assertThat(tamanhos("todos iguais", "varredura e poda").vezesAForcaBruta())
                .as("com todos iguais ela perde da grade")
                .isGreaterThan(tamanhos("todos iguais", "grade").vezesAForcaBruta());

        assertThat(tamanhos("1% grandes", "varredura e poda").vezesAForcaBruta())
                .as("com 1% grandes a ordem ja virou")
                .isLessThan(tamanhos("1% grandes", "grade").vezesAForcaBruta());

        assertThat(tamanhos("10% grandes", "varredura e poda").vezesAForcaBruta())
                .isCloseTo(0.211, within(0.001));

        assertThat(tamanhos("10% grandes", "grade").vezesAForcaBruta()
                / tamanhos("10% grandes", "varredura e poda").vezesAForcaBruta())
                .as("com 10% grandes ela ganha da grade por treze vezes")
                .isGreaterThan(12.0);

        assertThat(TAMANHOS).filteredOn(l -> l.metodo().equals("varredura e poda"))
                .allSatisfy(l -> assertThat(l.celulasTocadas()).isZero());
    }

    private static Medidas.LinhaTamanhos tamanhos(String mundo, String metodo) {
        return TAMANHOS.stream()
                .filter(l -> l.mundo().equals(mundo) && l.metodo().equals(metodo))
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 5. os pares repetidos ─────────────────────

    @Test
    @DisplayName("com celula pequena, 95% dos testes sem a regra sao repeticao")
    void aRepeticaoDominaComCelulaPequena() {
        assertThat(repetidos(75).fracaoRepetida()).isCloseTo(0.9555, within(0.0001));
        assertThat(repetidos(75).semARegra()).isEqualTo(6_624);
        assertThat(repetidos(75).comARegra()).isEqualTo(295);

        assertThat(repetidos(75).semARegra() / (double) repetidos(75).comARegra())
                .as("vinte e duas vezes mais trabalho pela mesma resposta")
                .isGreaterThan(22.0);
    }

    @Test
    @DisplayName("com celula grande ninguem divide celula e a regra nao economiza quase nada")
    void aRegraSoValeComCelulaPequena() {
        assertThat(repetidos(4_800).fracaoRepetida()).isCloseTo(0.0259, within(0.0001));

        for (int i = 1; i < REPETIDOS.size(); i++)
            assertThat(REPETIDOS.get(i).fracaoRepetida())
                    .as("celula " + REPETIDOS.get(i).celula() + ": celula maior, menos repeticao")
                    .isLessThan(REPETIDOS.get(i - 1).fracaoRepetida());

        assertThat(REPETIDOS).allSatisfy(l ->
                assertThat(l.semARegra())
                        .as("celula " + l.celula() + ": sem a regra nunca testa menos")
                        .isGreaterThanOrEqualTo(l.comARegra()));
    }

    /**
     * E A COLUNA SEM A REGRA TEM UM FUNDO PROPRIO, em outro lugar.
     *
     * <p>Com a regra, menos celula e sempre menos par testado. Sem a regra, celula pequena
     * multiplica a repeticao, e o minimo de pares testados sai do extremo e vai para o meio.
     */
    @Test
    @DisplayName("sem a regra, o minimo de pares testados sai do extremo e vai para o meio")
    void semARegraOMinimoAnda() {
        int comRegra = REPETIDOS.stream()
                .min((a, b) -> Long.compare(a.comARegra(), b.comARegra()))
                .orElseThrow()
                .celula();

        int semRegra = REPETIDOS.stream()
                .min((a, b) -> Long.compare(a.semARegra(), b.semARegra()))
                .orElseThrow()
                .celula();

        assertThat(comRegra).as("com a regra, quanto menor melhor").isEqualTo(75);
        assertThat(semRegra).as("sem a regra, o fundo e um diametro").isEqualTo(600);
    }

    private static Medidas.LinhaRepetidos repetidos(int celula) {
        return REPETIDOS.stream()
                .filter(l -> l.celula() == celula)
                .findFirst()
                .orElseThrow();
    }
}
