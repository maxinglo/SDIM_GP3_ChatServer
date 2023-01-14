package main;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    // 默认绑定端口
    public static final int PORT = 20200;
    // 从属线程链表
    private static ArrayList<MessageThread> clients = new ArrayList<>();
    // 创建服务器套接字
    private ServerSocket serverSocket;
    // 服务器主线程
    private ServerThread serverThread;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 实例化服务器套接字
        serverThread = new ServerThread(serverSocket);
        // 主线程开启
        new Thread(serverThread).start();
        // 获取链接并创建表
        System.out.println("服务器已开启");
    }

    public static void main(String[] args) {
        new Server();
    }

    /**
     * 主线程，用来等待客户的请求
     */
    class ServerThread implements Runnable {
        private ServerSocket serverSocket;

        ServerThread(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            while (true) {
                Socket socket = null;
                try {
                    // 等待客户机连接
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 创建该客户的消息处理线程
                MessageThread messageThread = new MessageThread(socket);
                // 开启该线程
                new Thread(messageThread).start();
                // 将此线程加入链表
                clients.add(messageThread);
                    syncPlayerList();

            }
        }
    }

    /**
     * 从属线程，处理消息
     */
    class MessageThread implements Runnable {
        private Socket socket;
        private BufferedReader reader = null;
        private PrintWriter writer = null;
        private String name;

        MessageThread(Socket socket) {
            this.socket = socket;
            try {
                System.out.println("// 实例化BufferedReader对象");
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("// 实例化PrintWriter对象");
                writer = new PrintWriter(socket.getOutputStream());
                System.out.println("// 获取用户信息");
                name = reader.readLine();
                writer.println(name + "已成功加入");
                writer.flush();
                System.out.println(name + "已成功加入");
                for (int i = clients.size() - 1; i >= 0; i--) {
                    System.out.println("// 告诉所有人有新人加入");
                    clients.get(i).getWriter().println(name + "已上线！");
                    clients.get(i).getWriter().flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(name+"断开连接");
                RemovePlayerInList(this);
                clients.remove(this);
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public PrintWriter getWriter() {
            return writer;
        }

        public BufferedReader getReader() {
            return reader;
        }

        @Override
        public void run() {
            String message;
            while (true) {
                try {
                    // 获取消息内容
                    message = reader.readLine();
                    if (message == null || "".equals(message)) {
                        continue;
                    }
                    command(name,message);
                    if ("Exit".equals(message)) { // 用户退出，清空连接
                        if (reader != null) {
                            reader.close();
                        }
                        if (writer != null) {
                            writer.close();
                        }
                        if (socket != null) {
                            socket.close();
                        }
                        RemovePlayerInList(this);
                        clients.remove(this);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("检查");
                try {
                    if (socket != null) {
                        socket.sendUrgentData(0xFF);
                    }
                    System.out.println("检查");
                } catch (IOException e) {
                    try {
                        if (reader != null) {

                            reader.close();
                            if (writer != null) {
                                writer.close();
                            }
                            if (socket != null) {
                                socket.close();
                            }}} catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    RemovePlayerInList(this);
                        clients.remove(this);
                        throw new RuntimeException(e);

                    }
                }
            }
        }
    public static void syncPlayerList() {
        for (int i = clients.size() - 1; i >= 0; i--) {
            if(clients.get(i).getName().equals("admin")) {
                clients.get(i).getWriter().println("!SyncPlayerList:" + getFormattedList(":"));
                clients.get(i).getWriter().flush();
            }
        }
    }

    public static void RemovePlayerInList(MessageThread messageThread) {
        for (int i = clients.size() - 1; i >= 0; i--) {
            if(clients.get(i).getName().equals("admin"))
            clients.get(i).getWriter().println("!RemovePlayerInList:" + messageThread.getName());
            clients.get(i).getWriter().flush();
        }
    }
    public static String getFormattedList(String dot){
        String formattedListOfPlayers = "";
        for (int i = 0; i < clients.size(); i++) {
            if (!clients.get(i).getName().equals("admin")){
                if (i < clients.size() - 1) {
                    formattedListOfPlayers = formattedListOfPlayers + clients.get(i).getName() + dot;
                } else {
                    formattedListOfPlayers = formattedListOfPlayers + clients.get(i).getName();
                }
            }
        }
        return formattedListOfPlayers;
    }
    public static void command(String name,String msg){
        /*if (msg.startsWith("/")){
            if (msg.startsWith("/a")){
                
            } else if (msg.startsWith("/b")) {
                
            }
        } else*/
        if (msg.startsWith("@")) {
            String[] args = msg.split(":");
            String clientName = args[0].replace("@","");
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).getName().equals(clientName)){
                    clients.get(i).getWriter().println(args[1]);
                    clients.get(i).getWriter().flush();
                }
            }
        } else{
            sendMessage(name,msg);
        }
    }
    public static void sendMessage(String name,String msg){
        for (int i = clients.size() - 1; i >= 0; i--) {
            // 向各个用户发送消息
            clients.get(i).getWriter().println(name + ": " + msg);
            clients.get(i).getWriter().flush();
            // 保存到数据库
            /*Dao.insert(name,clients.get(i).getName(),message);*/
        }
    }
}

