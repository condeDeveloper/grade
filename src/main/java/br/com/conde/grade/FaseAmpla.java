package br.com.conde.grade;

/**
 * A FASE AMPLA: achar todos os pares de corpos que se tocam.
 *
 * <p>Todos os metodos daqui devolvem exatamente o mesmo conjunto de pares. Nenhum aproxima,
 * nenhum deixa par de fora, nenhum devolve par a mais. O que muda e quantas vezes cada um
 * precisa perguntar "estes dois se tocam?" para chegar la.
 *
 * <p>"Ampla" e o nome que a area usa para a primeira das duas fases. A fase ESTREITA e a
 * que responde a pergunta de verdade, com a geometria dos corpos; a ampla so decide quais
 * perguntas vale a pena fazer. Aqui a fase estreita e uma distancia ao quadrado, e e de
 * proposito que ela seja barata: assim a coluna de pares testados mede so a fase ampla.
 */
public interface FaseAmpla {

    /** Os pares que se tocam, cada um uma vez so. */
    Corpo.Pares pares(Corpo[] corpos, Contador contador);

    String nome();
}
