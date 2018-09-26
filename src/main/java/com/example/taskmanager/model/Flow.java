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
    private Long job_id;
    private Long parent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private boolean getResult() {
        return result;
    }

    private void setResult(boolean result) {
        this.result = result;
    }

    private boolean result;

    private Long getType_id() {
        return type_id;
    }

    private int getKey() {
        return key;
    }

    private Long getJob_id() {
        return job_id;
    }

    private Long getParent() {
        return parent;
    }

    private void setType_id(Long type_id) {
        this.type_id = type_id;
    }

    private void setKey(int key) {
        this.key = key;
    }

    private void setJob_id(Long job_id) {
        this.job_id = job_id;
    }

    private void setParent(Long parent) {
        this.parent = parent;
    }

    // Возвращает кол-во работ в задании
    private int getCountjobs(Long id)
    {
        int result = 0;
        try
        {
            Connection connection;
            connection = DriverManager.getConnection(JDBCPostgreSQL_config.DB_URL, JDBCPostgreSQL_config.USER, JDBCPostgreSQL_config.PASS);
            PreparedStatement ps = connection.prepareStatement( "SELECT COUNT(*) as count FROM job WHERE task_id=(SELECT id FROM task WHERE type_id=?)" );
            ps.setLong( 1, id );

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
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

    // Вставляет строку в таблицу flow
    private void insertFlow(Flow flow)
    {
        try
        {
            Connection connection;
            connection = DriverManager.getConnection(JDBCPostgreSQL_config.DB_URL, JDBCPostgreSQL_config.USER, JDBCPostgreSQL_config.PASS);
            PreparedStatement ps;
            ps = connection.prepareStatement( "INSERT INTO flow (type_id, key, job_id, parent, result) VALUES (?,?,?,?,?)" );
            ps.setLong( 1, flow.getType_id() );
            ps.setInt( 2, flow.getKey() );
            ps.setLong( 3, flow.getJob_id() );
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

    // Удаляет из таблицы flow
    private void deleteFlow(Long id)
    {
        try
        {
            Connection connection;
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

    // Рекурсивно удаляет продолжение ненужной ветки
    private void deleteChildren(Long key){
        long id;
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

    // Находит все работы по id задания и запускает setChildren
    private void getJobId(Long task_id, int key, int n){
        List<Integer> result = new ArrayList<>();
        try
        {
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

    // Рекурсивно создаёт дерево
    private void setChildren(int level, List<Integer> result, int key, int n){
        int deltakey = (int) Math.pow( 2, n - level -1);
        this.setJob_id((long) result.get(level));
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

    // Создание flow используется в controller
    public void createFlow(Long task_id){

        int n = getCountjobs(getType(task_id));
        this.setType_id(getType(task_id));
        int key = (int)Math.pow( 2, n);
        this.setKey(key);
        this.setJob_id(getType(task_id));
        this.setParent(Long.valueOf("0"));
        this.setResult(true);
        insertFlow(this);
        getJobId(task_id, key, n);
    }

    // Удаляет первый узел, который не выбрали и запускает удаление его узлов детей
    public void deleteBranch(Long jobid, boolean result){
        try{
            Long task = getTask(jobid);
            Long type = getType(task);
            long id;

            Connection connection = DriverManager.getConnection(JDBCPostgreSQL_config.DB_URL, JDBCPostgreSQL_config.USER, JDBCPostgreSQL_config.PASS);
            PreparedStatement ps = connection.prepareStatement( "SELECT * FROM flow WHERE job_id=? and type_id=? and result=? and parent > 0" );
            ps.setLong( 1, jobid );
            ps.setLong( 2, type );
            ps.setBoolean( 3, !result );
            ResultSet rs = ps.executeQuery();
            long key;
            key = (long) 0;
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

    //  id Типа по id задания
    private Long getType(Long task_id){
        long type = (long) -1;
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

    // id Задания по id задачи
    private Long getTask(Long job_id){
        long task = (long) -1;
        try
        {
            Connection connection = DriverManager.getConnection(JDBCPostgreSQL_config.DB_URL, JDBCPostgreSQL_config.USER, JDBCPostgreSQL_config.PASS);
            PreparedStatement ps = connection.prepareStatement( "SELECT task_id FROM job WHERE id=?" );
            ps.setLong( 1, job_id );
            ResultSet rs = ps.executeQuery();
            while( rs.next() )
            {
                task = ( rs.getLong("job_id") );
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
