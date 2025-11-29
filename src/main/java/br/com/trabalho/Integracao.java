package br.com.trabalho;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 1. Anotação mágica que transforma isso num servidor
@SpringBootApplication
@RestController
public class Integracao {

    public static void main(String[] args) {
        SpringApplication.run(Integracao.class, args);
    }

    // 2. Formatar o JSON
    static class RelatorioItem {
        public String codigo;
        public String nome;
        public String quantidade;

        public RelatorioItem(String codigo, String nome, String quantidade) {
            this.codigo = codigo;
            this.nome = nome;
            this.quantidade = quantidade;
        }
    }

    // 3. Interface
    @CrossOrigin(origins = "*")
    @GetMapping("/relatorio")
    public List<RelatorioItem> gerarRelatorio() {
        List<RelatorioItem> listaResultado = new ArrayList<>();
        Map<Integer, String> pecasSQL = new HashMap<>();

        // --- CONEXÃO SQL ---
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5434/trabalho_integracao", "admin", "admin123")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT cod_peca, pnome FROM Peca");
            while (rs.next()) {
                pecasSQL.put(rs.getInt("cod_peca"), rs.getString("pnome"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of(new RelatorioItem("ERRO", "Falha no Banco", e.getMessage()));
        }

        // --- LEITURA XML ---
        try {
            File xmlFile = new File("script banco/xml/fornecimento.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("fornecimento");

            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    String codPecaXML = getTagValue("Cod_Peca", element);
                    String qtdXML = getTagValue("Quantidade", element);
                    String nomePeca = "---";

                    try {
                        int idBanco = Integer.parseInt(codPecaXML.toUpperCase().replace("P", "").trim());
                        nomePeca = pecasSQL.getOrDefault(idBanco, "NÃO ENCONTRADO");
                    } catch (Exception e) {
                        nomePeca = "ERRO FORMATO";
                    }

                    // Adiciona na lista (que vai virar JSON)
                    listaResultado.add(new RelatorioItem(codPecaXML, nomePeca, qtdXML));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaResultado;
    }

    private String getTagValue(String tag, Element element) {
        try {
            return element.getElementsByTagName(tag).item(0).getTextContent();
        } catch (Exception e) { return ""; }
    }
}