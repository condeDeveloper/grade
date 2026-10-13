package br.com.conde.grade;

/**
 * A FORCA BRUTA: pergunta de todos os pares.
 *
 * <p>Ela e o juiz deste repositorio e tambem um dos metodos medidos, e as duas coisas pelo
 * mesmo motivo: nao ha o que errar nela. Nao ha celula a escolher, lista ativa a manter nem
 * par repetido a evitar. Ela faz as n(n-1)/2 perguntas possiveis e anota as que deram sim.
 *
 * <p>O custo dela e conhecido e constante, e e essa constancia que faz dela a regua: toda
 * coluna deste repositorio e lida contra o numero que ela imprime.
 *
 * <p>E ela nao e uma versao de palha. Com corpos de tamanhos muito diferentes ela VENCE a
 * grade uniforme, e a tabela dos tamanhos mostra onde.
 */
public final class ForcaBruta implements FaseAmpla {

    @Override
    public String nome() {
        return "forca bruta";
    }

    @Override
    public Corpo.Pares pares(Corpo[] corpos, Contador contador) {
        contador.consulta();

        Corpo.Pares pares = new Corpo.Pares();

        for (int i = 0; i < corpos.length; i++)
            for (int j = i + 1; j < corpos.length; j++) {
                contador.testaPar();

                if (corpos[i].toca(corpos[j])) pares.achou(i, j);
            }

        return pares;
    }

    /** Quantos pares existem com tantos corpos. E o teto de toda coluna das tabelas. */
    public static long todosOsPares(int corpos) {
        return (long) corpos * (corpos - 1) / 2;
    }
}
