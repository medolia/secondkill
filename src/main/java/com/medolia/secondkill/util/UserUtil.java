package com.medolia.secondkill.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.medolia.secondkill.domain.SeckillUser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 生成 压测需要的 token 文件，存入 redis
 */
public class UserUtil {

    private static final String TOKEN_PATH = "/Documents/JmeterFiles/tokens.txt";

    private static void createUser(int count) throws Exception {
        List<SeckillUser> users = new ArrayList<>(count);

        // 生成用户
        for (int i = 0; i < count; i++) {
            SeckillUser user = new SeckillUser();
            user.setId(14000000000L + i);
            user.setLoginCount(1);
            user.setNickname("user" + i);
            user.setRegisterDate(new Date());
            user.setSalt("1a2b3c");
            user.setPassword(MD5Util.inputPassToDBPass("123456", user.getSalt()));
            users.add(user);
        }

        // 将用户记录插入数据库
        // insertUsersToDB(users);

        // 生成 token，存储于本地文件中
        createToken(count, users);
    }

    private static void createToken(int count, List<SeckillUser> users) throws IOException {
        String urlString = "http://localhost:8080/login/do_login";
        File file = new File(TOKEN_PATH);
        if (file.exists()) file.delete();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        for (int i = 0; i < count; i++) {
            SeckillUser user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            OutputStream out = conn.getOutputStream();
            String params = "mobile=" + user.getId() + "&password=" + MD5Util.inputPassToFormPass("123456");
            out.write(params.getBytes());
            out.flush();
            InputStream in = conn.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = in.read(buff)) >= 0) {
                bout.write(buff, 0, len);
            }
            in.close();
            bout.close();
            String response = bout.toString();
            JSONObject jo = JSON.parseObject(response);
            String token = jo.getString("data");
            System.out.println("create token: " + user.getId());

            String row = user.getId() + "," + token;
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            System.out.println("write to file: " + user.getId());
        }
        raf.close();

        System.out.println(count + " tokens created.");
    }

    private static void insertUsersToDB(List<SeckillUser> users) throws Exception {
        Connection conn = DBUtil.getConn();
        String sql = "insert into seckill_user(id, nickname, password, salt, register_date, login_count) "
                + "values(?, ?, ?, ?, ?, ?) ";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (SeckillUser user : users) {
            pstmt.setLong(1, user.getId());
            pstmt.setString(2, user.getNickname());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getSalt());
            pstmt.setTimestamp(5, new Timestamp(user.getRegisterDate().getTime()));
            pstmt.setInt(6, user.getLoginCount());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        pstmt.close();
        conn.close();
        System.out.println("" + users.size() + " users inserted into DB.");
    }

    public static void main(String[] args) throws Exception {
        createUser(5000);
    }
}
