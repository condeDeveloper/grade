package br.com.conde.grade;

import java.util.Locale;

/**
 * O MEDIDOR: imprime as tabelas que o README mostra e que os testes conferem.
 *
 * <p>Toda tabela daqui tem um teste do lado que afirma os mesmos numeros. Texto nao quebra
 * quando deixa de ser verdade; teste quebra.
 */
public final class Medidor {

    private Medidor() {
    }

    public static void main(String[] args) {
        String comando = args.length > 0 ? args[0] : "tudo";

        if (comando.equals("correcao") || comando.equals("tudo")) correcao();
        if (comando.equals("celula") || comando.equals("tudo")) celula();
        if (comando.equals("densidade") || comando.equals("tudo")) densidade();
        if (comando.equals("tamanhos") || comando.equals("tudo")) tamanhos();
        if (comando.equals("repetidos") || comando.equals("tudo")) repetidos();
    }

    private static void correcao() {
        System.out.println();
        System.out.println("TODO METODO ACHA EXATAMENTE O MESMO CONJUNTO DE PARES.");
        System.out.println();

        var linhas = Medidas.aCorrecao();
        long erradas = linhas.stream().filter(l -> l.divergencias() > 0).count();

        System.out.printf("  %-22s %-14s %12s %14s%n",
                "metodo", "mundo", "pares", "divergencias");

        for (var l : linhas)
            System.out.printf("  %-22s %-14s %12d %14d%n",
                    l.metodo(), l.mundo(), l.pares(), l.divergencias());

        System.out.println();
        System.out.printf("  %d combinacoes, %d com divergencia.%n", linhas.size(), erradas);
        System.out.println();
    }

    private static void celula() {
        System.out.println();
        System.out.println("O TAMANHO DA CELULA, com " + Medidas.CORPOS + " corpos de raio "
                + Medidas.RAIO + ".");
        System.out.println();
        System.out.printf("  %10s %14s %18s %18s %16s %20s%n",
                "celula", "em diametros", "pares testados", "celulas tocadas", "trabalho",
                "fracao da bruta");

        for (var l : Medidas.aCelula())
            System.out.printf(Locale.ROOT, "  %10d %14.3f %18d %18d %16d %20.5f%n",
                    l.celula(), l.emDiametros(), l.paresTestados(), l.celulasTocadas(),
                    l.trabalho(), l.fracaoDaForcaBruta());

        System.out.println();
    }

    private static void densidade() {
        System.out.println();
        System.out.println("O CUSTO POR DENSIDADE, celula de " + Medidas.CELULA + ".");
        System.out.println();
        System.out.printf("  %10s %-22s %18s %16s %20s %14s%n",
                "corpos", "metodo", "pares testados", "trabalho", "fracao da bruta",
                "pares achados");

        for (var l : Medidas.aDensidade())
            System.out.printf(Locale.ROOT, "  %10d %-22s %18d %16d %20.5f %14d%n",
                    l.corpos(), l.metodo(), l.paresTestados(), l.trabalho(),
                    l.fracaoDaForcaBruta(), l.paresAchados());

        System.out.println();
    }

    private static void tamanhos() {
        System.out.println();
        System.out.println("CORPOS DE TAMANHOS DIFERENTES, os grandes com raio "
                + (Medidas.RAIO * 50) + ".");
        System.out.println();
        System.out.printf("  %-16s %-22s %18s %18s %16s %20s%n",
                "mundo", "metodo", "pares testados", "celulas tocadas", "trabalho",
                "vezes a forca bruta");

        for (var l : Medidas.osTamanhos())
            System.out.printf(Locale.ROOT, "  %-16s %-22s %18d %18d %16d %20.3f%n",
                    l.mundo(), l.metodo(), l.paresTestados(), l.celulasTocadas(), l.trabalho(),
                    l.vezesAForcaBruta());

        System.out.println();
    }

    private static void repetidos() {
        System.out.println();
        System.out.println("O QUE A REPETICAO DE PAR CUSTA, por tamanho de celula.");
        System.out.println();
        System.out.printf("  %10s %14s %18s %18s %16s %18s%n",
                "celula", "em diametros", "sem a regra", "com a regra", "repetidos",
                "fracao repetida");

        for (var l : Medidas.osRepetidos())
            System.out.printf(Locale.ROOT, "  %10d %14.3f %18d %18d %16d %18.4f%n",
                    l.celula(), l.emDiametros(), l.semARegra(), l.comARegra(), l.repetidos(),
                    l.fracaoRepetida());

        System.out.println();
    }
}
