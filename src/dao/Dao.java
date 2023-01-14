package dao;

import java.sql.*;

public class Dao {
    protected static String DB_CLASSNAME = "com.mysql.cj.jdbc.Driver";
    // 连接数据库的URL
    protected static String DB_URL = "jdbc:mysql://localhost:3306/history?useSSL=false&serverTimezone=UTC";
    // 用户名
    protected static String DB_USER = "root";
    // 密码
    protected static String DB_PWD = "123456";
    // 创建表的SQL语句
    private static final String CREATE_SQL =
            "create table if not exists record(id int not null auto_increment primary key," +
            "sender varchar(20) not null,message text null," +
            "receiver varchar(100) not null)";
    // 声明Connection对象
    private static Connection conn = null;

    public static void create() {
        if (conn == null) {
            try {
                // 加载驱动程序
                Class.forName(DB_CLASSNAME);
                // 获取与数据库的连接
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PWD);
                // 创建Statement对象
                Statement stmt = conn.createStatement();
                // 执行创建表的SQL语句
                stmt.execute(CREATE_SQL);
                System.out.println("数据库已连接");
                stmt.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 插入数据
     * @param from : 发送方
     * @param to : 内容
     * @param message : 接收方
     */
    public static void insert(String from,String to,String message) {
        String sql = "insert into record(sender,message,receiver) values(?,?,?)";
        try {
            // 创建预处理对象
            PreparedStatement pst = conn.prepareStatement(sql);
            // 设置参数
            pst.setString(1,from);
            pst.setString(2,message);
            pst.setString(3,to);
            // 执行
            pst.executeUpdate();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

