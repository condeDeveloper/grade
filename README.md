# grade

Fase ampla de colisão do zero em Java 21: força bruta, grade uniforme e varredura e poda.

O juiz é a **força bruta**, e ela é o juiz certo e não só o conveniente: não há nela célula a
escolher, lista ativa a manter nem par repetido a evitar. Ela faz as n(n−1)/2 perguntas
possíveis e anota as que deram sim.

Os corpos são círculos com **coordenadas inteiras**, e a pergunta "estes dois se tocam?" é
`dx² + dy² <= (r1+r2)²`, exata num long. Com ponto flutuante, dois corpos exatamente
encostados poderiam dar respostas diferentes por um bit conforme a ordem das contas.

São **duas** unidades, e elas andam em direções opostas. O **par testado** tem teto conhecido,
n(n−1)/2. A **célula tocada** existe porque a primeira sozinha mentiria: diminuir a célula
sempre derruba os pares testados, e o que ela cobra em troca é pôr cada corpo em mais células.

## As duas colunas andam ao contrário

```
$ mvn exec:java -Dexec.args="celula"

      celula   em diametros     pares testados    celulas tocadas         trabalho      fracao da bruta
          75          0.125                295             317533           486452              0.00015
         150          0.250                333              97529           150408              0.00017
         300          0.500                419              34697            54477              0.00021
         600          1.000                585              15027            24661              0.00029
        1200          2.000               1119               8038            15218              0.00056
        2400          4.000               2488               4633            12975              0.00124
        4800          8.000               7033               2984            19768              0.00352
        9600         16.000              22502               2409            49876              0.01126
       19200         32.000              78361               2188           161213              0.03920
```

Olhando só a segunda coluna, célula menor é sempre melhor: 295 pares contra 78.361. Olhando só
a terceira, célula maior é sempre melhor: 2.188 células contra 317.533.

A soma tem **fundo**, e é por isso que o contador tem as duas.

## Como rodar

```
mvn test
mvn exec:java -Dexec.args="tudo"
```

Os comandos do medidor são `correcao`, `celula`, `densidade`, `tamanhos`, `repetidos` e
`tudo`.

## O que as medidas me corrigiram

**O fundo da curva não está na célula do tamanho do corpo.**

Eu escrevi que a regra de bolso que sai dessa tabela é a que todo mundo repete: célula do
tamanho do corpo. O fundo medido está em **quatro diâmetros**, e a célula de um diâmetro custa
**1,90 vezes** o fundo.

A razão é a densidade. A regra do tamanho do corpo supõe um mundo cheio, em que cada célula
tem vários corpos e o teste dentro dela é o que domina. Aqui são dois mil corpos num mundo de
cem mil por cem mil, e com célula de quatro diâmetros ainda sobra pouco mais de um corpo por
célula ocupada: não há o que economizar cortando mais, e cada corte a mais só multiplica as
células.

E o fundo também depende do **peso** que a unidade dá a cada conta. Aqui uma célula tocada e um
par testado valem um cada, e essa escolha é discutível: numa implementação de verdade o par
testado é uma distância e a célula tocada é uma consulta a um mapa, que custa mais. Com a
célula pesando mais, o fundo anda para a direita.

**A grade não foge do quadrático: ela divide a constante.**

```
$ mvn exec:java -Dexec.args="densidade"

      corpos metodo                     pares testados         trabalho      fracao da bruta  pares achados
         500 grade                                  44             6048              0.00035             24
        2000 grade                                 585            24661              0.00029            216
        8000 grade                               10296           111679              0.00032           3648
         500 varredura e poda                     1458             9198              0.01169             24
        8000 varredura e poda                   382777          1257651              0.01196           3648
        8000 forca bruta                      31996000         31996000              1.00000           3648
```

A fração da força bruta fica em **0,0003 de quinhentos a oito mil corpos**. Fração constante
quer dizer que os pares testados crescem com o quadrado, igual à força bruta, só que com uma
constante três mil vezes menor. A varredura e poda faz o mesmo, em 1,2%.

Mais corpos no mesmo espaço é mais corpos por célula, e o teste dentro da célula é quadrático.
A grade divide o quadrático pelo número de células; ela não o elimina.

**Com corpos de tamanhos diferentes, a grade perde até para a força bruta.**

```
$ mvn exec:java -Dexec.args="tamanhos"

  mundo            metodo                     pares testados    celulas tocadas         trabalho  vezes a forca bruta
  todos iguais     grade                                 585              15027            24661                0.012
  todos iguais     varredura e poda                    23992                  0            95327                0.048
  1% grandes       grade                                4012              90214           206774                0.103
  1% grandes       varredura e poda                    34796                  0           127742                0.064
  10% grandes      grade                               36745             571385          5401337                2.702
  10% grandes      varredura e poda                   133136                  0           422630                0.211
```

Com todos do mesmo tamanho a grade ganha da força bruta por 83 vezes e da varredura por 4. Com
**1% de corpos cinquenta vezes maiores** a ordem já virou. Com **10%**, a grade custa
**2,702 vezes a força bruta**: ela perde para o método que não faz nada de esperto.

A grade tem uma célula só, e o tamanho dela só pode estar certo para um tamanho de corpo. A
varredura e poda não tem parâmetro nenhum, e por isso se adapta sozinha.

Levado ao extremo isso deixa de ser lentidão e vira impossibilidade: um corpo de raio vinte
mil numa grade de célula um cobriria **1.600.080.001** células. A grade recusa, com o número
no texto do erro, em vez de estourar a memória.

**E a repetição de par, com célula pequena, é 95% do trabalho.**

```
$ mvn exec:java -Dexec.args="repetidos"

      celula   em diametros        sem a regra        com a regra        repetidos    fracao repetida
          75          0.125               6624                295             6329             0.9555
         150          0.250               2546                333             2213             0.8692
         300          0.500               1361                419              942             0.6921
         600          1.000               1049                585              464             0.4423
        1200          2.000               1442               1119              323             0.2240
        4800          8.000               7220               7033              187             0.0259
```

Dois corpos que dividem várias células são testados em cada uma. A regra que evita isso não
precisa de memória nenhuma: as faixas de células dos dois se cruzam num retângulo, e o par é
testado só na célula do **canto** desse retângulo, que se calcula em quatro contas.

Com célula de um oitavo do diâmetro ela corta 6.624 testes para 295: vinte e duas vezes menos
trabalho pela mesma resposta.

E repare na coluna "sem a regra": ela tem um fundo próprio, em **um diâmetro**. Com a regra,
quanto menor a célula melhor; sem ela, célula pequena multiplica a repetição e o mínimo sai do
extremo e vai para o meio. A mesma estrutura tem dois ótimos diferentes conforme uma linha de
código estar lá ou não.

## O que não tem aqui

Só círculos e só duas dimensões. A fase estreita é uma distância ao quadrado, de propósito:
assim a coluna de pares testados mede só a fase ampla, que é o assunto.

Falta a grade hierárquica, que é a resposta de verdade para os tamanhos misturados: várias
grades de células diferentes, cada corpo na que couber. A tabela dos tamanhos mostra o buraco
que ela preencheria.

## Licença

MIT.
