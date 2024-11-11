package com.hatsunemiku;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class MockSendMessage {
    public static void main(String[] args) throws IOException, SmackException, XMPPException, InterruptedException {
        System.out.println("Starting XMPP client...");

        // 配置 XMPP 连接
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword("2", "2") // 用户名和密码
                .setXmppDomain("sjeary") // XMPP 域名
                .setHost("127.0.0.1") // Openfire 服务器 IP 地址
                .setPort(5222) // 默认 XMPP 端口
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled) // 禁用 TLS
                .build();

        // 创建并连接 XMPP 会话
        AbstractXMPPConnection connection = new XMPPTCPConnection(config);
        connection.connect(); // 建立连接
        connection.login(); // 登录

        System.out.println("Connected and logged in as user 2.");

        // 创建 ChatManager 并添加消息监听器
        ChatManager chatManager = ChatManager.getInstanceFor(connection);

        // 添加消息监听器，监听来自任何用户的消息
        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                // 输出收到的消息内容
                System.out.println("New message from " + from + ": " + message.getBody());
            }
        });

        // 目标用户的 JID
        EntityBareJid recipientJid = JidCreate.entityBareFrom("1@sjeary"); // 目标用户 JID
        Chat chat = chatManager.chatWith(recipientJid);

        // 启动新的线程读取命令行输入并发送消息
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("You can now type messages to send to the user '1@sjeary':");

            while (true) {
                String messageToSend = scanner.nextLine(); // 读取用户输入的消息
                try {
                    chat.send(messageToSend); // 发送消息
                    System.out.println("Sent message: " + messageToSend);
                } catch (SmackException.NotConnectedException | InterruptedException e) {
                    System.err.println("Failed to send message: " + e.getMessage());
                }
            }
        }).start();

        // 保持程序运行
        CountDownLatch latch = new CountDownLatch(1); // 创建一个 CountDownLatch 用于阻塞主线程
        latch.await(); // 阻塞主线程，保持连接和消息监听
    }
}
