package br.com.conde.grade;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * OS MUNDOS DE CORPOS. Eles nao sao decoracao: a forma do mundo decide qual metodo ganha.
 *
 * <p>O parametro que mais manda e a relacao entre o TAMANHO DOS CORPOS e o tamanho da
 * celula, e e por isso que os geradores daqui controlam o raio diretamente em vez de
 * sortear qualquer coisa.
 */
public final class Mundos {

    private Mundos() {
    }

    /** O lado do quadrado em que os corpos sao espalhados. */
    public static final int LADO = 100_000;

    /**
     * O SORTEIO DA CASA: um gerador congruente linear escrito aqui, com constantes fixas.
     *
     * <p>Nao e {@link java.util.Random} de proposito. Uma medida que depende de uma
     * biblioteca nao e reproduzivel: a implementacao pode mudar entre versoes da maquina
     * virtual e as tabelas do README deixariam de bater sem que nada aqui tivesse mudado.
     */
    public static final class Sorteio {

        private long estado;

        public Sorteio(long semente) {
            this.estado = semente;
        }

        /**
         * O proximo inteiro.
         *
         * <p>O DESLOCAMENTO DE 32 CARREGA O METODO. Os bits BAIXOS de um gerador congruente
         * linear de modulo potencia de dois sao pessimos: o ultimo bit alterna zero e um
         * para sempre, o penultimo tem periodo quatro.
         *
         * <p>Num repositorio sobre grades isso seria direto: a celula de um corpo e a
         * coordenada dividida pelo lado da celula, e com lado potencia de dois isso e um
         * deslocamento. Coordenadas com bits ruins cairiam numa REDE REGULAR, os corpos
         * ficariam alinhados e a grade mediria a rede em vez de medir a densidade. Ha um
         * teste so para isto.
         */
        public int proximo() {
            estado = estado * 6364136223846793005L + 1442695040888963407L;
            return (int) (estado >>> 32);
        }

        public int ate(int limite) {
            return Math.floorMod(proximo(), limite);
        }
    }

    /** Corpos espalhados por igual, todos do mesmo raio. */
    public static Corpo[] uniforme(int quantos, int raio, long semente) {
        Sorteio sorteio = new Sorteio(semente);
        Corpo[] corpos = new Corpo[quantos];

        for (int i = 0; i < quantos; i++)
            corpos[i] = new Corpo(sorteio.ate(LADO), sorteio.ate(LADO), raio);

        return corpos;
    }

    /**
     * Corpos com raios bem diferentes: a maioria pequena e alguns enormes.
     *
     * <p>E o caso ruim da grade uniforme, e nao e uma situacao inventada: num jogo, as
     * balas sao pequenas, os personagens sao medios e o chao e uma coisa so do tamanho da
     * tela. Um corpo de raio mil numa grade de celula dez ocupa quarenta mil celulas.
     */
    public static Corpo[] deTamanhosMisturados(int quantos, int raioPequeno, int raioGrande,
                                               int quantosGrandes, long semente) {

        Sorteio sorteio = new Sorteio(semente);
        Corpo[] corpos = new Corpo[quantos];

        for (int i = 0; i < quantos; i++) {
            int raio = i < quantosGrandes ? raioGrande : raioPequeno;

            corpos[i] = new Corpo(sorteio.ate(LADO), sorteio.ate(LADO), raio);
        }

        return corpos;
    }

    /**
     * Corpos empilhados numa faixa estreita de um eixo.
     *
     * <p>E o caso ruim da varredura e poda: a lista ativa guarda quase todo mundo, porque
     * quase todo mundo se sobrepoe naquele eixo.
     */
    public static Corpo[] emColuna(int quantos, int raio, int largura, long semente) {
        Sorteio sorteio = new Sorteio(semente);
        Corpo[] corpos = new Corpo[quantos];

        for (int i = 0; i < quantos; i++)
            corpos[i] = new Corpo(LADO / 2 + sorteio.ate(largura), sorteio.ate(LADO), raio);

        return corpos;
    }

    /** Corpos em grupos apertados, que e como corpo de verdade costuma estar. */
    public static Corpo[] emGrupos(int quantos, int raio, int grupos, long semente) {
        Sorteio sorteio = new Sorteio(semente);
        int[][] centros = new int[grupos][2];

        for (int g = 0; g < grupos; g++) {
            centros[g][0] = sorteio.ate(LADO);
            centros[g][1] = sorteio.ate(LADO);
        }

        int espalhamento = Math.max(2, LADO / 100);
        Corpo[] corpos = new Corpo[quantos];

        for (int i = 0; i < quantos; i++) {
            int g = sorteio.ate(grupos);

            corpos[i] = new Corpo(centros[g][0] + sorteio.ate(espalhamento),
                    centros[g][1] + sorteio.ate(espalhamento), raio);
        }

        return corpos;
    }

    public static Map<String, Corpo[]> todos(int quantos, int raio, long semente) {
        Map<String, Corpo[]> mundos = new LinkedHashMap<>();

        mundos.put("uniforme", uniforme(quantos, raio, semente));
        mundos.put("em grupos", emGrupos(quantos, raio, 20, semente + 1));
        mundos.put("em coluna", emColuna(quantos, raio, raio * 4, semente + 2));
        mundos.put("misturado", deTamanhosMisturados(quantos, raio, raio * 50,
                Math.max(1, quantos / 100), semente + 3));

        return mundos;
    }

    /** Os metodos que as tabelas percorrem, cada um com o nome que a tabela imprime. */
    public static Map<String, Supplier<FaseAmpla>> osMetodos(int celula) {
        Map<String, Supplier<FaseAmpla>> metodos = new LinkedHashMap<>();

        metodos.put("forca bruta", ForcaBruta::new);
        metodos.put("grade", () -> new GradeUniforme(celula, true));
        metodos.put("grade com repetidos", () -> new GradeUniforme(celula, false));
        metodos.put("varredura e poda", VarreduraEPoda::new);

        return metodos;
    }
}
