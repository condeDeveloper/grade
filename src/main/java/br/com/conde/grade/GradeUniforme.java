package br.com.conde.grade;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A GRADE UNIFORME: o espaco cortado em celulas quadradas iguais.
 *
 * <p>Cada corpo e posto em TODAS as celulas que a caixa envolvente dele cobre, e depois os
 * pares sao testados dentro de cada celula. Isso e completo: dois circulos que se tocam tem
 * caixas que se cruzam, logo faixas de celulas que se cruzam, logo pelo menos uma celula em
 * comum. Nenhum par escapa, e nenhuma celula vizinha precisa ser consultada.
 *
 * <p>A grade e guardada num mapa e nao num vetor, de proposito: assim o espaco e ILIMITADO
 * e so as celulas ocupadas existem. Um vetor exigiria conhecer as bordas do mundo de
 * antemao, e o custo dele cresceria com a area e nao com os corpos.
 *
 * <h2>O par repetido</h2>
 *
 * <p>Dois corpos grandes dividem varias celulas, e o par deles aparece em cada uma. Testar
 * em todas esta CERTO e e desperdicio: a resposta e a mesma.
 *
 * <p>A regra que evita isso nao precisa de memoria nenhuma. As faixas de celulas dos dois
 * corpos se cruzam num retangulo, e o par e testado so na celula do CANTO desse retangulo.
 * Como as duas faixas sao conhecidas na hora, esse canto se calcula em quatro contas.
 */
public final class GradeUniforme implements FaseAmpla {

    /** Uma lista de indices que cresce sozinha. */
    private static final class Lista {

        private int[] indices = new int[4];
        private int quantos;

        void por(int indice) {
            if (quantos == indices.length) indices = Arrays.copyOf(indices, indices.length * 2);

            indices[quantos++] = indice;
        }
    }

    /**
     * O teto de celulas que um unico corpo pode cobrir.
     *
     * <p>ELE EXISTE PORQUE A GRADE TEM UM LIMITE DE VERDADE, e e melhor que ele seja uma
     * recusa do que um estouro de memoria. Um corpo de raio vinte mil numa grade de celula
     * um cobre um bilhao e seiscentos milhoes de celulas: nao e lentidao, e impossivel.
     *
     * <p>Essa e a mesma fraqueza que a tabela dos tamanhos mede em escala menor. Ali ela
     * aparece como um numero ruim; aqui, levada ao extremo, ela aparece como uma parede.
     */
    public static final int MAXIMO_DE_CELULAS_POR_CORPO = 1 << 16;

    /**
     * O teto de ocupacoes de celula somando todos os corpos.
     *
     * <p>O teto por corpo sozinho nao basta: mil corpos cabendo cada um no limite dariam
     * um bilhao de ocupacoes. Os dois juntos e que fecham a porta.
     */
    public static final long MAXIMO_DE_OCUPACOES = 1L << 24;

    private final int celula;
    private final boolean evitarRepetidos;

    public GradeUniforme(int celula, boolean evitarRepetidos) {
        if (celula < 1) throw new IllegalArgumentException("celula de pelo menos um");

        this.celula = celula;
        this.evitarRepetidos = evitarRepetidos;
    }

    public int celula() {
        return celula;
    }

    @Override
    public String nome() {
        return "grade " + celula + (evitarRepetidos ? "" : " com repetidos");
    }

    @Override
    public Corpo.Pares pares(Corpo[] corpos, Contador contador) {
        contador.consulta();

        Map<Long, Lista> grade = new HashMap<>();

        int[] deX = new int[corpos.length];
        int[] ateX = new int[corpos.length];
        int[] deY = new int[corpos.length];
        int[] ateY = new int[corpos.length];
        long ocupacoes = 0;

        for (int i = 0; i < corpos.length; i++) {
            Corpo corpo = corpos[i];

            deX[i] = Math.floorDiv(corpo.esquerda(), celula);
            ateX[i] = Math.floorDiv(corpo.direita(), celula);
            deY[i] = Math.floorDiv(corpo.baixo(), celula);
            ateY[i] = Math.floorDiv(corpo.cima(), celula);

            long quantasCelulas = (ateX[i] - deX[i] + 1L) * (ateY[i] - deY[i] + 1L);

            if (quantasCelulas > MAXIMO_DE_CELULAS_POR_CORPO)
                throw new IllegalArgumentException(
                        "o corpo de raio " + corpo.raio() + " cobre " + quantasCelulas
                                + " celulas de lado " + celula + ", o teto e "
                                + MAXIMO_DE_CELULAS_POR_CORPO);

            ocupacoes += quantasCelulas;

            if (ocupacoes > MAXIMO_DE_OCUPACOES)
                throw new IllegalArgumentException(
                        "os corpos cobririam mais de " + MAXIMO_DE_OCUPACOES
                                + " celulas de lado " + celula + " somando tudo");

            for (int cx = deX[i]; cx <= ateX[i]; cx++)
                for (int cy = deY[i]; cy <= ateY[i]; cy++) {
                    contador.tocaCelula();

                    grade.computeIfAbsent(chave(cx, cy), c -> new Lista()).por(i);
                }
        }

        Corpo.Pares pares = new Corpo.Pares();

        for (var entrada : grade.entrySet()) {
            contador.tocaCelula();

            Lista lista = entrada.getValue();
            int cx = (int) (entrada.getKey() >> 32);
            int cy = (int) (long) entrada.getKey();

            for (int a = 0; a < lista.quantos; a++) {
                contador.veCorpo();

                for (int b = a + 1; b < lista.quantos; b++) {
                    int i = lista.indices[a];
                    int j = lista.indices[b];

                    /*
                     * O CANTO DO RETANGULO DE CELULAS COMPARTILHADAS. As faixas dos dois
                     * corpos se cruzam a partir daqui, e o par so e testado nesta celula.
                     */
                    boolean noCanto = cx == Math.max(deX[i], deX[j])
                            && cy == Math.max(deY[i], deY[j]);

                    contador.compara();

                    if (evitarRepetidos && !noCanto) continue;

                    if (!noCanto) contador.parRepetido();

                    contador.testaPar();

                    if (corpos[i].toca(corpos[j])) pares.achou(i, j);
                }
            }
        }

        return pares;
    }

    private static long chave(int cx, int cy) {
        return ((long) cx << 32) | (cy & 0xFFFFFFFFL);
    }
}
