package com.example.taskmanager.model;
import com.example.taskmanager.JDBCPostgreSQL_config;

import javax.persistence.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flow")
public class Flow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;
    private Long type_id;
    private int key;
    private Long task_id;
    private Long parent;

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    private boolean result;

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

    public int getCountjobs( Long id )
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

    public void insertFlow(Flow flow )
    {
        try
        {
            Connection connection = null;
            connection = DriverManager.getConnection(JDBCPostgreSQL_config.DB_URL, JDBCPostgreSQL_config.USER, JDBCPostgreSQL_config.PASS);
            PreparedStatement ps;
            ps = connection.prepareStatement( "INSERT INTO flow (type_id, key, task_id, parent, result) VALUES (?,?,?,?,?)" );
            ps.setLong( 1, flow.getType_id() );
            ps.setInt( 2, flow.getKey() );
            ps.setLong( 3, flow.getTask_id() );
            ps.setLong( 4, flow.getParent() );
            ps.setBoolean(5,flow.getResult() );

            ps.executeUpdate();
            ps.close();
            connection.close();
        }
        catch( SQLException e )
        {
            e.printStackTrace();
        }
    }

    public void deleteFlow(Long id )
    {
        try
        {
            Connection connection = null;
            connection = DriverManager.getConnection(JDBCPostgreSQL_config.DB_URL, JDBCPostgreSQL_config.USER, JDBCPostgreSQL_config.PASS);
            PreparedStatement ps;
            ps = connection.prepareStatement( "DELETE from flow Where id=?" );
            ps.setLong( 1, id);
            ps.executeUpdate();
            ps.close();
            connection.close();
        }
        catch( SQLException e )
        {
            e.printStackTrace();
        }
    }

    public void deleteChildren(Long key){
        Long id = (long) 0;
        try
        {
            Connection connection = DriverManager.getConnection(JDBCPostgreSQL_config.DB_URL, JDBCPostgreSQL_config.USER, JDBCPostgreSQL_config.PASS);
            PreparedStatement ps = connection.prepareStatement( "SELECT * FROM flow WHERE parent=?" );
            ps.setLong( 1, key );

            ResultSet rs = ps.executeQuery();
            while( rs.next() )
            {
                id = ( rs.getLong("id") );
                key = ( rs.getLong("key"));
                deleteFlow(id);
                deleteChildren(key);
            }
            rs.close();
            connection.close();
        }
        catch( SQLException e )
        {
            e.printStackTrace();
        }
    }

    public void getJobId(Long task_id, int key, int n){
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
        setChildren(0, result, key, n);
    }

    public void setChildren(int level, List<Integer> result, int key, int n){
        int deltakey = (int) Math.pow( 2, n - level -1);
        System.out.println("Глубина : "+level);
        System.out.println("Айди задания! : "+result.get(level));
        //Flow flow = new Flow();
        //this.setType_id(task_id);
        this.setTask_id((long) result.get(level));
        this.setParent((long) key);
        this.setKey(key - deltakey);
        this.setResult(true);
        insertFlow(this);
        this.setKey(key + deltakey);
        this.setResult(false);
        insertFlow(this);
        if (level == n - 1) { return; }
        setChildren(level+1,result,key - deltakey, n);
        setChildren(level+1,result,key + deltakey, n);
    }

    public void createFlow(Long task_id){

        int n = getCountjobs(getType(task_id));
        System.out.println("Кол-во задач : "+n);
        System.out.println("Айди задания"+task_id);
        //Flow flow = new Flow();
        this.setType_id(task_id);
        int key = (int)Math.pow( 2, n);
        this.setKey(key);
        this.setTask_id(task_id);
        this.setParent(Long.valueOf("0"));
        this.setResult(true);
        insertFlow(this);
        getJobId(task_id, key, n);
    }

    public void deleteBranch(Long jobid, boolean result){
        try{
            Long task = getTask(jobid);
            Long id = (long) 0;
            Long key = (long) 0;

            Connection connection = DriverManager.getConnection(JDBCPostgreSQL_config.DB_URL, JDBCPostgreSQL_config.USER, JDBCPostgreSQL_config.PASS);
            PreparedStatement ps = connection.prepareStatement( "SELECT * FROM flow WHERE task_id=? and type_id=? and result=? and parent > 0" );
            ps.setLong( 1, jobid );
            ps.setLong( 2, task );
            ps.setBoolean( 3, !result );
            ResultSet rs = ps.executeQuery();
            if ( rs.next() )
            {
                id = ( rs.getLong("id") );
                key = ( rs.getLong("key"));
                deleteFlow(id);
            }
            rs.close();
            connection.close();
            deleteChildren(key);
        }
        catch( SQLException e )
        {
            e.printStackTrace();
        }
    }

    public Long getType(Long task_id){
        Long type = (long) -1;
        try
        {
            Connection connection = DriverManager.getConnection(JDBCPostgreSQL_config.DB_URL, JDBCPostgreSQL_config.USER, JDBCPostgreSQL_config.PASS);
            PreparedStatement ps = connection.prepareStatement( "SELECT type_id FROM task WHERE id=?" );
            ps.setLong( 1, task_id );
            ResultSet rs = ps.executeQuery();
            while( rs.next() )
            {
                type = ( rs.getLong("type_id") );
            }
            rs.close();
            connection.close();

        }
        catch( SQLException e )
        {
        e.printStackTrace();
        }
        return type;
    }

    public Long getTask(Long job_id){
        Long task = (long) -1;
        try
        {
            Connection connection = DriverManager.getConnection(JDBCPostgreSQL_config.DB_URL, JDBCPostgreSQL_config.USER, JDBCPostgreSQL_config.PASS);
            PreparedStatement ps = connection.prepareStatement( "SELECT task_id FROM job WHERE id=?" );
            ps.setLong( 1, job_id );
            ResultSet rs = ps.executeQuery();
            while( rs.next() )
            {
                task = ( rs.getLong("task_id") );
            }
            rs.close();
            connection.close();

        }
        catch( SQLException e )
        {
            e.printStackTrace();
        }
        return task;
    }
}
