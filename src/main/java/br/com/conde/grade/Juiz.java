package br.com.conde.grade;

import java.util.Arrays;

/**
 * O JUIZ: a forca bruta.
 *
 * <p>Ela e o juiz certo e nao so o conveniente. Nao ha nela celula a escolher, lista ativa
 * a manter nem par repetido a evitar: ela faz as n(n-1)/2 perguntas possiveis e anota as
 * que deram sim.
 *
 * <h2>O que ele compara</h2>
 *
 * <p>O CONJUNTO de pares, e nao a lista. A grade produz na ordem das celulas e a varredura
 * na ordem do eixo, e as duas estao certas; e a grade sem a regra do canto produz o mesmo
 * par varias vezes, o que tambem nao e erro, e desperdicio.
 *
 * <p>Comparar a ordem ou a contagem bruta reprovaria metodos corretos por um motivo que nao
 * tem nada a ver com colisao.
 */
public final class Juiz {

    private Juiz() {
    }

    public record Veredito(int pares, int divergencias, String falha) {

        public boolean passou() {
            return divergencias == 0;
        }
    }

    public static Veredito julgar(FaseAmpla metodo, Corpo[] corpos) {
        return julgar(metodo, corpos, esperado(corpos));
    }

    /** Os pares certos, calculados uma vez so para serem reusados em varias conferencias. */
    public static long[] esperado(Corpo[] corpos) {
        return new ForcaBruta().pares(corpos, new Contador()).distintos();
    }

    public static Veredito julgar(FaseAmpla metodo, Corpo[] corpos, long[] certo) {
        long[] dele = metodo.pares(corpos, new Contador()).distintos();

        if (Arrays.equals(certo, dele)) return new Veredito(certo.length, 0, null);

        String falha = dele.length != certo.length
                ? "achou " + dele.length + " pares, sao " + certo.length
                : "mesmo numero de pares, pares diferentes";

        return new Veredito(certo.length, 1, falha);
    }

    /**
     * Confere que um metodo nao deixa par de fora NEM inventa par, explicando qual dos dois.
     *
     * <p>As duas falhas sao bem diferentes. Deixar par de fora e o erro de uma grade que
     * esqueceu uma celula; inventar par e o erro de uma fase estreita errada. Um teste que
     * so compare tamanhos nao distingue os dois, e so um deles tem a ver com a fase ampla.
     */
    public static String diferenca(FaseAmpla metodo, Corpo[] corpos) {
        long[] certo = new ForcaBruta().pares(corpos, new Contador()).distintos();
        long[] dele = metodo.pares(corpos, new Contador()).distintos();

        int faltando = 0;
        int sobrando = 0;
        int i = 0;
        int j = 0;

        while (i < certo.length && j < dele.length) {
            if (certo[i] == dele[j]) {
                i++;
                j++;
            } else if (certo[i] < dele[j]) {
                faltando++;
                i++;
            } else {
                sobrando++;
                j++;
            }
        }

        faltando += certo.length - i;
        sobrando += dele.length - j;

        return faltando + " faltando, " + sobrando + " sobrando";
    }
}
