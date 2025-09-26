package com.blocopdfapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

/**
 * Classe principal que gerencia a interface gráfica da aplicação
 * Bradypus Torquatus Pdf - Editor de Texto com visualizador PDF
 */
public class InterfaceGrafica extends Application {

    // Componentes da interface
    private Stage primaryStage;          // Janela principal
    private BlocoDeNotas blocoDeNotas;   // Componente do editor de texto
    private EditPDF editPDF;             // Componente do visualizador PDF
    private SplitPane splitPane;         // Painel dividido (lado a lado)
    private BorderPane root;             // Layout raiz
    
    // Configurações de tema (apenas para o alerta "Sobre")
    private static final String DARK_MODE_CSS = "/dark-mode.css";

    /**
     * Método principal de inicialização da aplicação JavaFX
     */
    @Override
    public void start(Stage stage) {
        // Configuração da janela principal
        this.primaryStage = stage;
        this.primaryStage.setTitle("Bradypus Torquatus Pdf");

        // Inicialização dos componentes
        this.blocoDeNotas = new BlocoDeNotas();
        this.editPDF = new EditPDF();

        // Configuração dos callbacks (comunicação entre componentes)
        blocoDeNotas.setOnAbrirPDF(() -> abrirVisualizadorPDF());
        blocoDeNotas.setOnExportarPDF(() -> exportarParaPDF());
        blocoDeNotas.setOnSobre(() -> mostrarSobre());

        // Configura callbacks do PDF também
        editPDF.setOnAbrirPDF(() -> abrirVisualizadorPDF());
        editPDF.setOnExportarPDF(() -> exportarParaPDF());
        editPDF.setOnSobre(() -> mostrarSobre());

        // Configuração do layout principal
        root = new BorderPane();
        splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);  // Divisão horizontal
        
        // Painel do editor de texto (lado esquerdo)
        BorderPane editorPane = new BorderPane();
        editorPane.setCenter(blocoDeNotas.getView());
        
        // Painel do PDF (lado direito)
        BorderPane pdfPane = new BorderPane();
        pdfPane.setCenter(editPDF.getView());
        
        // Adiciona os painéis ao split pane
        splitPane.getItems().addAll(editorPane, pdfPane);
        splitPane.setDividerPositions(0.7);  // 70% para editor, 30% para PDF
        
        root.setCenter(splitPane);
        
        // Cria e configura a cena principal
        Scene scene = new Scene(root, 1400, 800);
        primaryStage.setScene(scene);
        primaryStage.show();  // Exibe a janela
    }

    /**
     * Exibe a janela "Sobre" como popup com o texto solicitado
     */
    private void mostrarSobre() {
        String textoCompleto = "Programa desenvolvido para matéria de Atividades Extensionista III, " +
                               "Por Otavio Alves Coelho Farnochia, no curso de Bacharel em engenharia de Software, " +
                               "na Instituição Uninter, como finalidade de dar apoio e integrar a comunidade da escolar Dr. Jose Fornari e a Universidade.";

        // Garante que o Alert seja criado na JavaFX Application Thread
        Platform.runLater(() -> {
            Alert sobreAlert = new Alert(Alert.AlertType.INFORMATION);
            sobreAlert.setTitle("Sobre");
            sobreAlert.setHeaderText("Bradypus Torquatus Pdf");
            sobreAlert.setContentText(textoCompleto);
            
            // Define a janela principal como dona do popup
            sobreAlert.initOwner(primaryStage);
            
            // Aplica o tema escuro ao alerta
            sobreAlert.getDialogPane().getStylesheets().add(getClass().getResource(DARK_MODE_CSS).toExternalForm());
            
            sobreAlert.showAndWait();
        });
    }

    /**
     * Abre o diálogo para selecionar e carregar um arquivo PDF
     */
    private void abrirVisualizadorPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir Arquivo PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                editPDF.loadPDF(file);  // Carrega o PDF no visualizador
            } catch (Exception e) {
                mostrarErro("Erro ao carregar PDF", e.getMessage());
            }
        }
    }

    /**
     * Exporta o conteúdo do editor de texto para PDF
     */
    private void exportarParaPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exportar para PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf"));
            File file = fileChooser.showSaveDialog(primaryStage);
            
            if (file != null) {
                // Simula exportação (funcionalidade básica)
                String texto = blocoDeNotas.getText();
                new Alert(Alert.AlertType.INFORMATION, 
                         "Funcionalidade de exportação em desenvolvimento.\n" +
                         "Texto pronto para exportação: " + texto.length() + " caracteres.").showAndWait();
            }
        } catch (Exception e) {
            mostrarErro("Erro ao exportar PDF", e.getMessage());
        }
    }

    /**
     * Exibe uma mensagem de erro na interface
     */
    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    /**
     * Método chamado quando a aplicação é fechada
     * Realiza limpeza de recursos
     */
    @Override
    public void stop() throws Exception {
        if (editPDF != null) {
            editPDF.close();  // Fecha o documento PDF
        }
        super.stop();
    }

    /**
     * Método principal - ponto de entrada da aplicação
     */
    public static void main(String[] args) {
        launch(args);  // Inicia a aplicação JavaFX
    }
}