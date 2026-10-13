package br.com.conde.grade;

import java.util.Arrays;

/**
 * VARREDURA E PODA: ordena os corpos por um eixo e varre, mantendo uma lista dos que ainda
 * podem tocar.
 *
 * <p>Se o corpo A termina em x = 10 e o corpo B comeca em x = 12, eles nao se tocam, e
 * nenhum corpo que comece depois de B pode tocar A tambem. Entao, percorrendo os corpos em
 * ordem de borda esquerda, da para jogar fora quem ja ficou para tras.
 *
 * <p>A lista ATIVA guarda os corpos cuja borda direita ainda nao passou. Cada corpo novo e
 * testado so contra ela.
 *
 * <h2>Por que este metodo esta aqui</h2>
 *
 * <p>Ele nao tem celula para escolher. A grade uniforme tem um parametro que precisa ser
 * acertado e que depende do tamanho dos corpos; este nao tem parametro nenhum, e por isso
 * ele e a resposta certa quando os corpos tem tamanhos muito diferentes.
 *
 * <p>E ele tem um caso ruim proprio, que e o oposto: corpos empilhados no mesmo x. Ai a
 * lista ativa guarda todo mundo e ele vira forca bruta com uma ordenacao a mais.
 */
public final class VarreduraEPoda implements FaseAmpla {

    @Override
    public String nome() {
        return "varredura e poda";
    }

    @Override
    public Corpo.Pares pares(Corpo[] corpos, Contador contador) {
        contador.consulta();

        Integer[] ordem = new Integer[corpos.length];

        for (int i = 0; i < corpos.length; i++) ordem[i] = i;

        Arrays.sort(ordem, (a, b) -> {
            contador.compara();
            return Integer.compare(corpos[a].esquerda(), corpos[b].esquerda());
        });

        Corpo.Pares pares = new Corpo.Pares();

        int[] ativos = new int[corpos.length];
        int quantosAtivos = 0;

        for (int posicao = 0; posicao < ordem.length; posicao++) {
            int i = ordem[posicao];
            int esquerda = corpos[i].esquerda();

            /*
             * A PODA ACONTECE AQUI. Quem termina antes da borda esquerda do corpo atual nao
             * pode tocar nem ele nem nenhum dos proximos, porque os proximos comecam ainda
             * mais a direita.
             */
            int sobraram = 0;

            for (int a = 0; a < quantosAtivos; a++) {
                contador.veCorpo();
                contador.compara();

                if (corpos[ativos[a]].direita() < esquerda) continue;

                ativos[sobraram++] = ativos[a];
            }

            quantosAtivos = sobraram;

            for (int a = 0; a < quantosAtivos; a++) {
                contador.testaPar();

                if (corpos[i].toca(corpos[ativos[a]])) pares.achou(i, ativos[a]);
            }

            ativos[quantosAtivos++] = i;
        }

        return pares;
    }
}
