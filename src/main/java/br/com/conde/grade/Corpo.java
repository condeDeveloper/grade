package br.com.conde.grade;

import java.util.Arrays;

/**
 * UM CORPO: um circulo com centro e raio, em coordenadas INTEIRAS.
 *
 * <p>Inteiras de proposito. A pergunta "estes dois se tocam?" vira
 * {@code dx*dx + dy*dy <= (r1+r2)*(r1+r2)}, que e exata num long. Com ponto flutuante, dois
 * corpos exatamente encostados poderiam dar respostas diferentes por um bit conforme a
 * ordem das contas, e o juiz acusaria divergencia onde nao ha nenhuma.
 *
 * <p>Circulo e nao retangulo porque e o caso em que a fase ampla faz diferenca de verdade:
 * com retangulos alinhados aos eixos, a caixa envolvente E o corpo, e a fase estreita some.
 */
public record Corpo(int x, int y, int raio) {

    public long distanciaAoQuadrado(Corpo outro) {
        long dx = (long) x - outro.x;
        long dy = (long) y - outro.y;

        return dx * dx + dy * dy;
    }

    /** Se os dois se tocam de verdade. E esta a pergunta que a fase ampla evita fazer. */
    public boolean toca(Corpo outro) {
        long soma = (long) raio + outro.raio;

        return distanciaAoQuadrado(outro) <= soma * soma;
    }

    public int esquerda() {
        return x - raio;
    }

    public int direita() {
        return x + raio;
    }

    public int baixo() {
        return y - raio;
    }

    public int cima() {
        return y + raio;
    }

    /**
     * Uma lista de pares que cresce sozinha.
     *
     * <p>Cada par vira um long com o menor indice em cima e o maior embaixo. Isso nao e
     * economia de memoria: e o que torna a comparacao entre dois resultados uma ordenacao
     * de vetor e nada mais, e e o que garante que o par (3,7) e o par (7,3) sao o mesmo.
     */
    public static final class Pares {

        private long[] pares = new long[64];
        private int quantos;

        public void achou(int a, int b) {
            if (quantos == pares.length) pares = Arrays.copyOf(pares, pares.length * 2);

            int menor = Math.min(a, b);
            int maior = Math.max(a, b);

            pares[quantos++] = ((long) menor << 32) | maior;
        }

        public int quantos() {
            return quantos;
        }

        public long[] ordenado() {
            long[] copia = Arrays.copyOf(pares, quantos);

            Arrays.sort(copia);

            return copia;
        }

        /**
         * Os pares em ordem, cada um uma vez so.
         *
         * <p>A versao da grade que NAO evita repeticao testa o mesmo par em cada celula que
         * os dois corpos dividem, e portanto anota o mesmo par varias vezes. Ela esta certa
         * sobre o CONJUNTO de pares e gastou trabalho a mais para chegar nele, e e assim
         * que o juiz precisa ver: o conjunto igual, a conta diferente.
         */
        public long[] distintos() {
            long[] copia = ordenado();
            int limpos = 0;

            for (int i = 0; i < copia.length; i++) {
                if (i > 0 && copia[i] == copia[i - 1]) continue;

                copia[limpos++] = copia[i];
            }

            return Arrays.copyOf(copia, limpos);
        }

        public int primeiroDe(int i) {
            return (int) (pares[i] >>> 32);
        }

        public int segundoDe(int i) {
            return (int) pares[i];
        }
    }
}
