package message

sealed trait AsyncMessage

case class AuthRequest extends AsyncMessage
case class AuthAccept extends AsyncMessage
case class AuthReject extends AsyncMessage
case class AuthBlock extends AsyncMessage
case class AuthSending(username: String, password: String) extends AsyncMessage
case class NotLoggedIn extends AsyncMessage

case class FileRequest extends AsyncMessage
//TODO: what is the payload?
case class FileRequestAck(port: Int, payload: String) extends AsyncMessage

case class IndexRequest extends AsyncMessage
//TODO: what is the payload?
case class IndexRequestAck(port: Int, payload: String) extends AsyncMessage
case class IndexSending(absolutePath: String) extends AsyncMessage

case class DeviceInfo(name: String, os:String) extends AsyncMessage
case class DeviceInfoRequest extends AsyncMessage
case class DeviceNotSupported(name: String, os: String) extends AsyncMessage

case class Index extends AsyncMessage
case class Cache extends AsyncMessage
case class Backup extends AsyncMessage