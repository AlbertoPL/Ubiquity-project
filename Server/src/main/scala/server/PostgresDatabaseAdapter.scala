package server

import java.io._
import java.util.Properties
import java.sql.ResultSet
import java.sql.DriverManager
import java.sql.Connection
import java.sql.SQLException
import java.sql.PreparedStatement

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
    var result = false;

    var conn: Connection = null;
    var login: PreparedStatement = null;

    try {
      conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
        PostgresDatabaseAdapter.props)
    } catch {
      case e: SQLException => e.printStackTrace
    }

    var loginString = "SELECT \"name\" FROM \"user\" WHERE \"name\" = ? " +
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
          result = true;
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
          path = path.replace("\\\\", "\\").trim
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

    if (conn != null) {
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
    exists
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
}
