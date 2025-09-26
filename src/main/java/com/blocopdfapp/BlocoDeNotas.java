package com.blocopdfapp;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.model.TwoDimensional;

import java.io.*;
import java.util.regex.Pattern;

/**
 * Componente do editor de texto com funcionalidades avançadas
 * Oferece funcionalidades de formatação, alinhamento e marca-texto
 */
public class BlocoDeNotas {

    // Componentes da interface
    private final BorderPane root;           // Layout principal
    private final InlineCssTextArea textArea; // Área de texto com suporte a CSS inline
    private final MenuBar menuBar;           // Barra de menus
    private final ToolBar toolBar;           // Barra de ferramentas
    private File currentFile;                // Arquivo atual em edição
    
    // Callbacks para comunicação com a interface principal
    private Runnable onAbrirPDF;
    private Runnable onExportarPDF;
    private Runnable onSobre;

    // Configurações de formatação
    private final String[] fontesPermitidas = {"Times New Roman", "Arial", "Helvetica", "Verdana", "Calibri"};
    private final int[] tamanhosFonte = {10, 11, 12, 14, 16, 18, 20};

    // Componentes da barra de ferramentas
    private ComboBox<String> comboFontes;
    private ComboBox<Integer> comboTamanhos;
    private ToggleButton btnNegrito, btnItalico, btnSublinhado;
    private ToggleGroup grupoAlinhamento;
    private ToggleButton btnEsquerda, btnCentralizado, btnDireita, btnJustificado;
    private ComboBox<String> comboMarcaTexto;

    /**
     * Construtor - inicializa todos os componentes
     */
    public BlocoDeNotas() {
        // Inicialização dos componentes básicos
        this.root = new BorderPane();
        this.textArea = new InlineCssTextArea();
        this.menuBar = criarMenuBar();
        this.toolBar = criarToolBar();

        // Configuração do componente
        configurarOuvintesMouse();
        configurarAreaTexto();
        configurarFormatoPadrao();
        
        // Montagem do layout
        VBox topContainer = new VBox(menuBar, toolBar);
        root.setTop(topContainer);
        root.setCenter(new VirtualizedScrollPane<>(textArea));  // Área de texto com scroll

        // Aplica tema escuro por padrão na área de texto
        isDarkMode = true;
        aplicarTemaTexto(isDarkMode);
    }

    // Estado do tema da área de texto
    private boolean isDarkMode = true;

    // Métodos setters para os callbacks
    public void setOnAbrirPDF(Runnable onAbrirPDF) { this.onAbrirPDF = onAbrirPDF; }
    public void setOnExportarPDF(Runnable onExportarPDF) { this.onExportarPDF = onExportarPDF; }
    public void setOnSobre(Runnable onSobre) { this.onSobre = onSobre; }

    /**
     * Configura propriedades básicas da área de texto
     */
    private void configurarAreaTexto() {
        textArea.setWrapText(true);              // Quebra de linha automática
        textArea.setParagraphGraphicFactory(null); // Remove gráficos de parágrafo padrão
    }

    /**
     * Aplica a formatação padrão
     */
    private void configurarFormatoPadrao() {
        Platform.runLater(() -> {
            aplicarTemaTexto(isDarkMode);
        });
    }

    /**
     * Configura ouvintes para atualizar a barra de ferramentas
     */
    private void configurarOuvintesMouse() {
        // Atualiza quando o cursor se move
        textArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(this::atualizarEstadoToolbar);
        });
        
        // Atualiza quando a seleção muda
        textArea.selectedTextProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(this::atualizarEstadoToolbar);
        });
    }

    /**
     * Cria e configura a barra de menus
     */
    private MenuBar criarMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // Menu Arquivo - operações com arquivos
        Menu menuArquivo = new Menu("Arquivo");
        
        // Operações com texto
        MenuItem novoItem = new MenuItem("Novo Documento");
        MenuItem abrirItem = new MenuItem("Abrir Texto");
        MenuItem salvarItem = new MenuItem("Salvar");
        MenuItem salvarComoItem = new MenuItem("Salvar Como");
        
        // Operações PDF
        MenuItem abrirPDFItem = new MenuItem("Abrir PDF...");
        MenuItem exportarPDFItem = new MenuItem("Exportar para PDF...");
        
        MenuItem sairItem = new MenuItem("Sair");

        // Configuração dos eventos dos itens do menu
        novoItem.setOnAction(e -> novoDocumento());
        abrirItem.setOnAction(e -> abrirArquivo());
        salvarItem.setOnAction(e -> salvarArquivo());
        salvarComoItem.setOnAction(e -> salvarComoArquivo());
        
        // Eventos para operações PDF
        abrirPDFItem.setOnAction(e -> { if (onAbrirPDF != null) onAbrirPDF.run(); });
        exportarPDFItem.setOnAction(e -> { if (onExportarPDF != null) onExportarPDF.run(); });
        
        sairItem.setOnAction(e -> Platform.exit());

        // Adiciona itens ao menu principal
        menuArquivo.getItems().addAll(novoItem, abrirItem, new SeparatorMenuItem(),
                                   salvarItem, salvarComoItem, new SeparatorMenuItem(),
                                   abrirPDFItem, exportarPDFItem, new SeparatorMenuItem(), sairItem);

        // Menu Editar - operações de edição
        Menu menuEditar = new Menu("Editar");
        MenuItem desfazerItem = new MenuItem("Desfazer");
        MenuItem refazerItem = new MenuItem("Refazer");
        MenuItem recortarItem = new MenuItem("Recortar");
        MenuItem copiarItem = new MenuItem("Copiar");
        MenuItem colarItem = new MenuItem("Colar");

        desfazerItem.setOnAction(e -> textArea.undo());
        refazerItem.setOnAction(e -> textArea.redo());
        recortarItem.setOnAction(e -> textArea.cut());
        copiarItem.setOnAction(e -> textArea.copy());
        colarItem.setOnAction(e -> textArea.paste());

        menuEditar.getItems().addAll(desfazerItem, refazerItem, new SeparatorMenuItem(),
                                     recortarItem, copiarItem, colarItem);
                                     
        // Menu Visualizar - configurações de visualização
        Menu menuVisualizar = new Menu("Visualizar");
        MenuItem alternarTemaItem = new MenuItem("Alternar Tema (Claro/Escuro)");
        alternarTemaItem.setOnAction(e -> alternarTemaTexto());
        menuVisualizar.getItems().add(alternarTemaItem);

        // Menu Formatar - opções de formatação de texto
        Menu menuFormatar = new Menu("Formatar");
        Menu menuFonte = new Menu("Fonte");
        Menu menuTamanho = new Menu("Tamanho");
        Menu menuAlinhamento = new Menu("Alinhamento");
        Menu menuMarcaTexto = new Menu("Marca Texto");

        // Popula o menu de fontes
        for (String fonte : fontesPermitidas) {
            MenuItem itemFonte = new MenuItem(fonte);
            itemFonte.setOnAction(e -> aplicarFonte(fonte));
            menuFonte.getItems().add(itemFonte);
        }

        // Popula o menu de tamanhos
        for (int tamanho : tamanhosFonte) {
            MenuItem itemTamanho = new MenuItem(String.valueOf(tamanho));
            itemTamanho.setOnAction(e -> aplicarTamanhoFonte(tamanho));
            menuTamanho.getItems().add(itemTamanho);
        }

        // Itens de alinhamento
        MenuItem itemEsquerda = new MenuItem("Alinhar à Esquerda");
        MenuItem itemCentralizado = new MenuItem("Centralizado");
        MenuItem itemDireita = new MenuItem("Alinhar à Direita");
        MenuItem itemJustificado = new MenuItem("Justificado");

        itemEsquerda.setOnAction(e -> aplicarAlinhamento("left"));
        itemCentralizado.setOnAction(e -> aplicarAlinhamento("center"));
        itemDireita.setOnAction(e -> aplicarAlinhamento("right"));
        itemJustificado.setOnAction(e -> aplicarAlinhamento("justify"));

        menuAlinhamento.getItems().addAll(itemEsquerda, itemCentralizado, itemDireita, itemJustificado);

        // Opções de marca-texto
        String[] cores = {"Amarelo", "Verde", "Azul", "Rosa", "Laranja", "Remover"};
        for (String cor : cores) {
            MenuItem itemCor = new MenuItem(cor);
            itemCor.setOnAction(e -> aplicarMarcaTexto(cor));
            menuMarcaTexto.getItems().add(itemCor);
        }

        menuFormatar.getItems().addAll(menuFonte, menuTamanho, menuAlinhamento, menuMarcaTexto);
        
        // Menu Ajuda
        Menu menuAjuda = new Menu("Ajuda");
        MenuItem sobreItem = new MenuItem("Sobre");
        sobreItem.setOnAction(e -> { if (onSobre != null) onSobre.run(); });
        menuAjuda.getItems().add(sobreItem);
        
        // Adiciona todos os menus à barra
        menuBar.getMenus().addAll(menuArquivo, menuEditar, menuFormatar, menuVisualizar, menuAjuda);
        return menuBar;
    }

    /**
     * Cria e configura a barra de ferramentas
     */
    private ToolBar criarToolBar() {
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-padding: 5; -fx-spacing: 5;");

        // ComboBox para seleção de fonte
        comboFontes = new ComboBox<>();
        comboFontes.getItems().addAll(fontesPermitidas);
        comboFontes.setValue("Times New Roman");
        comboFontes.setPrefWidth(120);
        comboFontes.setOnAction(e -> {
            if (comboFontes.getValue() != null) {
                aplicarFonte(comboFontes.getValue());
            }
        });

        // ComboBox para seleção de tamanho
        comboTamanhos = new ComboBox<>();
        for (int tamanho : tamanhosFonte) {
            comboTamanhos.getItems().add(tamanho);
        }
        comboTamanhos.setValue(12);
        comboTamanhos.setPrefWidth(60);
        comboTamanhos.setOnAction(e -> {
            if (comboTamanhos.getValue() != null) {
                aplicarTamanhoFonte(comboTamanhos.getValue());
            }
        });

        // Botões de estilo de texto
        btnNegrito = new ToggleButton("N");
        btnNegrito.setStyle("-fx-font-weight: bold;");
        btnNegrito.setTooltip(new Tooltip("Negrito"));
        btnNegrito.setOnAction(e -> toggleEstiloTexto("-fx-font-weight", "bold", btnNegrito));

        btnItalico = new ToggleButton("I");
        btnItalico.setStyle("-fx-font-style: italic;");
        btnItalico.setTooltip(new Tooltip("Itálico"));
        btnItalico.setOnAction(e -> toggleEstiloTexto("-fx-font-style", "italic", btnItalico));

        btnSublinhado = new ToggleButton("S");
        btnSublinhado.setStyle("-fx-underline: true;");
        btnSublinhado.setTooltip(new Tooltip("Sublinhado"));
        btnSublinhado.setOnAction(e -> toggleEstiloTexto("-fx-underline", "true", btnSublinhado));

        // Grupo de botões para alinhamento (apenas um selecionado por vez)
        grupoAlinhamento = new ToggleGroup();

        btnEsquerda = new ToggleButton("⫷");
        btnEsquerda.setToggleGroup(grupoAlinhamento);
        btnEsquerda.setTooltip(new Tooltip("Alinhar à Esquerda"));
        btnEsquerda.setOnAction(e -> aplicarAlinhamento("left"));

        btnCentralizado = new ToggleButton("⫸⫷");
        btnCentralizado.setToggleGroup(grupoAlinhamento);
        btnCentralizado.setTooltip(new Tooltip("Centralizado"));
        btnCentralizado.setOnAction(e -> aplicarAlinhamento("center"));

        btnDireita = new ToggleButton("⫸");
        btnDireita.setToggleGroup(grupoAlinhamento);
        btnDireita.setTooltip(new Tooltip("Alinhar à Direita"));
        btnDireita.setOnAction(e -> aplicarAlinhamento("right"));

        btnJustificado = new ToggleButton("☰");
        btnJustificado.setToggleGroup(grupoAlinhamento);
        btnJustificado.setTooltip(new Tooltip("Justificado"));
        btnJustificado.setSelected(true);  // Justificado é o padrão
        btnJustificado.setOnAction(e -> aplicarAlinhamento("justify"));

        // ComboBox para marca-texto
        comboMarcaTexto = new ComboBox<>();
        comboMarcaTexto.getItems().addAll("Marca Texto", "Amarelo", "Verde", "Azul", "Rosa", "Laranja", "Remover");
        comboMarcaTexto.setValue("Marca Texto");
        comboMarcaTexto.setPrefWidth(100);
        comboMarcaTexto.setOnAction(e -> {
            if (comboMarcaTexto.getValue() != null && !comboMarcaTexto.getValue().equals("Marca Texto")) {
                aplicarMarcaTexto(comboMarcaTexto.getValue());
                comboMarcaTexto.setValue("Marca Texto");  // Reseta para o texto padrão
            }
        });

        // Adiciona todos os componentes à barra de ferramentas
        toolbar.getItems().addAll(
            new Label("Fonte:"), comboFontes,
            new Separator(),
            new Label("Tamanho:"), comboTamanhos,
            new Separator(),
            btnNegrito, btnItalico, btnSublinhado,
            new Separator(),
            btnEsquerda, btnCentralizado, btnDireita, btnJustificado,
            new Separator(),
            new Label("Marca:"), comboMarcaTexto
        );
        return toolbar;
    }

    /**
     * Aplica estilo de fonte ou tamanho ao texto selecionado
     */
    private void aplicarEstiloFonteOuTamanho(String propriedade, String valor) {
        int start = textArea.getSelection().getStart();
        int end = textArea.getSelection().getEnd();
        
        if (start == end) {
            String estiloAtual = textArea.getStyle();
            String novoEstilo = atualizarEstiloCSS(estiloAtual, propriedade, valor);
            textArea.setStyle(novoEstilo);
            return;
        }
        
        for (int i = start; i < end; i++) {
            String estiloAtual = textArea.getStyleOfChar(i);
            String novoEstilo = atualizarEstiloCSS(estiloAtual, propriedade, valor);
            textArea.setStyle(i, i + 1, novoEstilo);
        }
        atualizarEstadoToolbar();
    }

    private String atualizarEstiloCSS(String estiloAtual, String propriedade, String valor) {
        String regex = propriedade + ":\\s*[^;]*;?";
        String estiloLimpo = estiloAtual.replaceAll(regex, "").trim();
        String novaPropriedade = propriedade + ": " + valor + ";";
        
        if (estiloLimpo.isEmpty()) {
            return novaPropriedade;
        } else {
            return estiloLimpo + " " + novaPropriedade;
        }
    }

    /**
     * Atualiza o estilo padrão da área de texto para novo texto
     */
    private void atualizarEstiloPadrao(String propriedade, String valor) {
        String estiloAtual = textArea.getStyle();
        // Remove a propriedade existente
        String estiloLimpo = estiloAtual.replaceAll(propriedade + ":\\s*[^;]*;?", "").trim();
        // Adiciona a nova propriedade
        String novaPropriedade = propriedade + ": " + valor + ";";
        String novoEstilo = estiloLimpo.isEmpty() ? novaPropriedade : estiloLimpo + " " + novaPropriedade;
        textArea.setStyle(novoEstilo);
    }

    private void aplicarFonte(String fonte) {
        // Aplica à seleção existente
        aplicarEstiloFonteOuTamanho("-fx-font-family", "'" + fonte + "'");
        // Define como padrão para novo texto
        atualizarEstiloPadrao("-fx-font-family", "'" + fonte + "'");
        // Atualiza a ComboBox
        comboFontes.setValue(fonte);
    }

    private void aplicarTamanhoFonte(int tamanho) {
        // Aplica à seleção existente
        aplicarEstiloFonteOuTamanho("-fx-font-size", tamanho + "pt");
        // Define como padrão para novo texto
        atualizarEstiloPadrao("-fx-font-size", tamanho + "pt");
        // Atualiza a ComboBox
        comboTamanhos.setValue(tamanho);
    }

    private void aplicarAlinhamento(String alinhamento) {
        int start = textArea.getSelection().getStart();
        if (textArea.getLength() > 0) {
            try {
                TwoDimensional.Position pos = textArea.offsetToPosition(start, TwoDimensional.Bias.Backward);
                int paragraph = pos.getMajor();
                textArea.setParagraphStyle(paragraph, "-fx-text-alignment: " + alinhamento + ";");
                atualizarEstadoAlinhamento(alinhamento);
            } catch (Exception e) {
                System.err.println("Erro ao aplicar alinhamento: " + e.getMessage());
            }
        }
    }

    private void toggleEstiloTexto(String propriedade, String valor, ToggleButton botao) {
        int start = textArea.getSelection().getStart();
        int end = textArea.getSelection().getEnd();
        
        if (start >= end) return;
        
        boolean hasStyle = false;
        for (int i = start; i < end; i++) {
            if (textArea.getStyleOfChar(i).contains(propriedade)) {
                hasStyle = true;
                break;
            }
        }

        String acao = hasStyle ? "remover" : "adicionar";
        
        for (int i = start; i < end; i++) {
            String estiloAtual = textArea.getStyleOfChar(i).toString();
            String novoEstilo;
            
            if (acao.equals("remover")) {
                novoEstilo = estiloAtual.replaceAll(propriedade + ":\\s*[^;]*;?", "").trim();
            } else {
                novoEstilo = estiloAtual + " " + propriedade + ": " + valor + ";";
            }
            
            textArea.setStyle(i, i + 1, novoEstilo.trim());
        }
        atualizarEstadoToolbar();
    }

    private void aplicarMarcaTexto(String cor) {
        int start = textArea.getSelection().getStart();
        int end = textArea.getSelection().getEnd();
        
        if (start >= end) return;
        
        String estilo = cor.equals("Remover") ? "" : "-rtfx-background-color: " + obterCorHex(cor) + ";";
        
        for (int i = start; i < end; i++) {
            String estiloAtual = textArea.getStyleOfChar(i).toString();
            String estiloLimpo = estiloAtual.replaceAll("-rtfx-background-color:\\s*#[A-Fa-f0-9]{6};?", "").trim();
            String novoEstilo = estilo.isEmpty() ? estiloLimpo : 
                (estiloLimpo.isEmpty() ? estilo : estiloLimpo + " " + estilo);
            
            textArea.setStyle(i, i + 1, novoEstilo);
        }
        atualizarEstadoToolbar();
    }

    private String obterCorHex(String cor) {
        switch (cor) {
            case "Amarelo": return "#FFFF00";
            case "Verde": return "#90EE90";
            case "Azul": return "#ADD8E6";
            case "Rosa": return "#FFB6C1";
            case "Laranja": return "#FFA07A";
            default: return "#FFFFFF";
        }
    }

    private void atualizarEstadoToolbar() {
        int start = textArea.getSelection().getStart();
        if (textArea.getLength() > 0 && start < textArea.getLength()) {
            int pos = Math.min(start, textArea.getLength() - 1);
            String estilo = textArea.getStyleOfChar(pos).toString();
            btnNegrito.setSelected(estilo.contains("-fx-font-weight: bold"));
            btnItalico.setSelected(estilo.contains("-fx-font-style: italic"));
            btnSublinhado.setSelected(estilo.contains("-fx-underline: true"));
        } else {
            btnNegrito.setSelected(false);
            btnItalico.setSelected(false);
            btnSublinhado.setSelected(false);
        }

        boolean disableControls = textArea.getLength() == 0;
        comboFontes.setDisable(disableControls);
        comboTamanhos.setDisable(disableControls);
        btnNegrito.setDisable(disableControls);
        btnItalico.setDisable(disableControls);
        btnSublinhado.setDisable(disableControls);
        btnEsquerda.setDisable(disableControls);
        btnCentralizado.setDisable(disableControls);
        btnDireita.setDisable(disableControls);
        btnJustificado.setDisable(disableControls);
        comboMarcaTexto.setDisable(disableControls);
    }

    private void atualizarEstadoAlinhamento(String alinhamento) {
        btnEsquerda.setSelected("left".equals(alinhamento));
        btnCentralizado.setSelected("center".equals(alinhamento));
        btnDireita.setSelected("right".equals(alinhamento));
        btnJustificado.setSelected("justify".equals(alinhamento));
    }

    private void novoDocumento() {
        textArea.clear();
        currentFile = null;
        configurarFormatoPadrao();
    }

    private void abrirArquivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir Documento de Texto");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos de Texto", "*.txt"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                textArea.replaceText(content.toString());
                currentFile = file;
                configurarFormatoPadrao();
            } catch (IOException e) {
                mostrarErro("Erro ao abrir arquivo", "Não foi possível ler o arquivo.");
            }
        }
    }

    private void salvarArquivo() {
        if (currentFile == null) {
            salvarComoArquivo();
        } else {
            salvarParaArquivo(currentFile);
        }
    }

    private void salvarComoArquivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Documento");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos de Texto", "*.txt"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            salvarParaArquivo(file);
            currentFile = file;
        }
    }

    private void salvarParaArquivo(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(textArea.getText());
        } catch (IOException e) {
            mostrarErro("Erro ao salvar arquivo", "Não foi possível salvar o arquivo.");
        }
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public Node getView() { return root; }
    public String getText() { return textArea.getText(); }
    
    /**
     * Alterna o tema da área de texto entre claro e escuro
     */
    private void alternarTemaTexto() {
        isDarkMode = !isDarkMode;
        aplicarTemaTexto(isDarkMode);
    }
    
    /**
     * Aplica o tema (escuro ou claro) APENAS à área de texto
     */
    private void aplicarTemaTexto(boolean darkMode) {
        String backgroundColor = darkMode ? "#1e1e1e" : "white";
        String textColor = darkMode ? "white" : "black";

        // Define estilo base para novo texto
        String estiloBase = "-fx-font-family: '" + comboFontes.getValue() + "'; -fx-font-size: " + comboTamanhos.getValue() + "pt; " +
                            "-fx-control-inner-background: " + backgroundColor + "; -fx-text-fill: " + textColor + ";";
        
        // Aplica estilo base ao componente (para novo texto)
        textArea.setStyle(estiloBase);

        // Aplica estilo a cada parágrafo existente
        int numParagraphs = textArea.getParagraphs().size();
        for (int i = 0; i < numParagraphs; i++) {
            String estiloParagrafo = "-fx-control-inner-background: " + backgroundColor + "; -fx-text-fill: " + textColor + ";";
            textArea.setParagraphStyle(i, estiloParagrafo);
        }

        // Se o texto estiver vazio, aplica estilo base
        if (textArea.getLength() == 0) {
            textArea.setStyle(estiloBase);
        }
    }
}