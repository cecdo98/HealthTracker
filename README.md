# HealthTracker

O **HealthTracker** é uma aplicação Android desenvolvida em Kotlin com Jetpack Compose, focada na gestão de bem-estar pessoal. Permite aos utilizadores monitorizar dados biométricos, definir metas de saúde e gerir notificações de lembretes.

---

## Funcionalidades Implementadas

### 1. Gestão de Perfil do Utilizador (ProfileScreen)

- **Dados Pessoais**  
  Registo de nome, apelido, idade, peso e altura.

- **Cálculo de IMC Automático**  
  Cálculo em tempo real do Índice de Massa Corporal (IMC), incluindo:
  - Indicador de categoria (Abaixo do peso, Peso normal, Sobrepeso, Obesidade)
  - Barra de progresso visual com cores dinâmicas conforme o estado de saúde
  - Conselhos de saúde baseados no resultado

- **Entrada de Voz**  
  Suporte para preenchimento de campos através de comandos de voz (Speech-to-Text).

- **Foto de Perfil**  
  Possibilidade de selecionar uma imagem da galeria do dispositivo, com persistência de permissões de URI.

- **Suporte Multi-Unidade**  
  Alternância entre sistema métrico e imperial.

---

### 2. Configurações e Metas (SettingsScreen)

- **Metas Diárias**
  - Passos diários
  - Consumo de água (em ml)

- **Sistema de Notificações**
  - Lembretes recorrentes para beber água e registar o estado de humor
  - Frequências configuráveis (30 min, 1 h, 2 h, 4 h, 8 h) através de `AlarmManager`

- **Personalização de Interface**
  - Alternância entre Modo Claro (Light Mode) e Modo Escuro (Dark Mode)

- **Integração**
  - Opção para ligar a conta Google para sincronização de dados

---

## Tecnologias Utilizadas

- **Linguagem:** Kotlin  
- **UI:** Jetpack Compose (arquitetura declarativa)  
- **Arquitetura:** MVVM (Model-View-ViewModel) com `UserViewModel`  
- **Persistência:** StateFlow para gestão de estado reativo e preferências do utilizador  

### Bibliotecas de Terceiros
- **Coil** – carregamento e processamento eficiente de imagens

### Componentes Android
- `ActivityResultContracts` – seleção de fotos e reconhecimento de voz  
- `BroadcastReceiver` & `AlarmManager` – gestão de notificações em background  
- `NotificationManager` – comunicação com o utilizador  

---

## Estrutura do Projeto (Destaques)

- `pages/`  
  Contém as principais UI Screens (ProfileScreen, SettingsScreen)

- `services/user/`  
  Lógica de negócio e gestão de estado no `UserViewModel`

- `ui/theme/`  
  Definições de cores, temas (Light/Dark) e tipografia personalizada

- `NotificationReceiver`  
  Classe responsável por intercetar alarmes e disparar notificações ao utilizador
