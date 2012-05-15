package server

import java.io._
import java.security.MessageDigest
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types
import java.util.Properties
import scala.collection.JavaConversions._
import org.postgresql.util.PSQLException
import util.BaseConversion
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap
import file.UbiquityFileData

object PostgresDatabaseAdapter {
  val props = new Properties
  props.setProperty("user", "postgres")
  props.setProperty("password", "bob")
}

class PostgresDatabaseAdapter extends DatabaseAdapter {

  try {
    Class.forName("org.postgresql.Driver")
  } catch {
    case e: ClassNotFoundException => e.printStackTrace
  }

  override def login(username: String, passwordHash: String) = {

    println("Logging user " + username + " in");
    var result:Int = 0;

    var conn: Connection = null;
    var login: PreparedStatement = null;

    try {
      conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
        PostgresDatabaseAdapter.props)
    } catch {
      case e: SQLException => e.printStackTrace
    }

    var loginString = "SELECT \"userid\" FROM \"users\" WHERE \"username\" = ? " +
      "AND \"password\" = ?"

    if (conn != null) {
      try {
        login = conn.prepareStatement(loginString)
        conn.setAutoCommit(false)
      } catch {
        case sqle: SQLException => sqle.printStackTrace
      }

      try {
        login.setString(1, username);
        login.setString(2, passwordHash);
        var rs = login.executeQuery();
        conn.commit();
        if (rs.next()) {
          result = rs.getInt(1);
          println("RESULT: " + result)
        }
      } catch {
        case sqle: SQLException => sqle.printStackTrace
      }

      try {
        if (login != null) {
          login.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch {
        case sqle: SQLException => sqle.printStackTrace
      }
    }
    result
  }

  override def storeIndexInDatabase(username: String, filename: String,
    deviceName: String) {
    var t = new Thread(new Runnable() {

      override def run() {
        System.out.println("Storing index in database")
        var start = System.currentTimeMillis

        var conn: Connection = null
        var insertIndexFiles: PreparedStatement = null
        var checkIndex: PreparedStatement = null

        try {
          conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
            PostgresDatabaseAdapter.props);
        } catch {
          case sqle: SQLException => sqle.printStackTrace
        }

        var insertString = "INSERT INTO file(\"name\", device, path, " +
          "\"user\", \"type\", size) values(?,?,?,?,?,?)"
        var checkString = "SELECT path FROM file WHERE path = ?"

        if (conn != null) {
          try {
            insertIndexFiles = conn.prepareStatement(insertString)
            checkIndex = conn.prepareStatement(checkString)
            conn.setAutoCommit(false)
          } catch {
            case sqle: SQLException => sqle.printStackTrace
          }

          var read: BufferedReader = null
          try {
            read = new BufferedReader(new FileReader(filename))
          } catch {
            case fnfe: FileNotFoundException => fnfe.printStackTrace
          }
          if (read != null) {
            var line = ""
            try {
              line = read.readLine
              while (line != null) {
                line = read.readLine
                var nameOfFile = line.substring(1, line.length() - 1)
                line = read.readLine
                var firstPath = line.substring(1, line.length() - 1)
                line = read.readLine
                var fileType = line.substring(1, line.length() - 1)
                line = read.readLine.trim
                var size: Long = java.lang.Long.parseLong(line)

                //first check to see if the path already exists in the database
                try {
                  checkIndex.setString(1, firstPath);
                  var rs = checkIndex.executeQuery
                  conn.commit
                  if (!rs.next()) {
                    insertIndexFiles.setString(1, nameOfFile)
                    insertIndexFiles.setString(2, deviceName)
                    insertIndexFiles.setString(3, firstPath)
                    insertIndexFiles.setString(4, username)
                    insertIndexFiles.setString(5, fileType)
                    insertIndexFiles.setLong(6, size)
                    insertIndexFiles.executeUpdate
                    conn.commit
                  }
                } catch {
                  case sqle: SQLException =>
                    System.out.println(firstPath)
                    sqle.printStackTrace
                    try {
                      System.err.println(
                        "Transaction is being rolled back: check firstPath")
                      conn.rollback
                    } catch {
                      case nested: SQLException => nested.printStackTrace
                    }
                }
                line = read.readLine
              }
            } catch {
              case ioe: IOException => ioe.printStackTrace
              case aioobe: ArrayIndexOutOfBoundsException =>
                aioobe.printStackTrace
            }
            try {
              read.close
            } catch {
              case ioe: IOException => ioe.printStackTrace
            }
          }
        }
        try {
          if (insertIndexFiles != null) {
            insertIndexFiles.close
          }
          if (conn != null) {
            conn.close
          }
        } catch {
          case sqle: SQLException => sqle.printStackTrace
        }
        var end = System.currentTimeMillis
        System.out.println("Time taken to index everything (seconds): " +
          ((end - start) / 1000.0))
      }

    });
    t.start
  }

  override def selectAllFilesFromUser(username: String) = {
    var objects: Vector[Array[String]] = Vector.empty[Array[String]]
    var conn: Connection = null;
    var getIndex: PreparedStatement = null;

    try {
      conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
        PostgresDatabaseAdapter.props)
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }
    var selectString = "SELECT * FROM file WHERE \"user\" = ?"

    if (conn != null) {
      try {
        getIndex = conn.prepareStatement(selectString)
        conn.setAutoCommit(false)
      } catch {
        case sqle: SQLException => sqle.printStackTrace
      }

      try {
        getIndex.setString(1, username)
        var rs = getIndex.executeQuery
        conn.commit
        while (rs.next) {
          var path = rs.getString("path")
          //path = path.replace("\\\\", "\\").trim
          objects :+= Array[String](rs.getString("device"),
            rs.getString("name").trim, path, rs.getString("type").trim)
        }
      } catch {
        case sqle: SQLException =>
          sqle.printStackTrace
          try {
            System.err.println(
              "Transaction is being rolled back: check firstPath")
            conn.rollback
          } catch {
            case sqle: SQLException => sqle.printStackTrace
          }
      }
    }
    try {
      if (getIndex != null) {
        getIndex.close
      }
      if (conn != null) {
        conn.close
      }
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }

    objects
  }

  override def userExists(username: String) = {
    var exists = false
    var conn: Connection = null
    var getUser: PreparedStatement = null

    try {
      conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
        PostgresDatabaseAdapter.props)
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }
    var getString = "SELECT * FROM \"user\" WHERE \"name\" = ?"
    var getBetaString = "SELECT * FROM betasignup WHERE username = ?"

    var selectUserIdByUsernameProc: CallableStatement = conn.prepareCall("{ ? = call selectuseridbyusername( ? ) }");
    selectUserIdByUsernameProc.registerOutParameter(1, Types.INTEGER);
    selectUserIdByUsernameProc.setString(2, username);
    selectUserIdByUsernameProc.execute();
    var id: Int = selectUserIdByUsernameProc.getInt(1);
    selectUserIdByUsernameProc.close();  
    
    if (id > 0) {
      exists = true
    }
    exists  
    
    /*if (conn != null) {
      try {
        getUser = conn.prepareStatement(getString)
        conn.setAutoCommit(false)
      } catch {
        case sqle: SQLException => sqle.printStackTrace
      }

      try {
        getUser.setString(1, username)
        var rs = getUser.executeQuery
        conn.commit
        if (rs.next) {
          exists = true
        }
      } catch {
        case sqle: SQLException =>
          sqle.printStackTrace
          try {
            System.err.println(
              "Transaction is being rolled back: check username")
            conn.rollback
          } catch {
            case nested: SQLException => nested.printStackTrace
          }
      }
      try {
        getUser = conn.prepareStatement(getBetaString);
        conn.setAutoCommit(false);
      } catch {
        case sqle: SQLException => sqle.printStackTrace
      }

      try {
        getUser.setString(1, username)
        var rs = getUser.executeQuery
        conn.commit
        if (rs.next) {
          exists = true
        }
      } catch {
        case sqle: SQLException =>
          sqle.printStackTrace
          try {
            System.err.println(
              "Transaction is being rolled back: check username")
            conn.rollback
          } catch {
            case nested: SQLException => nested.printStackTrace
          }
      }

    }
    try {
      if (getUser != null) {
        getUser.close
      }
      if (conn != null) {
        conn.close
      }
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }
    exists*/
  }

  override def betaSignup(username: String, email: String) = {
    var success = false
    var conn: Connection = null
    var insertString: PreparedStatement = null

    try {
      conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
        PostgresDatabaseAdapter.props)
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }
    var getString = "INSERT INTO betasignup (username, email) values(?,?)"

    if (conn != null) {
      try {
        insertString = conn.prepareStatement(getString)
        conn.setAutoCommit(false)
      } catch {
        case sqle: SQLException => sqle.printStackTrace
      }

      try {
        insertString.setString(1, username)
        insertString.setString(2, email)
        insertString.executeUpdate
        conn.commit
        success = true;
      } catch {
        case sqle: SQLException =>
          sqle.printStackTrace
          try {
            System.err.println(
              "Transaction is being rolled back: check username or email")
            conn.rollback
          } catch {
            case sqle: SQLException => sqle.printStackTrace
          }
      }
    }
    try {
      if (insertString != null) {
        insertString.close
      }
      if (conn != null) {
        conn.close
      }
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }
    success
  }

  override def getFileSize(username: String, filepath: String,
    deviceName: String) = {
    var size = 0L
    var conn: Connection = null
    var selectString: PreparedStatement = null

    try {
      conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
        PostgresDatabaseAdapter.props);
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }
    var getString = "SELECT size FROM file where \"user\"=? and path=? " +
      "and device=?";

    if (conn != null) {
      try {
        selectString = conn.prepareStatement(getString)
        conn.setAutoCommit(false)
      } catch {
        case sqle: SQLException => sqle.printStackTrace
      }

      try {
        selectString.setString(1, username)
        selectString.setString(2, filepath)
        selectString.setString(3, deviceName)
        var rs = selectString.executeQuery
        conn.commit
        if (rs.next) {
          size = rs.getLong(1)
        }
      } catch {
        case sqle: SQLException =>
          sqle.printStackTrace
          try {
            System.err.println(
              "Transaction is being rolled back: check username or email")
            conn.rollback
          } catch {
            case sqle: SQLException => sqle.printStackTrace
          }
      }
    }
    try {
      if (selectString != null) {
        selectString.close
      }
      if (conn != null) {
        conn.close
      }
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }
    size
  }
  
  override def insertDevice(devicename: String, macaddr: String, userid: Int) = {
    var conn: Connection = null

    try {
      conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
        PostgresDatabaseAdapter.props)
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }

    var id: Int = -1
    var devices: Array[String] = new Array[String](1)
    
    try {
    	devices = getDevices(userid)
    }
    catch {
	      case sqle: PSQLException => sqle.printStackTrace
	    }
    if (!devices.contains(devicename)) {
    
	    var insertdeviceProc: CallableStatement = conn.prepareCall("{ ? = call insertdevice( ?, ?, ?, ? ) }");
	    insertdeviceProc.registerOutParameter(1, Types.INTEGER);
	    insertdeviceProc.setString(2, devicename);
	    System.out.println(userid);
	    insertdeviceProc.setObject(3, macaddr, Types.OTHER);
	    insertdeviceProc.setObject(4, "0.0.0.0", Types.OTHER);
	    insertdeviceProc.setInt(5, userid);
	
	    try {
	    	insertdeviceProc.execute();
	    	id = insertdeviceProc.getInt(1);
	    } catch {
	      case sqle: PSQLException => sqle.printStackTrace
	    }
	    insertdeviceProc.close();  
    }
    
    id
  }
  
  override def getDevices(userid: Int) = {
	var conn: Connection = null
  
	try {
      conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
        PostgresDatabaseAdapter.props)
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }
	
    var devices: Array[String] = Array.empty[String]

    var getdevicesProc: PreparedStatement = conn.prepareStatement("select * from getDevices(?)")
    getdevicesProc.setInt(1,userid);
    
    System.out.println(userid);
    
    try {
	    var rs = getdevicesProc.executeQuery();
	    while (rs.next) {
	      devices :+= rs.getString("devicename")
	    }
    } catch {      
      case sqle: PSQLException => sqle.printStackTrace
    }
    getdevicesProc.close();  
    
	devices
  }
  
  override def storeFileInDatabase(userid: Int, filename: String, filepath: String, filesize: Long, devicename: String) {
    var conn: Connection = null
    var insertFile: PreparedStatement = null
    
    try {
      conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
        PostgresDatabaseAdapter.props)
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }
     var getfileidProc: CallableStatement = conn.prepareCall("{ ? = call getfileid( ?, ?, ?, ?, ?, ? ) }");
        getfileidProc.registerOutParameter(1, Types.BIGINT);
        getfileidProc.setString(2, filename);
        if (filename.lastIndexOf(".uprj").equals(filename.length() - 5)) {//TODO: Make this less hacky
          getfileidProc.setInt(3, 2);
        }
        else {
        	getfileidProc.setInt(3, 1); //TODO: 1 stands for nothing for now
        }
        getfileidProc.setLong(4, filesize);
        getfileidProc.setString(5, BaseConversion.toHexString(
	    MessageDigest.getInstance("SHA").digest(filename.getBytes())));
        getfileidProc.setString(6, "");
        getfileidProc.setInt(7, userid);
	    
        getfileidProc.execute();
        var fileid: Long = getfileidProc.getLong(1);
        getfileidProc.close();  

        var getdevicesProc: PreparedStatement = conn.prepareStatement("select * from getDevices(?)")
	    getdevicesProc.setInt(1, userid);
	
	    var rs = getdevicesProc.executeQuery();
	    var deviceid = -1
	    while (rs.next && deviceid < 0) {
	      var name = rs.getString("devicename")
	      if (name.equalsIgnoreCase(devicename)) {
	        deviceid = rs.getInt("deviceid")	        
	      }
	    }
	    
   	    getdevicesProc.close();  

	    
	    var insertfilepathProc : CallableStatement = conn.prepareCall("{? = call insertfilepath(?, ?, ?) }");
	    
	    insertfilepathProc.registerOutParameter(1, Types.INTEGER);
	    insertfilepathProc.setLong(2, fileid);
	    insertfilepathProc.setInt(3, deviceid);
	    insertfilepathProc.setString(4, filepath);
	    
	    insertfilepathProc.execute();
	    insertfilepathProc.close();
  }
  
  override def getAllFilesOfType(userid: Int, typeid: Int) = {
    var conn: Connection = null
    var getUser: PreparedStatement = null
    var objects: Array[UbiquityFileData] = Array.empty[UbiquityFileData]
    
    try {
      conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
        PostgresDatabaseAdapter.props)
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }

    
    var fileids: Array[Int] = Array.empty[Int]
    var filenames: Array[String] = Array.empty[String]
    var filelengths: Array[Long] = Array.empty[Long]
    
    var getfilesProc: PreparedStatement = conn.prepareCall("select * from selectfilebytype( ?, ? )");
        getfilesProc.setInt(1, userid);
        getfilesProc.setInt(2, typeid);

        System.out.println("Userid: " + userid);
        System.out.println("Typeid: " + typeid);
       try {
	        var rs = getfilesProc.executeQuery();
	       	while (rs.next) {
	       		fileids :+= rs.getInt("fileid")
	       		filenames :+= rs.getString("filename")
	       		filelengths :+= rs.getLong("size")
	       	}
       }
        catch {
        case sqle: PSQLException => sqle.printStackTrace
    }
        getfilesProc.close();
    
    var getdevicesProc: PreparedStatement = conn.prepareStatement("select * from getDevices(?)")
	    getdevicesProc.setInt(1, userid);
	
	    var rs = getdevicesProc.executeQuery();
	    while (rs.next) {
	        var deviceid = rs.getInt("deviceid")
	        for (i <- 0 until fileids.length) {
	            var selectfilepathProc: PreparedStatement = conn.prepareCall("select * from selectfilepath( ?, ? )");
		         selectfilepathProc.setInt(1, fileids(i));
		         selectfilepathProc.setInt(2, deviceid);
		         try {
			        var rs2 = selectfilepathProc.executeQuery();
			       	while (rs2.next) {
			       	    var filedata = new UbiquityFileData(filenames(i), rs2.getString("filepaths"), filelengths(i), userid)
			       		objects :+= filedata
			       	}
		         }
		        catch {
		        	case sqle: PSQLException => sqle.printStackTrace
			    }
		      	selectfilepathProc.close
	        }
	    }    
        getdevicesProc.close
    objects
  }
  
  override def shareFile(userid: Int, usertoshare: String, filename: String, filepath: String, filelength: Long, deviceName: String) {
	   var conn: Connection = null
    var getUser: PreparedStatement = null
    
    try {
      conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
        PostgresDatabaseAdapter.props)
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }
    var getfileid: PreparedStatement = null
    var getdeviceid: PreparedStatement = null
    
    //var insertString = "INSERT INTO file(\"name\", device, path, " +
      //    "\"user\", \"type\", size) values(?,?,?,?,?,?)"
        var getfileidstring = "SELECT fileid FROM filepath WHERE deviceid = ? AND filepath = ?"
        var getdeviceidstring = "SELECT deviceid FROM devices WHERE devicename = ?"
          
        if (conn != null) {
          try {
            //insertIndexFiles = conn.prepareStatement(insertString)
            getdeviceid = conn.prepareStatement(getdeviceidstring)
            getfileid = conn.prepareStatement(getfileidstring)
            conn.setAutoCommit(false)

          }catch {
            case sqle: SQLException => sqle.printStackTrace
          }
          
          getdeviceid.setString(1, deviceName);
          var rs = getdeviceid.executeQuery
          conn.commit
          var deviceid: Int = 0
          var fileid: Int = 0
          while (rs.next) {
            deviceid = rs.getInt("deviceid")
          }
          getdeviceid.close()
          getfileid.setInt(1, deviceid);
          getfileid.setString(2, filepath);
          rs = getfileid.executeQuery();
          conn.commit
          while (rs.next) {
            fileid = rs.getInt("fileid")
          }
          
          getfileid.close()
          
          if (fileid == 0) {
            storeFileInDatabase(userid, filename, filepath, filelength, deviceName)
            shareFile(userid, usertoshare, filename, filepath, filelength, deviceName)
          }
          else {
          var selectuseridbyusernameProc: CallableStatement = conn.prepareCall("{ ? = call selectuseridbyusername( ? ) }");
        selectuseridbyusernameProc.registerOutParameter(1, Types.INTEGER);
        selectuseridbyusernameProc.setString(2, usertoshare);
       
		    
	        selectuseridbyusernameProc.execute();
	        var usertosharewithid: Int = selectuseridbyusernameProc.getInt(1);
	        selectuseridbyusernameProc.close();  
	            var shareFile: PreparedStatement = conn.prepareCall("insert into sharedfile(userid, fileid) values(?,?) ");
	        shareFile.setInt(1, usertosharewithid);
	        shareFile.setInt(2, fileid);
	        shareFile.executeUpdate()
	        conn.commit
	        shareFile.close()
        }
        }
    
  }
  
   override def getAllSharedFiles(userid: Int) = {
      var conn: Connection = null
    var getUser: PreparedStatement = null
    var objects: Array[UbiquityFileData] = Array.empty[UbiquityFileData]
    
    try {
      conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
        PostgresDatabaseAdapter.props)
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }

    
    var fileids: Array[Int] = Array.empty[Int]
    var filenames: Array[String] = Array.empty[String]
    var filelengths: Array[Long] = Array.empty[Long]
    
    var getsharedfilesProc: PreparedStatement = conn.prepareCall("select * from sharedfile where userid = ?");
        getsharedfilesProc.setInt(1, userid);

        System.out.println("Userid: " + userid);
       try {
	        var rs = getsharedfilesProc.executeQuery();
	       	while (rs.next) {
	       		fileids :+= rs.getInt("fileid")
	       	}
       }
        catch {
        case sqle: PSQLException => sqle.printStackTrace
    }
        getsharedfilesProc.close();
    

	for (i <- 0 until fileids.length) {
	      var getfilesProc: PreparedStatement = conn.prepareStatement("select * from files WHERE fileid = ?")

	      getfilesProc.setInt(1, fileids(i));
	      try {
			        var rs2 = getfilesProc.executeQuery();
			       	while (rs2.next) {
				       	    var getuseridProc: PreparedStatement = conn.prepareStatement("select * from userfilereln WHERE fileid = ?");
			       			getuseridProc.setInt(1, fileids(i));
			       			var temprs = getuseridProc.executeQuery();
			       			while (temprs.next) {
					       	    var filedata = new UbiquityFileData(filenames(i), rs2.getString("filepaths"), filelengths(i), temprs.getInt("userid"))
					       		objects :+= filedata
			       			}
			       	}
		         }
	      catch {
		        	case sqle: PSQLException => sqle.printStackTrace
			    }
		      	getfilesProc.close
	}   
    objects
    }
    
    override def getAllSharedFilesOfType(userid: Int, typeid: Int) = {
      var conn: Connection = null
    var getUser: PreparedStatement = null
    var objects: Array[UbiquityFileData] = Array.empty[UbiquityFileData]
    
    try {
      conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
        PostgresDatabaseAdapter.props)
    } catch {
      case sqle: SQLException => sqle.printStackTrace
    }

    
    var fileids: Array[Int] = Array.empty[Int]
    var filenames: Array[String] = Array.empty[String]
    var filelengths: Array[Long] = Array.empty[Long]
    
    var getsharedfilesProc: PreparedStatement = conn.prepareCall("select * from sharedfile where userid = ?");
        getsharedfilesProc.setInt(1, userid);

        System.out.println("Userid: " + userid);
       try {
	        var rs = getsharedfilesProc.executeQuery();
	       	while (rs.next) {
	       		fileids :+= rs.getInt("fileid")
	       	}
       }
        catch {
        case sqle: PSQLException => sqle.printStackTrace
    }
        getsharedfilesProc.close();
    

	for (i <- 0 until fileids.length) {
	      var getfilesProc: PreparedStatement = conn.prepareStatement("select * from files WHERE fileid = ?")

	      getfilesProc.setInt(1, fileids(i));
	      try {
			        var rs2 = getfilesProc.executeQuery();
			       	while (rs2.next) {
			       	    if (rs2.getInt("typeid") == typeid) {
			       	      var getuseridProc: PreparedStatement = conn.prepareStatement("select * from userfilereln WHERE fileid = ?");
			       			getuseridProc.setInt(1, fileids(i));
			       			var temprs = getuseridProc.executeQuery();
			       			while (temprs.next) {
					       	    var filedata = new UbiquityFileData(filenames(i), rs2.getString("filepaths"), filelengths(i), temprs.getInt("userid"))
					       		objects :+= filedata
			       			}
			       	    }
			       	}
		         }
	      catch {
		        	case sqle: PSQLException => sqle.printStackTrace
			    }
		      	getfilesProc.close
	}   
    objects
    }
}
