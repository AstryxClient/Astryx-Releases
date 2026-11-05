package com.astryx.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class AstryxMultiplayer extends GuiScreen implements GuiYesNoCallback {

    private static final int PURPLE = 0xFF9B78E8;
    private static final int BLUE = 0xFF648BFA;

    private static final int PARTICLE_COUNT = 65;
    private static final int CARD_HEIGHT = 26;
    private static final int CARD_GAP = 3;

    private static final float PING_SCALE = 0.16F;
    private static final long RETRY_DELAY = 2500L;

    private final GuiScreen parent;
    private final Random random = new Random();
    private final Particle[] particles = new Particle[PARTICLE_COUNT];
    private final List<UiButton> buttons = new ArrayList<UiButton>();

    private final Map<ServerData, Long> retryTimes =
            new HashMap<ServerData, Long>();

    private final Map<String, ResourceLocation> serverIconTextures =
            new HashMap<String, ResourceLocation>();

    private ServerList serverList;
    private OldServerPinger serverPinger;

    private MainMenu.CustomFont regularFont;
    private MainMenu.CustomFont boldFont;

    private GuiTextField serverNameField;
    private GuiTextField serverAddressField;

    private int selectedIndex = -1;
    private int listTop;
    private int listBottom;

    private float scrollOffset;
    private float animation;

    private long lastFrame;
    private long lastClickTime;

    private int lastClickedIndex = -1;

    private boolean editingServer;
    private boolean creatingServer;
    private boolean confirmingDelete;

    /*
     * Startup is deliberately split into two stages.
     *
     * initGui() now performs only lightweight setup. The first frame
     * displays immediately, and the expensive initialisation happens
     * during the following updateScreen() call.
     */
    private boolean initialized;
    private boolean firstFrameRendered;

    private int editingIndex = -1;

    private String lastServerName = "";
    private String lastServerAddress = "";

    private String statusMessage = "";
    private long statusMessageTime;

    public AstryxMultiplayer(GuiScreen parent) {
        this.parent = parent;
    }

    // ============================================================
    // RESPONSIVE HEADER BUTTON LAYOUT
    // ============================================================

    private int getHeaderButtonHeight() {
        return clamp(height / 15, 28, 32);
    }

    private int getHeaderButtonY() {
        return Math.max(4, height / 160);
    }

    private int getHeaderButtonRightMargin() {
        return clamp(width / 65, 7, 18);
    }

    private int getHeaderButtonGap() {
        return clamp(width / 180, 4, 9);
    }

    private int getJoinLastWidth() {
        return clamp(width / 4, 110, 150);
    }

    private int getRefreshWidth() {
        return clamp(width / 9, 55, 70);
    }

    private int getJoinLastX() {
        return width - getHeaderButtonRightMargin() - getJoinLastWidth();
    }

    private int getJoinLastY() {
        return getHeaderButtonY();
    }

    private int getRefreshX() {
        return getJoinLastX() - getHeaderButtonGap() - getRefreshWidth();
    }

    private int getRefreshY() {
        return getHeaderButtonY();
    }

    private int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    // ============================================================
    // INITIALIZATION
    // ============================================================

    @Override
    public void initGui() {
        /*
         * Do not construct CustomFont, load the server list, read
         * properties, or create textures here. Those operations were
         * responsible for the delay before the screen appeared.
         */
        initialized = false;
        firstFrameRendered = false;

        lastFrame = System.currentTimeMillis();

        updateListBounds();
        buttons.clear();

        editingServer = false;
        creatingServer = false;
        confirmingDelete = false;

        serverNameField = null;
        serverAddressField = null;
    }

    private void initializeScreen() {
        if (initialized) {
            return;
        }

        /*
         * All OpenGL-related font and texture work stays on the
         * Minecraft render thread.
         */
        regularFont = new MainMenu.CustomFont(false);
        boldFont = new MainMenu.CustomFont(true);

        serverList = new ServerList(mc);
        serverList.loadServerList();

        updateListBounds();
        clampScroll();

        createParticles();
        loadLastServer();

        /*
         * Mark the screen as initialized BEFORE rebuilding buttons.
         * Otherwise rebuildButtons() exits without creating them.
         */
        initialized = true;

        rebuildButtons();

        resetRenderState();
        enableSmoothRendering();

        /*
         * Server pings are started only after the screen has already
         * been displayed.
         */
        refreshServers();
    }

    private void updateListBounds() {
        listTop = Math.max(45, height / 9);
        listBottom = Math.max(listTop + 30, height - 52);
    }

    @Override
    public void onGuiClosed() {
        saveLastServer();

        if (serverPinger != null) {
            serverPinger.clearPendingNetworks();
            serverPinger = null;
        }

        retryTimes.clear();
        serverIconTextures.clear();

        disposeFonts();

        initialized = false;
        firstFrameRendered = false;

        resetRenderState();
    }

    private void disposeFonts() {
        if (regularFont != null) {
            regularFont.dispose();
            regularFont = null;
        }

        if (boldFont != null) {
            boldFont.dispose();
            boldFont = null;
        }
    }

    private void createParticles() {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles[i] = new Particle(
                    random.nextFloat() * width,
                    random.nextFloat() * height,
                    0.12F + random.nextFloat() * 0.25F,
                    0.08F + random.nextFloat() * 0.18F,
                    random.nextBoolean(),
                    0.4F + random.nextFloat() * 0.6F
            );
        }
    }

    // ============================================================
    // SERVER DATA
    // ============================================================

    private int getServerCount() {
        return serverList == null ? 0 : serverList.countServers();
    }

    private boolean hasSelection() {
        return selectedIndex >= 0 && selectedIndex < getServerCount();
    }

    private ServerData getSelectedServer() {
        return hasSelection()
                ? serverList.getServerData(selectedIndex)
                : null;
    }

    private void saveServerList() {
        if (serverList != null) {
            serverList.saveServerList();
        }
    }

    private void refreshServers() {
        if (serverList == null) {
            return;
        }

        if (serverPinger != null) {
            serverPinger.clearPendingNetworks();
        }

        List<ServerData> refreshed = new ArrayList<ServerData>();

        for (int i = 0; i < getServerCount(); i++) {
            ServerData old = serverList.getServerData(i);

            ServerData replacement = new ServerData(
                    old.serverName,
                    old.serverIP,
                    false
            );

            replacement.setBase64EncodedIconData(
                    old.getBase64EncodedIconData()
            );

            resetServerStatus(replacement);
            refreshed.add(replacement);
        }

        replaceServerList(refreshed);

        serverPinger = new OldServerPinger();

        retryTimes.clear();

        setStatus("REFRESHING SERVER STATUS");
    }

    private void resetServerStatus(ServerData server) {
        server.field_78841_f = false;
        server.pingToServer = -1L;
        server.populationInfo = "";
        server.serverMOTD = "";
        server.gameVersion = "";
        server.version = 0;
    }

    private void updateServerPings() {
        if (serverPinger == null || serverList == null) {
            return;
        }

        serverPinger.pingPendingNetworks();

        for (int i = 0; i < getServerCount(); i++) {
            ServerData server = serverList.getServerData(i);

            if (server == null || server.field_78841_f) {
                continue;
            }

            server.field_78841_f = true;

            try {
                serverPinger.ping(server);
            } catch (UnknownHostException exception) {
                server.pingToServer = -1L;
                server.serverMOTD = "Can't connect to server.";
                server.populationInfo = "";
            }
        }
    }

    private boolean isConnectionFailure(ServerData server) {
        if (server.serverMOTD == null) {
            return false;
        }

        String motd = server.serverMOTD.trim();

        return motd.equalsIgnoreCase("Can't connect to server.")
                || motd.equalsIgnoreCase("Can't resolve hostname")
                || motd.equalsIgnoreCase("Unable to resolve server address");
    }

    // ============================================================
    // SERVER REORDERING
    // ============================================================

    private void moveServer(int index, int direction) {
        if (index < 0 || index >= getServerCount()) {
            return;
        }

        int newIndex = index + direction;

        if (newIndex < 0 || newIndex >= getServerCount()) {
            return;
        }

        List<ServerData> servers = new ArrayList<ServerData>();

        for (int i = 0; i < getServerCount(); i++) {
            servers.add(serverList.getServerData(i));
        }

        Collections.swap(servers, index, newIndex);
        replaceServerList(servers);

        selectedIndex = newIndex;

        saveServerList();
        rebuildButtons();

        setStatus(direction < 0
                ? "SERVER MOVED UP"
                : "SERVER MOVED DOWN");
    }

    private void replaceServerList(List<ServerData> servers) {
        for (int i = getServerCount() - 1; i >= 0; i--) {
            serverList.removeServerData(i);
        }

        for (ServerData server : servers) {
            serverList.addServerData(server);
        }
    }

    // ============================================================
    // LAST SERVER
    // ============================================================

    private File getLastServerFile() {
        File directory = new File(mc.mcDataDir, "astryx");

        if (!directory.exists()) {
            directory.mkdirs();
        }

        return new File(directory, "last-server.properties");
    }

    private void loadLastServer() {
        Properties properties = new Properties();

        try {
            File file = getLastServerFile();

            if (!file.exists()) {
                return;
            }

            FileInputStream input = new FileInputStream(file);

            try {
                properties.load(input);
            } finally {
                input.close();
            }

            lastServerName = properties.getProperty("name", "");
            lastServerAddress = properties.getProperty("address", "");

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void saveLastServer() {
        if (lastServerAddress == null
                || lastServerAddress.trim().isEmpty()) {
            return;
        }

        Properties properties = new Properties();

        properties.setProperty(
                "name",
                lastServerName == null ? "" : lastServerName
        );

        properties.setProperty("address", lastServerAddress);

        try {
            FileOutputStream output =
                    new FileOutputStream(getLastServerFile());

            try {
                properties.store(output, "Astryx Last Multiplayer Server");
            } finally {
                output.close();
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void rememberServer(ServerData server) {
        if (server == null || server.serverIP == null) {
            return;
        }

        lastServerName = server.serverName == null
                || server.serverName.trim().isEmpty()
                ? server.serverIP
                : server.serverName;

        lastServerAddress = server.serverIP;

        saveLastServer();
    }

    private void joinLastServer() {
        if (lastServerAddress == null
                || lastServerAddress.trim().isEmpty()) {
            setStatus("NO PREVIOUS SERVER FOUND");
            return;
        }

        connectToServer(new ServerData(
                lastServerName,
                lastServerAddress,
                false
        ));
    }

    // ============================================================
    // CONNECTION
    // ============================================================

    private void connectToServer(ServerData server) {
        if (server == null
                || server.serverIP == null
                || server.serverIP.trim().isEmpty()) {
            setStatus("INVALID SERVER ADDRESS");
            return;
        }

        rememberServer(server);
        mc.displayGuiScreen(new GuiConnecting(this, mc, server));
    }

    private void goBack() {
        mc.displayGuiScreen(parent);
    }

    // ============================================================
    // SERVER MANAGEMENT
    // ============================================================

    private void addServer() {
        if (!initialized) {
            return;
        }

        editingServer = true;
        creatingServer = true;
        editingIndex = -1;

        serverNameField = new GuiTextField(
                1, fontRendererObj,
                width / 2 - 125, height / 2 - 13,
                250, 22
        );

        serverAddressField = new GuiTextField(
                2, fontRendererObj,
                width / 2 - 125, height / 2 + 27,
                250, 22
        );

        serverNameField.setMaxStringLength(64);
        serverAddressField.setMaxStringLength(128);

        serverNameField.setText("");
        serverAddressField.setText("");
        serverNameField.setFocused(true);

        rebuildButtons();
    }

    private void editServer() {
        if (!initialized || !hasSelection()) {
            return;
        }

        ServerData server = getSelectedServer();

        editingServer = true;
        creatingServer = false;
        editingIndex = selectedIndex;

        serverNameField = new GuiTextField(
                1, fontRendererObj,
                width / 2 - 125, height / 2 - 13,
                250, 22
        );

        serverAddressField = new GuiTextField(
                2, fontRendererObj,
                width / 2 - 125, height / 2 + 27,
                250, 22
        );

        serverNameField.setMaxStringLength(64);
        serverAddressField.setMaxStringLength(128);

        serverNameField.setText(server.serverName);
        serverAddressField.setText(server.serverIP);
        serverNameField.setFocused(true);

        rebuildButtons();
    }

    private void saveEditedServer() {
        if (serverList == null || serverNameField == null
                || serverAddressField == null) {
            return;
        }

        String name = serverNameField.getText().trim();
        String address = serverAddressField.getText().trim();

        if (name.isEmpty() || address.isEmpty()) {
            setStatus("SERVER NAME AND ADDRESS ARE REQUIRED");
            return;
        }

        if (creatingServer) {
            serverList.addServerData(
                    new ServerData(name, address, false)
            );

            selectedIndex = getServerCount() - 1;

        } else if (editingIndex >= 0
                && editingIndex < getServerCount()) {

            List<ServerData> servers = new ArrayList<ServerData>();

            for (int i = 0; i < getServerCount(); i++) {
                servers.add(serverList.getServerData(i));
            }

            servers.set(
                    editingIndex,
                    new ServerData(name, address, false)
            );

            replaceServerList(servers);
            selectedIndex = editingIndex;
        }

        saveServerList();

        editingServer = false;
        serverNameField = null;
        serverAddressField = null;

        rebuildButtons();
        setStatus("SERVER SAVED");
        refreshServers();
    }

    private void cancelEditing() {
        editingServer = false;
        serverNameField = null;
        serverAddressField = null;

        rebuildButtons();
    }

    private void deleteServer() {
        if (!hasSelection()) {
            return;
        }

        confirmingDelete = true;

        ServerData server = getSelectedServer();

        mc.displayGuiScreen(new GuiYesNo(
                this,
                "Delete Server",
                "Are you sure you want to delete '"
                        + server.serverName + "'?",
                "DELETE",
                "CANCEL",
                selectedIndex
        ));
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (!confirmingDelete || serverList == null) {
            return;
        }

        confirmingDelete = false;

        if (result && id >= 0 && id < getServerCount()) {
            serverList.removeServerData(id);
            saveServerList();

            selectedIndex = -1;

            if (getServerCount() > 0) {
                selectedIndex = Math.min(id, getServerCount() - 1);
            }

            setStatus("SERVER DELETED");
            refreshServers();
        }

        rebuildButtons();
        mc.displayGuiScreen(this);
    }

    // ============================================================
    // BUTTON LAYOUT
    // ============================================================

    private void rebuildButtons() {
        buttons.clear();

        if (!initialized && !editingServer) {
            return;
        }

        if (editingServer) {
            int center = width / 2;

            buttons.add(new UiButton(
                    10, center - 130, height / 2 + 75,
                    120, 25, "SAVE SERVER", true
            ));

            buttons.add(new UiButton(
                    11, center + 10, height / 2 + 75,
                    120, 25, "CANCEL", false
            ));

            return;
        }

        int gap = 6;
        int buttonHeight = 20;
        int sideMargin = 18;
        int backWidth = 76;

        int availableWidth = Math.max(
                1,
                width - sideMargin - backWidth - gap - sideMargin
        );

        int y = height - 27;

        if (hasSelection()) {
            int buttonCount = 4;

            int buttonWidth = Math.max(
                    1,
                    Math.min(
                            112,
                            (availableWidth - gap * (buttonCount - 1))
                                    / buttonCount
                    )
            );

            int totalWidth = buttonWidth * buttonCount
                    + gap * (buttonCount - 1);

            int startX = sideMargin
                    + Math.max(0, (availableWidth - totalWidth) / 2);

            buttons.add(new UiButton(
                    1, startX, y, buttonWidth, buttonHeight,
                    "JOIN", true
            ));

            buttons.add(new UiButton(
                    2, startX + buttonWidth + gap, y,
                    buttonWidth, buttonHeight,
                    "ADD SERVER", false
            ));

            buttons.add(new UiButton(
                    3, startX + (buttonWidth + gap) * 2, y,
                    buttonWidth, buttonHeight,
                    "EDIT", false
            ));

            buttons.add(new UiButton(
                    4, startX + (buttonWidth + gap) * 3, y,
                    buttonWidth, buttonHeight,
                    "DELETE", false
            ));
        } else {
            buttons.add(new UiButton(
                    2,
                    sideMargin,
                    y,
                    Math.max(1, availableWidth),
                    buttonHeight,
                    "ADD SERVER",
                    true
            ));
        }

        buttons.add(new UiButton(
                7, width - sideMargin - backWidth, y,
                backWidth, buttonHeight,
                "BACK", false
        ));
    }

    private boolean handleButtonClick(int mouseX, int mouseY) {
        for (UiButton button : buttons) {
            if (!button.contains(mouseX, mouseY)) {
                continue;
            }

            if (editingServer) {
                if (button.id == 10) {
                    saveEditedServer();
                } else if (button.id == 11) {
                    cancelEditing();
                }

                return true;
            }

            switch (button.id) {
                case 1:
                    if (hasSelection()) {
                        connectToServer(getSelectedServer());
                    }
                    return true;

                case 2:
                    addServer();
                    return true;

                case 3:
                    if (hasSelection()) {
                        editServer();
                    }
                    return true;

                case 4:
                    if (hasSelection()) {
                        deleteServer();
                    }
                    return true;

                case 7:
                    goBack();
                    return true;
            }
        }

        return false;
    }

    // ============================================================
    // SERVER LIST LAYOUT
    // ============================================================

    private int getListWidth() {
        return Math.max(220, Math.min(620, width - 56));
    }

    private int getListLeft() {
        return Math.max(12, (width - getListWidth()) / 2);
    }

    private int getMaximumScroll() {
        int contentHeight =
                getServerCount() * (CARD_HEIGHT + CARD_GAP);

        return Math.max(
                0,
                contentHeight - (listBottom - listTop)
        );
    }

    private void clampScroll() {
        scrollOffset = Math.max(
                0,
                Math.min(getMaximumScroll(), scrollOffset)
        );
    }

    private void drawServerList(int mouseX, int mouseY) {
        clampScroll();

        if (getServerCount() == 0) {
            drawEmptyState();
            return;
        }

        int left = getListLeft();
        int listWidth = getListWidth();

        float scaleY = (float) mc.displayHeight / height;
        float scaleX = (float) mc.displayWidth / width;

        int scissorX = (int) (left * scaleX);
        int scissorY = (int) ((height - listBottom) * scaleY);
        int scissorWidth = (int) (listWidth * scaleX);
        int scissorHeight = (int) ((listBottom - listTop) * scaleY);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        GL11.glScissor(
                scissorX,
                scissorY,
                scissorWidth,
                scissorHeight
        );

        try {
            for (int i = 0; i < getServerCount(); i++) {
                int cardY = listTop
                        + i * (CARD_HEIGHT + CARD_GAP)
                        - (int) scrollOffset;

                if (cardY + CARD_HEIGHT < listTop
                        || cardY > listBottom) {
                    continue;
                }

                drawServerCard(
                        i, left, cardY, listWidth,
                        mouseX, mouseY
                );
            }
        } finally {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            resetRenderState();
        }

        drawScrollIndicator();
    }

    private void drawServerCard(
            int index,
            int left,
            int y,
            int listWidth,
            int mouseX,
            int mouseY
    ) {
        ServerData server = serverList.getServerData(index);

        int x = left + 4;
        int cardWidth = listWidth - 12;

        boolean selected = selectedIndex == index;

        boolean hovered = isInside(
                mouseX, mouseY,
                x, y, cardWidth, CARD_HEIGHT
        );

        drawRoundedRect(
                x, y, cardWidth, CARD_HEIGHT, 5,
                selected
                        ? 0xEE29233F
                        : hovered
                        ? 0xDD211F30
                        : 0xCC15151F
        );

        drawRoundedOutline(
                x, y, cardWidth, CARD_HEIGHT, 5,
                selected
                        ? PURPLE
                        : hovered
                        ? 0xFF77708F
                        : 0xFF363442
        );

        if (selected) {
            drawRoundedRect(
                    x + 1, y + 4, 3,
                    CARD_HEIGHT - 8, 2, PURPLE
            );
        }

        drawServerIcon(server, x + 7, y + 3, 20);

        int textLeft = x + 34;
        int arrowX = x + cardWidth - 26;
        int textRight = arrowX - 7;
        int textWidth = Math.max(40, textRight - textLeft);

        String serverName = server.serverName == null
                || server.serverName.isEmpty()
                ? server.serverIP
                : server.serverName;

        regularFont.drawString(
                trimText(serverName, textWidth, 0.15F),
                textLeft,
                y + 2,
                0.15F,
                0xFFFFFFFF
        );

        String motd = server.serverMOTD == null
                || server.serverMOTD.trim().isEmpty()
                || "Pinging...".equalsIgnoreCase(server.serverMOTD)
                ? server.serverIP
                : cleanMotd(server.serverMOTD);

        drawVanillaMotd(motd, textLeft, y + 14, textWidth);

        String ping;
        int pingColor;

        if ("Pinging...".equalsIgnoreCase(server.serverMOTD)
                || "PINGING".equalsIgnoreCase(server.serverMOTD)) {
            ping = "PINGING";
            pingColor = 0xFF9B8ACB;
        } else if (isConnectionFailure(server)) {
            ping = "RETRYING";
            pingColor = 0xFFE6A66F;
        } else if (server.pingToServer < 0L) {
            ping = server.field_78841_f ? "PINGING" : "WAITING";
            pingColor = 0xFF9B8ACB;
        } else {
            ping = server.pingToServer + " MS";

            if (server.pingToServer < 100L) {
                pingColor = 0xFF83D6AA;
            } else if (server.pingToServer < 250L) {
                pingColor = 0xFFE6C77B;
            } else {
                pingColor = 0xFFE78B8B;
            }
        }

        drawBoldRightAligned(
                ping, textRight, y + 14, pingColor, PING_SCALE
        );

        int arrowY = y + 1;

        boolean upEnabled = index > 0;
        boolean downEnabled = index < getServerCount() - 1;

        drawArrowButton(
                arrowX, arrowY, true, upEnabled,
                isInside(mouseX, mouseY, arrowX, arrowY, 12, 11)
        );

        drawArrowButton(
                arrowX, arrowY + 13, false, downEnabled,
                isInside(mouseX, mouseY, arrowX, arrowY + 13, 12, 11)
        );
    }

    private void drawServerIcon(
            ServerData server,
            int x,
            int y,
            int size
    ) {
        String iconData = server.getBase64EncodedIconData();

        if (iconData == null || iconData.trim().isEmpty()) {
            drawDefaultServerIcon(x, y, size);
            return;
        }

        ResourceLocation texture = getServerIconTexture(
                server.serverIP, iconData
        );

        if (texture == null) {
            drawDefaultServerIcon(x, y, size);
            return;
        }

        try {
            mc.getTextureManager().bindTexture(texture);

            drawModalRectWithCustomSizedTexture(
                    x, y, 0.0F, 0.0F,
                    size, size, size, size
            );
        } catch (Exception exception) {
            drawDefaultServerIcon(x, y, size);
        }
    }

    private ResourceLocation getServerIconTexture(
            String serverAddress,
            String iconData
    ) {
        String key = String.valueOf(serverAddress)
                + "|" + iconData.hashCode();

        ResourceLocation cached = serverIconTextures.get(key);

        if (cached != null) {
            return cached;
        }

        try {
            byte[] imageBytes = Base64.getDecoder().decode(iconData);

            BufferedImage image = ImageIO.read(
                    new ByteArrayInputStream(imageBytes)
            );

            if (image == null
                    || image.getWidth() != 64
                    || image.getHeight() != 64) {
                return null;
            }

            DynamicTexture dynamicTexture = new DynamicTexture(image);

            ResourceLocation location = new ResourceLocation(
                    "astryx",
                    "server_icons/" + Integer.toHexString(key.hashCode())
            );

            mc.getTextureManager().loadTexture(location, dynamicTexture);
            serverIconTextures.put(key, location);

            return location;

        } catch (Exception exception) {
            return null;
        }
    }

    private void drawDefaultServerIcon(int x, int y, int size) {
        mc.getTextureManager().bindTexture(
                new ResourceLocation("textures/misc/unknown_server.png")
        );

        drawModalRectWithCustomSizedTexture(
                x, y, 0.0F, 0.0F,
                size, size, size, size
        );
    }

    private String cleanMotd(String input) {
        if (input == null) {
            return "";
        }

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);

            if (current == '\u00A7'
                    && i + 1 < input.length()) {

                char code = input.charAt(i + 1);

                if (code == 'x' || code == 'X') {
                    if (i + 13 < input.length()) {
                        boolean validHex = true;

                        for (int j = i + 2; j <= i + 13; j += 2) {
                            if (input.charAt(j) != '\u00A7'
                                    || Character.digit(
                                    input.charAt(j + 1), 16
                            ) < 0) {
                                validHex = false;
                                break;
                            }
                        }

                        if (validHex) {
                            i += 13;
                            continue;
                        }
                    }

                    i++;
                    continue;
                }
            }

            if (current == '\n' || current == '\r'
                    || current == '\t'
                    || !Character.isISOControl(current)) {
                output.append(current);
            }
        }

        return output.toString();
    }

    private void drawVanillaMotd(
            String motd,
            int x,
            int y,
            int maxWidth
    ) {
        if (motd == null || motd.isEmpty() || maxWidth <= 0) {
            return;
        }

        float scale = 0.65F;
        int scaledMaxWidth = (int) (maxWidth / scale);

        String trimmed = fontRendererObj.trimStringToWidth(
                motd, scaledMaxWidth
        );

        if (fontRendererObj.getStringWidth(motd) > scaledMaxWidth) {
            int ellipsisWidth = fontRendererObj.getStringWidth("...");

            String shortened = fontRendererObj.trimStringToWidth(
                    motd,
                    Math.max(0, scaledMaxWidth - ellipsisWidth)
            );

            trimmed = shortened + "...";
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);

        fontRendererObj.drawStringWithShadow(
                trimmed, 0, 0, 0xFFAAA6BC
        );

        GlStateManager.popMatrix();
    }

    private void drawArrowButton(
            int x,
            int y,
            boolean up,
            boolean enabled,
            boolean hovered
    ) {
        int background = !enabled
                ? 0x66302D3D
                : hovered
                ? 0xFF51446F
                : 0xFF292638;

        drawRoundedRect(x, y, 12, 11, 3, background);

        drawRoundedOutline(
                x, y, 12, 11, 3,
                enabled ? 0xFF71618F : 0xFF403B4D
        );

        int color = enabled
                ? hovered ? 0xFFFFFFFF : 0xFFC8BCE5
                : 0xFF666273;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

        GlStateManager.color(
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F,
                ((color >> 24) & 255) / 255.0F
        );

        GL11.glBegin(GL11.GL_TRIANGLES);

        if (up) {
            GL11.glVertex2f(x + 3, y + 7);
            GL11.glVertex2f(x + 9, y + 7);
            GL11.glVertex2f(x + 6, y + 3);
        } else {
            GL11.glVertex2f(x + 3, y + 4);
            GL11.glVertex2f(x + 9, y + 4);
            GL11.glVertex2f(x + 6, y + 8);
        }

        GL11.glEnd();

        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
    }

    private void drawScrollIndicator() {
        int maximum = getMaximumScroll();

        if (maximum <= 0) {
            return;
        }

        int trackHeight = listBottom - listTop;

        int contentHeight =
                getServerCount() * (CARD_HEIGHT + CARD_GAP);

        int thumbHeight = Math.max(
                22,
                (int) ((float) trackHeight
                        * trackHeight / contentHeight)
        );

        int thumbY = listTop + (int) (
                (trackHeight - thumbHeight)
                        * (scrollOffset / maximum)
        );

        int x = getListLeft() + getListWidth() - 5;

        drawRoundedRect(x, listTop, 3, trackHeight, 2, 0x553F3C50);
        drawRoundedRect(x, thumbY, 3, thumbHeight, 2, 0xFF8068C6);
    }

    private void drawEmptyState() {
        int centerY = (listTop + listBottom) / 2;

        int boxWidth = Math.min(240, getListWidth());
        int boxX = getListLeft() + (getListWidth() - boxWidth) / 2;

        drawRoundedRect(
                boxX, centerY - 39,
                boxWidth, 78, 10, 0xAA171722
        );

        drawTrackedCentered(
                "NO SERVERS FOUND",
                boxX + boxWidth / 2,
                centerY - 17,
                0.18F,
                2,
                0xFFE5E0F3,
                true
        );

        regularFont.drawCenteredString(
                "Add a server to get started.",
                boxX + boxWidth / 2.0F,
                centerY + 8,
                0.155F,
                0xFF88859D
        );
    }

    // ============================================================
    // HEADER AND FOOTER
    // ============================================================

    private void drawHeader(int mouseX, int mouseY) {
        drawRect(0, 0, width, 40, 0xCC090A12);
        drawRect(0, 39, width, 40, 0x443F3970);

        drawRoundedRect(22, 8, 2, 23, 1, PURPLE);

        boldFont.drawString(
                "ASTRYX", 31, 6, 0.19F, 0xFFEDEAF8
        );

        regularFont.drawString(
                "Your game, your way.",
                31, 22, 0.12F, 0xFF85839C
        );

        drawTrackedCentered(
                "MULTIPLAYER",
                width / 2,
                6,
                0.18F,
                2,
                0xFFF4F0FF,
                true
        );

        regularFont.drawCenteredString(
                getServerCount() + (
                        getServerCount() == 1
                                ? " SAVED SERVER"
                                : " SAVED SERVERS"
                ),
                width / 2.0F,
                25,
                0.12F,
                0xFF9995AD
        );

        drawHeaderButtons(mouseX, mouseY);
    }

    // ============================================================
    // HEADER BUTTON RENDERING
    // ============================================================

    private void drawHeaderButtons(int mouseX, int mouseY) {
        int joinX = getJoinLastX();
        int joinY = getJoinLastY();
        int joinWidth = getJoinLastWidth();

        int refreshX = getRefreshX();
        int refreshY = getRefreshY();
        int refreshWidth = getRefreshWidth();

        int buttonHeight = getHeaderButtonHeight();

        boolean hasLastServer = lastServerAddress != null
                && !lastServerAddress.trim().isEmpty();

        boolean joinHovered = isInside(
                mouseX, mouseY,
                joinX, joinY, joinWidth, buttonHeight
        );

        boolean refreshHovered = isInside(
                mouseX, mouseY,
                refreshX, refreshY, refreshWidth, buttonHeight
        );

        drawRoundedRect(
                refreshX, refreshY,
                refreshWidth, buttonHeight,
                6,
                refreshHovered ? 0xFF342D4A : 0xFF242136
        );

        drawRoundedOutline(
                refreshX, refreshY,
                refreshWidth, buttonHeight,
                6,
                refreshHovered ? PURPLE : 0xFF67538F
        );

        float refreshTextHeight = boldFont.getHeight(0.12F);

        float refreshTextY = refreshY
                + (buttonHeight - refreshTextHeight) / 2.0F;

        boldFont.drawCenteredString(
                "REFRESH",
                refreshX + refreshWidth / 2.0F,
                refreshTextY,
                0.12F,
                0xFFCDBAFF
        );

        drawRoundedRect(
                joinX, joinY,
                joinWidth, buttonHeight,
                6,
                joinHovered ? 0xFF342D4A : 0xFF242136
        );

        drawRoundedOutline(
                joinX, joinY,
                joinWidth, buttonHeight,
                6,
                hasLastServer ? 0xFF67538F : 0xFF454153
        );

        String displayName;

        if (hasLastServer) {
            displayName = lastServerName;

            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = lastServerAddress;
            }
        } else {
            displayName = "NO PREVIOUS SERVER";
        }

        float titleScale = 0.12F;
        float subtitleScale = 0.095F;

        float titleHeight = boldFont.getHeight(titleScale);
        float subtitleHeight = regularFont.getHeight(subtitleScale);

        float lineGap = 1.0F;

        float totalTextHeight = titleHeight
                + lineGap
                + subtitleHeight;

        float textStartY = joinY
                + (buttonHeight - totalTextHeight) / 2.0F;

        boldFont.drawCenteredString(
                "JOIN LAST",
                joinX + joinWidth / 2.0F,
                textStartY,
                titleScale,
                hasLastServer ? 0xFFCDBAFF : 0xFF777386
        );

        regularFont.drawCenteredString(
                trimText(displayName, joinWidth - 12, subtitleScale),
                joinX + joinWidth / 2.0F,
                textStartY + titleHeight + lineGap,
                subtitleScale,
                hasLastServer ? 0xFFAAA3C0 : 0xFF777386
        );
    }

    private void drawFooter() {
        int top = height - 52;

        drawRect(0, top, width, height - 31, 0xCC090A12);
        drawRect(0, top, width, top + 1, 0x443F3970);

        String status = statusMessage;

        if (System.currentTimeMillis() - statusMessageTime > 5000L
                && statusMessageTime != 0L) {

            ServerData selected = getSelectedServer();

            status = selected != null
                    ? "SELECTED  /  " + selected.serverName
                    : "";
        }

        if (!status.isEmpty()) {
            regularFont.drawString(
                    trimText(status, width - 44, 0.115F),
                    22, top + 6, 0.115F, 0xFF9995AD
            );
        }
    }

    // ============================================================
    // SERVER EDITOR
    // ============================================================

    private void drawEditor() {
        drawRect(0, 0, width, height, 0xB9000000);

        int panelWidth = Math.min(310, width - 24);
        int panelHeight = Math.min(205, height - 24);

        int x = width / 2 - panelWidth / 2;
        int y = height / 2 - panelHeight / 2;

        drawRoundedRect(x, y, panelWidth, panelHeight, 12, 0xFF151520);
        drawRoundedOutline(x, y, panelWidth, panelHeight, 12, 0xFF66528E);

        drawRoundedRect(x + 17, y + 18, 2, 20, 1, PURPLE);

        boldFont.drawString(
                creatingServer ? "ADD SERVER" : "EDIT SERVER",
                x + 28, y + 17, 0.20F, 0xFFF0ECFF
        );

        regularFont.drawString(
                "Configure your multiplayer connection.",
                x + 28, y + 39, 0.14F, 0xFF918DA5
        );

        regularFont.drawString(
                "SERVER NAME", x + 29, y + 67, 0.13F, 0xFFAAA4BF
        );

        regularFont.drawString(
                "SERVER ADDRESS", x + 29, y + 107, 0.13F, 0xFFAAA4BF
        );

        drawRoundedRect(x + 25, y + 80, panelWidth - 50, 23, 5, 0xFF20202D);
        drawRoundedRect(x + 25, y + 120, panelWidth - 50, 23, 5, 0xFF20202D);

        serverNameField.xPosition = x + 32;
        serverNameField.yPosition = y + 84;
        serverNameField.width = panelWidth - 64;
        serverNameField.height = 16;

        serverAddressField.xPosition = x + 32;
        serverAddressField.yPosition = y + 124;
        serverAddressField.width = panelWidth - 64;
        serverAddressField.height = 16;

        serverNameField.drawTextBox();
        serverAddressField.drawTextBox();

        drawEditorButtons();
    }

    private void drawEditorButtons() {
        for (UiButton button : buttons) {
            drawButton(button, 0, 0);
        }
    }

    // ============================================================
    // BUTTON RENDERING
    // ============================================================

    private void drawButtons(int mouseX, int mouseY) {
        for (UiButton button : buttons) {
            drawButton(button, mouseX, mouseY);
        }
    }

    private void drawButton(
            UiButton button,
            int mouseX,
            int mouseY
    ) {
        boolean hovered = button.contains(mouseX, mouseY);

        if (button.primary) {
            drawRoundedGradient(
                    button.x, button.y,
                    button.width, button.height,
                    6,
                    hovered ? 0xFF9875EF : 0xFF7553D0,
                    hovered ? 0xFF7197FF : 0xFF4D70D5
            );

            drawRoundedOutline(
                    button.x, button.y,
                    button.width, button.height,
                    6, 0x887F72C6
            );
        } else {
            drawRoundedRect(
                    button.x, button.y,
                    button.width, button.height,
                    6,
                    hovered ? 0xFF292538 : 0xFF191923
            );

            drawRoundedOutline(
                    button.x, button.y,
                    button.width, button.height,
                    6,
                    hovered ? 0xFF8B73C9 : 0xFF454153
            );
        }

        float textY = button.y
                + (button.height - regularFont.getHeight(0.16F)) / 2.0F;

        regularFont.drawCenteredString(
                button.label,
                button.x + button.width / 2.0F,
                textY,
                0.16F,
                0xFFF3F0FF
        );
    }

    // ============================================================
    // TEXT HELPERS
    // ============================================================

    private String trimText(
            String text,
            int maxWidth,
            float scale
    ) {
        if (text == null || maxWidth <= 0) {
            return "";
        }

        if (regularFont.getStringWidth(text, scale) <= maxWidth) {
            return text;
        }

        while (text.length() > 3
                && regularFont.getStringWidth(text + "...", scale) > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }

        return text + "...";
    }

    private void drawTrackedCentered(
            String text,
            int centerX,
            int y,
            float scale,
            int spacing,
            int color,
            boolean bold
    ) {
        MainMenu.CustomFont font = bold ? boldFont : regularFont;

        float textWidth = font.getStringWidthFloat(text, scale)
                + Math.max(0, text.length() - 1) * spacing;

        float currentX = centerX - textWidth / 2.0F;

        for (int i = 0; i < text.length(); i++) {
            String character = String.valueOf(text.charAt(i));

            font.drawString(character, currentX, y, scale, color);

            currentX += font.getStringWidthFloat(character, scale)
                    + spacing;
        }
    }

    private void drawRightAligned(
            String text,
            int right,
            int y,
            int color,
            float scale
    ) {
        if (text == null || text.isEmpty()) {
            return;
        }

        regularFont.drawString(
                text,
                right - regularFont.getStringWidth(text, scale),
                y, scale, color
        );
    }

    private void drawBoldRightAligned(
            String text,
            int right,
            int y,
            int color,
            float scale
    ) {
        if (text == null || text.isEmpty()) {
            return;
        }

        boldFont.drawString(
                text,
                right - boldFont.getStringWidth(text, scale),
                y, scale, color
        );
    }

    private void setStatus(String message) {
        statusMessage = message;
        statusMessageTime = System.currentTimeMillis();
    }

    // ============================================================
    // BACKGROUND
    // ============================================================

    private void drawAstryxBackground() {
        drawGradientRect(
                0, 0, width, height,
                0xFF11111C, 0xFF080910
        );

        float radius = Math.min(width, height) * 0.45F;
        float movement = (float) Math.sin(animation * 0.7F);

        drawGlow(
                width * 0.48F + movement * 30,
                height * 0.29F,
                radius,
                0x267B45D5
        );

        drawGlow(
                width * 0.84F,
                height * 0.72F,
                radius * 0.75F,
                0x18506FEA
        );

        drawGlow(
                width * 0.15F,
                height * 0.74F,
                radius * 0.65F,
                0x155E3AB6
        );

        drawBackgroundGrid();
        drawParticles();
    }

    private void drawBackgroundGrid() {
        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glBegin(GL11.GL_LINES);

        for (int y = 58; y < height - 80; y += 42) {
            float wave = (float) Math.sin(
                    animation * 0.35F + y * 0.015F
            );

            int alpha = 4 + (int) ((wave + 1) * 2);

            setGlColor((alpha << 24) | 0x7777A5);

            GL11.glVertex2f(0, y);
            GL11.glVertex2f(width, y);
        }

        for (int x = 35; x < width; x += 55) {
            setGlColor(0x067C79C5);

            GL11.glVertex2f(x, 0);
            GL11.glVertex2f(x, height);
        }

        GL11.glEnd();

        GlStateManager.popMatrix();
        resetRenderState();
    }

    private void drawParticles() {
        for (Particle particle : particles) {
            if (particle == null) {
                continue;
            }

            int baseColor = particle.purple ? PURPLE : BLUE;
            int alpha = (int) (particle.opacity * 75);

            int color = (alpha << 24) | (baseColor & 0xFFFFFF);

            int size = particle.opacity > 0.85F ? 2 : 1;

            drawRect(
                    (int) particle.x,
                    (int) particle.y,
                    (int) particle.x + size,
                    (int) particle.y + size,
                    color
            );
        }
    }

    private void drawGlow(
            float centerX,
            float centerY,
            float radius,
            int color
    ) {
        float alpha = ((color >> 24) & 255) / 255.0F;
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        GlStateManager.color(red, green, blue, alpha);
        GL11.glVertex2f(centerX, centerY);

        GlStateManager.color(red, green, blue, 0);

        for (int angle = 0; angle <= 360; angle += 5) {
            double radians = Math.toRadians(angle);

            GL11.glVertex2f(
                    centerX + (float) Math.cos(radians) * radius,
                    centerY + (float) Math.sin(radians) * radius
            );
        }

        GL11.glEnd();

        GlStateManager.popMatrix();
        resetRenderState();
    }

    // ============================================================
    // ROUNDED SHAPES
    // ============================================================

    private void drawRoundedRect(
            float x,
            float y,
            float boxWidth,
            float boxHeight,
            float radius,
            int color
    ) {
        if (boxWidth <= 0 || boxHeight <= 0) {
            return;
        }

        radius = Math.min(
                radius,
                Math.min(boxWidth / 2.0F, boxHeight / 2.0F)
        );

        float alpha = ((color >> 24) & 255) / 255.0F;
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GlStateManager.color(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_POLYGON);

        for (int corner = 0; corner < 4; corner++) {
            float centerX;
            float centerY;
            int startAngle;

            if (corner == 0) {
                centerX = x + boxWidth - radius;
                centerY = y + radius;
                startAngle = -90;
            } else if (corner == 1) {
                centerX = x + boxWidth - radius;
                centerY = y + boxHeight - radius;
                startAngle = 0;
            } else if (corner == 2) {
                centerX = x + radius;
                centerY = y + boxHeight - radius;
                startAngle = 90;
            } else {
                centerX = x + radius;
                centerY = y + radius;
                startAngle = 180;
            }

            for (int angle = startAngle; angle <= startAngle + 90; angle += 3) {
                double radians = Math.toRadians(angle);

                GL11.glVertex2f(
                        centerX + (float) Math.cos(radians) * radius,
                        centerY + (float) Math.sin(radians) * radius
                );
            }
        }

        GL11.glEnd();

        GlStateManager.popMatrix();
        resetRenderState();
    }

    private void drawRoundedOutline(
            float x,
            float y,
            float boxWidth,
            float boxHeight,
            float radius,
            int color
    ) {
        if (boxWidth <= 0 || boxHeight <= 0) {
            return;
        }

        radius = Math.min(
                radius,
                Math.min(boxWidth / 2.0F, boxHeight / 2.0F)
        );

        float alpha = ((color >> 24) & 255) / 255.0F;
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glLineWidth(1.0F);
        GlStateManager.color(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_LINE_LOOP);

        for (int corner = 0; corner < 4; corner++) {
            float centerX;
            float centerY;
            int startAngle;

            if (corner == 0) {
                centerX = x + boxWidth - radius;
                centerY = y + radius;
                startAngle = -90;
            } else if (corner == 1) {
                centerX = x + boxWidth - radius;
                centerY = y + boxHeight - radius;
                startAngle = 0;
            } else if (corner == 2) {
                centerX = x + radius;
                centerY = y + boxHeight - radius;
                startAngle = 90;
            } else {
                centerX = x + radius;
                centerY = y + radius;
                startAngle = 180;
            }

            for (int angle = startAngle; angle < startAngle + 90; angle += 3) {
                double radians = Math.toRadians(angle);

                GL11.glVertex2f(
                        centerX + (float) Math.cos(radians) * radius,
                        centerY + (float) Math.sin(radians) * radius
                );
            }
        }

        GL11.glEnd();

        GlStateManager.popMatrix();
        resetRenderState();
    }

    private void drawRoundedGradient(
            float x,
            float y,
            float boxWidth,
            float boxHeight,
            float radius,
            int leftColor,
            int rightColor
    ) {
        if (boxWidth <= 0 || boxHeight <= 0) {
            return;
        }

        radius = Math.min(
                radius,
                Math.min(boxWidth / 2.0F, boxHeight / 2.0F)
        );

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(GL11.GL_POLYGON);

        for (int corner = 0; corner < 4; corner++) {
            float cornerX;
            float cornerY;
            int startAngle;

            if (corner == 0) {
                cornerX = x + boxWidth - radius;
                cornerY = y + radius;
                startAngle = -90;
            } else if (corner == 1) {
                cornerX = x + boxWidth - radius;
                cornerY = y + boxHeight - radius;
                startAngle = 0;
            } else if (corner == 2) {
                cornerX = x + radius;
                cornerY = y + boxHeight - radius;
                startAngle = 90;
            } else {
                cornerX = x + radius;
                cornerY = y + radius;
                startAngle = 180;
            }

            for (int angle = startAngle; angle <= startAngle + 90; angle += 3) {
                double radians = Math.toRadians(angle);

                float vertexX = cornerX
                        + (float) Math.cos(radians) * radius;

                float vertexY = cornerY
                        + (float) Math.sin(radians) * radius;

                float progress = Math.max(
                        0,
                        Math.min(1, (vertexX - x) / boxWidth)
                );

                GlStateManager.color(
                        blendChannel(leftColor, rightColor, progress, 16),
                        blendChannel(leftColor, rightColor, progress, 8),
                        blendChannel(leftColor, rightColor, progress, 0),
                        1.0F
                );

                GL11.glVertex2f(vertexX, vertexY);
            }
        }

        GL11.glEnd();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
        resetRenderState();
    }

    private float blendChannel(
            int left,
            int right,
            float progress,
            int shift
    ) {
        float a = (left >> shift) & 255;
        float b = (right >> shift) & 255;

        return (a + (b - a) * progress) / 255.0F;
    }

    private void setGlColor(int color) {
        float alpha = ((color >> 24) & 255) / 255.0F;
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.color(red, green, blue, alpha);
    }

    private void enableSmoothRendering() {
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
    }

    private void resetRenderState() {
        GL11.glDisable(GL11.GL_CULL_FACE);

        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        enableSmoothRendering();
    }

    // ============================================================
    // INPUT
    // ============================================================

    private boolean isInside(
            int mouseX,
            int mouseY,
            int x,
            int y,
            int boxWidth,
            int boxHeight
    ) {
        return mouseX >= x
                && mouseX < x + boxWidth
                && mouseY >= y
                && mouseY < y + boxHeight;
    }

    private boolean handleArrowClick(int mouseX, int mouseY) {
        int left = getListLeft();
        int cardWidth = getListWidth() - 12;
        int arrowX = left + 4 + cardWidth - 26;

        for (int i = 0; i < getServerCount(); i++) {
            int cardY = listTop
                    + i * (CARD_HEIGHT + CARD_GAP)
                    - (int) scrollOffset;

            if (isInside(mouseX, mouseY, arrowX, cardY + 1, 12, 11)) {
                if (i > 0) {
                    moveServer(i, -1);
                }

                return true;
            }

            if (isInside(mouseX, mouseY, arrowX, cardY + 14, 12, 11)) {
                if (i < getServerCount() - 1) {
                    moveServer(i, 1);
                }

                return true;
            }
        }

        return false;
    }

    private void handleHeaderButtonClick(int mouseX, int mouseY) {
        int joinX = getJoinLastX();
        int joinY = getJoinLastY();

        if (isInside(
                mouseX, mouseY,
                joinX, joinY,
                getJoinLastWidth(), getHeaderButtonHeight()
        )) {
            joinLastServer();
            return;
        }

        int refreshX = getRefreshX();
        int refreshY = getRefreshY();

        if (isInside(
                mouseX, mouseY,
                refreshX, refreshY,
                getRefreshWidth(), getHeaderButtonHeight()
        )) {
            refreshServers();
        }
    }

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton
    ) throws IOException {
        if (mouseButton != 0) {
            return;
        }

        if (!initialized) {
            return;
        }

        if (editingServer) {
            serverNameField.mouseClicked(mouseX, mouseY, mouseButton);
            serverAddressField.mouseClicked(mouseX, mouseY, mouseButton);

            handleButtonClick(mouseX, mouseY);
            return;
        }

        int joinX = getJoinLastX();
        int joinY = getJoinLastY();

        if (isInside(
                mouseX, mouseY,
                joinX, joinY,
                getJoinLastWidth(), getHeaderButtonHeight()
        )) {
            joinLastServer();
            return;
        }

        int refreshX = getRefreshX();
        int refreshY = getRefreshY();

        if (isInside(
                mouseX, mouseY,
                refreshX, refreshY,
                getRefreshWidth(), getHeaderButtonHeight()
        )) {
            refreshServers();
            return;
        }

        if (handleButtonClick(mouseX, mouseY)) {
            return;
        }

        if (mouseX >= getListLeft()
                && mouseX <= getListLeft() + getListWidth()
                && mouseY >= listTop
                && mouseY < listBottom) {

            if (handleArrowClick(mouseX, mouseY)) {
                return;
            }

            int relativeY = mouseY - listTop + (int) scrollOffset;

            int index = relativeY / (CARD_HEIGHT + CARD_GAP);
            int withinCard = relativeY % (CARD_HEIGHT + CARD_GAP);

            if (withinCard < CARD_HEIGHT
                    && index >= 0
                    && index < getServerCount()) {

                long now = System.currentTimeMillis();

                if (lastClickedIndex == index
                        && now - lastClickTime < 350L) {
                    selectedIndex = index;
                    rebuildButtons();
                    connectToServer(getSelectedServer());
                    return;
                }

                selectedIndex = index;
                lastClickedIndex = index;
                lastClickTime = now;

                rebuildButtons();
                return;
            }

            if (selectedIndex != -1) {
                selectedIndex = -1;
                rebuildButtons();
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode)
            throws IOException {

        if (!initialized) {
            if (keyCode == 1) {
                goBack();
            }
            return;
        }

        if (editingServer) {
            if (keyCode == 1) {
                cancelEditing();
                return;
            }

            if (serverNameField.textboxKeyTyped(typedChar, keyCode)) {
                return;
            }

            if (serverAddressField.textboxKeyTyped(typedChar, keyCode)) {
                return;
            }

            if (keyCode == 28 || keyCode == 156) {
                saveEditedServer();
                return;
            }

            return;
        }

        if (keyCode == 1) {
            goBack();
            return;
        }

        if (keyCode == 28 || keyCode == 156) {
            if (hasSelection()) {
                connectToServer(getSelectedServer());
            }

            return;
        }

        if (keyCode == 200 && getServerCount() > 0) {
            selectedIndex = Math.max(
                    0,
                    selectedIndex < 0 ? 0 : selectedIndex - 1
            );

            rebuildButtons();
            return;
        }

        if (keyCode == 208 && getServerCount() > 0) {
            selectedIndex = Math.min(
                    getServerCount() - 1,
                    selectedIndex < 0 ? 0 : selectedIndex + 1
            );

            rebuildButtons();
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getDWheel();

        super.handleMouseInput();

        if (initialized && !editingServer && wheel != 0) {
            scrollOffset -= wheel > 0 ? 42 : -42;
            clampScroll();
        }
    }

    // ============================================================
    // ANIMATION
    // ============================================================

    @Override
    public void updateScreen() {
        super.updateScreen();

        /*
         * The first drawScreen() call displays the lightweight loading
         * frame. Only after that frame has rendered do we initialise
         * the fonts, server list, saved server and particles.
         */
        if (!initialized) {
            if (firstFrameRendered) {
                initializeScreen();
            }

            lastFrame = System.currentTimeMillis();
            return;
        }

        long now = System.currentTimeMillis();

        float delta = Math.min(
                (now - lastFrame) / 16.0F,
                3.0F
        );

        lastFrame = now;
        animation += delta * 0.008F;

        for (Particle particle : particles) {
            if (particle == null) {
                continue;
            }

            particle.x += particle.speedX * delta;
            particle.y -= particle.speedY * delta;

            if (particle.y < -5) {
                particle.y = height + 5;
                particle.x = random.nextFloat() * width;
            }

            if (particle.x > width + 5) {
                particle.x = -5;
            }
        }

        if (!editingServer) {
            updateServerPings();
        } else {
            if (serverNameField != null) {
                serverNameField.updateCursorCounter();
            }

            if (serverAddressField != null) {
                serverAddressField.updateCursorCounter();
            }
        }
    }

    // ============================================================
    // MAIN DRAW
    // ============================================================

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (!initialized) {
            /*
             * This frame intentionally uses only Minecraft's built-in
             * renderer. No custom fonts, server list, textures or
             * particles are required to display it.
             */
            drawGradientRect(
                    0, 0, width, height,
                    0xFF11111C, 0xFF080910
            );

            drawRect(0, 0, width, 40, 0xCC090A12);
            drawRect(0, 39, width, 40, 0x443F3970);

            fontRendererObj.drawStringWithShadow(
                    "ASTRYX",
                    22,
                    15,
                    0xFFEDEAF8
            );

            String loading = "Loading multiplayer...";

            fontRendererObj.drawStringWithShadow(
                    loading,
                    width / 2 - fontRendererObj.getStringWidth(loading) / 2,
                    height / 2 - 4,
                    0xFFAAA3C0
            );

            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            /*
             * updateScreen() will now perform the heavier setup.
             */
            firstFrameRendered = true;
            return;
        }

        resetRenderState();
        drawAstryxBackground();

        drawHeader(mouseX, mouseY);

        if (editingServer) {
            drawEditor();
        } else {
            drawServerList(mouseX, mouseY);
            drawFooter();
            drawButtons(mouseX, mouseY);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        resetRenderState();
    }

    @Override
    public void onResize(Minecraft mc, int width, int height) {
        super.onResize(mc, width, height);

        updateListBounds();

        if (!initialized) {
            return;
        }

        clampScroll();
        rebuildButtons();

        if (editingServer
                && serverNameField != null
                && serverAddressField != null) {
            int centerX = width / 2;

            serverNameField.xPosition = centerX - 125;
            serverNameField.yPosition = height / 2 - 13;

            serverAddressField.xPosition = centerX - 125;
            serverAddressField.yPosition = height / 2 + 27;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // ============================================================
    // DATA CLASSES
    // ============================================================

    private static class UiButton {
        final int id;
        final int x;
        final int y;
        final int width;
        final int height;
        final String label;
        final boolean primary;

        UiButton(
                int id,
                int x,
                int y,
                int width,
                int height,
                String label,
                boolean primary
        ) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.label = label;
            this.primary = primary;
        }

        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x
                    && mouseX < x + width
                    && mouseY >= y
                    && mouseY < y + height;
        }
    }

    private static class Particle {
        float x;
        float y;

        float speedX;
        float speedY;

        boolean purple;
        float opacity;

        Particle(
                float x,
                float y,
                float speedX,
                float speedY,
                boolean purple,
                float opacity
        ) {
            this.x = x;
            this.y = y;
            this.speedX = speedX;
            this.speedY = speedY;
            this.purple = purple;
            this.opacity = opacity;
        }
    }
}