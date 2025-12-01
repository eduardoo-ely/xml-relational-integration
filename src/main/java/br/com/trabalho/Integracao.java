package br.com.trabalho;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.*;
import java.util.*;

@SpringBootApplication
@RestController
public class Integracao {

    // Caminho base dos arquivos
    private static final String XML_PATH = "script banco/xml/";

    public static void main(String[] args) {
        SpringApplication.run(Integracao.class, args);
    }

    // --- CLASSES DE RESPOSTA (JSON) ---

    // Para a Etapa 2 (Tabela)
    static class RelatorioItem {
        public String codigo, nome, cor, cidade, quantidade;
        public String origem; // "XML" ou "SQL"

        public RelatorioItem(String codigo, String nome, String cor, String cidade, String quantidade) {
            this.codigo = codigo; this.nome = nome; this.cor = cor; this.cidade = cidade; this.quantidade = quantidade;
        }
    }

    // Para a Etapa 1 (Cards e Listas)
    static class DashboardStats {
        public String somaQuantidades;    // Questão E
        public String precoMedioPecas;    // Questão J
        public List<String> projetosParis; // Questão F
        public String maiorFornecedor;    // Questão G
        public String penultimaPeca;      // Questão A

        public DashboardStats() {
            this.projetosParis = new ArrayList<>();
        }
    }

    // --- ENDPOINTS DA API ---

    @CrossOrigin(origins = "*")
    @GetMapping("/etapa1")
    public DashboardStats getEtapa1() {
        DashboardStats stats = new DashboardStats();

        try {
            // 1. Ler Fornecimentos (Soma e Maior Fornecedor)
            Document docFornec = lerXML("fornecimento.xml");
            NodeList listaF = docFornec.getElementsByTagName("fornecimento");
            int soma = 0;
            Map<String, Integer> mapFornecedor = new HashMap<>();

            for (int i = 0; i < listaF.getLength(); i++) {
                Element el = (Element) listaF.item(i);
                int qtd = Integer.parseInt(getTag(el, "Quantidade"));
                String codF = getTag(el, "Cod_Fornec");

                // Soma (Questão E)
                soma += qtd;

                // Contagem para Maior Fornecedor (Questão G)
                mapFornecedor.put(codF, mapFornecedor.getOrDefault(codF, 0) + qtd);
            }
            stats.somaQuantidades = String.valueOf(soma);

            // Achar o maior fornecedor
            stats.maiorFornecedor = mapFornecedor.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).orElse("N/A");

            // 2. Ler Peças (Preço Médio e Penúltima)
            Document docPeca = lerXML("peca.xml");
            NodeList listaP = docPeca.getElementsByTagName("peca");
            double somaPreco = 0;
            int countPreco = 0;

            // Penúltima Peça (Questão A) - Pega o length - 2
            if (listaP.getLength() >= 2) {
                Element penultima = (Element) listaP.item(listaP.getLength() - 2);
                stats.penultimaPeca = getTag(penultima, "Cod_Peca") + " - " + getTag(penultima, "PNome");
            }

            for (int i = 0; i < listaP.getLength(); i++) {
                Element el = (Element) listaP.item(i);
                try {
                    somaPreco += Double.parseDouble(getTag(el, "Preco"));
                    countPreco++;
                } catch (Exception ignored) {}
            }
            // Média (Questão J)
            if (countPreco > 0) {
                stats.precoMedioPecas = String.format("%.2f", somaPreco / countPreco);
            }

            // 3. Ler Projetos (Projetos de Paris - Questão F)
            Document docProj = lerXML("projeto.xml");
            NodeList listaJ = docProj.getElementsByTagName("projeto");

            for (int i = 0; i < listaJ.getLength(); i++) {
                Element el = (Element) listaJ.item(i);
                if ("PARIS".equalsIgnoreCase(getTag(el, "Cidade"))) {
                    stats.projetosParis.add(getTag(el, "Jnome"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            stats.somaQuantidades = "Erro";
        }
        return stats;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/relatorio")
    public List<RelatorioItem> gerarRelatorioIntegracao() {
        List<RelatorioItem> relatorio = new ArrayList<>();
        Map<Integer, String[]> mapaSQL = new HashMap<>(); // Guarda Nome, Cor, Cidade

        // 1. Busca no SQL
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5434/trabalho_integracao", "admin", "admin123")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT cod_peca, pnome, cor, cdade FROM Peca");
            while (rs.next()) {
                mapaSQL.put(rs.getInt("cod_peca"), new String[]{
                        rs.getString("pnome"), rs.getString("cor"), rs.getString("cdade")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 2. Busca no XML e Cruza
        try {
            Document doc = lerXML("fornecimento.xml");
            NodeList list = doc.getElementsByTagName("fornecimento");

            for (int i = 0; i < list.getLength(); i++) {
                Element el = (Element) list.item(i);
                String codXML = getTag(el, "Cod_Peca");
                String qtd = getTag(el, "Quantidade");

                String nome = "---", cor = "---", cidade = "---";

                try {
                    int id = Integer.parseInt(codXML.toUpperCase().replace("P", "").trim());
                    if (mapaSQL.containsKey(id)) {
                        String[] dados = mapaSQL.get(id);
                        nome = dados[0]; cor = dados[1]; cidade = dados[2];
                    }
                } catch (Exception ignored) {}

                relatorio.add(new RelatorioItem(codXML, nome, cor, cidade, qtd));
            }
        } catch (Exception e) { e.printStackTrace(); }

        return relatorio;
    }

    // --- UTILITÁRIOS ---
    private Document lerXML(String arquivo) throws Exception {
        File xmlFile = new File(XML_PATH + arquivo);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.parse(xmlFile);
    }

    private String getTag(Element el, String tag) {
        try { return el.getElementsByTagName(tag).item(0).getTextContent(); } catch (Exception e) { return ""; }
    }
}