package com.example.taskmanager.model;
import com.example.taskmanager.JDBCPostgreSQL_config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Flow {

    private Long type_id;
    private int key;
    private Long task_id;
    private Long parent;

    public Long getType_id() {
        return type_id;
    }

    public int getKey() {
        return key;
    }

    public Long getTask_id() {
        return task_id;
    }

    public Long getParent() {
        return parent;
    }

    public void setType_id(Long type_id) {
        this.type_id = type_id;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setTask_id(Long task_id) {
        this.task_id = task_id;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public static int getCountjobs( Long id )
    {
        int result = 0;
        try
        {
            Connection connection = null;
            connection = DriverManager.getConnection(JDBCPostgreSQL_config.DB_URL, JDBCPostgreSQL_config.USER, JDBCPostgreSQL_config.PASS);
            //PreparedStatement ps;
            System.out.println(connection);
            PreparedStatement ps = connection.prepareStatement( "SELECT COUNT(*) as count FROM job WHERE task_id=(SELECT id FROM task WHERE type_id=?)" );
            ps.setLong( 1, id );

            ResultSet rs = ps.executeQuery();
            System.out.println(rs);
            if (rs.next()) {
                System.out.println(rs);
                result = rs.getInt("count");
            }
            ps.close();

            connection.close();
        }
        catch( SQLException e )
        {
            e.printStackTrace();
        }

        return result;
    }

    public static void updateContract( Flow flow )
    {
        try
        {
            Connection connection = null;
            connection = DriverManager.getConnection(JDBCPostgreSQL_config.DB_URL, JDBCPostgreSQL_config.USER, JDBCPostgreSQL_config.PASS);
            PreparedStatement ps;
            ps = connection.prepareStatement( "INSERT INTO flow (type_id, key, task_id, parent) VALUES (?,?,?,?)" );
            ps.setLong( 1, flow.getType_id() );
            ps.setInt( 2, flow.getKey() );
            ps.setLong( 3, flow.getTask_id() );
            ps.setLong( 4, flow.getParent() );

            ps.executeUpdate();
            ps.close();
            connection.close();
        }
        catch( SQLException e )
        {
            e.printStackTrace();
        }
    }

    public static void cheakchildren(Long task_id, int key, int n){
        List<Integer> result = new ArrayList<Integer>();
        try
        {
            System.out.println("таск айди  : " + task_id);
            Connection connection = DriverManager.getConnection(JDBCPostgreSQL_config.DB_URL, JDBCPostgreSQL_config.USER, JDBCPostgreSQL_config.PASS);
            PreparedStatement ps = connection.prepareStatement( "SELECT * FROM job WHERE task_id=?" );
            ps.setLong( 1, task_id );

            ResultSet rs = ps.executeQuery();
            while( rs.next() )
            {
                result.add( rs.getInt("id") );
            }
            rs.close();

            connection.close();
            System.out.println("Размер массива интеджерей  : " + result.size());
        }
        catch( SQLException e )
        {
            e.printStackTrace();
        }
        doJobsflow(0, result, key, n, task_id);
    }

    public static void doJobsflow(int level, List<Integer> result, int key, int n, Long task_id){
        int deltakey = (int)Math.pow( 2, n - level -1);
        System.out.println("Глубина : "+level);
        System.out.println("Айди задания! : "+result.get(level));
        Flow flow = new Flow();
        flow.setType_id(task_id);
        flow.setTask_id(Long.valueOf(result.get(level)));
        flow.setParent((long) key);
        flow.setKey(key - deltakey);
        updateContract(flow);
        flow.setKey(key + deltakey);
        updateContract(flow);
        if (level == n - 1) { return; }
        doJobsflow(level+1,result,key - deltakey, n, task_id);
        doJobsflow(level+1,result,key + deltakey, n, task_id);
    }

    public static void createFlow(Long type_id){

        int n = getCountjobs(Long.valueOf("1000"));
        System.out.println("Кол-во задач : "+n);
        System.out.println("Айди задания"+type_id);
        Flow flow = new Flow();
        flow.setType_id(type_id);
        int key = (int)Math.pow( 2, n);
        flow.setKey(key);
        flow.setTask_id(type_id);
        flow.setParent(Long.valueOf("0"));
        updateContract(flow);
        cheakchildren(type_id, key, n);
    }
}
