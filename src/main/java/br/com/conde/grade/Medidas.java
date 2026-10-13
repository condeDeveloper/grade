package br.com.conde.grade;

import java.util.ArrayList;
import java.util.List;

/**
 * As MEDIDAS, em pares testados e celulas tocadas. Nunca segundos.
 *
 * <p>O PAR TESTADO tem um teto conhecido, que e n(n-1)/2, e e isso que faz dele a medida
 * certa: cada numero ja vem como fracao do unico valor que importa.
 *
 * <p>A CELULA TOCADA existe porque a primeira sozinha mentiria. Diminuir a celula sempre
 * derruba os pares testados; o que ela cobra em troca e por cada corpo em mais celulas, e
 * sem essa segunda coluna a conclusao seria que celula menor e sempre melhor.
 *
 * <p>Toda tabela daqui tem um teste do lado que afirma os mesmos numeros.
 */
public final class Medidas {

    private Medidas() {
    }

    public static final int CORPOS = 2_000;

    /** O raio dos corpos nas tabelas. O diametro, 600, e a unidade da tabela da celula. */
    public static final int RAIO = 300;

    /** A celula de referencia: o diametro de um corpo, que e a escolha que todo mundo faz. */
    public static final int CELULA = 2 * RAIO;

    public static final long SEMENTE = 20_251_012L;

    // ───────────────────── 1. a correcao ─────────────────────

    public record LinhaCorrecao(String metodo, String mundo, int pares, int divergencias) {
    }

    /** TODO METODO ACHA EXATAMENTE O MESMO CONJUNTO DE PARES, em todo mundo. */
    public static List<LinhaCorrecao> aCorrecao() {
        List<LinhaCorrecao> linhas = new ArrayList<>();

        for (var mundo : Mundos.todos(800, RAIO, SEMENTE).entrySet())
            for (var metodo : Mundos.osMetodos(CELULA).entrySet()) {
                Juiz.Veredito veredito = Juiz.julgar(metodo.getValue().get(), mundo.getValue());

                linhas.add(new LinhaCorrecao(metodo.getKey(), mundo.getKey(),
                        veredito.pares(), veredito.divergencias()));
            }

        return linhas;
    }

    // ───────────────────── 2. o tamanho da celula ─────────────────────

    public record LinhaCelula(int celula, double emDiametros, long paresTestados,
                              long celulasTocadas, long trabalho, double fracaoDaForcaBruta) {
    }

    /**
     * O TAMANHO DA CELULA, de um oitavo do diametro a trinta e dois diametros.
     *
     * <p>E a tabela central do repositorio e ela tem um FUNDO. Celula grande demais poe
     * corpos demais juntos e o teste de pares volta a ser quadratico dentro de cada celula.
     * Celula pequena demais poe cada corpo em celulas demais, e a conta vai toda para a
     * insercao.
     *
     * <p>As duas pontas pioram e o meio ganha. Eu escrevi que o meio seria a regra de bolso
     * que todo mundo repete, celula do tamanho do corpo; o fundo medido esta em QUATRO
     * diametros, e a celula de um diametro custa quase o dobro dele.
     *
     * <p>A razao e a densidade. A regra do tamanho do corpo supoe um mundo cheio, em que
     * cada celula tem varios corpos e o teste dentro dela e o que domina. Aqui sao dois mil
     * corpos num mundo de cem mil por cem mil, e com celula de quatro diametros ainda sobra
     * pouco mais de um corpo por celula ocupada: nao ha o que economizar cortando mais, e
     * cada corte a mais so multiplica as celulas.
     *
     * <p>E o fundo tambem depende do PESO que a unidade da a cada conta. Aqui uma celula
     * tocada e um par testado valem um cada, e essa escolha e discutivel: numa implementacao
     * de verdade o par testado e uma distancia e a celula tocada e uma consulta a um mapa,
     * que custa mais. Com a celula pesando mais, o fundo anda para a direita.
     *
     * <p>A coluna da direita e a fracao dos n(n-1)/2 pares que a forca bruta testaria.
     */
    public static List<LinhaCelula> aCelula() {
        List<LinhaCelula> linhas = new ArrayList<>();
        Corpo[] corpos = Mundos.uniforme(CORPOS, RAIO, SEMENTE);
        double teto = ForcaBruta.todosOsPares(CORPOS);

        for (int celula : new int[] {75, 150, 300, 600, 1_200, 2_400, 4_800, 9_600, 19_200}) {
            Contador contador = new Contador();

            new GradeUniforme(celula, true).pares(corpos, contador);

            linhas.add(new LinhaCelula(celula, celula / (2.0 * RAIO), contador.paresTestados(),
                    contador.celulasTocadas(), contador.trabalho(),
                    contador.paresTestados() / teto));
        }

        return linhas;
    }

    // ───────────────────── 3. a densidade ─────────────────────

    public record LinhaDensidade(int corpos, String metodo, long paresTestados, long trabalho,
                                 double fracaoDaForcaBruta, int paresAchados) {
    }

    /**
     * O CUSTO POR DENSIDADE, com a celula de referencia e o mundo do mesmo tamanho.
     *
     * <p>Mais corpos no mesmo espaco e mais corpos por celula, e o teste dentro da celula e
     * quadratico. Entao a grade nao foge do quadratico: ela so divide a constante pelo
     * numero de celulas.
     *
     * <p>E essa e a leitura certa da coluna da fracao: ela nao vai a zero conforme n cresce,
     * ela se aproxima de uma constante que depende da densidade.
     */
    public static List<LinhaDensidade> aDensidade() {
        List<LinhaDensidade> linhas = new ArrayList<>();

        for (int quantos : new int[] {500, 1_000, 2_000, 4_000, 8_000}) {
            Corpo[] corpos = Mundos.uniforme(quantos, RAIO, SEMENTE);
            double teto = ForcaBruta.todosOsPares(quantos);

            for (var metodo : Mundos.osMetodos(CELULA).entrySet()) {
                if (metodo.getKey().equals("grade com repetidos")) continue;

                Contador contador = new Contador();

                Corpo.Pares pares = metodo.getValue().get().pares(corpos, contador);

                linhas.add(new LinhaDensidade(quantos, metodo.getKey(), contador.paresTestados(),
                        contador.trabalho(), contador.paresTestados() / teto,
                        pares.distintos().length));
            }
        }

        return linhas;
    }

    // ───────────────────── 4. os tamanhos misturados ─────────────────────

    public record LinhaTamanhos(String mundo, String metodo, long paresTestados,
                                long celulasTocadas, long trabalho, double vezesAForcaBruta) {
    }

    /**
     * CORPOS DE TAMANHOS DIFERENTES, que e o caso ruim da grade uniforme.
     *
     * <p>A grade tem uma celula so, e o tamanho dela so pode estar certo para um tamanho de
     * corpo. Com corpos cinquenta vezes maiores no mesmo mundo, cada um deles e posto em
     * dez mil celulas e aparece em todas elas.
     *
     * <p>A varredura e poda nao tem esse problema porque nao tem celula: ela se adapta ao
     * tamanho de cada corpo sozinha, e por isso ela e a resposta certa quando os tamanhos
     * variam.
     */
    public static List<LinhaTamanhos> osTamanhos() {
        List<LinhaTamanhos> linhas = new ArrayList<>();

        var mundos = new java.util.LinkedHashMap<String, Corpo[]>();

        mundos.put("todos iguais", Mundos.uniforme(CORPOS, RAIO, SEMENTE));
        mundos.put("1% grandes", Mundos.deTamanhosMisturados(CORPOS, RAIO, RAIO * 50,
                CORPOS / 100, SEMENTE));
        mundos.put("10% grandes", Mundos.deTamanhosMisturados(CORPOS, RAIO, RAIO * 50,
                CORPOS / 10, SEMENTE));

        for (var mundo : mundos.entrySet()) {
            Contador daForcaBruta = new Contador();

            new ForcaBruta().pares(mundo.getValue(), daForcaBruta);

            for (var metodo : Mundos.osMetodos(CELULA).entrySet()) {
                if (metodo.getKey().equals("grade com repetidos")) continue;

                Contador contador = new Contador();

                metodo.getValue().get().pares(mundo.getValue(), contador);

                linhas.add(new LinhaTamanhos(mundo.getKey(), metodo.getKey(),
                        contador.paresTestados(), contador.celulasTocadas(), contador.trabalho(),
                        (double) contador.trabalho() / daForcaBruta.trabalho()));
            }
        }

        return linhas;
    }

    // ───────────────────── 5. os pares repetidos ─────────────────────

    public record LinhaRepetidos(int celula, double emDiametros, long semARegra,
                                 long comARegra, long repetidos, double fracaoRepetida) {
    }

    /**
     * O QUE A REPETICAO DE PAR CUSTA, por tamanho de celula.
     *
     * <p>Dois corpos que dividem varias celulas sao testados em cada uma. A regra do canto
     * evita isso em quatro contas e sem memoria nenhuma.
     *
     * <p>E ela so vale a pena quando os corpos ocupam mais de uma celula. Com celula grande,
     * cada corpo cabe numa so, ninguem divide nada e a regra nao economiza nada: ela e uma
     * comparacao a mais por par, de graca.
     */
    public static List<LinhaRepetidos> osRepetidos() {
        List<LinhaRepetidos> linhas = new ArrayList<>();
        Corpo[] corpos = Mundos.uniforme(CORPOS, RAIO, SEMENTE);

        for (int celula : new int[] {75, 150, 300, 600, 1_200, 4_800}) {
            Contador semRegra = new Contador();
            Contador comRegra = new Contador();

            new GradeUniforme(celula, false).pares(corpos, semRegra);
            new GradeUniforme(celula, true).pares(corpos, comRegra);

            linhas.add(new LinhaRepetidos(celula, celula / (2.0 * RAIO),
                    semRegra.paresTestados(), comRegra.paresTestados(),
                    semRegra.paresRepetidos(),
                    semRegra.paresTestados() == 0 ? 0
                            : (double) semRegra.paresRepetidos() / semRegra.paresTestados()));
        }

        return linhas;
    }
}
