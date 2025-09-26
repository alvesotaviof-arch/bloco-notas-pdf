#!/bin/bash

echo "=== BRADYPUS TORQUATUS PDF ==="
echo "Iniciando aplicação..."

cd "/home/klawz/Área de trabalho/PdfNb/bloco-notas-pdf"

# Verificar se os arquivos CSS existem
if [ ! -f "src/main/resources/light-mode.css" ] || [ ! -f "src/main/resources/dark-mode.css" ]; then
    echo "Criando arquivos CSS..."
    mkdir -p src/main/resources
    
    # Criar light-mode.css
    cat > src/main/resources/light-mode.css << 'CSSEOF'
/* Tema Claro - Bradypus Torquatus Pdf */
.root { -fx-background-color: #f5f5f5; }
.border-pane { -fx-background-color: #f5f5f5; }
.split-pane { -fx-background-color: #f5f5f5; }
.split-pane-divider { -fx-background-color: #cccccc; }
.tool-bar { -fx-background-color: #e8e8e8; }
.menu-bar { -fx-background-color: #e8e8e8; }
.menu-bar .menu { -fx-background-color: #e8e8e8; -fx-text-fill: black; }
.menu-item { -fx-background-color: white; -fx-text-fill: black; }
.text-area, .inline-css-text-area { 
    -fx-control-inner-background: white; 
    -fx-text-fill: black; 
    -fx-font-family: "Times New Roman"; 
    -fx-font-size: 12pt; 
}
.label { -fx-text-fill: black; }
.button, .toggle-button { -fx-background-color: #f0f0f0; -fx-text-fill: black; }
.scroll-pane { -fx-background-color: white; }
.vbox { -fx-background-color: white; }
.dialog-pane { -fx-background-color: white; }
.dialog-pane .label { -fx-text-fill: black; }
.separator { -fx-background-color: #cccccc; }
CSSEOF

    # Criar dark-mode.css
    cat > src/main/resources/dark-mode.css << 'CSSEOF'
/* Tema Escuro - APENAS para a área de texto do editor */
.root { -fx-background-color: #f5f5f5; }
.border-pane { -fx-background-color: #f5f5f5; }
.split-pane { -fx-background-color: #f5f5f5; }
.split-pane-divider { -fx-background-color: #cccccc; }
.tool-bar { -fx-background-color: #e8e8e8; }
.menu-bar { -fx-background-color: #e8e8e8; }
.menu-bar .menu { -fx-background-color: #e8e8e8; -fx-text-fill: black; }
.menu-item { -fx-background-color: white; -fx-text-fill: black; }
.text-area, .inline-css-text-area { 
    -fx-control-inner-background: #1e1e1e; 
    -fx-text-fill: white; 
    -fx-font-family: "Times New Roman"; 
    -fx-font-size: 12pt; 
}
.label { -fx-text-fill: black; }
.button, .toggle-button { -fx-background-color: #f0f0f0; -fx-text-fill: black; }
.scroll-pane { -fx-background-color: white; }
.vbox { -fx-background-color: white; }
.dialog-pane { -fx-background-color: white; }
.dialog-pane .label { -fx-text-fill: black; }
.separator { -fx-background-color: #cccccc; }
CSSEOF

    echo "✅ Arquivos CSS criados!"
fi

# Compilar
echo "Compilando projeto..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "✅ Compilação bem-sucedida!"
    echo ""
    echo "🎯 INICIANDO APLICAÇÃO BRADYPUS TORQUATUS PDF"
    echo "=============================================="
    echo "Recursos disponíveis:"
    echo "• Editor de texto com formatação avançada"
    echo "• Visualizador PDF integrado" 
    echo "• Temas claro/escuro (apenas no editor)"
    echo "• Menu Sobre com informações do projeto"
    echo "=============================================="
    mvn javafx:run
else
    echo "❌ Erro na compilação."
    echo "Executando com detalhes:"
    mvn clean compile -e
fi
