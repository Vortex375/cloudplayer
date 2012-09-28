package de.pandaserv.music.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author: Hayato Hess
 * Date: 3/28/12
 */
class LocalPreparedStatement extends ThreadLocal<PreparedStatement> {
    //to create all PreparedStatements (to find syntax errors)
    private final static List<LocalPreparedStatement> allLocalPreparedStatements = new LinkedList<LocalPreparedStatement>();
    private final HashMap<Thread, Integer> connectionRetries = new HashMap<Thread, Integer>();
    private final String statement;
    private int autoGeneratedKeys = -1;
    private final int MAX_RETRIES=60;

    public LocalPreparedStatement(String statement) {
        this.statement = statement;

        //adds PS to a list with all prepared statement used for unitTesting
        synchronized (allLocalPreparedStatements) {
            allLocalPreparedStatements.add(this);
        }
    }

    public LocalPreparedStatement(String statement, int autoGeneratedKeys) {
        this(statement);
        this.autoGeneratedKeys = autoGeneratedKeys;
    }

    @Override
    public PreparedStatement get() {
        //throws the PS away when it or the connection was closed
        PreparedStatement ps = super.get();
        try {
            if (ps.isClosed() || !ps.getConnection().isValid(2))
                remove();
        } catch (SQLException e) {
            remove();
            e.printStackTrace();
        }

        //get the PS (this will get a new one if the PS was thrown away or when the PS was never created)
        return super.get();
    }

    @Override
    protected PreparedStatement initialValue() {
        //close the server if the retries of one thread exceed the value of MAX_RETRIES
        synchronized (connectionRetries) {
            if (connectionRetries.get(Thread.currentThread()) != null) {
                //print status
                Logger.getLogger(LocalPreparedStatement.class.getName()).log(Level.WARNING,
                        "Retrying to prepare statement after previous failure.");

                //close server if value > MAX_RETRIES
                if (connectionRetries.get(Thread.currentThread()) >= MAX_RETRIES){
                    Logger.getLogger(LocalPreparedStatement.class.getName()).log(Level.SEVERE,
                            "Unable to establish database connection"
                            + " after {0} tries! Shutting down.", MAX_RETRIES);
                    System.exit(1);
                }
            }
        }

        //result of this method will be stored in this var
        PreparedStatement preparedStatement;

        //get a new connection
        Connection connection = DatabaseManager.getInstance().getConnection();
        try {

            //creates a new Prepared statement from the connection. If autoGeneratedKeys =! -1 the Connection
            //will return the generated Keys
            if (autoGeneratedKeys == -1) {
                preparedStatement = connection.prepareStatement(statement);
            }
            else {
                preparedStatement = connection.prepareStatement(statement, autoGeneratedKeys);
            }


        } catch (SQLException e) {
            //print warning and retry
            e.printStackTrace();
            return retryInit();

        } catch (NullPointerException e) {
            //e.printStackTrace();
            return retryInit();
        }

        //getting ps was successful -> removing all retry coutners
        synchronized (connectionRetries) {
            connectionRetries.clear();
        }

        return preparedStatement;
    }

    private PreparedStatement retryInit() {
        //increase the retry counter for the current thread
        synchronized (connectionRetries) {
            int tries;
            if (connectionRetries.containsKey(Thread.currentThread())) {
                tries = connectionRetries.get(Thread.currentThread())+1;
            } else {
                tries = 1;
            }
            Logger.getLogger(LocalPreparedStatement.class.getName()).log(Level.WARNING,
                    "Failed to prepare statement. Retrying in 5 seconds ({0}/{1})",
                    new Object[] {tries, MAX_RETRIES});
            connectionRetries.put(Thread.currentThread(), tries);
        }

        //wait 5sec
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
        }

        //retry
        return initialValue();
    }

    /**
     * this will create all available preparedStatements once to find syntax errors.
     */
    public static PreparedStatement[] testAllLocalPreparedStatements() {
        List<PreparedStatement> preparedStatementList = new LinkedList<PreparedStatement>();

        for (LocalPreparedStatement localPreparedStatement : allLocalPreparedStatements) {
            preparedStatementList.add(localPreparedStatement.get());
        }

        return preparedStatementList.toArray(new PreparedStatement[preparedStatementList.size()]);
    }

}
