package interfaces;

import java.util.List;

import file.UbiquityFileData;

public interface View {

	public void fileBackupSuccess(boolean success, String filepath);
	public void fileShareSuccess(boolean success, String filepath, List<String> users);
	public void login();
	public void loginSuccess(boolean success, String username, String passwordHash, String message);
	public void getFilesByTypeSuccess(int filetypeid, UbiquityFileData[] files);
}
