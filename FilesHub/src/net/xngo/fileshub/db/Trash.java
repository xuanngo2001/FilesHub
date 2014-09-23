package net.xngo.fileshub.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.xngo.fileshub.db.Conn;
import net.xngo.fileshub.report.Chronometer;
import net.xngo.fileshub.struct.Document;

/**
 * Implement functionalities related to duplicate documents(files) in database.
 * @author Xuan Ngo
 *
 */
public class Trash
{
  private final String tablename  = "Trash";
  
  private Conn conn = Conn.getInstance();
  
  private PreparedStatement insert = null;
  private PreparedStatement select = null;
  private PreparedStatement update = null;
  private PreparedStatement delete = null;
 
 
  public void createTable()
  {
    // Create table.
    String query = this.createTableQuery();
    this.conn.executeUpdate(query);
    
    // Create indices.
    this.createIndices();
  }
  
  public void deleteTable()
  {
    // Delete table.
    String query="DROP TABLE IF EXISTS " + this.tablename;
    this.conn.executeUpdate(query);    
  }
  
  public boolean isSameFile(String canonicalPath)
  {
    return this.isStringExists("canonical_path", canonicalPath);
  }  
  
  /**
   * @deprecated This is only used by unit test. Remove this if used in application.
   * @param canonicalPath
   * @return Document UID.
   */
  public int getDuidByCanonicalPath(String canonicalPath)
  {
    return Integer.parseInt(this.getString("duid", "canonical_path", canonicalPath));
  }
  
  /**
   * @param canonicalPath
   * @return {@link Document}
   */
  public Document findDocByCanonicalPath(final String canonicalPath)
  {
    return this.findDocBy("canonical_path", canonicalPath);
  }
  
  /**
   * @param hash
   * @return {@link Document}
   */
  public Document findDocByHash(String hash)
  {
    return this.findDocBy("hash", hash);
  }
  
  /**
   * @deprecated Currently used in unit test. Otherwise, remove deprecated.
   * @return
   */
  public int getTotalDocs()
  {
    final String query = String.format("SELECT COUNT(*) FROM %s", this.tablename);
    
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      ResultSet resultSet =  this.select.executeQuery();
      if(resultSet.next())
      {
        return resultSet.getInt(1);
      }
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return 0;
  }  
  
  public int addDoc(Document doc)
  {
    return this.insertDoc(doc);
  }
  
  public int removeDoc(Document doc)
  {
    return this.deleteDoc(doc);
  }
  
  /**
   * 
   * @param filename
   * @return {@link Document}
   */
  public Document findDocByFilename(String filename)
  {
    return this.findDocBy("filename", filename);
  }
  
  /**
   * 
   * @param modifiedTime
   * @param filename
   * @return {@link Document}
   */
  public Document findDocByModifiedTimeAndFilename(long modifiedTime, String filename)
  {
    Document doc = null;
    
    final String query = String.format("SELECT duid, canonical_path, filename, last_modified, hash, comment "
                                        + " FROM %s "
                                        + "WHERE last_modified = ? and filename = ?", this.tablename);
    
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      int i=1;
      this.select.setLong(i++, modifiedTime);
      this.select.setString(i++, filename);
      
      ResultSet resultSet =  this.select.executeQuery();
      if(resultSet.next())
      {
        doc = new Document();
        int j=1;
        doc.uid             = resultSet.getInt(j++); // Shelf.uid is equal to Trash.duid.
        doc.canonical_path  = resultSet.getString(j++);
        doc.filename        = resultSet.getString(j++);
        doc.last_modified   = resultSet.getLong(j++);
        doc.hash            = resultSet.getString(j++);
        doc.comment         = resultSet.getString(j++);
        
        return doc;
      }
      else
        return doc;

    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return doc;    
  }
  
  public int markDuplicate(int duplicate, int of)
  {
    final String query = "UPDATE "+this.tablename+  " SET duid = ? WHERE duid = ?";
    
    int rowAffected = 0;
    try
    {
      // Prepare the query.
      this.update = this.conn.connection.prepareStatement(query);
      
      // Set the data.
      int i=1;
      this.update.setInt(i++, of);
      this.update.setInt(i++, duplicate);
      
      // update row.
      rowAffected = this.update.executeUpdate();
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
  
    return rowAffected;    
  }
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/
  
  private int deleteDoc(Document doc)
  {
    // Add conditions that make Document unique.
    final String query = "DELETE FROM "+this.tablename+" WHERE uid=? AND hash=? and canonical_path=?";
    int rowsAffected = 0;
    try
    {
      this.delete = this.conn.connection.prepareStatement(query);
      
      int i=1; // Order must match with query.
      this.delete.setInt   (i++, doc.uid);
      this.delete.setString(i++, doc.hash);
      this.delete.setString(i++, doc.canonical_path);
      
      rowsAffected = this.delete.executeUpdate();
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    return rowsAffected;    
  }
  
  
  private boolean isHashExists(final String hash)
  {
    return this.isStringExists("hash", hash);
  }

  
  private boolean isStringExists(String columnName, String value)
  {
    final String query = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", this.tablename, columnName);
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      this.select.setString(1, value);
      
      ResultSet resultSet =  this.select.executeQuery();
      
      if(resultSet.next())
      {
        int count = resultSet.getInt(1);
        if(count>0)
          return true;
        else
          return false;        
      }
      else
        return false;

    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return false;
  }
  
  
 
  private String getString(String returnColumn, String findColumn, String findValue)
  {
    String returnValue = null;
    
    final String query = String.format("SELECT %s FROM %s WHERE %s = ?", returnColumn, this.tablename, findColumn);
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      this.select.setString(1, findValue);
      
      ResultSet resultSet =  this.select.executeQuery();
      
      if(resultSet.next())
      {
        returnValue = resultSet.getString(1);
      }
      
      if(returnValue == null)
      {
        String e = String.format("Result not found: SELECT %s FROM %s WHERE %s = %s", returnColumn, this.tablename, findColumn, findValue);
        throw new RuntimeException(e);
      }
      return returnValue;
    }
    catch(SQLException e)
    {
      e.printStackTrace();
      return returnValue;
    }
  }
  
  /**
   * 
   * @param column
   * @param value
   * @return {@link Document}
   */
  private Document findDocBy(String column, String value)
  {
    Document doc = null;
    
    final String query = String.format("SELECT duid, canonical_path, filename, last_modified, hash, comment "
                                        + " FROM %s "
                                        + "WHERE %s = ?", this.tablename, column);
    
    try
    {
      this.select = this.conn.connection.prepareStatement(query);
      
      int i=1;
      this.select.setString(i++, value);
      
Chronometer c = new Chronometer();
c.start();       
      ResultSet resultSet =  this.select.executeQuery();
c.stop();
long runTime = c.getRuntime(0, c.getNumberOfStops()-1);
if(runTime>10)
  System.out.println(String.format("SELECT = %,dms | Trash.findDocBy()=%s", c.getRuntime(0, c.getNumberOfStops()-1), query));

      if(resultSet.next())
      {
        doc = new Document();
        int j=1;
        doc.uid             = resultSet.getInt(j++); // Shelf.uid is equal to Trash.duid.
        doc.canonical_path  = resultSet.getString(j++);
        doc.filename        = resultSet.getString(j++);
        doc.last_modified   = resultSet.getLong(j++);
        doc.hash            = resultSet.getString(j++);
        doc.comment         = resultSet.getString(j++);
        
        return doc;
      }
      else
        return doc;

    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
    
    return doc;
  }
  
  /**
   * Insert a document.
   * @param doc
   * @return Generated key. Otherwise, 0 for failure.
   */
  private final int insertDoc(final Document doc)
  {
    doc.checkUid();
    doc.sanityCheck();
    
    final String query = "INSERT INTO "+this.tablename+  "(duid, canonical_path, filename, last_modified, hash, comment) VALUES(?, ?, ?, ?, ?, ?)";
    
    int generatedKey = 0;
    try
    {
      // Prepare the query.
      this.insert = this.conn.connection.prepareStatement(query);
      
      // Set the data.
      int i=1; // Order must match with query.
      this.insert.setInt   (i++, doc.uid);
      this.insert.setString(i++, doc.canonical_path);
      this.insert.setString(i++, doc.filename);
      this.insert.setLong  (i++, doc.last_modified);
      this.insert.setString(i++, doc.hash);
      this.insert.setString(i++, doc.comment);

Chronometer c = new Chronometer();
c.start();      
      // Insert row.
      this.insert.executeUpdate();
      ResultSet resultSet =  this.insert.getGeneratedKeys();
      if(resultSet.next())
      {
        generatedKey = resultSet.getInt(1);
 
      }
c.stop();
long runTime = c.getRuntime(0, c.getNumberOfStops()-1);
if(runTime>10)
  System.out.println(String.format("INSERT = %,dms | Trash.insertDoc()=%s", c.getRuntime(0, c.getNumberOfStops()-1), doc.canonical_path));

    }
    catch(SQLException e)
    {
      if(e.getMessage().indexOf("not unique")!=-1)
      {
        System.err.println(String.format("WARNING: [%s] already exists in database!", doc.filename));
      }
      else
      {
        e.printStackTrace();
      }
    }
    finally
    {
      try
      {
        if(this.insert!=null)
          this.insert.close();
      }
      catch(SQLException ex) 
      {
        RuntimeException rException = new RuntimeException();
        rException.setStackTrace(ex.getStackTrace());
        throw rException;
      }
    }    
  
    return generatedKey;
  }
  
  
  /**
   *   
   * @return Create table query.
   */
  private String createTableQuery()
  {
    return  "CREATE TABLE "+tablename+" ("
                + "uid            INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "duid           INTEGER NOT NULL, " // Document UID
                + "canonical_path TEXT NOT NULL, "
                + "filename       TEXT NOT NULL, "
                + "last_modified  INTEGER NOT NULL, " // Optimization: Rerun same directories but files have changed since last run.
                + "hash           TEXT, "
                + "comment        TEXT "
                + ")";
  }
  
  private void createIndices()
  {
    String[] indices={"CREATE INDEX trash_hash ON "+this.tablename+" (hash);",
                      "CREATE INDEX trash_canonical_path ON "+this.tablename+" (canonical_path);"};
    for(String query: indices)
    {
      this.conn.executeUpdate(query);
    }
  }
  
}
