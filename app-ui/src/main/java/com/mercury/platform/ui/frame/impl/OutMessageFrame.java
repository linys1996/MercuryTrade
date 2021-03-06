package com.mercury.platform.ui.frame.impl;

import com.mercury.platform.core.AppStarter;
import com.mercury.platform.shared.FrameStates;
import com.mercury.platform.shared.events.EventRouter;
import com.mercury.platform.shared.events.custom.ChatCommandEvent;
import com.mercury.platform.shared.events.custom.OutTradeMessageEvent;
import com.mercury.platform.shared.pojo.CurrencyMessage;
import com.mercury.platform.shared.pojo.ItemMessage;
import com.mercury.platform.shared.pojo.Message;
import com.mercury.platform.ui.components.fields.font.FontStyle;
import com.mercury.platform.ui.components.fields.font.TextAlignment;
import com.mercury.platform.ui.frame.ComponentFrame;
import com.mercury.platform.ui.misc.AppThemeColor;
import com.mercury.platform.ui.misc.TooltipConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Константин on 12.01.2017.
 */
public class OutMessageFrame extends ComponentFrame {
    public OutMessageFrame() {
        super("MercuryTrade");
    }

    private void addNewMessage(Message message){
        JPanel root = componentsFactory.getTransparentPanel(new BorderLayout());
        root.setBackground(AppThemeColor.HEADER);
        root.setBorder(BorderFactory.createEmptyBorder(-10,0,-10,0));
        JLabel whisperLabel = componentsFactory.getTextLabel(FontStyle.BOLD, AppThemeColor.TEXT_NICKNAME, TextAlignment.LEFTOP,15f,message.getWhisperNickname() + ":");
        whisperLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                x = e.getX();
                y = e.getY();
            }
        });
        whisperLabel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                OutMessageFrame.this.setLocation(e.getLocationOnScreen().x -x,e.getLocationOnScreen().y -y);
                configManager.saveFrameLocation(OutMessageFrame.this.getClass().getSimpleName(),OutMessageFrame.this.getLocation());
            }
        });

        JPanel interactionPanel = componentsFactory.getTransparentPanel(new BorderLayout());
        JPanel miscPanel = componentsFactory.getTransparentPanel(new FlowLayout(FlowLayout.LEFT));
        miscPanel.add(componentsFactory.getTextLabel(FontStyle.BOLD, AppThemeColor.TEXT_MESSAGE, TextAlignment.CENTER, 17f, "=>"));
        if(message.getCurrency() != null) {
            miscPanel.add(getCurCountPanel(message.getCurCount(),message.getCurrency()));
        }
        if(message instanceof CurrencyMessage){
            CurrencyMessage msg = (CurrencyMessage) message;
            JPanel curCountPanel = getCurCountPanel(msg.getCurrForSaleCount(), msg.getCurrForSaleTitle());
            curCountPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
            interactionPanel.add(curCountPanel,BorderLayout.CENTER);
        }else {
            JLabel itemLabel = componentsFactory.getTextLabel(FontStyle.BOLD, AppThemeColor.TEXT_IMPORTANT, TextAlignment.CENTER, 16f, ((ItemMessage)message).getItemName());
            interactionPanel.add(itemLabel,BorderLayout.CENTER);
        }
        JButton hoIn = componentsFactory.getIconButton("app/hideout-in.png", 16, AppThemeColor.HEADER, TooltipConstants.HO_IN);
        hoIn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                EventRouter.CORE.fireEvent(new ChatCommandEvent("/hideout " + message.getWhisperNickname()));
            }
        });
        miscPanel.add(hoIn);
        JButton hoOut = componentsFactory.getIconButton("app/hideout-out.png", 16, AppThemeColor.HEADER, TooltipConstants.HO_OUT);
        hoOut.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                EventRouter.CORE.fireEvent(new ChatCommandEvent("/hideout"));
            }
        });
        miscPanel.add(hoOut);
        JButton hideButton = componentsFactory.getIconButton("app/close.png", 14, AppThemeColor.HEADER,TooltipConstants.HIDE_PANEL);
        hideButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                OutMessageFrame.this.getContentPane().remove(root);
                OutMessageFrame.this.pack();
                if(OutMessageFrame.this.getContentPane().getComponentCount() == 0){
                    OutMessageFrame.this.setAlwaysOnTop(false);
                    OutMessageFrame.this.setVisible(false);
                }
            }
        });
        miscPanel.add(hideButton);

        interactionPanel.add(miscPanel,BorderLayout.LINE_END);
        root.add(interactionPanel,BorderLayout.LINE_END);
        root.add(whisperLabel,BorderLayout.CENTER);

        this.add(root);
        pack();
    }

    private JPanel getCurCountPanel(Double curCount, String currency){
        JPanel curCountPanel = new JPanel();
        curCountPanel.setPreferredSize(new Dimension(30,20));
        curCountPanel.setBackground(AppThemeColor.TRANSPARENT);
        curCountPanel.setBorder(BorderFactory.createEmptyBorder(-8,0,-8,0));

        JLabel priceLabel = componentsFactory.getTextLabel(FontStyle.BOLD, AppThemeColor.TEXT_MESSAGE, TextAlignment.CENTER, 17f, String.valueOf(curCount));
        curCountPanel.add(priceLabel);
        JLabel currencyLabel;
        //todo
        currencyLabel = componentsFactory.getIconLabel("currency/" + currency + ".png", 26);
        JPanel curPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        curPanel.setBackground(AppThemeColor.TRANSPARENT);
        curPanel.add(curCountPanel);
        curPanel.add(currencyLabel);
        curPanel.setBorder(BorderFactory.createMatteBorder(4,0,0,0,AppThemeColor.TRANSPARENT));
        return curPanel;
    }
    @Override
    public void initHandlers() {
        EventRouter.CORE.registerHandler(OutTradeMessageEvent.class, event -> {
            if (!this.isVisible() && AppStarter.APP_STATUS == FrameStates.SHOW) {
                this.setAlwaysOnTop(true);
                this.setVisible(true);
            } else {
                prevState = FrameStates.SHOW;
            }
            Message message = ((OutTradeMessageEvent) event).getMessage();
            addNewMessage(message);
        });
    }

    @Override
    protected LayoutManager getFrameLayout() {
        return new BoxLayout(this.getContentPane(),BoxLayout.Y_AXIS);
    }


}
