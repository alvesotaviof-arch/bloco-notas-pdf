package com.blocopdfapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Componente para visualização de arquivos PDF
 */
public class EditPDF {

    private final BorderPane root;
    private final ScrollPane scrollPane;
    private final VBox pdfContainer;
    private PDDocument document;
    private PDFRenderer renderer;
    private int currentPage = 0;
    private double currentZoom = 1.0;
    private int totalPages = 0;

    // Componentes da interface
    private Label pageLabel;
    private ComboBox<String> zoomCombo;
    private MenuBar menuBar;
    
    // Callbacks para comunicação com a interface principal
    private Runnable onAbrirPDF;
    private Runnable onExportarPDF;
    private Runnable onSobre;
    
    /**
     * Construtor - inicializa o visualizador PDF
     */
    public EditPDF() {
        pdfContainer = new VBox(5);
        pdfContainer.setAlignment(Pos.CENTER);
        pdfContainer.setStyle("-fx-background-color: white;");
        
        scrollPane = new ScrollPane(pdfContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        
        root = new BorderPane();
        
        // Adiciona menu bar ao topo
        menuBar = criarMenuBar();
        VBox topContainer = new VBox(menuBar);
        root.setTop(topContainer);
        
        root.setCenter(scrollPane);
        root.setBottom(createToolBar());
    }

    // Setters para os callbacks
    public void setOnAbrirPDF(Runnable onAbrirPDF) { this.onAbrirPDF = onAbrirPDF; }
    public void setOnExportarPDF(Runnable onExportarPDF) { this.onExportarPDF = onExportarPDF; }
    public void setOnSobre(Runnable onSobre) { this.onSobre = onSobre; }

    /**
     * Cria menu bar para o visualizador PDF
     */
    private MenuBar criarMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // Menu Arquivo PDF
        Menu menuArquivo = new Menu("Arquivo");
        MenuItem abrirPDFItem = new MenuItem("Abrir PDF...");
        MenuItem exportarPDFItem = new MenuItem("Exportar para PDF...");
        
        // Menu Ajuda
        Menu menuAjuda = new Menu("Ajuda");
        MenuItem sobreItem = new MenuItem("Sobre");

        // Configura eventos
        abrirPDFItem.setOnAction(e -> { if (onAbrirPDF != null) onAbrirPDF.run(); });
        exportarPDFItem.setOnAction(e -> { if (onExportarPDF != null) onExportarPDF.run(); });
        sobreItem.setOnAction(e -> { if (onSobre != null) onSobre.run(); });

        menuArquivo.getItems().addAll(abrirPDFItem, exportarPDFItem);
        menuAjuda.getItems().add(sobreItem);
        
        menuBar.getMenus().addAll(menuArquivo, menuAjuda);
        return menuBar;
    }

    /**
     * Cria a barra de ferramentas inferior
     */
    private Node createToolBar() {
        HBox toolBar = new HBox(10);
        toolBar.setAlignment(Pos.CENTER);
        toolBar.setPadding(new Insets(5));
        toolBar.setStyle("-fx-background-color: #e8e8e8;");

        // Botões de navegação
        Button prevButton = new Button("Anterior");
        Button nextButton = new Button("Próximo");
        pageLabel = new Label("Página -/-");

        // Configura navegação
        prevButton.setOnAction(e -> goToPage(currentPage - 1));
        nextButton.setOnAction(e -> goToPage(currentPage + 1));

        // Controle de zoom
        zoomCombo = new ComboBox<>();
        zoomCombo.getItems().addAll("50%", "75%", "100%", "125%", "150%", "200%");
        zoomCombo.setValue("100%");
        zoomCombo.setOnAction(e -> {
            String zoomValue = zoomCombo.getValue().replace("%", "");
            currentZoom = Double.parseDouble(zoomValue) / 100.0;
            if (document != null) {
                renderPage(currentPage);
            }
        });

        // Botões de ação rápida
        Button btnAbrir = new Button("Abrir PDF");
        Button btnExportar = new Button("Exportar");
        
        btnAbrir.setOnAction(e -> { if (onAbrirPDF != null) onAbrirPDF.run(); });
        btnExportar.setOnAction(e -> { if (onExportarPDF != null) onExportarPDF.run(); });

        toolBar.getChildren().addAll(
            btnAbrir, new Separator(),
            prevButton, pageLabel, nextButton, new Separator(),
            new Label("Zoom:"), zoomCombo, new Separator(),
            btnExportar
        );

        return toolBar;
    }

    /**
     * Carrega um arquivo PDF para visualização
     */
    public void loadPDF(File file) {
        close(); // Fecha PDF anterior se existir
        try {
            document = PDDocument.load(file);
            renderer = new PDFRenderer(document);
            totalPages = document.getNumberOfPages();
            currentPage = 0;
            renderPage(currentPage);
        } catch (IOException e) {
            showError("Erro ao carregar PDF", e.getMessage());
            close();
        }
    }

    /**
     * Navega para uma página específica
     */
    private void goToPage(int page) {
        if (page >= 0 && page < totalPages) {
            currentPage = page;
            renderPage(currentPage);
        }
    }

    /**
     * Renderiza a página atual do PDF
     */
    private void renderPage(int pageIndex) {
        if (renderer == null || pageIndex < 0 || pageIndex >= totalPages) return;
        try {
            // Renderiza a página com o zoom atual
            BufferedImage bufferedImage = renderer.renderImage(pageIndex, (float) currentZoom);
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            
            ImageView imageView = new ImageView(fxImage);
            imageView.setPreserveRatio(true);
            
            // Ajusta o tamanho baseado no zoom e na largura disponível
            double scrollWidth = scrollPane.getWidth() - 20;
            if (scrollWidth > 0) {
                imageView.setFitWidth(scrollWidth * currentZoom);
            }

            pdfContainer.getChildren().clear();
            pdfContainer.getChildren().add(imageView);
            
            // Atualiza o label da página
            pageLabel.setText(String.format("Página %d/%d", pageIndex + 1, totalPages));
            scrollPane.setVvalue(0.0); // Volta ao topo
        } catch (IOException e) {
            showError("Erro ao renderizar PDF", e.getMessage());
        }
    }

    /**
     * Exibe mensagem de erro
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Retorna o componente visual do PDF
     */
    public Node getView() {
        return root;
    }

    /**
     * Fecha o documento PDF atual
     */
    public void close() {
        if (document != null) {
            try {
                document.close();
            } catch (IOException e) {
                // Ignora erros ao fechar
            }
            document = null;
            renderer = null;
            pdfContainer.getChildren().clear();
            pageLabel.setText("Página -/-");
            totalPages = 0;
        }
    }
}