package com.mercury.platform.ui.components.panel;


import com.mercury.platform.shared.ConfigManager;
import com.mercury.platform.shared.HasEventHandlers;
import com.mercury.platform.shared.events.EventRouter;
import com.mercury.platform.shared.events.custom.*;
import com.mercury.platform.shared.pojo.CurrencyMessage;
import com.mercury.platform.shared.pojo.ItemMessage;
import com.mercury.platform.shared.pojo.Message;
import com.mercury.platform.shared.pojo.ResponseButton;
import com.mercury.platform.ui.components.ComponentsFactory;
import com.mercury.platform.ui.components.fields.font.FontStyle;
import com.mercury.platform.ui.components.fields.font.TextAlignment;
import com.mercury.platform.ui.components.panel.misc.MessagePanelStyle;
import com.mercury.platform.ui.frame.ComponentFrame;
import com.mercury.platform.ui.frame.impl.HistoryContainer;
import com.mercury.platform.ui.frame.impl.MessagesContainer;
import com.mercury.platform.ui.misc.AppThemeColor;
import com.mercury.platform.ui.misc.TooltipConstants;
import com.mercury.platform.ui.misc.event.CloseMessagePanelEvent;
import com.mercury.platform.ui.misc.event.RepaintEvent;
import com.mercury.platform.ui.misc.event.ShowItemGridEvent;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;


public class MessagePanel extends JPanel implements HasEventHandlers{
    private ComponentsFactory componentsFactory = ComponentsFactory.INSTANCE;
    private ComponentFrame owner;
    private MessagePanelStyle style;

    private String whisper;
    private JLabel whisperLabel;
    private JButton tradeButton;
    private JButton expandButton;

    private Message message;

    private Timer timeAgo;
    private String cachedTime = "0m";
    private JLabel timeLabel;
    private Color cachedWhisperColor = AppThemeColor.TEXT_NICKNAME;
    private JPanel whisperPanel;
    private JPanel messagePanel;
    private JPanel customButtonsPanel;

    private boolean expanded = false;

    public MessagePanel(Message message, ComponentFrame owner, MessagePanelStyle style){
        super(new BorderLayout());
        this.message = message;
        this.owner = owner;
        this.style = style;
        this.whisper = message.getWhisperNickname();
        this.setBackground(AppThemeColor.FRAME);
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppThemeColor.TRANSPARENT,1),
                BorderFactory.createLineBorder(AppThemeColor.BORDER, 1)));

        this.messagePanel = getFormattedMessagePanel();
        this.customButtonsPanel = getButtonsPanel();
        init();
        initHandlers();
        setMaximumSize(new Dimension(Integer.MAX_VALUE,getPreferredSize().height));
        setMinimumSize(new Dimension(Integer.MAX_VALUE,getPreferredSize().height));
    }
    private void init(){
        this.removeAll();
        this.whisperPanel = getWhisperPanel();
        whisperPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, AppThemeColor.MSG_HEADER_BORDER),
                BorderFactory.createEmptyBorder(-6, 0, -6, 0)));
        if(style.equals(MessagePanelStyle.DOWNWARDS_SMALL) ||
                style.equals(MessagePanelStyle.HISTORY) || style.equals(MessagePanelStyle.SP_MODE)) {
            this.add(whisperPanel,BorderLayout.PAGE_START);
            this.add(messagePanel,BorderLayout.CENTER);
            this.add(customButtonsPanel,BorderLayout.PAGE_END);
        }else {
            this.add(customButtonsPanel,BorderLayout.PAGE_START);
            this.add(messagePanel,BorderLayout.CENTER);
            this.add(whisperPanel,BorderLayout.PAGE_END);
        }
        switch (style){
            case DOWNWARDS_SMALL:{
                messagePanel.setVisible(false);
                customButtonsPanel.setVisible(false);
                break;
            }
            case UPWARDS_SMALL:{
                messagePanel.setVisible(false);
                customButtonsPanel.setVisible(false);
                break;
            }
            case HISTORY:{
                messagePanel.setVisible(true);
                customButtonsPanel.setVisible(false);
                break;
            }
            case SP_MODE:{
                messagePanel.setVisible(true);
                customButtonsPanel.setVisible(true);
            }
        }
        this.repaint();
    }

    private JPanel getFormattedMessagePanel(){
        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel,BoxLayout.Y_AXIS));
        labelsPanel.setBackground(AppThemeColor.TRANSPARENT);

        JPanel tradePanel = new JPanel(new BorderLayout());
        tradePanel.setBackground(AppThemeColor.TRANSPARENT);
        tradePanel.setBorder(BorderFactory.createEmptyBorder(-11,2,-11,0));
        if(message instanceof ItemMessage) {
            JButton itemButton = componentsFactory.getButton(((ItemMessage) message).getItemName());
            itemButton.setForeground(AppThemeColor.TEXT_IMPORTANT);
            itemButton.setFont(componentsFactory.getFont(FontStyle.BOLD).deriveFont(17f));
            itemButton.setBackground(AppThemeColor.TRANSPARENT);
            itemButton.setBorder(BorderFactory.createEmptyBorder(0,4,0,2));
            itemButton.setHorizontalAlignment(SwingConstants.LEFT);
            itemButton.setContentAreaFilled(false);
            itemButton.setRolloverEnabled(false);
            itemButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if(message instanceof ItemMessage) {
                        copyItemNameToClipboard(((ItemMessage) message).getItemName());
                        if (((ItemMessage) message).getTabName() != null) {
                            EventRouter.UI.fireEvent(new ShowItemGridEvent((ItemMessage) message));
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    owner.repaint();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    itemButton.setBorder(new CompoundBorder(
                            BorderFactory.createMatteBorder(0,1,0,1,AppThemeColor.BORDER),
                            BorderFactory.createEmptyBorder(0,3,0,1)));
                    owner.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    itemButton.setBorder(BorderFactory.createEmptyBorder(0,4,0,2));
                    owner.repaint();
                }
            });
            tradePanel.add(itemButton,BorderLayout.CENTER);
        }else if(message instanceof CurrencyMessage){
            CurrencyMessage message = (CurrencyMessage) this.message;
            JPanel curCountPanel = new JPanel();
            curCountPanel.setPreferredSize(new Dimension(40,34));
            curCountPanel.setBackground(AppThemeColor.TRANSPARENT);

            String curCount = message.getCurrForSaleCount() % 1 == 0 ? String.valueOf(message.getCurrForSaleCount().intValue()) : String.valueOf(message.getCurrForSaleCount());
            JLabel priceLabel = componentsFactory.getTextLabel(FontStyle.BOLD, AppThemeColor.TEXT_MESSAGE, TextAlignment.CENTER, 17f, curCount);
            curCountPanel.add(priceLabel);
            JLabel currencyLabel;
            currencyLabel = componentsFactory.getIconLabel("currency/" + message.getCurrForSaleTitle() + ".png", 26);
            JPanel curPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            curPanel.setBackground(AppThemeColor.TRANSPARENT);
            curPanel.add(curCountPanel);
            curPanel.add(currencyLabel);
            curPanel.setBorder(BorderFactory.createMatteBorder(4,0,0,0,AppThemeColor.TRANSPARENT));
            tradePanel.add(curPanel,BorderLayout.CENTER);
        }

        JPanel forPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        forPanel.setBackground(AppThemeColor.TRANSPARENT);

        JLabel separator = componentsFactory.getTextLabel(FontStyle.BOLD, AppThemeColor.TEXT_MESSAGE, TextAlignment.CENTER, 18f, "=>");
        separator.setHorizontalAlignment(SwingConstants.CENTER);
        forPanel.add(separator);
        String curCount = " ";
        if(message.getCurCount() > 0) {
            curCount = message.getCurCount() % 1 == 0 ? String.valueOf(message.getCurCount().intValue()) : String.valueOf(message.getCurCount());
        }
        String currency = message.getCurrency();
        if(!Objects.equals(curCount, "") && currency != null) {
            JPanel curCountPanel = new JPanel();
            curCountPanel.setPreferredSize(new Dimension(40,34));
            curCountPanel.setBackground(AppThemeColor.TRANSPARENT);

            JLabel priceLabel = componentsFactory.getTextLabel(FontStyle.BOLD, AppThemeColor.TEXT_MESSAGE, TextAlignment.CENTER, 17f, curCount);
            curCountPanel.add(priceLabel);
            JLabel currencyLabel = componentsFactory.getIconLabel("currency/" + currency + ".png", 26);
            JPanel curPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            curPanel.setBackground(AppThemeColor.TRANSPARENT);
            curPanel.add(curCountPanel);
            curPanel.add(currencyLabel);
            forPanel.add(curPanel);
        }
        tradePanel.add(forPanel,BorderLayout.LINE_END);
        labelsPanel.add(tradePanel);
        String offer = message.getOffer();
        if(offer != null && offer.trim().length() > 0) {
            JLabel offerLabel = componentsFactory.getTextLabel(FontStyle.BOLD, AppThemeColor.TEXT_MESSAGE, TextAlignment.CENTER, 17f, offer);
            offerLabel.setAlignmentY(Component.TOP_ALIGNMENT);
            labelsPanel.add(offerLabel);
        }
        return labelsPanel;
    }
    private JPanel getWhisperPanel(){
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(AppThemeColor.MSG_HEADER);

        whisperLabel = componentsFactory.getTextLabel(FontStyle.BOLD,cachedWhisperColor, TextAlignment.LEFTOP,15f,whisper + ":");
        Border border = whisperLabel.getBorder();
        whisperLabel.setBorder(new CompoundBorder(border,new EmptyBorder(0,0,0,5)));
        whisperLabel.setVerticalAlignment(SwingConstants.CENTER);

        JPanel nickNamePanel = componentsFactory.getTransparentPanel(new BorderLayout());
        if(style.equals(MessagePanelStyle.HISTORY)){
            nickNamePanel.add(whisperLabel,BorderLayout.CENTER);
        }else {
            JPanel buttonWrapper = componentsFactory.getTransparentPanel(new FlowLayout(FlowLayout.CENTER));
            buttonWrapper.setBorder(BorderFactory.createEmptyBorder(1,0,0,0));
            buttonWrapper.add(getExpandButton());
            if(!style.equals(MessagePanelStyle.SP_MODE)) {
                nickNamePanel.add(buttonWrapper, BorderLayout.LINE_START);
            }
            nickNamePanel.add(whisperLabel,BorderLayout.CENTER);
        }
        topPanel.add(nickNamePanel,BorderLayout.CENTER);

        JPanel interactionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        interactionPanel.setBorder(BorderFactory.createEmptyBorder(1,0,1,0));
        interactionPanel.setBackground(AppThemeColor.TRANSPARENT);
        interactionPanel.add(getTimePanel());
        if(!style.equals(MessagePanelStyle.HISTORY)) {
            JButton inviteButton = componentsFactory.getIconButton("app/invite.png", 14, AppThemeColor.MSG_HEADER, TooltipConstants.INVITE);
            inviteButton.addActionListener(e -> {
                EventRouter.CORE.fireEvent(new ChatCommandEvent("/invite " + whisper));
                if(message instanceof ItemMessage) {
                    copyItemNameToClipboard(((ItemMessage) message).getItemName());
                    if (((ItemMessage) message).getTabName() != null) {
                        EventRouter.UI.fireEvent(new ShowItemGridEvent((ItemMessage) message));
                    }
                }
            });
            JButton kickButton = componentsFactory.getIconButton("app/kick.png", 14, AppThemeColor.MSG_HEADER, TooltipConstants.KICK);
            kickButton.addActionListener(e -> {
                EventRouter.CORE.fireEvent(new ChatCommandEvent("/kick " + whisper));
                if(ConfigManager.INSTANCE.isDismissAfterKick()){
                    Timer timer = new Timer(30, action -> {
                        EventRouter.UI.fireEvent(new CloseMessagePanelEvent(MessagePanel.this, message));
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            });
            tradeButton = componentsFactory.getIconButton("app/trade.png", 14, AppThemeColor.MSG_HEADER, TooltipConstants.TRADE);
            tradeButton.addActionListener(e ->
                    EventRouter.CORE.fireEvent(new ChatCommandEvent("/tradewith " + whisper)));
            interactionPanel.add(inviteButton);
            interactionPanel.add(kickButton);
            interactionPanel.add(tradeButton);
        }else {
            JButton reloadButton = componentsFactory.getIconButton("app/reload-history.png", 14, AppThemeColor.MSG_HEADER, "Restore");
            reloadButton.addActionListener(e -> {
                ((HistoryContainer)owner).onReloadMessage(MessagePanel.this);
            });
            interactionPanel.add(reloadButton);
        }
        JButton openChatButton = componentsFactory.getIconButton("app/openChat.png", 14, AppThemeColor.MSG_HEADER, TooltipConstants.OPEN_CHAT);
        openChatButton.setToolTipText("Open chat");
        openChatButton.addActionListener(e ->
                EventRouter.CORE.fireEvent(new OpenChatEvent(whisper)));

        interactionPanel.add(openChatButton);
        JButton hideButton = componentsFactory.getIconButton("app/close.png", 14, AppThemeColor.MSG_HEADER, TooltipConstants.HIDE_PANEL);
        hideButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    EventRouter.UI.fireEvent(new CloseMessagePanelEvent(MessagePanel.this, message));
                }
            }
        });
        if(!style.equals(MessagePanelStyle.HISTORY) && !style.equals(MessagePanelStyle.SP_MODE)) {
            interactionPanel.add(hideButton);
        }

        topPanel.add(interactionPanel,BorderLayout.LINE_END);
        return topPanel;
    }
    private JPanel getTimePanel(){
        JPanel panel = new JPanel();
        panel.setBackground(AppThemeColor.TRANSPARENT);
        timeLabel = componentsFactory.getTextLabel(FontStyle.BOLD, AppThemeColor.TEXT_MISC, TextAlignment.CENTER, 14, cachedTime);
        if(timeAgo == null) {
            timeAgo = new Timer(60000, new ActionListener() {
                private int minute = 0;
                private int hours = 0;
                private int day = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    String labelText = "";
                    minute++;
                    if (minute > 60) {
                        hours++;
                        minute = 0;
                        if (hours > 24) {
                            day++;
                            hours = 0;
                        }
                    }
                    if (hours == 0 && day == 0) {
                        labelText = minute + "m";
                    } else if (hours > 0) {
                        labelText = hours + "h " + minute + "m";
                    } else if (day > 0) {
                        labelText = day + "d " + hours + "h " + minute + "m";
                    }
                    timeLabel.setText(labelText);
                    EventRouter.UI.fireEvent(new RepaintEvent.RepaintMessageFrame());
                }
            });
            timeAgo.start();
        }
        panel.add(timeLabel);
        return panel;
    }
    public void disableTime(){
        if(timeAgo != null) {
            timeAgo.stop();
            timeLabel.setText("");
        }
    }
    private JButton getExpandButton(){
        String iconPath = "app/default-mp.png";
        expandButton = componentsFactory.getIconButton(iconPath, 18, AppThemeColor.MSG_HEADER,"");
        expandButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    if (!messagePanel.isVisible()) {
                        expand();
                    } else {
                        collapse();
                    }
                }
            }
        });
        return expandButton;
    }
    public void expand(){
        expanded = true;
        if(style.equals(MessagePanelStyle.DOWNWARDS_SMALL)) {
            expandButton.setIcon(componentsFactory.getIcon("app/expand-mp.png", 18));
            messagePanel.setVisible(true);
            customButtonsPanel.setVisible(true);
        }else {
            expandButton.setIcon(componentsFactory.getIcon("app/collapse-mp.png", 18));
            messagePanel.setVisible(true);
            customButtonsPanel.setVisible(true);
        }
        setMaximumSize(new Dimension(Integer.MAX_VALUE,getPreferredSize().height));
        setMinimumSize(new Dimension(Integer.MAX_VALUE,getPreferredSize().height));
        ((MessagesContainer)owner).onExpandMessage();
    }
    public void collapse(){
        expanded = false;
        if(style.equals(MessagePanelStyle.DOWNWARDS_SMALL)) {
            expandButton.setIcon(componentsFactory.getIcon("app/default-mp.png", 18));
            messagePanel.setVisible(false);
            customButtonsPanel.setVisible(false);
        }else {
            expandButton.setIcon(componentsFactory.getIcon("app/default-mp.png", 18));
            messagePanel.setVisible(false);
            customButtonsPanel.setVisible(false);
        }
        setMaximumSize(new Dimension(Integer.MAX_VALUE,getPreferredSize().height));
        setMinimumSize(new Dimension(Integer.MAX_VALUE,getPreferredSize().height));
        ((MessagesContainer)owner).onCollapseMessage();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setStyle(MessagePanelStyle style) {
        this.style = style;
        this.cachedTime = timeLabel.getText();
        init();
        setMaximumSize(new Dimension(Integer.MAX_VALUE,getPreferredSize().height));
        setMinimumSize(new Dimension(Integer.MAX_VALUE,getPreferredSize().height));
    }

    public MessagePanelStyle getStyle() {
        return style;
    }
    private JPanel getButtonsPanel(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(AppThemeColor.TRANSPARENT);
        initResponseButtons(panel);
        return panel;
    }
    @Override
    public void initHandlers() {
        EventRouter.CORE.registerHandler(PlayerJoinEvent.class, event -> {
            SwingUtilities.invokeLater(()-> {
                String nickName = ((PlayerJoinEvent) event).getNickName();
                if(nickName.equals(whisper)){
                    whisperLabel.setForeground(AppThemeColor.TEXT_SUCCESS);
                    cachedWhisperColor = AppThemeColor.TEXT_SUCCESS;
                    if(!style.equals(MessagePanelStyle.HISTORY)) {
                        tradeButton.setEnabled(true);
                    }
                    EventRouter.UI.fireEvent(new RepaintEvent.RepaintMessageFrame());
                }
            });
        });
        EventRouter.CORE.registerHandler(PlayerLeftEvent.class, event -> {
            SwingUtilities.invokeLater(()-> {
                String nickName = ((PlayerLeftEvent) event).getNickName();
                if (nickName.equals(whisper)) {
                    whisperLabel.setForeground(AppThemeColor.TEXT_DISABLE);
                    cachedWhisperColor = AppThemeColor.TEXT_DISABLE;
                    if (!style.equals(MessagePanelStyle.HISTORY)) {
                        tradeButton.setEnabled(false);
                    }
                    EventRouter.UI.fireEvent(new RepaintEvent.RepaintMessageFrame());
                }
            });
        });
        EventRouter.UI.registerHandler(CustomButtonsChangedEvent.class, event -> {
            this.customButtonsPanel.removeAll();
            initResponseButtons(customButtonsPanel);
            owner.repaint();
        });
    }
    private void initResponseButtons(JPanel panel){
        List<ResponseButton> buttonsConfig = ConfigManager.INSTANCE.getButtonsConfig();
        Collections.sort(buttonsConfig);
        buttonsConfig.forEach((buttonConfig)->{
            JButton button = componentsFactory.getBorderedButton(buttonConfig.getTitle());
            button.setFont(componentsFactory.getFont(FontStyle.BOLD).deriveFont(15f));
            button.addActionListener(e -> {
                EventRouter.CORE.fireEvent(new ChatCommandEvent("@" + whisper + " " + buttonConfig.getResponseText()));
                if(buttonConfig.isClose() && !style.equals(MessagePanelStyle.SP_MODE)){
                    Timer timer = new Timer(30, action -> {
                        EventRouter.UI.fireEvent(new CloseMessagePanelEvent(MessagePanel.this, message));
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            });
            panel.add(button);
        });
    }
    private void copyItemNameToClipboard(String itemName){
        Timer timer = new Timer(30, action -> {
            StringSelection selection = new StringSelection(itemName);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        });
        timer.setRepeats(false);
        timer.start();
    }
}
