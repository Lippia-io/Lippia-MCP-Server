# 🚀 Getting Started

> Complete guide to configure and use the AI Agent + Dual MCP Servers architecture

**Follow these steps to deploy the MCP servers and start automating with artificial intelligence.**

---

## Navigation

- **[🏠 Home](index.md)**
- **[▶️ Getting Started](getting-started.md)** *(current page)*

---

## 📋 Prerequisites

### ☕ Java Development Kit
- **Version**: Java 17 or higher
- **Verify**: `java --version`

### 📦 Apache Maven
- **Version**: Maven 3.8+ recommended
- **Verify**: `mvn --version`

### 🌐 Web Browsers
- **Supported**: Chrome, Firefox
- **WebDrivers**: Downloaded automatically

---

## 💾 Installation

Choose your operating system:

### 🪟 Windows

#### 1️⃣ Clone the Repository
Open PowerShell as Administrator and clone the Lippia MCP Server project.

```bash
git clone https://gitlab.crowdaronline.com/lippia/products/lippia-framework/mcp-server/lippia-mcp-server.git && cd "$(basename "$_" .git)"
```

> ✅ **Expected result**: Project cloned successfully and you're now in the project directory.

#### 2️⃣ Compile the Project
Use Maven to compile and package the Lippia MCP server.

```bash
mvn clean package
```

> ✅ **Expected result**: The file `target\mcp-server-1.0-SNAPSHOT.jar` is generated

#### 3️⃣ Install Cursor via CLI
Install Cursor AI Editor using Windows Package Manager (winget).

```bash
winget install Cursor
```

> ⚠️ **Alternative**: If winget is not available, download from [cursor.sh](https://cursor.sh)
> 
> ✅ **Verify installation**: Type `cursor --version` in PowerShell

#### 4️⃣ Configure MCP Servers in Cursor
Open Cursor settings and configure the MCP servers in the configuration JSON.

> ⚠️ **Location**: `%APPDATA%\Cursor\User\globalStorage\cursor.settings\settings.json`

**Steps to configure:**
1. Open Cursor
2. Press `Ctrl+Shift+P` and search for "Preferences: Open Settings (JSON)"
3. Add the following MCP servers configuration:

```json
{
  "mcpServers": {
    "lippia": {
      "command": "java",
      "args": ["-jar", "/home/your-username/path/to/project/target/mcp-server-1.0-SNAPSHOT.jar"]
    }
  }
}
```

> ⚠️ **Important**: Replace `C:\path\to\your\project` with your actual project path using double backslashes.

#### 5️⃣ Verify Connectivity
Restart Cursor and test that Lippia MCP Server is working correctly.

**Lippia MCP Server Tools:**
```
# Building & Reporting
- mcp_lippia_create_feature
- mcp_lippia_create_test
- mcp_lippia_execute_test
- mcp_lippia_send_report

# Navigation & Actions
- mcp_lippia_open_browser
- mcp_lippia_close_browser
- mcp_lippia_navigate_to
- mcp_lippia_click
- mcp_lippia_type
- mcp_lippia_get_text
- mcp_lippia_get_page_title
- mcp_lippia_get_page_source
- mcp_lippia_get_current_url
- mcp_lippia_screenshot

# Waits
- mcp_lippia_wait_clickable
- mcp_lippia_wait_visibility
- mcp_lippia_wait_visibilities
- mcp_lippia_wait_invisibility
- mcp_lippia_wait_invisibilities
- mcp_lippia_wait_presence
- mcp_lippia_wait_presences

# Verifications
- mcp_lippia_is_enabled
- mcp_lippia_is_selected
- mcp_lippia_is_present
- mcp_lippia_is_visible
```

> ✅ **Test**: In Cursor chat, type: "Show me available MCP tools" to verify both servers are connected.

---

### 🍎 macOS

#### 1️⃣ Clone the Repository
Open Terminal and clone the Lippia MCP Server project.

```bash
git clone https://gitlab.crowdaronline.com/lippia/products/lippia-framework/mcp-server/lippia-mcp-server.git && cd "$(basename "$_" .git)"
```

> ✅ **Expected result**: Project cloned successfully and you're now in the project directory.

#### 2️⃣ Compile the Project
Use Maven to compile and package the Lippia MCP server.

```bash
mvn clean package
```

> ✅ **Expected result**: The file `target/mcp-server-1.0-SNAPSHOT.jar` is generated

#### 3️⃣ Install Cursor via CLI
Install Cursor AI Editor using Homebrew.

```bash
# Install Homebrew if you don't have it
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Cursor
brew install --cask cursor
```

> ℹ️ **Alternative**: If you prefer, you can download from [cursor.sh](https://cursor.sh)
> 
> ✅ **Verify installation**: Type `cursor --version` in Terminal

#### 4️⃣ Configure MCP Servers in Cursor
Open Cursor settings and configure the MCP servers in the configuration JSON.

> ⚠️ **Location**: `~/Library/Application Support/Cursor/User/globalStorage/cursor.settings/settings.json`

**Steps to configure:**
1. Open Cursor
2. Press `⌘+Shift+P` and search for "Preferences: Open Settings (JSON)"
3. Add the following MCP servers configuration:

```json
{
  "mcpServers": {
    "lippia": {
      "command": "java",
      "args": ["-jar", "/home/your-username/path/to/project/target/mcp-server-1.0-SNAPSHOT.jar"]
    }
  }
}
```

> ⚠️ **Important**: Replace `/Users/your-username/path/to/project` with your actual project path.

#### 5️⃣ Verify Connectivity
Restart Cursor and test that Lippia MCP Server is working correctly.

> ✅ **Test**: In Cursor chat, type: "Show me available MCP tools" to verify both servers are connected.

---

### 🐧 Linux

#### 1️⃣ Clone the Repository
Open Terminal and clone the Lippia MCP Server project.

```bash
git clone https://gitlab.crowdaronline.com/lippia/products/lippia-framework/mcp-server/lippia-mcp-server.git && cd "$(basename "$_" .git)"
```

> ✅ **Expected result**: Project cloned successfully and you're now in the project directory.

#### 2️⃣ Compile the Project
Use Maven to compile and package the Lippia MCP server.

```bash
mvn clean package
```

> ✅ **Expected result**: The file `target/mcp-server-1.0-SNAPSHOT.jar` is generated

#### 3️⃣ Install Cursor via CLI
Install Cursor AI Editor using your preferred package manager.

**Option 1: Ubuntu/Debian (APT)**
```bash
# Download and install Cursor
wget -qO- https://download.todesktop.com/200823hxqfhhbr/linux | sudo apt-key add -
echo "deb [arch=amd64] https://download.todesktop.com/200823hxqfhhbr/linux/ stable main" | sudo tee /etc/apt/sources.list.d/cursor.list
sudo apt update && sudo apt install cursor
```

**Option 2: Snap Package (Universal)**
```bash
sudo snap install cursor --classic
```

**Option 3: AppImage (Portable)**
```bash
# Download AppImage from cursor.sh
wget https://downloader.cursor.sh/linux/appImage/x64 -O cursor.AppImage
chmod +x cursor.AppImage
./cursor.AppImage
```

> ℹ️ **Alternative**: You can also download from [cursor.sh](https://cursor.sh)
> 
> ✅ **Verify installation**: Type `cursor --version` in Terminal

#### 4️⃣ Configure MCP Servers in Cursor
Open Cursor settings and configure the MCP servers in the configuration JSON.

> ⚠️ **Location**: `~/.config/Cursor/User/globalStorage/cursor.settings/settings.json`

**Steps to configure:**
1. Open Cursor
2. Press `Ctrl+Shift+P` and search for "Preferences: Open Settings (JSON)"
3. Add the following MCP servers configuration:

```json
{
  "mcpServers": {
    "lippia": {
      "command": "java",
      "args": ["-jar", "/home/your-username/path/to/project/target/mcp-server-1.0-SNAPSHOT.jar"]
    }
  }
}
```

> ⚠️ **Important**: Replace `/home/your-username/path/to/project` with your actual project path.

#### 5️⃣ Verify Connectivity
Restart Cursor and test that Lippia MCP Server is working correctly.

> ✅ **Test**: In Cursor chat, type: "Show me available MCP tools" to verify both servers are connected.

---

## 🤖 Usage Example

### Complete Autonomous Flow

**Prompt:**

```
Hello, please follow these steps:

1. Read and understand the proposed User Story

User Story: Successful Product Purchase on SauceDemo
Title: As a logged-in user, I want to be able to successfully purchase a product to complete my order and receive it.

Acceptance Criteria:

- The user must be able to log in with valid credentials.
- The user must be able to add at least one product to the cart from the store.
- The user must be able to access the cart and verify the selected products.
- The user must be able to proceed to checkout and enter personal information (name, surname, postal code).
- The user must be able to continue to the order summary and confirm the purchase.
- Upon completion, the system must display a successful purchase confirmation message: "THANK YOU FOR YOUR ORDER".

Test Scenario (Happy Path):

- Navigate to https://www.saucedemo.com.
- Log in as standard_user / secret_sauce.
- Add the "Sauce Labs Backpack" product to the cart.
- Go to the cart and click on Checkout.

Complete the personal information:

- First Name: John
- Last Name: Doe
- Zip Code: 12345

- Click on Continue and then on Finish.
- Verify the purchase confirmation message.

2. Lippia will open the browser and navigate autonomously through the application to map the flow and actions.
3. Generate the feature files based on the User Story, leveraging the navigation performed by Lippia.
4. Create the tests in Java with Lippia MCP.
5. Execute the tests in Java with Lippia MCP.
```

*The AI Agent coordinates the entire flow using Lippia MCP Server for navigation and testing.*

---

## 🛠️ Troubleshooting

### Setup Issues

#### TBC
TBC Paragraph

```
!!codeblock
```

#### TBC
TBC Paragraph

```
!!codeblock
```

#### Error: "MCP Connection Failed"
!!textblock

---

## 🚀 Next Steps

### 📚 Documentation
Explore the complete API documentation and advanced examples.
**[View Architecture](index.md)**

### 💻 Contribute
Contribute to the project and share your improvements with the community.
**[GitHub Repo](#)**

### 💬 Support
Need help? Join our developer community.
**[Community](#)**

---

## 🏠 Footer

**Getting Started - AI Agent + Dual MCP Servers** - Ready to Automate

*Automated Testing • Autonomous Navigation • Artificial Intelligence*
