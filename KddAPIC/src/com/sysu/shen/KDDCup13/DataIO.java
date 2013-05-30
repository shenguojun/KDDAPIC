package com.sysu.shen.KDDCup13;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import weka.core.*;
import weka.filters.Filter;

public class DataIO {

    public static void main(String[] argv) throws Exception {
        // 
    }

    public static Connection getDBConn() {
        try {
            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(
                    Config.connString,
                    Config.connUser,
                    Config.connPassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static Instances getDatasetDB(String tableName) throws Exception {
        Statement st = null;
        ResultSet rs = null;
        Connection conn = getDBConn();
        String query = getFeatureQuery(tableName);
        st = conn.createStatement();
        rs = st.executeQuery(query);

        ResultSetMetaData rsmd = rs.getMetaData();

        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        int numAtts = rsmd.getColumnCount();
        for (int i = 1; i <= numAtts; i++) {
            String attName = (rsmd.getColumnName(i));
            Attribute att = new Attribute(attName);
            attributes.add(att);
        }

        Instances data = new Instances(tableName, attributes, 0);

        weka.filters.unsupervised.attribute.Add addAtt = new weka.filters.unsupervised.attribute.Add();
        addAtt.setOptions(weka.core.Utils.splitOptions("-T NOM -N class -L T,F -C last"));
        addAtt.setInputFormat(data);

        data = Filter.useFilter(data, addAtt);
        while (rs.next()) {
            double[] values = new double[numAtts + 1];
            for (int i = 1; i <= numAtts; i++) {
                values[i - 1] = rs.getDouble(i);
            }
            data.add(new DenseInstance(1.0, values));
        }
        return data;
    }

    private static String readFile(String path) throws Exception {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }

    public static String getFeatureQuery(String tableName) throws Exception {
        String q = readFile(Config.sqlFilePath);
        q = q.replaceAll("##DataTable##", tableName);
        return q;
    }
}