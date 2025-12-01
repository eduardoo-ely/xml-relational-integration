# 🔗 Sistema de Integração de Dados (XML & SQL)

Este projeto demonstra a integração de dados heterogêneos, unificando informações relacionais (PostgreSQL) com dados semiestruturados (XML) através de uma aplicação Java Spring Boot.

O sistema processa essas fontes distintas e disponibiliza um relatório consolidado através de uma API REST e um Dashboard Frontend moderno.

## 🛠️ Tecnologias Utilizadas

* **Backend:** Java 17, Spring Boot 3.1.5 (Web)
* **Banco de Dados:** PostgreSQL 15 (via Docker)
* **Processamento XML:** Java DOM Parser
* **Banco de Dados XML (Etapa 1):** BaseX
* **Frontend:** HTML5, CSS3, JavaScript (Fetch API)
* **Containerização:** Docker & Docker Compose
* **Gerenciador de Dependências:** Maven

---

## 📂 Estrutura do Projeto

A organização dos arquivos é crucial para o funcionamento da integração:

```text
projeto_db_xml
├── docker-compose.yml         # Configuração dos containers (Postgres e BaseX)
├── pom.xml                    # Dependências Maven (Spring Boot, Driver SQL)
├── script banco/              # Pasta de DADOS (Cuidado com o nome)
│   ├── postcriatab.sql        # Script de criação das tabelas SQL
│   └── xml/                   # Arquivos de dados semiestruturados
│       ├── fornecimento.xml   # Dados de ligação
│       └── peca.xml           # Outros dados...
└── src/
    └── main/
        ├── java/br/com/trabalho
        │   └── Integracao.java       # Código Principal (API e Lógica)
        └── resources/static
            ├── index.html            # Dashboard Frontend
            └── style.css             # Estilização
````

-----

## 🚀 Como Executar

### 1\. Pré-requisitos

* Java 17 instalado.
* Docker e Docker Compose instalados e rodando.
* IDE recomendada: IntelliJ IDEA.

### 2\. Subindo o Ambiente (Docker)

Na raiz do projeto, execute o comando para subir os bancos de dados:

```bash
docker-compose up -d
```

*Isso iniciará o PostgreSQL na porta **5434** e criará as tabelas automaticamente.*

### 3\. Executando a Aplicação

1.  Abra o projeto no IntelliJ.
2.  Certifique-se de carregar as dependências do Maven (`pom.xml`).
3.  Execute a classe `Integracao.java`.

O servidor iniciará na porta `8080`.

### 4\. Acessando o Sistema

Abra seu navegador e acesse:

👉 **[http://localhost:8080](https://www.google.com/search?q=http://localhost:8080)**

-----

## ⚙️ Arquitetura e Lógica

O sistema resolve o problema de **Integração de Dados** da seguinte forma:

1.  **Leitura Relacional:** Conecta via JDBC no PostgreSQL (Docker) e carrega os dados da tabela `Peca` para um `HashMap` em memória (para alta performance).
2.  **Leitura Semiestruturada:** Utiliza `DocumentBuilder` para ler o arquivo `fornecimento.xml` localmente.
3.  **Processamento (Join):**
    * Itera sobre os nós do XML.
    * Normaliza as chaves (Ex: converte `P1` do XML para `1` do SQL).
    * Cruza as informações consultando o Mapa em memória.
4.  **Exposição:** Disponibiliza os dados processados via JSON no endpoint `/relatorio`.

-----

## 🔌 API Endpoints

### `GET /relatorio`

Retorna a lista completa de peças integradas com seus fornecimentos.

**Exemplo de Resposta (JSON):**

```json
[
  {
    "codigo": "P1",
    "nome": "NULT",
    "quantidade": "200"
  },
  {
    "codigo": "P2",
    "nome": "BOLT",
    "quantidade": "500"
  }
]
```

-----

## 👤 Autores

Desenvolvido por **Eduardo de Paula, Inaye Machado, Mateus Conte e Mateus da Silva** como parte da disciplina de Banco de Dados (Integração de Dados Semiestruturados).