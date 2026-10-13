package br.com.conde.grade;

/**
 * O CONTADOR: pares testados e celulas tocadas. Nunca segundos.
 *
 * <p>O PAR TESTADO e a medida certa e nao so a conveniente. Toda fase ampla de colisao
 * existe para uma coisa so: evitar perguntar "estes dois se tocam?" para os n(n-1)/2 pares
 * possiveis. O que separa um metodo do outro e so quantas dessas perguntas ele consegue nao
 * fazer.
 *
 * <p>E a medida tem um TETO conhecido, que e n(n-1)/2, e isso e raro e util: uma coluna de
 * pares testados ja vem como fracao do unico numero que importa. Testar 499.500 pares com
 * mil corpos e uma frase completa.
 *
 * <p>A CELULA TOCADA e a segunda conta, e ela existe porque a primeira sozinha mentiria.
 * Diminuir a celula sempre derruba os pares testados; o que ela cobra em troca e colocar
 * cada corpo em mais celulas. Uma tabela com uma coluna so diria que celula menor e sempre
 * melhor, e a tabela da celula mostra onde isso deixa de ser verdade.
 *
 * <p>Segundos nao serviriam: a integracao continua roda em tres sistemas, e percorrer um
 * vetor de corpos contiguo e percorrer listas espalhadas por uma grade tem velocidades bem
 * diferentes pelo mesmo numero de operacoes.
 */
public final class Contador {

    private long paresTestados;
    private long paresRepetidos;
    private long celulasTocadas;
    private long corposVistos;
    private long comparacoes;
    private long consultas;

    /** Uma pergunta "estes dois se tocam?", respondida por distancia. */
    public void testaPar() {
        paresTestados++;
    }

    /** Um par que ja tinha sido testado numa celula anterior. */
    public void parRepetido() {
        paresRepetidos++;
    }

    /** Um corpo foi posto numa celula, ou uma celula foi aberta. */
    public void tocaCelula() {
        celulasTocadas++;
    }

    public void tocaCelula(long quantas) {
        celulasTocadas += quantas;
    }

    /** Um corpo foi olhado dentro de uma celula ou de uma lista ativa. */
    public void veCorpo() {
        corposVistos++;
    }

    public void compara() {
        comparacoes++;
    }

    public void consulta() {
        consultas++;
    }

    public long paresTestados() {
        return paresTestados;
    }

    public long paresRepetidos() {
        return paresRepetidos;
    }

    public long celulasTocadas() {
        return celulasTocadas;
    }

    public long corposVistos() {
        return corposVistos;
    }

    public long comparacoes() {
        return comparacoes;
    }

    /**
     * O trabalho total, somando as contas de natureza diferente.
     *
     * <p>Somar par testado com celula tocada nao e exato. Mas sem a soma nao da para
     * ordenar os metodos, porque cada um gasta numa coluna diferente, e e justamente essa
     * troca que a tabela da celula existe para mostrar. As colunas separadas ficam do lado.
     */
    public long trabalho() {
        return paresTestados + celulasTocadas + corposVistos + comparacoes;
    }

    public double paresPorConsulta() {
        return consultas == 0 ? 0 : (double) paresTestados / consultas;
    }

    public double trabalhoPorConsulta() {
        return consultas == 0 ? 0 : (double) trabalho() / consultas;
    }

    public void zerar() {
        paresTestados = 0;
        paresRepetidos = 0;
        celulasTocadas = 0;
        corposVistos = 0;
        comparacoes = 0;
        consultas = 0;
    }
}
