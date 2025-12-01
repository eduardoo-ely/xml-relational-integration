(: ============================================================================ :)
(: TRABALHO DE BANCO DE DADOS - ETAPA 1: CONSULTAS XQUERY                    :)
(: ============================================================================ :)

(: ============================================================================ :)
(: a) Retornar os dados da penúltima peça da árvore XML                       :)
(: ============================================================================ :)

let $pecas := doc("peca.xml")//peca
let $penultima := $pecas[last() - 1]
return $penultima

(: ============================================================================ :)
(: b) Inserir um atributo data (YYYY-MM-DD) em todos os fornecimentos         :)
(: ============================================================================ :)

for $fornec in doc("fornecimento.xml")//fornecimento
return
  copy $novo := $fornec
  modify insert node attribute data {"2024-12-01"} into $novo
  return $novo

(: ============================================================================ :)
(: c) Atualizar o status dos fornecedores de Londres para 50                  :)
(: ============================================================================ :)

for $fornecedor in doc("fornecedor.xml")//fornecedor
where $fornecedor/Cidade = "LONDRES"
return
  copy $atualizado := $fornecedor
  modify replace value of node $atualizado/Status with "50"
  return $atualizado

(: ============================================================================ :)
(: d) Retornar o código, a cidade e cor de todas as peças                     :)
(: ============================================================================ :)

for $peca in doc("peca.xml")//peca
return
  <resultado>
    <codigo>{data($peca/Cod_Peca)}</codigo>
    <cidade>{data($peca/Cidade)}</cidade>
    <cor>{data($peca/Cor)}</cor>
  </resultado>

(: ============================================================================ :)
(: e) Obter o somatório das quantidades dos fornecimentos                     :)
(: ============================================================================ :)

let $total := sum(doc("fornecimento.xml")//fornecimento/Quantidade)
return
  <total_fornecimentos>{$total}</total_fornecimentos>

(: ============================================================================ :)
(: f) Obter os nomes dos projetos de Paris                                    :)
(: ============================================================================ :)

for $projeto in doc("projeto.xml")//projeto
where $projeto/Cidade = "PARIS"
return
  <projeto_paris>{data($projeto/Jnome)}</projeto_paris>

(: ============================================================================ :)
(: g) Obter o código dos fornecedores que forneceram peças em maior quantidade:)
(: ============================================================================ :)

let $max_qtd := max(doc("fornecimento.xml")//fornecimento/Quantidade)
for $fornec in doc("fornecimento.xml")//fornecimento
where $fornec/Quantidade = $max_qtd
return
  <fornecedor_maior_quantidade>
    <codigo_fornecedor>{data($fornec/Cod_Fornec)}</codigo_fornecedor>
    <quantidade>{data($fornec/Quantidade)}</quantidade>
  </fornecedor_maior_quantidade>

(: ============================================================================ :)
(: h) Excluir os projetos da cidade de Atenas                                 :)
(: ============================================================================ :)

copy $doc := doc("projeto.xml")
modify delete node $doc//projeto[Cidade = "ATENAS"]
return $doc

(: ============================================================================ :)
(: i) Obter os nomes das peças e seus dados de fornecimento                   :)
(: ============================================================================ :)

for $fornec in doc("fornecimento.xml")//fornecimento
let $cod_peca := $fornec/Cod_Peca
let $peca := doc("peca.xml")//peca[Cod_Peca = $cod_peca]
return
  <fornecimento_detalhado>
    <codigo_peca>{data($peca/Cod_Peca)}</codigo_peca>
    <nome_peca>{data($peca/PNome)}</nome_peca>
    <codigo_fornecedor>{data($fornec/Cod_Fornec)}</codigo_fornecedor>
    <codigo_projeto>{data($fornec/Cod_Proj)}</codigo_projeto>
    <quantidade>{data($fornec/Quantidade)}</quantidade>
  </fornecimento_detalhado>

(: ============================================================================ :)
(: j) Obter o preço médio das peças                                           :)
(: ============================================================================ :)

let $media := avg(doc("peca.xml")//peca/Preco)
return
  <preco_medio_pecas>{$media}</preco_medio_pecas>

(: ============================================================================ :)
(: FIM DAS CONSULTAS XQUERY - ETAPA 1                                         :)
(: ============================================================================ :)